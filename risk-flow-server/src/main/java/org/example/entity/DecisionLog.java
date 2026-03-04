package org.example.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 决策日志实体
 */
@Data
@Entity
@Table(name = "decision_log")
public class DecisionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String eventId;

    @Column
    private Long workflowId;

    @Column(length = 50)
    private String userId;

    @Column(length = 30)
    private String eventType;

    @Column(columnDefinition = "jsonb")
    private String requestData;

    @Column(nullable = false)
    private Integer riskScore = 0;

    @Column(length = 20)
    private String decision;

    @Column(length = 500)
    private String decisionMsg;

    @Column(columnDefinition = "jsonb")
    private String nodeResults;

    @Column
    private Integer executionTime;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }
}
