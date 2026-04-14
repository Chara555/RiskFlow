package org.example.conversion;

import org.example.conversion.analyzer.FlowAnalyzer;
import org.example.conversion.analyzer.FlowAnalyzer.ExecutableNode;
import org.example.conversion.model.FlowDefinition;
import org.example.conversion.model.FlowEdge;
import org.example.conversion.model.FlowNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * FlowAnalyzer 单元测试
 * <p>
 * 测试 AST 分析核心方法 analyzeToAST，验证其正确生成可执行节点树的能力。
 */
class FlowAnalyzerTest {

    private FlowAnalyzer analyzer;

    @BeforeEach
    void setUp() {
        analyzer = new FlowAnalyzer();
    }

    @Test
    @DisplayName("单节点流程 - AST 应生成单个组件节点")
    void testSingleComponentNode() {
        FlowDefinition flow = new FlowDefinition();
        flow.setCode("test-flow");
        flow.setName("测试流程");

        FlowNode node = new FlowNode();
        node.setId("node_1");
        node.setType("process");
        node.setComponentId("singleComponent");

        flow.setNodes(Collections.singletonList(node));
        flow.setEdges(Collections.emptyList());

        ExecutableNode astRoot = analyzer.analyzeToAST(flow);
        String el = astRoot.toEL();

        assertNotNull(el);
        assertTrue(el.contains("singleComponent"));
    }

    @Test
    @DisplayName("串行流程 - AST 应生成 THEN 串行组")
    void testSequentialFlow() {
        FlowDefinition flow = new FlowDefinition();
        flow.setCode("sequential-flow");
        flow.setName("串行流程");

        FlowNode node1 = createNode("node_1", "componentA");
        FlowNode node2 = createNode("node_2", "componentB");
        FlowNode node3 = createNode("node_3", "componentC");
        FlowNode node4 = createNode("node_4", "componentD");

        FlowEdge edge1 = createEdge("e1", "node_1", "node_2");
        FlowEdge edge2 = createEdge("e2", "node_2", "node_3");
        FlowEdge edge3 = createEdge("e3", "node_3", "node_4");

        flow.setNodes(Arrays.asList(node1, node2, node3, node4));
        flow.setEdges(Arrays.asList(edge1, edge2, edge3));

        ExecutableNode astRoot = analyzer.analyzeToAST(flow);
        String el = astRoot.toEL();

        assertNotNull(el);
        assertTrue(el.contains("componentA"));
        assertTrue(el.contains("componentB"));
        assertTrue(el.contains("componentC"));
        assertTrue(el.contains("componentD"));
        // 串行流程应该生成 THEN 表达式
        assertTrue(el.contains("THEN"));
    }

    @Test
    @DisplayName("并行流程 - AST 应生成 WHEN 并行组")
    void testParallelFlow() {
        FlowDefinition flow = new FlowDefinition();
        flow.setCode("parallel-flow");
        flow.setName("并行流程");

        // 创建三个无依赖关系的节点（孤岛图）
        FlowNode node1 = createNode("node_1", "componentA");
        FlowNode node2 = createNode("node_2", "componentB");
        FlowNode node3 = createNode("node_3", "componentC");

        flow.setNodes(Arrays.asList(node1, node2, node3));
        flow.setEdges(Collections.emptyList());

        ExecutableNode astRoot = analyzer.analyzeToAST(flow);
        String el = astRoot.toEL();

        assertNotNull(el);
        assertTrue(el.contains("componentA"));
        assertTrue(el.contains("componentB"));
        assertTrue(el.contains("componentC"));
        // 孤岛图应该生成 WHEN 并行表达式
        assertTrue(el.contains("WHEN"));
    }

    @Test
    @DisplayName("复杂拓扑 - 菱形结构应生成 THEN-WHEN-THEN 嵌套")
    void testComplexTopology() {
        FlowDefinition flow = new FlowDefinition();
        flow.setCode("complex-flow");
        flow.setName("复杂流程");

        // 创建一个菱形结构:
        //     node_1
        //     /    \
        // node_2  node_3
        //     \    /
        //     node_4

        FlowNode node1 = createNode("node_1", "start");
        FlowNode node2 = createNode("node_2", "branchA");
        FlowNode node3 = createNode("node_3", "branchB");
        FlowNode node4 = createNode("node_4", "merge");

        FlowEdge edge1 = createEdge("e1", "node_1", "node_2");
        FlowEdge edge2 = createEdge("e2", "node_1", "node_3");
        FlowEdge edge3 = createEdge("e3", "node_2", "node_4");
        FlowEdge edge4 = createEdge("e4", "node_3", "node_4");

        flow.setNodes(Arrays.asList(node1, node2, node3, node4));
        flow.setEdges(Arrays.asList(edge1, edge2, edge3, edge4));

        ExecutableNode astRoot = analyzer.analyzeToAST(flow);
        String el = astRoot.toEL();

        assertNotNull(el);
        assertTrue(el.contains("start"));
        assertTrue(el.contains("branchA"));
        assertTrue(el.contains("branchB"));
        assertTrue(el.contains("merge"));
        // 菱形结构应该包含 WHEN（分支并行）
        assertTrue(el.contains("WHEN"));
    }

    @Test
    @DisplayName("空流程 - AST 应返回空串行组")
    void testEmptyFlow() {
        FlowDefinition flow = new FlowDefinition();
        flow.setCode("empty-flow");
        flow.setName("空流程");
        flow.setNodes(Collections.emptyList());
        flow.setEdges(Collections.emptyList());

        ExecutableNode astRoot = analyzer.analyzeToAST(flow);
        String el = astRoot.toEL();

        assertNotNull(el);
        assertTrue(el.isEmpty());
    }

    private FlowNode createNode(String id, String componentId) {
        FlowNode node = new FlowNode();
        node.setId(id);
        node.setType("process");
        node.setComponentId(componentId);
        return node;
    }

    private FlowEdge createEdge(String id, String source, String target) {
        FlowEdge edge = new FlowEdge();
        edge.setId(id);
        edge.setSourceNodeId(source);
        edge.setTargetNodeId(target);
        return edge;
    }
}
