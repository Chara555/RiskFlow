package org.example.client;

import lombok.extern.slf4j.Slf4j;
import org.example.dto.RiskFlowRequest;
import org.example.dto.RiskFlowResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * RiskFlow REST 客户端
 * 
 * 通过 HTTP 调用独立的 RiskFlow Server 服务。
 * 适用于：
 * 1. 非 Java 语言调用（Python/Go/Node.js）
 * 2. 微服务架构解耦
 * 3. 需要独立扩缩容的场景
 */
@Slf4j
@Component
public class RiskFlowRestClient {

    @Value("${riskflow-server.base-url:http://localhost:8080}")
    private String serverBaseUrl;

    private final WebClient webClient;

    public RiskFlowRestClient() {
        this.webClient = WebClient.builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .responseTimeout(10, TimeUnit.SECONDS)
                .build();
    }

    /**
     * 执行风控决策（完整版）
     */
    public RiskFlowResponse decide(String eventType, String userId, String userIp,
                                    String deviceId, Map<String, Object> features) {
        RiskFlowRequest request = RiskFlowRequest.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType(eventType)
                .userId(userId)
                .userIp(userIp)
                .deviceId(deviceId)
                .features(features)
                .build();

        log.info("========== [REST] 发送风控请求 ==========");
        log.info("Server: {}", serverBaseUrl);
        log.info("Request: {}", request);

        try {
            RiskFlowResponse response = webClient.post()
                    .uri(serverBaseUrl + "/api/v1/risk/decide")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToFlux(RiskFlowResponse.class)
                    .next()
                    .block();

            log.info("========== [REST] 收到风控响应 ==========");
            log.info("Response: {}", response);
            
            return response;
        } catch (Exception e) {
            log.error("[REST] 调用风控服务异常", e);
            return RiskFlowResponse.builder()
                    .decisionId(request.getEventId())
                    .result("ERROR")
                    .riskScore(0)
                    .message("调用风控服务异常：" + e.getMessage())
                    .build();
        }
    }

    /**
     * 执行风控决策（简化版）
     */
    public RiskFlowResponse decide(String eventType, String userId, String userIp,
                                    Map<String, Object> features) {
        return decide(eventType, userId, userIp, null, features);
    }

    /**
     * 执行登录风控
     */
    public RiskFlowResponse loginRiskCheck(String userId, String userIp,
                                             boolean isNewDevice, int failedLoginCount) {
        Map<String, Object> features = Map.of(
                "isNewDevice", isNewDevice,
                "failedLoginCount", failedLoginCount
        );
        return decide("login", userId, userIp, features);
    }

    /**
     * 执行支付风控
     */
    public RiskFlowResponse paymentRiskCheck(String userId, String userIp,
                                              double amount, String paymentMethod) {
        Map<String, Object> features = Map.of(
                "amount", amount,
                "paymentMethod", paymentMethod
        );
        return decide("payment", userId, userIp, features);
    }

    /**
     * 执行注册风控
     */
    public RiskFlowResponse registerRiskCheck(String userId, String userIp,
                                               String inviteCode) {
        Map<String, Object> features = new java.util.HashMap<>();
        if (inviteCode != null) {
            features.put("hasInviteCode", true);
            features.put("inviteCode", inviteCode);
        }
        return decide("register", userId, userIp, features);
    }

    /**
     * 健康检查
     */
    public boolean healthCheck() {
        try {
            String result = webClient.get()
                    .uri(serverBaseUrl + "/actuator/health")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            log.info("RiskFlow Server 健康状态: {}", result);
            return result != null;
        } catch (Exception e) {
            log.warn("RiskFlow Server 健康检查失败: {}", e.getMessage());
            return false;
        }
    }
}
