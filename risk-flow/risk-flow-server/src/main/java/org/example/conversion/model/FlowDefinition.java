package org.example.conversion.model;

import lombok.Data;
import java.util.List;

/**
 * 流程定义
 * 对应前端 LogicFlow 导出的完整画布数据（graphData）
 *
 * 前端导出格式示例：
 * <pre>
 * {
 *   "code": "my-flow-001",
 *   "name": "登录风控流程",
 *   "nodes": [
 *     {"id": "node_1", "type": "process", "componentId": "loadContext", "label": "加载上下文"},
 *     {"id": "node_2", "type": "process", "componentId": "ipBlacklistCheck", "label": "IP黑名单"},
 *     {"id": "node_3", "type": "process", "componentId": "decisionJudge", "label": "决策"}
 *   ],
 *   "edges": [
 *     {"id": "edge_1", "sourceNodeId": "node_1", "targetNodeId": "node_2"},
 *     {"id": "edge_2", "sourceNodeId": "node_2", "targetNodeId": "node_3"}
 *   ]
 * }
 * </pre>
 */
@Data
public class FlowDefinition {

    /**
     * 流程唯一编码（对应 workflow.code，LiteFlow 执行时作为 chainId）
     * 若为空，保存时自动生成
     */
    private String code;

    /**
     * 流程名称
     */
    private String name;

    /**
     * 流程描述
     */
    private String description;

    /**
     * 所有节点列表
     */
    private List<FlowNode> nodes;

    /**
     * 所有连线列表
     */
    private List<FlowEdge> edges;
}
