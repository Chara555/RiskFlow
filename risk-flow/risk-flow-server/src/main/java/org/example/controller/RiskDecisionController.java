package org.example.controller;

import org.example.context.RiskFlowContext;
import org.example.dto.DecisionRequest;
import org.example.dto.DecisionResponse;
import org.example.service.RiskDecisionService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 风控决策 REST API
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
     */
    @PostMapping("/decide")
    public DecisionResponse decide(@RequestBody DecisionRequest request) {
        RiskFlowContext context = decisionService.decide(
                request.getEventId(),
                request.getUserId(),
                request.getEventType(),
                request.getUserIp(),
                request.getDeviceId(),
                request.getFeatures(),
                request.getExtInfo()
        );
        return toResponse(context);
    }

    private DecisionResponse toResponse(RiskFlowContext context) {
        DecisionResponse response = new DecisionResponse();
        response.setDecisionId(context.getEventId());
        response.setResult(context.getResult() != null ? context.getResult().name() : "UNKNOWN");
        response.setRiskScore(context.getTotalRiskScore());
        response.setMessage(context.getResultMessage());
        response.setDecisionTime(context.getDecisionTime());
        response.setExecutionTimeMs(context.getExecutionTimeMs());
        return response;
    }
}
