package org.example.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 规则配置实体
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

    @Column(columnDefinition = "TEXT")
    private String expression;

    @Column(nullable = false)
    private Integer score = 0;

    @Column(nullable = false)
    private Boolean enabled = true;

    @Column(nullable = false)
    private Integer priority = 0;

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
