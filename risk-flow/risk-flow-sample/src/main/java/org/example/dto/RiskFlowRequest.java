package org.example.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 风控决策请求 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskFlowRequest {

    /**
     * 事件ID（可选，为空时服务端自动生成）
     */
    private String eventId;

    /**
     * 事件类型：login / payment / register
     */
    private String eventType;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 用户IP
     */
    private String userIp;

    /**
     * 设备ID（可选）
     */
    private String deviceId;

    /**
     * 特征数据
     */
    private Map<String, Object> features;

    /**
     * 扩展信息（可选）
     */
    private Map<String, Object> extInfo;
}
