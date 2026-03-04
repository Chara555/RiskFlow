package org.example.component.post;

import org.example.context.RiskFlowContext;
import com.yomahub.liteflow.annotation.LiteflowComponent;
import com.yomahub.liteflow.core.NodeComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 空操作组件 - 用于流程占位
 */
@LiteflowComponent("dummyNode")
public class DummyNodeComponent extends NodeComponent {

    private static final Logger log = LoggerFactory.getLogger(DummyNodeComponent.class);

    @Override
    public void process() {
        // 空操作，仅用于流程占位
        log.debug("[DummyNode] 执行空操作节点");
    }

    @Override
    public boolean isAccess() {
        return true;
    }
}
