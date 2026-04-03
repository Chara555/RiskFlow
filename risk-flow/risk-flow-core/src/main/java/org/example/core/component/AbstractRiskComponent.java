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
                //基于原信号克隆一个新信号，并把耗时塞进去
                signal = signal.toBuilder().latencyMs(latency).build();

                // 3. 将包含耗时的新信号写入上下文
                context.addRiskSignal(componentId, signal);

                // 4. 累加分数
                if (signal.getScoreContribution() != null && signal.getScoreContribution() != 0) {
                    context.addTotalScore(signal.getScoreContribution());
                }
            }
        } catch (Exception e) {
            long latency = RiskTimeUtils.nowMs() - startTime;
            log.error("[RiskFlow] Component {} execution failed. EventID: {}", componentId, context.getEventId(), e);
            RiskSignal errorSignal = RiskSignal.builder()
                    .scoreContribution(0)
                    .success(false)
                    .evidence("Component Error: " + e.getMessage())
                    .latencyMs(latency)
                    .build();
            context.addRiskSignal(componentId, errorSignal);
        }
    }

    @Override
    public boolean isContinueOnError() { return true; }

    protected abstract RiskSignal doEvaluate(RiskFlowContext context);
}