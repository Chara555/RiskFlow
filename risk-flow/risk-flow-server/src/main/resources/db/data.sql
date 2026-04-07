-- RiskFlow 初始化数据
-- 说明: 全部使用 ON CONFLICT DO NOTHING，保证幂等（重复执行不报错）

-- ============================================================
-- 1. 工作流初始化（注册默认流程）
-- ============================================================
INSERT INTO workflow (code, name, description, status, version)
VALUES (
    'riskDecisionFlow',
    '通用风控决策流程',
    '基于 LiteFlow 的标准风控决策流程，包含基础检测、规则执行和决策判定',
    'PUBLISHED',
    1
) ON CONFLICT (code) DO NOTHING;

-- ============================================================
-- 2. 事件路由初始化（事件类型 → 工作流链路映射）
-- ============================================================
INSERT INTO event_routing (event_type, workflow_code, enabled, description)
VALUES
    ('login',    'riskDecisionFlow', TRUE, '登录场景→通用风控决策流程'),
    ('payment',  'riskDecisionFlow', TRUE, '支付场景→通用风控决策流程'),
    ('register', 'riskDecisionFlow', TRUE, '注册场景→通用风控决策流程')
ON CONFLICT (event_type) DO NOTHING;

-- ============================================================
-- 3. 动态决策阈值初始化（全局默认，不区分流程/事件/用户等级）
-- ============================================================
INSERT INTO decision_threshold
    (workflow_id, event_type, user_level,
     reject_threshold, review_threshold, challenge_threshold,
     enabled, priority, description)
VALUES
    -- 全局默认阈值
    (NULL, NULL, NULL, 80, 50, 30, TRUE, 0,
     '全局默认阈值：拒绝>=80，审核>=50，挑战>=30'),
    -- 新用户更严格
    (NULL, NULL, 'NEW', 60, 40, 20, TRUE, 10,
     '新用户阈值：比默认更严格，拒绝>=60，审核>=40，挑战>=20'),
    -- VIP用户更宽松
    (NULL, NULL, 'VIP', 90, 70, 50, TRUE, 10,
     'VIP用户阈值：比默认更宽松，拒绝>=90，审核>=70，挑战>=50'),
    -- 高风险用户最严格
    (NULL, NULL, 'RISK', 50, 30, 15, TRUE, 20,
     '高风险用户阈值：最严格，拒绝>=50，审核>=30，挑战>=15');

-- ============================================================
-- 4. 登录类规则初始化
-- ============================================================
INSERT INTO rule_config (code, name, type, risk_level, enabled)
VALUES
    ('login_new_device',    '新设备登录',   'login', 'MEDIUM', TRUE),
    ('login_abnormal_time', '异常时间登录', 'login', 'LOW',    TRUE),
    ('login_frequent_fail', '频繁登录失败', 'login', 'HIGH',   TRUE)
ON CONFLICT (code) DO NOTHING;

-- ============================================================
-- 5. 支付类规则初始化
-- ============================================================
INSERT INTO rule_config (code, name, type, risk_level, enabled)
VALUES
    ('payment_large_amount',  '大额支付',     'payment', 'HIGH',   TRUE),
    ('payment_medium_amount', '中额支付',     'payment', 'MEDIUM', TRUE),
    ('payment_first_large',   '首次大额充值', 'payment', 'LOW',    TRUE)
ON CONFLICT (code) DO NOTHING;

-- ============================================================
-- 6. 注册类规则初始化
-- ============================================================
INSERT INTO rule_config (code, name, type, risk_level, enabled)
VALUES
    ('register_with_invite', '邀请码注册（降低风险）', 'register', 'LOW', TRUE)
ON CONFLICT (code) DO NOTHING;

-- ============================================================
-- 7. 测试用黑名单数据
-- ============================================================
INSERT INTO blacklist (type, value, reason, source)
VALUES
    ('IP',   '192.168.100.1', '测试用黑名单IP',   'MANUAL'),
    ('IP',   '10.0.0.99',     '测试用黑名单IP',   'MANUAL'),
    ('USER', 'blocked_user1', '测试用黑名单用户', 'MANUAL');
