package org.example.core.model;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 风控核心信号模型 (JSON 驱动制基石)
 */
@Getter
@Builder(toBuilder = true)
public class RiskSignal {
    /*
     风险等级常量定义
     * 1. 局部定性：以下常量仅代表【单一风控节点】对某一具体特征（如IP、设备）的风险定性。
     * 2. 非最终决策：它绝不代表最终的放行/拒绝决策！最终决策由大脑节点汇总后，产出 DecisionResult。
     * 3. 示例：当指纹节点产出 HIGH 时，并不意味着拦截。如果其他节点都是 NONE，可能最终判决 ACCEPT 或 REVIEW。
     */
    /** * 无风险：特征正常，完全安全。
     * 动作参考：对最终决策无负面影响。
     */
    public static final String LEVEL_NONE = "NONE";
    /** * 低风险：有轻微偏离正常模式的异常 (如：非惯用时间登录)。
     * 动作参考：单点不足以构成阻断，需大量叠加。
     */
    public static final String LEVEL_LOW = "LOW";
    /** * 中风险：存在明显可疑特征 (如：短时间内密码输入错误频繁)。
     * 动作参考：通常需要与其他中/低风险叠加，或触发验证码挑战 (CHALLENGE)。
     */
    public static final String LEVEL_MEDIUM = "MEDIUM";
    /** * 高风险：高度疑似黑产或欺诈 (如：异地新设备 + 代理IP)。
     * 动作参考：极大概率触发人工审核 (REVIEW) 或拦截 (REJECT)。
     */
    public static final String LEVEL_HIGH = "HIGH";

    public static final String LEVEL_CRITICAL = "CRITICAL";

    // ==================== Level 1: 强基准信息 ====================

    /** 产出此信号的规则/节点名 */
    private String ruleName;

    /** 风险等级：默认无风险 */
    @Builder.Default
    private String riskLevel = LEVEL_NONE;

    /** 置信度 (0.0 ~ 1.0)，表示判断的准确率 */
    @Builder.Default
    private double confidence = 1.0;

    /** 风险判定摘要 - 英文版（默认，给全球开发者/机器/LLM读） */
    private String evidence;

    /** 风险判定摘要 - 中文版（给国内用户/前端展示） */
    private String evidenceZh;

    /** 信号产生时间 (ISO-8601 格式，尾部带 Z 表示严格 UTC) */
    @Builder.Default
    private String timestamp = Instant.now().toString();

    // ==================== Level 2: 动态扩展信息 ====================

    /** 结构化的具体上下文证据（给规则引擎或大模型提取特征用） */
    @Builder.Default
    private Map<String, Object> details = new HashMap<>();

    /** 风险标签（用于下游聚合分析） */
    @Builder.Default
    private List<String> tags = Collections.emptyList();

    // ==================== 元数据 ====================

    /** 算子执行耗时 */
    private Long latencyMs;

    /** 默认执行成功 */
    @Builder.Default
    private boolean success = true;

    // ==================== 便捷工厂方法 ====================
    /**
     * 快速构建"无风险"信号 (双语版)
     * 使用场景：节点检查完毕，结论为安全，需要记录理由。
     *
     * @param ruleName  规则名称
     * @param evidence  英文证据描述
     * @param evidenceZh 中文证据描述
     */
    public static RiskSignal pass(String ruleName, String evidence, String evidenceZh) {
        return RiskSignal.builder()
                .ruleName(ruleName)
                .riskLevel(LEVEL_NONE)
                .confidence(1.0)
                .evidence(evidence)
                .evidenceZh(evidenceZh)
                .success(true)
                .timestamp(java.time.Instant.now().toString())
                .build();
    }

    /**
     * 快速构建命中风险信号
     *
     * @param ruleName  规则名称
     * @param riskLevel 风险等级（如 LEVEL_HIGH）
     * @param evidence  英文判决摘要
     * @param evidenceZh 中文判决摘要
     * @param details   结构化细节数据（可为 null）
     * @param tags      风险标签（可变参数）
     */
    public static RiskSignal hit(String ruleName, String riskLevel, String evidence, String evidenceZh,
                                  Map<String, Object> details, String... tags) {
        return RiskSignal.builder()
                .ruleName(ruleName)
                .riskLevel(riskLevel)
                .success(true)
                .evidence(evidence)
                .evidenceZh(evidenceZh)
                .details(details != null ? details : new HashMap<>())
                .tags(tags != null && tags.length > 0 ? Arrays.asList(tags) : Collections.emptyList())
                .build();
    }

    /**
     * 快速构建命中风险信号 (仅英文版，兼容旧调用)
     */
    public static RiskSignal hit(String ruleName, String riskLevel, String evidence,
                                  Map<String, Object> details, String... tags) {
        return hit(ruleName, riskLevel, evidence, null, details, tags);
    }
}