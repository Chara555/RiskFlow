package org.example.service.impl;

import org.example.entity.Blacklist;
import org.example.repository.BlacklistRepository;
import org.example.service.BlacklistService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 黑名单服务实现
 */
@Service
public class BlacklistServiceImpl implements BlacklistService {

    private static final Logger log = LoggerFactory.getLogger(BlacklistServiceImpl.class);

    private final BlacklistRepository blacklistRepository;

    // 内存缓存
    private final Map<String, Boolean> ipCache = new ConcurrentHashMap<>();
    private final Map<String, Boolean> deviceCache = new ConcurrentHashMap<>();
    private final Map<String, Boolean> phoneCache = new ConcurrentHashMap<>();
    private final Map<String, Boolean> userCache = new ConcurrentHashMap<>();

    public BlacklistServiceImpl(BlacklistRepository blacklistRepository) {
        this.blacklistRepository = blacklistRepository;
        // 启动时加载缓存
        refreshCache();
    }

    @Override
    public boolean isIpBlacklisted(String ip) {
        if (ip == null || ip.isEmpty()) {
            return false;
        }
        return ipCache.getOrDefault(ip, false);
    }

    @Override
    public boolean isDeviceBlacklisted(String deviceId) {
        if (deviceId == null || deviceId.isEmpty()) {
            return false;
        }
        return deviceCache.getOrDefault(deviceId, false);
    }

    @Override
    public boolean isPhoneBlacklisted(String phone) {
        if (phone == null || phone.isEmpty()) {
            return false;
        }
        return phoneCache.getOrDefault(phone, false);
    }

    @Override
    public boolean isUserBlacklisted(String userId) {
        if (userId == null || userId.isEmpty()) {
            return false;
        }
        return userCache.getOrDefault(userId, false);
    }

    @Override
    public void refreshCache() {
        log.info("开始刷新黑名单缓存...");
        List<Blacklist> allBlacklist = blacklistRepository.findAll();

        // 清空缓存
        ipCache.clear();
        deviceCache.clear();
        phoneCache.clear();
        userCache.clear();

        LocalDateTime now = LocalDateTime.now();

        // 重新加载
        for (Blacklist item : allBlacklist) {
            // 跳过过期的
            if (item.getExpireTime() != null && item.getExpireTime().isBefore(now)) {
                continue;
            }

            switch (item.getType().toUpperCase()) {
                case "IP":
                    ipCache.put(item.getValue(), true);
                    break;
                case "DEVICE":
                    deviceCache.put(item.getValue(), true);
                    break;
                case "PHONE":
                    phoneCache.put(item.getValue(), true);
                    break;
                case "USER":
                    userCache.put(item.getValue(), true);
                    break;
            }
        }

        log.info("黑名单缓存刷新完成: IP={}, DEVICE={}, PHONE={}, USER={}",
                ipCache.size(), deviceCache.size(), phoneCache.size(), userCache.size());
    }
}
