package org.example.component.rule;

import com.yomahub.liteflow.annotation.LiteflowComponent;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.example.context.RiskFlowContext;
import org.example.core.component.AbstractRiskComponent;
import org.example.core.constant.RiskFeatureKeys;
import org.example.core.model.RiskSignal;
import org.example.service.RuleConfigService;
import org.example.service.RuleConfigService.ComponentConfig;

import java.util.Map;

/**
 * 操作失败次数检测组件（原子能力）
 * <p>
 * 检测用户近期操作失败次数，多次失败可能表示暴力破解或恶意试探。
 * 适用于登录、支付、验证码等任何需要失败次数检测的场景。
 * 支持通过配置中心动态调整风险等级和失败次数阈值。
 * </p>
 */
@Slf4j
@LiteflowComponent("failCountCheck")
public class FailCountCheckComponent extends AbstractRiskComponent {

    private static final String RULE_NAME = "FAIL_COUNT_CHECK";

    // ==================== 兜底默认值 ====================
    private static final String DEFAULT_RISK_LEVEL = RiskSignal.LEVEL_MEDIUM;
    private static final int DEFAULT_FAIL_COUNT_THRESHOLD = 3;

    // ==================== 配置参数键 ====================
    private static final String PARAM_THRESHOLD = "failCountThreshold";

    /**
     * 规则配置服务（通过 Spring 标准注入）
     */
    @Resource
    private RuleConfigService ruleConfigService;

    @Override
    protected RiskSignal doEvaluate(RiskFlowContext context) {
        // 1. 加载动态配置（若服务不可用则使用兜底默认值）
        String riskLevel = DEFAULT_RISK_LEVEL;
        int failCountThreshold = DEFAULT_FAIL_COUNT_THRESHOLD;

        if (ruleConfigService != null) {
            ComponentConfig config = ruleConfigService.getConfig(RULE_NAME);
            if (config != null) {
                // 从配置读取风险等级，未配置则使用默认值
                riskLevel = config.getParam("riskLevel", DEFAULT_RISK_LEVEL);
                failCountThreshold = config.getIntParam(PARAM_THRESHOLD, DEFAULT_FAIL_COUNT_THRESHOLD);
                log.debug("[{}] 加载动态配置成功: riskLevel={}, failCountThreshold={}",
                        RULE_NAME, riskLevel, failCountThreshold);
            } else {
                log.debug("[{}] 配置不存在，使用兜底默认值: riskLevel={}, failCountThreshold={}",
                        RULE_NAME, riskLevel, failCountThreshold);
            }
        } else {
            log.debug("[{}] 配置服务未注入/不可用，使用兜底默认值", RULE_NAME);
        }

        // 2. 从上下文中获取操作失败次数特征（使用常量）
        Object failedCountFeature = context.getFeature(RiskFeatureKeys.FAILED_LOGIN_COUNT);

        // 特征不存在，返回无风险信号
        if (failedCountFeature == null) {
            return RiskSignal.pass(RULE_NAME,
                    "Feature [failedLoginCount] not found, cannot evaluate",
                    "特征 [failedLoginCount] 不存在，无法判断失败次数");
        }

        // 3. 防御性编程：安全解析失败次数
        int failedCount;
        try {
            // 兼容 Number 或 String 的健壮性解析
            failedCount = Integer.parseInt(String.valueOf(failedCountFeature));
        } catch (NumberFormatException e) {
            // 解析失败：极其危险的信号，记录 ERROR 日志
            log.error("[{}] 严重：操作失败次数特征解析失败，原始值='{}', 可能存在绕过探测",
                    RULE_NAME, failedCountFeature);
            return RiskSignal.pass(RULE_NAME,
                    String.format("Feature parse error: failedLoginCount='%s'", failedCountFeature),
                    String.format("特征解析异常: failedLoginCount='%s' 无法转换为整数", failedCountFeature));
        }

        // 4. 判断失败次数是否达到阈值
        if (failedCount >= failCountThreshold) {
            log.warn("[{}] 命中风险规则: 操作失败次数过多, 当前次数={}, 阈值={}",
                    RULE_NAME, failedCount, failCountThreshold);
            return RiskSignal.hit(
                    RULE_NAME,
                    riskLevel,
                    String.format("Too many operation failures (count: %d, threshold: %d)", failedCount, failCountThreshold),
                    String.format("操作失败次数过多 (失败次数: %d, 阈值: %d)", failedCount, failCountThreshold),
                    Map.of("failedCount", failedCount, "threshold", failCountThreshold),
                    "brute_force", "fail_count"
            );
        }

        // 失败次数在正常范围内，返回无风险信号
        return RiskSignal.pass(RULE_NAME,
                String.format("Operation failure count is normal (count: %d)", failedCount),
                String.format("操作失败次数正常 (失败次数: %d)", failedCount));
    }
}
