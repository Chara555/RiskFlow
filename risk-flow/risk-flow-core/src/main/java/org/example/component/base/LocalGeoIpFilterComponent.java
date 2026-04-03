package org.example.component.base;

import com.yomahub.liteflow.annotation.LiteflowComponent;
import org.example.context.RiskFlowContext;
import org.example.core.component.AbstractRiskComponent;
import org.example.core.model.RiskSignal;

import java.util.Arrays;
import java.util.List;

/**
 * 本地极速 IP 风险检测算子 (基于新一代 Signal 驱动架构)
 * 替代了老旧的 IpBlacklistCheckComponent
 */
@LiteflowComponent(id = "localGeoIpFilter", name = "Local GeoIP Filter")
public class LocalGeoIpFilterComponent extends AbstractRiskComponent {

    // 模拟的本地高危地区黑名单 (真实场景下会读取本地 .mmdb 离线库或 BloomFilter)
    private static final List<String> HIGH_RISK_REGIONS = Arrays.asList("RU", "NG", "SY");

    /**
     * 门神：拦截无效流量。没有 IP 的请求连 doEvaluate 都不配进
     */
    @Override
    public boolean isAccess() {
        RiskFlowContext context = this.getContextBean(RiskFlowContext.class);
        String ip = context.getUserIp();
        return ip != null && !ip.trim().isEmpty();
    }

    /**
     * 核心逻辑：只负责查案打分，不负责直接阻断
     */
    @Override
    protected RiskSignal doEvaluate(RiskFlowContext context) {
        String ip = context.getUserIp();

        // 1. 基础校验：如果是本地环回地址，直接放行，0分
        if ("127.0.0.1".equals(ip) || "0.0.0.0".equals(ip)) {
            return RiskSignal.builder()
                    .scoreContribution(0)
                    .success(true)
                    .evidence("IP is localhost/internal, implicitly trusted.")
                    .tags(List.of("IP_INTERNAL", "TRUSTED"))
                    .build();
        }

        // 2. 模拟 GeoIP 解析 (真实情况这里耗时大约 0.5ms)
        // 这里我们做一个 Mock：假设以 '8' 开头的 IP 都被解析为 'RU' (俄罗斯)
        String regionCode = ip.startsWith("8.") ? "RU" : "CN";

        // 3. 风险判定与信号组装
        if (HIGH_RISK_REGIONS.contains(regionCode)) {
            // 命中高危地区，贡献 45 分风险值
            return RiskSignal.builder()
                    .scoreContribution(45)
                    .success(true)
                    .evidence(String.format("GeoIP parsing resolved to high-risk region: %s", regionCode))
                    .tags(Arrays.asList("GEO_IP", "HIGH_RISK_REGION"))
                    .build();
        }

        // 4. 正常国内 IP，无风险
        return RiskSignal.builder()
                .scoreContribution(0)
                .success(true)
                .evidence(String.format("GeoIP parsing resolved to standard region: %s", regionCode))
                .tags(Arrays.asList("GEO_IP", "STANDARD"))
                .build();
    }
}