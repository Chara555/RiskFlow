package org.example.component.base;

import org.example.context.RiskFlowContext;
import com.yomahub.liteflow.annotation.LiteflowComponent;
import com.yomahub.liteflow.core.NodeComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 上下文加载组件 - 初始化风险上下文
 */
@LiteflowComponent("loadContext")
public class LoadContextComponent extends NodeComponent {

    private static final Logger log = LoggerFactory.getLogger(LoadContextComponent.class);

    @Override
    public void process() {
        RiskFlowContext context = this.getContextBean(RiskFlowContext.class);
        
        // 如果没有事件ID，生成一个
        if (context.getEventId() == null || context.getEventId().isEmpty()) {
            context.setEventId(UUID.randomUUID().toString());
        }
        
        // 如果没有请求时间，设置当前时间
        if (context.getRequestTime() == null) {
            context.setRequestTime(LocalDateTime.now());
        }
        
        log.info("[LoadContext] 初始化上下文，eventId={}, eventType={}, userId={}", 
                context.getEventId(), context.getEventType(), context.getUserId());
    }

    @Override
    public boolean isAccess() {
        // 始终执行
        return true;
    }
}
