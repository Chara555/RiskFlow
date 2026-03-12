package org.example.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 黑名单实体
 * type: IP / DEVICE / PHONE / USER / EMAIL
 * source: MANUAL(手动添加) / AUTO(自动识别) / THIRD_PARTY(第三方)
 */
@Data
@Entity
@Table(name = "blacklist", indexes = {
    @Index(name = "idx_blacklist_type_value", columnList = "type, value"),
    @Index(name = "idx_blacklist_expire_time", columnList = "expireTime")
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

    /** 过期时间（NULL表示永久） */
    @Column
    private LocalDateTime expireTime;

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
