package org.example.component.base;

import org.example.context.RiskFlowContext;
import org.example.service.BlacklistService;
import com.yomahub.liteflow.annotation.LiteflowComponent;
import com.yomahub.liteflow.core.NodeComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * IP 黑名单检测组件
 */
@LiteflowComponent("ipBlacklistCheck")
public class IpBlacklistCheckComponent extends NodeComponent {

    private static final Logger log = LoggerFactory.getLogger(IpBlacklistCheckComponent.class);

    private static BlacklistService blacklistService;

    // 静态注入黑名单服务
    public static void setStaticBlacklistService(BlacklistService service) {
        blacklistService = service;
    }

    @Override
    public void process() {
        RiskFlowContext context = this.getContextBean(RiskFlowContext.class);

        String userIp = context.getUserIp();

        if (userIp == null || userIp.isEmpty()) {
            log.info("[IpBlacklistCheck] 用户IP为空，跳过检测");
            context.setBaseCheckResult("ipBlacklist", true);
            return;
        }

        // 检查黑名单
        boolean isBlacklisted = blacklistService != null && blacklistService.isIpBlacklisted(userIp);

        // 设置检测结果
        context.setBaseCheckResult("ipBlacklist", !isBlacklisted);

        // 如果命中黑名单，增加风险评分
        if (isBlacklisted) {
            context.addRuleScore("ipBlacklist", 50);
            log.warn("[IpBlacklistCheck] IP命中黑名单: {}", userIp);
        } else {
            log.info("[IpBlacklistCheck] IP检测通过: {}", userIp);
        }
    }

    @Override
    public boolean isAccess() {
        RiskFlowContext context = this.getContextBean(RiskFlowContext.class);
        // 有IP才执行
        return context.getUserIp() != null && !context.getUserIp().isEmpty();
    }
}
