import { Handle, Position } from '@xyflow/react';

// 这是一个通用的风控节点组件，根据传入的 data 渲染不同颜色和内容
export default function RiskNode({ data, selected }) {
    // 根据不同的节点类型，定义它的主题色
    const theme = {
        trigger: { bg: 'bg-primary-container/20', border: 'border-primary', text: 'text-primary' },
        check: { bg: 'bg-surface-bright', border: 'border-outline-variant/20', text: 'text-tertiary' },
        logic: { bg: 'bg-surface-bright', border: 'border-outline-variant/20', text: 'text-outline' },
        action: { bg: 'bg-error-container/20', border: 'border-error/30', text: 'text-error' }
    }[data.type] || theme.check;

    return (
        <div className={`w-64 bg-[#171f33] border-2 rounded-xl shadow-xl overflow-hidden transition-all duration-200 
      ${selected ? `border-[#8083ff] shadow-[0_0_20px_rgba(128,131,255,0.2)] ring-4 ring-[#8083ff]/10` : theme.border}`}>

            {/* 左侧输入点 (如果不是触发器节点就有输入) */}
            {data.type !== 'trigger' && (
                <Handle type="target" position={Position.Left} className="w-3 h-3 bg-surface border-2 border-outline rounded-full" />
            )}

            {/* 节点头部 */}
            <div className={`${theme.bg} px-4 py-2 flex items-center justify-between border-b ${theme.border}`}>
                <div className="flex items-center gap-2">
                    <span className={`material-symbols-outlined text-sm ${theme.text}`}>{data.icon}</span>
                    <span className={`text-[10px] font-bold uppercase tracking-widest ${theme.text}`}>{data.label}</span>
                </div>
                {selected && <div className={`w-2 h-2 rounded-full ${theme.text.replace('text-', 'bg-')} animate-pulse`}></div>}
            </div>

            {/* 节点内容 */}
            <div className="p-4">
                {data.content}
            </div>

            {/* 右侧输出点 (可以有多个，比如判断节点的 If/Else) */}
            {data.handles?.map((handle, index) => (
                <Handle
                    key={handle.id}
                    type="source"
                    position={Position.Right}
                    id={handle.id}
                    style={{ top: handle.top || '50%' }}
                    className="w-3 h-3 bg-[#8083ff] border-2 border-surface rounded-full"
                />
            ))}
        </div>
    );
}