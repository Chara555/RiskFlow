package org.example.service.impl.redis;

import org.example.entity.Blacklist;
import org.example.repository.BlacklistRepository;
import org.example.service.BlacklistService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
// 核心开关：只有配置为 redis 时才加载，不配置则不加载
@ConditionalOnProperty(name = "riskflow.cache.type", havingValue = "redis")
public class RedisBlacklistServiceImpl implements BlacklistService {

    private static final Logger log = LoggerFactory.getLogger(RedisBlacklistServiceImpl.class);

    private final StringRedisTemplate redisTemplate;
    private final BlacklistRepository blacklistRepository;

    // Redis Key 前缀规范
    private static final String CACHE_PREFIX = "riskflow:blacklist:";

    public RedisBlacklistServiceImpl(StringRedisTemplate redisTemplate, BlacklistRepository blacklistRepository) {
        this.redisTemplate = redisTemplate;
        this.blacklistRepository = blacklistRepository;
        // 建议：Redis 模式下，通常由专门的同步任务或后台管理界面触发重载，
        // 如果想启动时强行同步一次，可以取消下面注释：
        // this.reload();
    }

    @Override
    public boolean isBlacklisted(String type, String value) {
        if (value == null || value.isBlank()) return false;

        String key = buildKey(type, value);
        // 直接利用 Redis 的原生存在性判断（Key 存在且未过期即为黑名单）
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    @Override
    public void addBlacklist(String type, String value, Long durationMs) {
        // 1. 持久化到数据库
        Blacklist entity = new Blacklist();
        entity.setType(type.toUpperCase());
        entity.setValue(value);
        entity.setSource("AUTO");
        if (durationMs != null && durationMs > 0) {
            entity.setExpireTime(Instant.now().plusMillis(durationMs));
        }
        blacklistRepository.save(entity);

        // 2. 同步写入 Redis 并设置 TTL
        String key = buildKey(type, value);
        if (durationMs != null && durationMs > 0) {
            // 有效期拉黑
            redisTemplate.opsForValue().set(key, "1", durationMs, TimeUnit.MILLISECONDS);
        } else {
            // 永久拉黑 (设定一个逻辑上的最大值，或者不设 TTL)
            redisTemplate.opsForValue().set(key, "1");
        }
        log.info("[RedisBlacklist] 动态拉黑成功: {}, 过期时间: {}ms", key, durationMs);
    }

    @Override
    public void removeBlacklist(String type, String value) {
        // 从 Redis 移除
        redisTemplate.delete(buildKey(type, value));
        // 注意：数据库通常需要同步更新为已过期或删除，这里建议由 Repository 处理
    }

    @Override
    public void reload() {
        log.info("[RedisBlacklist] 开始从数据库全量同步黑名单到 Redis...");
        // 1. 获取所有有效的黑名单
        List<Blacklist> allValid = blacklistRepository.findAll();

        // 2. 遍历并写入 Redis (建议生产环境用 Pipeline 优化)
        for (Blacklist b : allValid) {
            long ttl = -1;
            if (b.getExpireTime() != null) {
                ttl = b.getExpireTime().toEpochMilli() - Instant.now().toEpochMilli();
                if (ttl <= 0) continue; // 已过期跳过
            }

            String key = buildKey(b.getType(), b.getValue());
            if (ttl > 0) {
                redisTemplate.opsForValue().set(key, "1", ttl, TimeUnit.MILLISECONDS);
            } else {
                redisTemplate.opsForValue().set(key, "1");
            }
        }
        log.info("[RedisBlacklist] 同步完成，共加载 {} 条数据", allValid.size());
    }

    private String buildKey(String type, String value) {
        return CACHE_PREFIX + type.toUpperCase() + ":" + value;
    }
}