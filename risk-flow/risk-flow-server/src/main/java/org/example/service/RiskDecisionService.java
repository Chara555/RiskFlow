package org.example.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yomahub.liteflow.core.FlowExecutor;
import org.example.context.RiskFlowContext;
import org.example.entity.DecisionLog;
import org.example.entity.Workflow;
import org.example.repository.DecisionLogRepository;
import org.example.repository.WorkflowRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 风控决策服务 - 使用 LiteFlow 执行
 */
@Service
public class RiskDecisionService {

    private static final Logger log = LoggerFactory.getLogger(RiskDecisionService.class);

    private static final String DEFAULT_WORKFLOW_CODE = "riskDecisionFlow";

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

    /**
     * 执行风控决策
     */
    public RiskFlowContext decide(String userId, String eventType, String userIp, Map<String, Object> features) {
        return decide(null, userId, eventType, userIp, null, features, null);
    }

    /**
     * 执行风控决策（完整参数）
     */
    public RiskFlowContext decide(String eventId, String userId, String eventType,
                                  String userIp, String deviceId,
                                  Map<String, Object> features, Map<String, Object> extInfo) {
        // 生成事件ID
        if (eventId == null || eventId.isEmpty()) {
            eventId = "evt-" + System.currentTimeMillis();
        }

        long startTime = System.currentTimeMillis();

        RiskFlowContext context = new RiskFlowContext();
        context.setEventId(eventId);
        context.setUserId(userId);
        context.setEventType(eventType);
        context.setUserIp(userIp);
        context.setDeviceId(deviceId);

        if (features != null) {
            context.setFeatures(new HashMap<>(features));
        }
        if (extInfo != null) {
            context.setExtInfo(new HashMap<>(extInfo));
        }

        // 获取流程配置
        Workflow workflow = workflowRepository.findByCodeAndStatus(DEFAULT_WORKFLOW_CODE, "PUBLISHED")
                .orElse(null);
        if (workflow != null) {
            context.setWorkflowId(workflow.getId());
        }

        try {
            flowExecutor.execute(DEFAULT_WORKFLOW_CODE, context);
        } catch (Exception e) {
            log.error("流程执行失败: {}", e.getMessage(), e);
            context.setResult(RiskFlowContext.DecisionResult.REJECT);
            context.setResultMessage("系统错误: " + e.getMessage());
        }

        long executionTime = System.currentTimeMillis() - startTime;
        context.setExecutionTimeMs(executionTime);

        // 保存决策日志
        saveDecisionLog(context, features, extInfo);

        return context;
    }

    /**
     * 保存决策日志
     */
    private void saveDecisionLog(RiskFlowContext context, Map<String, Object> features, Map<String, Object> extInfo) {
        try {
            DecisionLog decisionLog = new DecisionLog();
            decisionLog.setEventId(context.getEventId());
            decisionLog.setWorkflowId(context.getWorkflowId());
            decisionLog.setUserId(context.getUserId());
            decisionLog.setEventType(context.getEventType());
            decisionLog.setRequestData(objectMapper.writeValueAsString(
                    buildRequestData(context, features, extInfo)));
            decisionLog.setRiskScore(context.getTotalRiskScore());
            decisionLog.setDecision(context.getResult() != null ? context.getResult().name() : null);
            decisionLog.setDecisionMsg(context.getResultMessage());
            decisionLog.setNodeResults(objectMapper.writeValueAsString(context.getNodeResults()));
            decisionLog.setExecutionTime(context.getExecutionTimeMs() != null ? context.getExecutionTimeMs().intValue() : null);

            decisionLogRepository.save(decisionLog);
            log.debug("决策日志已保存: eventId={}", context.getEventId());
        } catch (Exception e) {
            log.error("保存决策日志失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 构建请求数据
     */
    private Map<String, Object> buildRequestData(RiskFlowContext context, Map<String, Object> features, Map<String, Object> extInfo) {
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("userId", context.getUserId());
        requestData.put("eventType", context.getEventType());
        requestData.put("userIp", context.getUserIp());
        requestData.put("deviceId", context.getDeviceId());
        if (features != null) {
            requestData.put("features", features);
        }
        if (extInfo != null) {
            requestData.put("extInfo", extInfo);
        }
        return requestData;
    }
}
