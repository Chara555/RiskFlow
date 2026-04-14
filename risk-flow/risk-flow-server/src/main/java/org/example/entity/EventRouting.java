package org.example.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.example.core.util.RiskTimeUtils;

import java.time.Instant;

/**
 * 事件路由表实体
 * 将业务事件类型（如 login、payment）映射到对应的 LiteFlow 工作流链路
 */
@Data
@Entity
@Table(name = "event_routing", indexes = {
    @Index(name = "idx_event_routing_event_type", columnList = "eventType", unique = true)
})
public class EventRouting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 事件类型（唯一键，如 login / payment / register） */
    @Column(nullable = false, length = 50, unique = true)
    private String eventType;

    /** 对应的 LiteFlow chainId（即 workflow.code） */
    @Column(nullable = false, length = 50)
    private String workflowCode;

    /** 是否启用（关闭后该事件类型走降级放行） */
    @Column(nullable = false)
    private Boolean enabled = true;

    /** 备注说明 */
    @Column(length = 200)
    private String description;

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
