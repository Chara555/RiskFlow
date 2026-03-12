package org.example.conversion.model;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

/**
 * 流程验证结果
 */
@Data
public class ValidationResult {

    /**
     * 错误列表（不为空则验证不通过）
     */
    private final List<String> errors = new ArrayList<>();

    /**
     * 警告列表（不影响执行，仅提示用户）
     */
    private final List<String> warnings = new ArrayList<>();

    /**
     * 是否验证通过
     */
    public boolean isValid() {
        return errors.isEmpty();
    }

    /**
     * 添加错误
     */
    public void addError(String error) {
        errors.add(error);
    }

    /**
     * 添加警告
     */
    public void addWarning(String warning) {
        warnings.add(warning);
    }

    /**
     * 获取第一条错误信息（方便快速提示）
     */
    public String getFirstError() {
        return errors.isEmpty() ? null : errors.get(0);
    }

    @Override
    public String toString() {
        if (isValid()) {
            return "ValidationResult{valid=true, warnings=" + warnings + "}";
        }
        return "ValidationResult{valid=false, errors=" + errors + ", warnings=" + warnings + "}";
    }
}
