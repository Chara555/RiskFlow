# RiskFlow 开发任务清单

> 最后更新：2026-03-09
> 
> **项目定位**：开源的低代码风控流程编排平台
> 
> **核心理念**：简单好用、易上手、可视化编排、组件化扩展

---

## 📊 优先级说明

- 🔴 **P0 - 本周必做**：核心功能，必须立即完成
- 🟡 **P1 - 2周内完成**：重要功能，近期完成
- 🟢 **P2 - 1个月内**：优化改进，中期完成
- 🔵 **P3 - 长期规划**：锦上添花，长期规划

---

## 🎯 第一阶段：核心能力建设（本周）

### 🔴 P0-1: 用户画像系统

**目标**：实现用户画像作为流程节点，支持基于画像的差异化流程

#### 数据库设计

- [ ] **创建 user_profile 表**
  ```sql
  - user_id (用户ID)
  - user_level (用户等级: VIP/NORMAL/NEW/RISK)
  - risk_level (风险等级: LOW/MEDIUM/HIGH)
  - credit_score (信用分: 0-100)
  - total_events (总事件数)
  - reject_count (拒绝次数)
  - review_count (审核次数)
  - avg_risk_score (平均风险评分)
  - device_count (关联设备数)
  - common_devices (常用设备 JSONB)
  - common_locations (常用地点 JSONB)
  - profile_data (扩展数据 JSONB)
  - tags (用户标签 JSONB)
  ```

#### 服务实现

- [ ] **UserProfileService**
  - [ ] `getOrCreate(userId)` - 获取或创建用户画像
  - [ ] `updateProfile(userId, data)` - 更新画像
  - [ ] `calculateRiskLevel(userId)` - 计算风险等级
  - [ ] `updateStatistics(userId, decision)` - 更新统计信息

#### 组件实现

- [ ] **LoadUserProfileComponent** - 加载用户画像
- [ ] **IsVipUserComponent** - 判断是否VIP用户
- [ ] **IsNewUserComponent** - 判断是否新用户
- [ ] **IsHighRiskUserComponent** - 判断是否高风险用户
- [ ] **RouteByUserLevelComponent** - 根据用户等级路由

#### 测试

- [ ] 单元测试
- [ ] 集成测试
- [ ] 性能测试

**预期成果**：
- 用户画像可以作为节点在流程中使用
- 支持基于画像的流程路由

---

### 🔴 P0-2: 动态决策阈值系统

**目标**：支持针对不同场景、不同用户等级配置不同的决策阈值

#### 数据库设计

- [ ] **创建 decision_threshold 表**
  ```sql
  - workflow_id (流程ID，NULL表示全局)
  - event_type (事件类型: login/payment/register)
  - user_level (用户等级: VIP/NORMAL/NEW/RISK)
  - reject_threshold (拒绝阈值，默认80)
  - review_threshold (审核阈值，默认50)
  - challenge_threshold (挑战阈值，默认30)
  - reject_action (拒绝后动作 JSONB)
  - review_action (审核后动作 JSONB)
  - challenge_action (挑战后动作 JSONB)
  - accept_action (通过后动作 JSONB)
  - enabled (是否启用)
  - priority (优先级)
  ```

#### 服务实现

- [ ] **DecisionThresholdService**
  - [ ] `getThreshold(workflowId, eventType, userLevel)` - 获取阈值配置
  - [ ] `updateThreshold(threshold)` - 更新阈值（热更新）
  - [ ] `getDefaultThreshold()` - 获取默认阈值

#### 组件改造

- [ ] **改造 DecisionJudgeComponent**
  - [ ] 支持动态阈值
  - [ ] 支持后续动作执行
  - [ ] 支持不同用户等级不同阈值

#### 初始化数据

- [ ] 插入默认阈值配置
  - [ ] VIP用户：90/60/40
  - [ ] 普通用户：80/50/30
  - [ ] 新用户：70/40/20
  - [ ] 高风险用户：50/30/10

**预期成果**：
- 阈值可以动态配置
- 不同用户等级使用不同阈值
- 支持热更新

---

### 🔴 P0-3: 决策日志插件化

**目标**：将日志存储设计为可插拔的组件

#### 接口定义

- [ ] **DecisionLogStorage 接口**
  ```java
  - save(DecisionLog log)
  - saveBatch(List<DecisionLog> logs)
  - query(DecisionLogQuery query)
  - getStorageType()
  ```

#### 默认实现

- [ ] **PostgreSQLLogStorage** (默认)
  - [ ] 同步写入
  - [ ] 异步写入支持
  - [ ] 批量写入支持

#### 配置支持

- [ ] **application.yml 配置**
  ```yaml
  riskflow:
    log:
      storage: postgresql  # 默认
      postgresql:
        async: true
        batch-size: 100
  ```

#### 服务改造

- [ ] **RiskDecisionService**
  - [ ] 使用 DecisionLogStorage 接口
  - [ ] 支持异步日志写入

**预期成果**：
- 日志存储可插拔
- 默认使用 PostgreSQL
- 为后续 ES/ClickHouse 插件预留接口

---

### 🔴 P0-4: JSON → EL 转换引擎

**目标**：实现独立的流程转换引擎，支持可视化编排

#### 核心接口

- [ ] **FlowConversionEngine 接口**
  ```java
  - convert(String flowJson) → String el
  - convert(FlowDefinition flow) → String el
  - parse(String el) → FlowDefinition
  - validate(FlowDefinition flow) → ValidationResult
  ```

#### 核心组件

- [ ] **FlowParser (解析器)**
  - [ ] `parseJson(String json)` - 解析 JSON
  - [ ] `parseEL(String el)` - 解析 EL (后续)

- [ ] **FlowAnalyzer (分析器)**
  - [ ] `analyze(FlowDefinition)` - 分析流程结构
  - [ ] 识别串行结构
  - [ ] 识别并行结构 (后续)
  - [ ] 识别条件分支 (后续)

- [ ] **ELGenerator (生成器)**
  - [ ] `generate(FlowAST)` - 生成 EL 表达式
  - [ ] 生成串行 EL: `THEN(...)`
  - [ ] 生成并行 EL: `WHEN(...)` (后续)
  - [ ] 生成条件 EL: `IF(...)` (后续)

- [ ] **FlowValidator (验证器)**
  - [ ] 基本验证（节点、连接）
  - [ ] 结构验证（环检测、孤立节点）
  - [ ] 语义验证（条件节点分支数）

#### 数据模型

- [ ] **FlowDefinition** - 流程定义
- [ ] **FlowNode** - 流程节点
- [ ] **FlowEdge** - 流程连接
- [ ] **FlowAST** - 抽象语法树
- [ ] **FlowASTNode** - AST 节点
  - [ ] LeafNode (叶子节点)
  - [ ] SequentialNode (串行节点)
  - [ ] ParallelNode (并行节点) (后续)
  - [ ] ConditionalNode (条件节点) (后续)

#### 实现策略（渐进式）

**第一版（本周）**：
- [ ] 只支持串行流程
- [ ] 简单的拓扑排序
- [ ] 生成 `THEN(a, b, c)` 格式

**第二版（下周）**：
- [ ] 支持并行流程
- [ ] 识别并行节点
- [ ] 生成 `THEN(a, WHEN(b, c), d)` 格式

**第三版（2周后）**：
- [ ] 支持条件分支
- [ ] 识别条件节点
- [ ] 生成 `IF(cond, then, else)` 格式

#### 测试

- [ ] **单元测试**
  - [ ] FlowParserTest
  - [ ] FlowAnalyzerTest
  - [ ] ELGeneratorTest
  - [ ] FlowValidatorTest

- [ ] **集成测试**
  - [ ] 串行流程转换测试
  - [ ] 复杂流程转换测试

**预期成果**：
- 独立的转换引擎
- 支持串行流程（第一版）
- 为可视化编排打好基础

---

### 🔴 P0-5: 流程管理服务

**目标**：提供流程的 CRUD 和动态加载能力

#### API 设计

- [ ] **FlowManagementController**
  - [ ] `POST /api/v1/flows` - 创建流程
  - [ ] `PUT /api/v1/flows/{id}` - 更新流程
  - [ ] `GET /api/v1/flows/{id}` - 获取流程
  - [ ] `GET /api/v1/flows` - 流程列表
  - [ ] `DELETE /api/v1/flows/{id}` - 删除流程
  - [ ] `POST /api/v1/flows/{id}/deploy` - 部署流程

#### 服务实现

- [ ] **FlowManagementService**
  - [ ] `saveFlow(FlowDefinition)` - 保存流程
    - 转换为 EL
    - 保存到数据库
    - 热加载到 LiteFlow
  - [ ] `updateFlow(id, FlowDefinition)` - 更新流程
  - [ ] `deployFlow(id)` - 部署流程
  - [ ] `getFlow(id)` - 获取流程
  - [ ] `listFlows()` - 流程列表

#### 数据库改造

- [ ] **workflow 表增加字段**
  - [ ] `el_expression` - LiteFlow EL 表达式
  - [ ] `flow_data` - 前端 JSON 数据

**预期成果**：
- 流程可以通过 API 管理
- 流程修改后自动热加载
- 支持流程版本管理

---

### 🔴 P0-6: 组件元数据管理

**目标**：前端需要知道有哪些组件可用

#### API 设计

- [ ] **ComponentController**
  - [ ] `GET /api/v1/components` - 组件列表
  - [ ] `GET /api/v1/components/{id}` - 组件详情
  - [ ] `GET /api/v1/components/categories` - 组件分类

#### 数据模型

- [ ] **ComponentMetadata**
  ```java
  - id (组件ID)
  - name (组件名称)
  - category (分类: 基础检测/规则执行/AI分析/决策/用户画像)
  - description (描述)
  - icon (图标)
  - inputs (输入参数)
  - outputs (输出参数)
  - configSchema (配置Schema)
  ```

#### 组件注册

- [ ] 自动扫描 `@LiteflowComponent` 注解
- [ ] 生成组件元数据
- [ ] 提供给前端使用

**预期成果**：
- 前端可以获取所有可用组件
- 组件有清晰的分类和说明
- 支持组件配置

---

### 🔴 P0-7: 补充基础组件

**目标**：提供 10-15 个常用组件

#### 基础检测组件

- [ ] **IpWhitelistCheckComponent** - IP白名单检测
- [ ] **DeviceFingerprintComponent** - 设备指纹检测
- [ ] **UserBlacklistCheckComponent** - 用户黑名单检测

#### 规则执行组件

- [ ] **FrequencyCheckComponent** - 频率检测
  - 滑动窗口统计
  - 支持多种时间窗口
- [ ] **VelocityCheckComponent** - 速度检测
  - 检测操作速度异常

#### 用户画像组件（已在 P0-1）

- [ ] LoadUserProfileComponent
- [ ] IsVipUserComponent
- [ ] IsNewUserComponent
- [ ] IsHighRiskUserComponent
- [ ] RouteByUserLevelComponent

**预期成果**：
- 提供 10+ 个常用组件
- 覆盖常见风控场景
- 每个组件都有清晰的文档

---

### 🔴 P0-8: 数据库初始化

**目标**：提供完整的数据库初始化脚本

- [ ] **init.sql** - 建表脚本
  - [ ] workflow 表
  - [ ] rule_config 表
  - [ ] blacklist 表
  - [ ] decision_log 表
  - [ ] user_profile 表 (新增)
  - [ ] decision_threshold 表 (新增)

- [ ] **data.sql** - 测试数据
  - [ ] 默认流程
  - [ ] 示例规则（15条）
  - [ ] 示例黑名单
  - [ ] 默认阈值配置

- [ ] **Docker 支持**
  - [ ] 数据库自动初始化
  - [ ] 测试数据自动导入

**预期成果**：
- 一键初始化数据库
- 提供丰富的测试数据

---

## 🚀 第二阶段：可视化编排（下周）

### 🟡 P1-1: 前端项目初始化

**技术栈**：React + LogicFlow + Ant Design

- [ ] **项目搭建**
  - [ ] Create React App / Vite
  - [ ] 安装依赖
  - [ ] 配置路由

- [ ] **集成 LogicFlow**
  - [ ] 安装 `@logicflow/core`
  - [ ] 安装 `@logicflow/extension`
  - [ ] 基础画布搭建

**预期成果**：
- 前端项目可以运行
- LogicFlow 画布可以显示

---

### 🟡 P1-2: 流程设计器

- [ ] **节点面板**
  - [ ] 显示所有可用组件
  - [ ] 按分类展示
  - [ ] 支持搜索

- [ ] **画布**
  - [ ] 拖拽节点
  - [ ] 连接节点
  - [ ] 删除节点/连接

- [ ] **属性面板**
  - [ ] 节点配置
  - [ ] 参数设置

- [ ] **工具栏**
  - [ ] 保存流程
  - [ ] 测试流程
  - [ ] 部署流程
  - [ ] 导入/导出

**预期成果**：
- 用户可以拖拽编排流程
- 流程可以保存到后端

---

### 🟡 P1-3: 流程测试功能

- [ ] **测试界面**
  - [ ] 输入测试数据
  - [ ] 执行流程
  - [ ] 查看执行结果
  - [ ] 查看节点执行详情

- [ ] **后端支持**
  - [ ] `POST /api/v1/flows/{id}/test` - 测试流程
  - [ ] 返回详细的执行日志

**预期成果**：
- 用户可以在线测试流程
- 查看每个节点的执行情况

---

### 🟡 P1-4: 模板市场

- [ ] **模板定义**
  - [ ] 登录风控模板
  - [ ] 支付风控模板
  - [ ] 注册风控模板

- [ ] **模板管理**
  - [ ] `GET /api/v1/templates` - 模板列表
  - [ ] `POST /api/v1/templates/{id}/import` - 导入模板

- [ ] **前端界面**
  - [ ] 模板展示
  - [ ] 一键导入
  - [ ] 模板预览

**预期成果**：
- 提供 3-5 个流程模板
- 用户可以一键导入

---

### 🟡 P1-5: Docker 一键启动

- [ ] **Dockerfile**
  - [ ] risk-flow-server 镜像
  - [ ] 多阶段构建
  - [ ] 镜像优化

- [ ] **docker-compose.yml**
  - [ ] PostgreSQL 服务
  - [ ] RiskFlow Server 服务
  - [ ] 前端服务（可选）

- [ ] **启动脚本**
  - [ ] `start.sh` - 一键启动
  - [ ] `stop.sh` - 停止服务
  - [ ] `logs.sh` - 查看日志

- [ ] **.env.example**
  - [ ] 环境变量示例
  - [ ] 配置说明

**预期成果**：
- `docker-compose up` 一键启动
- 5分钟内完成部署

---

## 📚 第三阶段：文档和优化（2周后）

### 🟢 P2-1: 文档完善

- [ ] **Quick Start**
  - [ ] 5分钟快速开始
  - [ ] 环境要求
  - [ ] 安装步骤
  - [ ] 第一个流程

- [ ] **用户手册**
  - [ ] 流程设计指南
  - [ ] 组件使用说明
  - [ ] 最佳实践

- [ ] **开发文档**
  - [ ] 自定义组件开发
  - [ ] 插件开发
  - [ ] API 文档

- [ ] **部署文档**
  - [ ] Docker 部署
  - [ ] 手动部署
  - [ ] 配置说明

**预期成果**：
- 文档完整、清晰
- 用户可以快速上手

---

### 🟢 P2-2: 阈值配置界面

- [ ] **阈值管理页面**
  - [ ] 阈值列表
  - [ ] 创建/编辑阈值
  - [ ] 阈值测试

- [ ] **配置表单**
  - [ ] 事件类型选择
  - [ ] 用户等级选择
  - [ ] 阈值滑块
  - [ ] 后续动作配置

**预期成果**：
- 阈值可以在界面上配置
- 实时生效

---

### 🟢 P2-3: 规则管理界面

- [ ] **规则列表**
  - [ ] 规则展示
  - [ ] 启用/禁用
  - [ ] 规则测试

- [ ] **规则编辑**
  - [ ] 规则表达式编辑
  - [ ] 评分设置
  - [ ] 优先级设置

**预期成果**：
- 规则可以在界面上管理
- 支持在线测试

---

### 🟢 P2-4: 决策日志查询

- [ ] **日志列表**
  - [ ] 多条件查询
  - [ ] 分页展示
  - [ ] 导出功能

- [ ] **日志详情**
  - [ ] 请求数据
  - [ ] 节点执行详情
  - [ ] 决策结果

**预期成果**：
- 可以查询历史决策
- 查看详细的执行过程

---

### 🟢 P2-5: 性能优化

- [ ] **Redis 缓存**
  - [ ] 黑名单缓存
  - [ ] 规则配置缓存
  - [ ] 用户画像缓存

- [ ] **异步处理**
  - [ ] 异步日志写入
  - [ ] 批量写入

- [ ] **数据库优化**
  - [ ] 索引优化
  - [ ] 分区表（decision_log）

**预期成果**：
- 响应时间 < 100ms
- 支持高并发

---

## 🔮 第四阶段：高级特性（长期）

### 🔵 P3-1: 脚本组件支持

- [ ] **Groovy 脚本组件**
  - [ ] 在线编写脚本
  - [ ] 脚本保存和加载
  - [ ] 脚本执行

- [ ] **脚本编辑器**
  - [ ] 代码高亮
  - [ ] 自动补全
  - [ ] 语法检查

**预期成果**：
- 用户可以在线编写 Groovy 脚本
- 无需重启即可生效

---

### 🔵 P3-2: AI 服务插件化

- [ ] **AI 服务接口**
  - [ ] AiAnalysisService 接口
  - [ ] MockAiService (默认)
  - [ ] OpenAiService (可选)
  - [ ] QianwenService (可选)

- [ ] **配置支持**
  ```yaml
  riskflow:
    ai:
      provider: mock  # mock / openai / qianwen
  ```

**预期成果**：
- AI 服务可插拔
- 默认使用 Mock 实现

---

### 🔵 P3-3: 日志存储插件

- [ ] **risk-flow-plugin-es**
  - [ ] Elasticsearch 日志存储
  - [ ] 全文搜索
  - [ ] 海量数据分析

- [ ] **risk-flow-plugin-clickhouse**
  - [ ] ClickHouse 日志存储
  - [ ] OLAP 分析

**预期成果**：
- 用户可以选择日志存储方式
- 按需安装插件

---

### 🔵 P3-4: 监控和告警

- [ ] **Prometheus 集成**
  - [ ] 指标采集
  - [ ] Grafana 大盘

- [ ] **告警规则**
  - [ ] 决策失败率告警
  - [ ] 响应时间告警

**预期成果**：
- 实时监控系统状态
- 异常自动告警

---

### 🔵 P3-5: 多语言 SDK

- [ ] **Python SDK**
- [ ] **Go SDK**
- [ ] **Node.js SDK**

**预期成果**：
- 支持多种语言接入

---

## 📝 开发规范

### 代码规范

- [ ] 配置 CheckStyle
- [ ] 配置 SpotBugs
- [ ] 代码审查流程

### 测试规范

- [ ] 单元测试覆盖率 > 80%
- [ ] 集成测试
- [ ] 端到端测试

### 文档规范

- [ ] 代码注释
- [ ] API 文档
- [ ] 变更日志

---

## 🎯 本周重点（明天开始）

### Day 1-2: 用户画像 + 动态阈值

- [ ] 创建数据库表
- [ ] 实现服务层
- [ ] 实现组件
- [ ] 单元测试

### Day 3-4: JSON → EL 转换引擎

- [ ] 定义接口和数据模型
- [ ] 实现 Parser
- [ ] 实现 Analyzer (串行)
- [ ] 实现 Generator (串行)
- [ ] 实现 Validator

### Day 5: 流程管理服务

- [ ] 实现 API
- [ ] 集成转换引擎
- [ ] 测试流程保存和加载

### Day 6-7: 组件补充 + 数据库初始化

- [ ] 补充基础组件
- [ ] 编写初始化脚本
- [ ] 集成测试

---

## 📊 进度跟踪

| 阶段 | 任务数 | 已完成 | 进行中 | 未开始 | 完成率 |
|------|--------|--------|--------|--------|--------|
| 第一阶段 | 8 | 0 | 0 | 8 | 0% |
| 第二阶段 | 5 | 0 | 0 | 5 | 0% |
| 第三阶段 | 5 | 0 | 0 | 5 | 0% |
| 第四阶段 | 5 | 0 | 0 | 5 | 0% |

---

## 🤔 待确认的问题

### 技术选型

- [x] 日志存储：PostgreSQL (默认) + 插件化
- [x] 前端框架：React + LogicFlow + Ant Design
- [x] 转换引擎：独立模块，渐进式实现
- [ ] 开源协议：Apache 2.0 还是 MIT？

### 功能优先级

- [x] 用户画像：P0，本周完成
- [x] 动态阈值：P0，本周完成
- [x] 转换引擎：P0，本周完成（串行版本）
- [x] 可视化编排：P1，下周开始

---

## 📞 每日站会

**时间**：每天早上 10:00

**内容**：
1. 昨天完成了什么
2. 今天计划做什么
3. 遇到什么问题

---

**最后更新时间**：2026-03-09
**下次更新时间**：每天更新进度
