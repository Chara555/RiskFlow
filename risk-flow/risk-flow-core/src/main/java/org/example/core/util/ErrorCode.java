package org.example.core.util;

/**
 * 业务错误码枚举
 *
 * <p>职责：
 *   定义系统中所有业务错误码，与国际化资源文件 messages.properties 对应。
 *   错误码采用 HTTP 状态码风格，便于理解和排查问题。
 *
 * <p>错误码范围规划：
 *   - 200: 成功
 *   - 400-499: 客户端错误（参数错误、资源不存在等）
 *   - 500-599: 服务端错误（系统内部错误）
 *   - 1000-1999: 流程相关错误
 *   - 2000-2999: 规则相关错误
 *   - 3000-3999: 决策相关错误
 *
 * <p>使用示例：
 *   <pre>
 *   throw new BizException(ErrorCode.FLOW_NOT_FOUND, flowCode);
 *   return Result.error(ErrorCode.PARAM_ERROR.getCode(), msg);
 *   </pre>
 */
public enum ErrorCode {

    // ==================== 通用成功 ====================
    SUCCESS(200, "operation.success"),

    // ==================== 客户端错误 (400-499) ====================
    BAD_REQUEST(400, "bad.request"),
    PARAM_ERROR(400, "param.error"),
    PARAM_MISSING(400, "param.missing"),
    PARAM_INVALID(400, "param.invalid"),
    UNAUTHORIZED(401, "unauthorized"),
    FORBIDDEN(403, "forbidden"),
    NOT_FOUND(404, "not.found"),

    // ==================== 服务端错误 (500-599) ====================
    SYSTEM_ERROR(500, "system.error"),
    INTERNAL_ERROR(500, "internal.error"),
    SERVICE_UNAVAILABLE(503, "service.unavailable"),

    // ==================== 流程相关错误 (1000-1999) ====================
    FLOW_NOT_FOUND(1000, "flow.not.found"),
    FLOW_ALREADY_EXISTS(1001, "flow.already.exists"),
    FLOW_INVALID(1002, "flow.invalid"),
    FLOW_DEPLOY_FAILED(1003, "flow.deploy.failed"),
    FLOW_PUBLISH_FAILED(1004, "flow.publish.failed"),
    FLOW_VERSION_CONFLICT(1005, "flow.version.conflict"),
    FLOW_CONVERT_ERROR(1006, "flow.convert.error"),

    // ==================== 规则相关错误 (2000-2999) ====================
    RULE_NOT_FOUND(2000, "rule.not.found"),
    RULE_ALREADY_EXISTS(2001, "rule.already.exists"),
    RULE_INVALID(2002, "rule.invalid"),
    RULE_EXECUTION_ERROR(2003, "rule.execution.error"),

    // ==================== 决策相关错误 (3000-3999) ====================
    DECISION_ERROR(3000, "decision.error"),
    DECISION_TIMEOUT(3001, "decision.timeout"),
    DECISION_THRESHOLD_NOT_FOUND(3002, "decision.threshold.not.found"),

    // ==================== 黑名单相关错误 (4000-4999) ====================
    BLACKLIST_ALREADY_EXISTS(4000, "blacklist.already.exists"),
    BLACKLIST_NOT_FOUND(4001, "blacklist.not.found"),

    // ==================== 数据相关错误 ====================
    DATA_NOT_FOUND(404, "data.not.found"),
    DATA_ALREADY_EXISTS(409, "data.already.exists"),
    DATA_VALIDATION_ERROR(400, "data.validation.error");

    /**
     * 错误状态码
     */
    private final int code;

    /**
     * 国际化消息 key（对应 messages.properties 中的键）
     */
    private final String messageKey;

    ErrorCode(int code, String messageKey) {
        this.code = code;
        this.messageKey = messageKey;
    }

    /**
     * 获取错误状态码
     *
     * @return 状态码
     */
    public int getCode() {
        return code;
    }

    /**
     * 获取国际化消息 key
     *
     * @return 消息 key
     */
    public String getMessageKey() {
        return messageKey;
    }

    /**
     * 根据状态码查找错误码
     *
     * @param code 状态码
     * @return ErrorCode 枚举，找不到返回 SYSTEM_ERROR
     */
    public static ErrorCode of(int code) {
        for (ErrorCode errorCode : values()) {
            if (errorCode.code == code) {
                return errorCode;
            }
        }
        return SYSTEM_ERROR;
    }
}
