package org.example.service.impl.redis;

import org.example.core.service.ThreatIpCacheService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
// 显式配置为 redis 且 StringRedisTemplate 存在时才加载
@ConditionalOnProperty(name = "riskflow.cache.type", havingValue = "redis")
@ConditionalOnBean(StringRedisTemplate.class)
public class RedisThreatIpCacheServiceImpl implements ThreatIpCacheService {

    private final StringRedisTemplate redisTemplate;
    private static final String CACHE_PREFIX = "riskflow:threat_cache:";

    public RedisThreatIpCacheServiceImpl(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public String getRiskLevel(String ip) {
        return redisTemplate.opsForValue().get(CACHE_PREFIX + ip);
    }

    @Override
    public void setRiskLevel(String ip, String level, long duration, TimeUnit unit) {
        redisTemplate.opsForValue().set(CACHE_PREFIX + ip, level, duration, unit);
    }
}
