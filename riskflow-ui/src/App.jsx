import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { useMemo, useState } from 'react';
import Sidebar from './components/Sidebar';
import DashboardContent from './components/DashboardContent';
import ComponentsMarketplace from './pages/ComponentsMarketplace';
import WorkflowBuilder from './pages/WorkflowBuilder';
import Incidents from './pages/Incidents';
import Configuration from './pages/Configuration';
import { initialVaultUsers } from './data/knowledgeVault';
import KnowledgeVaultHub from './pages/KnowledgeVaultHub';
import KnowledgeVaultDetail from './pages/KnowledgeVaultDetail';
function App() {
  const [vaultUsers, setVaultUsers] = useState(initialVaultUsers);
  const [activeVaultUserId, setActiveVaultUserId] = useState(initialVaultUsers[0]?.id ?? null);

  const activeVaultUser = useMemo(
    () => vaultUsers.find((user) => user.id === activeVaultUserId) ?? null,
    [vaultUsers, activeVaultUserId]
  );

  const updateActiveVaultDocs = (updater) => {
    setVaultUsers((prev) =>
      prev.map((user) => {
        if (user.id !== activeVaultUserId) {
          return user;
        }

        const nextDocs = typeof updater === 'function' ? updater(user.docs) : updater;
        return { ...user, docs: nextDocs };
      })
    );
  };

  return (
    <BrowserRouter>
      <div className="flex bg-[#0b1326] min-h-screen text-[#dae2fd] font-body overflow-hidden">
        <Sidebar
          vaultUsers={vaultUsers}
          activeVaultUserId={activeVaultUserId}
          onVaultUserChange={setActiveVaultUserId}
        />

        <div className="flex-1 flex flex-col ml-64">
          <Routes>
            <Route path="/" element={<DashboardContent />} />
            <Route path="/overview" element={<DashboardContent />} />
            <Route path="/components" element={<ComponentsMarketplace />} />
            <Route path="/workflows" element={<WorkflowBuilder />} />
            <Route path="/logs" element={<Incidents />} />
            <Route path="/knowledge-base" element={<KnowledgeVaultHub />} />
            <Route path="/knowledge-base/:id" element={<KnowledgeVaultDetail />} />
            <Route
              path="/configuration"
              element={
                <Configuration
                  activeVaultUser={activeVaultUser}
                  onActiveVaultDocsChange={updateActiveVaultDocs}
                />
              }
            />
          </Routes>
        </div>
      </div>
    </BrowserRouter>
  );
}

export default App;
