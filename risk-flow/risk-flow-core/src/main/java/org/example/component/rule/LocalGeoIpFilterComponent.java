package org.example.component.rule;

import com.yomahub.liteflow.annotation.LiteflowComponent;
import org.example.context.RiskFlowContext;
import org.example.core.component.AbstractRiskComponent;
import org.example.core.model.RiskSignal;
import org.example.core.service.GeoIpService;
import org.example.service.RuleConfigService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 本地极速 IP 地理位置检测算子 (动态配置版)
 *
 * <p>【业务逻辑】：
 * 基于 MaxMind GeoLite2-Country 离线库解析 IP 国家，命中动态黑名单则报警。
 *
 * <p>【动态配置指南 / 数据库 JSONB 格式】：
 * 对应数据库 code: LOCAL_GEO_IP_FILTER
 * 期望的 JSON 格式：
 * {
 * "highRiskRegions": ["RU", "NG", "SY", "IR", "KP"] // (必填) 高危国家 ISO 简码数组
 * }
 */
@LiteflowComponent(id = "localGeoIpFilter", name = "Local GeoIP Filter")
public class LocalGeoIpFilterComponent extends AbstractRiskComponent {

    private static final String RULE_NAME = "LOCAL_GEO_IP_FILTER";

    @Autowired
    private GeoIpService geoIpService;

    @Autowired
    private RuleConfigService ruleConfigService;

    @Override
    public boolean isAccess() {
        // 1. 拦截无效 IP
        RiskFlowContext context = this.getContextBean(RiskFlowContext.class);
        String ip = context.getUserIp();
        if (ip == null || ip.trim().isEmpty()) {
            return false;
        }

        // 2. GeoIP 库可用性
        if (!geoIpService.isCountryAvailable()) {
            return false;
        }

        // 3. 动态配置降级开关
        RuleConfigService.ComponentConfig config = ruleConfigService.getConfig(RULE_NAME);
        return config != null && config.isEnabled();
    }

    @Override
    protected RiskSignal doEvaluate(RiskFlowContext context) {
        String ip = context.getUserIp();

        if ("127.0.0.1".equals(ip) || "0.0.0.0".equals(ip)) {
            return RiskSignal.pass(RULE_NAME, "IP is localhost", "IP 为本地地址");
        }

        Optional<String> countryOpt = geoIpService.resolveCountryCode(ip);

        if (countryOpt.isEmpty()) {
            return RiskSignal.builder()
                    .ruleName(RULE_NAME)
                    .riskLevel(RiskSignal.LEVEL_NONE)
                    .evidence("GeoIP country resolution unavailable for ip=" + ip)
                    .evidenceZh("GeoIP 国家解析不可用，ip=" + ip)
                    .tags(Arrays.asList("GEO_IP", "GEOIP_UNAVAILABLE"))
                    .success(true)
                    .build();
        }

        String regionCode = countryOpt.get().toUpperCase();

        // 获取动态配置
        RuleConfigService.ComponentConfig config = ruleConfigService.getConfig(RULE_NAME);
        if (config == null) {
            return RiskSignal.pass(RULE_NAME, "Rule config missing", "缺少规则配置");
        }

        String hitRiskLevel = config.getRiskLevel() != null ? config.getRiskLevel() : RiskSignal.LEVEL_HIGH;
        // 动态读取高危地区数组
        List<String> dynamicRegions = config.getParam("highRiskRegions", List.of());

        if (dynamicRegions.contains(regionCode)) {
            return RiskSignal.hit(
                    RULE_NAME,
                    hitRiskLevel,
                    String.format("GeoIP resolved to high-risk region: %s", regionCode),
                    String.format("命中高危地区: %s", regionCode),
                    Map.of("regionCode", regionCode, "ip", ip),
                    "GEO_IP", "HIGH_RISK_REGION"
            );
        }

        return RiskSignal.pass(RULE_NAME, String.format("Standard region: %s", regionCode), "正常地区: " + regionCode);
    }
}