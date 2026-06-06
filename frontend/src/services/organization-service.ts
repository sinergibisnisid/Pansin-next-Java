import apiClient from './api-client';
import type { BackendOrganization } from '@/types';

export const organizationService = {
  create: async (data: {
    name: string;
    code: string;
    description?: string;
    address?: string;
    phone?: string;
    email?: string;
  }): Promise<BackendOrganization> => {
    const payload = {
      code: data.code,
      name: data.name,
      description: data.description || '',
      address: data.address || '',
      phone: data.phone || '',
      email: data.email || '',
    };
    const response = await apiClient.post('/organizations', payload);
    return response.data.data;
  },

  getAll: async (): Promise<BackendOrganization[]> => {
    const response = await apiClient.get('/organizations');
    return Array.isArray(response.data.data) ? response.data.data : [];
  },

  update: async (id: string, data: {
    name?: string;
    code?: string;
    description?: string;
    address?: string;
    phone?: string;
    email?: string;
  }): Promise<BackendOrganization> => {
    const response = await apiClient.put(`/organizations/${id}`, data);
    return response.data.data;
  },

  delete: async (id: string): Promise<void> => {
    await apiClient.delete(`/organizations/${id}`);
  },
};
