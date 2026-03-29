import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';

// 模拟的知识库列表 (Datasets)
const mockVaultSpaces = [
    {
        id: 'kb_fraud',
        name: 'Anti-Fraud Policies',
        description: 'Core rules, historical fraud patterns, and device fingerprint specifications for the RiskFlow engine.',
        icon: 'shield_person',
        color: 'rose',      // 警示红
        docsCount: 12,
        chunksCount: '4.5k',
        status: 'Active',
        lastUpdated: '2 hours ago'
    },
    {
        id: 'kb_compliance',
        name: 'Compliance & AML',
        description: 'Anti-Money Laundering regulations, cross-border transaction limits, and KYC guidelines.',
        icon: 'account_balance',
        color: 'indigo',    // 稳重蓝
        docsCount: 3,
        chunksCount: '1.2k',
        status: 'Active',
        lastUpdated: '1 day ago'
    },
    {
        id: 'kb_threat_intel',
        name: 'Threat Intel (2026 Q1)',
        description: 'Global blacklisted IPs, botnet signatures, and zero-day exploit indicators.',
        icon: 'bug_report',
        color: 'amber',     // 警告黄
        docsCount: 1,
        chunksCount: 'Syncing...',
        status: 'Syncing',
        lastUpdated: 'Just now'
    }
];

export default function KnowledgeVaultHub() {
    const navigate = useNavigate();
    const [searchQuery, setSearchQuery] = useState('');

    // 状态徽章渲染器
    const renderStatusBadge = (status) => {
        if (status === 'Active') {
            return (
                <span className="flex items-center gap-1.5 px-2 py-0.5 rounded text-[10px] font-bold uppercase tracking-wider bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">
                    <span className="w-1.5 h-1.5 rounded-full bg-emerald-400"></span>
                    Active
                </span>
            );
        }
        return (
            <span className="flex items-center gap-1.5 px-2 py-0.5 rounded text-[10px] font-bold uppercase tracking-wider bg-amber-500/10 text-amber-400 border border-amber-500/20">
                <span className="w-1.5 h-1.5 rounded-full bg-amber-400 animate-pulse"></span>
                Syncing
            </span>
        );
    };

    return (
        <div className="flex-1 flex flex-col h-screen overflow-hidden bg-[#070c1a] text-white font-headline antialiased">
            
            {/* 顶部 Header 区 */}
            <header className="flex items-center justify-between px-8 py-6 shrink-0 bg-[#0b1326]/80 backdrop-blur-md border-b border-slate-800/50 sticky top-0 z-30">
                <div>
                    <h2 className="text-2xl font-bold text-white tracking-tighter flex items-center gap-3">
                        <span className="material-symbols-outlined text-[#c0c1ff] text-3xl">database</span>
                        Knowledge Base Hub
                    </h2>
                    <p className="text-sm text-slate-500 mt-1">Manage vector datasets for LangChain4j RAG agents.</p>
                </div>
                
                <div className="flex items-center gap-4">
                    {/* 搜索框 */}
                    <div className="relative w-64 group">
                        <span className="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-slate-500 group-focus-within:text-[#c0c1ff] transition-colors text-sm">search</span>
                        <input
                            type="text"
                            placeholder="Search databases..."
                            value={searchQuery}
                            onChange={(e) => setSearchQuery(e.target.value)}
                            className="w-full bg-[#101a31] border border-slate-800 focus:border-[#c0c1ff]/50 rounded-lg py-2 pl-9 pr-4 text-sm text-white placeholder-slate-500 outline-none transition-all shadow-inner"
                        />
                    </div>
                    {/* 新建按钮 */}
                    <button className="flex items-center gap-2 px-4 py-2 bg-[#222a3d] hover:bg-[#c0c1ff] text-[#c0c1ff] hover:text-[#0b1326] font-bold text-sm rounded-lg transition-all duration-300 shadow-lg">
                        <span className="material-symbols-outlined text-sm">add</span>
                        Create Knowledge Base
                    </button>
                </div>
            </header>

            {/* 主内容区：卡片网格 */}
            <main className="flex-1 overflow-y-auto p-8 custom-scrollbar">
                <div className="max-w-7xl mx-auto">
                    
                    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
                        {mockVaultSpaces.map((space) => (
                            <div 
                                key={space.id}
                                // 点击卡片，路由跳转到知识库详情页 (带上 ID)
                                onClick={() => navigate(`/knowledge-base/${space.id}`)}
                                className="group flex flex-col bg-[#0b1326] border border-slate-800/80 rounded-2xl p-6 hover:bg-[#101a31] hover:border-[#c0c1ff]/30 cursor-pointer transition-all duration-300 hover:-translate-y-1 shadow-lg shadow-black/20 hover:shadow-[#c0c1ff]/5 relative overflow-hidden"
                            >
                                {/* 顶部微妙的高亮渐变线 */}
                                <div className="absolute top-0 left-0 w-full h-[2px] bg-gradient-to-r from-transparent via-slate-700 group-hover:via-[#c0c1ff]/50 to-transparent transition-all duration-500"></div>

                                {/* Header: Icon + Status */}
                                <div className="flex justify-between items-start mb-4">
                                    <div className={`w-12 h-12 rounded-xl bg-${space.color}-500/10 flex items-center justify-center text-${space.color}-400 border border-${space.color}-500/20 group-hover:scale-110 transition-transform duration-300`}>
                                        <span className="material-symbols-outlined text-2xl">{space.icon}</span>
                                    </div>
                                    {renderStatusBadge(space.status)}
                                </div>

                                {/* Body: Title + Desc */}
                                <div className="mb-6 flex-1">
                                    <h3 className="text-lg font-bold text-slate-200 group-hover:text-white mb-2 tracking-tight transition-colors">
                                        {space.name}
                                    </h3>
                                    <p className="text-sm text-slate-500 leading-relaxed line-clamp-2">
                                        {space.description}
                                    </p>
                                </div>

                                {/* Footer: Stats */}
                                <div className="pt-4 border-t border-slate-800/80 flex items-center justify-between text-slate-400">
                                    <div className="flex gap-4">
                                        <div className="flex flex-col">
                                            <span className="text-[10px] uppercase tracking-wider mb-0.5 opacity-60">Docs</span>
                                            <span className="text-sm font-mono font-bold text-slate-300 group-hover:text-[#c0c1ff] transition-colors">{space.docsCount}</span>
                                        </div>
                                        <div className="flex flex-col">
                                            <span className="text-[10px] uppercase tracking-wider mb-0.5 opacity-60">Chunks</span>
                                            <span className="text-sm font-mono font-bold text-slate-300 group-hover:text-[#c0c1ff] transition-colors">{space.chunksCount}</span>
                                        </div>
                                    </div>
                                    
                                    <button className="w-8 h-8 rounded-full flex items-center justify-center hover:bg-slate-800 text-slate-500 hover:text-white transition-colors opacity-0 group-hover:opacity-100">
                                        <span className="material-symbols-outlined text-[18px]">more_vert</span>
                                    </button>
                                </div>
                            </div>
                        ))}
                    </div>

                </div>
            </main>
        </div>
    );
}