import { useMemo, useState } from 'react';

const STATUS_CLASS_MAP = {
    Vectorized: 'bg-emerald-500/10 text-emerald-400 border-emerald-500/20',
    'Embedding...': 'bg-amber-500/10 text-amber-400 border-amber-500/20 animate-pulse',
};

const DOC_ACCENT_CLASS_MAP = {
    emerald: 'bg-emerald-500/10 text-emerald-400',
    amber: 'bg-amber-500/10 text-amber-400',
};

function getFileIcon(name) {
    const lowerName = name.toLowerCase();

    if (lowerName.endsWith('.pdf')) {
        return 'picture_as_pdf';
    }

    if (lowerName.endsWith('.csv')) {
        return 'csv';
    }

    return 'description';
}

function KnowledgeVaultDocumentRow({ doc, onDelete, onStatusChange }) {
    return (
        <div className="p-4 rounded-xl border border-outline-variant/10 bg-surface-container hover:bg-surface-container-high transition-colors flex items-center justify-between group">
            <div className="flex items-center gap-4 min-w-0">
                <div className={`w-10 h-10 rounded-lg flex items-center justify-center ${DOC_ACCENT_CLASS_MAP[doc.color] ?? DOC_ACCENT_CLASS_MAP.emerald}`}>
                    <span className="material-symbols-outlined">{getFileIcon(doc.name)}</span>
                </div>
                <div className="min-w-0">
                    <div className="font-bold text-sm text-white mb-0.5 group-hover:text-primary transition-colors cursor-pointer truncate">{doc.name}</div>
                    <div className="text-[10px] text-on-surface-variant font-mono">{doc.size} - Uploaded {doc.uploadTime}</div>
                </div>
            </div>

            <div className="flex items-center gap-3">
                {doc.status === 'Embedding...' && (
                    <div className="w-24 h-1.5 bg-surface-container-highest rounded-full overflow-hidden hidden lg:block">
                        <div className="h-full bg-amber-500 animate-pulse" style={{ width: `${doc.progress ?? 0}%` }}></div>
                    </div>
                )}

                <select
                    className="bg-[#060e20] border border-outline-variant/20 rounded px-2 py-1 text-[10px] uppercase tracking-wider font-bold text-white outline-none"
                    value={doc.status}
                    onChange={(event) => onStatusChange(doc.id, event.target.value)}
                >
                    <option value="Vectorized">Vectorized</option>
                    <option value="Embedding...">Embedding...</option>
                </select>

                <span className={`px-2.5 py-1 rounded text-[10px] font-bold uppercase tracking-wider border ${STATUS_CLASS_MAP[doc.status]}`}>
                    {doc.status} {doc.chunks ? `(${doc.chunks} Chunks)` : `${doc.progress ?? 0}%`}
                </span>

                <button
                    className="text-outline hover:text-rose-400 transition-colors"
                    onClick={() => onDelete(doc.id)}
                >
                    <span className="material-symbols-outlined text-lg">delete</span>
                </button>
            </div>
        </div>
    );
}

export default function KnowledgeVaultContent({ activeVaultUser, onActiveVaultDocsChange }) {
    const [newFileName, setNewFileName] = useState('');
    const [newFileType, setNewFileType] = useState('pdf');

    const docs = activeVaultUser?.docs ?? [];

    const vaultStats = useMemo(() => {
        const vectorizedCount = docs.filter((doc) => doc.status === 'Vectorized').length;
        return {
            total: docs.length,
            vectorized: vectorizedCount,
            embedding: docs.length - vectorizedCount,
        };
    }, [docs]);

    const handleAddDocument = () => {
        const trimmedName = newFileName.trim();
        if (!trimmedName) {
            return;
        }

        const safeName = trimmedName.includes('.') ? trimmedName : `${trimmedName}.${newFileType}`;
        const nowId = `doc-${Date.now()}`;

        onActiveVaultDocsChange((prevDocs) => [
            {
                id: nowId,
                name: safeName,
                size: '1.0 MB',
                uploadTime: 'Just now',
                status: 'Embedding...',
                progress: 0,
                color: 'amber',
            },
            ...prevDocs,
        ]);

        setNewFileName('');
    };

    const handleDeleteDocument = (id) => {
        onActiveVaultDocsChange((prevDocs) => prevDocs.filter((doc) => doc.id !== id));
    };

    const handleStatusChange = (id, nextStatus) => {
        onActiveVaultDocsChange((prevDocs) =>
            prevDocs.map((doc) => {
                if (doc.id !== id) {
                    return doc;
                }

                if (nextStatus === 'Vectorized') {
                    return {
                        ...doc,
                        status: nextStatus,
                        chunks: doc.chunks ?? Math.floor(Math.random() * 120) + 30,
                        color: 'emerald',
                    };
                }

                return {
                    ...doc,
                    status: nextStatus,
                    progress: doc.progress ?? 35,
                    chunks: undefined,
                    color: 'amber',
                };
            })
        );
    };

    if (!activeVaultUser) {
        return (
            <div className="flex h-full items-center justify-center text-outline">
                No vault user selected.
            </div>
        );
    }

    return (
        <div className="max-w-5xl animate-in fade-in duration-300">
            <div className="mb-8 flex items-end justify-between gap-4">
                <div>
                    <h2 className="text-3xl font-extrabold tracking-tighter mb-2 text-white">AI Knowledge Vault</h2>
                    <p className="text-sm text-outline">Manage user-specific RAG knowledge files for incident review.</p>
                </div>
                <div className="px-4 py-2 rounded-lg border border-outline-variant/20 bg-surface-container-low text-right">
                    <div className="text-xs text-outline">Active User</div>
                    <div className="text-sm font-bold text-white">{activeVaultUser.name}</div>
                    <div className="text-[10px] text-on-surface-variant">{activeVaultUser.role}</div>
                </div>
            </div>

            <div className="mb-6 grid grid-cols-3 gap-3">
                <div className="rounded-lg bg-surface-container p-3 border border-outline-variant/10">
                    <div className="text-[10px] uppercase text-outline mb-1">Total Files</div>
                    <div className="text-xl font-bold text-white">{vaultStats.total}</div>
                </div>
                <div className="rounded-lg bg-surface-container p-3 border border-outline-variant/10">
                    <div className="text-[10px] uppercase text-outline mb-1">Vectorized</div>
                    <div className="text-xl font-bold text-emerald-400">{vaultStats.vectorized}</div>
                </div>
                <div className="rounded-lg bg-surface-container p-3 border border-outline-variant/10">
                    <div className="text-[10px] uppercase text-outline mb-1">Embedding</div>
                    <div className="text-xl font-bold text-amber-400">{vaultStats.embedding}</div>
                </div>
            </div>

            <div className="mb-8 w-full rounded-2xl border-2 border-dashed border-outline-variant/30 bg-surface-container-lowest/50 hover:bg-surface-container-low/50 hover:border-primary/50 transition-all duration-300 p-6">
                <div className="flex flex-col lg:flex-row items-start lg:items-center gap-4">
                    <div className="w-14 h-14 rounded-full bg-surface-container flex items-center justify-center shrink-0">
                        <span className="material-symbols-outlined text-2xl text-outline">cloud_upload</span>
                    </div>
                    <div className="flex-1 w-full">
                        <h4 className="text-base font-bold text-white mb-2">Add file to {activeVaultUser.name}'s vault</h4>
                        <div className="flex flex-col sm:flex-row gap-2">
                            <input
                                className="flex-1 bg-[#060e20] border border-outline-variant/20 rounded-lg px-3 py-2 text-sm text-white outline-none"
                                placeholder="File name"
                                value={newFileName}
                                onChange={(event) => setNewFileName(event.target.value)}
                            />
                            <select
                                className="bg-[#060e20] border border-outline-variant/20 rounded-lg px-3 py-2 text-sm text-white outline-none"
                                value={newFileType}
                                onChange={(event) => setNewFileType(event.target.value)}
                            >
                                <option value="pdf">PDF</option>
                                <option value="csv">CSV</option>
                                <option value="txt">TXT</option>
                            </select>
                            <button
                                className="px-4 py-2 bg-gradient-to-br from-primary to-primary-container text-[#1000a9] font-bold text-sm rounded-lg hover:opacity-90 transition-all"
                                onClick={handleAddDocument}
                            >
                                Add File
                            </button>
                        </div>
                    </div>
                </div>
            </div>

            <div>
                <h3 className="font-label text-[0.6875rem] uppercase tracking-[0.05em] text-outline mb-4 flex items-center justify-between">
                    <span>Knowledge Files ({docs.length})</span>
                </h3>

                <div className="space-y-3">
                    {docs.map((doc) => (
                        <KnowledgeVaultDocumentRow
                            key={doc.id}
                            doc={doc}
                            onDelete={handleDeleteDocument}
                            onStatusChange={handleStatusChange}
                        />
                    ))}
                </div>
            </div>
        </div>
    );
}
