export default function DashboardContent() {
    const logs = [
        { time: '2023-10-24 14:22:01', user: 'usr_992182', rule: 'Geo_Fencing_Reject', status: 'Blocked', statusColor: 'error' },
        { time: '2023-10-24 14:21:48', user: 'usr_102234', rule: 'Standard_Checkout', status: 'Passed', statusColor: 'tertiary' },
        { time: '2023-10-24 14:20:12', user: 'usr_882011', rule: 'Velocity_Freq_Limit', status: 'Flagged', statusColor: 'secondary' },
        { time: '2023-10-24 14:19:55', user: 'usr_331092', rule: 'Standard_Checkout', status: 'Passed', statusColor: 'tertiary' },
    ];

    return (
        <main className="flex-1 p-8 min-h-screen bg-[#0b1326] overflow-y-auto custom-scrollbar">
            {/* Header Section */}
            <div className="flex items-end justify-between mb-8">
                <div>
                    <span className="text-[10px] font-bold uppercase tracking-[0.2em] text-primary/80 mb-1 block">Live Dashboard</span>
                    <h2 className="text-3xl font-headline font-extrabold tracking-tight text-on-surface">System Overview</h2>
                </div>
                <div className="flex gap-3">
                    <div className="flex items-center gap-2 px-4 py-2 bg-surface-container rounded-lg border border-outline-variant/10">
                        <div className="w-2 h-2 bg-tertiary rounded-full animate-pulse"></div>
                        <span className="text-xs font-semibold text-tertiary">Real-time Node: Active</span>
                    </div>
                </div>
            </div>

            {/* Metrics Row */}
            <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
                <div className="bg-surface-container p-6 rounded-xl relative overflow-hidden group">
                    <div className="absolute top-0 right-0 p-4 opacity-10 group-hover:opacity-20 transition-opacity">
                        <span className="material-symbols-outlined text-5xl">analytics</span>
                    </div>
                    <p className="text-xs font-bold text-slate-400 uppercase tracking-wider mb-2">Total Requests</p>
                    <div className="flex items-baseline gap-2">
                        <h3 className="text-3xl font-headline font-bold text-on-surface">1,284,032</h3>
                        <span className="text-xs text-tertiary font-bold">+12.4%</span>
                    </div>
                </div>

                <div className="bg-surface-container p-6 rounded-xl relative overflow-hidden group">
                    <div className="absolute top-0 right-0 p-4 opacity-10 group-hover:opacity-20 transition-opacity">
                        <span className="material-symbols-outlined text-5xl text-tertiary">check_circle</span>
                    </div>
                    <p className="text-xs font-bold text-slate-400 uppercase tracking-wider mb-2">Safe Traffic</p>
                    <div className="flex items-baseline gap-2">
                        <h3 className="text-3xl font-headline font-bold text-tertiary">98.2%</h3>
                        <span className="text-xs text-slate-500 font-medium">Standard baseline</span>
                    </div>
                </div>

                <div className="bg-surface-container p-6 rounded-xl relative overflow-hidden group border-b-2 border-error/20">
                    <div className="absolute top-0 right-0 p-4 opacity-10 group-hover:opacity-20 transition-opacity">
                        <span className="material-symbols-outlined text-5xl text-error">security</span>
                    </div>
                    <p className="text-xs font-bold text-slate-400 uppercase tracking-wider mb-2">Threats Blocked</p>
                    <div className="flex items-baseline gap-2">
                        <h3 className="text-3xl font-headline font-bold text-error">24,912</h3>
                        <span className="text-xs text-error font-bold">+3.1%</span>
                    </div>
                </div>
            </div>

            {/* Workflow Visualization */}
            <div className="mb-8 bg-surface-container-low rounded-xl p-8 border border-outline-variant/10">
                <div className="flex items-center justify-between mb-10">
                    <div>
                        <h4 className="text-lg font-headline font-bold text-on-surface">Active Rule Workflow</h4>
                        <p className="text-sm text-slate-500">Node-based execution path for incoming traffic</p>
                    </div>
                    <button className="text-xs font-bold text-primary hover:underline">Edit Flow</button>
                </div>
                <div className="flex items-center justify-center gap-4 py-12">
                    <div className="flex flex-col items-center gap-3">
                        <div className="w-16 h-16 rounded-full bg-surface-container-highest flex items-center justify-center border-2 border-primary/20 shadow-[0_0_20px_rgba(192,193,255,0.1)]">
                            <span className="material-symbols-outlined text-primary">input</span>
                        </div>
                        <span className="text-xs font-bold text-on-surface">Source</span>
                    </div>
                    <div className="w-20 h-[2px] bg-gradient-to-r from-primary/40 to-primary-container"></div>

                    <div className="flex flex-col items-center gap-3">
                        <div className="px-6 py-4 rounded-xl bg-primary-container/10 border border-primary/40 flex items-center gap-3 shadow-xl">
                            <span className="material-symbols-outlined text-primary">speed</span>
                            <div>
                                <p className="text-[10px] font-bold text-primary uppercase">Middleware</p>
                                <p className="text-sm font-bold text-on-surface">Velocity Check</p>
                            </div>
                        </div>
                    </div>
                    <div className="w-20 h-[2px] bg-gradient-to-r from-primary-container to-error/40"></div>

                    <div className="flex flex-col items-center gap-3">
                        <div className="w-16 h-16 rounded-full bg-error-container/20 flex items-center justify-center border-2 border-error/40 shadow-[0_0_20px_rgba(255,180,171,0.1)]">
                            <span className="material-symbols-outlined text-error">block</span>
                        </div>
                        <span className="text-xs font-bold text-error">Block Action</span>
                    </div>
                </div>
            </div>

            {/* Bottom Row */}
            <div className="grid grid-cols-1 lg:grid-cols-12 gap-8">
                {/* Activity Logs */}
                <div className="lg:col-span-8 bg-surface-container rounded-xl overflow-hidden">
                    <div className="px-6 py-5 border-b border-outline-variant/10 flex items-center justify-between">
                        <h4 className="font-headline font-bold text-on-surface">Recent Activity Logs</h4>
                        <button className="text-xs font-bold text-slate-400 hover:text-on-surface transition-colors">View All Logs</button>
                    </div>
                    <div className="overflow-x-auto">
                        <table className="w-full text-left">
                            <thead>
                                <tr className="bg-surface-container-high/30">
                                    <th className="px-6 py-3 text-[10px] font-bold text-slate-500 uppercase tracking-widest">Timestamp</th>
                                    <th className="px-6 py-3 text-[10px] font-bold text-slate-500 uppercase tracking-widest">UserID</th>
                                    <th className="px-6 py-3 text-[10px] font-bold text-slate-500 uppercase tracking-widest">Triggered Rule</th>
                                    <th className="px-6 py-3 text-[10px] font-bold text-slate-500 uppercase tracking-widest">Status</th>
                                </tr>
                            </thead>
                            <tbody className="divide-y divide-outline-variant/5">
                                {logs.map((log, i) => (
                                    <tr key={i} className="hover:bg-surface-bright transition-colors">
                                        <td className="px-6 py-4 text-xs font-medium text-slate-400">{log.time}</td>
                                        <td className="px-6 py-4 text-sm font-bold text-on-surface">{log.user}</td>
                                        <td className="px-6 py-4 text-xs font-semibold text-primary">{log.rule}</td>
                                        <td className="px-6 py-4">
                                            <span className={`px-2 py-1 bg-${log.statusColor}-container/30 text-${log.statusColor} text-[10px] font-bold rounded uppercase`}>
                                                {log.status}
                                            </span>
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                </div>

                {/* Library Previews */}
                <div className="lg:col-span-4 flex flex-col gap-6">
                    <div className="bg-surface-container rounded-xl p-6">
                        <div className="flex items-center justify-between mb-6">
                            <h4 className="font-headline font-bold text-on-surface">Library Previews</h4>
                            <span className="material-symbols-outlined text-slate-500">grid_view</span>
                        </div>
                        <div className="space-y-4">
                            <div className="p-4 bg-surface-container-high rounded-lg border border-outline-variant/10 hover:border-primary/40 transition-all cursor-pointer group">
                                <div className="flex items-start justify-between mb-3">
                                    <div className="w-10 h-10 rounded bg-primary/10 flex items-center justify-center text-primary">
                                        <span className="material-symbols-outlined">public</span>
                                    </div>
                                    <span className="material-symbols-outlined text-slate-500 text-sm opacity-0 group-hover:opacity-100 transition-opacity">open_in_new</span>
                                </div>
                                <h5 className="font-bold text-sm text-on-surface mb-1">IP Blacklist</h5>
                                <p className="text-xs text-slate-500">Block high-risk autonomous systems and proxies.</p>
                                <div className="mt-4 flex gap-2">
                                    <span className="text-[9px] px-2 py-0.5 bg-surface-dim rounded border border-outline-variant/10 text-slate-400 uppercase">Networking</span>
                                    <span className="text-[9px] px-2 py-0.5 bg-surface-dim rounded border border-outline-variant/10 text-slate-400 uppercase">Core</span>
                                </div>
                            </div>

                            <div className="p-4 bg-surface-container-high rounded-lg border border-outline-variant/10 hover:border-primary/40 transition-all cursor-pointer group">
                                <div className="flex items-start justify-between mb-3">
                                    <div className="w-10 h-10 rounded bg-tertiary/10 flex items-center justify-center text-tertiary">
                                        <span className="material-symbols-outlined">fingerprint</span>
                                    </div>
                                    <span className="material-symbols-outlined text-slate-500 text-sm opacity-0 group-hover:opacity-100 transition-opacity">open_in_new</span>
                                </div>
                                <h5 className="font-bold text-sm text-on-surface mb-1">Device Fingerprint</h5>
                                <p className="text-xs text-slate-500">Analyze browser headers and hardware IDs.</p>
                                <div className="mt-4 flex gap-2">
                                    <span className="text-[9px] px-2 py-0.5 bg-surface-dim rounded border border-outline-variant/10 text-slate-400 uppercase">Hardware</span>
                                    <span className="text-[9px] px-2 py-0.5 bg-surface-dim rounded border border-outline-variant/10 text-slate-400 uppercase">Auth</span>
                                </div>
                            </div>
                        </div>
                        <button className="w-full mt-6 py-3 border border-outline-variant/20 rounded-lg text-xs font-bold text-slate-400 hover:text-on-surface hover:bg-surface-bright transition-all">
                            Browse Component Store
                        </button>
                    </div>
                </div>
            </div>
        </main>
    );
}