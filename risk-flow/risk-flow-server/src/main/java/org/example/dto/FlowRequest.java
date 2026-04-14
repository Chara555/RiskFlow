package org.example.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.List;
import java.util.Map;

/**
 * 创建/更新流程的请求体
 */
@Data
public class FlowRequest {

    /**
     * 流程唯一编码
     */
    @NotBlank(message = "流程编码不能为空")
    private String code;

    /**
     * 流程名称
     */
    @NotBlank(message = "流程名称不能为空")
    private String name;

    /**
     * 流程描述
     */
    private String description;

    /**
     * 节点列表
     */
    @NotEmpty(message = "节点列表不能为空")
    private List<NodeDto> nodes;

    /**
     * 连线列表
     */
    private List<EdgeDto> edges;

    @Data
    public static class NodeDto {
        private String id;
        private String type;
        private String componentId;
        private String label;
        private Map<String, Object> properties;
        private Double x;
        private Double y;
    }

    @Data
    public static class EdgeDto {
        private String id;
        private String sourceNodeId;
        private String targetNodeId;

        /**
         * UI 连线标签（如："高风险拦截"）
         */
        private String label;

        /**
         * 连线类型（非常重要！）
         * 取值建议："default" (普通流转), "condition" (条件分支)
         */
        private String type;

        //以下为新增的条件分支核心字段

        /**
         * 条件匹配值（当 type = "condition" 时必填）
         * 比如底层算子返回 "HIGH"，如果这个值配的是 "HIGH"，引擎就会走这条线。
         */
        private String conditionValue;

        /**
         * 为连线增加扩展属性（与 NodeDto 保持一致，为未来铺路）
         * 比如未来你可能需要做 A/B 测试，连线上需要配权重 "weight": 80
         */
        private Map<String, Object> properties;
    }
}
