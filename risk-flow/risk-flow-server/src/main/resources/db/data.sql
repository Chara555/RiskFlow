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
-- 4. 核心算子动态配置初始化 (信号驱动架构)
-- 【注：已清理所有旧版无 params 的硬编码规则，仅保留动态规则】
-- ============================================================
INSERT INTO rule_config (code, name, type, risk_level, enabled, params)
VALUES
    (
        'DATACENTER_ASN_CHECK',
        '机房与云厂商ASN检测',
        'BLACKLIST',
        'HIGH',
        TRUE,
        '{
          "datacenterAsns": [16509, 14618, 15169, 396982, 8075, 8068, 8069, 37963, 45102, 45090, 132203, 55990, 23455, 14061, 20473, 63949, 62240, 7922, 31898, 16276, 24940],
          "datacenterKeywords": ["amazon", "google", "microsoft", "azure", "alibaba", "tencent", "huawei", "digitalocean", "linode", "akamai", "vultr", "oracle", "ovh", "hetzner"]
        }'::jsonb
    )
ON CONFLICT (code) DO NOTHING;

