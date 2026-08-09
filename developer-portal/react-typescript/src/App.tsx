import { useState, useEffect } from 'react';
import './App.css';

interface BackendHealth {
  status: string;
  service: string;
  timestamp: number;
  database: string;
}

function App() {
  const [health, setHealth] = useState<BackendHealth | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchHealth = async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await fetch(import.meta.env.VITE_API_URL || 'http://localhost:8080/api/health');
      if (!response.ok) {
        throw new Error(`Server returned status: ${response.status}`);
      }
      const data = await response.json();
      setHealth(data);
    } catch (err: any) {
      setError(err.message || 'Failed to connect to core backend API');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchHealth();
  }, []);

  return (
    <div className="dashboard-container">
      <header className="dashboard-header">
        <div className="logo-section">
          <div className="logo-icon">🛡️</div>
          <div className="logo-text">
            <h1>CodeGuard <span className="highlight">AI</span></h1>
            <p className="subtitle">Developer Portal (Phase 1)</p>
          </div>
        </div>
        <div className="system-status">
          <button className="btn-refresh" onClick={fetchHealth} disabled={loading}>
            {loading ? 'Checking...' : 'Refresh Status'}
          </button>
        </div>
      </header>

      <main className="dashboard-content">
        <section className="status-grid">
          {/* Backend Status Card */}
          <div className="status-card">
            <h3>Core Backend API</h3>
            {loading ? (
              <div className="pulse-loader">Checking core service...</div>
            ) : error ? (
              <div className="status-indicator error">
                <span className="dot"></span>
                <span>Disconnected</span>
                <p className="error-details">{error}</p>
              </div>
            ) : (
              <div className="status-indicator success">
                <span className="dot"></span>
                <span>Connected ({health?.status})</span>
                <p className="status-details">Service: {health?.service}</p>
              </div>
            )}
          </div>

          {/* Database Status Card */}
          <div className="status-card">
            <h3>PostgreSQL Database</h3>
            {loading ? (
              <div className="pulse-loader">Checking database...</div>
            ) : error ? (
              <div className="status-indicator error">
                <span className="dot"></span>
                <span>Unavailable</span>
                <p className="error-details">Core backend is down</p>
              </div>
            ) : health?.database === 'CONNECTED' ? (
              <div className="status-indicator success">
                <span className="dot"></span>
                <span>Connected</span>
                <p className="status-details">Dynamic schema verified</p>
              </div>
            ) : (
              <div className="status-indicator warning">
                <span className="dot"></span>
                <span>Issue Detected</span>
                <p className="error-details">{health?.database}</p>
              </div>
            )}
          </div>

          {/* Platform Version Card */}
          <div className="status-card">
            <h3>Infrastructure</h3>
            <div className="status-indicator success">
              <span className="dot"></span>
              <span>Running (Docker)</span>
              <p className="status-details">Mode: Development</p>
            </div>
          </div>
        </section>

        <section className="preview-container">
          <h2>CodeGuard AI Platform Pipeline Preview</h2>
          <p className="preview-desc">Here is the active pipeline diagram and mock overview of the system modules being configured.</p>
          
          <div className="pipeline-grid">
            <div className="pipeline-step active">
              <div className="step-num">1</div>
              <h4>Developer Portal</h4>
              <p>React UI (This screen)</p>
            </div>
            <div className="pipeline-arrow">➔</div>
            <div className="pipeline-step active">
              <div className="step-num">2</div>
              <h4>Spring Boot</h4>
              <p>Core Backend REST API</p>
            </div>
            <div className="pipeline-arrow">➔</div>
            <div className="pipeline-step active">
              <div className="step-num">3</div>
              <h4>PostgreSQL</h4>
              <p>Relational Database Schema</p>
            </div>
            <div className="pipeline-arrow">➔</div>
            <div className="pipeline-step pending">
              <div className="step-num">4</div>
              <h4>Analysis Engine</h4>
              <p>Kafka & Workers (Phase 4-7)</p>
            </div>
          </div>

          <div className="dashboard-info">
            <h3>Next Steps (Phase 2)</h3>
            <p>Phase 2 will integrate User Management, Spring Security with JWT tokens, Organization setups, and schema migrations.</p>
          </div>
        </section>
      </main>

      <footer className="dashboard-footer">
        <p>CodeGuard AI © {new Date().getFullYear()} - Phase 1 Foundation Verified</p>
      </footer>
    </div>
  );
}

export default App;
