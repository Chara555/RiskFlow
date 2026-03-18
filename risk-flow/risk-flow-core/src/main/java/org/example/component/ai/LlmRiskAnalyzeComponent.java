package org.example.component.ai;

import org.example.context.RiskFlowContext;
import com.yomahub.liteflow.annotation.LiteflowComponent;
import com.yomahub.liteflow.core.NodeComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AI 风险分析组件 - 基于 LLM 的智能分析
 *
 * 注意：此为模拟实现，实际需接入 LLM 服务（如 OpenAI、Azure OpenAI 等）
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

        // 保存分析结果
        context.setAiAnalysisResult(analysisResult);

        // 根据分析结果调整风险评分
        adjustRiskScore(context, analysisResult);

        log.info("[LlmRiskAnalyze] AI 分析完成，结果: {}", analysisResult);
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
        prompt.append("当前风险评分：").append(context.getTotalRiskScore()).append("\n");
        prompt.append("规则得分详情：").append(context.getRuleScores()).append("\n");

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
     * 根据 AI 分析结果调整风险评分
     */
    private void adjustRiskScore(RiskFlowContext context, String analysisResult) {
        if (analysisResult.contains("REJECT")) {
            context.addRuleScore("llm_analysis", 30);
            context.setIsHighRisk(true);
        } else if (analysisResult.contains("REVIEW")) {
            context.addRuleScore("llm_analysis", 15);
        } else {
            // PASS，不加分
            context.addRuleScore("llm_analysis", 0);
        }
    }

    /**
     * 仅在高风险时触发 AI 分析（由 RuleExecuteComponent 在 process 末尾设置 isHighRisk 标志）
     */
    @Override
    public boolean isAccess() {
        RiskFlowContext context = this.getContextBean(RiskFlowContext.class);
        boolean shouldRun = Boolean.TRUE.equals(context.getIsHighRisk());
        if (!shouldRun) {
            log.info("[LlmRiskAnalyze] 非高风险，跳过 AI 分析，当前评分={}", context.getTotalRiskScore());
        }
        return shouldRun;
    }
}
