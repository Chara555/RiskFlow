package org.example.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.client.RiskFlowRestClient;
import org.example.context.RiskFlowContext;
import org.example.dto.RiskFlowResponse;
import org.example.service.RiskFlowService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * RiskFlow 示例接口
 * 
 * 演示两种调用方式：
 * 1. SDK 嵌入模式 - 直接调用 risk-flow-core
 * 2. REST API 模式 - 通过 HTTP 调用 risk-flow-server
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/sample")
@RequiredArgsConstructor
public class SampleController {

    private final RiskFlowService riskFlowService;
    private final RiskFlowRestClient riskFlowRestClient;

    // ==================== SDK 嵌入模式 ====================

    /**
     * 登录风控（SDK 模式）
     */
    @PostMapping("/login")
    public RiskFlowResponse loginViaSdk(@RequestBody LoginRequest request) {
        log.info("收到登录风控请求(SDK): userId={}, isNewDevice={}, failedLoginCount={}",
                request.getUserId(), request.isNewDevice(), request.getFailedLoginCount());

        RiskFlowContext context = riskFlowService.loginRiskCheck(
                request.getUserId(),
                request.getUserIp(),
                request.isNewDevice(),
                request.getFailedLoginCount()
        );

        return toResponse(context);
    }

    /**
     * 支付风控（SDK 模式）
     */
    @PostMapping("/payment")
    public RiskFlowResponse paymentViaSdk(@RequestBody PaymentRequest request) {
        log.info("收到支付风控请求(SDK): userId={}, amount={}, paymentMethod={}",
                request.getUserId(), request.getAmount(), request.getPaymentMethod());

        RiskFlowContext context = riskFlowService.paymentRiskCheck(
                request.getUserId(),
                request.getUserIp(),
                request.getAmount(),
                request.getPaymentMethod()
        );

        return toResponse(context);
    }

    /**
     * 注册风控（SDK 模式）
     */
    @PostMapping("/register")
    public RiskFlowResponse registerViaSdk(@RequestBody RegisterRequest request) {
        log.info("收到注册风控请求(SDK): userId={}, hasInviteCode={}",
                request.getUserId(), request.getInviteCode() != null);

        RiskFlowContext context = riskFlowService.registerRiskCheck(
                request.getUserId(),
                request.getUserIp(),
                request.getInviteCode()
        );

        return toResponse(context);
    }

    // ==================== REST API 模式 ====================

    /**
     * 登录风控（REST 模式）
     */
    @PostMapping("/login-via-http")
    public RiskFlowResponse loginViaHttp(@RequestBody LoginRequest request) {
        log.info("收到登录风控请求(REST): userId={}, isNewDevice={}, failedLoginCount={}",
                request.getUserId(), request.isNewDevice(), request.getFailedLoginCount());

        return riskFlowRestClient.loginRiskCheck(
                request.getUserId(),
                request.getUserIp(),
                request.isNewDevice(),
                request.getFailedLoginCount()
        );
    }

    /**
     * 支付风控（REST 模式）
     */
    @PostMapping("/payment-via-http")
    public RiskFlowResponse paymentViaHttp(@RequestBody PaymentRequest request) {
        log.info("收到支付风控请求(REST): userId={}, amount={}, paymentMethod={}",
                request.getUserId(), request.getAmount(), request.getPaymentMethod());

        return riskFlowRestClient.paymentRiskCheck(
                request.getUserId(),
                request.getUserIp(),
                request.getAmount(),
                request.getPaymentMethod()
        );
    }

    // ==================== 对比测试 ====================

    /**
     * 对比 SDK 和 REST 两种方式
     */
    @PostMapping("/compare")
    public Map<String, Object> compare(@RequestBody LoginRequest request) {
        log.info("========== 开始对比测试 ==========");

        // SDK 模式
        long sdkStart = System.currentTimeMillis();
        RiskFlowContext sdkResult = riskFlowService.loginRiskCheck(
                request.getUserId(),
                request.getUserIp(),
                request.isNewDevice(),
                request.getFailedLoginCount()
        );
        long sdkCost = System.currentTimeMillis() - sdkStart;

        // REST 模式
        long restStart = System.currentTimeMillis();
        RiskFlowResponse restResult = riskFlowRestClient.loginRiskCheck(
                request.getUserId(),
                request.getUserIp(),
                request.isNewDevice(),
                request.getFailedLoginCount()
        );
        long restCost = System.currentTimeMillis() - restStart;

        Map<String, Object> compareResult = new HashMap<>();
        compareResult.put("sdk", Map.of(
                "result", sdkResult.getResult(),
                "score", sdkResult.getTotalRiskScore(),
                "costMs", sdkCost,
                "message", sdkResult.getResultMessage()
        ));
        compareResult.put("rest", Map.of(
                "result", restResult.getResult(),
                "score", restResult.getRiskScore(),
                "costMs", restCost,
                "message", restResult.getMessage()
        ));
        compareResult.put("summary", Map.of(
                "faster", sdkCost <= restCost ? "SDK" : "REST",
                "sdkAdvantage", Math.abs(sdkCost - restCost) + "ms"
        ));

        log.info("========== 对比测试完成 ==========");
        log.info("SDK 耗时: {}ms, REST 耗时: {}ms", sdkCost, restCost);

        return compareResult;
    }

    /**
     * 健康检查
     */
    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("sdk", "available");
        health.put("rest", riskFlowRestClient.healthCheck() ? "available" : "unavailable");
        return health;
    }

    // ==================== 辅助方法 ====================

    /**
     * 将 RiskFlowContext 转换为响应 DTO
     */
    private RiskFlowResponse toResponse(RiskFlowContext context) {
        return RiskFlowResponse.builder()
                .decisionId(context.getEventId())
                .result(context.getResult() != null ? context.getResult().name() : null)
                .riskScore(context.getTotalRiskScore())
                .message(context.getResultMessage())
                .decisionTime(context.getDecisionTime())
                .executionTimeMs(context.getExecutionTimeMs())
                .aiAnalysisResult(context.getAiAnalysisResult())
                .details(Map.of(
                        "isHighRisk", context.getIsHighRisk(),
                        "ruleScores", context.getRuleScores(),
                        "baseCheckResults", context.getBaseCheckResults()
                ))
                .build();
    }

    // ==================== 请求 DTO ====================

    @Data
    public static class LoginRequest {
        private String userId;
        private String userIp;
        private boolean newDevice;
        private int failedLoginCount;

        public boolean isNewDevice() {
            return newDevice;
        }

        public int getFailedLoginCount() {
            return failedLoginCount;
        }
    }

    @Data
    public static class PaymentRequest {
        private String userId;
        private String userIp;
        private double amount;
        private String paymentMethod;
    }

    @Data
    public static class RegisterRequest {
        private String userId;
        private String userIp;
        private String inviteCode;
    }
}
