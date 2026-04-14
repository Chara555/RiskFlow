package org.example.conversion.model;

import lombok.Data;
import java.util.Map;

/**
 * 流程连线 (AST 引擎核心基础模型)
 * 对应前端画布中两个节点之间的一条有向边
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
     * UI 连线标签（如："高风险拦截"），主要供前端展示使用
     */
    private String label;

    /**
     * 连线类型（引擎核心路由依据！）
     * 约定："default" (普通顺序流转) / "condition" (条件分支流转)
     */
    private String type;

    // ==================== 以下为 SWITCH 条件路由核心字段 ====================

    /**
     * 条件匹配值（当 type = "condition" 时必填）
     * 引擎生成 AST 时，会将其作为 SWITCH(...).to(...) 分支对应的 ID 标签
     * 例："HIGH", "SAFE"
     */
    private String conditionValue;

    /**
     * 连线扩展属性（为未来预留）
     * 例如：A/B 测试时的流量权重配置 ("weight": 80)
     */
    private Map<String, Object> properties;
}