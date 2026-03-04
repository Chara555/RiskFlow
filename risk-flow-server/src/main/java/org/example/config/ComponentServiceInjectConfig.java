package org.example.config;

import org.example.component.base.IpBlacklistCheckComponent;
import org.example.component.rule.RuleExecuteComponent;
import org.example.service.BlacklistService;
import org.example.service.RuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

/**
 * 组件服务注入配置
 */
@Configuration
public class ComponentServiceInjectConfig {

    @Autowired(required = false)
    private BlacklistService blacklistService;

    @Autowired(required = false)
    private RuleService ruleService;

    @PostConstruct
    public void injectServices() {
        // 注入 IpBlacklistCheckComponent
        IpBlacklistCheckComponent.setStaticBlacklistService(blacklistService);

        // 注入 RuleExecuteComponent
        RuleExecuteComponent.setStaticRuleService(ruleService);
    }
}
