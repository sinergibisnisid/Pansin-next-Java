import apiClient from './api-client';
import type { User, PageResponse } from '@/types';

export const userService = {
  getAll: async (params?: {
    page?: number;
    size?: number;
    search?: string;
  }): Promise<PageResponse<User>> => {
    const response = await apiClient.get('/users', { params });
    return response.data.data;
  },

  getById: async (id: string): Promise<User> => {
    const response = await apiClient.get(`/users/${id}`);
    return response.data.data;
  },

  create: async (data: {
    username: string;
    email: string;
    password: string;
    fullName: string;
    phone?: string;
    nik?: string;
    employeeId?: string;
    organizationId?: string;
    branchId?: string;
    roleCodes: string[];
  }): Promise<User> => {
    const response = await apiClient.post('/users', data);
    return response.data.data;
  },

  update: async (id: string, data: {
    email?: string;
    fullName?: string;
    phone?: string;
    nik?: string;
    employeeId?: string;
    organizationId?: string;
    branchId?: string;
    roleCodes?: string[];
    enabled?: boolean;
  }): Promise<User> => {
    const response = await apiClient.put(`/users/${id}`, data);
    return response.data.data;
  },

  delete: async (id: string): Promise<void> => {
    await apiClient.delete(`/users/${id}`);
  },
};
