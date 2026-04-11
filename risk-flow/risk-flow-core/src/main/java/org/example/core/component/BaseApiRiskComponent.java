package org.example.core.component;

import org.example.context.RiskFlowContext;
import org.example.core.model.ApiResult;
import org.example.core.model.RiskSignal;
import org.example.core.service.ThreatIpCacheService;
import org.example.service.RuleConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.*;

/**
 * 外部 API 算子抽象基座 (V2.0 增强版)
 * 特性：异步执行、双轨缓存、Fail-Open/Close 策略、原始数据无损留痕
 */
public abstract class BaseApiRiskComponent extends AbstractRiskComponent {

    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    protected final RuleConfigService ruleConfigService;

    protected final ThreatIpCacheService cacheService;

    /**
     * 构造器注入：子类通过 super() 传递依赖，IDE 不会报自动装配警告
     */
    protected BaseApiRiskComponent(RuleConfigService ruleConfigService, ThreatIpCacheService cacheService) {
        this.ruleConfigService = ruleConfigService;
        this.cacheService = cacheService;
    }

    /**
     * 利用 Java 21 虚拟线程：处理大量外部 IO 等待的最佳选择
     */
    private static final ExecutorService API_EXECUTOR = Executors.newVirtualThreadPerTaskExecutor();

    @Override
    public boolean isAccess() {
        RuleConfigService.ComponentConfig config = ruleConfigService.getConfig(getRuleName());
        return config != null && config.isEnabled();
    }

    @Override
    protected RiskSignal doEvaluate(RiskFlowContext context) {
        String ip = context.getUserIp();
        String ruleName = getRuleName();

        // 1. 获取动态配置参数
        RuleConfigService.ComponentConfig config = ruleConfigService.getConfig(ruleName);
        if (config == null) {
            log.warn("[{}] 规则配置缺失，默认放行. IP: {}", ruleName, ip);
            return RiskSignal.pass(ruleName, "Rule config missing", "缺少规则配置，默认放行");
        }
        String strategy = config.getParam("failStrategy", "OPEN"); // 默认旁路模式
        int timeoutMs = config.getIntParam("timeoutMs", 300);
        int cacheHours = config.getIntParam("cacheHours", 24);

        // 2. 查双轨缓存 (防止重复计费/重复耗时)
        // 注意：缓存里通常只存 level 字符串以节省空间，若需存全量数据可在此扩展
        String cachedLevel = cacheService.getRiskLevel(ip);
        if (cachedLevel != null) {
            log.debug("[{}] 命中情报缓存: {} -> {}", ruleName, ip, cachedLevel);
            return buildSignal(ip, new ApiResult(cachedLevel, Map.of("source", "CACHE")), "CACHE");
        }

        // 3. 执行外部异步调用 (带严格超时保护)
        try {
            ApiResult apiResult = CompletableFuture.supplyAsync(() -> {
                try {
                    // 调用子类实现的具体厂商逻辑
                    return doRemoteCall(ip, config.getParams());
                } catch (Exception e) {
                    throw new CompletionException(e);
                }
            }, API_EXECUTOR).get(timeoutMs, TimeUnit.MILLISECONDS);

            // 4. 异步更新缓存 (不阻塞主流程响应)
            API_EXECUTOR.execute(() -> cacheService.setRiskLevel(ip, apiResult.level(), cacheHours, TimeUnit.HOURS));

            return buildSignal(ip, apiResult, "REALTIME");

        } catch (TimeoutException e) {
            // ============================================================
            // 核心逻辑：根据用户配置的策略，决定超时后是“放行”还是“拦截”
            // ============================================================
            if ("OPEN".equalsIgnoreCase(strategy)) {
                log.warn("[{}] API调用超时({}ms) -> 触发【旁路模式】放行, IP: {}", ruleName, timeoutMs, ip);
                return RiskSignal.pass(ruleName, "API_TIMEOUT_FAIL_OPEN", "API响应超时，降级放行");
            } else {
                log.error("[{}] API调用超时({}ms) -> 触发【强控模式】拦截, IP: {}", ruleName, timeoutMs, ip);
                return RiskSignal.hit(
                        ruleName,
                        RiskSignal.LEVEL_HIGH,
                        "API_TIMEOUT_FAIL_CLOSE",
                        "安全情报缺失风险(强控)",
                        Map.of("ip", ip, "timeoutMs", timeoutMs, "strategy", "CLOSE"),
                        "SYSTEM_PROTECTION"
                );
            }

        } catch (Exception e) {
            log.error("[{}] API执行发生异常, 触发自动降级放行. IP: {}", ruleName, ip, e);
            return RiskSignal.pass(ruleName, "API_ERROR_BYPASS", "API服务故障降级");
        }
    }

    /**
     * 信号构建工厂：将 ApiResult 转换为风控系统可识别的 RiskSignal
     */
    private RiskSignal buildSignal(String ip, ApiResult result, String source) {
        String level = result.level();

        // 如果是安全等级，直接 Pass
        if ("SAFE".equalsIgnoreCase(level) || RiskSignal.LEVEL_NONE.equalsIgnoreCase(level)) {
            return RiskSignal.pass(getRuleName(), "API_CLEAR", "外部情报确认安全");
        }

        // 如果是危险等级，Hit 并携带原始报文数据
        return RiskSignal.hit(
                getRuleName(),
                level,
                "THREAT_INTEL_HIT",
                "命中外部威胁情报库",
                Map.of(
                        "ip", ip,
                        "dataSource", source,
                        "apiDetails", result.rawData() // 【关键】：原始数据无损留痕
                ),
                "API_INTELLIGENCE", source
        );
    }

    /**
     * 子类必须实现：具体的厂商 API 调用与解析逻辑
     */
    protected abstract ApiResult doRemoteCall(String ip, Map<String, Object> params) throws Exception;

    /**
     * 子类必须实现：对应数据库 rule_config 表中的 code
     */
    protected abstract String getRuleName();
}