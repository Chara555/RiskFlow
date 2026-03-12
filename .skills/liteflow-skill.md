# LiteFlow 规则引擎使用 Skill

## 概述

LiteFlow 是一个轻量级、快速、稳定、可编排的组件式规则引擎/流程引擎，专为简化复杂业务逻辑编排而生。它采用独特的工作台模式设计理念，将业务逻辑拆分为独立的组件，通过规则文件进行灵活编排。

### 核心设计理念

LiteFlow 基于**工作台模式**设计：
- **工人** = 组件（Component）
- **工作台** = 上下文（Context/Slot）
- **零件** = 数据参数
- **机器** = 完整业务流程

这种设计的优势：
1. **解耦**：组件之间无需直接沟通，只关心工作台资源
2. **稳定**：组件位置调换不影响工作内容
3. **复用**：同一组件可在不同工作台复用
4. **灵活**：运行时动态增删改组件

### 适用场景

| 适用场景 | 不适用场景 |
|---------|-----------|
| 复杂业务逻辑编排（价格引擎、下单流程） | 基于角色任务的审批流（推荐 flowlong/flowable） |
| 需要频繁变更业务流程 | 简单 CRUD 操作 |
| 高内聚低耦合的系统设计 | 纯数据查询场景 |
| 需要热更新规则的业务 | |

---

## 一、快速开始

### 1.1 添加依赖

**Spring Boot 场景：**

```xml
<dependency>
    <groupId>com.yomahub</groupId>
    <artifactId>liteflow-spring-boot-starter</artifactId>
    <version>2.15.3</version>
</dependency>
```

**非 Spring 场景：**

```xml
<dependency>
    <groupId>com.yomahub</groupId>
    <artifactId>liteflow-core</artifactId>
    <version>2.15.3</version>
</dependency>
```

### 1.2 定义组件

```java
// 普通组件 - 串行/并行执行
@LiteflowComponent("validateOrder")
public class ValidateOrderComponent extends NodeComponent {
    @Override
    public void process() {
        // 获取上下文
        OrderContext context = this.getContextBean(OrderContext.class);
        
        // 执行业务逻辑
        System.out.println("验证订单: " + context.getOrderNo());
        
        // 设置结果到上下文
        context.setValid(true);
    }
    
    // 可选：控制是否进入该节点
    @Override
    public boolean isAccess() {
        OrderContext context = this.getContextBean(OrderContext.class);
        return context.getOrderNo() != null;
    }
    
    // 可选：出错时是否继续执行下一个组件（默认 false）
    @Override
    public boolean isContinueOnError() {
        return false;
    }
}

// 选择组件 - 类似 switch
@LiteflowComponent("checkUserType")
public class CheckUserTypeComponent extends NodeSwitchComponent {
    @Override
    public String processSwitch() {
        OrderContext context = this.getContextBean(OrderContext.class);
        // 返回要执行的节点ID
        return context.isVip() ? "vipProcess" : "normalProcess";
    }
}

// 布尔组件 - 条件判断
@LiteflowComponent("isPremium")
public class IsPremiumComponent extends NodeBooleanComponent {
    @Override
    public boolean processBoolean() {
        OrderContext context = this.getContextBean(OrderContext.class);
        return context.getAmount() > 1000;
    }
}
```

### 1.3 配置规则文件

**XML 格式（推荐）：**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<flow>
    <chain name="orderProcessChain">
        THEN(
            validateOrder,
            checkInventory,
            IF(isPremium, applyDiscount),
            calculatePrice,
            WHEN(sendSms, sendEmail, sendAppNotify),
            createOrder
        );
    </chain>
    
    <chain name="vipOrderChain">
        THEN(
            validateOrder,
            SWITCH(checkUserType).to(vipProcess, normalProcess)
        );
    </chain>
</flow>
```

**YAML 格式：**

```yaml
liteflow:
  rules:
    - chainName: "orderProcessChain"
      content: |
        THEN(
          validateOrder,
          checkInventory,
          WHEN(sendSms, sendEmail)
        )
```

**JSON 格式：**

```json
{
  "chain": [
    {
      "name": "orderProcessChain",
      "value": "THEN(validateOrder, checkInventory, WHEN(sendSms, sendEmail))"
    }
  ]
}
```

### 1.4 Spring Boot 配置

```properties
# application.properties
# 规则文件路径
liteflow.rule-source=config/flow.xml

# 开启文件监控热刷新
liteflow.enable-monitor-file=true

# 打印执行日志
liteflow.print-execution-log=true

# 失败重试次数
liteflow.retry-count=0

# 上下文槽位数量（默认1024）
liteflow.slot-size=1024

# 并行执行线程池配置
liteflow.main-executor-works=64
liteflow.when-max-wait-seconds=15
liteflow.when-max-workers=16
liteflow.when-queue-limit=5120
```

### 1.5 执行流程

```java
@Service
public class OrderService {
    
    @Resource
    private FlowExecutor flowExecutor;
    
    public void processOrder(OrderRequest request) {
        // 创建上下文
        OrderContext context = new OrderContext();
        context.setOrderNo(request.getOrderNo());
        context.setUserId(request.getUserId());
        context.setAmount(request.getAmount());
        
        // 执行流程
        LiteflowResponse response = flowExecutor.execute2Resp(
            "orderProcessChain",  // 规则链名称
            request,              // 初始参数（可选）
            context               // 上下文对象
        );
        
        // 处理结果
        if (response.isSuccess()) {
            System.out.println("订单处理成功");
            // 从上下文获取结果
            OrderContext resultContext = response.getFirstContextBean();
            System.out.println("最终结果: " + resultContext.getResult());
        } else {
            System.err.println("订单处理失败: " + response.getMessage());
            Exception e = response.getCause();
            // 处理异常
        }
    }
}
```

---

## 二、组件类型详解

### 2.1 组件类型对照表

| 组件类型 | 继承类 | 可用关键字 | 用途 |
|---------|--------|-----------|------|
| **普通组件** | `NodeComponent` | THEN, WHEN | 基础业务单元 |
| **选择组件** | `NodeSwitchComponent` | SWITCH | 根据返回值路由 |
| **布尔组件** | `NodeBooleanComponent` | IF, ELIF, ELSE, WHILE | 条件判断 |
| **次数循环组件** | `NodeForComponent` | FOR...DO... | 固定次数循环 |
| **迭代循环组件** | `NodeIteratorComponent` | ITERATOR...DO... | 集合迭代循环 |

### 2.2 普通组件（NodeComponent）

```java
@LiteflowComponent("myComponent")
public class MyComponent extends NodeComponent {
    
    @Override
    public void process() {
        // 核心业务逻辑
        MyContext context = this.getContextBean(MyContext.class);
        // ... 业务处理
    }
    
    // ========== 生命周期方法（可选覆盖） ==========
    
    @Override
    public void beforeProcess() {
        // 执行前回调
    }
    
    @Override
    public void afterProcess() {
        // 执行后回调
    }
    
    @Override
    public void onError(Exception e) {
        // 异常处理
    }
    
    // ========== 控制方法（可选覆盖） ==========
    
    @Override
    public boolean isAccess() {
        // 是否执行该组件（默认 true）
        return true;
    }
    
    @Override
    public boolean isContinueOnError() {
        // 出错时是否继续执行下一个（默认 false）
        return false;
    }
    
    @Override
    public boolean isEnd() {
        // 是否终止整个流程（默认 false）
        return false;
    }
}
```

### 2.3 选择组件（NodeSwitchComponent）

```java
@LiteflowComponent("routeByType")
public class RouteByTypeComponent extends NodeSwitchComponent {
    
    @Override
    public String processSwitch() {
        OrderContext context = this.getContextBean(OrderContext.class);
        String orderType = context.getOrderType();
        
        // 返回目标节点ID
        switch (orderType) {
            case "NORMAL": return "normalHandler";
            case "VIP": return "vipHandler";
            case "ENTERPRISE": return "enterpriseHandler";
            default: return "defaultHandler";
        }
    }
}
```

**规则文件中使用：**

```xml
<chain name="orderRouteChain">
    THEN(
        validateOrder,
        SWITCH(routeByType).to(normalHandler, vipHandler, enterpriseHandler, defaultHandler),
        sendNotification
    );
</chain>
```

### 2.4 布尔组件（NodeBooleanComponent）

```java
@LiteflowComponent("checkStock")
public class CheckStockComponent extends NodeBooleanComponent {
    
    @Override
    public boolean processBoolean() {
        OrderContext context = this.getContextBean(OrderContext.class);
        // 返回 true/false
        return inventoryService.hasEnoughStock(context.getSkuId(), context.getQuantity());
    }
}
```

**规则文件中使用：**

```xml
<chain name="stockCheckChain">
    THEN(
        validateOrder,
        IF(checkStock, 
            THEN(deductStock, createOrder),
            notifyOutOfStock
        )
    );
</chain>
```

### 2.5 循环组件

**次数循环组件：**

```java
@LiteflowComponent("retryCount")
public class RetryCountComponent extends NodeForComponent {
    @Override
    public int processFor() {
        // 返回循环次数
        return 3;
    }
}
```

**迭代循环组件：**

```java
@LiteflowComponent("itemIterator")
public class ItemIteratorComponent extends NodeIteratorComponent {
    @Override
    public Iterator<?> processIterator() {
        OrderContext context = this.getContextBean(OrderContext.class);
        return context.getOrderItems().iterator();
    }
}
```

**规则文件中使用：**

```xml
<chain name="processItemsChain">
    THEN(
        validateOrder,
        FOR(retryCount).DO(processItem),
        ITERATOR(itemIterator).DO(calculateItemPrice),
        sumTotalPrice
    );
</chain>
```

---

## 三、EL 规则表达式详解

### 3.1 串行编排（THEN）

```xml
<!-- 简单串行 -->
<chain name="simpleChain">
    THEN(a, b, c, d);
</chain>

<!-- 嵌套串行 -->
<chain name="nestedChain">
    THEN(a, THEN(b, c), d);
</chain>
```

### 3.2 并行编排（WHEN）

```xml
<!-- 简单并行 -->
<chain name="parallelChain">
    WHEN(a, b, c);
</chain>

<!-- 串行+并行混合 -->
<chain name="mixedChain">
    THEN(a, WHEN(b, c, d), e);
</chain>

<!-- 并行后串行（等待所有并行完成） -->
<chain name="waitChain">
    THEN(WHEN(a, b), c);
</chain>
```

**并行配置：**

```properties
# 开启并行线程池隔离
liteflow.when-thread-pool-isolate=true

# 并行最大等待时间（秒）
liteflow.when-max-wait-seconds=15
```

### 3.3 条件编排（IF）

```xml
<!-- 简单条件 -->
<chain name="ifChain">
    IF(condition, thenNode, elseNode);
</chain>

<!-- 多条件判断 -->
<chain name="multiIfChain">
    IF(cond1, node1, ELIF(cond2, node2, ELIF(cond3, node3, defaultNode)));
</chain>

<!-- 条件+串行 -->
<chain name="ifThenChain">
    THEN(IF(isVip, vipDiscount), processPayment);
</chain>
```

### 3.4 选择编排（SWITCH）

```xml
<!-- 简单选择 -->
<chain name="switchChain">
    SWITCH(selector).to(a, b, c);
</chain>

<!-- 选择+后续流程 -->
<chain name="switchThenChain">
    THEN(
        validate,
        SWITCH(routeByType).to(typeA, typeB, typeC),
        finalize
    );
</chain>

<!-- 选择返回标签 -->
<chain name="switchTagChain">
    SWITCH(selector).to(
        THEN(a, b).id("option1"),
        THEN(c, d).id("option2")
    );
</chain>
```

### 3.5 循环编排

```xml
<!-- 固定次数循环 -->
<chain name="forChain">
    FOR(5).DO(processNode);
</chain>

<!-- 使用组件返回次数 -->
<chain name="forCmpChain">
    FOR(countProvider).DO(processNode);
</chain>

<!-- 条件循环 -->
<chain name="whileChain">
    WHILE(conditionChecker).DO(processNode);
</chain>

<!-- 迭代循环 -->
<chain name="iteratorChain">
    ITERATOR(itemProvider).DO(processItem);
</chain>

<!-- 带Break的循环 -->
<chain name="breakChain">
    FOR(10).DO(processNode).BREAK(breakChecker);
</chain>
```

### 3.6 高级编排技巧

**前置/后置组件：**

```xml
<chain name="prePostChain">
    THEN(
        PRE(logStart),
        businessLogic,
        POST(logEnd)
    );
</chain>
```

**子流程调用：**

```xml
<chain name="mainChain">
    THEN(
        step1,
        chain("subChain"),  <!-- 调用其他链 -->
        step3
    );
</chain>

<chain name="subChain">
    THEN(subStep1, subStep2);
</chain>
```

**节点标签（用于返回标识）：**

```xml
<chain name="tagChain">
    THEN(
        a,
        THEN(b, c).id("section1"),
        d
    );
</chain>
```

---

## 四、数据上下文与数据传递

### 4.1 上下文定义

```java
// 自定义上下文 - 普通 POJO
@Data
public class OrderContext {
    // 输入参数
    private String orderNo;
    private Long userId;
    private BigDecimal amount;
    private List<OrderItem> orderItems;
    
    // 中间结果
    private boolean valid;
    private BigDecimal discountAmount;
    
    // 最终结果
    private OrderResult result;
}
```

### 4.2 在组件中访问上下文

```java
@LiteflowComponent("example")
public class ExampleComponent extends NodeComponent {
    
    @Override
    public void process() {
        // 方式1：获取指定类型的上下文
        OrderContext context = this.getContextBean(OrderContext.class);
        
        // 方式2：获取默认上下文
        DefaultContext defaultContext = this.getFirstContextBean();
        
        // 方式3：获取所有上下文
        List<Object> allContexts = this.getContextBeanList();
        
        // 读取数据
        String orderNo = context.getOrderNo();
        
        // 写入数据
        context.setValid(true);
    }
}
```

### 4.3 多上下文支持

```java
// 执行时传入多个上下文
LiteflowResponse response = flowExecutor.execute2Resp(
    "chain1",
    request,
    OrderContext.class,
    UserContext.class,
    InventoryContext.class
);

// 在组件中获取特定上下文
OrderContext orderCtx = this.getContextBean(OrderContext.class);
UserContext userCtx = this.getContextBean(UserContext.class);
```

### 4.4 请求参数传递

```java
// 第一个参数会传递给第一个组件
LiteflowResponse response = flowExecutor.execute2Resp(
    "chain1",
    "初始参数",  // 通过 this.getRequestData() 获取
    OrderContext.class
);

// 在组件中获取
@Override
public void process() {
    Object requestData = this.getRequestData();
    // 通常第一个组件将 requestData 设置到上下文中
    OrderContext context = this.getContextBean(OrderContext.class);
    context.setOrderNo((String) requestData);
}
```

---

## 五、声明式组件（方法级别）

从 v2.9.0+ 开始支持方法级别声明，避免类过多的问题。

```java
@LiteflowComponent
public class CmpConfig {

    // 普通组件
    @LiteflowMethod(
        value = LiteFlowMethodEnum.PROCESS,
        nodeId = "validateOrder",
        nodeName = "验证订单",
        nodeType = NodeTypeEnum.COMMON
    )
    public void processValidate(NodeComponent bindCmp) {
        OrderContext context = bindCmp.getContextBean(OrderContext.class);
        // 验证逻辑
    }

    // SWITCH组件
    @LiteflowMethod(
        value = LiteFlowMethodEnum.PROCESS_SWITCH,
        nodeId = "routeByType",
        nodeName = "路由选择",
        nodeType = NodeTypeEnum.SWITCH
    )
    public String processRoute(NodeComponent bindCmp) {
        OrderContext context = bindCmp.getContextBean(OrderContext.class);
        return context.isVip() ? "vipHandler" : "normalHandler";
    }

    // 布尔组件
    @LiteflowMethod(
        value = LiteFlowMethodEnum.PROCESS_BOOLEAN,
        nodeId = "checkStock",
        nodeName = "检查库存",
        nodeType = NodeTypeEnum.BOOLEAN
    )
    public boolean processCheckStock(NodeComponent bindCmp) {
        OrderContext context = bindCmp.getContextBean(OrderContext.class);
        return checkInventory(context.getSkuId());
    }

    // FOR组件
    @LiteflowMethod(
        value = LiteFlowMethodEnum.PROCESS_FOR,
        nodeId = "getRetryCount",
        nodeName = "获取重试次数",
        nodeType = NodeTypeEnum.FOR
    )
    public int processRetryCount(NodeComponent bindCmp) {
        return 3;
    }

    // ITERATOR组件
    @LiteflowMethod(
        value = LiteFlowMethodEnum.PROCESS_ITERATOR,
        nodeId = "getItems",
        nodeName = "获取订单项",
        nodeType = NodeTypeEnum.ITERATOR
    )
    public Iterator<?> processItems(NodeComponent bindCmp) {
        OrderContext context = bindCmp.getContextBean(OrderContext.class);
        return context.getOrderItems().iterator();
    }
}
```

---

## 六、脚本组件支持

LiteFlow 支持使用脚本语言编写组件逻辑，实现动态业务逻辑。

### 6.1 支持的脚本语言

| 语言 | 依赖 | 用途 |
|-----|------|-----|
| Groovy | liteflow-script-groovy | 复杂业务计算 |
| JavaScript | liteflow-script-javascript | 前端友好 |
| Python | liteflow-script-python | 数据科学场景 |
| QLExpress | liteflow-script-qlexpress | 阿里表达式 |
| Lua | liteflow-script-lua | 游戏/嵌入式 |
| Aviator | liteflow-script-aviator | 高性能计算 |
| Kotlin | liteflow-script-kotlin | JVM生态 |

### 6.2 添加脚本依赖

```xml
<dependency>
    <groupId>com.yomahub</groupId>
    <artifactId>liteflow-script-groovy</artifactId>
    <version>2.15.3</version>
</dependency>
```

### 6.3 在规则文件中使用脚本

```xml
<flow>
    <!-- 内联脚本 -->
    <chain name="scriptChain">
        THEN(
            <script type="groovy">
                def context = this.getContextBean("com.example.OrderContext")
                context.setDiscount(context.getAmount() * 0.1)
                println "Groovy脚本计算折扣: ${context.getDiscount()}"
            </script>,
            nextComponent
        );
    </chain>
    
    <!-- 引用外部脚本文件 -->
    <chain name="externalScriptChain">
        THEN(
            <script file="classpath:scripts/calculate.groovy"/>,
            nextComponent
        );
    </chain>
</flow>
```

### 6.4 脚本中调用 Java

```groovy
// 获取上下文
def context = this.getContextBean("com.example.OrderContext")

// 获取 Spring Bean
def userService = this.getBean("userService")

// 调用 Java 方法
def user = userService.getById(context.getUserId())

// 设置结果
context.setUserName(user.getName())
```

---

## 七、规则存储与热刷新

### 7.1 支持的存储方式

| 存储方式 | 配置示例 | 适用场景 |
|---------|---------|---------|
| 本地文件 | `config/flow.xml` | 开发/测试 |
| 数据库 | 自定义扩展 | 生产环境 |
| Nacos | `nacos://{group}@{dataId}` | 微服务 |
| Apollo | `apollo://{namespace}` | 配置中心 |
| Etcd | `etcd://{key}` | 云原生 |
| Zookeeper | `zk://{path}` | 分布式 |
| Redis | `redis://{key}` | 缓存场景 |

### 7.2 数据库存储实现

```java
@Component
public class DatabaseFlowParser extends JsonFlowParser {
    
    @Autowired
    private FlowRuleRepository ruleRepository;
    
    @Override
    public void parseMain(List<String> pathList) {
        // 从数据库读取规则
        List<FlowRule> rules = ruleRepository.findAll();
        
        for (FlowRule rule : rules) {
            // 解析规则内容
            String chainId = rule.getChainId();
            String elData = rule.getElContent();
            
            // 构建链
            LiteFlowChainELBuilder.createChain()
                .setChainId(chainId)
                .setEL(elData)
                .build();
        }
    }
}
```

### 7.3 热刷新配置

```properties
# 开启文件监控热刷新
liteflow.enable-monitor-file=true

# 监控周期（毫秒）
liteflow.monitor-interval=3000
```

**编程方式热刷新：**

```java
@Autowired
private FlowExecutor flowExecutor;

public void reloadRules() {
    // 重新加载所有规则
    flowExecutor.reloadRule();
    
    // 或重新加载指定链
    flowExecutor.reloadRule("chain1");
}
```

---

## 八、高级特性

### 8.1 节点执行监控

```properties
# 开启监控日志
liteflow.monitor.enable-log=true

# 监控周期（毫秒）
liteflow.monitor.period=300000
```

**自定义监控：**

```java
@Component
public class CustomMonitor implements LiteflowMonitor {
    
    @Override
    public void onNodeBegin(String nodeId, Integer slotIndex) {
        System.out.println("节点开始: " + nodeId);
    }
    
    @Override
    public void onNodeEnd(String nodeId, Integer slotIndex, long timeSpent) {
        System.out.println("节点结束: " + nodeId + ", 耗时: " + timeSpent + "ms");
    }
}
```

### 8.2 请求 ID 生成器

```java
@Component
public class CustomRequestIdGenerator implements RequestIdGenerator {
    
    @Override
    public String generate(RequestIdGeneratorContext context) {
        return UUID.randomUUID().toString();
    }
}
```

```properties
liteflow.request-id-generator-class=com.example.CustomRequestIdGenerator
```

### 8.3 并行线程池自定义

```java
@Configuration
public class LiteFlowConfig {
    
    @Bean("whenExecutor")
    public ExecutorService whenExecutor() {
        return new ThreadPoolExecutor(
            16, 32, 60L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(5120),
            new ThreadFactoryBuilder().setNameFormat("liteflow-when-%d").build(),
            new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }
}
```

### 8.4 错误处理与重试

```java
@LiteflowComponent("retryableComponent")
public class RetryableComponent extends NodeComponent {
    
    @Override
    public void process() {
        // 业务逻辑
    }
    
    @Override
    public void onError(Exception e) {
        // 自定义错误处理
        log.error("组件执行失败", e);
    }
}
```

```properties
# 全局重试次数
liteflow.retry-count=3

# 重试间隔（毫秒）
liteflow.retry-interval=1000
```

### 8.5 前置/后置编排

```xml
<chain name="prePostChain">
    THEN(
        PRE(validateToken, checkPermission),
        businessLogic,
        POST(logOperation, sendNotification)
    );
</chain>
```

---

## 九、最佳实践

### 9.1 组件设计原则

1. **单一职责**：每个组件只做一件事
2. **幂等性**：组件应支持重复执行
3. **无副作用**：避免在组件中修改外部状态
4. **异常处理**：合理处理异常，决定是否继续

### 9.2 命名规范

| 类型 | 命名规范 | 示例 |
|-----|---------|------|
| 组件ID | 小写驼峰 | `validateOrder`, `checkInventory` |
| 链名称 | 驼峰命名 | `orderProcessChain`, `paymentFlow` |
| 上下文 | 业务+Context | `OrderContext`, `UserContext` |

### 9.3 项目结构建议

```
src/main/java/com/example/
├── component/           # LiteFlow 组件
│   ├── order/
│   │   ├── ValidateOrderComponent.java
│   │   ├── CheckInventoryComponent.java
│   │   └── CalculatePriceComponent.java
│   └── payment/
│       ├── ProcessPaymentComponent.java
│       └── SendNotificationComponent.java
├── context/             # 上下文定义
│   ├── OrderContext.java
│   └── PaymentContext.java
├── service/             # 业务服务
│   └── OrderService.java
└── config/              # 配置类
    └── LiteFlowConfig.java

src/main/resources/
├── config/
│   └── flow.xml         # 规则文件
└── scripts/             # 脚本文件（如使用）
    └── calculate.groovy
```

### 9.4 性能优化建议

1. **合理设置 slot-size**：根据并发量调整
2. **并行线程池隔离**：避免不同链互相影响
3. **组件缓存**：避免重复创建对象
4. **监控告警**：建立规则执行监控体系

---

## 十、完整示例

### 电商订单处理流程

**规则文件（order-flow.xml）：**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<flow>
    <chain name="orderProcessChain">
        THEN(
            validateOrder,
            checkInventory,
            IF(isVipCustomer, 
                THEN(applyVipDiscount, applyCoupon),
                applyNormalPrice
            ),
            calculateFinalPrice,
            WHEN(
                sendOrderSms,
                sendOrderEmail,
                sendAppNotification
            ),
            createOrder,
            POST(updateStatistics)
        );
    </chain>
</flow>
```

**上下文定义：**

```java
@Data
public class OrderContext {
    // 输入
    private String orderNo;
    private Long userId;
    private List<OrderItem> items;
    private BigDecimal originalAmount;
    
    // 中间结果
    private boolean inventoryChecked;
    private boolean isVip;
    private BigDecimal discountAmount;
    private BigDecimal couponAmount;
    
    // 输出
    private Order order;
    private BigDecimal finalAmount;
}
```

**组件实现：**

```java
@LiteflowComponent("validateOrder")
public class ValidateOrderComponent extends NodeComponent {
    @Override
    public void process() {
        OrderContext ctx = this.getContextBean(OrderContext.class);
        // 验证订单数据完整性
        if (StringUtils.isBlank(ctx.getOrderNo())) {
            throw new BusinessException("订单号不能为空");
        }
        if (CollectionUtils.isEmpty(ctx.getItems())) {
            throw new BusinessException("订单项不能为空");
        }
    }
}

@LiteflowComponent("checkInventory")
public class CheckInventoryComponent extends NodeComponent {
    @Autowired
    private InventoryService inventoryService;
    
    @Override
    public void process() {
        OrderContext ctx = this.getContextBean(OrderContext.class);
        for (OrderItem item : ctx.getItems()) {
            boolean hasStock = inventoryService.checkStock(item.getSkuId(), item.getQuantity());
            if (!hasStock) {
                throw new BusinessException("商品库存不足: " + item.getSkuId());
            }
        }
        ctx.setInventoryChecked(true);
    }
}

@LiteflowComponent("isVipCustomer")
public class IsVipCustomerComponent extends NodeBooleanComponent {
    @Autowired
    private UserService userService;
    
    @Override
    public boolean processBoolean() {
        OrderContext ctx = this.getContextBean(OrderContext.class);
        boolean isVip = userService.isVip(ctx.getUserId());
        ctx.setVip(isVip);
        return isVip;
    }
}

@LiteflowComponent("applyVipDiscount")
public class ApplyVipDiscountComponent extends NodeComponent {
    @Override
    public void process() {
        OrderContext ctx = this.getContextBean(OrderContext.class);
        BigDecimal discount = ctx.getOriginalAmount().multiply(new BigDecimal("0.1"));
        ctx.setDiscountAmount(discount);
    }
}

@LiteflowComponent("sendOrderSms")
public class SendOrderSmsComponent extends NodeComponent {
    @Autowired
    private SmsService smsService;
    
    @Override
    public void process() {
        OrderContext ctx = this.getContextBean(OrderContext.class);
        smsService.sendOrderSuccessSms(ctx.getUserId(), ctx.getOrderNo());
    }
}
```

**服务层调用：**

```java
@Service
@Slf4j
public class OrderService {
    
    @Resource
    private FlowExecutor flowExecutor;
    
    public OrderResult processOrder(OrderRequest request) {
        // 构建上下文
        OrderContext context = new OrderContext();
        context.setOrderNo(request.getOrderNo());
        context.setUserId(request.getUserId());
        context.setItems(request.getItems());
        context.setOriginalAmount(request.getTotalAmount());
        
        // 执行流程
        LiteflowResponse response = flowExecutor.execute2Resp(
            "orderProcessChain",
            request,
            context
        );
        
        if (!response.isSuccess()) {
            log.error("订单处理失败", response.getCause());
            throw new BusinessException("订单处理失败: " + response.getMessage());
        }
        
        // 返回结果
        OrderContext resultContext = response.getFirstContextBean();
        OrderResult result = new OrderResult();
        result.setOrderNo(resultContext.getOrderNo());
        result.setFinalAmount(resultContext.getFinalAmount());
        result.setOrderId(resultContext.getOrder().getId());
        
        return result;
    }
}
```

---

## 十一、常见问题

### Q1: LiteFlow 与 Drools 的区别？

| 特性 | LiteFlow | Drools |
|-----|---------|--------|
| 定位 | 流程编排引擎 | 规则推理引擎 |
| 学习成本 | 低（10分钟上手） | 高 |
| 性能 | 高（预编译） | 中等 |
| 适用场景 | 业务流程编排 | 复杂规则推理 |
| 依赖 | 轻量 | 较重 |

### Q2: 如何实现动态规则？

1. 将规则存储在数据库/配置中心
2. 实现自定义 FlowParser
3. 通过 API 触发规则重载

### Q3: 如何保证高并发性能？

1. 使用 Slot 池化机制
2. 合理设置 slot-size
3. 并行执行使用独立线程池
4. 开启线程池隔离

### Q4: 如何调试流程？

1. 开启执行日志：`liteflow.print-execution-log=true`
2. 使用 IDEA 插件：LiteFlow Helper
3. 添加监控组件记录执行路径

---

## 参考资源

- **官方文档**: https://liteflow.cc
- **GitHub**: https://github.com/dromara/liteflow
- **Gitee**: https://gitee.com/dromara/liteflow
- **IDEA 插件**: LiteFlow Helper
- **社区**: LF CLUB (https://liteflow.cc/pages/8d8888/)

---

*文档版本: 基于 LiteFlow v2.15.3*
*最后更新: 2026-03-03*
