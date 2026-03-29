import React, { useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';

// 模拟当前知识库里的文档数据
const mockDocuments = [
    { id: 'd1', name: 'Account_Takeover_Rules_2026.pdf', status: 'Active', chunks: 245, size: '2.4 MB', date: '2 hrs ago' },
    { id: 'd2', name: 'Device_Fingerprint_Specs.md', status: 'Embedding', progress: 78, chunks: 120, size: '850 KB', date: 'Just now' },
    { id: 'd3', name: 'Historical_Scam_IPs.csv', status: 'Queued', chunks: 0, size: '12 MB', date: 'Pending' }
];

export default function KnowledgeVaultDetail() {
    const { id } = useParams(); // 从路由获取知识库 ID (如: kb_fraud)
    const navigate = useNavigate();
    const [activeTab, setActiveTab] = useState('Documents');
    
    // 召回测试的模拟状态
    const [testQuery, setTestQuery] = useState('');
    const [isTesting, setIsTesting] = useState(false);
    const [testResults, setTestResults] = useState(null);

    // 模拟名字映射
    const vaultName = id === 'kb_fraud' ? 'Anti-Fraud Policies' : (id === 'kb_compliance' ? 'Compliance & AML' : 'Threat Intel');

    // 模拟运行召回测试
    const handleRunTest = () => {
        if (!testQuery.trim()) return;
        setIsTesting(true);
        setTestResults(null);
        // 模拟网络延迟和 LangChain4j 检索时间
        setTimeout(() => {
            setTestResults([
                { score: 0.92, text: "Rule 4.1.2: If a user attempts to login from a device with a mismatching hardware fingerprint and the IP originates from a high-risk ASN, the system MUST trigger a step-up authentication or block the request entirely based on the velocity.", doc: "Account_Takeover_Rules_2026.pdf" },
                { score: 0.85, text: "Device fingerprinting involves collecting screen resolution, user-agent, canvas rendering hashes, and WebGL parameters. A deviation of >20% from the historical profile is considered highly suspicious.", doc: "Device_Fingerprint_Specs.md" }
            ]);
            setIsTesting(false);
        }, 1500);
    };

    return (
        <div className="flex-1 flex flex-col h-screen overflow-hidden bg-[#070c1a] text-white font-headline antialiased">
            
            {/* 顶部 Header：面包屑导航 */}
            <header className="flex flex-col px-8 pt-6 pb-0 shrink-0 bg-[#0b1326]/80 backdrop-blur-md border-b border-slate-800/50 sticky top-0 z-30">
                <div className="flex items-center gap-2 text-sm text-slate-500 mb-4 font-semibold">
                    <button onClick={() => navigate('/knowledge-base')} className="hover:text-[#c0c1ff] transition-colors flex items-center gap-1">
                        <span className="material-symbols-outlined text-[16px]">database</span>
                        Knowledge Base Hub
                    </button>
                    <span className="material-symbols-outlined text-[14px]">chevron_right</span>
                    <span className="text-slate-200">{vaultName}</span>
                </div>
                
                <div className="flex items-center justify-between mb-6">
                    <h2 className="text-3xl font-extrabold text-white tracking-tighter">{vaultName}</h2>
                    <button className="flex items-center gap-2 px-4 py-2 bg-[#222a3d] hover:bg-[#c0c1ff] text-[#c0c1ff] hover:text-[#0b1326] font-bold text-sm rounded-lg transition-all shadow-lg">
                        <span className="material-symbols-outlined text-sm">upload_file</span>
                        Add Document
                    </button>
                </div>

                {/* Dify 风格的 Tab 导航 */}
                <div className="flex gap-8">
                    {['Documents', 'Hit Testing', 'Settings'].map(tab => (
                        <button 
                            key={tab}
                            onClick={() => setActiveTab(tab)}
                            className={`pb-4 text-sm font-bold transition-all relative ${
                                activeTab === tab ? 'text-[#c0c1ff]' : 'text-slate-500 hover:text-slate-300'
                            }`}
                        >
                            {tab}
                            {activeTab === tab && (
                                <div className="absolute bottom-0 left-0 w-full h-[3px] bg-[#c0c1ff] rounded-t-full shadow-[0_-2px_10px_rgba(192,193,255,0.5)]"></div>
                            )}
                        </button>
                    ))}
                </div>
            </header>

            {/* 主内容区 */}
            <main className="flex-1 overflow-y-auto p-8 custom-scrollbar">
                <div className="max-w-6xl mx-auto">
                    
                    {/* ===== Tab 1: 文档管理 ===== */}
                    {activeTab === 'Documents' && (
                        <div className="animate-in fade-in slide-in-from-bottom-4 duration-300">
                            {/* 拖拽上传区 */}
                            <div className="mb-8 w-full rounded-2xl border-2 border-dashed border-slate-700 bg-[#0b1326]/50 hover:bg-[#101a31]/80 hover:border-[#c0c1ff]/50 transition-all duration-300 flex flex-col items-center justify-center py-12 cursor-pointer group">
                                <div className="w-14 h-14 mb-4 rounded-full bg-slate-800/50 flex items-center justify-center group-hover:scale-110 group-hover:bg-[#c0c1ff]/10 transition-transform duration-300">
                                    <span className="material-symbols-outlined text-3xl text-slate-400 group-hover:text-[#c0c1ff] transition-colors">cloud_upload</span>
                                </div>
                                <h4 className="text-lg font-bold text-slate-200 mb-1 group-hover:text-white">Drag & drop files here to vectorize</h4>
                                <p className="text-xs text-slate-500 font-mono">Supported: PDF, CSV, TXT, MD (Max 50MB/file)</p>
                            </div>

                            {/* 文档列表 */}
                            <div className="bg-[#0b1326] border border-slate-800/80 rounded-2xl overflow-hidden shadow-xl">
                                <div className="px-6 py-4 border-b border-slate-800/80 bg-[#101a31]/50 flex justify-between items-center">
                                    <h3 className="font-bold text-slate-300 text-sm">Indexed Documents ({mockDocuments.length})</h3>
                                    <span className="material-symbols-outlined text-slate-500 text-sm cursor-pointer hover:text-white">filter_list</span>
                                </div>
                                <div className="divide-y divide-slate-800/80">
                                    {mockDocuments.map(doc => (
                                        <div key={doc.id} className="p-4 hover:bg-[#101a31] transition-colors flex items-center justify-between group">
                                            <div className="flex items-center gap-4">
                                                <span className={`material-symbols-outlined text-2xl ${doc.name.endsWith('pdf') ? 'text-rose-400' : (doc.name.endsWith('csv') ? 'text-emerald-400' : 'text-blue-400')}`}>
                                                    {doc.name.endsWith('pdf') ? 'picture_as_pdf' : 'description'}
                                                </span>
                                                <div>
                                                    <div className="font-bold text-sm text-slate-200 group-hover:text-white mb-0.5 cursor-pointer">{doc.name}</div>
                                                    <div className="text-[10px] text-slate-500 font-mono">{doc.size} • Uploaded {doc.date}</div>
                                                </div>
                                            </div>
                                            <div className="flex items-center gap-6">
                                                <div className="flex flex-col items-end">
                                                    {doc.status === 'Active' && <span className="px-2 py-0.5 rounded text-[10px] font-bold uppercase bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">Active ({doc.chunks} Chunks)</span>}
                                                    {doc.status === 'Embedding' && <span className="px-2 py-0.5 rounded text-[10px] font-bold uppercase bg-amber-500/10 text-amber-400 border border-amber-500/20 animate-pulse">Embedding... {doc.progress}%</span>}
                                                    {doc.status === 'Queued' && <span className="px-2 py-0.5 rounded text-[10px] font-bold uppercase bg-slate-500/10 text-slate-400 border border-slate-500/20">Queued</span>}
                                                </div>
                                                <button className="text-slate-600 hover:text-white transition-colors">
                                                    <span className="material-symbols-outlined text-lg">more_horiz</span>
                                                </button>
                                            </div>
                                        </div>
                                    ))}
                                </div>
                            </div>
                        </div>
                    )}

                    {/* ===== Tab 2: 命中测试 (核心功能) ===== */}
                    {activeTab === 'Hit Testing' && (
                        <div className="flex gap-6 h-[500px] animate-in fade-in slide-in-from-bottom-4 duration-300">
                            {/* 左侧：输入区 */}
                            <div className="w-1/3 flex flex-col gap-4">
                                <div className="flex-1 bg-[#0b1326] border border-slate-800/80 rounded-2xl p-5 flex flex-col shadow-lg">
                                    <h3 className="text-sm font-bold text-slate-300 mb-3 flex items-center gap-2">
                                        <span className="material-symbols-outlined text-[18px]">manage_search</span>
                                        Test Retrieval Query
                                    </h3>
                                    <textarea 
                                        className="flex-1 bg-[#101a31] border border-slate-700/50 rounded-xl p-4 text-sm text-slate-200 placeholder-slate-500 resize-none focus:outline-none focus:border-[#c0c1ff]/50 custom-scrollbar"
                                        placeholder="Enter text to simulate what the RAG agent will search for... e.g., 'What is the policy for mismatching device fingerprints?'"
                                        value={testQuery}
                                        onChange={(e) => setTestQuery(e.target.value)}
                                    ></textarea>
                                    <button 
                                        onClick={handleRunTest}
                                        disabled={!testQuery.trim() || isTesting}
                                        className="mt-4 w-full py-3 bg-[#222a3d] hover:bg-[#c0c1ff] disabled:opacity-50 disabled:hover:bg-[#222a3d] disabled:hover:text-[#c0c1ff] text-[#c0c1ff] hover:text-[#0b1326] font-bold text-sm rounded-xl transition-all shadow-lg flex justify-center items-center gap-2"
                                    >
                                        {isTesting ? <span className="material-symbols-outlined animate-spin">refresh</span> : 'Run Hit Test'}
                                    </button>
                                </div>
                            </div>

                            {/* 右侧：结果区 */}
                            <div className="w-2/3 bg-[#0b1326] border border-slate-800/80 rounded-2xl p-6 shadow-lg overflow-y-auto custom-scrollbar relative">
                                {!testResults && !isTesting && (
                                    <div className="absolute inset-0 flex flex-col items-center justify-center text-slate-500">
                                        <span className="material-symbols-outlined text-5xl mb-3 opacity-20">youtube_searched_for</span>
                                        <p className="text-sm font-medium">Input a query to test vector similarity search.</p>
                                    </div>
                                )}
                                
                                {isTesting && (
                                    <div className="absolute inset-0 flex flex-col items-center justify-center text-[#c0c1ff]">
                                        <span className="material-symbols-outlined text-4xl mb-3 animate-spin">radar</span>
                                        <p className="text-sm font-bold animate-pulse">Scanning pgvector database...</p>
                                    </div>
                                )}

                                {testResults && !isTesting && (
                                    <div className="space-y-4 animate-in fade-in duration-300">
                                        <h3 className="text-sm font-bold text-emerald-400 mb-4 flex items-center gap-2">
                                            <span className="material-symbols-outlined text-[18px]">check_circle</span>
                                            Retrieved Top {testResults.length} Chunks
                                        </h3>
                                        {testResults.map((result, idx) => (
                                            <div key={idx} className="bg-[#101a31] border border-slate-700/50 rounded-xl p-5 relative overflow-hidden group">
                                                <div className="absolute top-0 left-0 w-1 h-full bg-[#c0c1ff]"></div>
                                                <div className="flex justify-between items-start mb-3 pl-2">
                                                    <div className="flex items-center gap-2 text-[10px] text-slate-400 font-mono bg-[#0b1326] px-2 py-1 rounded border border-slate-800">
                                                        <span className="material-symbols-outlined text-[12px]">description</span>
                                                        {result.doc}
                                                    </div>
                                                    <div className="flex items-center gap-1 text-xs font-bold text-[#c0c1ff] bg-[#c0c1ff]/10 px-2 py-1 rounded">
                                                        <span className="material-symbols-outlined text-[14px]">troubleshoot</span>
                                                        Score: {result.score}
                                                    </div>
                                                </div>
                                                <p className="text-sm text-slate-300 leading-relaxed pl-2 font-mono">
                                                    {result.text}
                                                </p>
                                            </div>
                                        ))}
                                    </div>
                                )}
                            </div>
                        </div>
                    )}

                    {/* ===== Tab 3: 设置 (占位) ===== */}
                    {activeTab === 'Settings' && (
                        <div className="animate-in fade-in slide-in-from-bottom-4 duration-300 text-center py-20">
                            <span className="material-symbols-outlined text-6xl text-slate-600 mb-4">settings_applications</span>
                            <h3 className="text-xl font-bold text-slate-300 mb-2">Retrieval Settings</h3>
                            <p className="text-sm text-slate-500">Configure Chunk Size, Overlap, and Top-K retrieval parameters here.</p>
                        </div>
                    )}

                </div>
            </main>
        </div>
    );
}