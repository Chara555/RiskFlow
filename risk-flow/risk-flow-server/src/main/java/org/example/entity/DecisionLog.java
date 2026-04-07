package org.example.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

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

    /** 请求数据（JSON）—— 业务方传入的动态特征集 */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> requestData;

    /** 决策结果：ACCEPT / REJECT / REVIEW / CHALLENGE */
    @Column(length = 20)
    private String decision;

    /** 决策消息 */
    @Column(length = 500)
    private String decisionMsg;

    /** 各节点执行结果（JSON）—— 完整信号快照，用于审计与回溯 */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> nodeResults;

    /** 信号摘要：各等级信号数量统计（如 {"HIGH": 1, "LOW": 2}） */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Long> signalSummary;

    /** 执行耗时（毫秒） */
    @Column
    private Integer executionTime;

    /** 用户等级快照 */
    @Column(length = 20)
    private String userLevel;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }
}
