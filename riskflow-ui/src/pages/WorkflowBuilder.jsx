import { useState, useCallback } from 'react';
import {
    ReactFlow,
    Background,
    Controls,
    MiniMap,
    useNodesState,
    useEdgesState,
    addEdge,
    BackgroundVariant
} from '@xyflow/react';
import '@xyflow/react/dist/style.css';
import RiskNode from '../components/RiskNode';

const nodeTypes = {
    riskNode: RiskNode,
};

// --- 模拟的多个工作流数据 ---
const mockWorkflows = [
    { id: 'wf-01', name: 'Transaction Monitoring v2.4', status: 'Draft' },
    { id: 'wf-02', name: 'Account Takeover Protection', status: 'Active' },
    { id: 'wf-03', name: 'New Device Registration', status: 'Archived' },
];

const initialNodes = [
    // ... (保留你原来完整的 initialNodes 数据)
    {
        id: 'ip-check',
        type: 'riskNode',
        position: { x: 50, y: 160 },
        data: {
            label: 'IP Check', icon: 'public', type: 'check',
            content: <p className="text-xs text-[#c7c4d7]">Analyze geolocation and reputation of inbound IP address.</p>,
            handles: [{ id: 'out' }]
        }
    },
    {
        id: 'condition',
        type: 'riskNode',
        position: { x: 380, y: 150 },
        data: {
            label: 'Condition', icon: 'alt_route', type: 'logic',
            content: (
                <div className="space-y-3">
                    <div className="flex items-center justify-between"><span className="text-xs font-semibold text-[#ffb4ab]">If High Risk</span></div>
                    <div className="flex items-center justify-between"><span className="text-xs font-semibold text-[#908fa0]">Else</span></div>
                </div>
            ),
            handles: [{ id: 'high-risk', top: '40%' }, { id: 'else', top: '75%' }]
        }
    },
    {
        id: 'block',
        type: 'riskNode',
        position: { x: 720, y: 80 },
        data: {
            label: 'Block Action', icon: 'block', type: 'action',
            content: <p className="text-xs text-[#ffb4ab]/80">Reject transaction and log as fraud attempt.</p>,
            handles: []
        }
    },
    {
        id: 'velocity',
        type: 'riskNode',
        position: { x: 720, y: 240 },
        data: {
            label: 'Velocity Check', icon: 'speed', type: 'trigger',
            content: (
                <>
                    <p className="text-xs text-[#c7c4d7] mb-2">Monitor frequency of events per identity.</p>
                    <div className="flex gap-2">
                        <span className="px-2 py-0.5 rounded bg-[#2d3449] text-[9px] text-[#c0c1ff] uppercase font-bold">1h Period</span>
                        <span className="px-2 py-0.5 rounded bg-[#2d3449] text-[9px] text-[#c0c1ff] uppercase font-bold">Max 10</span>
                    </div>
                </>
            ),
            handles: [{ id: 'out' }]
        }
    }
];

const initialEdges = [
    { id: 'e1-2', source: 'ip-check', target: 'condition', animated: true, style: { stroke: '#8083ff', strokeWidth: 2 } },
    { id: 'e2-3', source: 'condition', sourceHandle: 'high-risk', target: 'block', style: { stroke: '#ffb4ab', strokeWidth: 2 } },
    { id: 'e2-4', source: 'condition', sourceHandle: 'else', target: 'velocity', style: { stroke: '#908fa0', strokeWidth: 2 } },
];


export default function WorkflowBuilder() {
    const [nodes, setNodes, onNodesChange] = useNodesState(initialNodes);
    const [edges, setEdges, onEdgesChange] = useEdgesState(initialEdges);
    const [selectedNode, setSelectedNode] = useState(null);

    // 工作流切换状态
    const [activeWorkflow, setActiveWorkflow] = useState(mockWorkflows[0]);
    const [isSwitcherOpen, setIsSwitcherOpen] = useState(false);

    const onConnect = useCallback((params) => setEdges((eds) => addEdge({ ...params, style: { stroke: '#8083ff', strokeWidth: 2 } }, eds)), [setEdges]);
    const onNodeClick = (_, node) => setSelectedNode(node);
    const onPaneClick = () => setSelectedNode(null);

    return (
        <div className="flex-1 flex flex-col h-screen overflow-hidden bg-[#0b1326]">

            {/* --- 新增：画布专属顶部工具栏 --- */}
            <header className="h-14 shrink-0 flex items-center justify-between px-6 bg-[#060e20] border-b border-[#2d3449]/50 z-30">

                {/* 左侧：工作流切换器 */}
                <div className="relative">
                    <div
                        className="flex items-center gap-3 cursor-pointer hover:bg-[#171f33] px-3 py-1.5 rounded-lg transition-colors"
                        onClick={() => setIsSwitcherOpen(!isSwitcherOpen)}
                    >
                        <span className="font-headline font-semibold text-[#dae2fd]">{activeWorkflow.name}</span>
                        <span className={`px-2 py-0.5 rounded bg-[#171f33] border border-[#2d3449] text-[10px] uppercase tracking-widest ${activeWorkflow.status === 'Active' ? 'text-[#4edea3]' : 'text-[#908fa0]'}`}>
                            {activeWorkflow.status}
                        </span>
                        <span className="material-symbols-outlined text-[#908fa0] text-sm">unfold_more</span>
                    </div>

                    {/* 下拉菜单 */}
                    {isSwitcherOpen && (
                        <div className="absolute top-full left-0 mt-2 w-72 bg-[#131b2e] border border-[#2d3449]/80 rounded-xl shadow-2xl py-2 z-50 animate-in slide-in-from-top-2">
                            <div className="px-4 py-2 text-[10px] font-bold text-[#908fa0] uppercase tracking-widest">Switch Workflow</div>
                            {mockWorkflows.map(wf => (
                                <div
                                    key={wf.id}
                                    onClick={() => {
                                        setActiveWorkflow(wf);
                                        setIsSwitcherOpen(false);
                                        // TODO: 这里将来可以调用后端 API 加载对应 wf.id 的节点数据
                                    }}
                                    className={`px-4 py-3 hover:bg-[#171f33] cursor-pointer flex items-center justify-between transition-colors ${activeWorkflow.id === wf.id ? 'bg-[#171f33]/50' : ''}`}
                                >
                                    <span className={`text-sm ${activeWorkflow.id === wf.id ? 'text-[#c0c1ff] font-semibold' : 'text-[#dae2fd]'}`}>{wf.name}</span>
                                    {activeWorkflow.id === wf.id && <span className="material-symbols-outlined text-[#c0c1ff] text-sm">check</span>}
                                </div>
                            ))}
                        </div>
                    )}
                </div>

                {/* 右侧：操作按钮 */}
                <div className="flex items-center gap-3">
                    <button className="px-4 py-1.5 rounded-lg text-xs font-semibold bg-[#171f33] text-white hover:bg-[#222a3d] border border-[#2d3449]/50 transition-all">
                        Simulate
                    </button>
                    <button className="px-4 py-1.5 rounded-lg text-xs font-semibold bg-[#171f33] text-white hover:bg-[#222a3d] border border-[#2d3449]/50 transition-all">
                        Save Draft
                    </button>
                    <button className="px-4 py-1.5 rounded-lg text-xs font-semibold bg-[#c0c1ff] text-[#1000a9] hover:opacity-90 transition-all shadow-lg shadow-[#c0c1ff]/10">
                        Publish Flow
                    </button>
                </div>
            </header>

            {/* --- 原有的画布区域包裹在一个 flex-1 容器里 --- */}
            <div className="flex-1 relative overflow-hidden">
                {/* 1. 左侧悬浮组件库 */}
                <div className="absolute left-4 top-4 bottom-4 w-56 bg-[#171f33]/90 backdrop-blur-md rounded-xl z-20 p-4 border border-[#464554]/30 shadow-2xl overflow-y-auto pointer-events-auto">
                    <h3 className="text-[10px] font-bold uppercase tracking-widest text-[#908fa0] mb-6">Risk Components</h3>
                    <div className="space-y-3">
                        <div className="group flex items-center gap-3 p-3 rounded-lg bg-[#060e20] border border-[#464554]/20 hover:border-[#c0c1ff]/40 cursor-grab transition-all text-white">
                            <span className="material-symbols-outlined text-[#c0c1ff]">speed</span>
                            <span className="text-sm font-medium">Velocity Check</span>
                        </div>
                        <div className="group flex items-center gap-3 p-3 rounded-lg bg-[#060e20] border border-[#464554]/20 hover:border-[#c0c1ff]/40 cursor-grab transition-all text-white">
                            <span className="material-symbols-outlined text-[#4edea3]">public</span>
                            <span className="text-sm font-medium">IP Check</span>
                        </div>
                        <div className="group flex items-center gap-3 p-3 rounded-lg bg-[#060e20] border border-[#ffb4ab]/30 hover:border-[#ffb4ab]/60 cursor-grab transition-all text-white">
                            <span className="material-symbols-outlined text-[#ffb4ab]">block</span>
                            <span className="text-sm font-medium">Block Action</span>
                        </div>
                    </div>
                </div>

                {/* 2. React Flow 核心画布 */}
                <ReactFlow
                    nodes={nodes}
                    edges={edges}
                    onNodesChange={onNodesChange}
                    onEdgesChange={onEdgesChange}
                    onConnect={onConnect}
                    onNodeClick={onNodeClick}
                    onPaneClick={onPaneClick}
                    nodeTypes={nodeTypes}
                    fitView
                    className="z-10"
                >
                    <Background variant={BackgroundVariant.Dots} gap={24} size={2} color="#2d3449" />
                    <MiniMap nodeStrokeWidth={3} nodeColor="#2d3449" maskColor="rgba(11, 19, 38, 0.7)" className="bg-[#171f33] border border-[#464554]/30 rounded-lg !bottom-6 !right-[340px]" />
                    <Controls className="!bottom-6 !left-64 bg-[#171f33] border border-[#464554]/30 fill-white" />
                </ReactFlow>

                {/* 3. 右侧动态配置面板 */}
                <aside className={`absolute right-0 top-0 bottom-0 w-80 bg-[#222a3d]/95 backdrop-blur-2xl border-l border-[#464554]/30 z-40 flex flex-col shadow-2xl transition-transform duration-300 ${selectedNode ? 'translate-x-0' : 'translate-x-full'}`}>
                    {selectedNode && (
                        <>
                            <div className="p-6 border-b border-[#464554]/30">
                                <div className="flex items-center justify-between mb-2">
                                    <h2 className="text-lg font-bold text-white">Node Settings</h2>
                                    <span className="material-symbols-outlined text-[#908fa0] cursor-pointer hover:text-white" onClick={onPaneClick}>close</span>
                                </div>
                                <div className="flex items-center gap-2">
                                    <span className="material-symbols-outlined text-[#c0c1ff] text-sm">{selectedNode.data.icon}</span>
                                    <span className="text-xs text-[#908fa0] font-medium">{selectedNode.data.label}</span>
                                </div>
                            </div>

                            <div className="flex-1 p-6 space-y-6">
                                <label className="block">
                                    <span className="text-[10px] font-bold uppercase tracking-widest text-[#908fa0] mb-2 block">Threshold Limit</span>
                                    <input className="w-full bg-[#060e20] border border-[#464554]/30 rounded-lg text-sm text-white py-3 px-4 focus:ring-1 focus:ring-[#c0c1ff] outline-none" type="number" defaultValue="10" />
                                </label>
                                <button className="w-full py-3 rounded-lg bg-gradient-to-br from-[#c0c1ff] to-[#8083ff] text-[#1000a9] font-bold hover:opacity-90 active:scale-95 transition-all mt-4">
                                    Update Node
                                </button>
                            </div>
                        </>
                    )}
                </aside>
            </div>
        </div>
    );
}