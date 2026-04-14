package org.example.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.example.core.util.Result;
import org.example.dto.FlowRequest;
import org.example.dto.FlowResponse;
import org.example.service.FlowManagementService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 流程管理 API
 *
 * 提供流程的 CRUD 和部署功能：
 *   - POST   /api/v1/flows      创建流程
 *   - PUT    /api/v1/flows/{id} 更新流程
 *   - GET    /api/v1/flows/{id} 获取流程
 *   - GET    /api/v1/flows      流程列表
 *   - DELETE /api/v1/flows/{id} 删除流程
 *   - POST   /api/v1/flows/{id}/deploy 部署流程
 *
 * <p>所有接口返回统一响应结构 {@link Result}
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/flows")
public class FlowManagementController {

    private final FlowManagementService flowService;

    public FlowManagementController(FlowManagementService flowService) {
        this.flowService = flowService;
    }

    /**
     * 创建流程
     *
     * 接收前端可视化的 JSON 数据，转换为 LiteFlow EL 表达式并保存
     */
    @PostMapping
    public Result<FlowResponse> createFlow(@Valid @RequestBody FlowRequest request) {
        log.info("创建流程: code={}, name={}", request.getCode(), request.getName());
        FlowResponse response = flowService.createFlow(request);
        return Result.success(response);
    }

    /**
     * 更新流程
     */
    @PutMapping("/{id}")
    public Result<FlowResponse> updateFlow(
            @PathVariable Long id,
            @Valid @RequestBody FlowRequest request) {
        log.info("更新流程: id={}, code={}", id, request.getCode());
        FlowResponse response = flowService.updateFlow(id, request);
        return Result.success(response);
    }

    /**
     * 获取流程
     */
    @GetMapping("/{id}")
    public Result<FlowResponse> getFlow(@PathVariable Long id) {
        FlowResponse response = flowService.getFlow(id);
        return Result.success(response);
    }

    /**
     * 获取流程列表
     */
    @GetMapping
    public Result<List<FlowResponse>> listFlows() {
        List<FlowResponse> flows = flowService.listFlows();
        return Result.success(flows);
    }

    /**
     * 删除流程
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteFlow(@PathVariable Long id) {
        log.info("删除流程: id={}", id);
        flowService.deleteFlow(id);
        return Result.success();
    }

    /**
     * 部署流程
     *
     * 将流程的 EL 表达式加载到 LiteFlow 运行时
     */
    @PostMapping("/{id}/deploy")
    public Result<FlowResponse> deployFlow(@PathVariable Long id) {
        log.info("部署流程: id={}", id);
        FlowResponse response = flowService.deployFlow(id);
        return Result.success(response);
    }
}
