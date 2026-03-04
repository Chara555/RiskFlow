package org.example.service.impl;

import org.example.context.RiskFlowContext;
import org.example.entity.RuleConfig;
import org.example.repository.RuleConfigRepository;
import org.example.service.RuleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 规则服务实现
 */
@Service
public class RuleServiceImpl implements RuleService {

    private static final Logger log = LoggerFactory.getLogger(RuleServiceImpl.class);

    private final RuleConfigRepository ruleConfigRepository;

    // 缓存规则列表
    private volatile List<Rule> cachedRules = new ArrayList<>();

    public RuleServiceImpl(RuleConfigRepository ruleConfigRepository) {
        this.ruleConfigRepository = ruleConfigRepository;
        refreshCache();
    }

    @Override
    public List<Rule> getEnabledRules() {
        return cachedRules;
    }

    @Override
    public List<Rule> getRulesByEventType(String eventType) {
        // 根据事件类型筛选规则（简化实现：规则名包含事件类型则匹配）
        List<Rule> result = new ArrayList<>();
        for (Rule rule : cachedRules) {
            String ruleCode = rule.getCode();
            // 规则编码以事件类型开头，如 login_new_device
            if (ruleCode.startsWith(eventType + "_")) {
                result.add(rule);
            }
        }
        return result;
    }

    @Override
    public List<Rule> executeRules(RiskFlowContext context) {
        List<Rule> matchedRules = new ArrayList<>();
        List<Rule> rules = getRulesByEventType(context.getEventType());

        for (Rule rule : rules) {
            if (evaluateRule(rule, context)) {
                matchedRules.add(rule);
                context.addRuleScore(rule.getCode(), rule.getScore());
                log.info("[RuleExecute] 规则命中: {} - {}，加分: {}",
                        rule.getCode(), rule.getName(), rule.getScore());
            }
        }

        return matchedRules;
    }

    @Override
    public void refreshCache() {
        log.info("开始刷新规则缓存...");
        List<RuleConfig> configs = ruleConfigRepository.findByEnabledTrueOrderByPriorityAsc();

        List<Rule> rules = new ArrayList<>();
        for (RuleConfig config : configs) {
            rules.add(new Rule(
                    config.getCode(),
                    config.getName(),
                    config.getExpression(),
                    config.getScore()
            ));
        }

        cachedRules = rules;
        log.info("规则缓存刷新完成，共 {} 条规则", rules.size());
    }

    /**
     * 简单规则评估
     * 实际生产中可使用 Drools/Aviator 等规则引擎
     */
    private boolean evaluateRule(Rule rule, RiskFlowContext context) {
        String expression = rule.getExpression();
        Map<String, Object> features = context.getFeatures();

        if (features == null || expression == null) {
            return false;
        }

        try {
            // 简化实现：支持简单的 key == value 和 key > value 表达式
            return evaluateSimpleExpression(expression, features);
        } catch (Exception e) {
            log.warn("规则评估失败: {} - {}", rule.getCode(), e.getMessage());
            return false;
        }
    }

    /**
     * 评估简单表达式
     * 支持: isNewDevice == true, amount > 10000, failedLoginCount >= 3
     */
    private boolean evaluateSimpleExpression(String expression, Map<String, Object> features) {
        expression = expression.trim();

        // 处理 == 表达式
        if (expression.contains("==")) {
            String[] parts = expression.split("==");
            String key = parts[0].trim();
            String value = parts[1].trim();

            Object featureValue = features.get(key);
            if (featureValue == null) {
                return false;
            }

            // 处理布尔值
            if ("true".equalsIgnoreCase(value)) {
                return Boolean.TRUE.equals(featureValue);
            } else if ("false".equalsIgnoreCase(value)) {
                return Boolean.FALSE.equals(featureValue);
            }
            // 处理字符串
            return value.equals(String.valueOf(featureValue));
        }

        // 处理 > >= < <= 表达式
        if (expression.contains(">=")) {
            String[] parts = expression.split(">=");
            return evaluateNumeric(expression, parts, features, ">=");
        } else if (expression.contains(">")) {
            String[] parts = expression.split(">");
            return evaluateNumeric(expression, parts, features, ">");
        } else if (expression.contains("<=")) {
            String[] parts = expression.split("<=");
            return evaluateNumeric(expression, parts, features, "<=");
        } else if (expression.contains("<")) {
            String[] parts = expression.split("<");
            return evaluateNumeric(expression, parts, features, "<");
        }

        return false;
    }

    private boolean evaluateNumeric(String expression, String[] parts, Map<String, Object> features, String operator) {
        if (parts.length != 2) {
            return false;
        }

        String key = parts[0].trim();
        String valueStr = parts[1].trim();

        Object featureValue = features.get(key);
        if (featureValue == null) {
            return false;
        }

        try {
            double featureNum = ((Number) featureValue).doubleValue();
            double targetNum = Double.parseDouble(valueStr);

            return switch (operator) {
                case ">" -> featureNum > targetNum;
                case ">=" -> featureNum >= targetNum;
                case "<" -> featureNum < targetNum;
                case "<=" -> featureNum <= targetNum;
                default -> false;
            };
        } catch (Exception e) {
            log.warn("数值评估失败: {}", expression);
            return false;
        }
    }
}
