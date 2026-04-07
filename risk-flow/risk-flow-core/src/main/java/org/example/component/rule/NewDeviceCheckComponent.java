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
 * 新设备检测组件（原子能力）
 * <p>
 * 检测用户是否使用新设备操作，新设备通常意味着更高的风险。
 * 适用于登录、支付、转账、改密码等任何需要设备风险检测的场景。
 * 支持通过配置中心动态调整风险等级。
 * </p>
 */
@Slf4j
@LiteflowComponent("newDeviceCheck")
public class NewDeviceCheckComponent extends AbstractRiskComponent {

    private static final String RULE_NAME = "NEW_DEVICE_CHECK";

    // ==================== 兜底默认值 ====================
    private static final String DEFAULT_RISK_LEVEL = RiskSignal.LEVEL_LOW;

    /**
     * 规则配置服务（通过 Spring 标准注入）
     */
    @Resource
    private RuleConfigService ruleConfigService;

    @Override
    protected RiskSignal doEvaluate(RiskFlowContext context) {
        // 1. 加载动态配置（若服务不可用则使用兜底默认值）
        String riskLevel = DEFAULT_RISK_LEVEL;

        if (ruleConfigService != null) {
            ComponentConfig config = ruleConfigService.getConfig(RULE_NAME);
            if (config != null) {
                riskLevel = config.getParam("riskLevel", DEFAULT_RISK_LEVEL);
                log.debug("[{}] 加载动态配置成功: riskLevel={}", RULE_NAME, riskLevel);
            } else {
                log.debug("[{}] 配置不存在，使用兜底默认值: riskLevel={}", RULE_NAME, riskLevel);
            }
        } else {
            log.debug("[{}] 配置服务未注入/不可用，使用兜底默认值", RULE_NAME);
        }

        // 2. 从上下文中获取新设备标识特征（使用常量）
        Object newDeviceFeature = context.getFeature(RiskFeatureKeys.IS_NEW_DEVICE);

        // 特征不存在，视为非新设备，返回无风险信号
        if (newDeviceFeature == null) {
            return RiskSignal.pass(RULE_NAME,
                    "Feature [isNewDevice] not found, assuming not a new device",
                    "特征 [isNewDevice] 不存在，视为非新设备");
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
            log.info("[{}] 命中风险规则: 检测到新设备操作, 风险等级={}", RULE_NAME, riskLevel);
            return RiskSignal.hit(
                    RULE_NAME,
                    riskLevel,
                    "New device detected",
                    "检测到新设备操作",
                    Map.of("isNewDevice", true),
                    "new_device"
            );
        }

        // 非新设备，返回无风险信号
        return RiskSignal.pass(RULE_NAME,
                "Not a new device, low risk",
                "非新设备，风险较低");
    }
}
