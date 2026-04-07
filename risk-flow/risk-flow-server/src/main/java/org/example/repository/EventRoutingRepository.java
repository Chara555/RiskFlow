package org.example.repository;

import org.example.entity.EventRouting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EventRoutingRepository extends JpaRepository<EventRouting, Long> {

    /**
     * 根据事件类型查找路由配置
     */
    Optional<EventRouting> findByEventType(String eventType);
}
