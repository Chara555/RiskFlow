package org.example.conversion.analyzer;

import org.example.conversion.model.FlowDefinition;
import org.example.conversion.model.FlowEdge;
import org.example.conversion.model.FlowNode;
import org.example.core.util.BizException;
import org.example.core.util.ErrorCode;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 流程分析器 (工业级高并发重构版)
 *
 * 职责：
 * 分析 FlowDefinition 的拓扑结构，通过分层拓扑排序识别图中的串行与并行结构，
 * 为 ELGenerator 提供支持 THEN/WHEN 语义的抽象语法树（AST），或兼容的平铺列表。
 *
 * 核心优化：
 * 1. 消除冗余：analyze 直接复用核心图解析逻辑 (DRY 原则)。
 * 2. 防御性编程：引入成环检测，拦截由于配置错误导致的死循环图 (Silent Failure 防御)。
 * 3. 降维打击：使用分层 Kahn 算法，将有向无环图 (DAG) 转换为具备并发语义的执行树。
 */
@Component
public class FlowAnalyzer {

    // =================================================================================
    // 内部 AST (抽象语法树) 模型定义
    // 这些类用于将网状的图结构，表达为 LiteFlow 能够理解的树状嵌套结构
    // =================================================================================

    public interface ExecutableNode {
        /** 生成 LiteFlow 表达式片段 */
        String toEL();
    }

    /** 叶子节点：具体的业务算子 */
    public static class ComponentNode implements ExecutableNode {
        private final String componentId;
        public ComponentNode(String componentId) { this.componentId = componentId; }
        @Override
        public String toEL() { return componentId; }
    }

    /** 复合节点基类：执行组 */
    public static abstract class ExecutionGroup implements ExecutableNode {
        protected List<ExecutableNode> children = new ArrayList<>();
        public void add(ExecutableNode node) { children.add(node); }
        public boolean isEmpty() { return children.isEmpty(); }
    }

    /** 串行执行组 (对应 LiteFlow 的 THEN) */
    public static class SerialGroup extends ExecutionGroup {
        @Override
        public String toEL() {
            if (children.isEmpty()) return "";
            if (children.size() == 1) return children.get(0).toEL();
            String inner = children.stream().map(ExecutableNode::toEL).collect(Collectors.joining(", "));
            return "THEN(" + inner + ")";
        }
    }

    /** 并行执行组 (对应 LiteFlow 的 WHEN) */
    public static class ParallelGroup extends ExecutionGroup {
        @Override
        public String toEL() {
            if (children.isEmpty()) return "";
            if (children.size() == 1) return children.get(0).toEL();
            String inner = children.stream().map(ExecutableNode::toEL).collect(Collectors.joining(", "));
            return "WHEN(" + inner + ")";
        }
    }

    // =================================================================================
    // 核心解析逻辑
    // =================================================================================

    /**
     * 【全新架构】分析图结构，生成具备并发语义的 AST 语法树
     * 示例输出：THEN(nodeA, WHEN(nodeB, nodeC), nodeD)
     *
     * @param flow 流程定义
     * @return 代表整个流程执行入口的 AST 根节点
     */
    public ExecutableNode analyzeToAST(FlowDefinition flow) {
        List<FlowNode> nodes = flow.getNodes();
        List<FlowEdge> edges = flow.getEdges();

        // 根容器始终是一个 THEN 串行组
        SerialGroup root = new SerialGroup();

        if (nodes == null || nodes.isEmpty()) {
            return root;
        }

        // 孤岛图处理 (没有连线，直接全量并发)
        if (edges == null || edges.isEmpty()) {
            ParallelGroup parallel = new ParallelGroup();
            nodes.stream()
                    .map(n -> new ComponentNode(n.getComponentId()))
                    .forEach(parallel::add);
            root.add(parallel);
            return root;
        }

        Map<String, FlowNode> nodeMap = nodes.stream().collect(Collectors.toMap(FlowNode::getId, n -> n));
        Map<String, List<String>> adjacency = new HashMap<>();
        Map<String, Integer> inDegree = new HashMap<>();

        for (FlowNode node : nodes) {
            adjacency.put(node.getId(), new ArrayList<>());
            inDegree.put(node.getId(), 0);
        }

        for (FlowEdge edge : edges) {
            adjacency.get(edge.getSourceNodeId()).add(edge.getTargetNodeId());
            inDegree.merge(edge.getTargetNodeId(), 1, Integer::sum);
        }

        Queue<String> queue = new LinkedList<>();
        for (Map.Entry<String, Integer> entry : inDegree.entrySet()) {
            if (entry.getValue() == 0) queue.offer(entry.getKey());
        }

        int processedCount = 0;

        // 分层 Kahn 算法 (Level-Order BFS)
        while (!queue.isEmpty()) {
            int levelSize = queue.size(); // 当前批次中，互不依赖、可完全并行的节点数量

            if (levelSize == 1) {
                // 如果本层只有一个节点，直接作为串行节点挂载
                String nodeId = queue.poll();
                root.add(new ComponentNode(nodeMap.get(nodeId).getComponentId()));
                decrementDegrees(nodeId, adjacency, inDegree, queue);
                processedCount++;
            } else {
                // 如果本层有多个节点，打包成一个 WHEN 并发组
                ParallelGroup parallelGroup = new ParallelGroup();
                for (int i = 0; i < levelSize; i++) {
                    String nodeId = queue.poll();
                    parallelGroup.add(new ComponentNode(nodeMap.get(nodeId).getComponentId()));
                    decrementDegrees(nodeId, adjacency, inDegree, queue);
                    processedCount++;
                }
                root.add(parallelGroup); // 将并发组作为一个整体挂载到主线上
            }
        }

        // 【防御性编程】循环依赖检测
        if (processedCount != nodes.size()) {
            throw new BizException(ErrorCode.FLOW_INVALID, 
                    "检测到流程图内存在死循环（环状图），已处理节点数: " + processedCount + ", 总节点数: " + nodes.size());
        }

        return root;
    }

    /**
     * 辅助方法：削减后继节点入度
     */
    private void decrementDegrees(String nodeId, Map<String, List<String>> adjacency,
                                  Map<String, Integer> inDegree, Queue<String> queue) {
        for (String successor : adjacency.get(nodeId)) {
            int newDegree = inDegree.get(successor) - 1;
            inDegree.put(successor, newDegree);
            if (newDegree == 0) {
                queue.offer(successor);
            }
        }
    }
}