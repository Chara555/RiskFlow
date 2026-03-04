package org.example.dto;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 决策响应DTO
 */
public class DecisionResponse {

    private String decisionId;
    private String result;
    private Integer riskScore;
    private String message;
    private Map<String, Object> details;
    private LocalDateTime decisionTime;
    private Long executionTimeMs;

    // Getters and Setters
    public String getDecisionId() { return decisionId; }
    public void setDecisionId(String decisionId) { this.decisionId = decisionId; }

    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }

    public Integer getRiskScore() { return riskScore; }
    public void setRiskScore(Integer riskScore) { this.riskScore = riskScore; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Map<String, Object> getDetails() { return details; }
    public void setDetails(Map<String, Object> details) { this.details = details; }

    public LocalDateTime getDecisionTime() { return decisionTime; }
    public void setDecisionTime(LocalDateTime decisionTime) { this.decisionTime = decisionTime; }

    public Long getExecutionTimeMs() { return executionTimeMs; }
    public void setExecutionTimeMs(Long executionTimeMs) { this.executionTimeMs = executionTimeMs; }
}
