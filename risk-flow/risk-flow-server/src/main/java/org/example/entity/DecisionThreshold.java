package org.example.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 动态决策阈值配置实体
 *
 * 优先级（从高到低）：
 * 1. workflowId + eventType + userLevel（最精确）
 * 2. eventType + userLevel
 * 3. eventType
 * 4. 全局默认（workflowId=NULL, eventType=NULL, userLevel=NULL）
 */
@Data
@Entity
@Table(name = "decision_threshold", indexes = {
    @Index(name = "idx_threshold_workflow", columnList = "workflowId"),
    @Index(name = "idx_threshold_event_type", columnList = "eventType"),
    @Index(name = "idx_threshold_user_level", columnList = "userLevel"),
    @Index(name = "idx_threshold_enabled", columnList = "enabled")
})
public class DecisionThreshold {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 关联流程ID（NULL表示适用所有流程） */
    @Column
    private Long workflowId;

    /**
     * 事件类型（NULL表示适用所有事件类型）
     * login / payment / register / ...
     */
    @Column(length = 50)
    private String eventType;

    /**
     * 用户等级（NULL表示适用所有用户等级）
     * VIP / NORMAL / NEW / RISK
     */
    @Column(length = 20)
    private String userLevel;

    // ========== 阈值配置 ==========

    /** 拒绝阈值（>= 该分数直接拒绝） */
    @Column(nullable = false)
    private Integer rejectThreshold = 80;

    /** 人工审核阈值（>= 该分数进入审核队列） */
    @Column(nullable = false)
    private Integer reviewThreshold = 50;

    /** 验证码挑战阈值（>= 该分数要求验证码） */
    @Column(nullable = false)
    private Integer challengeThreshold = 30;

    // ========== 后续动作配置（JSON） ==========

    /**
     * 拒绝后的动作（JSON）
     * 示例：{"type": "block", "notify": ["sms", "email"], "message": "操作被拒绝"}
     */
    @Column(columnDefinition = "jsonb")
    private String rejectAction;

    /**
     * 审核后的动作（JSON）
     * 示例：{"type": "manual_review", "queue": "default", "priority": "normal"}
     */
    @Column(columnDefinition = "jsonb")
    private String reviewAction;

    /**
     * 挑战后的动作（JSON）
     * 示例：{"type": "verification", "methods": ["sms_code"], "timeout": 300}
     */
    @Column(columnDefinition = "jsonb")
    private String challengeAction;

    /**
     * 通过后的动作（JSON）
     * 示例：{"type": "allow", "log": true}
     */
    @Column(columnDefinition = "jsonb")
    private String acceptAction;

    /** 是否启用 */
    @Column(nullable = false)
    private Boolean enabled = true;

    /** 优先级（数字越大优先级越高） */
    @Column(nullable = false)
    private Integer priority = 0;

    /** 描述 */
    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
