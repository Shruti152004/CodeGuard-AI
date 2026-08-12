import React from 'react';
import { AlertCircle, ChevronRight } from 'lucide-react';
import type { Issue } from '../services/analyses';

interface IssueCardProps {
  issue: Issue;
  onSelect: (issue: Issue) => void;
}

const IssueCard: React.FC<IssueCardProps> = ({ issue, onSelect }) => {
  const getSeverityClass = (severity: string) => {
    switch (severity.toUpperCase()) {
      case 'CRITICAL':
        return 'score-low';
      case 'HIGH':
      case 'MEDIUM':
        return 'score-medium';
      default:
        return 'score-high';
    }
  };

  return (
    <div
      onClick={() => onSelect(issue)}
      style={{
        background: '#151b24',
        border: '1px solid #2e303a',
        borderRadius: '8px',
        padding: '20px',
        marginBottom: '15px',
        cursor: 'pointer',
        transition: 'border-color 0.2s, transform 0.2s',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
      }}
      onMouseEnter={(e) => {
        e.currentTarget.style.borderColor = '#66fcf1';
        e.currentTarget.style.transform = 'translateX(4px)';
      }}
      onMouseLeave={(e) => {
        e.currentTarget.style.borderColor = '#2e303a';
        e.currentTarget.style.transform = 'none';
      }}
    >
      <div style={{ display: 'flex', gap: '15px', alignItems: 'flex-start' }}>
        <AlertCircle size={22} style={{ marginTop: '2px', color: issue.severity === 'CRITICAL' ? '#ff4a4a' : '#f7a81b' }} />
        <div>
          <div style={{ display: 'flex', gap: '10px', alignItems: 'center', marginBottom: '6px' }}>
            <span style={{ fontWeight: 600, color: '#fff', fontSize: '16px' }}>{issue.title}</span>
            <span className={`score-badge ${getSeverityClass(issue.severity)}`} style={{ padding: '2px 8px', fontSize: '11px' }}>
              {issue.severity}
            </span>
            <span style={{ background: '#2e303a', color: '#9ea0a5', fontSize: '11px', padding: '2px 8px', borderRadius: '4px' }}>
              {issue.category}
            </span>
          </div>
          <p style={{ color: '#9ea0a5', fontSize: '14px', marginBottom: '8px' }}>{issue.description}</p>
          <div style={{ fontSize: '12px', color: '#66fcf1', fontFamily: 'monospace' }}>
            {issue.filePath} : Line {issue.lineNumber}
          </div>
        </div>
      </div>
      <ChevronRight size={20} style={{ color: '#9ea0a5' }} />
    </div>
  );
};

export default IssueCard;
