package org.example.service;

/**
 * 黑名单服务接口
 */
public interface BlacklistService {

    /**
     * 检查IP是否在黑名单中
     */
    boolean isIpBlacklisted(String ip);

    /**
     * 检查设备是否在黑名单中
     */
    boolean isDeviceBlacklisted(String deviceId);

    /**
     * 检查手机号是否在黑名单中
     */
    boolean isPhoneBlacklisted(String phone);

    /**
     * 检查用户是否在黑名单中
     */
    boolean isUserBlacklisted(String userId);

    /**
     * 刷新黑名单缓存
     */
    void refreshCache();
}
