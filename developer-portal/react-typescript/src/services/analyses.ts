import api from './api';

export interface Analysis {
  id: number;
  repositoryName: string;
  branch: string;
  status: string;
  overallScore: number;
  securityScore: number;
  reliabilityScore: number;
  maintainabilityScore: number;
  performanceScore: number;
  codeQualityScore: number;
  technicalDebtHours: number;
  createdAt: string;
}

export interface Issue {
  id: number;
  title: string;
  category: string;
  severity: string;
  filePath: string;
  lineNumber: number;
  description: string;
  impact: string;
  recommendation: string;
  suggestedFix: string;
  source: string;
}

export interface TechnicalDebt {
  id: number;
  repositoryName: string;
  totalHours: number;
  updatedAt: string;
}

export interface StartAnalysisRequest {
  repositoryName: string;
  branch: string;
}

export const startAnalysis = async (req: StartAnalysisRequest, githubToken?: string): Promise<Analysis> => {
  const headers = githubToken ? { 'X-GitHub-Token': githubToken } : {};
  const res = await api.post<Analysis>('/api/analyses/start', req, { headers });
  return res.data;
};

export const getAnalysisDetails = async (id: number): Promise<Analysis> => {
  const res = await api.get<Analysis>(`/api/analyses/${id}`);
  return res.data;
};

export const getAnalysisIssues = async (id: number): Promise<Issue[]> => {
  const res = await api.get<Issue[]>(`/api/analyses/${id}/issues`);
  return res.data;
};

export const getTechnicalDebt = async (repoName: string): Promise<TechnicalDebt> => {
  const res = await api.get<TechnicalDebt>(`/api/analyses/technical-debt/${repoName}`);
  return res.data;
};

export const getRecentAnalyses = async (repoName: string): Promise<Analysis[]> => {
  const res = await api.get<Analysis[]>(`/api/analyses/history/${repoName}`);
  return res.data;
};

export interface GitHubRepo {
  id: number;
  name: string;
  full_name: string;
  description: string;
  html_url: string;
  isPrivate: boolean;
  default_branch: string;
}

export const getGitHubRepositories = async (githubToken?: string): Promise<GitHubRepo[]> => {
  const headers = githubToken ? { 'X-GitHub-Token': githubToken } : {};
  const res = await api.get<GitHubRepo[]>('/api/github/repos', { headers });
  return res.data;
};
