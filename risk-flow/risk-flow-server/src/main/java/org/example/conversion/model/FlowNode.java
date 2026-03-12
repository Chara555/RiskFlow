package org.example.conversion.model;

import lombok.Data;
import java.util.Map;

/**
 * 流程节点
 * 对应前端 LogicFlow 中的一个节点
 */
@Data
public class FlowNode {

    /**
     * 节点唯一ID（前端生成，如 "node_1"）
     */
    private String id;

    /**
     * 节点类型（前端渲染用，决定节点外观）
     * 例："start" / "end" / "process" / "condition" / "parallel"
     */
    private String type;

    /**
     * 对应的 LiteFlow 组件ID（最关键字段）
     * 例："ipBlacklistCheck" / "ruleExecute" / "decisionJudge"
     * 前端节点拖入时，用户选择的组件，绑定到此字段
     */
    private String componentId;

    /**
     * 节点显示名称（仅用于前端展示）
     */
    private String label;

    /**
     * 节点自定义配置属性（JSON对象）
     * 例：{"threshold": 80, "timeout": 3000}
     */
    private Map<String, Object> properties;

    /**
     * 节点在画布上的 X 坐标（仅前端布局用，引擎不关心）
     */
    private Double x;

    /**
     * 节点在画布上的 Y 坐标（仅前端布局用，引擎不关心）
     */
    private Double y;
}
