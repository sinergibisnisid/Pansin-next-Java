import apiClient from './api-client';
import type { PermissionRecord } from '@/types';

export const permissionService = {
  getAll: async (): Promise<PermissionRecord[]> => {
    const response = await apiClient.get('/permissions');
    const data = response.data.data;
    return Array.isArray(data) ? data : [];
  },
};
