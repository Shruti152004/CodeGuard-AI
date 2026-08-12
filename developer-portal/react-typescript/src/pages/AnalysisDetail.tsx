import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { getAnalysisDetails, getAnalysisIssues } from '../services/analyses';
import type { Analysis, Issue } from '../services/analyses';
import IssueCard from '../components/IssueCard';
import CodeViewer from '../components/CodeViewer';

const AnalysisDetail: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [analysis, setAnalysis] = useState<Analysis | null>(null);
  const [issues, setIssues] = useState<Issue[]>([]);
  const [filteredIssues, setFilteredIssues] = useState<Issue[]>([]);
  const [selectedIssue, setSelectedIssue] = useState<Issue | null>(null);
  const [loading, setLoading] = useState(true);
  
  // Filters
  const [severityFilter, setSeverityFilter] = useState('');
  const [categoryFilter, setCategoryFilter] = useState('');

  const fetchDetails = async () => {
    if (!id) return;
    setLoading(true);
    try {
      const details = await getAnalysisDetails(Number(id));
      setAnalysis(details);
      const list = await getAnalysisIssues(Number(id));
      setIssues(list);
      setFilteredIssues(list);
    } catch {
      alert('Failed to retrieve analysis details.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchDetails();
  }, [id]);

  useEffect(() => {
    let result = issues;
    if (severityFilter) {
      result = result.filter((issue) => issue.severity.toUpperCase() === severityFilter.toUpperCase());
    }
    if (categoryFilter) {
      result = result.filter((issue) => issue.category.toUpperCase() === categoryFilter.toUpperCase());
    }
    setFilteredIssues(result);
  }, [severityFilter, categoryFilter, issues]);

  if (loading) return <div>Loading analysis details...</div>;
  if (!analysis) return <div>Analysis not found.</div>;

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '30px' }}>
        <div>
          <h1 style={{ fontSize: '24px', color: '#fff', fontWeight: 700 }}>Analysis Report #{analysis.id}</h1>
          <p style={{ color: '#9ea0a5', fontSize: '15px' }}>Repository: {analysis.repositoryName} | Branch: {analysis.branch}</p>
        </div>
        <button
          onClick={() => navigate('/')}
          style={{
            padding: '10px 16px',
            background: 'none',
            border: '1px solid #2e303a',
            borderRadius: '6px',
            color: '#fff',
            cursor: 'pointer'
          }}
        >
          Back to Dashboard
        </button>
      </div>

      {/* Metrics breakdown */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))', gap: '20px', marginBottom: '30px' }}>
        {[
          { label: 'Security', score: analysis.securityScore },
          { label: 'Reliability', score: analysis.reliabilityScore },
          { label: 'Maintainability', score: analysis.maintainabilityScore },
          { label: 'Performance', score: analysis.performanceScore },
          { label: 'Code Quality', score: analysis.codeQualityScore }
        ].map((item) => (
          <div key={item.label} style={{ background: '#151b24', border: '1px solid #2e303a', borderRadius: '8px', padding: '20px', textAlign: 'center' }}>
            <h4 style={{ color: '#9ea0a5', fontSize: '13px', fontWeight: 500, marginBottom: '8px' }}>{item.label}</h4>
            <div style={{ fontSize: '28px', fontWeight: 700, color: item.score > 80 ? '#2ecc71' : item.score > 50 ? '#f7a81b' : '#ff4a4a' }}>
              {item.score}%
            </div>
          </div>
        ))}
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: selectedIssue ? '1fr 1fr' : '1fr', gap: '30px', alignItems: 'start' }}>
        {/* Issue Explorer */}
        <div style={{ background: '#151b24', border: '1px solid #2e303a', borderRadius: '8px', padding: '24px' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
            <h3 style={{ color: '#fff', fontSize: '18px', fontWeight: 600 }}>Detected Issues ({filteredIssues.length})</h3>
            <div style={{ display: 'flex', gap: '10px' }}>
              <select
                value={severityFilter}
                onChange={(e) => setSeverityFilter(e.target.value)}
                style={{ background: '#0b0c10', border: '1px solid #2e303a', padding: '6px', color: '#fff', borderRadius: '4px', fontSize: '13px' }}
              >
                <option value="">All Severities</option>
                <option value="CRITICAL">Critical</option>
                <option value="HIGH">High</option>
                <option value="MEDIUM">Medium</option>
                <option value="LOW">Low</option>
              </select>

              <select
                value={categoryFilter}
                onChange={(e) => setCategoryFilter(e.target.value)}
                style={{ background: '#0b0c10', border: '1px solid #2e303a', padding: '6px', color: '#fff', borderRadius: '4px', fontSize: '13px' }}
              >
                <option value="">All Categories</option>
                <option value="SECURITY">Security</option>
                <option value="BUG">Bug</option>
                <option value="MAINTAINABILITY">Maintainability</option>
                <option value="CODE_SMELL">Code Smell</option>
              </select>
            </div>
          </div>

          <div style={{ maxHeight: '600px', overflowY: 'auto' }}>
            {filteredIssues.map((issue) => (
              <IssueCard
                key={issue.id}
                issue={issue}
                onSelect={(selected) => setSelectedIssue(selected)}
              />
            ))}
          </div>
        </div>

        {/* Code Viewer Panel */}
        {selectedIssue && (
          <CodeViewer
            issue={selectedIssue}
            onClose={() => setSelectedIssue(null)}
          />
        )}
      </div>
    </div>
  );
};

export default AnalysisDetail;
