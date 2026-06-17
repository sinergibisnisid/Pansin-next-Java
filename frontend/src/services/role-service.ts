import apiClient from './api-client';
import type { Role } from '@/types';

export const roleService = {
  getAll: async (): Promise<Role[]> => {
    const response = await apiClient.get('/roles');
    const data = response.data.data;
    return Array.isArray(data) ? data : [];
  },

  getById: async (id: string): Promise<Role> => {
    const response = await apiClient.get(`/roles/${id}`);
    return response.data.data;
  },

  create: async (data: {
    code: string;
    name: string;
    description?: string;
    permissionCodes: string[];
  }): Promise<Role> => {
    const response = await apiClient.post('/roles', data);
    return response.data.data;
  },

  update: async (
    id: string,
    data: {
      code: string;
      name: string;
      description?: string;
      permissionCodes: string[];
    }
  ): Promise<Role> => {
    const response = await apiClient.put(`/roles/${id}`, data);
    return response.data.data;
  },

  delete: async (id: string): Promise<void> => {
    await apiClient.delete(`/roles/${id}`);
  },
};
