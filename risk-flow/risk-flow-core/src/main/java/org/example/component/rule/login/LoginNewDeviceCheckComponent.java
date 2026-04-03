package org.example.component.rule.login;

import com.yomahub.liteflow.annotation.LiteflowComponent;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.example.context.RiskFlowContext;
import org.example.core.component.AbstractRiskComponent;
import org.example.core.constant.RiskFeatureKeys;
import org.example.core.model.RiskSignal;
import org.example.service.RuleConfigService;
import org.example.service.RuleConfigService.ComponentConfig;

/**
 * 登录新设备检测组件
 * <p>
 * 检测用户是否使用新设备登录，新设备登录通常意味着更高的风险。
 * 支持通过配置中心动态调整风险分数。
 * </p>
 */
@Slf4j
@LiteflowComponent("loginNewDeviceCheck")
public class LoginNewDeviceCheckComponent extends AbstractRiskComponent {

    private static final String RULE_NAME = "LOGIN_NEW_DEVICE_CHECK";

    // ==================== 兜底默认值 ====================
    private static final int DEFAULT_HIT_SCORE = 20;

    /**
     * 规则配置服务（通过 Spring 标准注入）
     */
    @Resource
    private RuleConfigService ruleConfigService;

    @Override
    protected RiskSignal doEvaluate(RiskFlowContext context) {
        // 1. 加载动态配置（若服务不可用则使用兜底默认值）
        int hitScore = DEFAULT_HIT_SCORE;

        if (ruleConfigService != null) {
            ComponentConfig config = ruleConfigService.getConfig(RULE_NAME);
            if (config != null) {
                hitScore = config.getHitScore() != null ? config.getHitScore() : DEFAULT_HIT_SCORE;
                log.debug("[{}] 加载动态配置成功: hitScore={}", RULE_NAME, hitScore);
            } else {
                log.debug("[{}] 配置不存在，使用兜底默认值: hitScore={}", RULE_NAME, hitScore);
            }
        } else {
            log.debug("[{}] 配置服务未注入/不可用，使用兜底默认值", RULE_NAME);
        }

        // 2. 从上下文中获取新设备标识特征（使用常量）
        Object newDeviceFeature = context.getFeature(RiskFeatureKeys.IS_NEW_DEVICE);

        // 特征不存在，视为非新设备，返回无风险信号
        if (newDeviceFeature == null) {
            return RiskSignal.pass(RULE_NAME, "特征 [isNewDevice] 不存在，视为非新设备");
        }

        // 3. 防御性编程：极其健壮的布尔值解析（兼容 Boolean, String "true"/"1", Number 1）
        boolean isNewDevice = false;
        if (newDeviceFeature instanceof Boolean) {
            isNewDevice = (Boolean) newDeviceFeature;
        } else if (newDeviceFeature instanceof String) {
            String strVal = ((String) newDeviceFeature).trim().toLowerCase();
            isNewDevice = "true".equals(strVal) || "1".equals(strVal);
        } else if (newDeviceFeature instanceof Number) {
            isNewDevice = ((Number) newDeviceFeature).intValue() == 1;
        } else {
            log.warn("[{}] 警告：未知的特征类型，无法解析为布尔值。类型='{}', 原始值='{}'。默认放行。",
                    RULE_NAME, newDeviceFeature.getClass().getName(), newDeviceFeature);
        }

        // 4. 判断是否为新设备
        if (isNewDevice) {
            log.info("[{}] 命中风险规则: 检测到新设备登录, 贡献分数={}", RULE_NAME, hitScore);
            return RiskSignal.hit(
                    RULE_NAME,
                    hitScore,
                    "检测到新设备登录",
                    "new_device", "login_risk"
            );
        }

        // 非新设备，返回无风险信号
        return RiskSignal.pass(RULE_NAME, "非新设备登录，风险较低");
    }
}