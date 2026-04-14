package org.example.component.api;

import com.yomahub.liteflow.annotation.LiteflowComponent;
import org.example.core.component.BaseApiRiskComponent;
import org.example.core.model.ApiResult;
import org.example.core.service.ThreatIpCacheService;
import org.example.core.util.BizException;
import org.example.core.util.ErrorCode;
import org.example.service.RuleConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.Map;

/**
 * AbuseIPDB 全球威胁情报检测算子 (动态配置版)
 *
 * 【业务逻辑】：
 * 调用 AbuseIPDB API v2 实时查询目标 IP 的滥用历史与画像。
 * 判定策略：若命中官方白名单则直接放行；若为暗网节点(Tor)则直接标为极高风险(HIGH)；
 * 其余情况基于滥用置信度(Score)进行分级管控 (>=80为HIGH，>=20为MEDIUM)。
 * 支持底层 API 超时降级与原始报文无损留痕。
 *
 * 【动态配置指南 / 数据库 JSONB 格式】：
 * 对应数据库 code: ABUSE_IPDB_CHECK
 * 期望的 JSON 格式：
 * {
 * "apiKey": "YOUR_API_KEY",   // (必填) AbuseIPDB 官网申请的有效密钥
 * "failStrategy": "OPEN",     // (选填) API 超时降级策略：OPEN(旁路降级放行) 或 CLOSE(强控拦截)，默认 OPEN
 * "timeoutMs": 500,           // (选填) 接口请求最大容忍耗时(毫秒)，建议国外 API 设为 500+
 * "cacheHours": 24            // (选填) 请求结果双轨缓存的有效时长(小时)，默认 24
 * }
 */
@LiteflowComponent(id = "abuseIpdbCheck", name = "AbuseIPDB检测")
public class AbuseIpdbCheckComponent extends BaseApiRiskComponent {

    private final RestTemplate restTemplate;

    // 基础 URL (不包含动态参数)
    private static final String BASE_URL = "https://api.abuseipdb.com/api/v2/check";

    @Autowired
    public AbuseIpdbCheckComponent(RuleConfigService ruleConfigService, ThreatIpCacheService cacheService, RestTemplate restTemplate) {
        super(ruleConfigService, cacheService);
        this.restTemplate = restTemplate;
    }

    @Override
    protected ApiResult doRemoteCall(String ip, Map<String, Object> params) throws Exception {

        // 1. 从配置中提取 API Key
        String apiKey = (String) params.get("apiKey");
        if (apiKey == null || apiKey.isBlank()) {
            throw new BizException(ErrorCode.PARAM_MISSING, "apiKey");
        }

        // 2. 构造请求 Headers (严格按照文档要求)
        HttpHeaders headers = new HttpHeaders();
        headers.set("Key", apiKey);
        headers.set("Accept", "application/json"); // 必须加，否则限流时会返回 HTML
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        // 3. 构造带参数的 URL (使用 UriComponentsBuilder 自动处理 IP 的 URL 编码)
        String url = UriComponentsBuilder.fromHttpUrl(BASE_URL)
                .queryParam("ipAddress", ip)
                .queryParam("maxAgeInDays", "90") // 查过去 90 天的记录
                .queryParam("verbose", "")        // 开启详细模式，为了拿到完整的字段
                .toUriString();

        // 4. 发起 GET 请求
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
        Map<String, Object> body = response.getBody();

        if (body == null || !body.containsKey("data")) {
            return new ApiResult("SAFE", Map.of("error", "API 返回数据为空"));
        }

        // 5. 解析核心业务数据
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) body.get("data");

        // 提取黄金字段（防御性取值，避免 NPE）
        int score = ((Number) data.getOrDefault("abuseConfidenceScore", 0)).intValue();
        boolean isTor = Boolean.TRUE.equals(data.get("isTor"));
        boolean isWhitelisted = Boolean.TRUE.equals(data.get("isWhitelisted"));
        String usageType = (String) data.getOrDefault("usageType", "Unknown");
        String countryCode = (String) data.getOrDefault("countryCode", "Unknown");

        // 6. 核心风控判定逻辑
        String level = "SAFE";

        if (!isWhitelisted) { // 如果在白名单里，直接放过
            if (isTor) {
                // 如果是暗网节点，无视分数，直接极高风险！
                level = "HIGH";
            } else if (score >= 80) {
                level = "HIGH";
            } else if (score >= 20) {
                level = "MEDIUM";
            }
        }

        // 7. 封装无损留痕数据 (这些会存进数据库的 details 里)
        Map<String, Object> rawData = new HashMap<>();
        rawData.put("score", score);
        rawData.put("isTor", isTor);
        rawData.put("usageType", usageType);
        rawData.put("countryCode", countryCode);
        rawData.put("isp", data.getOrDefault("isp", "Unknown"));
        rawData.put("totalReports", data.getOrDefault("totalReports", 0));

        return new ApiResult(level, rawData);
    }

    @Override
    protected String getRuleName() {
        return "ABUSE_IPDB_CHECK"; // 必须与 rule_config 表里的 code 一致
    }
}
