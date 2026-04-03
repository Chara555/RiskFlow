package org.example.context;

import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.example.core.model.RiskSignal;

/**
 * 风控流程上下文 - 使用 DataBus 机制
 * 注意：此类不直接继承 DataBus，而是作为数据载体
 */
@Data
public class RiskFlowContext implements Serializable {

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

    // ==================== 中间结果 ====================
    /** 基础检测结果 */
    private Map<String, Boolean> baseCheckResults = new ConcurrentHashMap<>();
    
    /** 存储每个节点的风险信号 */
    private Map<String, RiskSignal> riskSignals = new ConcurrentHashMap<>();
    
    /** 总风险评分 (线程安全) */
    private AtomicInteger totalRiskScore = new AtomicInteger(0);
    
    /** AI 分析结果 */
    private String aiAnalysisResult;
    
    /** 是否高风险 */
    private Boolean isHighRisk = false;

    // ==================== 输出结果 ====================
    /** 决策结果 */
    private DecisionResult result;
    
    /** 决策消息 */
    private String resultMessage;
    
    /** 决策完成的绝对时间戳（毫秒） */
    private Long decisionTimeMs;
    
    /** 执行耗时（毫秒） */
    private Long executionTimeMs;

    /** 节点执行结果（用于日志） */
    private Map<String, Object> nodeResults = new ConcurrentHashMap<>();

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

    /** 将单个组件的风险信号保存到上下文 */
    public void addRiskSignal(String componentId, RiskSignal signal) {
        this.riskSignals.put(componentId, signal);
    }

    /** 线程安全地增加总风险分数（可为正或负） */
    public void addTotalScore(int score) {
        this.totalRiskScore.addAndGet(score);
    }

    /** 返回当前总风险分数 */
    public int getTotalRiskScore() {
        return this.totalRiskScore.get();
    }

    /**
     * 设置基础检测结果
     */
    public void setBaseCheckResult(String checkName, boolean passed) {
        this.baseCheckResults.put(checkName, passed);
    }

    /**
     * 获取基础检测结果
     */
    public Boolean getBaseCheckResult(String checkName) {
        return this.baseCheckResults.get(checkName);
    }
}
