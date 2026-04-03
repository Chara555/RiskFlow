package org.example.core.constant;

/**
 * 风控特征键常量接口
 * <p>
 * 统一管理所有风控特征的键名，避免魔法字符串，便于重构和维护。
 * </p>
 */
public interface RiskFeatureKeys {

    // ==================== 登录相关特征 ====================

    /**
     * 是否新设备登录
     */
    String IS_NEW_DEVICE = "isNewDevice";

    /**
     * 登录小时（0-23）
     */
    String LOGIN_HOUR = "loginHour";

    /**
     * 登录失败次数
     */
    String FAILED_LOGIN_COUNT = "failedLoginCount";

    /**
     * 登录地理位置
     */
    String LOGIN_LOCATION = "loginLocation";

    /**
     * 登录 IP
     */
    String LOGIN_IP = "loginIp";

    // ==================== 支付相关特征 ====================

    /**
     * 支付金额
     */
    String AMOUNT = "amount";

    /**
     * 是否首次充值
     */
    String IS_FIRST_RECHARGE = "isFirstRecharge";

    /**
     * 收款方账户
     */
    String PAYEE_ACCOUNT = "payeeAccount";

    // ==================== 注册相关特征 ====================

    /**
     * 是否使用邀请码
     */
    String INVITE_CODE_USED = "inviteCodeUsed";

    /**
     * 注册手机号
     */
    String REGISTER_PHONE = "registerPhone";

    // ==================== 设备相关特征 ====================

    /**
     * 设备指纹
     */
    String DEVICE_FINGERPRINT = "deviceFingerprint";

    /**
     * 设备类型
     */
    String DEVICE_TYPE = "deviceType";
}
