package org.example.core.util;

import java.util.Arrays;

/**
 * 业务异常
 *
 * <p>职责：
 *   封装业务逻辑错误，持有 ErrorCode 和动态参数，支持国际化消息格式化。
 *   所有业务层异常都应使用此类抛出，由 GlobalExceptionHandler 统一处理。
 *
 * <p>使用示例：
 *   <pre>
 *   // 简单抛出
 *   throw new BizException(ErrorCode.FLOW_NOT_FOUND);
 *
 *   // 带动态参数（用于填充消息占位符）
 *   throw new BizException(ErrorCode.FLOW_NOT_FOUND, "myFlowCode");
 *
 *   // 带原始异常
 *   throw new BizException(ErrorCode.SYSTEM_ERROR, e);
 *
 *   // 带参数和原始异常
 *   throw new BizException(ErrorCode.FLOW_CONVERT_ERROR, e, flowCode);
 *   </pre>
 */
public class BizException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * 错误码
     */
    private final ErrorCode errorCode;

    /**
     * 动态参数（用于国际化消息格式化）
     */
    private final Object[] args;

    /**
     * 构造业务异常
     *
     * @param errorCode 错误码
     */
    public BizException(ErrorCode errorCode) {
        super(errorCode.getMessageKey());
        this.errorCode = errorCode;
        this.args = new Object[0];
    }

    /**
     * 构造业务异常（带动态参数）
     *
     * @param errorCode 错误码
     * @param args      动态参数（用于填充消息占位符，如 {0}, {1}）
     */
    public BizException(ErrorCode errorCode, Object... args) {
        super(errorCode.getMessageKey() + " - " + Arrays.toString(args));
        this.errorCode = errorCode;
        this.args = args;
    }

    /**
     * 构造业务异常（带原始异常）
     *
     * @param errorCode 错误码
     * @param cause     原始异常
     */
    public BizException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessageKey(), cause);
        this.errorCode = errorCode;
        this.args = new Object[0];
    }

    /**
     * 构造业务异常（带原始异常和动态参数）
     *
     * @param errorCode 错误码
     * @param cause     原始异常
     * @param args      动态参数
     */
    public BizException(ErrorCode errorCode, Throwable cause, Object... args) {
        super(errorCode.getMessageKey() + " - " + Arrays.toString(args), cause);
        this.errorCode = errorCode;
        this.args = args;
    }

    /**
     * 获取错误码
     *
     * @return ErrorCode
     */
    public ErrorCode getErrorCode() {
        return errorCode;
    }

    /**
     * 获取错误状态码
     *
     * @return 状态码数值
     */
    public int getCode() {
        return errorCode.getCode();
    }

    /**
     * 获取国际化消息 key
     *
     * @return 消息 key
     */
    public String getMessageKey() {
        return errorCode.getMessageKey();
    }

    /**
     * 获取动态参数
     *
     * @return 参数数组
     */
    public Object[] getArgs() {
        return args;
    }

    @Override
    public String toString() {
        return "BizException{" +
                "errorCode=" + errorCode +
                ", code=" + getCode() +
                ", messageKey='" + getMessageKey() + '\'' +
                ", args=" + Arrays.toString(args) +
                "}";
    }
}
