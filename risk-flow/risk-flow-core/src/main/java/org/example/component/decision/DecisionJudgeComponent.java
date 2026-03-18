package org.example.component.decision;

import org.example.context.RiskFlowContext;
import org.example.service.DecisionThresholdService;
import org.example.service.DecisionThresholdService.Threshold;
import com.yomahub.liteflow.annotation.LiteflowComponent;
import com.yomahub.liteflow.core.NodeComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 决策判定组件 - 根据风险评分做出最终决策
 * 阈值优先从数据库动态读取，无配置时使用默认值 (80/50/30)
 */
@LiteflowComponent("decisionJudge")
public class DecisionJudgeComponent extends NodeComponent {

    private static final Logger log = LoggerFactory.getLogger(DecisionJudgeComponent.class);

    /** 动态阈值服务（由 ComponentServiceInjectConfig 静态注入） */
    private static DecisionThresholdService thresholdService;

    public static void setStaticThresholdService(DecisionThresholdService service) {
        thresholdService = service;
    }

    @Override
    public void process() {
        RiskFlowContext context = this.getContextBean(RiskFlowContext.class);

        int totalScore = context.getTotalRiskScore();

        // 从数据库获取动态阈值，未配置则使用默认值
        Threshold threshold = resolveThreshold(context);

        // 根据评分做出决策
        if (totalScore >= threshold.getRejectThreshold()) {
            context.setResult(RiskFlowContext.DecisionResult.REJECT);
            context.setResultMessage("高风险操作，系统直接拒绝");
        } else if (totalScore >= threshold.getReviewThreshold()) {
            context.setResult(RiskFlowContext.DecisionResult.REVIEW);
            context.setResultMessage("存在一定风险，建议人工审核");
        } else if (totalScore >= threshold.getChallengeThreshold()) {
            context.setResult(RiskFlowContext.DecisionResult.CHALLENGE);
            context.setResultMessage("需要通过验证码验证");
        } else {
            context.setResult(RiskFlowContext.DecisionResult.ACCEPT);
            context.setResultMessage("风险可控，审核通过");
        }

        // 更新高风险标记（AI 分析后评分可能变化）
        context.setIsHighRisk(totalScore >= threshold.getReviewThreshold());

        log.info("[DecisionJudge] 决策完成，总评分={}, 阈值(拒绝/审核/挑战)={}/{}/{}, 决策结果={}",
                totalScore,
                threshold.getRejectThreshold(),
                threshold.getReviewThreshold(),
                threshold.getChallengeThreshold(),
                context.getResult());
    }

    /**
     * 解析当前上下文对应的阈值配置
     */
    private Threshold resolveThreshold(RiskFlowContext context) {
        if (thresholdService == null) {
            log.warn("[DecisionJudge] thresholdService 未注入，使用默认阈值");
            return Threshold.defaultThreshold();
        }
        return thresholdService.resolve(
                context.getWorkflowId(),
                context.getEventType(),
                null  // userLevel 暂未接入用户画像，后续扩展
        );
    }

    @Override
    public boolean isAccess() {
        return true;
    }
}
