package org.example.service;

import lombok.Getter;

import java.util.Map;

/**
 * 规则配置服务接口
 * <p>
 * 提供单个规则组件的动态配置查询能力，支持运营后台动态调整规则参数。
 * </p>
 */
public interface RuleConfigService {

    /**
     * 根据规则代码获取配置
     *
     * @param ruleCode 规则代码（如 LOGIN_TIME_CHECK）
     * @return 规则配置对象，若不存在返回 null
     */
    ComponentConfig getConfig(String ruleCode);

    /**
     * 刷新配置缓存
     */
    void refreshCache();

    /**
     * 组件配置数据载体
     */
    class ComponentConfig {
        private final String ruleCode;
        @Getter
        private final Integer hitScore;
        private final Map<String, Object> params;
        @Getter
        private final boolean enabled;

        public ComponentConfig(String ruleCode, Integer hitScore, Map<String, Object> params, boolean enabled) {
            this.ruleCode = ruleCode;
            this.hitScore = hitScore;
            this.params = params;
            this.enabled = enabled;
        }

        public String getRuleCode() {
            return ruleCode;
        }

        /**
         * 获取扩展参数
         *
         * @param key 参数键
         * @param defaultValue 默认值
         * @return 参数值，不存在时返回默认值
         */
        @SuppressWarnings("unchecked")
        public <T> T getParam(String key, T defaultValue) {
            if (params == null || !params.containsKey(key)) {
                return defaultValue;
            }
            Object value = params.get(key);
            if (value == null) {
                return defaultValue;
            }
            try {
                return (T) value;
            } catch (ClassCastException e) {
                return defaultValue;
            }
        }

        /**
         * 获取整型参数
         */
        public Integer getIntParam(String key, Integer defaultValue) {
            if (params == null || !params.containsKey(key)) {
                return defaultValue;
            }
            Object value = params.get(key);
            if (value instanceof Number) {
                return ((Number) value).intValue();
            }
            return defaultValue;
        }

    }
}
