import { CONFIG_TABS, TAB_ICONS } from './configuration.constants';

export default function ConfigurationSidebar({ activeTab, onTabChange }) {
    return (
        <nav className="w-64 flex flex-col p-6 bg-[#060e20] border-r border-outline-variant/5 shrink-0">
            <h3 className="font-label text-[0.6875rem] uppercase tracking-[0.05em] text-outline mb-6">Settings Menu</h3>
            <ul className="flex flex-col gap-2">
                {CONFIG_TABS.map((tab) => {
                    const isActive = activeTab === tab;

                    return (
                        <li key={tab}>
                            <button
                                onClick={() => onTabChange(tab)}
                                className={`w-full flex items-center gap-3 px-4 py-3 rounded-lg text-sm transition-all duration-200 ${isActive
                                    ? 'bg-surface-container text-[#c0c1ff] font-bold shadow-md shadow-[#c0c1ff]/5'
                                    : 'text-on-surface-variant hover:bg-surface-bright hover:text-white'
                                    }`}
                            >
                                <span className="material-symbols-outlined text-lg">{TAB_ICONS[tab]}</span>
                                {tab}
                            </button>
                        </li>
                    );
                })}
            </ul>
        </nav>
    );
}
