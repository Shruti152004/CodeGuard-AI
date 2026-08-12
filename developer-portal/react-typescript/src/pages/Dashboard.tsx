import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { getTechnicalDebt, getRecentAnalyses, startAnalysis } from '../services/analyses';
import type { Analysis } from '../services/analyses';
import { Play, Clipboard, Clock, AlertTriangle, Plus, X } from 'lucide-react';

const Dashboard: React.FC = () => {
  const navigate = useNavigate();
  const [selectedRepo, setSelectedRepo] = useState('codeguard-core-backend');
  const [recentRuns, setRecentRuns] = useState<Analysis[]>([]);
  const [debtHours, setDebtHours] = useState(0.0);
  const [loading, setLoading] = useState(false);
  const [triggering, setTriggering] = useState(false);

  // Dynamic Repository List
  const [repositories, setRepositories] = useState<string[]>(() => {
    const saved = localStorage.getItem('addedRepos');
    const base = ['codeguard-core-backend', 'example-react-app'];
    if (saved) {
      try {
        const parsed = JSON.parse(saved);
        return [...base, ...parsed];
      } catch {
        return base;
      }
    }
    return base;
  });

  const [showModal, setShowModal] = useState(false);
  const [newRepoInput, setNewRepoInput] = useState('');
  const [modalError, setModalError] = useState('');

  const fetchDashboardData = async () => {
    setLoading(true);
    try {
      const debt = await getTechnicalDebt(selectedRepo);
      setDebtHours(debt.totalHours);
    } catch {
      setDebtHours(0.0);
    }

    try {
      const history = await getRecentAnalyses(selectedRepo);
      setRecentRuns(history);
    } catch {
      setRecentRuns([]);
    }
    setLoading(false);
  };

  useEffect(() => {
    fetchDashboardData();
  }, [selectedRepo]);

  const handleTriggerAnalysis = async () => {
    setTriggering(true);
    try {
      const analysis = await startAnalysis({ repositoryName: selectedRepo, branch: 'main' });
      // Add the new run to list
      setRecentRuns((prev) => [analysis, ...prev]);
      // Refetch technical debt
      const debt = await getTechnicalDebt(selectedRepo);
      setDebtHours(debt.totalHours);
    } catch (e) {
      alert('Failed to trigger analysis.');
    } finally {
      setTriggering(false);
    }
  };

  const handleAddRepository = () => {
    let input = newRepoInput.trim();
    if (!input) {
      setModalError('Repository name or URL cannot be empty.');
      return;
    }

    // Parse URL if provided
    if (input.startsWith('http://') || input.startsWith('https://')) {
      try {
        const urlObj = new URL(input);
        const pathSegments = urlObj.pathname.split('/').filter(s => s.trim().length > 0);
        if (pathSegments.length >= 2) {
          input = `${pathSegments[0]}/${pathSegments[1]}`;
        } else {
          setModalError('Invalid GitHub URL format.');
          return;
        }
      } catch {
        setModalError('Invalid URL.');
        return;
      }
    }

    // Basic owner/repo validation
    const parts = input.split('/');
    if (parts.length !== 2 || !parts[0].trim() || !parts[1].trim()) {
      setModalError('Please enter in owner/repo format (e.g. google/gson).');
      return;
    }

    const finalRepoName = `${parts[0].trim()}/${parts[1].trim()}`;
    if (repositories.includes(finalRepoName)) {
      setModalError('Repository is already registered.');
      return;
    }

    const newRepos = [...repositories, finalRepoName];
    setRepositories(newRepos);

    // Save only dynamically added repos to localStorage
    const base = ['codeguard-core-backend', 'example-react-app'];
    const addedOnly = newRepos.filter(r => !base.includes(r));
    localStorage.setItem('addedRepos', JSON.stringify(addedOnly));

    setSelectedRepo(finalRepoName);
    setNewRepoInput('');
    setModalError('');
    setShowModal(false);
  };

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '30px' }}>
        <div>
          <h1 style={{ fontSize: '28px', color: '#fff', fontWeight: 700 }}>Code Guard Dashboard</h1>
          <p style={{ color: '#9ea0a5', fontSize: '15px' }}>Monitor security metrics, quality scores, and debt trends.</p>
        </div>
        <div style={{ display: 'flex', gap: '15px', alignItems: 'center' }}>
          <button
            onClick={() => setShowModal(true)}
            style={{
              padding: '10px 16px',
              background: '#1f2833',
              border: '1px solid #2e303a',
              borderRadius: '6px',
              color: '#fff',
              fontWeight: 600,
              cursor: 'pointer',
              display: 'flex',
              alignItems: 'center',
              gap: '6px'
            }}
          >
            <Plus size={16} /> Add Repo
          </button>
          <select
            value={selectedRepo}
            onChange={(e) => setSelectedRepo(e.target.value)}
            style={{
              padding: '10px 16px',
              background: '#151b24',
              border: '1px solid #2e303a',
              borderRadius: '6px',
              color: '#fff',
              cursor: 'pointer'
            }}
          >
            {repositories.map((repo) => (
              <option key={repo} value={repo}>{repo}</option>
            ))}
          </select>
          <button
            onClick={handleTriggerAnalysis}
            disabled={triggering}
            style={{
              padding: '10px 16px',
              background: '#66fcf1',
              color: '#0b0c10',
              border: 'none',
              borderRadius: '6px',
              fontWeight: 600,
              cursor: 'pointer',
              display: 'flex',
              alignItems: 'center',
              gap: '8px'
            }}
          >
            <Play size={16} />
            {triggering ? 'Triggering Run...' : 'Run Analysis'}
          </button>
        </div>
      </div>

      {/* Modal Dialog */}
      {showModal && (
        <div style={{
          position: 'fixed',
          top: 0,
          left: 0,
          width: '100%',
          height: '100%',
          background: 'rgba(0,0,0,0.7)',
          display: 'flex',
          justifyContent: 'center',
          alignItems: 'center',
          zIndex: 1000
        }}>
          <div style={{
            background: '#1f2833',
            border: '1px solid #2e303a',
            borderRadius: '8px',
            width: '100%',
            maxWidth: '450px',
            padding: '24px',
            position: 'relative'
          }}>
            <button
              onClick={() => { setShowModal(false); setModalError(''); }}
              style={{
                position: 'absolute',
                top: '16px',
                right: '16px',
                background: 'none',
                border: 'none',
                color: '#9ea0a5',
                cursor: 'pointer'
              }}
            >
              <X size={20} />
            </button>
            <h3 style={{ color: '#fff', fontSize: '18px', fontWeight: 600, marginBottom: '12px' }}>Add GitHub Repository</h3>
            <p style={{ color: '#9ea0a5', fontSize: '14px', marginBottom: '20px' }}>Enter the repository identifier in <strong>owner/repository</strong> format or paste the full GitHub URL.</p>
            
            <div style={{ display: 'flex', flexDirection: 'column', gap: '15px' }}>
              <input
                type="text"
                value={newRepoInput}
                onChange={(e) => setNewRepoInput(e.target.value)}
                placeholder="e.g. google/gson"
                style={{
                  padding: '10px 16px',
                  background: '#151b24',
                  border: '1px solid #2e303a',
                  borderRadius: '6px',
                  color: '#fff',
                  fontSize: '14px'
                }}
              />
              {modalError && (
                <div style={{ color: '#ff4a4a', fontSize: '13px' }}>⚠️ {modalError}</div>
              )}
              <button
                onClick={handleAddRepository}
                style={{
                  padding: '12px',
                  background: '#66fcf1',
                  color: '#0b0c10',
                  border: 'none',
                  borderRadius: '6px',
                  fontWeight: 600,
                  cursor: 'pointer'
                }}
              >
                Add Repository
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Overview Cards */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: '20px', marginBottom: '30px' }}>
        <div style={{ background: '#151b24', border: '1px solid #2e303a', borderRadius: '8px', padding: '24px', display: 'flex', alignItems: 'center', gap: '20px' }}>
          <div style={{ background: 'rgba(102, 252, 241, 0.1)', padding: '15px', borderRadius: '8px' }}>
            <Clock size={28} color="#66fcf1" />
          </div>
          <div>
            <h4 style={{ color: '#9ea0a5', fontSize: '14px', fontWeight: 500 }}>Technical Debt</h4>
            <div style={{ fontSize: '24px', fontWeight: 700, color: '#fff', marginTop: '4px' }}>{debtHours} Hours</div>
          </div>
        </div>

        <div style={{ background: '#151b24', border: '1px solid #2e303a', borderRadius: '8px', padding: '24px', display: 'flex', alignItems: 'center', gap: '20px' }}>
          <div style={{ background: 'rgba(46, 204, 113, 0.1)', padding: '15px', borderRadius: '8px' }}>
            <Clipboard size={28} color="#2ecc71" />
          </div>
          <div>
            <h4 style={{ color: '#9ea0a5', fontSize: '14px', fontWeight: 500 }}>Latest Quality Score</h4>
            <div style={{ fontSize: '24px', fontWeight: 700, color: '#fff', marginTop: '4px' }}>
              {recentRuns.length > 0 ? recentRuns[0].overallScore : 'N/A'} / 100
            </div>
          </div>
        </div>

        <div style={{ background: '#151b24', border: '1px solid #2e303a', borderRadius: '8px', padding: '24px', display: 'flex', alignItems: 'center', gap: '20px' }}>
          <div style={{ background: 'rgba(231, 76, 60, 0.1)', padding: '15px', borderRadius: '8px' }}>
            <AlertTriangle size={28} color="#e74c3c" />
          </div>
          <div>
            <h4 style={{ color: '#9ea0a5', fontSize: '14px', fontWeight: 500 }}>Latest Run Status</h4>
            <div style={{ fontSize: '24px', fontWeight: 700, color: '#fff', marginTop: '4px', textTransform: 'capitalize' }}>
              {recentRuns.length > 0 ? recentRuns[0].status.toLowerCase() : 'N/A'}
            </div>
          </div>
        </div>
      </div>

      {/* Analysis History */}
      <div style={{ background: '#151b24', border: '1px solid #2e303a', borderRadius: '8px', padding: '24px' }}>
        <h3 style={{ fontSize: '20px', color: '#fff', fontWeight: 600, marginBottom: '20px' }}>Analysis History</h3>
        {loading ? (
          <div style={{ color: '#9ea0a5', textAlign: 'center', padding: '40px' }}>Loading history...</div>
        ) : recentRuns.length === 0 ? (
          <div style={{ color: '#9ea0a5', textAlign: 'center', padding: '40px' }}>No analysis runs recorded yet. Click "Run Analysis" to start.</div>
        ) : (
          <div style={{ overflowX: 'auto' }}>
            <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left' }}>
              <thead>
                <tr style={{ borderBottom: '2px solid #2e303a', fontSize: '14px', color: '#9ea0a5' }}>
                  <th style={{ padding: '12px' }}>Run ID</th>
                  <th style={{ padding: '12px' }}>Branch</th>
                  <th style={{ padding: '12px' }}>Status</th>
                  <th style={{ padding: '12px' }}>Overall Score</th>
                  <th style={{ padding: '12px' }}>Debt Hours</th>
                  <th style={{ padding: '12px' }}>Created At</th>
                  <th style={{ padding: '12px' }}>Action</th>
                </tr>
              </thead>
              <tbody style={{ fontSize: '14px', color: '#c5c6c7' }}>
                {recentRuns.map((run) => (
                  <tr key={run.id} style={{ borderBottom: '1px solid #2e303a' }}>
                    <td style={{ padding: '12px' }}>#{run.id}</td>
                    <td style={{ padding: '12px' }}>{run.branch}</td>
                    <td style={{ padding: '12px' }}>
                      <span style={{
                        background: run.status === 'COMPLETED' ? 'rgba(46, 204, 113, 0.15)' : 'rgba(247, 168, 27, 0.15)',
                        color: run.status === 'COMPLETED' ? '#2ecc71' : '#f7a81b',
                        padding: '2px 8px',
                        borderRadius: '4px',
                        fontSize: '12px'
                      }}>{run.status}</span>
                    </td>
                    <td style={{ padding: '12px', fontWeight: 600 }}>{run.overallScore}</td>
                    <td style={{ padding: '12px' }}>{run.technicalDebtHours} hrs</td>
                    <td style={{ padding: '12px' }}>{new Date(run.createdAt).toLocaleString()}</td>
                    <td style={{ padding: '12px' }}>
                      <button
                        onClick={() => navigate(`/analyses/${run.id}`)}
                        style={{
                          background: 'none',
                          border: 'none',
                          color: '#66fcf1',
                          cursor: 'pointer',
                          fontWeight: 600
                        }}
                      >
                        Inspect
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
};

export default Dashboard;
