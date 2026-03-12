# RiskFlow

高性能、可扩展的实时风控决策平台。

## 概述

RiskFlow 是一个基于 LiteFlow 的开源风控平台，提供可视化拖拽编排、高并发、低延迟的风控决策能力。

## 技术栈

- Java 21
- Spring Boot 3.2
- LiteFlow 2.15.3（流程编排引擎）
- PostgreSQL（支持 JSON）
- Maven

---

## 项目结构

```
risk-flow/
├── risk-flow-core/                 # 核心引擎模块（可嵌入）
├── risk-flow-server/               # 独立部署服务
├── risk-flow-sdk-starter/         # Spring Boot 启动器
└── risk-flow-sample/              # 示例项目
```

---

## 模块详解

### 1. risk-flow-core（核心引擎）

基于 LiteFlow 的风控流程引擎，提供组件化的节点执行能力。

```
risk-flow-core/
├── component/                     # LiteFlow 组件（节点）
│   ├── base/                     # 基础检测组件
│   │   ├── LoadContextComponent        # 上下文加载组件
│   │   └── IpBlacklistCheckComponent   # IP 黑名单检测组件
│   ├── rule/                     # 规则执行组件
│   │   └── RuleExecuteComponent         # 通用规则执行器
│   ├── ai/                       # AI 分析组件
│   │   └── LlmRiskAnalyzeComponent     # LLM 智能风险分析
│   ├── decision/                 # 决策组件
│   │   └── DecisionJudgeComponent       # 决策判定
│   └── post/                     # 后处理组件
│       └── DummyNodeComponent            # 空操作占位
│
├── context/                      # 上下文定义
│   └── RiskFlowContext                 # 风控流程上下文
│
└── resources/
    └── flow/
        └── risk-decision.xml            # LiteFlow 流程定义
```

#### 组件说明

| 类名 | 功能 | 状态 |
|------|------|------|
| **LoadContextComponent** | 初始化风险上下文，生成事件ID | 已实现 |
| **IpBlacklistCheckComponent** | IP 黑名单检测，命中加 50 分 | 已实现 |
| **RuleExecuteComponent** | 根据事件类型执行不同规则（login/payment/register） | 已实现 |
| **LlmRiskAnalyzeComponent** | LLM 智能风险分析（风险评分 >= 50 时触发） | 已实现（模拟） |
| **DecisionJudgeComponent** | 根据评分做出最终决策（ACCEPT/REVIEW/CHALLENGE/REJECT） | 已实现 |
| **DummyNodeComponent** | 空操作，用于流程占位 | 已实现 |

#### RiskFlowContext（上下文）

风控流程的数据载体，包含：

- **输入参数**：eventId, eventType, userId, userIp, deviceId, features, extInfo
- **中间结果**：baseCheckResults, ruleScores, totalRiskScore, aiAnalysisResult, isHighRisk
- **输出结果**：result, resultMessage, decisionTime, executionTimeMs

---

### 2. risk-flow-server（REST 服务）

独立部署的风控决策 HTTP 服务。

```
risk-flow-server/
├── controller/
│   └── RiskDecisionController          # 风控决策 API
├── service/
│   └── RiskDecisionService            # 决策服务
├── dto/
│   ├── DecisionRequest                # 请求 DTO
│   └── DecisionResponse               # 响应 DTO
└── resources/
    └── application.yml                # 配置文件
```

#### API 端点

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/v1/risk/decide | 执行风控决策 |

#### 请求示例

```json
{
  "eventId": "evt-001",
  "eventType": "login",
  "userId": "user123",
  "userIp": "192.168.1.1",
  "deviceId": "device-001",
  "features": {
    "isNewDevice": true,
    "loginHour": 3,
    "failedLoginCount": 5
  }
}
```

#### 响应示例

```json
{
  "decisionId": "evt-001",
  "result": "REJECT",
  "riskScore": 85,
  "message": "高风险操作，系统直接拒绝",
  "decisionTime": "2026-03-04T10:30:00",
  "executionTimeMs": 150
}
```

---

### 3. risk-flow-sdk-starter（嵌入式 SDK）

以依赖方式嵌入到其他 Spring Boot 项目中使用。

```
risk-flow-sdk-starter/
└── autoconfigure/
    └── RiskFlowAutoConfiguration      # Spring Boot 自动配置
```

---

### 4. risk-flow-sample（示例项目）

待开发 - 提供完整的使用示例

---

## 流程执行图

```
请求 → RiskDecisionController → RiskDecisionService
                                      ↓
                              LiteFlow FlowExecutor
                                      ↓
                        ┌─────────────┴─────────────┐
                        ↓                           ↓
                   loadContext              ipBlacklistCheck
                        ↓                           ↓
                    ruleExecute ←──并行── (基础检测)
                        ↓
              IF(isHighRisk) → llmRiskAnalyze (高风险)
                        ↓
                 decisionJudge
                        ↓
                   返回决策结果
```

---

## 决策阈值

| 阈值 | 决策结果 | 说明 |
|------|---------|------|
| >= 80 | REJECT | 直接拒绝 |
| >= 50 | REVIEW | 人工审核 |
| >= 30 | CHALLENGE | 验证码挑战 |
| < 30 | ACCEPT | 审核通过 |

---

## TODO 列表

### 高优先级

- [ ] **完善 IP 黑名单组件**
  - [ ] 从数据库/缓存加载黑名单
  - [ ] 支持 IPv6

- [ ] **实现更多基础检测组件**
  - [ ] IpWhitelistCheckComponent（白名单）
  - [ ] IpGeoComponent（IP 地理位置）
  - [ ] DeviceFingerprintComponent（设备指纹）
  - [ ] UserBlacklistCheckComponent（用户黑名单）
  - [ ] PhoneBlacklistCheckComponent（手机号黑名单）

- [ ] **实现更多规则执行组件**
  - [ ] FrequencyCheckComponent（频率检测）
  - [ ] VelocityCheckComponent（速度检测）
  - [ ] DecisionTableComponent（决策表）
  - [ ] ScoreCardComponent（评分卡）

### 中优先级

- [ ] **AI 节点增强**
  - [ ] 接入真实 LLM 服务（OpenAI/Azure/通义千问）
  - [ ] 支持流式输出
  - [ ] 添加 AI 分析阈值配置

- [ ] **规则管理**
  - [ ] 从数据库加载规则
  - [ ] 规则热更新
  - [ ] 规则版本管理

- [ ] **决策记录**
  - [ ] 决策日志持久化
  - [ ] 审计日志
  - [ ] 统计分析

### 低优先级

- [ ] **可视化编排**
  - [ ] 工作流设计器
  - [ ] 拖拽式节点配置
  - [ ] 流程版本管理

- [ ] **管理后台**
  - [ ] 规则管理
  - [ ] 流程管理
  - [ ] 统计报表
  - [ ] 实时监控

- [ ] **高可用**
  - [ ] 集群支持
  - [ ] 限流熔断
  - [ ] 性能监控

---

## 许可证

Apache License 2.0 - 详见 [LICENSE](LICENSE)。
