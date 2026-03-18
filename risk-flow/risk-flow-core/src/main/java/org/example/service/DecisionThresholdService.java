package org.example.service;

/**
 * 决策阈值服务接口
 * 支持按流程ID、事件类型、用户等级进行优先级匹配
 */
public interface DecisionThresholdService {

    /**
     * 获取匹配的决策阈值（优先级从高到低）：
     * 1. workflowId + eventType + userLevel
     * 2. eventType + userLevel
     * 3. eventType
     * 4. 全局默认
     *
     * @param workflowId 流程ID（可为null）
     * @param eventType  事件类型（可为null）
     * @param userLevel  用户等级（可为null）
     * @return 匹配到的阈值，未匹配则返回默认值
     */
    Threshold resolve(Long workflowId, String eventType, String userLevel);

    /**
     * 阈值数据载体
     */
    class Threshold {
        private final int rejectThreshold;
        private final int reviewThreshold;
        private final int challengeThreshold;

        public Threshold(int rejectThreshold, int reviewThreshold, int challengeThreshold) {
            this.rejectThreshold = rejectThreshold;
            this.reviewThreshold = reviewThreshold;
            this.challengeThreshold = challengeThreshold;
        }

        /** 全局默认阈值（无数据库配置时使用） */
        public static Threshold defaultThreshold() {
            return new Threshold(80, 50, 30);
        }

        public int getRejectThreshold()    { return rejectThreshold; }
        public int getReviewThreshold()    { return reviewThreshold; }
        public int getChallengeThreshold() { return challengeThreshold; }
    }
}
