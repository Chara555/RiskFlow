package org.example.autoconfigure;

import com.yomahub.liteflow.core.FlowExecutor;
import org.example.context.RiskFlowContext;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

/**
 * RiskFlow 自动配置
 * 
 * 注意：LiteFlow 会自动配置 FlowExecutor
 * 这里只需要配置业务相关的 Bean
 */
@AutoConfiguration
@ConditionalOnProperty(prefix = "riskflow", name = "enabled", havingValue = "true", matchIfMissing = true)
public class RiskFlowAutoConfiguration {

    /**
     * 风险上下文类型
     * LiteFlow 需要知道上下文类型
     */
    @Bean
    public Class<RiskFlowContext> riskFlowContextClass() {
        return RiskFlowContext.class;
    }
}
