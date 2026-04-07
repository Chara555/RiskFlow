package org.example.component.ai;

import org.example.context.RiskFlowContext;
import com.yomahub.liteflow.annotation.LiteflowComponent;
import com.yomahub.liteflow.core.NodeComponent;
import org.example.core.model.RiskSignal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * AI 风险分析组件 - 基于 LLM 的智能分析
 *
 * <p>职责：收集之前所有节点的信号，综合分析后产出 AI 信号。
 * 不再直接操作分数，遵循信号驱动制。
 *
 * <p>注意：此为模拟实现，实际需接入 LLM 服务（如 OpenAI、Azure OpenAI 等）
 */
@LiteflowComponent("llmRiskAnalyze")
public class LlmRiskAnalyzeComponent extends NodeComponent {

    private static final Logger log = LoggerFactory.getLogger(LlmRiskAnalyzeComponent.class);

    @Override
    public void process() {
        RiskFlowContext context = this.getContextBean(RiskFlowContext.class);

        // 构建提示词
        String prompt = buildPrompt(context);

        // 调用 LLM（模拟实现）
        String analysisResult = callLLM(prompt);

        // 根据分析结果产出 AI 信号（所有节点众生平等，用 RiskSignal 说话）
        RiskSignal aiSignal = buildAISignal(analysisResult);
        context.addRiskSignal("llmRiskAnalyze", aiSignal);

        log.info("[LlmRiskAnalyze] AI 分析完成，等级: {}, 结果: {}", aiSignal.getRiskLevel(), analysisResult);
    }

    /**
     * 构建提示词
     */
    private String buildPrompt(RiskFlowContext context) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("请分析以下风控事件的风险性：\n");
        prompt.append("事件类型：").append(context.getEventType()).append("\n");
        prompt.append("用户ID：").append(context.getUserId()).append("\n");
        prompt.append("用户IP：").append(context.getUserIp()).append("\n");
        // 信号摘要：统计各等级信号数量
        Map<String, Long> levelCounts = context.getLevelCounts();
        prompt.append("信号摘要：").append(levelCounts).append("\n");

        if (context.getFeatures() != null && !context.getFeatures().isEmpty()) {
            prompt.append("特征数据：").append(context.getFeatures()).append("\n");
        }

        prompt.append("\n请返回风险分析和最终建议（PASS/REVIEW/REJECT）：");

        return prompt.toString();
    }

    /**
     * 调用 LLM 服务
     *
     * 实际实现可根据需求选择：
     * - OpenAI GPT-4
     * - Azure OpenAI
     * - 阿里云通义千问
     * - 本地部署的 LLM
     */
    private String callLLM(String prompt) {
        log.info("[LlmRiskAnalyze] 调用 LLM 服务...");

        // 模拟返回，实际项目中替换为真实 LLM API 调用
        String[] responses = {
            "风险较高，建议人工审核。用户IP异常，且存在多次失败登录记录。建议REJECT。",
            "风险可控，未发现明显异常行为。建议PASS。",
            "存在一定风险，建议二次验证。建议REVIEW。"
        };

        int index = (int) (Math.random() * responses.length);
        return responses[index];
    }

    /**
     * 根据分析结果构建 AI 信号
     */
    private RiskSignal buildAISignal(String analysisResult) {
        if (analysisResult.contains("REJECT")) {
            return RiskSignal.hit(
                    "LLM_RISK_ANALYZE",
                    RiskSignal.LEVEL_HIGH,
                    "AI analysis suggests REJECT",
                    "AI 分析建议拒绝",
                    Map.of("rawResponse", analysisResult),
                    "AI_ANALYSIS"
            );
        } else if (analysisResult.contains("REVIEW")) {
            return RiskSignal.hit(
                    "LLM_RISK_ANALYZE",
                    RiskSignal.LEVEL_MEDIUM,
                    "AI analysis suggests REVIEW",
                    "AI 分析建议人工审核",
                    Map.of("rawResponse", analysisResult),
                    "AI_ANALYSIS"
            );
        } else {
            return RiskSignal.pass(
                    "LLM_RISK_ANALYZE",
                    "AI analysis suggests PASS",
                    "AI 分析建议通过"
            );
        }
    }

    /**
     * 判断是否需要触发 AI 分析
     * 
     * 策略：存在 HIGH 或 CRITICAL 信号时触发，避免对所有请求都调用 LLM
     */
    @Override
    public boolean isAccess() {
        RiskFlowContext context = this.getContextBean(RiskFlowContext.class);
        
        // 存在 CRITICAL 信号，必须 AI 分析
        if (context.hasCriticalSignal()) {
            log.info("[LlmRiskAnalyze] 存在 CRITICAL 信号，触发 AI 分析");
            return true;
        }
        
        // 存在 HIGH 信号，触发 AI 分析
        boolean hasHighRisk = !context.getSignalsByLevel(RiskSignal.LEVEL_HIGH).isEmpty();
        if (!hasHighRisk) {
            log.info("[LlmRiskAnalyze] 无高风险信号，跳过 AI 分析");
        }
        return hasHighRisk;
    }
}
