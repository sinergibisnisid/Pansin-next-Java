import apiClient from './api-client';
import type {
  LoginCredentials,
  OTPVerification,
  User,
  AuthTokens,
} from '@/types';

// Backend response types
interface LoginResponse {
  otpRequired: boolean;
  otpSessionId: string;
  expiresIn: number;
  message: string;
}

interface TokenResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
  user: {
    id: string;
    username: string;
    email: string;
    fullName: string;
    branchId: string | null;
    roles: string[];
    permissions: string[];
  };
}

export const authService = {
  login: async (credentials: LoginCredentials): Promise<LoginResponse> => {
    const response = await apiClient.post('/auth/login', {
      identifier: credentials.username,
      password: credentials.password,
      userAgent: navigator.userAgent,
    });
    return response.data.data;
  },

  verifyLoginOtp: async (otpSessionId: string, otp: string): Promise<{ user: User; tokens: AuthTokens }> => {
    const response = await apiClient.post('/auth/login/verify-otp', {
      otpSessionId,
      otp,
    });
    
    const data: TokenResponse = response.data.data;
    
    return {
      user: {
        id: data.user.id,
        username: data.user.username,
        email: data.user.email,
        fullName: data.user.fullName,
        role: data.user.roles[0] as any,
        permissions: data.user.permissions as any[],
        branchId: data.user.branchId || '',
        branchName: '', // TODO: Get from user data
        status: 'active',
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
      },
      tokens: {
        accessToken: data.accessToken,
        refreshToken: data.refreshToken,
        expiresAt: Date.now() + data.expiresIn * 1000,
      },
    };
  },

  verifyOTP: async (data: OTPVerification): Promise<{ verified: boolean }> => {
    const response = await apiClient.post('/auth/otp/verify', {
      identifier: data.userId,
      otp: data.otp,
    });
    return { verified: response.data.success };
  },

  requestOtp: async (identifier: string): Promise<void> => {
    await apiClient.post('/auth/otp/request', { identifier });
  },

  refreshToken: async (refreshToken: string): Promise<AuthTokens> => {
    const response = await apiClient.post('/auth/refresh', { refreshToken });
    const data: TokenResponse = response.data.data;
    
    return {
      accessToken: data.accessToken,
      refreshToken: data.refreshToken,
      expiresAt: Date.now() + data.expiresIn * 1000,
    };
  },

  logout: async (refreshToken?: string): Promise<void> => {
    await apiClient.post('/auth/logout', refreshToken ? { refreshToken } : undefined);
  },

  forgotPassword: async (email: string): Promise<{ message: string }> => {
    const response = await apiClient.post('/auth/forgot-password', { email });
    return { message: response.data.message };
  },

  getProfile: async (): Promise<User> => {
    const response = await apiClient.get('/auth/profile');
    const data = response.data.data;
    
    return {
      id: data.id,
      username: data.username,
      email: data.email,
      fullName: data.fullName,
      role: data.roles[0] as any,
      permissions: data.permissions as any[],
      branchId: data.branchId || '',
      branchName: '', // TODO: Get from user data
      status: 'active',
      createdAt: data.createdAt,
      updatedAt: data.updatedAt,
    };
  },
};
