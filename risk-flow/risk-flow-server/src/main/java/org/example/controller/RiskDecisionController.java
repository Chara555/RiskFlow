package org.example.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.DecisionRequest;
import org.example.dto.DecisionResponse;
import org.example.service.RiskDecisionService;
import org.springframework.web.bind.annotation.*;

/**
 * 风控决策 API 入口
 *
 * 由虚拟线程池托管（spring.threads.virtual.enabled=true），
 * 每次调用都在轻量级虚拟线程中运行。
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/risk")
public class RiskDecisionController {

    private final RiskDecisionService decisionService;

    public RiskDecisionController(RiskDecisionService decisionService) {
        this.decisionService = decisionService;
    }

    /**
     * 执行风控决策
     *
     * POST /api/v1/risk/decide
     * 请求体：{@link DecisionRequest}
     * 响应体：{@link DecisionResponse}
     */
    @PostMapping("/decide")
    public DecisionResponse decide(@Valid @RequestBody DecisionRequest request) {
        return decisionService.evaluate(request);
    }
}
