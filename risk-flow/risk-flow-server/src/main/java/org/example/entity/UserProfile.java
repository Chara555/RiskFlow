package org.example.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.example.core.util.RiskTimeUtils;

import java.time.Instant;

/**
 * 用户画像实体
 * 记录用户的历史行为和风险画像，用于差异化风控流程
 */
@Data
@Entity
@Table(name = "user_profile", indexes = {
    @Index(name = "idx_user_profile_user_id", columnList = "userId", unique = true),
    @Index(name = "idx_user_profile_user_level", columnList = "userLevel"),
    @Index(name = "idx_user_profile_risk_level", columnList = "riskLevel")
})
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 用户ID（唯一） */
    @Column(nullable = false, length = 100, unique = true)
    private String userId;

    /**
     * 用户等级
     * VIP    - VIP用户（高价值，容忍度更高）
     * NORMAL - 普通用户（默认）
     * NEW    - 新用户（严格审查）
     * RISK   - 高风险用户（最严格）
     */
    @Column(nullable = false, length = 20)
    private String userLevel = "NORMAL";

    /**
     * 风险等级
     * LOW    - 低风险
     * MEDIUM - 中风险
     * HIGH   - 高风险
     */
    @Column(nullable = false, length = 20)
    private String riskLevel = "LOW";

    /** 信用分（0-100，越高越可信） */
    @Column(nullable = false)
    private Integer creditScore = 100;

    // ========== 统计信息 ==========

    /** 总事件数 */
    @Column(nullable = false)
    private Integer totalEvents = 0;

    /** 拒绝次数 */
    @Column(nullable = false)
    private Integer rejectCount = 0;

    /** 人工审核次数 */
    @Column(nullable = false)
    private Integer reviewCount = 0;

    /** 验证码挑战次数 */
    @Column(nullable = false)
    private Integer challengeCount = 0;

    /** 通过次数 */
    @Column(nullable = false)
    private Integer acceptCount = 0;

    /** 平均风险评分 */
    @Column
    private Double avgRiskScore;

    // ========== 行为特征 ==========

    /** 最后事件时间 */
    @Column
    private Instant lastEventTime;

    /** 最后登录时间 */
    @Column
    private Instant lastLoginTime;

    /** 最后支付时间 */
    @Column
    private Instant lastPaymentTime;

    /** 关联设备数量 */
    @Column(nullable = false)
    private Integer deviceCount = 0;

    /** 常用设备列表（JSON数组） */
    @Column(columnDefinition = "jsonb")
    private String commonDevices;

    /** 常用地点（JSON数组） */
    @Column(columnDefinition = "jsonb")
    private String commonLocations;

    /** 用户标签（JSON数组，如 ["高价值", "频繁登录"]） */
    @Column(columnDefinition = "jsonb")
    private String tags;

    /** 扩展画像数据（JSON） */
    @Column(columnDefinition = "jsonb")
    private String profileData;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @PrePersist
    public void prePersist() {
        createdAt = RiskTimeUtils.now();
        updatedAt = RiskTimeUtils.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = RiskTimeUtils.now();
    }
}
