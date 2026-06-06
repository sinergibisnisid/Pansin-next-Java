import apiClient from './api-client';
import type { BackendBranch } from '@/types';

export const branchService = {
  getAll: async (): Promise<BackendBranch[]> => {
    const response = await apiClient.get('/branches');
    return Array.isArray(response.data.data) ? response.data.data : [];
  },

  getById: async (id: string): Promise<BackendBranch> => {
    const response = await apiClient.get(`/branches/${id}`);
    return response.data.data;
  },

  create: async (data: {
    organizationId: string;
    code: string;
    name: string;
    address: string;
    city: string;
    province: string;
    postalCode: string;
    phone: string;
    email: string;
    latitude?: number;
    longitude?: number;
    timezone: string;
  }): Promise<BackendBranch> => {
    const response = await apiClient.post('/branches', data);
    return response.data.data;
  },

  update: async (id: string, data: {
    organizationId?: string;
    code?: string;
    name?: string;
    address?: string;
    city?: string;
    province?: string;
    postalCode?: string;
    phone?: string;
    email?: string;
    latitude?: number;
    longitude?: number;
    timezone?: string;
  }): Promise<BackendBranch> => {
    const response = await apiClient.put(`/branches/${id}`, data);
    return response.data.data;
  },

  delete: async (id: string): Promise<void> => {
    await apiClient.delete(`/branches/${id}`);
  },
};
