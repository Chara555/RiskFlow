package org.example.service.impl.local;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.example.core.service.ThreatIpCacheService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
// 默认加载本地版
@ConditionalOnProperty(name = "riskflow.cache.type", havingValue = "local", matchIfMissing = true)
public class LocalThreatIpCacheServiceImpl implements ThreatIpCacheService {

    // 默认上限 10000 条，24小时过期，防止内存溢出 + 情报过时
    private final Cache<String, String> cache = Caffeine.newBuilder()
            .maximumSize(10000)
            .expireAfterWrite(24, TimeUnit.HOURS)
            .build();

    @Override
    public String getRiskLevel(String ip) {
        return cache.getIfPresent(ip);
    }

    @Override
    public void setRiskLevel(String ip, String level, long duration, TimeUnit unit) {
        // 本地版简化处理，暂不使用动态 duration
        cache.put(ip, level);
    }
}
