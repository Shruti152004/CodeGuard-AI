import React, { useState, useEffect } from 'react';
import { getGitHubRepositories } from '../services/analyses';
import type { GitHubRepo } from '../services/analyses';
import { GitFork, ExternalLink, RefreshCw, Lock, Unlock } from 'lucide-react';

const Repositories: React.FC = () => {
  const [repos, setRepos] = useState<GitHubRepo[]>([]);
  const [loading, setLoading] = useState(false);
  const [tokenInput, setTokenInput] = useState('');

  const fetchRepositories = async (token?: string) => {
    setLoading(true);
    try {
      const data = await getGitHubRepositories(token || undefined);
      setRepos(data);
    } catch (e) {
      // If unauthorized/error, load fallback mock repo items
      setRepos([
        {
          id: 1,
          name: 'codeguard-core-backend',
          full_name: 'CodeGuard/codeguard-core-backend',
          description: 'Spring Boot core microservice backend implementing JWT and Kafka analysis streams.',
          html_url: 'https://github.com/Shruti152004/CodeGuard-AI',
          isPrivate: true,
          default_branch: 'main'
        },
        {
          id: 2,
          name: 'example-react-app',
          full_name: 'CodeGuard/example-react-app',
          description: 'A frontend client dashboard for code review metrics.',
          html_url: 'https://github.com/Shruti152004/CodeGuard-AI',
          isPrivate: false,
          default_branch: 'main'
        }
      ]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchRepositories();
  }, []);

  const handleSync = () => {
    fetchRepositories(tokenInput);
  };

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '30px' }}>
        <div>
          <h1 style={{ fontSize: '28px', color: '#fff', fontWeight: 700 }}>Repositories</h1>
          <p style={{ color: '#9ea0a5', fontSize: '15px' }}>Manage connected repositories and synchronize branches.</p>
        </div>
        <button
          onClick={handleSync}
          disabled={loading}
          style={{
            padding: '10px 16px',
            background: 'none',
            border: '1px solid #2e303a',
            borderRadius: '6px',
            color: '#fff',
            fontWeight: 600,
            cursor: 'pointer',
            display: 'flex',
            alignItems: 'center',
            gap: '8px',
            transition: 'border-color 0.2s'
          }}
        >
          <RefreshCw size={16} className={loading ? 'spin' : ''} />
          {loading ? 'Syncing...' : 'Sync Repositories'}
        </button>
      </div>

      <div style={{ background: '#151b24', border: '1px solid #2e303a', borderRadius: '8px', padding: '20px', marginBottom: '30px' }}>
        <h3 style={{ color: '#fff', marginBottom: '10px', fontSize: '16px' }}>Provide GitHub Personal Access Token</h3>
        <p style={{ color: '#9ea0a5', fontSize: '14px', marginBottom: '15px' }}>Input a token to fetch your private repositories from live GitHub API.</p>
        <div style={{ display: 'flex', gap: '15px' }}>
          <input
            type="password"
            value={tokenInput}
            onChange={(e) => setTokenInput(e.target.value)}
            placeholder="ghp_xxxxxxxxxxxxxxxxxxxx"
            style={{
              flex: 1,
              padding: '10px 16px',
              background: '#0b0c10',
              border: '1px solid #2e303a',
              borderRadius: '6px',
              color: '#fff',
              fontSize: '14px'
            }}
          />
          <button
            onClick={handleSync}
            style={{
              padding: '10px 20px',
              background: '#66fcf1',
              color: '#0b0c10',
              border: 'none',
              borderRadius: '6px',
              fontWeight: 600,
              cursor: 'pointer'
            }}
          >
            Apply Token
          </button>
        </div>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(320px, 1fr))', gap: '20px' }}>
        {repos.map((repo) => (
          <div key={repo.id} style={{ background: '#151b24', border: '1px solid #2e303a', borderRadius: '8px', padding: '24px', display: 'flex', flexDirection: 'column', justifyContent: 'space-between' }}>
            <div>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '12px' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '8px', color: '#66fcf1' }}>
                  <GitFork size={20} />
                  <h3 style={{ color: '#fff', fontSize: '18px', fontWeight: 600, margin: 0 }}>{repo.name}</h3>
                </div>
                <span style={{ display: 'flex', alignItems: 'center', gap: '4px', fontSize: '12px', background: '#0b0c10', padding: '4px 8px', borderRadius: '4px', color: '#9ea0a5' }}>
                  {repo.isPrivate ? <Lock size={12} /> : <Unlock size={12} />}
                  {repo.isPrivate ? 'Private' : 'Public'}
                </span>
              </div>
              <p style={{ color: '#9ea0a5', fontSize: '14px', lineHeight: 1.5, marginBottom: '20px' }}>
                {repo.description || 'No description provided.'}
              </p>
            </div>
            
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', borderTop: '1px solid #2e303a', paddingTop: '15px' }}>
              <span style={{ fontSize: '13px', color: '#9ea0a5' }}>Default Branch: <strong style={{ color: '#fff' }}>{repo.default_branch}</strong></span>
              <a
                href={repo.html_url}
                target="_blank"
                rel="noreferrer"
                style={{
                  color: '#66fcf1',
                  textDecoration: 'none',
                  fontSize: '13px',
                  display: 'flex',
                  alignItems: 'center',
                  gap: '4px'
                }}
              >
                GitHub <ExternalLink size={14} />
              </a>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};

export default Repositories;
