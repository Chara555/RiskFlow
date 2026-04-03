package org.example.service;

/**
 * 黑名单服务接口 (核心标准 API)
 */
public interface BlacklistService {

    // 1. 核心通用能力 (支持无限扩展维度)

    /**
     * 检查指定维度的值是否在黑名单中
     * @param type 维度类型 (如 IP, DEVICE, EMAIL, CARD_NO 等)
     * @param value 具体的值
     * @return true: 已被拉黑 / false: 安全
     */
    boolean isBlacklisted(String type, String value);

    /**
     * 动态拉黑 (反制能力：供风控规则引擎实时调用)
     * @param type 维度类型
     * @param value 具体的值
     * @param durationMs 拉黑时长（毫秒）。如果为 null 或 0，表示永久拉黑
     */
    void addBlacklist(String type, String value, Long durationMs);

    /**
     * 移除黑名单 (供后台管理界面解封使用)
     */
    void removeBlacklist(String type, String value);


    // 2. 语法糖 (便捷调用，防止硬编码写错 Type)
    default boolean isIpBlacklisted(String ip) {
        return isBlacklisted("IP", ip);
    }

    default boolean isDeviceBlacklisted(String deviceId) {
        return isBlacklisted("DEVICE", deviceId);
    }

    default boolean isPhoneBlacklisted(String phone) {
        return isBlacklisted("PHONE", phone);
    }

    default boolean isUserBlacklisted(String userId) {
        return isBlacklisted("USER", userId);
    }
    // 3. 运维能力

    /**
     * 强制重新加载/清空底层存储或缓存
     */
    void reload();
}