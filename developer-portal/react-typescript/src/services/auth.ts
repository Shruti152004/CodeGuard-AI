import api from './api';

export interface LoginRequest {
  usernameOrEmail: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
  organizationName?: string;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  username: string;
}

export const login = async (req: LoginRequest): Promise<AuthResponse> => {
  const res = await api.post<AuthResponse>('/api/auth/login', req);
  return res.data;
};

export const register = async (req: RegisterRequest): Promise<any> => {
  const res = await api.post('/api/auth/register', req);
  return res.data;
};
