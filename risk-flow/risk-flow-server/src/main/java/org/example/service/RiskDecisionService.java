package org.example.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yomahub.liteflow.core.FlowExecutor;
import com.yomahub.liteflow.flow.LiteflowResponse;
import org.example.context.RiskFlowContext;
import org.example.core.util.RiskTimeUtils;
import org.example.dto.DecisionRequest;
import org.example.entity.DecisionLog;
import org.example.entity.Workflow;
import org.example.repository.DecisionLogRepository;
import org.example.repository.WorkflowRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

@Service
public class RiskDecisionService {

    private static final Logger log = LoggerFactory.getLogger(RiskDecisionService.class);

    // 必须与你的 risk-decision.xml 中的 chain name 保持一致
    private static final String LOGIN_CHAIN = "riskDecisionFlow";

    private final FlowExecutor flowExecutor;
    private final WorkflowRepository workflowRepository;
    private final DecisionLogRepository decisionLogRepository;
    private final ObjectMapper objectMapper;

    public RiskDecisionService(FlowExecutor flowExecutor,
                               WorkflowRepository workflowRepository,
                               DecisionLogRepository decisionLogRepository,
                               ObjectMapper objectMapper) {
        this.flowExecutor = flowExecutor;
        this.workflowRepository = workflowRepository;
        this.decisionLogRepository = decisionLogRepository;
        this.objectMapper = objectMapper;
    }

    public RiskFlowContext decide(DecisionRequest req) {
        // 1. 初始化上下文与全局唯一 EventID
        long startTime = RiskTimeUtils.nowMs();
        String eventId = (req.getEventId() == null || req.getEventId().isEmpty())
                ? "evt-" + startTime : req.getEventId();

        RiskFlowContext context = new RiskFlowContext();
        context.setEventId(eventId);
        context.setUserId(req.getUserId());
        context.setEventType(req.getEventType());
        context.setUserIp(req.getUserIp());
        context.setDeviceId(req.getDeviceId());
        context.setRequestTimeMs(startTime);

        // 使用深拷贝防止并发修改异常
        context.setFeatures(req.getFeatures() != null ? new HashMap<>(req.getFeatures()) : new HashMap<>());

        // 2. 关联动态工作流配置 (可选)
        workflowRepository.findByCodeAndStatus(LOGIN_CHAIN, "PUBLISHED")
                .ifPresent(w -> context.setWorkflowId(w.getId()));

        try {
            // 3. 执行 LiteFlow 链路 (execute2Resp 可以捕获完整的执行树和异常)
            LiteflowResponse response = flowExecutor.execute2Resp(LOGIN_CHAIN, null, context);

            if (!response.isSuccess()) {
                log.error("[RiskFlow] 链路执行失败: {}, 错误原因: {}", eventId, response.getMessage());
                context.setResult(RiskFlowContext.DecisionResult.REJECT);
                context.setResultMessage("Engine Error: " + response.getMessage());
            }

            // 记录执行耗时
            context.setExecutionTimeMs(RiskTimeUtils.nowMs() - startTime);

        } catch (Exception e) {
            log.error("[RiskFlow] 系统运行异常: {}", eventId, e);
            context.setResult(RiskFlowContext.DecisionResult.REJECT);
            context.setResultMessage("System Error: " + e.getMessage());
        } finally {
            context.setDecisionTimeMs(RiskTimeUtils.nowMs());
        }

        // 4. 异步保存日志 (不阻塞主响应，充分利用虚拟线程)
        CompletableFuture.runAsync(() -> saveDecisionLog(context));

        return context;
    }

    private void saveDecisionLog(RiskFlowContext context) {
        try {
            DecisionLog decisionLog = new DecisionLog();
            decisionLog.setEventId(context.getEventId());
            decisionLog.setWorkflowId(context.getWorkflowId());
            decisionLog.setUserId(context.getUserId());
            decisionLog.setEventType(context.getEventType());

            // 将 Context 的快照序列化存入 JSONB 字段
            decisionLog.setRequestData(objectMapper.writeValueAsString(context.getFeatures()));
            decisionLog.setRiskScore(context.getTotalRiskScore());
            decisionLog.setDecision(context.getResult() != null ? context.getResult().name() : "REJECT");
            decisionLog.setDecisionMsg(context.getResultMessage());

            // 记录每个算子的命中详情
            decisionLog.setNodeResults(objectMapper.writeValueAsString(context.getNodeResults()));
            decisionLog.setExecutionTime(context.getExecutionTimeMs().intValue());

            decisionLogRepository.save(decisionLog);
        } catch (Exception e) {
            log.error("持久化决策日志失败: {}", context.getEventId(), e);
        }
    }
}