package org.example.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 决策日志实体
 */
@Data
@Entity
@Table(name = "decision_log", indexes = {
    @Index(name = "idx_decision_log_event_id", columnList = "eventId"),
    @Index(name = "idx_decision_log_user_id", columnList = "userId"),
    @Index(name = "idx_decision_log_event_type", columnList = "eventType"),
    @Index(name = "idx_decision_log_created_at", columnList = "createdAt"),
    @Index(name = "idx_decision_log_decision", columnList = "decision")
})
public class DecisionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 事件ID */
    @Column(nullable = false, length = 100)
    private String eventId;

    /** 关联流程ID */
    @Column
    private Long workflowId;

    /** 用户ID */
    @Column(length = 100)
    private String userId;

    /** 事件类型：login / payment / register */
    @Column(length = 50)
    private String eventType;

    /** 用户IP */
    @Column(length = 50)
    private String userIp;

    /** 设备ID */
    @Column(length = 100)
    private String deviceId;

    /** 请求数据（JSON） */
    @Column(columnDefinition = "jsonb")
    private String requestData;

    /** 风险评分 */
    @Column(nullable = false)
    private Integer riskScore = 0;

    /** 决策结果：ACCEPT / REJECT / REVIEW / CHALLENGE */
    @Column(length = 20)
    private String decision;

    /** 决策消息 */
    @Column(length = 500)
    private String decisionMsg;

    /** 各节点执行结果（JSON） */
    @Column(columnDefinition = "jsonb")
    private String nodeResults;

    /** 执行耗时（毫秒） */
    @Column
    private Integer executionTime;

    /** 用户等级快照 */
    @Column(length = 20)
    private String userLevel;

    /** AI分析结果 */
    @Column(columnDefinition = "text")
    private String aiAnalysisResult;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }
}
