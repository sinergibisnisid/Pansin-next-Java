import apiClient from './api-client';
import type {
  VaultMonitor,
  DashboardStats,
  ActivityEvent,
  PageResponse,
} from '@/types';

export const monitoringService = {
  getStats: async (): Promise<DashboardStats> => {
    const response = await apiClient.get('/monitoring/stats');
    return response.data.data ?? response.data;
  },

  getVaults: async (): Promise<VaultMonitor[]> => {
    const response = await apiClient.get('/monitoring/vaults');
    const data = response.data.data ?? response.data;
    return Array.isArray(data) ? data : [];
  },

  getVaultById: async (id: string): Promise<VaultMonitor> => {
    const response = await apiClient.get(`/monitoring/vaults/${id}`);
    return response.data.data ?? response.data;
  },

  getActivities: async (page = 0, size = 20): Promise<PageResponse<ActivityEvent>> => {
    const response = await apiClient.get('/monitoring/activities', {
      params: { page, size },
    });
    return response.data.data;
  },
};
