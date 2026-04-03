package org.example.controller;

import jakarta.validation.Valid;
import org.example.context.RiskFlowContext;
import org.example.dto.DecisionRequest;
import org.example.dto.DecisionResponse;
import org.example.service.RiskDecisionService;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * 风控决策入口：由虚拟线程池托管处理
 */
@RestController
@RequestMapping("/api/v1/risk")
public class RiskDecisionController {

    private final RiskDecisionService decisionService;

    public RiskDecisionController(RiskDecisionService decisionService) {
        this.decisionService = decisionService;
    }

    /**
     * 执行风控决策
     * 注意：由于开启了 spring.threads.virtual.enabled=true，
     * 这里的每一次调用都会在一个轻量级的虚拟线程中运行。
     */
    @PostMapping("/decide")
    public DecisionResponse decide(@Valid @RequestBody DecisionRequest request) {
        // 直接传递 Request 对象，保持接口简洁
        RiskFlowContext context = decisionService.decide(request);
        return toResponse(context);
    }

    private DecisionResponse toResponse(RiskFlowContext context) {
        DecisionResponse response = new DecisionResponse();
        response.setDecisionId(context.getEventId());

        // 健壮性处理：确保结果不为空
        String resultStr = (context.getResult() != null) ? context.getResult().name() : "UNKNOWN";
        response.setResult(resultStr);

        response.setRiskScore(context.getTotalRiskScore());
        response.setMessage(context.getResultMessage());

        // 统一时区处理：将毫秒戳转为 UTC 展现时间
        if (context.getDecisionTimeMs() != null) {
            response.setDecisionTime(
                    LocalDateTime.ofInstant(Instant.ofEpochMilli(context.getDecisionTimeMs()), ZoneOffset.UTC)
            );
        }

        response.setExecutionTimeMs(context.getExecutionTimeMs());
        return response;
    }
}