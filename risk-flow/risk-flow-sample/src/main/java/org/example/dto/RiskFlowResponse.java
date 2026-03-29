package org.example.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 风控决策响应 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskFlowResponse {

    /**
     * 决策 ID（即 eventId）
     */
    private String decisionId;

    /**
     * 决策结果：ACCEPT / CHALLENGE / REVIEW / REJECT
     */
    private String result;

    /**
     * 风险评分
     */
    private Integer riskScore;

    /**
     * 决策描述
     */
    private String message;

    /**
     * 决策时间
     */
    private LocalDateTime decisionTime;

    /**
     * 执行耗时（毫秒）
     */
    private Long executionTimeMs;

    /**
     * 详细信息
     */
    private Map<String, Object> details;

    /**
     * AI 分析结果（如果有）
     */
    private String aiAnalysisResult;

    /**
     * 创建成功响应用于测试
     */
    public static RiskFlowResponse success(String eventId, String result, Integer score) {
        return RiskFlowResponse.builder()
                .decisionId(eventId)
                .result(result)
                .riskScore(score)
                .message("风控决策完成")
                .decisionTime(LocalDateTime.now())
                .executionTimeMs(System.currentTimeMillis())
                .build();
    }
}
