package org.example.repository;

import org.example.entity.DecisionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DecisionLogRepository extends JpaRepository<DecisionLog, Long> {

    List<DecisionLog> findByUserId(String userId);

    List<DecisionLog> findByEventId(String eventId);

    List<DecisionLog> findByEventType(String eventType);
}
