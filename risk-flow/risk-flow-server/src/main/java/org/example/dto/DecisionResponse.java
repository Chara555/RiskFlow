package org.example.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 风控决策响应 DTO
 * 返回给调用方的最终决策结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DecisionResponse {

    /** 事件唯一标识 */
    private String eventId;

    /** 最终决策结果：ACCEPT / REJECT / REVIEW / CHALLENGE */
    private String decision;

    /** 决策原因（人类可读的消息） */
    private String decisionMsg;

    /** 执行耗时（毫秒） */
    private Long executionTimeMs;

    /** 信号摘要：各等级信号数量（如 {"HIGH": 1, "LOW": 2}），方便调用方做二次判断 */
    private Map<String, Long> signalSummary;
}