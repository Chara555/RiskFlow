package org.example.context;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.example.core.model.RiskSignal;

/**
 * 风控流程上下文 - 信号驱动制
 * 
 * 核心设计原则：
 *   所有风控节点只产出 {@link RiskSignal}，不直接做决策
 *   大脑节点收集所有信号后综合判断最终结果
 *   信号容器使用 ConcurrentHashMap 保障 LiteFlow 并发执行安全
 *
 */
@Data
public class RiskFlowContext implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    // ==================== 输入参数 ====================
    /** 流程ID */
    private Long workflowId;

    /** 事件ID */
    private String eventId;
    
    /** 事件类型（如 login, payment, transfer） */
    private String eventType;
    
    /** 用户ID */
    private String userId;
    
    /** 用户IP */
    private String userIp;
    
    /** 设备ID */
    private String deviceId;
    
    /** 特征数据 */
    private Map<String, Object> features = new HashMap<>();
    
    /** 扩展信息 */
    private Map<String, Object> extInfo = new HashMap<>();
    
    /** 请求到达的绝对时间戳（毫秒） */
    private Long requestTimeMs;

    // ==================== 信号容器（核心） ====================
    
    /**
     * 唯一的信号收集篮：componentId -> RiskSignal
     * 使用 ConcurrentHashMap 保障 LiteFlow WHEN 并发编排安全
     */
    private Map<String, RiskSignal> riskSignals = new ConcurrentHashMap<>();

    // ==================== 输出结果 ====================
    /** 决策结果 */
    private DecisionResult result;
    
    /** 决策消息 */
    private String resultMessage;
    
    /** 决策完成的绝对时间戳（毫秒） */
    private Long decisionTimeMs;
    
    /** 执行耗时（毫秒） */
    private Long executionTimeMs;

    /**
     * 决策结果枚举
     */
    public enum DecisionResult {
        ACCEPT,     // 通过
        REJECT,     // 拒绝
        REVIEW,     // 人工审核
        CHALLENGE   // 挑战（验证码）
    }

    // ==================== 便捷方法 ====================

    /**
     * 设置特征值
     */
    public void setFeature(String key, Object value) {
        this.features.put(key, value);
    }

    /**
     * 获取特征值
     */
    public Object getFeature(String key) {
        return this.features.get(key);
    }

    // ==================== 信号操作 API ====================

    /**
     * 写入信号（供 AbstractRiskComponent 调用）
     */
    public void addRiskSignal(String componentId, RiskSignal signal) {
        if (signal != null) {
            this.riskSignals.put(componentId, signal);
        }
    }

    /**
     * 获取不可变快照（供大脑节点和落库使用，防止篡改证据）
     */
    public Map<String, RiskSignal> getSignalSnapshot() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(this.riskSignals));
    }

    /**
     * 按风险等级过滤信号
     */
    public List<RiskSignal> getSignalsByLevel(String level) {
        return riskSignals.values().stream()
                .filter(s -> level.equals(s.getRiskLevel()))
                .toList();
    }

    /**
     * 是否存在任何 CRITICAL 级别信号（大脑节点快速判断用）
     */
    public boolean hasCriticalSignal() {
        return riskSignals.values().stream()
                .anyMatch(s -> RiskSignal.LEVEL_CRITICAL.equals(s.getRiskLevel()));
    }

    /**
     * 统计各等级信号数量（大脑节点决策参考）
     */
    public Map<String, Long> getLevelCounts() {
        return riskSignals.values().stream()
                .collect(Collectors.groupingBy(RiskSignal::getRiskLevel, Collectors.counting()));
    }
}
