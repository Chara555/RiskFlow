# RiskFlow Sample

> 演示 RiskFlow 的两种集成方式：SDK 嵌入 和 REST API 调用

## 方式一：SDK 嵌入模式（推荐 Java 应用）

直接在业务应用中引入 RiskFlow，通过 LiteFlow 直接执行决策流程。

```java
@Autowired
private RiskFlowService riskFlowService;

// 执行登录风控
RiskFlowContext result = riskFlowService.decide(
    "login",
    "user123",
    "192.168.1.100",
    Map.of("isNewDevice", true, "failedLoginCount", 3)
);
```

## 方式二：REST API 模式（跨语言调用）

通过 HTTP 调用独立的 RiskFlow Server 服务。

```java
RiskFlowResponse response = riskFlowClient.decide(request);
```

## API 接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/v1/sample/login` | POST | 登录风控（SDK 模式） |
| `/api/v1/sample/payment` | POST | 支付风控（SDK 模式） |
| `/api/v1/sample/login-via-http` | POST | 登录风控（REST 模式） |
| `/api/v1/sample/payment-via-http` | POST | 支付风控（REST 模式） |
| `/api/v1/sample/compare` | POST | 对比 SDK 和 REST 两种方式 |
