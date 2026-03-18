package org.example.service.impl;

import org.example.entity.DecisionThreshold;
import org.example.repository.DecisionThresholdRepository;
import org.example.service.DecisionThresholdService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 决策阈值服务实现
 * 按优先级依次匹配：精确 → eventType+userLevel → eventType → 全局默认
 */
@Service
public class DecisionThresholdServiceImpl implements DecisionThresholdService {

    private static final Logger log = LoggerFactory.getLogger(DecisionThresholdServiceImpl.class);

    private final DecisionThresholdRepository thresholdRepository;

    public DecisionThresholdServiceImpl(DecisionThresholdRepository thresholdRepository) {
        this.thresholdRepository = thresholdRepository;
    }

    @Override
    public Threshold resolve(Long workflowId, String eventType, String userLevel) {
        DecisionThreshold entity = null;

        // 1. 精确匹配：workflowId + eventType + userLevel
        if (workflowId != null && eventType != null && userLevel != null) {
            entity = thresholdRepository
                    .findByWorkflowIdAndEventTypeAndUserLevelAndEnabledTrue(workflowId, eventType, userLevel)
                    .orElse(null);
        }

        // 2. 匹配：eventType + userLevel
        if (entity == null && eventType != null && userLevel != null) {
            entity = thresholdRepository
                    .findByWorkflowIdIsNullAndEventTypeAndUserLevelAndEnabledTrue(eventType, userLevel)
                    .orElse(null);
        }

        // 3. 匹配：只有 eventType
        if (entity == null && eventType != null) {
            entity = thresholdRepository
                    .findByWorkflowIdIsNullAndEventTypeAndUserLevelIsNullAndEnabledTrue(eventType)
                    .orElse(null);
        }

        // 4. 全局默认
        if (entity == null) {
            entity = thresholdRepository
                    .findByWorkflowIdIsNullAndEventTypeIsNullAndUserLevelIsNullAndEnabledTrue()
                    .orElse(null);
        }

        if (entity != null) {
            log.debug("[DecisionThreshold] 命中阈值配置 id={}，reject={}, review={}, challenge={}",
                    entity.getId(), entity.getRejectThreshold(),
                    entity.getReviewThreshold(), entity.getChallengeThreshold());
            return new Threshold(
                    entity.getRejectThreshold(),
                    entity.getReviewThreshold(),
                    entity.getChallengeThreshold()
            );
        }

        // 5. 兜底：使用代码默认值
        log.warn("[DecisionThreshold] 未找到任何阈值配置，使用默认值 (80/50/30)");
        return Threshold.defaultThreshold();
    }
}
