package org.example.service.impl;

import org.example.context.RiskFlowContext;
import org.example.entity.RuleConfig;
import org.example.repository.RuleConfigRepository;
import org.example.service.RuleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RuleServiceImplTest {

    @Mock
    private RuleConfigRepository ruleConfigRepository;

    private RuleServiceImpl ruleService;

    @BeforeEach
    void setUp() {
        when(ruleConfigRepository.findByEnabledTrueOrderByPriorityAsc()).thenReturn(List.of(
                ruleConfig("login_new_device", "新设备登录", "isNewDevice == true", 20),
                ruleConfig("login_failed_count", "登录失败次数", "failedLoginCount >= 3", 30),
                ruleConfig("payment_large_amount", "大额支付", "amount > 10000", 40)
        ));
        ruleService = new RuleServiceImpl(ruleConfigRepository);
    }

    @Test
    void executeRules_shouldMatchOnlyCurrentEventTypeRules() {
        RiskFlowContext context = new RiskFlowContext();
        context.setEventType("login");
        context.setFeatures(Map.of(
                "isNewDevice", true,
                "failedLoginCount", 4,
                "amount", 20000
        ));

        List<RuleService.Rule> matchedRules = ruleService.executeRules(context);

        assertEquals(2, matchedRules.size());
        assertTrue(matchedRules.stream().map(RuleService.Rule::getCode).toList().contains("login_new_device"));
        assertTrue(matchedRules.stream().map(RuleService.Rule::getCode).toList().contains("login_failed_count"));
        assertEquals(50, context.getTotalRiskScore());
    }

    @Test
    void executeRules_shouldSkipInvalidOrUnmatchedExpressions() {
        when(ruleConfigRepository.findByEnabledTrueOrderByPriorityAsc()).thenReturn(List.of(
                ruleConfig("login_invalid_expression", "非法表达式", "amount > abc", 20),
                ruleConfig("login_missing_feature", "缺失字段", "unknownFeature == true", 30)
        ));
        ruleService = new RuleServiceImpl(ruleConfigRepository);

        RiskFlowContext context = new RiskFlowContext();
        context.setEventType("login");
        context.setFeatures(Map.of("amount", 100));

        List<RuleService.Rule> matchedRules = ruleService.executeRules(context);

        assertTrue(matchedRules.isEmpty());
        assertEquals(0, context.getTotalRiskScore());
    }

    private RuleConfig ruleConfig(String code, String name, String expression, int score) {
        RuleConfig config = new RuleConfig();
        config.setCode(code);
        config.setName(name);
        config.setExpression(expression);
        config.setScore(score);
        return config;
    }
}
