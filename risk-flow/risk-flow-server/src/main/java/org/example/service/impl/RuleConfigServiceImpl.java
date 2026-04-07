package org.example.service.impl;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.example.entity.RuleConfig;
import org.example.repository.RuleConfigRepository;
import org.example.service.RuleConfigService;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 规则配置服务实现
 * <p>
 * 提供规则组件的动态配置查询能力，支持内存缓存和动态刷新。
 * </p>
 */
@Slf4j
@Service
public class RuleConfigServiceImpl implements RuleConfigService {

    private final RuleConfigRepository ruleConfigRepository;

    /**
     * 配置缓存：key = ruleCode, value = ComponentConfig
     */
    private final Map<String, ComponentConfig> configCache = new ConcurrentHashMap<>();

    public RuleConfigServiceImpl(RuleConfigRepository ruleConfigRepository) {
        this.ruleConfigRepository = ruleConfigRepository;
    }

    /**
     * 启动时加载配置到缓存
     */
    @PostConstruct
    public void init() {
        refreshCache();
        log.info("[RuleConfigService] 初始化完成，已加载 {} 条规则配置", configCache.size());
    }

    @Override
    public ComponentConfig getConfig(String ruleCode) {
        if (ruleCode == null || ruleCode.isBlank()) {
            return null;
        }
        return configCache.get(ruleCode);
    }

    @Override
    public void refreshCache() {
        log.info("[RuleConfigService] 开始刷新配置缓存...");

        // 清空旧缓存
        configCache.clear();

        // 从数据库加载所有启用的配置
        var configs = ruleConfigRepository.findByEnabledTrue();

        for (RuleConfig entity : configs) {
            // ✅ 核心修改：将 getScore() 替换为 getRiskLevel()
            ComponentConfig config = new ComponentConfig(
                    entity.getCode(),
                    entity.getRiskLevel(),
                    entity.getParams(),
                    entity.getEnabled()
            );
            configCache.put(entity.getCode(), config);

            // ✅ 核心修改：日志打印也同步改为 riskLevel
            log.debug("[RuleConfigService] 加载配置: code={}, riskLevel={}, params={}",
                    entity.getCode(), entity.getRiskLevel(), entity.getParams());
        }

        log.info("[RuleConfigService] 配置缓存刷新完成，共加载 {} 条配置", configCache.size());
    }
}