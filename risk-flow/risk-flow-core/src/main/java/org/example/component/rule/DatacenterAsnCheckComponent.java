package org.example.component.rule;

import com.yomahub.liteflow.annotation.LiteflowComponent;
import org.example.context.RiskFlowContext;
import org.example.core.component.AbstractRiskComponent;
import org.example.core.model.RiskSignal;
import org.example.core.service.GeoIpService;
import org.example.service.RuleConfigService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 机房/云厂商 ASN 检测算子 (动态配置版)
 *
 * <p>【业务逻辑】：
 * 基于 MaxMind GeoLite2-ASN 离线库，解析用户 IP 的 ASN（自治系统号）。
 * 如果查出的 ASN 号或组织名称命中了数据库中配置的"黑名单"，则产出对应风险等级的信号。
 *
 * <p>【动态配置指南 / 数据库 JSONB 格式约定】：
 * 本算子已彻底去硬编码，所有黑名单阈值均从 rule_config 表的 params (JSONB) 字段动态加载。
 * 对应数据库 code: DATACENTER_ASN_CHECK
 * * 期望的 JSON 格式如下：
 * {
 * "datacenterAsns": [16509, 14618, 15169, 37963, 45102], // (选填) 精确匹配的 ASN 号数组，类型为 List<Integer>
 * "datacenterKeywords": ["amazon", "google", "aliyun", "tencent"] // (选填) 模糊匹配的云厂商小写名称数组，类型为 List<String>
 * }
 *
 * <p>【如何热更新策略？】：
 * 1. 增删名单：直接在管理后台修改上述 JSON 内容，或手动 UPDATE 数据库的 params 字段。
 * 2. 策略降级：修改 rule_config 表的 risk_level 字段（如 HIGH 改为 MEDIUM）。
 * 3. 熔断停用：修改 rule_config 表的 enabled 字段为 false。
 * 修改后，调用 RuleConfigService 的缓存刷新接口，本算子将在下一毫秒立刻按新规则执行，无需重启服务。
 */
@LiteflowComponent(id = "datacenterAsnCheck", name = "Datacenter ASN Check")
public class DatacenterAsnCheckComponent extends AbstractRiskComponent {

    private static final String RULE_NAME = "DATACENTER_ASN_CHECK";

    @Autowired
    private GeoIpService geoIpService;

    // 引入动态配置服务
    @Autowired
    private RuleConfigService ruleConfigService;

    /**
     * 门神：库未加载，或者规则被动态禁用时，跳过本节点
     */
    @Override
    public boolean isAccess() {
        if (!geoIpService.isAsnAvailable()) {
            return false;
        }
        RuleConfigService.ComponentConfig config = ruleConfigService.getConfig(RULE_NAME);
        // 如果数据库中配置了禁用，则算子不执行
        return config != null && config.isEnabled();
    }

    @Override
    protected RiskSignal doEvaluate(RiskFlowContext context) {
        String ip = context.getUserIp();

        // 修复健壮性隐患：增加判空
        if (ip == null || ip.isBlank()) {
            return RiskSignal.pass(RULE_NAME, "IP is missing", "缺少 IP 地址");
        }

        if ("127.0.0.1".equals(ip) || "0.0.0.0".equals(ip)) {
            return RiskSignal.pass(RULE_NAME, "IP is localhost", "IP 为本地地址");
        }

        Optional<GeoIpService.AsnInfo> asnOpt = geoIpService.resolveAsn(ip);
        if (asnOpt.isEmpty()) {
            return RiskSignal.pass(RULE_NAME, "ASN resolution failed", "ASN 解析无结果");
        }

        GeoIpService.AsnInfo asnInfo = asnOpt.get();
        Long asnNumber = asnInfo.asnNumber();
        String asnOrg = asnInfo.asnOrganization() != null ? asnInfo.asnOrganization() : "";

        // 1. 获取动态配置
        RuleConfigService.ComponentConfig config = ruleConfigService.getConfig(RULE_NAME);
        if (config == null) {
            return RiskSignal.pass(RULE_NAME, "Rule config missing", "缺少规则配置，默认放行");
        }

        // 2. 动态读取参数与风险等级
        String hitRiskLevel = config.getRiskLevel() != null ? config.getRiskLevel() : RiskSignal.LEVEL_HIGH;
        // 这里的 key 必须和数据库 params 里的 JSON key 一致
        // 防御性转换：JSON 反序列化可能存为 Long/Double，统一转为 int
        List<Number> rawAsns = config.getParam("datacenterAsns", List.of());
        List<Integer> dynamicAsns = rawAsns.stream().map(Number::intValue).toList();
        List<String> dynamicKeywords = config.getParam("datacenterKeywords", List.of());

        // 3. 精确匹配：从动态列表中比对
        if (asnNumber != null && dynamicAsns.contains(asnNumber.intValue())) {
            return RiskSignal.hit(
                    RULE_NAME,
                    hitRiskLevel, // 使用动态风险等级
                    String.format("IP belongs to datacenter ASN: %d (%s)", asnNumber, asnOrg),
                    String.format("命中机房 ASN: %d (%s)", asnNumber, asnOrg),
                    Map.of("asnNumber", asnNumber, "asnOrganization", asnOrg, "ip", ip),
                    "DATACENTER", "CLOUD_ASN"
            );
        }

        // 4. 模糊匹配：从动态关键词中比对
        String orgLower = asnOrg.toLowerCase();
        for (String keyword : dynamicKeywords) {
            if (orgLower.contains(keyword)) {
                // 关键词命中，也可以考虑统一使用 hitRiskLevel，或者单独配置。这里统一使用动态等级。
                return RiskSignal.hit(
                        RULE_NAME,
                        hitRiskLevel,
                        String.format("ASN matches datacenter keyword '%s': %s", keyword, asnOrg),
                        String.format("命中机房关键词 '%s': %s", keyword, asnOrg),
                        Map.of("asnNumber", asnNumber, "asnOrganization", asnOrg, "matchedKeyword", keyword),
                        "DATACENTER", "CLOUD_KEYWORD"
                );
            }
        }

        return RiskSignal.pass(RULE_NAME, "Non-datacenter ASN", "非机房 ASN");
    }
}
