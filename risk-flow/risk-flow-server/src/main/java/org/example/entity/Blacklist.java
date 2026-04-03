package org.example.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.Instant;

/**
 * 黑名单实体 - 全局 UTC 标准版
 */
@Data
@Entity
@Table(name = "blacklist", indexes = {
        @Index(name = "idx_blacklist_type_value", columnList = "type, value"),
        @Index(name = "idx_blacklist_expire_time", columnList = "expire_time") // 注意：JPA通常映射为下划线
})
public class Blacklist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 黑名单类型：IP / DEVICE / PHONE / USER / EMAIL */
    @Column(nullable = false, length = 20)
    private String type;

    /** 黑名单值 */
    @Column(nullable = false, length = 200)
    private String value;

    /** 加入黑名单原因 */
    @Column(length = 500)
    private String reason;

    /** 来源：MANUAL / AUTO / THIRD_PARTY */
    @Column(length = 50)
    private String source;

    /** * 过期时间（NULL 表示永久）
     * 对应 PostgreSQL 的 timestamptz 类型
     */
    @Column(name = "expire_time")
    private Instant expireTime;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    public void prePersist() {
        // 使用 Instant.now() 产生的是标准 UTC 时间戳
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }
}