package org.example.conversion;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.conversion.analyzer.FlowAnalyzer;
import org.example.conversion.engine.FlowConversionEngine;
import org.example.conversion.generator.ELGenerator;
import org.example.conversion.model.ConversionResult;
import org.example.conversion.validator.FlowValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * FlowConversionEngine 集成测试
 */
class FlowConversionEngineTest {

    private FlowConversionEngine engine;

    @BeforeEach
    void setUp() {
        FlowValidator validator = new FlowValidator();
        FlowAnalyzer analyzer = new FlowAnalyzer();
        ELGenerator generator = new ELGenerator();
        ObjectMapper objectMapper = new ObjectMapper();

        engine = new FlowConversionEngine(validator, analyzer, generator, objectMapper);
    }

    @Test
    @DisplayName("完整转换流程 - 串行流程 JSON → EL")
    void testConvertSequentialFlow() {
        String json = """
            {
                "code": "login-flow",
                "name": "登录风控流程",
                "description": "检测登录风险",
                "nodes": [
                    {"id": "node_1", "type": "process", "componentId": "loadContext", "label": "加载上下文"},
                    {"id": "node_2", "type": "process", "componentId": "ipCheck", "label": "IP检测"},
                    {"id": "node_3", "type": "process", "componentId": "ruleExecute", "label": "规则执行"},
                    {"id": "node_4", "type": "process", "componentId": "decisionJudge", "label": "决策判断"}
                ],
                "edges": [
                    {"id": "e1", "sourceNodeId": "node_1", "targetNodeId": "node_2"},
                    {"id": "e2", "sourceNodeId": "node_2", "targetNodeId": "node_3"},
                    {"id": "e3", "sourceNodeId": "node_3", "targetNodeId": "node_4"}
                ]
            }
            """;

        ConversionResult result = engine.convert(json);

        assertTrue(result.isSuccess());
        assertEquals("login-flow", result.getChainName());
        assertEquals("THEN(loadContext, ipCheck, ruleExecute, decisionJudge);", result.getElExpression());
        assertNotNull(result.getXmlContent());
        assertTrue(result.getXmlContent().contains("<chain name=\"login-flow\">"));
    }

    @Test
    @DisplayName("单节点流程转换")
    void testConvertSingleComponentNode() {
        String json = """
            {
                "code": "simple-flow",
                "name": "简单流程",
                "nodes": [
                    {"id": "node_1", "type": "process", "componentId": "singleComponent", "label": "单一组件"}
                ],
                "edges": []
            }
            """;

        ConversionResult result = engine.convert(json);

        assertTrue(result.isSuccess());
        assertEquals("singleComponent;", result.getElExpression());
    }

    @Test
    @DisplayName("无效 JSON - 转换失败")
    void testConvertInvalidJson() {
        String invalidJson = "{ invalid json }";

        ConversionResult result = engine.convert(invalidJson);

        assertFalse(result.isSuccess());
        assertNotNull(result.getErrors());
        assertFalse(result.getErrors().isEmpty());
        assertTrue(result.getErrors().get(0).contains("JSON 解析失败"));
    }

    @Test
    @DisplayName("验证失败 - 节点 componentId 为空")
    void testConvertMissingComponentId() {
        String json = """
            {
                "code": "invalid-flow",
                "name": "无效流程",
                "nodes": [
                    {"id": "node_1", "type": "process", "componentId": "", "label": "空组件"}
                ],
                "edges": []
            }
            """;

        ConversionResult result = engine.convert(json);

        assertFalse(result.isSuccess());
        assertNotNull(result.getErrors());
        assertTrue(result.getErrors().stream()
                .anyMatch(e -> e.contains("componentId 为空")));
    }

    @Test
    @DisplayName("验证失败 - 存在环")
    void testConvertCyclicFlow() {
        String json = """
            {
                "code": "cyclic-flow",
                "name": "有环流程",
                "nodes": [
                    {"id": "node_1", "type": "process", "componentId": "a", "label": "A"},
                    {"id": "node_2", "type": "process", "componentId": "b", "label": "B"},
                    {"id": "node_3", "type": "process", "componentId": "c", "label": "C"}
                ],
                "edges": [
                    {"id": "e1", "sourceNodeId": "node_1", "targetNodeId": "node_2"},
                    {"id": "e2", "sourceNodeId": "node_2", "targetNodeId": "node_3"},
                    {"id": "e3", "sourceNodeId": "node_3", "targetNodeId": "node_1"}
                ]
            }
            """;

        ConversionResult result = engine.convert(json);

        assertFalse(result.isSuccess());
        assertTrue(result.getErrors().stream()
                .anyMatch(e -> e.contains("环")));
    }

    @Test
    @DisplayName("生成完整 XML 规则文件")
    void testGenerateFullXml() {
        String json = """
            {
                "code": "payment-risk",
                "name": "支付风控",
                "nodes": [
                    {"id": "n1", "type": "process", "componentId": "loadOrder", "label": "加载订单"},
                    {"id": "n2", "type": "process", "componentId": "checkRisk", "label": "风险检测"},
                    {"id": "n3", "type": "process", "componentId": "makeDecision", "label": "决策"}
                ],
                "edges": [
                    {"id": "e1", "sourceNodeId": "n1", "targetNodeId": "n2"},
                    {"id": "e2", "sourceNodeId": "n2", "targetNodeId": "n3"}
                ]
            }
            """;

        ConversionResult result = engine.convert(json);

        assertTrue(result.isSuccess());

        String xml = result.getXmlContent();
        System.out.println("生成的 XML:\n" + xml);

        assertTrue(xml.startsWith("<?xml version=\"1.0\""));
        assertTrue(xml.contains("<flow>"));
        assertTrue(xml.contains("<chain name=\"payment-risk\">"));
        assertTrue(xml.contains("THEN(loadOrder, checkRisk, makeDecision)"));
        assertTrue(xml.contains("</chain>"));
        assertTrue(xml.contains("</flow>"));
    }
}
