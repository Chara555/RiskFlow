/**
 * 流程数据转换工具
 *
 * 将 ReactFlow 的数据格式转换为后端 FlowDefinition 格式
 */

/**
 * 节点类型映射（ReactFlow type → 后端 type）
 */
const NODE_TYPE_MAP = {
    riskNode: 'process',
    process: 'process',
    condition: 'condition',
    start: 'start',
    end: 'end',
};

/**
 * 将 ReactFlow 节点转换为后端 FlowNode 格式
 *
 * @param {Object} node - ReactFlow 节点
 * @returns {Object} FlowNode 格式的节点
 */
function convertNode(node) {
    return {
        id: node.id,
        type: NODE_TYPE_MAP[node.type] || node.type || 'process',
        componentId: node.data?.componentId || node.id,
        label: node.data?.label || node.id,
        properties: node.data?.properties || {},
        x: node.position?.x,
        y: node.position?.y,
    };
}

/**
 * 将 ReactFlow 边转换为后端 FlowEdge 格式
 *
 * @param {Object} edge - ReactFlow 边
 * @returns {Object} FlowEdge 格式的边
 */
function convertEdge(edge) {
    return {
        id: edge.id,
        sourceNodeId: edge.source,
        targetNodeId: edge.target,
        label: edge.label || null,
        type: edge.type || 'default',
    };
}

/**
 * 将 ReactFlow 数据转换为 FlowDefinition 格式
 *
 * @param {Object} params - 参数
 * @param {Array} params.nodes - ReactFlow 节点数组
 * @param {Array} params.edges - ReactFlow 边数组
 * @param {string} params.code - 流程编码
 * @param {string} params.name - 流程名称
 * @param {string} params.description - 流程描述
 * @returns {Object} FlowDefinition 格式的数据
 */
export function toFlowDefinition({ nodes, edges, code, name, description }) {
    return {
        code: code || `flow-${Date.now()}`,
        name: name || '未命名流程',
        description: description || '',
        nodes: nodes.map(convertNode),
        edges: edges.map(convertEdge),
    };
}

/**
 * 将 FlowDefinition 转换回 ReactFlow 格式（用于加载保存的流程）
 *
 * @param {Object} flowDefinition - FlowDefinition 格式的数据
 * @returns {Object} ReactFlow 格式的数据 { nodes, edges }
 */
export function toReactFlow(flowDefinition) {
    const nodes = flowDefinition.nodes.map((node) => ({
        id: node.id,
        type: 'riskNode',
        position: { x: node.x || 0, y: node.y || 0 },
        data: {
            label: node.label,
            componentId: node.componentId,
            properties: node.properties,
            // 根据 type 设置默认图标和样式
            ...getNodeTypeDefaults(node.type),
        },
    }));

    const edges = flowDefinition.edges.map((edge) => ({
        id: edge.id,
        source: edge.sourceNodeId,
        target: edge.targetNodeId,
        label: edge.label,
        type: edge.type || 'default',
        animated: true,
        style: { stroke: '#8083ff', strokeWidth: 2 },
    }));

    return { nodes, edges };
}

/**
 * 根据节点类型获取默认样式配置
 */
function getNodeTypeDefaults(type) {
    const defaults = {
        process: { icon: 'settings', type: 'check' },
        condition: { icon: 'alt_route', type: 'logic' },
        start: { icon: 'play_arrow', type: 'trigger' },
        end: { icon: 'stop', type: 'action' },
    };
    return defaults[type] || defaults.process;
}

/**
 * 验证流程数据是否有效
 *
 * @param {Object} flowDefinition - FlowDefinition 格式的数据
 * @returns {Object} { valid: boolean, errors: string[] }
 */
export function validateFlowDefinition(flowDefinition) {
    const errors = [];

    if (!flowDefinition.nodes || flowDefinition.nodes.length === 0) {
        errors.push('流程至少需要一个节点');
    }

    // 检查节点 componentId
    flowDefinition.nodes?.forEach((node) => {
        if (!node.componentId) {
            errors.push(`节点 [${node.id}] 缺少 componentId`);
        }
    });

    // 检查边的有效性
    const nodeIds = new Set(flowDefinition.nodes?.map((n) => n.id) || []);
    flowDefinition.edges?.forEach((edge) => {
        if (!nodeIds.has(edge.sourceNodeId)) {
            errors.push(`边 [${edge.id}] 的源节点不存在: ${edge.sourceNodeId}`);
        }
        if (!nodeIds.has(edge.targetNodeId)) {
            errors.push(`边 [${edge.id}] 的目标节点不存在: ${edge.targetNodeId}`);
        }
    });

    return {
        valid: errors.length === 0,
        errors,
    };
}

export default {
    toFlowDefinition,
    toReactFlow,
    validateFlowDefinition,
};
