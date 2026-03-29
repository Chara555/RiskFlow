import { useState, useEffect } from 'react';
import ExecutionTraceGraph from '../components/logs/ExecutionTraceGraph';

// 模拟实时的风控拦截数据流
const mockIncidents = [
    { id: 'RF-9928', time: '12s ago', ip: '192.168.1.104', rule: 'High Velocity Login', status: 'BLOCKED', risk: 98 },
    { id: 'RF-9927', time: '45s ago', ip: '203.0.113.42', rule: 'Suspicious Geo-location', status: 'REVIEW', risk: 75 },
    { id: 'RF-9926', time: '2m ago', ip: '10.0.0.5', rule: 'Standard Check', status: 'PASSED', risk: 12 },
    { id: 'RF-9925', time: '5m ago', ip: '172.16.254.1', rule: 'Device Fingerprint Mismatch', status: 'BLOCKED', risk: 91 },
    { id: 'RF-9924', time: '12m ago', ip: '198.51.100.22', rule: 'Botnet IP Pool', status: 'BLOCKED', risk: 99 },
];

export default function Incidents() {
    const [selectedIncident, setSelectedIncident] = useState(null);
    const [typingText, setTypingText] = useState('');

    // 模拟 AI Copilot 的流式打字机效果
    useEffect(() => {
        if (selectedIncident && selectedIncident.status !== 'PASSED') {
            setTypingText('');
            const fullText = `[LiteFlow Executor] DAG execution halted at node: ${selectedIncident.rule}.\n\n[LangChain4j RAG Agent] Analyzing context...\n➜ IP ${selectedIncident.ip} cross-referenced with global threat intelligence.\n➜ Historical behavior indicates a 94% probability of credential stuffing attack.\n\n[Decision] Maintain BLOCKED status. Recommended to add device fingerprint to permanent Vault.`;

            let i = 0;
            const timer = setInterval(() => {
                setTypingText(fullText.substring(0, i));
                i++;
                if (i > fullText.length) clearInterval(timer);
            }, 20); // 打字速度
            return () => clearInterval(timer);
        }
    }, [selectedIncident]);

    // 状态徽章颜色映射
    const getStatusStyle = (status) => {
        switch (status) {
            case 'BLOCKED': return 'bg-rose-500/10 text-rose-400 border-rose-500/20';
            case 'REVIEW': return 'bg-amber-500/10 text-amber-400 border-amber-500/20';
            case 'PASSED': return 'bg-emerald-500/10 text-emerald-400 border-emerald-500/20';
            default: return 'bg-slate-500/10 text-slate-400 border-slate-500/20';
        }
    };

    return (
        // 外部容器：填满 Sidebar 右侧的全部空间
        <div className="flex h-screen bg-[#070c1a] text-slate-300 font-headline antialiased w-full overflow-hidden">

            {/* 左侧：实时事件流 (Event Stream) */}
            <div className="w-[420px] flex flex-col border-r border-slate-800/50 bg-[#0b1326]/40 shadow-2xl z-10">
                {/* 头部 */}
                <div className="p-6 border-b border-slate-800/50 flex justify-between items-center bg-[#0b1326]/80 backdrop-blur-md">
                    <div>
                        <h2 className="text-xl font-bold text-[#c0c1ff] tracking-tight">Real-time Incidents</h2>
                        <p className="text-xs text-slate-500 mt-1 font-medium">Monitoring LiteFlow Execution...</p>
                    </div>
                    <button className="p-2 rounded-lg text-slate-400 hover:text-white hover:bg-[#171f33] transition-all">
                        <span className="material-symbols-outlined">filter_list</span>
                    </button>
                </div>

                {/* 列表流 */}
                <div className="flex-1 overflow-y-auto p-4 space-y-3 custom-scrollbar">
                    {mockIncidents.map((incident) => (
                        <div
                            key={incident.id}
                            onClick={() => setSelectedIncident(incident)}
                            className={`p-4 rounded-xl border cursor-pointer transition-all duration-200 group ${selectedIncident?.id === incident.id
                                ? 'bg-[#171f33] border-[#c0c1ff]/40 shadow-lg shadow-[#c0c1ff]/5 translate-x-1'
                                : 'bg-[#0b1326]/50 border-slate-800/60 hover:border-slate-600 hover:bg-[#171f33]/50'
                                }`}
                        >
                            <div className="flex justify-between items-start mb-3">
                                <span className={`text-[10px] font-bold px-2.5 py-1 rounded border uppercase tracking-wider ${getStatusStyle(incident.status)}`}>
                                    {incident.status}
                                </span>
                                <span className="text-xs text-slate-500 font-mono">{incident.time}</span>
                            </div>
                            <div className="text-base font-semibold text-slate-200 group-hover:text-white transition-colors">
                                {incident.ip}
                            </div>
                            <div className="flex justify-between items-end mt-2">
                                <div className="text-xs text-slate-500 truncate flex items-center gap-1.5">
                                    <span className="material-symbols-outlined text-[14px]">account_tree</span>
                                    {incident.rule}
                                </div>
                                <div className="text-xs font-mono font-bold text-slate-400">
                                    Risk: <span className={incident.risk > 80 ? 'text-rose-400' : 'text-emerald-400'}>{incident.risk}</span>
                                </div>
                            </div>
                        </div>
                    ))}
                </div>
            </div>

            {/* 右侧：案件详情与 AI 审查抽屉 */}
            <div className="flex-1 flex flex-col relative bg-gradient-to-br from-[#070c1a] to-[#0a1020]">
                {selectedIncident ? (
                    <div className="flex-1 flex flex-col p-8 lg:p-12 overflow-y-auto animate-in fade-in slide-in-from-right-8 duration-300">

                        {/* 头部信息 */}
                        <div className="mb-10 flex justify-between items-start">
                            <div>
                                <div className="flex items-center gap-3 mb-2">
                                    <span className="material-symbols-outlined text-rose-500 text-3xl">shield_alert</span>
                                    <h2 className="text-3xl font-bold text-white tracking-tighter">Case #{selectedIncident.id}</h2>
                                </div>
                                <p className="text-sm text-slate-400 flex items-center gap-2">
                                    Captured at <span className="text-slate-200 font-mono">2026-03-24 15:42:01</span> via Netty Gateway
                                </p>
                            </div>
                            <div className="flex gap-3">
                                <button className="px-5 py-2.5 bg-transparent border border-slate-700 hover:border-slate-500 text-slate-300 rounded-xl text-sm font-bold transition-all">
                                    Export JSON
                                </button>
                                <button className="px-5 py-2.5 bg-[#222a3d] hover:bg-[#c0c1ff] text-[#c0c1ff] hover:text-[#0b1326] rounded-xl text-sm font-bold shadow-lg transition-all duration-300">
                                    Override Decision
                                </button>
                            </div>
                        </div>

                        {/* 真正的 DAG 执行轨迹画布 */}
                        <div className="mb-6">
                            <h3 className="text-sm font-bold text-slate-400 mb-3 uppercase tracking-widest flex items-center gap-2">
                                <span className="material-symbols-outlined text-base">route</span>
                                Execution Trace
                            </h3>
                            {/* 注意这里去掉了 flex items-center justify-center，让 React Flow 填满容器 */}
                            <div className="h-64 w-full rounded-2xl border border-slate-800/80 bg-[#070c1a] shadow-inner relative overflow-hidden">
                                {/* 关键：把当前选中的 incident 传给画布组件 */}
                                <ExecutionTraceGraph incident={selectedIncident} />
                            </div>
                        </div>

                        {/* AI Copilot 终端 */}
                        <div className="flex-1 min-h-[300px] flex flex-col">
                            <h3 className="text-sm font-bold text-[#c0c1ff] mb-3 uppercase tracking-widest flex items-center gap-2">
                                <span className="material-symbols-outlined text-base animate-pulse">smart_toy</span>
                                AI Copilot Analysis
                            </h3>
                            <div className="flex-1 rounded-2xl border border-[#c0c1ff]/20 bg-[#040710] p-6 font-mono text-sm relative shadow-2xl shadow-[#c0c1ff]/5 overflow-hidden">
                                {/* 终端顶部高亮线 */}
                                <div className="absolute top-0 left-0 w-full h-[2px] bg-gradient-to-r from-transparent via-[#c0c1ff]/50 to-transparent"></div>

                                {selectedIncident.status === 'PASSED' ? (
                                    <p className="text-slate-500">No anomaly detected. AI deep inspection bypassed.</p>
                                ) : (
                                    <div className="text-slate-300 leading-relaxed whitespace-pre-wrap">
                                        {typingText}
                                        <span className="inline-block w-2 h-4 bg-[#c0c1ff] ml-1 animate-pulse align-middle"></span>
                                    </div>
                                )}
                            </div>
                        </div>

                    </div>
                ) : (
                    // 未选中时的空状态
                    <div className="flex-1 flex flex-col items-center justify-center text-slate-500 bg-[radial-gradient(ellipse_at_center,_var(--tw-gradient-stops))] from-[#0b1326] to-[#070c1a]">
                        <div className="w-24 h-24 mb-6 rounded-full bg-[#171f33] flex items-center justify-center shadow-2xl border border-slate-800/50">
                            <span className="material-symbols-outlined text-5xl text-slate-600">policy</span>
                        </div>
                        <h3 className="text-xl font-bold text-slate-400 mb-2">No Incident Selected</h3>
                        <p className="text-sm font-medium">Click on an event stream card to view AI analysis and execution traces.</p>
                    </div>
                )}
            </div>
        </div>
    );
}