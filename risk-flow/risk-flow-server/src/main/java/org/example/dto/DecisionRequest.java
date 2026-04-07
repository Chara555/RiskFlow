package org.example.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Map;

/**
 * 风控决策请求 DTO
 * 接收前端或业务网关的风控评估请求
 */
@Data
public class DecisionRequest {

    /** 事件唯一标识（可选，不传则由服务端自动生成） */
    private String eventId;

    /** 事件类型（必填，如 login / payment / register，用于路由到对应工作流链路） */
    @NotBlank(message = "eventType 不能为空")
    private String eventType;

    /** 用户ID */
    private String userId;

    /** 用户IP */
    private String userIp;

    /** 设备ID */
    private String deviceId;

    /** 动态特征集（由业务方按需传入，如 failedLoginCount、isNewDevice 等） */
    private Map<String, Object> features;

    /** 扩展信息（业务透传字段，不参与风控计算） */
    private Map<String, Object> extInfo;
}
