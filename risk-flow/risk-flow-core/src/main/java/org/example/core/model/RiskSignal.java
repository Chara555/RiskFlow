package org.example.core.model;

import lombok.Builder;
import lombok.Getter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Getter
@Builder(toBuilder = true)
public class RiskSignal {

    private String ruleName; // 规则名称

    @Builder.Default
    private Integer scoreContribution = 0; // 默认贡献 0 分，防止 NPE

    private String evidence; // 判决证据

    @Builder.Default
    private List<String> tags = Collections.emptyList(); // 默认空列表，防止 NPE

    private Long latencyMs; // 算子耗时

    @Builder.Default
    private boolean success = true; // 默认执行成功

    /**
     * 快速构建无风险信号（带耗时）
     */
    public static RiskSignal pass(long latency) {
        return RiskSignal.builder()
                .scoreContribution(0)
                .success(true)
                .latencyMs(latency)
                .evidence("Passed without risk")
                .build();
    }

    /**
     * 快速构建无风险信号（带规则名和证据）
     *
     * @param ruleName 规则名称
     * @param evidence 判决证据
     */
    public static RiskSignal pass(String ruleName, String evidence) {
        return RiskSignal.builder()
                .ruleName(ruleName)
                .scoreContribution(0)
                .success(true)
                .evidence(evidence)
                .build();
    }

    /**
     * 快速构建命中风险信号
     *
     * @param ruleName 规则名称
     * @param score    风险贡献分数
     * @param evidence 判决证据
     * @param tags     风险标签（可变参数）
     */
    public static RiskSignal hit(String ruleName, int score, String evidence, String... tags) {
        return RiskSignal.builder()
                .ruleName(ruleName)
                .scoreContribution(score)
                .success(true)
                .evidence(evidence)
                .tags(tags != null && tags.length > 0 ? Arrays.asList(tags) : Collections.emptyList())
                .build();
    }
}