import React, { useState, useEffect } from 'react';
import { getRecentAnalyses, getAnalysisDetails } from '../services/analyses';
import type { Analysis } from '../services/analyses';
import { ArrowLeftRight } from 'lucide-react';

const CompareAnalyses: React.FC = () => {
  const [repo] = useState('codeguard-core-backend');
  const [runs, setRuns] = useState<Analysis[]>([]);
  const [runAId, setRunAId] = useState<number | ''>('');
  const [runBId, setRunBId] = useState<number | ''>('');
  const [runA, setRunA] = useState<Analysis | null>(null);
  const [runB, setRunB] = useState<Analysis | null>(null);

  useEffect(() => {
    getRecentAnalyses(repo).then((list) => {
      setRuns(list);
      if (list.length >= 2) {
        setRunAId(list[0].id);
        setRunBId(list[1].id);
      }
    });
  }, [repo]);

  useEffect(() => {
    if (runAId) getAnalysisDetails(Number(runAId)).then(setRunA);
    else setRunA(null);
  }, [runAId]);

  useEffect(() => {
    if (runBId) getAnalysisDetails(Number(runBId)).then(setRunB);
    else setRunB(null);
  }, [runBId]);

  return (
    <div>
      <div style={{ marginBottom: '30px' }}>
        <h1 style={{ fontSize: '24px', color: '#fff', fontWeight: 700 }}>Compare Analyses</h1>
        <p style={{ color: '#9ea0a5', fontSize: '15px' }}>Compare two distinct runs side-by-side to track quality score improvements.</p>
      </div>

      <div style={{ display: 'flex', gap: '20px', marginBottom: '30px', alignItems: 'center' }}>
        <select
          value={runAId}
          onChange={(e) => setRunAId(Number(e.target.value) || '')}
          style={{ padding: '10px', background: '#151b24', border: '1px solid #2e303a', color: '#fff', borderRadius: '6px', flex: 1 }}
        >
          <option value="">Select First Run</option>
          {runs.map((r) => <option key={r.id} value={r.id}>Run #{r.id} ({new Date(r.createdAt).toLocaleDateString()})</option>)}
        </select>

        <ArrowLeftRight size={24} style={{ color: '#66fcf1' }} />

        <select
          value={runBId}
          onChange={(e) => setRunBId(Number(e.target.value) || '')}
          style={{ padding: '10px', background: '#151b24', border: '1px solid #2e303a', color: '#fff', borderRadius: '6px', flex: 1 }}
        >
          <option value="">Select Second Run</option>
          {runs.map((r) => <option key={r.id} value={r.id}>Run #{r.id} ({new Date(r.createdAt).toLocaleDateString()})</option>)}
        </select>
      </div>

      {runA && runB ? (
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '30px' }}>
          {/* Run A Column */}
          <div style={{ background: '#151b24', border: '1px solid #2e303a', borderRadius: '8px', padding: '24px' }}>
            <h3 style={{ color: '#fff', fontSize: '18px', marginBottom: '15px' }}>Run #{runA.id} Details</h3>
            <ul style={{ display: 'flex', flexDirection: 'column', gap: '12px', listStyle: 'none' }}>
              <li><strong>Overall Score:</strong> {runA.overallScore}%</li>
              <li><strong>Security Score:</strong> {runA.securityScore}%</li>
              <li><strong>Reliability Score:</strong> {runA.reliabilityScore}%</li>
              <li><strong>Maintainability Score:</strong> {runA.maintainabilityScore}%</li>
              <li><strong>Technical Debt Hours:</strong> {runA.technicalDebtHours} hours</li>
            </ul>
          </div>

          {/* Run B Column */}
          <div style={{ background: '#151b24', border: '1px solid #2e303a', borderRadius: '8px', padding: '24px' }}>
            <h3 style={{ color: '#fff', fontSize: '18px', marginBottom: '15px' }}>Run #{runB.id} Details</h3>
            <ul style={{ display: 'flex', flexDirection: 'column', gap: '12px', listStyle: 'none' }}>
              <li><strong>Overall Score:</strong> {runB.overallScore}%</li>
              <li><strong>Security Score:</strong> {runB.securityScore}%</li>
              <li><strong>Reliability Score:</strong> {runB.reliabilityScore}%</li>
              <li><strong>Maintainability Score:</strong> {runB.maintainabilityScore}%</li>
              <li><strong>Technical Debt Hours:</strong> {runB.technicalDebtHours} hours</li>
            </ul>
          </div>
        </div>
      ) : (
        <div style={{ color: '#9ea0a5', textAlign: 'center', padding: '40px' }}>Please select two runs to begin comparison.</div>
      )}
    </div>
  );
};

export default CompareAnalyses;
