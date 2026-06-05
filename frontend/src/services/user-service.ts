import apiClient from './api-client';
import type { User } from '@/types';

export const userService = {
  create: async (data: {
    username: string;
    email: string;
    fullName: string;
    password: string;
    role: string;
    branchId?: string;
  }): Promise<User> => {
    const payload = {
      username: data.username,
      email: data.email,
      password: data.password,
      fullName: data.fullName,
      roleCodes: [data.role],
      branchId: data.branchId || null,
    };
    const response = await apiClient.post('/users', payload);
    return response.data.data;
  },

  getAll: async (): Promise<User[]> => {
    const response = await apiClient.get('/users');
    return Array.isArray(response.data.data) ? response.data.data : [];
  },
};
