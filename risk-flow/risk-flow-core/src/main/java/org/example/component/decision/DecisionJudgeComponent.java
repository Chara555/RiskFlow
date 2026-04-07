package org.example.component.decision;

import org.example.context.RiskFlowContext;
import org.example.core.model.RiskSignal;
import com.yomahub.liteflow.annotation.LiteflowComponent;
import com.yomahub.liteflow.core.NodeComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * 决策大脑组件 (Decision Brain) - 信号驱动制核心
 * * <p>当前阶段 (MVP): 使用硬编码的默认兜底规则进行研判。
 * * <p>TODO [架构演进计划 - 低代码规则引擎]:
 * 1. 废弃当前的 if-else 硬编码逻辑。
 * 2. 引入 QLExpress、Aviator 或 JsonLogic 等动态脚本引擎。
 * 3. 在 process() 中，从数据库动态拉取运营人员在“低代码前端”配置的 DSL 规则。
 * 4. 将 context.getSignalSnapshot() 作为沙箱变量传入脚本引擎，直接得出最终结论。
 */
@LiteflowComponent("decisionJudge")
public class DecisionJudgeComponent extends NodeComponent {

    private static final Logger log = LoggerFactory.getLogger(DecisionJudgeComponent.class);

    @Override
    public void process() {
        RiskFlowContext context = this.getContextBean(RiskFlowContext.class);

        // 1. 提取信号摘要（直接使用底层封装好的高效统计方法）
        Map<String, Long> levelCounts = context.getLevelCounts();
        long criticalCount = levelCounts.getOrDefault(RiskSignal.LEVEL_CRITICAL, 0L);
        long highCount = levelCounts.getOrDefault(RiskSignal.LEVEL_HIGH, 0L);
        long mediumCount = levelCounts.getOrDefault(RiskSignal.LEVEL_MEDIUM, 0L);
        long lowCount = levelCounts.getOrDefault(RiskSignal.LEVEL_LOW, 0L);

        // 2. 默认兜底裁决逻辑 (MVP 阶段硬编码，等待替换为脚本引擎)
        if (criticalCount > 0) {
            // Rule 1: 任何 CRITICAL 信号 → 直接拒绝 (一票否决)
            context.setResult(RiskFlowContext.DecisionResult.REJECT);
            context.setResultMessage(buildRejectMessage(criticalCount, "致命风险信号"));

        } else if (highCount >= 2) {
            // Rule 2: 2个以上 HIGH 信号 → 拒绝
            context.setResult(RiskFlowContext.DecisionResult.REJECT);
            context.setResultMessage(buildRejectMessage(highCount, "多重高风险信号"));

        } else if (highCount == 1 || mediumCount >= 2) {
            // Rule 3: 1个 HIGH 或 2个以上 MEDIUM → 人工审核
            context.setResult(RiskFlowContext.DecisionResult.REVIEW);
            context.setResultMessage(buildReviewMessage(highCount, mediumCount));

        } else if (mediumCount == 1) {
            // Rule 4: 1个 MEDIUM → 验证码挑战
            context.setResult(RiskFlowContext.DecisionResult.CHALLENGE);
            context.setResultMessage("存在中等风险，需验证码确认");

        } else {
            // Rule 5: 全是 LOW 或 NONE → 放行
            context.setResult(RiskFlowContext.DecisionResult.ACCEPT);
            context.setResultMessage("未发现异常风险，审核通过");
        }

        // 3. 打印最终决策日志
        log.info("[DecisionBrain] 决策完成: Result={}, Msg={}, Signals(CRITICAL={}, HIGH={}, MEDIUM={}, LOW={})",
                context.getResult(), context.getResultMessage(),
                criticalCount, highCount, mediumCount, lowCount);
    }

    /**
     * 辅助方法：构建拒绝消息
     */
    private String buildRejectMessage(long count, String reason) {
        return String.format("高风险操作，系统直接拒绝 (%s: %d)", reason, count);
    }

    /**
     * 辅助方法：构建审核消息
     */
    private String buildReviewMessage(long highCount, long mediumCount) {
        if (highCount > 0) {
            return String.format("存在高风险信号，建议人工审核 (HIGH=%d)", highCount);
        }
        return String.format("存在多重中等风险，建议人工审核 (MEDIUM=%d)", mediumCount);
    }

    @Override
    public boolean isAccess() {
        return true; // 大脑节点作为整个风控链路的最终收口，必须强制执行
    }
}