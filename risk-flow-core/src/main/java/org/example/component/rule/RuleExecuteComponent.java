package org.example.component.rule;

import org.example.context.RiskFlowContext;
import org.example.service.RuleService;
import com.yomahub.liteflow.annotation.LiteflowComponent;
import com.yomahub.liteflow.core.NodeComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 规则执行组件 - 通用规则执行器
 */
@LiteflowComponent("ruleExecute")
public class RuleExecuteComponent extends NodeComponent {

    private static final Logger log = LoggerFactory.getLogger(RuleExecuteComponent.class);

    private static RuleService ruleService;

    public static void setStaticRuleService(RuleService service) {
        ruleService = service;
    }

    @Override
    public void process() {
        RiskFlowContext context = this.getContextBean(RiskFlowContext.class);

        String eventType = context.getEventType();

        // 如果有规则服务，从数据库加载规则
        if (ruleService != null) {
            List<RuleService.Rule> matchedRules = ruleService.executeRules(context);
            log.info("[RuleExecute] 规则执行完成，事件类型={}, 命中规则数={}, 总风险评分={}",
                    eventType, matchedRules.size(), context.getTotalRiskScore());
        } else {
            // 兼容旧逻辑
            executeHardcodedRules(context);
        }

        log.info("[RuleExecute] 总风险评分={}", context.getTotalRiskScore());
    }

    /**
     * 兼容旧的硬编码规则
     */
    private void executeHardcodedRules(RiskFlowContext context) {
        String eventType = context.getEventType();

        switch (eventType) {
            case "login":
                executeLoginRules(context);
                break;
            case "payment":
                executePaymentRules(context);
                break;
            case "register":
                executeRegisterRules(context);
                break;
            default:
                executeDefaultRules(context);
        }
    }

    private void executeLoginRules(RiskFlowContext context) {
        Object newDevice = context.getFeature("isNewDevice");
        if (Boolean.TRUE.equals(newDevice)) {
            context.addRuleScore("login_new_device", 20);
        }

        Object loginHour = context.getFeature("loginHour");
        if (loginHour != null) {
            int hour = (int) loginHour;
            if (hour >= 0 && hour < 6) {
                context.addRuleScore("login_abnormal_time", 15);
            }
        }

        Object failedCount = context.getFeature("failedLoginCount");
        if (failedCount != null && (int) failedCount >= 3) {
            context.addRuleScore("login_frequent_fail", 30);
        }
    }

    private void executePaymentRules(RiskFlowContext context) {
        Object amount = context.getFeature("amount");
        if (amount != null) {
            double amt = ((Number) amount).doubleValue();
            if (amt > 10000) {
                context.addRuleScore("payment_large_amount", 40);
            } else if (amt > 5000) {
                context.addRuleScore("payment_medium_amount", 20);
            }
        }

        Object isFirstRecharge = context.getFeature("isFirstRecharge");
        if (Boolean.TRUE.equals(isFirstRecharge)) {
            context.addRuleScore("payment_first_large", 15);
        }
    }

    private void executeRegisterRules(RiskFlowContext context) {
        Object inviteCodeUsed = context.getFeature("inviteCodeUsed");
        if (Boolean.TRUE.equals(inviteCodeUsed)) {
            context.addRuleScore("register_with_invite", -10);
        }
    }

    private void executeDefaultRules(RiskFlowContext context) {
        context.addRuleScore("default_rule", 0);
    }

    @Override
    public boolean isAccess() {
        return true;
    }
}
