package org.example.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

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
