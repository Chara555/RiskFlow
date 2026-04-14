package org.example.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.example.core.util.RiskTimeUtils;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;

/**
 * 规则配置实体
 * 作用：在信号驱动制下，仅用于存放各风控算子的动态阈值 (params) 和产出等级 (riskLevel)
 */
@Data
@Entity
@Table(name = "rule_config")
public class RuleConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50, unique = true)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 30)
    private String type;

    // 新增：信号制下的动态风险等级 (替代了旧的 score)
    @Column(length = 20)
    private String riskLevel = "NONE";

    /**
     * 扩展参数（JSON 格式）
     * 极其重要：用于存放节点内部逻辑的动态阈值。例如: {"threshold": 5, "timeWindowHours": 24}
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> params;

    @Column(nullable = false)
    private Boolean enabled = true;

    @Column(nullable = false, updatable = false)
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