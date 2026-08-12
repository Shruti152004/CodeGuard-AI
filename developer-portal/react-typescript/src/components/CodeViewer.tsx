import React from 'react';
import type { Issue } from '../services/analyses';
import { Code, Lightbulb } from 'lucide-react';

interface CodeViewerProps {
  issue: Issue;
  onClose: () => void;
}

const CodeViewer: React.FC<CodeViewerProps> = ({ issue, onClose }) => {
  // Mock file contents containing the bug structure
  const getMockFileContent = (path: string) => {
    if (path.includes('SecurityFilter.java')) {
      return [
        'package com.codeguard.core;',
        'public class SecurityFilter {',
        '    private String password = "super_secret_credentials_123";',
        '    public void doFilter() {',
        '        try {',
        '            System.out.println("Filtering request");',
        '        } catch (Exception e) {} ',
        '    }',
        '}'
      ];
    }
    if (path.includes('utils.py')) {
      return [
        'def run_script(user_input):',
        '    print("Running custom script")',
        '    try:',
        '        eval(user_input)',
        '    except:',
        '        pass'
      ];
    }
    return [
      '// File Content View Mock',
      `// Inspecting ${path}`,
      'function executeProcess() {',
      '    console.log("Starting execution context...");',
      '    eval("var test = 1");',
      '}'
    ];
  };

  const lines = getMockFileContent(issue.filePath);

  return (
    <div style={{
      background: '#151b24',
      border: '1px solid #2e303a',
      borderRadius: '8px',
      padding: '24px',
      color: '#fff',
      display: 'flex',
      flexDirection: 'column',
      gap: '20px'
    }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', borderBottom: '1px solid #2e303a', paddingBottom: '10px' }}>
        <div style={{ display: 'flex', gap: '10px', alignItems: 'center' }}>
          <Code size={20} color="#66fcf1" />
          <span style={{ fontWeight: 600 }}>{issue.filePath.split('/').pop()}</span>
        </div>
        <button
          onClick={onClose}
          style={{
            background: 'none',
            border: 'none',
            color: '#ff4a4a',
            cursor: 'pointer',
            fontSize: '14px',
            fontWeight: 600
          }}
        >
          Close Viewer
        </button>
      </div>

      {/* Code Render */}
      <div style={{
        background: '#0b0c10',
        border: '1px solid #2e303a',
        borderRadius: '6px',
        padding: '15px',
        fontFamily: 'monospace',
        fontSize: '13px',
        overflowX: 'auto',
        maxHeight: '300px'
      }}>
        {lines.map((line, idx) => {
          const isHighlighted = (idx + 1) === issue.lineNumber;
          return (
            <div
              key={idx}
              style={{
                background: isHighlighted ? 'rgba(255, 74, 74, 0.15)' : 'none',
                borderLeft: isHighlighted ? '3px solid #ff4a4a' : 'none',
                paddingLeft: isHighlighted ? '10px' : '13px',
                color: isHighlighted ? '#ff8888' : '#c5c6c7',
                whiteSpace: 'pre'
              }}
            >
              <span style={{ color: '#9ea0a5', marginRight: '15px', display: 'inline-block', width: '20px', textAlign: 'right' }}>{idx + 1}</span>
              {line}
            </div>
          );
        })}
      </div>

      {/* AI Recommendation Overlay */}
      <div style={{
        background: 'rgba(102, 252, 241, 0.05)',
        border: '1px solid rgba(102, 252, 241, 0.2)',
        borderRadius: '6px',
        padding: '20px',
        display: 'flex',
        flexDirection: 'column',
        gap: '12px'
      }}>
        <div style={{ display: 'flex', gap: '10px', alignItems: 'center' }}>
          <Lightbulb size={20} color="#66fcf1" />
          <span style={{ fontWeight: 600, color: '#66fcf1' }}>AI Suggestion & Impact</span>
        </div>
        <div style={{ fontSize: '14px', color: '#c5c6c7' }}>
          <strong>Impact:</strong> {issue.impact}
        </div>
        <div style={{ fontSize: '14px', color: '#c5c6c7' }}>
          <strong>Recommendation:</strong> {issue.recommendation}
        </div>
        {issue.suggestedFix && (
          <div>
            <div style={{ fontSize: '13px', color: '#9ea0a5', marginBottom: '6px' }}>Suggested Fix:</div>
            <pre style={{
              background: '#0b0c10',
              padding: '10px',
              borderRadius: '4px',
              fontSize: '12px',
              fontFamily: 'monospace',
              color: '#2ecc71',
              border: '1px solid #2e303a'
            }}>{issue.suggestedFix}</pre>
          </div>
        )}
      </div>
    </div>
  );
};

export default CodeViewer;
