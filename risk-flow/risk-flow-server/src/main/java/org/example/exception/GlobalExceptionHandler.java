package org.example.exception;

import org.example.core.util.BizException;
import org.example.core.util.ErrorCode;
import org.example.core.util.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 全局异常处理器
 *
 * <p>职责：
 *   统一处理所有 Controller 层抛出的异常，转换为标准化的 Result 响应。
 *   支持国际化消息，根据请求的 Locale 自动选择语言。
 *
 * <p>处理顺序：
 *   1. BizException - 业务异常（已知错误）
 *   2. MethodArgumentNotValidException - 参数校验失败
 *   3. BindException - 参数绑定失败
 *   4. IllegalArgumentException - 非法参数
 *   5. Exception - 兜底异常（未知错误）
 *
 * <p>使用示例：
 *   无需手动调用，由 Spring 自动拦截处理。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @Resource
    private MessageSource messageSource;

    /**
     * 处理业务异常
     *
     * @param e       业务异常
     * @param request HTTP 请求
     * @return 统一响应结果
     */
    @ExceptionHandler(BizException.class)
    public Result<Void> handleBizException(BizException e, HttpServletRequest request) {
        log.warn("业务异常: [{}] {}, URI: {}", e.getCode(), e.getMessageKey(), request.getRequestURI());

        String message = getLocalizedMessage(e.getMessageKey(), e.getArgs());
        return Result.error(e.getCode(), message);
    }

    /**
     * 处理参数校验异常（@Valid 校验失败）
     *
     * @param e       校验异常
     * @param request HTTP 请求
     * @return 统一响应结果
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Map<String, String>> handleValidationException(MethodArgumentNotValidException e, HttpServletRequest request) {
        log.warn("参数校验失败, URI: {}", request.getRequestURI());

        Map<String, String> errors = e.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fieldError -> {
                            String defaultMessage = fieldError.getDefaultMessage();
                            return defaultMessage != null ? defaultMessage : getLocalizedMessage("validation.error");
                        },
                        (existing, replacement) -> existing
                ));

        String message = getLocalizedMessage("param.error");
        return Result.error(ErrorCode.PARAM_ERROR.getCode(), message, errors);
    }

    /**
     * 处理参数绑定异常
     *
     * @param e       绑定异常
     * @param request HTTP 请求
     * @return 统一响应结果
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Map<String, String>> handleBindException(BindException e, HttpServletRequest request) {
        log.warn("参数绑定失败, URI: {}", request.getRequestURI());

        Map<String, String> errors = e.getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fieldError -> {
                            String defaultMessage = fieldError.getDefaultMessage();
                            return defaultMessage != null ? defaultMessage : getLocalizedMessage("validation.error");
                        },
                        (existing, replacement) -> existing
                ));

        String message = getLocalizedMessage("param.error");
        return Result.error(ErrorCode.PARAM_ERROR.getCode(), message, errors);
    }

    /**
     * 处理非法参数异常
     *
     * @param e       非法参数异常
     * @param request HTTP 请求
     * @return 统一响应结果
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleIllegalArgumentException(IllegalArgumentException e, HttpServletRequest request) {
        log.warn("非法参数: {}, URI: {}", e.getMessage(), request.getRequestURI());

        String message = getLocalizedMessage("param.invalid", e.getMessage());
        return Result.error(ErrorCode.PARAM_INVALID.getCode(), message);
    }

    /**
     * 处理非法状态异常
     *
     * @param e       非法状态异常
     * @param request HTTP 请求
     * @return 统一响应结果
     */
    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleIllegalStateException(IllegalStateException e, HttpServletRequest request) {
        log.warn("非法状态: {}, URI: {}", e.getMessage(), request.getRequestURI());

        String message = e.getMessage() != null ? e.getMessage() : getLocalizedMessage("system.error");
        return Result.error(ErrorCode.BAD_REQUEST.getCode(), message);
    }

    /**
     * 处理资源未找到异常（兜底）
     *
     * @param e       异常
     * @param request HTTP 请求
     * @return 统一响应结果
     */
    @ExceptionHandler({java.util.NoSuchElementException.class, jakarta.persistence.EntityNotFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Result<Void> handleNotFoundException(Exception e, HttpServletRequest request) {
        log.warn("资源未找到: {}, URI: {}", e.getMessage(), request.getRequestURI());

        String message = getLocalizedMessage("data.not.found");
        return Result.error(ErrorCode.DATA_NOT_FOUND.getCode(), message);
    }

    /**
     * 兜底异常处理
     *
     * @param e       异常
     * @param request HTTP 请求
     * @return 统一响应结果
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleException(Exception e, HttpServletRequest request) {
        log.error("系统异常, URI: {}", request.getRequestURI(), e);

        String message = getLocalizedMessage("system.error");
        return Result.error(ErrorCode.SYSTEM_ERROR.getCode(), message);
    }

    /**
     * 获取国际化消息
     *
     * @param code 消息 key
     * @param args 动态参数
     * @return 本地化消息
     */
    private String getLocalizedMessage(String code, Object... args) {
        try {
            return messageSource.getMessage(code, args, LocaleContextHolder.getLocale());
        } catch (Exception e) {
            // 如果找不到对应的消息，返回 code 本身
            return code;
        }
    }
}
