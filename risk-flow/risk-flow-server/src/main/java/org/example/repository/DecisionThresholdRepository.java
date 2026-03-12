package org.example.repository;

import org.example.entity.DecisionThreshold;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DecisionThresholdRepository extends JpaRepository<DecisionThreshold, Long> {

    /**
     * 精确匹配：workflowId + eventType + userLevel
     */
    Optional<DecisionThreshold> findByWorkflowIdAndEventTypeAndUserLevelAndEnabledTrue(
            Long workflowId, String eventType, String userLevel);

    /**
     * 匹配：eventType + userLevel
     */
    Optional<DecisionThreshold> findByWorkflowIdIsNullAndEventTypeAndUserLevelAndEnabledTrue(
            String eventType, String userLevel);

    /**
     * 匹配：只有 eventType
     */
    Optional<DecisionThreshold> findByWorkflowIdIsNullAndEventTypeAndUserLevelIsNullAndEnabledTrue(
            String eventType);

    /**
     * 全局默认（所有字段为NULL）
     */
    Optional<DecisionThreshold> findByWorkflowIdIsNullAndEventTypeIsNullAndUserLevelIsNullAndEnabledTrue();

    /**
     * 按优先级查询所有启用的阈值
     */
    List<DecisionThreshold> findByEnabledTrueOrderByPriorityDesc();

    /**
     * 按事件类型查询
     */
    List<DecisionThreshold> findByEventTypeAndEnabledTrue(String eventType);
}
