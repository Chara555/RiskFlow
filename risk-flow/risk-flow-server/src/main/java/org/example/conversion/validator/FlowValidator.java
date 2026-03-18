package org.example.conversion.validator;

import org.example.conversion.model.FlowDefinition;
import org.example.conversion.model.FlowEdge;
import org.example.conversion.model.FlowNode;
import org.example.conversion.model.ValidationResult;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 流程定义验证器
 *
 * 作用：
 *   在 FlowAnalyzer 分析和 ELGenerator 生成 EL 表达式之前，对前端传入的
 *   FlowDefinition 进行合法性校验，充当「安全门」，
 *   拦截非法输入并返回清晰的错误提示，避免非法数据进入后续流程导致：
 *     - 生成语法错误的 LiteFlow EL 表达式（如 componentId 为空）
 *     - Analyzer 陷入死循环（流程图存在环）
 *     - 执行顺序不确定（存在多个起始节点）
 *     - 条件分支缺失导致 EL 生成残缺（IF 少了 true/false 分支）
 *
 * 验证分三层：
 *   1. 基础检查 - 空值、节点ID唯一性、componentId 不能为空
 *   2. 结构检查 - 有且仅有一个起始节点、无环（DFS检测）、无孤立节点（警告）
 *   3. 语义检查 - 条件节点必须有两条出边（label="true" 和 label="false"）
 *
 * 返回值：
 *   - errors  不为空 → 验证不通过，调用方应直接返回错误给前端
 *   - warnings 不为空 → 仅警告，不影响执行（如孤立节点）
 *
 * 调用方：
 *   LiteFlowConversionEngine#convert() 在调用 FlowAnalyzer 之前先调用此类。
 *
 * 扩展方式：
 *   如需增加新的验证规则，在对应的私有方法中添加即可：
 *     - 基础规则 -> validateBasic()
 *     - 结构规则 -> validateStructure()
 *     - 语义规则 -> validateSemantics()
 */
@Component
public class FlowValidator {

    /**
     * 条件节点的类型标识（前端节点 type 字段值）
     * 条件节点需要有且仅有 true/false 两条出边
     */
    private static final String CONDITION_NODE_TYPE = "condition";

    /** 条件边的 true 标签 */
    private static final String LABEL_TRUE = "true";

    /** 条件边的 false 标签 */
    private static final String LABEL_FALSE = "false";

    /**
     * 执行完整验证
     *
     * @param flow 前端传入的流程定义
     * @return 验证结果，result.isValid() 为 true 表示验证通过
     */
    public ValidationResult validate(FlowDefinition flow) {
        ValidationResult result = new ValidationResult();

        // 第一层：基础检查（后续检查依赖这层通过）
        validateBasic(flow, result);
        if (!result.isValid()) {
            return result;
        }

        // 第二层：结构检查
        validateStructure(flow, result);
        if (!result.isValid()) {
            return result;
        }

        // 第三层：语义检查
        validateSemantics(flow, result);

        return result;
    }

    // =========================================================
    // 第一层：基础检查
    // =========================================================

    /**
     * 基础检查：空值、节点ID唯一性、componentId 非空
     */
    private void validateBasic(FlowDefinition flow, ValidationResult result) {
        if (flow == null) {
            result.addError("流程定义不能为空");
            return;
        }

        // nodes 不能为空
        if (flow.getNodes() == null || flow.getNodes().isEmpty()) {
            result.addError("流程至少需要一个节点");
            return;
        }

        // edges 不能为 null（可以为空列表，单节点流程无边）
        if (flow.getEdges() == null) {
            result.addError("edges 字段不能为 null，单节点流程请传空数组 []");
            return;
        }

        // 节点ID唯一性检查
        Set<String> nodeIds = new HashSet<>();
        for (FlowNode node : flow.getNodes()) {
            if (node.getId() == null || node.getId().isBlank()) {
                result.addError("存在 id 为空的节点，所有节点必须有唯一ID");
                continue;
            }
            if (!nodeIds.add(node.getId())) {
                result.addError("节点ID重复：" + node.getId());
            }
        }

        // componentId 不能为空（LiteFlow 执行依赖此字段）
        for (FlowNode node : flow.getNodes()) {
            if (node.getComponentId() == null || node.getComponentId().isBlank()) {
                result.addError("节点 [" + node.getId() + "] 的 componentId 为空，" +
                        "请在前端为该节点选择对应的 LiteFlow 组件");
            }
        }
    }

    // =========================================================
    // 第二层：结构检查
    // =========================================================

    /**
     * 结构检查：起始节点唯一、无环、无孤立节点
     */
    private void validateStructure(FlowDefinition flow, ValidationResult result) {
        List<FlowNode> nodes = flow.getNodes();
        List<FlowEdge> edges = flow.getEdges();

        // 计算每个节点的入度
        Map<String, Integer> inDegree = new HashMap<>();
        nodes.forEach(n -> inDegree.put(n.getId(), 0));
        for (FlowEdge edge : edges) {
            inDegree.merge(edge.getTargetNodeId(), 1, Integer::sum);
        }

        // 找起始节点（入度为0）
        List<String> startNodes = inDegree.entrySet().stream()
                .filter(e -> e.getValue() == 0)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        if (startNodes.isEmpty()) {
            result.addError("找不到起始节点（所有节点都有入边，流程可能存在环）");
            return;
        }
        if (startNodes.size() > 1) {
            result.addError("存在多个起始节点：" + startNodes + "，流程只能有一个入口");
            return;
        }

        // 环检测（DFS）
        if (hasCycle(nodes, edges)) {
            result.addError("流程存在环，LiteFlow 不支持循环结构（请使用 FOR/WHILE 节点）");
            return;
        }

        // 孤立节点检查（警告，不报错）
        // 单节点流程（无边）不算孤立
        if (!edges.isEmpty()) {
            Set<String> connectedNodeIds = new HashSet<>();
            edges.forEach(e -> {
                connectedNodeIds.add(e.getSourceNodeId());
                connectedNodeIds.add(e.getTargetNodeId());
            });
            nodes.stream()
                    .filter(n -> !connectedNodeIds.contains(n.getId()))
                    .forEach(n -> result.addWarning(
                            "节点 [" + n.getId() + "|" + n.getComponentId() + "] 未连接任何边，将不会被执行"));
        }
    }

    /**
     * 环检测（DFS 着色法）
     *
     * 节点状态：白色（未访问）-> 灰色（访问中）-> 黑色（已完成）
     * 若在 DFS 过程中访问到灰色节点，说明存在环。
     */
    private boolean hasCycle(List<FlowNode> nodes, List<FlowEdge> edges) {
        // 构建邻接表
        Map<String, List<String>> graph = new HashMap<>();
        nodes.forEach(n -> graph.put(n.getId(), new ArrayList<>()));
        edges.forEach(e -> graph.computeIfAbsent(e.getSourceNodeId(), k -> new ArrayList<>())
                .add(e.getTargetNodeId()));

        Set<String> visited = new HashSet<>();  // 黑色：已完成
        Set<String> inStack  = new HashSet<>();  // 灰色：访问中

        for (FlowNode node : nodes) {
            if (!visited.contains(node.getId())) {
                if (dfsCycleDetect(node.getId(), graph, visited, inStack)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean dfsCycleDetect(String nodeId,
                                   Map<String, List<String>> graph,
                                   Set<String> visited,
                                   Set<String> inStack) {
        inStack.add(nodeId);

        for (String neighbor : graph.getOrDefault(nodeId, Collections.emptyList())) {
            if (inStack.contains(neighbor)) {
                return true;  // 访问到灰色节点 -> 有环
            }
            if (!visited.contains(neighbor)) {
                if (dfsCycleDetect(neighbor, graph, visited, inStack)) {
                    return true;
                }
            }
        }

        inStack.remove(nodeId);
        visited.add(nodeId);  // 标记为黑色
        return false;
    }

    // =========================================================
    // 第三层：语义检查
    // =========================================================

    /**
     * 语义检查：条件节点必须有且仅有 label="true" 和 label="false" 两条出边
     */
    private void validateSemantics(FlowDefinition flow, ValidationResult result) {
        List<FlowEdge> edges = flow.getEdges();

        for (FlowNode node : flow.getNodes()) {
            if (!CONDITION_NODE_TYPE.equals(node.getType())) {
                continue;
            }

            // 获取该条件节点的所有出边
            List<FlowEdge> outEdges = edges.stream()
                    .filter(e -> node.getId().equals(e.getSourceNodeId()))
                    .collect(Collectors.toList());

            if (outEdges.size() != 2) {
                result.addError("条件节点 [" + node.getId() + "|" + node.getComponentId() +
                        "] 必须有且仅有 2 条出边，当前有 " + outEdges.size() + " 条");
                continue;
            }

            // 检查必须同时有 true 和 false 标签
            Set<String> labels = outEdges.stream()
                    .map(FlowEdge::getLabel)
                    .collect(Collectors.toSet());

            if (!labels.contains(LABEL_TRUE)) {
                result.addError("条件节点 [" + node.getId() + "|" + node.getComponentId() +
                        "] 缺少 label=\"true\" 的出边");
            }
            if (!labels.contains(LABEL_FALSE)) {
                result.addError("条件节点 [" + node.getId() + "|" + node.getComponentId() +
                        "] 缺少 label=\"false\" 的出边");
            }
        }
    }
}
