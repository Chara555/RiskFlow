-- RiskFlow 数据库建表脚本（与实际数据库结构对齐）
-- 数据库: PostgreSQL
-- 说明: 使用 IF NOT EXISTS 保证幂等，不破坏已有数据

-- ============================================================
-- 1. 工作流表（其他表有外键依赖，需先建）
-- ============================================================
CREATE TABLE IF NOT EXISTS workflow (
    id            BIGSERIAL PRIMARY KEY,
    name          VARCHAR(100) NOT NULL,
    code          VARCHAR(50)  NOT NULL UNIQUE,
    description   VARCHAR(500),
    version       INTEGER     DEFAULT 1,
    status        VARCHAR(20) DEFAULT 'DRAFT',   -- DRAFT / PUBLISHED / ARCHIVED
    flow_data     JSONB,                          -- 前端可视化编排 JSON
    el_expression TEXT,                           -- LiteFlow EL 表达式
    created_by    VARCHAR(50),
    created_at    TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP   DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- 2. 黑名单表
-- ============================================================
CREATE TABLE IF NOT EXISTS blacklist (
    id          BIGSERIAL PRIMARY KEY,
    type        VARCHAR(20)  NOT NULL,           -- IP / DEVICE / PHONE / USER / EMAIL
    value       VARCHAR(200) NOT NULL,
    reason      VARCHAR(500),
    source      VARCHAR(50),                     -- MANUAL / AUTO / THIRD_PARTY
    expire_time TIMESTAMP,                       -- NULL 表示永久
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_blacklist_type_value  ON blacklist (type, value);
CREATE INDEX IF NOT EXISTS idx_blacklist_expire_time ON blacklist (expire_time);

-- ============================================================
-- 3. 规则配置表
-- ============================================================
CREATE TABLE IF NOT EXISTS rule_config (
    id         BIGSERIAL PRIMARY KEY,
    code       VARCHAR(50)  NOT NULL UNIQUE,
    name       VARCHAR(100) NOT NULL,
    type       VARCHAR(30),
    risk_level VARCHAR(20) DEFAULT 'NONE',   -- 信号制下的风险等级: NONE / LOW / MEDIUM / HIGH
    params     JSONB,                          -- 扩展参数（如 threshold、timeWindowHours）
    enabled    BOOLEAN  DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_rule_config_enabled ON rule_config (enabled);
CREATE INDEX IF NOT EXISTS idx_rule_config_code    ON rule_config (code);

-- ============================================================
-- 3.1 规则配置表迁移（处理历史表结构差异）
-- ============================================================
DO $$
BEGIN
    -- 添加 risk_level 列（如果不存在）
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_name = 'rule_config' AND column_name = 'risk_level') THEN
        ALTER TABLE rule_config ADD COLUMN risk_level VARCHAR(20) DEFAULT 'NONE';
    END IF;
    
    -- 删除已废弃的列（如果存在）
    IF EXISTS (SELECT 1 FROM information_schema.columns
               WHERE table_name = 'rule_config' AND column_name = 'expression') THEN
        ALTER TABLE rule_config DROP COLUMN expression;
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns
               WHERE table_name = 'rule_config' AND column_name = 'score') THEN
        ALTER TABLE rule_config DROP COLUMN score;
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns
               WHERE table_name = 'rule_config' AND column_name = 'priority') THEN
        ALTER TABLE rule_config DROP COLUMN priority;
    END IF;
END $$;

-- ============================================================
-- 4. 动态决策阈值表
-- ============================================================
CREATE TABLE IF NOT EXISTS decision_threshold (
    id                  BIGSERIAL PRIMARY KEY,
    workflow_id         BIGINT,
    event_type          VARCHAR(50),
    user_level          VARCHAR(20),
    reject_threshold    INTEGER   DEFAULT 80   NOT NULL,
    review_threshold    INTEGER   DEFAULT 50   NOT NULL,
    challenge_threshold INTEGER   DEFAULT 30   NOT NULL,
    reject_action       JSONB,
    review_action       JSONB,
    challenge_action    JSONB,
    accept_action       JSONB,
    enabled             BOOLEAN   DEFAULT TRUE NOT NULL,
    priority            INTEGER   DEFAULT 0    NOT NULL,
    description         VARCHAR(500),
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_threshold_workflow   ON decision_threshold (workflow_id);
CREATE INDEX IF NOT EXISTS idx_threshold_event_type ON decision_threshold (event_type);
CREATE INDEX IF NOT EXISTS idx_threshold_user_level ON decision_threshold (user_level);
CREATE INDEX IF NOT EXISTS idx_threshold_enabled    ON decision_threshold (enabled);

-- ============================================================
-- 5. 事件路由表（事件类型 → 工作流链路映射）
-- ============================================================
CREATE TABLE IF NOT EXISTS event_routing (
    id             BIGSERIAL PRIMARY KEY,
    event_type     VARCHAR(50)  NOT NULL UNIQUE,  -- 事件类型（如 login / payment / register）
    workflow_code  VARCHAR(50)  NOT NULL,          -- 对应 LiteFlow chainId
    enabled        BOOLEAN      DEFAULT TRUE,
    description    VARCHAR(200),
    created_at     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- 6. 决策日志表（信号驱动制）
-- ============================================================
CREATE TABLE IF NOT EXISTS decision_log (
    id                 BIGSERIAL PRIMARY KEY,
    event_id           VARCHAR(100) NOT NULL,
    workflow_id        BIGINT REFERENCES workflow (id),
    user_id            VARCHAR(100),
    event_type         VARCHAR(50),
    user_ip            VARCHAR(50),
    device_id          VARCHAR(100),
    request_data       JSONB,
    decision           VARCHAR(20),              -- ACCEPT / CHALLENGE / REVIEW / REJECT
    decision_msg       VARCHAR(500),
    node_results       JSONB,                    -- 完整信号快照
    signal_summary     JSONB,                    -- 各等级信号数量统计
    execution_time     INTEGER,                  -- 毫秒
    user_level         VARCHAR(20),
    created_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_decision_log_event_id   ON decision_log (event_id);
CREATE INDEX IF NOT EXISTS idx_decision_log_user_id    ON decision_log (user_id);
CREATE INDEX IF NOT EXISTS idx_decision_log_event_type ON decision_log (event_type);
CREATE INDEX IF NOT EXISTS idx_decision_log_created_at ON decision_log (created_at);
CREATE INDEX IF NOT EXISTS idx_decision_log_decision   ON decision_log (decision);

-- ============================================================
-- 7. 用户画像表
-- ============================================================
CREATE TABLE IF NOT EXISTS user_profile (
    id                BIGSERIAL PRIMARY KEY,
    user_id           VARCHAR(100) NOT NULL UNIQUE,
    user_level        VARCHAR(20)  NOT NULL DEFAULT 'NORMAL',  -- VIP / NORMAL / NEW / RISK
    risk_level        VARCHAR(20)  NOT NULL DEFAULT 'LOW',     -- LOW / MEDIUM / HIGH
    credit_score      INTEGER      NOT NULL DEFAULT 100,
    total_events      INTEGER      NOT NULL DEFAULT 0,
    reject_count      INTEGER      NOT NULL DEFAULT 0,
    review_count      INTEGER      NOT NULL DEFAULT 0,
    challenge_count   INTEGER      NOT NULL DEFAULT 0,
    accept_count      INTEGER      NOT NULL DEFAULT 0,
    avg_risk_score    NUMERIC(5,2),
    last_event_time   TIMESTAMP,
    last_login_time   TIMESTAMP,
    last_payment_time TIMESTAMP,
    device_count      INTEGER      NOT NULL DEFAULT 0,
    common_devices    JSONB,
    common_locations  JSONB,
    tags              JSONB,
    profile_data      JSONB,
    created_at        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_user_profile_user_id   ON user_profile (user_id);
CREATE INDEX IF NOT EXISTS idx_user_profile_user_level ON user_profile (user_level);
CREATE INDEX IF NOT EXISTS idx_user_profile_risk_level ON user_profile (risk_level);
