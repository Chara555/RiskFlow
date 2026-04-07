package org.example.core.component;

import com.yomahub.liteflow.core.NodeComponent;
import org.example.context.RiskFlowContext;
import org.example.core.model.RiskSignal;
import org.example.core.util.RiskTimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractRiskComponent extends NodeComponent {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public final void process() throws Exception {
        RiskFlowContext context = this.getContextBean(RiskFlowContext.class);
        String componentId = this.getNodeId();
        long startTime = RiskTimeUtils.nowMs();

        try {
            // 1. 获取子类生成的信号（此时 latencyMs 还是空的）
            RiskSignal signal = doEvaluate(context);

            long latency = RiskTimeUtils.nowMs() - startTime;

            if (signal != null) {
                // 基于原信号克隆，补充耗时信息
                signal = signal.toBuilder().latencyMs(latency).build();
                // 将信号写入上下文（不再累加分数，决策权交给大脑节点）
                context.addRiskSignal(componentId, signal);
            }
        } catch (Exception e) {
            long latency = RiskTimeUtils.nowMs() - startTime;
            log.error("[RiskFlow] Component {} execution failed. EventID: {}", componentId, context.getEventId(), e);
            
            // 发生异常时，不能简单视为安全。标记为 NONE 但打上强异常标签，由大脑节点决定是否拦截/审核。
            RiskSignal errorSignal = RiskSignal.builder()
                    .ruleName(componentId)
                    .riskLevel(RiskSignal.LEVEL_NONE)
                    .success(false)
                    .evidence("Component Execution Error: " + e.getMessage())
                    .evidenceZh("组件执行异常: " + e.getMessage())
                    .tags(java.util.Collections.singletonList("SYSTEM_ERROR"))
                    .latencyMs(latency)
                    .build();
            context.addRiskSignal(componentId, errorSignal);
        }
    }

    @Override
    public boolean isContinueOnError() { return true; }

    protected abstract RiskSignal doEvaluate(RiskFlowContext context);
}