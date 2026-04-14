package org.example.component.api;

import org.example.core.model.ApiResult;
import org.example.core.service.ThreatIpCacheService;
import org.example.service.RuleConfigService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * AbuseIpdbCheckComponent 单元测试
 *
 * <p>测试策略：mock RestTemplate，不真调 API，只测 doRemoteCall 的解析与判定逻辑。
 * 父类 BaseApiRiskComponent 的缓存/超时/降级逻辑不在本测试范围。
 */
@ExtendWith(MockitoExtension.class)
class AbuseIpdbCheckComponentTest {

    @Mock
    private RuleConfigService ruleConfigService;

    @Mock
    private ThreatIpCacheService cacheService;

    @Mock
    private RestTemplate restTemplate;

    private AbuseIpdbCheckComponent component;

    @BeforeEach
    void setUp() {
        component = new AbuseIpdbCheckComponent(ruleConfigService, cacheService, restTemplate);
    }

    // ==================== 辅助方法：构造 AbuseIPDB API 响应 ====================

    private Map<String, Object> buildApiResponse(int score, boolean isTor, boolean isWhitelisted,
                                                  String usageType, String countryCode) {
        return Map.of(
                "data", Map.of(
                        "abuseConfidenceScore", score,
                        "isTor", isTor,
                        "isWhitelisted", isWhitelisted,
                        "usageType", usageType,
                        "countryCode", countryCode,
                        "isp", "Test ISP",
                        "totalReports", 42
                )
        );
    }

    private void mockRestTemplateReturn(Map<String, Object> body) {
        ResponseEntity<Map> response = new ResponseEntity<>(body, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(Class.class)))
                .thenReturn(response);
    }

    // ==================== 测试用例 ====================

    @Test
    @DisplayName("安全 IP (score=0, 非Tor, 非白名单) → SAFE")
    void testSafeIp() throws Exception {
        mockRestTemplateReturn(buildApiResponse(0, false, false, "Residential", "CN"));

        ApiResult result = component.doRemoteCall("1.2.3.4", Map.of("apiKey", "test-key"));

        assertEquals("SAFE", result.level());
        assertEquals(0, result.rawData().get("score"));
    }

    @Test
    @DisplayName("白名单 IP → 无视分数，直接 SAFE")
    void testWhitelistedIp() throws Exception {
        // 即使 score=100，白名单也应该放行
        mockRestTemplateReturn(buildApiResponse(100, false, true, "Data Center", "US"));

        ApiResult result = component.doRemoteCall("8.8.8.8", Map.of("apiKey", "test-key"));

        assertEquals("SAFE", result.level());
    }

    @Test
    @DisplayName("Tor 暗网出口 → 无视分数，直接 HIGH")
    void testTorExitNode() throws Exception {
        // score 只有 5，但 isTor=true，应该 HIGH
        mockRestTemplateReturn(buildApiResponse(5, true, false, "Tor Exit", "DE"));

        ApiResult result = component.doRemoteCall("1.2.3.4", Map.of("apiKey", "test-key"));

        assertEquals("HIGH", result.level());
        assertEquals(true, result.rawData().get("isTor"));
    }

    @Test
    @DisplayName("滥用置信度 >= 80 → HIGH")
    void testHighScoreIp() throws Exception {
        mockRestTemplateReturn(buildApiResponse(85, false, false, "Data Center", "US"));

        ApiResult result = component.doRemoteCall("1.2.3.4", Map.of("apiKey", "test-key"));

        assertEquals("HIGH", result.level());
        assertEquals(85, result.rawData().get("score"));
    }

    @Test
    @DisplayName("滥用置信度 20~79 → MEDIUM")
    void testMediumScoreIp() throws Exception {
        mockRestTemplateReturn(buildApiResponse(50, false, false, "Commercial", "RU"));

        ApiResult result = component.doRemoteCall("1.2.3.4", Map.of("apiKey", "test-key"));

        assertEquals("MEDIUM", result.level());
    }

    @Test
    @DisplayName("滥用置信度 < 20 → SAFE")
    void testLowScoreIp() throws Exception {
        mockRestTemplateReturn(buildApiResponse(10, false, false, "Residential", "JP"));

        ApiResult result = component.doRemoteCall("1.2.3.4", Map.of("apiKey", "test-key"));

        assertEquals("SAFE", result.level());
    }

    @Test
    @DisplayName("API 返回空 body → SAFE 降级")
    void testEmptyResponseBody() throws Exception {
        ResponseEntity<Map> response = new ResponseEntity<>(null, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(Class.class)))
                .thenReturn(response);

        ApiResult result = component.doRemoteCall("1.2.3.4", Map.of("apiKey", "test-key"));

        assertEquals("SAFE", result.level());
    }

    @Test
    @DisplayName("API 返回无 data 字段 → SAFE 降级")
    void testNoDataField() throws Exception {
        ResponseEntity<Map> response = new ResponseEntity<>(Map.of("errors", "something"), HttpStatus.OK);
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(Class.class)))
                .thenReturn(response);

        ApiResult result = component.doRemoteCall("1.2.3.4", Map.of("apiKey", "test-key"));

        assertEquals("SAFE", result.level());
    }

    @Test
    @DisplayName("缺少 apiKey 配置 → 抛 IllegalArgumentException")
    void testMissingApiKey() {
        assertThrows(IllegalArgumentException.class, () ->
                component.doRemoteCall("1.2.3.4", Map.of()));
    }

    @Test
    @DisplayName("apiKey 为空白 → 抛 IllegalArgumentException")
    void testBlankApiKey() {
        assertThrows(IllegalArgumentException.class, () ->
                component.doRemoteCall("1.2.3.4", Map.of("apiKey", "   ")));
    }

    @Test
    @DisplayName("无损留痕：rawData 包含完整字段")
    void testRawDataFields() throws Exception {
        mockRestTemplateReturn(buildApiResponse(75, false, false, "Data Center", "US"));

        ApiResult result = component.doRemoteCall("1.2.3.4", Map.of("apiKey", "test-key"));

        Map<String, Object> rawData = result.rawData();
        assertEquals(75, rawData.get("score"));
        assertEquals(false, rawData.get("isTor"));
        assertEquals("Data Center", rawData.get("usageType"));
        assertEquals("US", rawData.get("countryCode"));
        assertEquals("Test ISP", rawData.get("isp"));
        assertEquals(42, rawData.get("totalReports"));
    }

    @Test
    @DisplayName("getRuleName 返回正确的规则代码")
    void testGetRuleName() {
        assertEquals("ABUSE_IPDB_CHECK", component.getRuleName());
    }
}
