package org.example.core.model;

import java.util.Map;

/**
 * 外部 API 标准化返回包装
 * @param level 标准化后的风险等级 (SAFE, LOW, MEDIUM, HIGH)
 * @param rawData 厂商返回的原始关键字段，用于后续留痕与精细化决策
 */
public record ApiResult(
        String level,
        Map<String, Object> rawData
) {}