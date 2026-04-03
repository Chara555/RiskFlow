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
 * 登录时间检测组件
 * <p>
 * 检测用户是否在凌晨时段登录，异常时段登录可能表示账号被盗用。
 * 支持通过配置中心动态调整风险分数和时间阈值。
 * </p>
 *
 * <h3>动态配置参数</h3>
 * <ul>
 * <li>hitScore: 命中风险时的贡献分数（默认 5 分）</li>
 * <li>startHour: 风险时段起始小时（默认 0 点，含）</li>
 * <li>endHour: 风险时段结束小时（默认 6 点，不含）</li>
 * </ul>
 */
@Slf4j
@LiteflowComponent("loginTimeCheck")
public class LoginTimeCheckComponent extends AbstractRiskComponent {

    private static final String RULE_NAME = "LOGIN_TIME_CHECK";

    // ==================== 兜底默认值（配置中心不可用时使用） ====================
    private static final int DEFAULT_HIT_SCORE = 5;
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
        int hitScore = DEFAULT_HIT_SCORE;
        int startHour = DEFAULT_START_HOUR;
        int endHour = DEFAULT_END_HOUR;

        // 保留 null 判断，便于不启动 Spring 容器的轻量级单元测试
        if (ruleConfigService != null) {
            ComponentConfig config = ruleConfigService.getConfig(RULE_NAME);
            if (config != null) {
                hitScore = config.getHitScore() != null ? config.getHitScore() : DEFAULT_HIT_SCORE;
                startHour = config.getIntParam(PARAM_START_HOUR, DEFAULT_START_HOUR);
                endHour = config.getIntParam(PARAM_END_HOUR, DEFAULT_END_HOUR);
                log.debug("[{}] 加载动态配置成功: hitScore={}, startHour={}, endHour={}",
                        RULE_NAME, hitScore, startHour, endHour);
            } else {
                log.debug("[{}] 配置不存在，使用兜底默认值: hitScore={}, startHour={}, endHour={}",
                        RULE_NAME, hitScore, startHour, endHour);
            }
        } else {
            log.debug("[{}] 配置服务未注入/不可用，使用兜底默认值", RULE_NAME);
        }

        // 2. 从上下文中获取登录小时特征（使用常量避免魔法字符串）
        Object loginHourFeature = context.getFeature(RiskFeatureKeys.LOGIN_HOUR);

        // 特征不存在，返回无风险信号
        if (loginHourFeature == null) {
            return RiskSignal.pass(RULE_NAME, "特征 [loginHour] 不存在，无法判断登录时段");
        }

        // 3. 防御性编程：安全解析登录小时（兼容 Number 和 String 类型）
        int loginHour;
        try {
            // 统一转为字符串后解析，兼容上游传入的 Number 或 String
            String hourStr = String.valueOf(loginHourFeature);
            loginHour = Integer.parseInt(hourStr);
        } catch (NumberFormatException e) {
            // 解析失败：记录严重错误日志，包含原始特征值以便排查
            log.error("[{}] 严重：登录小时特征解析失败，原始值='{}', 类型='{}', 可能存在恶意攻击或上游数据异常",
                    RULE_NAME, loginHourFeature, loginHourFeature.getClass().getName());
            return RiskSignal.pass(RULE_NAME,
                    String.format("特征解析异常: loginHour='%s' 无法转换为整数", loginHourFeature));
        }

        // 4. 校验小时有效范围（0-23）
        if (loginHour < 0 || loginHour > 23) {
            log.warn("[{}] 登录小时值超出有效范围: loginHour={}", RULE_NAME, loginHour);
            return RiskSignal.pass(RULE_NAME,
                    String.format("登录小时超出有效范围 (loginHour=%d, 有效范围: 0-23)", loginHour));
        }

        // 5. 判断是否在风险时段登录
        if (loginHour >= startHour && loginHour < endHour) {
            log.info("[{}] 命中风险规则: 凌晨时段登录, loginHour={}, 风险时段=[{}:00-{}:00), 贡献分数={}",
                    RULE_NAME, loginHour, startHour, endHour, hitScore);
            return RiskSignal.hit(
                    RULE_NAME,
                    hitScore,
                    String.format("凌晨时段登录 (当前时间: %d点, 风险时段: %d:00-%d:00)", loginHour, startHour, endHour),
                    "odd_hour", "login_risk"
            );
        }

        // 正常时段，返回无风险信号
        return RiskSignal.pass(RULE_NAME, String.format("正常时段登录 (当前时间: %d点)", loginHour));
    }
}