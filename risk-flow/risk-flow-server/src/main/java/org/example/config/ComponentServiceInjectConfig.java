package org.example.config;

import org.example.component.base.IpBlacklistCheckComponent;
import org.example.component.decision.DecisionJudgeComponent;
import org.example.component.rule.RuleExecuteComponent;
import org.example.service.BlacklistService;
import org.example.service.DecisionThresholdService;
import org.example.service.RuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

/**
 * 组件服务注入配置
 * LiteFlow 组件通过静态字段持有 Spring Bean 引用
 */
@Configuration
public class ComponentServiceInjectConfig {

    @Autowired(required = false)
    private BlacklistService blacklistService;

    @Autowired(required = false)
    private RuleService ruleService;

    @Autowired(required = false)
    private DecisionThresholdService decisionThresholdService;

    @PostConstruct
    public void injectServices() {
        // 注入 IpBlacklistCheckComponent
        IpBlacklistCheckComponent.setStaticBlacklistService(blacklistService);

        // 注入 RuleExecuteComponent
        RuleExecuteComponent.setStaticRuleService(ruleService);

        // 注入 DecisionJudgeComponent
        DecisionJudgeComponent.setStaticThresholdService(decisionThresholdService);
    }
}
