import { useState } from 'react';

// 将组件数据抽象出来，方便以后对接后端
const componentsData = [
    {
        id: 'geoip',
        title: 'Advanced GeoIP',
        icon: 'public',
        color: 'primary',
        version: 'v2.1',
        tag: 'Official',
        desc: 'High-resolution geographic data retrieval with proxy detection and ISP risk scoring.',
        statIcon: '',
        statText: '',
    },
    {
        id: 'biometrics',
        title: 'Behavioral Biometrics',
        icon: 'fingerprint',
        color: 'secondary',
        version: 'v1.4',
        tag: 'Community',
        desc: 'Analyzes typing rhythm, mouse movement, and touch pressure to identify bots.',
        statIcon: 'bolt',
        statText: 'Low Latency',
    },
    {
        id: 'neural',
        title: 'Neural Fraud Score',
        icon: 'psychology',
        color: 'error',
        version: 'v3.0.1',
        tag: 'Official',
        desc: 'Real-time risk scoring engine using recurrent neural networks for sequence analysis.',
        statIcon: 'data_usage',
        statText: 'High Density',
    },
    {
        id: 'rootcheck',
        title: 'Root Check SDK',
        icon: 'developer_mode',
        color: 'tertiary',
        version: 'v4.2',
        tag: 'Official',
        desc: 'Detects jailbroken, rooted, or emulated environments across mobile platforms.',
        statIcon: 'security',
        statText: 'Shielded',
    }
];

export default function ComponentsMarketplace() {
    // 状态：当前选中的组件，为空则收起右侧抽屉
    const [selectedComponent, setSelectedComponent] = useState(componentsData[0]);

    return (
        <div className="flex-1 flex flex-col h-screen overflow-hidden">
            {/* 专属的 Top Header */}
            <header className="flex items-center justify-between px-6 sticky top-0 z-30 bg-slate-900/80 backdrop-blur-md w-full h-16 border-b border-outline-variant/10 font-headline font-semibold shrink-0">
                <div className="flex items-center gap-8 flex-1">
                    <span className="text-lg font-bold text-white tracking-tighter">Components Marketplace</span>
                    <div className="relative w-96 max-w-md bg-surface-container-lowest rounded-lg border border-outline-variant/10">
                        <span className="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-outline text-sm">search</span>
                        <input
                            className="bg-transparent border-none focus:ring-0 text-sm py-2 pl-10 w-full placeholder:text-outline/50 text-white outline-none"
                            placeholder="Search 142 components..."
                            type="text"
                        />
                    </div>
                </div>
                <div className="flex items-center gap-4">
                    <button className="flex items-center gap-2 px-4 py-2 bg-gradient-to-br from-primary to-primary-container text-[#1000a9] font-bold text-sm rounded-lg hover:opacity-90 transition-all active:scale-95 shadow-lg shadow-primary/10">
                        <span className="material-symbols-outlined text-sm">add</span>
                        Create Custom
                    </button>
                </div>
            </header>

            {/* 主体内容区 (左右分栏) */}
            <main className="flex-1 flex overflow-hidden">

                {/* 左侧筛选侧边栏 */}
                <nav className="w-56 flex flex-col p-6 bg-[#060e20] border-r border-outline-variant/5 shrink-0">
                    <h3 className="font-label text-[0.6875rem] uppercase tracking-[0.05em] text-outline mb-6">Filter Marketplace</h3>
                    <ul className="flex flex-col gap-2">
                        {['All Components', 'Networking', 'Device Fingerprint', 'ML Models', 'Integrations'].map((filter, idx) => (
                            <li key={filter}>
                                <button className={`w-full flex items-center gap-3 px-3 py-2 rounded-lg text-sm transition-all ${idx === 0 ? 'bg-surface-container text-primary font-semibold' : 'text-on-surface-variant hover:bg-surface-bright'}`}>
                                    {filter}
                                </button>
                            </li>
                        ))}
                    </ul>
                </nav>

                {/* 中央卡片网格 */}
                <section className="flex-1 overflow-y-auto p-8 bg-[#0b1326] custom-scrollbar">
                    <div className="grid grid-cols-1 xl:grid-cols-2 2xl:grid-cols-3 gap-6">
                        {componentsData.map((comp) => {
                            const isActive = selectedComponent?.id === comp.id;
                            return (
                                <article
                                    key={comp.id}
                                    onClick={() => setSelectedComponent(comp)}
                                    className={`p-5 rounded-xl border transition-all flex flex-col gap-4 cursor-pointer relative group
                    ${isActive
                                            ? 'bg-surface-container-high border-primary/50 ring-1 ring-primary/50 shadow-2xl'
                                            : 'bg-surface-container border-outline-variant/10 hover:bg-surface-container-high'
                                        }`}
                                >
                                    <div className="flex justify-between items-start">
                                        <div className={`w-10 h-10 bg-${comp.color}/10 rounded-lg flex items-center justify-center text-${comp.color}`}>
                                            <span className="material-symbols-outlined">{comp.icon}</span>
                                        </div>
                                        <div className="flex gap-2">
                                            <span className="px-2 py-0.5 bg-surface-bright text-[0.625rem] font-bold rounded text-white">{comp.version}</span>
                                            <span className={`px-2 py-0.5 bg-tertiary-container/20 text-tertiary text-[0.625rem] font-bold rounded flex items-center gap-1`}>
                                                {comp.tag === 'Official' && <span className="w-1 h-1 bg-tertiary rounded-full"></span>}
                                                {comp.tag}
                                            </span>
                                        </div>
                                    </div>
                                    <div>
                                        <h4 className="font-headline font-bold text-lg text-white mb-1">{comp.title}</h4>
                                        <p className="text-sm text-outline leading-relaxed line-clamp-2">{comp.desc}</p>
                                    </div>
                                    <div className="mt-auto flex items-center justify-between">
                                        {comp.statIcon ? (
                                            <div className="flex gap-2 items-center">
                                                <span className={`material-symbols-outlined text-sm text-${comp.color}`}>{comp.statIcon}</span>
                                                <span className="text-[10px] text-outline">{comp.statText}</span>
                                            </div>
                                        ) : (
                                            <div className="flex -space-x-2">
                                                <div className="w-6 h-6 rounded-full bg-slate-700 flex items-center justify-center text-[10px] text-white border-2 border-surface-container-high">JD</div>
                                                <div className="w-6 h-6 rounded-full bg-indigo-700 flex items-center justify-center text-[10px] text-white border-2 border-surface-container-high">MK</div>
                                            </div>
                                        )}
                                        <button className="text-sm font-bold text-primary px-3 py-1.5 rounded-lg hover:bg-primary/10 transition-colors">Configure</button>
                                    </div>
                                </article>
                            )
                        })}
                    </div>
                </section>

                {/* 右侧滑出抽屉 (Detail Panel) */}
                {selectedComponent && (
                    <aside className="w-96 glass-effect bg-surface-container-highest/95 border-l border-outline-variant/10 flex flex-col shrink-0 shadow-2xl z-20 animate-in slide-in-from-right-10 duration-200">
                        <div className="p-6 border-b border-outline-variant/10 flex items-center justify-between">
                            <h2 className="font-headline font-extrabold text-xl text-white">{selectedComponent.title}</h2>
                            <span
                                onClick={() => setSelectedComponent(null)}
                                className="material-symbols-outlined text-outline cursor-pointer hover:text-white transition-colors"
                            >
                                close
                            </span>
                        </div>

                        <div className="flex-1 overflow-y-auto p-6 space-y-8 custom-scrollbar">
                            <section>
                                <h3 className="font-label text-[0.6875rem] uppercase tracking-[0.05em] text-outline mb-3">Module Readme</h3>
                                <div className="p-4 bg-surface-container-lowest rounded-lg border border-outline-variant/5">
                                    <p className="text-xs text-on-surface-variant leading-relaxed font-mono">
                                        {selectedComponent.desc} Ready for sub-100ms high-throughput processing.
                                    </p>
                                </div>
                            </section>

                            <section>
                                <h3 className="font-label text-[0.6875rem] uppercase tracking-[0.05em] text-outline mb-4">Port Schema</h3>
                                <div className="space-y-4">
                                    <div className="relative p-3 bg-surface-container-low rounded-lg border-l-2 border-primary">
                                        <div className="text-[10px] text-primary font-bold uppercase mb-1">Input Schema</div>
                                        <pre className="text-[10px] text-on-surface-variant font-mono">{`{ "ip": "string", "ua": "string" }`}</pre>
                                    </div>
                                    <div className="flex justify-center py-1">
                                        <div className="h-6 w-px bg-gradient-to-b from-primary to-tertiary opacity-30"></div>
                                    </div>
                                    <div className="relative p-3 bg-surface-container-low rounded-lg border-l-2 border-tertiary">
                                        <div className="text-[10px] text-tertiary font-bold uppercase mb-1">Output Schema</div>
                                        <pre className="text-[10px] text-on-surface-variant font-mono">{`{ "risk_score": 0.12, "loc": "NYC" }`}</pre>
                                    </div>
                                </div>
                            </section>

                            <section>
                                <div className="grid grid-cols-2 gap-4">
                                    <div className="p-4 bg-[#171f33] rounded-xl border border-[#2d3449]">
                                        <div className="text-[10px] text-outline mb-1">Avg Latency</div>
                                        <div className="text-2xl font-bold font-headline text-tertiary">12<span className="text-sm font-normal">ms</span></div>
                                    </div>
                                    <div className="p-4 bg-[#171f33] rounded-xl border border-[#2d3449]">
                                        <div className="text-[10px] text-outline mb-1">Throughput</div>
                                        <div className="text-2xl font-bold font-headline text-white">8.4k<span className="text-xs font-normal text-outline">/s</span></div>
                                    </div>
                                </div>
                            </section>

                            <button className="w-full py-3 bg-primary text-[#1000a9] font-bold rounded-xl shadow-xl shadow-primary/20 hover:scale-[1.02] active:scale-95 transition-all">
                                Install to Pipeline
                            </button>
                        </div>
                    </aside>
                )}
            </main>
        </div>
    );
}