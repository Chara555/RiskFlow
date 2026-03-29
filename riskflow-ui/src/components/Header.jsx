export default function Header() {
    return (
        <header className="sticky top-0 right-0 z-30 flex items-center justify-between px-8 ml-64 w-[calc(100%-16rem)] bg-[#0b1326]/80 backdrop-blur-xl h-16 font-body text-sm font-medium">
            <div className="flex items-center flex-1 max-w-xl">
                <div className="relative w-full group">
                    <span className="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-slate-400 text-sm">search</span>
                    <input
                        type="text"
                        placeholder="Global search..."
                        className="w-full bg-surface-container-lowest border-none rounded-full pl-10 pr-4 py-2 text-on-surface focus:ring-1 focus:ring-[#6366F1]/50 placeholder-slate-500 outline-none"
                    />
                </div>
            </div>
            <div className="flex items-center gap-6">
                <button className="relative p-2 text-slate-400 hover:bg-slate-800/50 rounded-full transition-all">
                    <span className="material-symbols-outlined">notifications</span>
                    <span className="absolute top-1.5 right-1.5 w-2 h-2 bg-error rounded-full"></span>
                </button>
                <div className="flex items-center gap-3 pl-4 border-l border-outline-variant/20">
                    <div className="text-right">
                        <p className="text-xs font-bold text-on-surface leading-tight">Analyst One</p>
                        <p className="text-[10px] text-slate-500 uppercase tracking-widest">Administrator</p>
                    </div>
                    <img
                        src="https://api.dicebear.com/7.x/avataaars/svg?seed=RiskFlow"
                        alt="User avatar"
                        className="w-8 h-8 rounded-full border border-primary/20 bg-slate-800"
                    />
                </div>
            </div>
        </header>
    );
}