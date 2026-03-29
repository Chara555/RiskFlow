// src/pages/Configuration.jsx
import { useState } from 'react';
import ConfigurationSidebar from '../components/configuration/ConfigurationSidebar';

export default function Configuration() {
    // 默认选中的 Tab 改为系统的其它配置项
    const [activeTab, setActiveTab] = useState('Threat Lists');

    return (
        <div className="flex-1 flex flex-col h-screen overflow-hidden bg-[#0b1326] text-white font-headline antialiased">
            <header className="flex items-center justify-between px-6 sticky top-0 z-30 bg-slate-900/80 backdrop-blur-md w-full h-16 border-b border-outline-variant/10 font-semibold shrink-0">
                <div className="flex items-center gap-8 flex-1">
                    <span className="text-lg font-bold text-white tracking-tighter">System Configuration</span>
                </div>
                <div className="flex items-center gap-4">
                    <button className="flex items-center gap-2 px-5 py-2 bg-gradient-to-br from-primary to-primary-container text-[#1000a9] font-bold text-sm rounded-lg hover:opacity-90 transition-all active:scale-95 shadow-lg shadow-primary/10">
                        <span className="material-symbols-outlined text-sm">save</span>
                        Save Changes
                    </button>
                </div>
            </header>

            <main className="flex-1 flex overflow-hidden">
                {/* 记得在你的 ConfigurationSidebar 里把 'AI Knowledge Vault' 这个选项删掉 */}
                <ConfigurationSidebar activeTab={activeTab} onTabChange={setActiveTab} />

                <section className="flex-1 overflow-y-auto p-8 lg:p-12 custom-scrollbar">
                    {/* 纯净的配置页面占位符 */}
                    <div className="flex flex-col items-center justify-center h-full text-outline animate-in zoom-in-95 duration-200">
                        <span className="material-symbols-outlined text-6xl mb-4 opacity-20">construction</span>
                        <h3 className="text-xl font-bold text-white mb-2">{activeTab}</h3>
                        <p className="text-sm">This configuration module is under construction.</p>
                    </div>
                </section>
            </main>
        </div>
    );
}