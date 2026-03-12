package org.example.service;

import org.example.context.RiskFlowContext;

import java.util.List;

/**
 * 规则服务接口
 */
public interface RuleService {

    /**
     * 获取所有启用的规则
     */
    List<Rule> getEnabledRules();

    /**
     * 根据事件类型获取规则
     */
    List<Rule> getRulesByEventType(String eventType);

    /**
     * 执行规则并返回命中的规则列表
     */
    List<Rule> executeRules(RiskFlowContext context);

    /**
     * 刷新规则缓存
     */
    void refreshCache();

    /**
     * 规则定义
     */
    class Rule {
        private String code;
        private String name;
        private String expression;
        private Integer score;

        public Rule(String code, String name, String expression, Integer score) {
            this.code = code;
            this.name = name;
            this.expression = expression;
            this.score = score;
        }

        public String getCode() { return code; }
        public String getName() { return name; }
        public String getExpression() { return expression; }
        public Integer getScore() { return score; }
    }
}
