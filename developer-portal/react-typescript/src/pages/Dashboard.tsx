import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { getTechnicalDebt, getRecentAnalyses, startAnalysis } from '../services/analyses';
import type { Analysis } from '../services/analyses';
import { Play, Clipboard, Clock, AlertTriangle } from 'lucide-react';

const Dashboard: React.FC = () => {
  const navigate = useNavigate();
  const [selectedRepo, setSelectedRepo] = useState('codeguard-core-backend');
  const [recentRuns, setRecentRuns] = useState<Analysis[]>([]);
  const [debtHours, setDebtHours] = useState(0.0);
  const [loading, setLoading] = useState(false);
  const [triggering, setTriggering] = useState(false);

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

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '30px' }}>
        <div>
          <h1 style={{ fontSize: '28px', color: '#fff', fontWeight: 700 }}>Code Guard Dashboard</h1>
          <p style={{ color: '#9ea0a5', fontSize: '15px' }}>Monitor security metrics, quality scores, and debt trends.</p>
        </div>
        <div style={{ display: 'flex', gap: '15px', alignItems: 'center' }}>
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
            <option value="codeguard-core-backend">codeguard-core-backend</option>
            <option value="example-react-app">example-react-app</option>
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
          <div style={{ background: 'rgba(255, 74, 74, 0.1)', padding: '15px', borderRadius: '8px' }}>
            <AlertTriangle size={28} color="#ff4a4a" />
          </div>
          <div>
            <h4 style={{ color: '#9ea0a5', fontSize: '14px', fontWeight: 500 }}>Recent Health Status</h4>
            <div style={{ fontSize: '24px', fontWeight: 700, color: '#fff', marginTop: '4px' }}>
              {recentRuns.length > 0 && recentRuns[0].overallScore > 80 ? 'EXCELLENT' : recentRuns.length > 0 ? 'WARNING' : 'N/A'}
            </div>
          </div>
        </div>
      </div>

      {/* Recent Analysis Table */}
      <div style={{ background: '#151b24', border: '1px solid #2e303a', borderRadius: '8px', padding: '30px' }}>
        <h3 style={{ color: '#fff', fontSize: '18px', fontWeight: 600, marginBottom: '20px' }}>Recent Analyses</h3>
        {loading ? (
          <div>Loading history...</div>
        ) : recentRuns.length === 0 ? (
          <div style={{ color: '#9ea0a5' }}>No analysis runs found for {selectedRepo}.</div>
        ) : (
          <div style={{ overflowX: 'auto' }}>
            <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left' }}>
              <thead>
                <tr style={{ borderBottom: '1px solid #2e303a', color: '#9ea0a5', fontSize: '14px' }}>
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
