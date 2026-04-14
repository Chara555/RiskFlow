package org.example.core.service;

import java.util.concurrent.TimeUnit;

/**
 * 外部威胁情报 IP 缓存服务接口
 */
public interface ThreatIpCacheService {

    /**
     * 获取缓存的风险等级
     * @return 风险等级 (如 SAFE, HIGH)，未命中返回 null
     */
    String getRiskLevel(String ip);

    /**
     * 设置缓存（包括安全结果也要存，防止高频重复调用消耗 API 额度）
     */
    void setRiskLevel(String ip, String level, long duration, TimeUnit unit);
}
