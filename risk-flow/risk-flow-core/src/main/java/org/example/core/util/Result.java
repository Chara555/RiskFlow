package org.example.core.util;

import java.io.Serializable;

/**
 * 统一 API 响应结构
 *
 * <p>职责：
 *   为所有 REST API 提供标准化的响应格式，支持国际化消息和统一时间戳。
 *
 * <p>结构说明：
 *   - code: 业务状态码（非 HTTP 状态码），200 表示成功
 *   - msg: 提示信息，支持国际化
 *   - data: 数据体，泛型支持任意类型
 *   - timestamp: 响应时间戳（毫秒），统一使用 RiskTimeUtils 生成
 *
 * <p>使用示例：
 *   <pre>
 *   return Result.success(flowResponse);
 *   return Result.error(ErrorCode.FLOW_NOT_FOUND);
 *   return Result.error(400, "参数错误");
 *   </pre>
 *
 * @param <T> 数据体类型
 */
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 业务状态码（200 表示成功）
     */
    private int code;

    /**
     * 提示信息（国际化消息）
     */
    private String msg;

    /**
     * 数据体
     */
    private T data;

    /**
     * 响应时间戳（毫秒）
     */
    private long timestamp;

    /**
     * 私有构造器，强制使用静态工厂方法
     */
    private Result() {
        this.timestamp = RiskTimeUtils.nowMs();
    }

    /**
     * 构造成功响应
     *
     * @param code 状态码
     * @param msg  消息
     * @param data 数据体
     */
    private Result(int code, String msg, T data) {
        this();
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    // ==================== 静态工厂方法 ====================

    /**
     * 成功响应（无数据）
     *
     * @return Result 实例
     */
    public static <T> Result<T> success() {
        return new Result<>(200, "success", null);
    }

    /**
     * 成功响应（带数据）
     *
     * @param data 数据体
     * @return Result 实例
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "success", data);
    }

    /**
     * 成功响应（带消息和数据）
     *
     * @param msg  成功消息
     * @param data 数据体
     * @return Result 实例
     */
    public static <T> Result<T> success(String msg, T data) {
        return new Result<>(200, msg, data);
    }

    /**
     * 错误响应（带状态码和消息）
     *
     * @param code 错误状态码
     * @param msg  错误消息
     * @return Result 实例
     */
    public static <T> Result<T> error(int code, String msg) {
        return new Result<>(code, msg, null);
    }

    /**
     * 错误响应（带状态码、消息和数据）
     *
     * @param code 错误状态码
     * @param msg  错误消息
     * @param data 错误详情数据
     * @return Result 实例
     */
    public static <T> Result<T> error(int code, String msg, T data) {
        return new Result<>(code, msg, data);
    }

    // ==================== Getter / Setter ====================

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    // ==================== 便捷方法 ====================

    /**
     * 判断是否成功
     *
     * @return true 如果 code == 200
     */
    public boolean isSuccess() {
        return code == 200;
    }

    @Override
    public String toString() {
        return "Result{" +
                "code=" + code +
                ", msg='" + msg + '\'' +
                ", data=" + data +
                ", timestamp=" + timestamp +
                '}';
    }
}
