package org.example.conversion.model;

import lombok.Data;

/**
 * 流程连线
 * 对应前端 LogicFlow 中两个节点之间的一条有向边
 */
@Data
public class FlowEdge {

    /**
     * 边唯一ID（前端生成）
     */
    private String id;

    /**
     * 来源节点ID
     */
    private String sourceNodeId;

    /**
     * 目标节点ID
     */
    private String targetNodeId;

    /**
     * 边的标签（条件分支时使用）
     * 条件节点的出边必须设置 label："true" 或 "false"
     * 普通边可以为 null
     */
    private String label;

    /**
     * 边的类型（前端渲染用，引擎不关心）
     * 例："polyline" / "bezier" / "line"
     */
    private String type;
}
