package org.example.component.decision;

import org.example.context.RiskFlowContext;
import com.yomahub.liteflow.annotation.LiteflowComponent;
import com.yomahub.liteflow.core.NodeComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 决策判定组件 - 根据风险评分做出最终决策
 */
@LiteflowComponent("decisionJudge")
public class DecisionJudgeComponent extends NodeComponent {

    private static final Logger log = LoggerFactory.getLogger(DecisionJudgeComponent.class);

    // 决策阈值配置
    private static final int REJECT_THRESHOLD = 80;    // 直接拒绝
    private static final int REVIEW_THRESHOLD = 50;     // 人工审核
    private static final int CHALLENGE_THRESHOLD = 30; // 验证码挑战

    @Override
    public void process() {
        RiskFlowContext context = this.getContextBean(RiskFlowContext.class);
        
        int totalScore = context.getTotalRiskScore();
        
        // 根据评分做出决策
        if (totalScore >= REJECT_THRESHOLD) {
            context.setResult(RiskFlowContext.DecisionResult.REJECT);
            context.setResultMessage("高风险操作，系统直接拒绝");
        } else if (totalScore >= REVIEW_THRESHOLD) {
            context.setResult(RiskFlowContext.DecisionResult.REVIEW);
            context.setResultMessage("存在一定风险，建议人工审核");
        } else if (totalScore >= CHALLENGE_THRESHOLD) {
            context.setResult(RiskFlowContext.DecisionResult.CHALLENGE);
            context.setResultMessage("需要通过验证码验证");
        } else {
            context.setResult(RiskFlowContext.DecisionResult.ACCEPT);
            context.setResultMessage("风险可控，审核通过");
        }
        
        // 设置高风险标记
        context.setIsHighRisk(totalScore >= REVIEW_THRESHOLD);
        
        log.info("[DecisionJudge] 决策完成，总评分={}, 决策结果={}", 
                totalScore, context.getResult());
    }

    @Override
    public boolean isAccess() {
        return true;
    }
}
