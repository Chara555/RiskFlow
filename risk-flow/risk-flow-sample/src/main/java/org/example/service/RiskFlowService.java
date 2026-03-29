package org.example.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.context.RiskFlowContext;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

/**
 * RiskFlow SDK 服务（嵌入式模式）
 * 
 * 通过直接调用 LiteFlow 执行风控决策流程。
 * 这是推荐的方式，性能最优，适合 Java 应用集成。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RiskFlowService {

    // 注入 LiteFlow FlowExecutor（由 LiteFlow 自动装配）
    private final com.yomahub.liteflow.flow.FlowExecutor flowExecutor;

    /**
     * 执行风控决策（简化版）
     *
     * @param eventType 事件类型：login / payment / register
     * @param userId    用户ID
     * @param userIp    用户IP
     * @param features  特征数据
     * @return 风控上下文（含决策结果）
     */
    public RiskFlowContext decide(String eventType, String userId, String userIp, Map<String, Object> features) {
        return decide(eventType, userId, userIp, null, features);
    }

    /**
     * 执行风控决策（完整版）
     *
     * @param eventType 事件类型
     * @param userId    用户ID
     * @param userIp    用户IP
     * @param deviceId  设备ID（可选）
     * @param features  特征数据
     * @return 风控上下文
     */
    public RiskFlowContext decide(String eventType, String userId, String userIp, 
                                   String deviceId, Map<String, Object> features) {
        // 构建上下文
        RiskFlowContext context = buildContext(eventType, userId, userIp, deviceId, features);
        
        // 执行决策流程
        return executeDecision(context);
    }

    /**
     * 构建风控上下文
     */
    private RiskFlowContext buildContext(String eventType, String userId, String userIp,
                                          String deviceId, Map<String, Object> features) {
        RiskFlowContext context = new RiskFlowContext();
        context.setEventId(UUID.randomUUID().toString());
        context.setEventType(eventType);
        context.setUserId(userId);
        context.setUserIp(userIp);
        context.setDeviceId(deviceId);
        
        // 设置特征数据
        if (features != null) {
            context.setFeatures(new java.util.concurrent.ConcurrentHashMap<>(features));
        }
        
        return context;
    }

    /**
     * 执行决策流程
     */
    private RiskFlowContext executeDecision(RiskFlowContext context) {
        long startTime = System.currentTimeMillis();
        
        try {
            log.info("========== 开始风控决策 ==========");
            log.info("事件类型: {}, 用户ID: {}, IP: {}", 
                    context.getEventType(), context.getUserId(), context.getUserIp());
            log.info("特征数据: {}", context.getFeatures());
            
            // 调用 LiteFlow 执行决策流程
            RiskFlowContext result = flowExecutor.execute2Resp("riskDecisionFlow", context);
            
            long cost = System.currentTimeMillis() - startTime;
            result.setExecutionTimeMs(cost);
            
            log.info("========== 决策完成 ==========");
            log.info("决策结果: {}, 风险评分: {}, 耗时: {}ms", 
                    result.getResult(), result.getTotalRiskScore(), cost);
            log.info("决策描述: {}", result.getResultMessage());
            
            return result;
            
        } catch (Exception e) {
            log.error("风控决策执行异常", e);
            context.setResult(RiskFlowContext.DecisionResult.REJECT);
            context.setResultMessage("系统异常：" + e.getMessage());
            context.setExecutionTimeMs(System.currentTimeMillis() - startTime);
            return context;
        }
    }

    /**
     * 执行登录风控（快捷方法）
     */
    public RiskFlowContext loginRiskCheck(String userId, String userIp, 
                                           boolean isNewDevice, int failedLoginCount) {
        Map<String, Object> features = Map.of(
                "isNewDevice", isNewDevice,
                "failedLoginCount", failedLoginCount
        );
        return decide("login", userId, userIp, features);
    }

    /**
     * 执行支付风控（快捷方法）
     */
    public RiskFlowContext paymentRiskCheck(String userId, String userIp, 
                                             double amount, String paymentMethod) {
        Map<String, Object> features = Map.of(
                "amount", amount,
                "paymentMethod", paymentMethod
        );
        return decide("payment", userId, userIp, features);
    }

    /**
     * 执行注册风控（快捷方法）
     */
    public RiskFlowContext registerRiskCheck(String userId, String userIp, 
                                              String inviteCode) {
        Map<String, Object> features = new java.util.HashMap<>();
        if (inviteCode != null) {
            features.put("hasInviteCode", true);
            features.put("inviteCode", inviteCode);
        }
        return decide("register", userId, userIp, features);
    }
}
