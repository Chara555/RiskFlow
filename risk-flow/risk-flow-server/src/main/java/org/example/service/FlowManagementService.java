package org.example.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.example.core.util.BizException;
import org.example.core.util.ErrorCode;
import org.example.conversion.engine.FlowConversionEngine;
import org.example.conversion.model.ConversionResult;
import org.example.conversion.model.FlowDefinition;
import org.example.conversion.model.FlowEdge;
import org.example.conversion.model.FlowNode;
import org.example.dto.FlowRequest;
import org.example.dto.FlowResponse;
import org.example.entity.Workflow;
import org.example.repository.WorkflowRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 流程管理服务
 */
@Slf4j
@Service
public class FlowManagementService {

    // 提取魔法字符串为常量
    private static final String STATUS_DRAFT = "DRAFT";
    private static final String STATUS_ACTIVE = "ACTIVE";

    private final WorkflowRepository workflowRepository;
    private final FlowConversionEngine conversionEngine;
    private final ObjectMapper objectMapper;

    public FlowManagementService(WorkflowRepository workflowRepository,
                                 FlowConversionEngine conversionEngine,
                                 ObjectMapper objectMapper) {
        this.workflowRepository = workflowRepository;
        this.conversionEngine = conversionEngine;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public FlowResponse createFlow(FlowRequest request) {
        FlowDefinition flow = toFlowDefinition(request);
        String flowJson;
        try {
            flowJson = objectMapper.writeValueAsString(flow);
        } catch (JsonProcessingException e) {
            throw new BizException(ErrorCode.FLOW_CONVERT_ERROR, e, "序列化失败");
        }

        ConversionResult result = conversionEngine.convert(flow);

        Workflow workflow = new Workflow();
        workflow.setCode(request.getCode());
        workflow.setName(request.getName());
        workflow.setDescription(request.getDescription());
        workflow.setFlowData(flowJson);
        workflow.setStatus(STATUS_DRAFT);
        workflow.setVersion(1);

        if (result.isSuccess()) {
            workflow.setElExpression(result.getElExpression());
        }

        workflow = workflowRepository.save(workflow);
        return toResponse(workflow, result);
    }

    @Transactional
    public FlowResponse updateFlow(Long id, FlowRequest request) {
        Workflow workflow = workflowRepository.findById(id)
                .orElseThrow(() -> new BizException(ErrorCode.FLOW_NOT_FOUND, id));

        FlowDefinition flow = toFlowDefinition(request);
        String flowJson;
        try {
            flowJson = objectMapper.writeValueAsString(flow);
        } catch (JsonProcessingException e) {
            throw new BizException(ErrorCode.FLOW_CONVERT_ERROR, e, "序列化失败");
        }

        ConversionResult result = conversionEngine.convert(flow);

        workflow.setName(request.getName());
        workflow.setDescription(request.getDescription());
        workflow.setFlowData(flowJson);

        if (result.isSuccess()) {
            workflow.setElExpression(result.getElExpression());
            workflow.setStatus(STATUS_DRAFT);
        }

        workflow = workflowRepository.save(workflow);
        return toResponse(workflow, result);
    }

    public FlowResponse getFlow(Long id) {
        Workflow workflow = workflowRepository.findById(id)
                .orElseThrow(() -> new BizException(ErrorCode.FLOW_NOT_FOUND, id));
        return toResponse(workflow, null);
    }

    public List<FlowResponse> listFlows() {
        return workflowRepository.findAll().stream()
                .map(w -> toResponse(w, null))
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteFlow(Long id) {
        workflowRepository.deleteById(id);
    }

    @Transactional
    public FlowResponse deployFlow(Long id) {
        Workflow workflow = workflowRepository.findById(id)
                .orElseThrow(() -> new BizException(ErrorCode.FLOW_NOT_FOUND, id));

        if (workflow.getElExpression() == null || workflow.getElExpression().isBlank()) {
            throw new BizException(ErrorCode.FLOW_DEPLOY_FAILED, "EL表达式为空");
        }

        // TODO: 调用 LiteFlow API 动态加载规则
        // flowExecutor.reloadRule(workflow.getCode());

        workflow.setStatus(STATUS_ACTIVE);
        workflow.setVersion(workflow.getVersion() + 1);
        workflow = workflowRepository.save(workflow);

        log.info("流程部署成功: code={}, el={}", workflow.getCode(), workflow.getElExpression());
        return toResponse(workflow, null);
    }

    private FlowDefinition toFlowDefinition(FlowRequest request) {
        FlowDefinition flow = new FlowDefinition();
        flow.setCode(request.getCode());
        flow.setName(request.getName());
        flow.setDescription(request.getDescription());

        List<FlowNode> nodes = request.getNodes().stream()
                .map(n -> {
                    FlowNode node = new FlowNode();
                    node.setId(n.getId());
                    node.setType(n.getType());
                    node.setComponentId(n.getComponentId());
                    node.setLabel(n.getLabel());
                    node.setProperties(n.getProperties());
                    node.setX(n.getX());
                    node.setY(n.getY());
                    return node;
                })
                .collect(Collectors.toList());

        List<FlowEdge> edges = request.getEdges() == null ? List.of() :
                request.getEdges().stream()
                        .map(e -> {
                            FlowEdge edge = new FlowEdge();
                            edge.setId(e.getId());
                            edge.setSourceNodeId(e.getSourceNodeId());
                            edge.setTargetNodeId(e.getTargetNodeId());
                            edge.setLabel(e.getLabel());
                            edge.setType(e.getType());

                            // 【关键修复】：接通 SWITCH 的输血管！
                            edge.setConditionValue(e.getConditionValue());
                            edge.setProperties(e.getProperties());

                            return edge;
                        })
                        .collect(Collectors.toList());

        flow.setNodes(nodes);
        flow.setEdges(edges);
        return flow;
    }

    private FlowResponse toResponse(Workflow workflow, ConversionResult result) {
        FlowResponse response = new FlowResponse();
        response.setId(workflow.getId());
        response.setCode(workflow.getCode());
        response.setName(workflow.getName());
        response.setDescription(workflow.getDescription());
        response.setVersion(workflow.getVersion());
        response.setStatus(workflow.getStatus());
        response.setFlowData(workflow.getFlowData());
        response.setElExpression(workflow.getElExpression());
        response.setCreatedBy(workflow.getCreatedBy());
        response.setCreatedAt(workflow.getCreatedAt());
        response.setUpdatedAt(workflow.getUpdatedAt());

        if (result != null) {
            response.setErrors(result.getErrors());
            response.setWarnings(result.getWarnings());
        }
        return response;
    }
}