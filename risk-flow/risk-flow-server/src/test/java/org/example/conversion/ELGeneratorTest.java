package org.example.conversion;

import org.example.conversion.generator.ELGenerator;
import org.example.conversion.model.FlowNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ELGenerator 单元测试
 */
class ELGeneratorTest {

    private ELGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new ELGenerator();
    }

    @Test
    @DisplayName("单组件 - 不使用 THEN 包裹")
    void testSingleComponent() {
        List<String> components = Collections.singletonList("singleComponent");
        String el = generator.generate(components);

        assertEquals("singleComponent;", el);
    }

    @Test
    @DisplayName("多组件串行 - 使用 THEN 包裹")
    void testMultipleComponents() {
        List<String> components = Arrays.asList("loadContext", "ipCheck", "ruleExecute", "decisionJudge");
        String el = generator.generate(components);

        assertEquals("THEN(loadContext, ipCheck, ruleExecute, decisionJudge);", el);
    }

    @Test
    @DisplayName("两个组件串行")
    void testTwoComponents() {
        List<String> components = Arrays.asList("start", "end");
        String el = generator.generate(components);

        assertEquals("THEN(start, end);", el);
    }

    @Test
    @DisplayName("生成带链名称的 EL")
    void testGenerateWithChain() {
        List<String> components = Arrays.asList("a", "b", "c");
        String el = generator.generateWithChain("myChain", components);

        assertTrue(el.contains("chain name=\"myChain\""));
        assertTrue(el.contains("THEN(a, b, c)"));
    }

    @Test
    @DisplayName("生成完整 XML 规则文件")
    void testGenerateXml() {
        List<String> components = Arrays.asList("ipCheck", "ruleExecute");
        String xml = generator.generateXml("login-flow", components);

        assertTrue(xml.startsWith("<?xml version=\"1.0\""));
        assertTrue(xml.contains("<flow>"));
        assertTrue(xml.contains("<chain name=\"login-flow\">"));
        assertTrue(xml.contains("THEN(ipCheck, ruleExecute)"));
        assertTrue(xml.contains("</chain>"));
        assertTrue(xml.contains("</flow>"));
    }

    @Test
    @DisplayName("空组件列表 - 抛出异常")
    void testEmptyComponents() {
        assertThrows(IllegalArgumentException.class, () -> {
            generator.generate(Collections.emptyList());
        });
    }

    @Test
    @DisplayName("null 组件列表 - 抛出异常")
    void testNullComponents() {
        assertThrows(IllegalArgumentException.class, () -> {
            generator.generate(null);
        });
    }

    @Test
    @DisplayName("从节点列表生成 EL")
    void testGenerateFromNodes() {
        FlowNode node1 = new FlowNode();
        node1.setId("n1");
        node1.setComponentId("componentA");

        FlowNode node2 = new FlowNode();
        node2.setId("n2");
        node2.setComponentId("componentB");

        List<FlowNode> nodes = Arrays.asList(node1, node2);
        String el = generator.generateFromNodes(nodes);

        assertEquals("THEN(componentA, componentB);", el);
    }
}
