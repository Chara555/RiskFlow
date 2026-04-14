package org.example.service.impl.local;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.example.core.util.RiskTimeUtils;
import org.example.entity.Blacklist;
import org.example.repository.BlacklistRepository;
import org.example.service.BlacklistService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
//如果没有配置，默认加载这个类
@ConditionalOnProperty(name = "riskflow.cache.type", havingValue = "local", matchIfMissing = true)
public class LocalBlacklistServiceImpl implements BlacklistService {

    private static final Logger log = LoggerFactory.getLogger(LocalBlacklistServiceImpl.class);

    private final BlacklistRepository blacklistRepository;

    // 核心改造：使用 Caffeine 替代 ConcurrentHashMap，最多存 1万条，5分钟自动过期重新查库
    private final Cache<String, Boolean> cache = Caffeine.newBuilder()
            .maximumSize(10000)
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build();

    public LocalBlacklistServiceImpl(BlacklistRepository blacklistRepository) {
        this.blacklistRepository = blacklistRepository;
    }

    @Override
    public boolean isBlacklisted(String type, String value) {
        if (value == null || value.isBlank()) return false;

        String cacheKey = type.toUpperCase() + ":" + value;

        // Read-Through 模式：缓存有直接返回，没有就执行 lambda 去数据库查
        return cache.get(cacheKey, key -> {
            log.debug("本地缓存未命中，查询 DB: {}", key);
            // 使用 Repository 定义的 JPQL 方法，自动处理过期时间判断
            return blacklistRepository.existsValidBlacklist(type.toUpperCase(), value);
        });
    }

    @Override
    public void addBlacklist(String type, String value, Long durationMs) {
        // 1. 存入数据库（持久化）
        Blacklist entity = new Blacklist();
        entity.setType(type.toUpperCase());
        entity.setValue(value);
        if (durationMs != null && durationMs > 0) {
            entity.setExpireTime(RiskTimeUtils.now().plusMillis(durationMs));
        }
        entity.setSource("AUTO"); // 标记为系统自动拉黑
        blacklistRepository.save(entity);

        // 2. 同步更新本地缓存
        cache.put(type.toUpperCase() + ":" + value, true);
        log.info("动态拉黑成功: type={}, value={}, durationMs={}", type, value, durationMs);
    }

    @Override
    public void removeBlacklist(String type, String value) {
        // 实际业务中通常是逻辑删除或修改过期时间，这里简化处理
        cache.invalidate(type.toUpperCase() + ":" + value);
    }

    @Override
    public void reload() {
        log.info("执行本地黑名单缓存清空指令...");
        cache.invalidateAll();
    }
}