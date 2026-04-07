package org.example.service;

import com.yomahub.liteflow.core.FlowExecutor;
import com.yomahub.liteflow.flow.LiteflowResponse;
import lombok.extern.slf4j.Slf4j;
import org.example.context.RiskFlowContext;
import org.example.core.util.RiskTimeUtils;
import org.example.dto.DecisionRequest;
import org.example.dto.DecisionResponse;
import org.example.entity.DecisionLog;
import org.example.entity.EventRouting;
import org.example.repository.DecisionLogRepository;
import org.example.repository.EventRoutingRepository;
import org.example.repository.WorkflowRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 风控决策服务（核心入口）
 *
 * 处理流程：
 * Step 1: 路由寻址 — 根据 eventType 查询 event_routing 表
 * Step 2: 降级放行 — 路由不存在或未启用，直接 ACCEPT（Fail-Open）
 * Step 3: 构建上下文 — 组装 RiskFlowContext
 * Step 4: 执行引擎 — 调用 LiteFlow 动态工作流
 * Step 5: 提取结果与落库 — 组装 Response，异步写 decision_log
 */
@Slf4j
@Service
public class RiskDecisionService {

    private final FlowExecutor flowExecutor;
    private final EventRoutingRepository eventRoutingRepository;
    private final WorkflowRepository workflowRepository;
    private final DecisionLogRepository decisionLogRepository;

    public RiskDecisionService(FlowExecutor flowExecutor,
                               EventRoutingRepository eventRoutingRepository,
                               WorkflowRepository workflowRepository,
                               DecisionLogRepository decisionLogRepository) {
        this.flowExecutor = flowExecutor;
        this.eventRoutingRepository = eventRoutingRepository;
        this.workflowRepository = workflowRepository;
        this.decisionLogRepository = decisionLogRepository;
    }

    /**
     * 执行风控决策（对外唯一入口）
     */
    public DecisionResponse evaluate(DecisionRequest request) {
        long startTime = RiskTimeUtils.nowMs();

        // 自动生成 eventId（调用方未传时兜底）
        String eventId = (request.getEventId() == null || request.getEventId().isBlank())
                ? "evt-" + startTime : request.getEventId();

        log.info("[RiskFlow] 收到决策请求: eventId={}, eventType={}, userId={}, ip={}",
                eventId, request.getEventType(), request.getUserId(), request.getUserIp());

        // ========== Step 1: 路由寻址 ==========
        Optional<EventRouting> routingOpt = eventRoutingRepository.findByEventType(request.getEventType());

        // ========== Step 2: 降级放行（Fail-Open） ==========
        if (routingOpt.isEmpty()) {
            log.warn("[RiskFlow] 事件路由不存在，降级放行: eventType={}, eventId={}", request.getEventType(), eventId);
            return buildAcceptFallback(eventId, "No routing configured for eventType: " + request.getEventType(), startTime);
        }

        EventRouting routing = routingOpt.get();
        if (!Boolean.TRUE.equals(routing.getEnabled())) {
            log.warn("[RiskFlow] 事件路由已禁用，降级放行: eventType={}, eventId={}", request.getEventType(), eventId);
            return buildAcceptFallback(eventId, "Routing disabled for eventType: " + request.getEventType(), startTime);
        }

        String workflowCode = routing.getWorkflowCode();
        log.info("[RiskFlow] 路由命中: eventType={} -> workflowCode={}", request.getEventType(), workflowCode);

        // ========== Step 3: 构建上下文 ==========
        RiskFlowContext context = new RiskFlowContext();
        context.setEventId(eventId);
        context.setEventType(request.getEventType());
        context.setUserId(request.getUserId());
        context.setUserIp(request.getUserIp());
        context.setDeviceId(request.getDeviceId());
        context.setRequestTimeMs(startTime);
        // 使用深拷贝防止并发修改异常
        context.setFeatures(request.getFeatures() != null ? new HashMap<>(request.getFeatures()) : new HashMap<>());

        // 关联动态工作流配置（可选）
        workflowRepository.findByCodeAndStatus(workflowCode, "PUBLISHED")
                .ifPresent(w -> context.setWorkflowId(w.getId()));

        // ========== Step 4: 执行引擎 ==========
        try {
            LiteflowResponse response = flowExecutor.execute2Resp(workflowCode, null, context);

            if (!response.isSuccess()) {
                // 引擎执行失败 → Fail-Open 降级放行
                log.error("[RiskFlow] 链路执行失败，降级放行: eventId={}, chain={}, error={}",
                        eventId, workflowCode, response.getMessage());
                context.setResult(RiskFlowContext.DecisionResult.ACCEPT);
                context.setResultMessage("Engine execution failed, fail-open applied: " + response.getMessage());
            }
        } catch (Exception e) {
            // 未知异常 → Fail-Open 降级放行（风控不能阻塞业务）
            log.error("[RiskFlow] 系统运行异常，降级放行: eventId={}, chain={}", eventId, workflowCode, e);
            context.setResult(RiskFlowContext.DecisionResult.ACCEPT);
            context.setResultMessage("System error, fail-open applied: " + e.getMessage());
        }

        // 计算执行耗时
        long executionTimeMs = RiskTimeUtils.nowMs() - startTime;
        context.setExecutionTimeMs(executionTimeMs);
        context.setDecisionTimeMs(RiskTimeUtils.nowMs());

        // ========== Step 5: 提取结果与落库 ==========
        String decision = context.getResult() != null ? context.getResult().name() : "ACCEPT";
        Map<String, Long> signalSummary = context.getLevelCounts();

        log.info("[RiskFlow] 决策完成: eventId={}, decision={}, executionTimeMs={}, signalSummary={}",
                eventId, decision, executionTimeMs, signalSummary);

        // 异步落库（虚拟线程，不阻塞主响应）
        Thread.startVirtualThread(() -> saveDecisionLog(context));

        return DecisionResponse.builder()
                .eventId(eventId)
                .decision(decision)
                .decisionMsg(context.getResultMessage())
                .executionTimeMs(executionTimeMs)
                .signalSummary(signalSummary)
                .build();
    }

    /**
     * 构造降级放行的 Response（路由不存在或禁用时）
     */
    private DecisionResponse buildAcceptFallback(String eventId, String reason, long startTime) {
        long executionTimeMs = RiskTimeUtils.nowMs() - startTime;
        return DecisionResponse.builder()
                .eventId(eventId)
                .decision("ACCEPT")
                .decisionMsg(reason)
                .executionTimeMs(executionTimeMs)
                .signalSummary(Map.of())
                .build();
    }

    /**
     * 异步持久化决策日志（使用 Hibernate 6 原生 JSONB 映射）
     */
    private void saveDecisionLog(RiskFlowContext context) {
        try {
            DecisionLog decisionLog = new DecisionLog();
            decisionLog.setEventId(context.getEventId());
            decisionLog.setWorkflowId(context.getWorkflowId());
            decisionLog.setUserId(context.getUserId());
            decisionLog.setEventType(context.getEventType());
            decisionLog.setUserIp(context.getUserIp());
            decisionLog.setDeviceId(context.getDeviceId());

            // Hibernate 6 原生 JSONB 映射，直接赋 Map
            decisionLog.setRequestData(context.getFeatures());
            decisionLog.setDecision(context.getResult() != null ? context.getResult().name() : "ACCEPT");
            decisionLog.setDecisionMsg(context.getResultMessage());

            // 完整信号快照与信号摘要
            decisionLog.setNodeResults(new HashMap<>(context.getSignalSnapshot()));
            decisionLog.setSignalSummary(context.getLevelCounts());
            decisionLog.setExecutionTime(
                    context.getExecutionTimeMs() != null ? context.getExecutionTimeMs().intValue() : 0);

            decisionLogRepository.save(decisionLog);
            log.debug("[RiskFlow] 决策日志持久化成功: eventId={}", context.getEventId());
        } catch (Exception e) {
            log.error("[RiskFlow] 决策日志持久化失败: eventId={}", context.getEventId(), e);
        }
    }
}