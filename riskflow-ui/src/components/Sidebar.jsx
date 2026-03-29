import React from 'react';
import { NavLink } from 'react-router-dom';

// 1. 将 Knowledge Base 作为一级菜单加入导航数组
const navItems = [
    { name: 'Overview', icon: 'dashboard', path: '/' },
    { name: 'Workflows', icon: 'account_tree', path: '/workflows' },
    { name: 'Components', icon: 'extension', path: '/components' },
    { name: 'Logs', icon: 'receipt_long', path: '/logs' },
    { name: 'Knowledge Base', icon: 'database', path: '/knowledge-base' }, // 新增知识库
    { name: 'Configuration', icon: 'settings', path: '/configuration' },
];

export default function Sidebar() {
    return (
        <aside className="fixed left-0 top-0 z-40 flex flex-col py-6 px-4 bg-[#0b1326] h-screen w-64 border-r border-slate-800/50 shadow-2xl shadow-black/20 font-headline antialiased tracking-tight">
            
            {/* 顶部 Logo 区 (固定不缩放) */}
            <div className="mb-10 px-4 shrink-0">
                <h1 className="text-xl font-bold tracking-tighter text-[#c0c1ff]">RiskFlow</h1>
                <p className="text-xs text-slate-500 font-medium">Risk Management</p>
            </div>

            {/* 中间导航区域 (极简、统一的 UI) */}
            <div className="flex-1 flex flex-col overflow-y-auto custom-scrollbar pr-1 space-y-2">
                {navItems.map((item) => (
                    <NavLink
                        key={item.name}
                        to={item.path}
                        className={({ isActive }) =>
                            `w-full flex items-center gap-3 px-4 py-3 rounded-lg font-semibold transition-all duration-200 shrink-0 ${
                                isActive
                                    ? 'text-[#c0c1ff] bg-[#222a3d] scale-[0.98]'
                                    : 'text-slate-400 hover:text-slate-100 hover:bg-[#171f33]'
                            }`
                        }
                    >
                        <span className="material-symbols-outlined">{item.icon}</span>
                        <span>{item.name}</span>
                    </NavLink>
                ))}
            </div>

            {/* 底部按钮区 (固定在底部) */}
            <div className="mt-4 px-2 shrink-0">
                <button className="w-full py-3 px-4 bg-gradient-to-br from-primary to-primary-container text-on-primary rounded-xl font-bold text-sm shadow-lg shadow-primary/20 flex items-center justify-center gap-2 hover:opacity-90 transition-opacity">
                    <span className="material-symbols-outlined text-sm">add</span>
                    New Analysis
                </button>
            </div>

        </aside>
    );
}