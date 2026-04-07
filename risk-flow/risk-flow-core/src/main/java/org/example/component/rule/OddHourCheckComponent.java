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
 * 异常时段检测组件（原子能力）
 * <p>
 * 检测用户是否在异常时段（如凌晨）执行操作，异常时段操作可能表示账号被盗用。
 * 适用于登录、支付、转账等任何需要时段风险检测的场景。
 * 支持通过配置中心动态调整风险等级和时间阈值。
 * </p>
 *
 * <h3>动态配置参数</h3>
 * <ul>
 * <li>riskLevel: 命中风险时的风险等级（默认 LOW）</li>
 * <li>startHour: 风险时段起始小时（默认 0 点，含）</li>你
 * <li>endHour: 风险时段结束小时（默认 6 点，不含）</li>
 * </ul>
 */
@Slf4j
@LiteflowComponent("oddHourCheck")
public class OddHourCheckComponent extends AbstractRiskComponent {

    private static final String RULE_NAME = "ODD_HOUR_CHECK";

    // ==================== 兜底默认值（配置中心不可用时使用） ====================
    private static final String DEFAULT_RISK_LEVEL = RiskSignal.LEVEL_LOW;
    private static final int DEFAULT_START_HOUR = 0;
    private static final int DEFAULT_END_HOUR = 6;

    // ==================== 配置参数键 ====================
    private static final String PARAM_START_HOUR = "startHour";
    private static final String PARAM_END_HOUR = "endHour";

    /**
     * 规则配置服务（通过 Spring 标准注入，拥抱 IoC 容器）
     */
    @Resource
    private RuleConfigService ruleConfigService;

    @Override
    protected RiskSignal doEvaluate(RiskFlowContext context) {
        // 1. 加载动态配置（若服务不可用则使用兜底默认值）
        String riskLevel = DEFAULT_RISK_LEVEL;
        int startHour = DEFAULT_START_HOUR;
        int endHour = DEFAULT_END_HOUR;

        // 保留 null 判断，便于不启动 Spring 容器的轻量级单元测试
        if (ruleConfigService != null) {
            ComponentConfig config = ruleConfigService.getConfig(RULE_NAME);
            if (config != null) {
                riskLevel = config.getParam("riskLevel", DEFAULT_RISK_LEVEL);
                startHour = config.getIntParam(PARAM_START_HOUR, DEFAULT_START_HOUR);
                endHour = config.getIntParam(PARAM_END_HOUR, DEFAULT_END_HOUR);
                log.debug("[{}] 加载动态配置成功: riskLevel={}, startHour={}, endHour={}",
                        RULE_NAME, riskLevel, startHour, endHour);
            } else {
                log.debug("[{}] 配置不存在，使用兜底默认值: riskLevel={}, startHour={}, endHour={}",
                        RULE_NAME, riskLevel, startHour, endHour);
            }
        } else {
            log.debug("[{}] 配置服务未注入/不可用，使用兜底默认值", RULE_NAME);
        }

        // 2. 从上下文中获取操作小时特征（使用常量避免魔法字符串）
        Object loginHourFeature = context.getFeature(RiskFeatureKeys.LOGIN_HOUR);

        // 特征不存在，返回无风险信号
        if (loginHourFeature == null) {
            return RiskSignal.pass(RULE_NAME,
                    "Feature [loginHour] not found, cannot evaluate",
                    "特征 [loginHour] 不存在，无法判断操作时段");
        }

        // 3. 防御性编程：安全解析操作小时（兼容 Number 和 String 类型）
        int loginHour;
        try {
            // 统一转为字符串后解析，兼容上游传入的 Number 或 String
            String hourStr = String.valueOf(loginHourFeature);
            loginHour = Integer.parseInt(hourStr);
        } catch (NumberFormatException e) {
            // 解析失败：记录严重错误日志，包含原始特征值以便排查
            log.error("[{}] 严重：操作小时特征解析失败，原始值='{}', 类型='{}', 可能存在恶意攻击或上游数据异常",
                    RULE_NAME, loginHourFeature, loginHourFeature.getClass().getName());
            return RiskSignal.pass(RULE_NAME,
                    String.format("Feature parse error: loginHour='%s'", loginHourFeature),
                    String.format("特征解析异常: loginHour='%s' 无法转换为整数", loginHourFeature));
        }

        // 4. 校验小时有效范围（0-23）
        if (loginHour < 0 || loginHour > 23) {
            log.warn("[{}] 操作小时值超出有效范围: loginHour={}", RULE_NAME, loginHour);
            return RiskSignal.pass(RULE_NAME,
                    String.format("Operation hour out of valid range (hour=%d, valid: 0-23)", loginHour),
                    String.format("操作小时超出有效范围 (hour=%d, 有效范围: 0-23)", loginHour));
        }

        // 5. 判断是否在风险时段操作
        if (loginHour >= startHour && loginHour < endHour) {
            log.info("[{}] 命中风险规则: 异常时段操作, hour={}, 风险时段=[{}:00-{}:00), 风险等级={}",
                    RULE_NAME, loginHour, startHour, endHour, riskLevel);
            return RiskSignal.hit(
                    RULE_NAME,
                    riskLevel,
                    String.format("Odd hour operation detected (current: %d:00, risk window: %d:00-%d:00)", loginHour, startHour, endHour),
                    String.format("异常时段操作 (当前时间: %d点, 风险时段: %d:00-%d:00)", loginHour, startHour, endHour),
                    Map.of("operationHour", loginHour, "riskWindow", startHour + "-" + endHour),
                    "odd_hour"
            );
        }

        // 正常时段，返回无风险信号
        return RiskSignal.pass(RULE_NAME,
                String.format("Normal operation time (current: %d:00)", loginHour),
                String.format("正常时段操作 (当前时间: %d点)", loginHour));
    }
}
