package org.example.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 流程定义实体
 */
@Data
@Entity
@Table(name = "workflow")
public class Workflow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 50, unique = true)
    private String code;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private Integer version = 1;

    @Column(length = 20)
    private String status = "DRAFT";

    /** 前端可视化编排的 JSON 数据 */
    @Column(columnDefinition = "jsonb")
    private String flowData;

    /** 由 flowData 转换生成的 LiteFlow EL 表达式 */
    @Column(columnDefinition = "text")
    private String elExpression;

    @Column(length = 50)
    private String createdBy;

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
