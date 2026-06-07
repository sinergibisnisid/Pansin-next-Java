import apiClient from './api-client';
import type {
  VaultMonitor,
  DashboardStats,
  ActivityEvent,
  PageResponse,
  BranchActivityData,
  VaultStatusSummaryData,
  RealtimeStatsData,
  ServerMetricData,
  ServerHealthData,
  BranchUtilizationResponse,
} from '@/types';

export const monitoringService = {
  getStats: async (): Promise<DashboardStats> => {
    try {
      const response = await apiClient.get('/monitoring/stats');
      return response.data.data ?? response.data;
    } catch {
      return {
        totalBranches: 0,
        activeVaults: 0,
        activeAlarms: 0,
        onlineDevices: 0,
        totalDevices: 0,
        activeUsers: 0,
        todayActivities: 0,
        mqttConnections: 0,
        serverStatus: 'down',
      };
    }
  },

  getVaults: async (): Promise<VaultMonitor[]> => {
    try {
      const response = await apiClient.get('/monitoring/vaults');
      const data = response.data.data ?? response.data;
      return Array.isArray(data) ? data : [];
    } catch {
      return [];
    }
  },

  getVaultById: async (id: string): Promise<VaultMonitor> => {
    const response = await apiClient.get(`/monitoring/vaults/${id}`);
    return response.data.data ?? response.data;
  },

  getActivities: async (page = 1, pageSize = 20): Promise<PageResponse<ActivityEvent>> => {
    try {
      const response = await apiClient.get('/monitoring/activities', {
        params: { page, pageSize },
      });
      const data = response.data.data;
      // backend returns { data: [...], total, page, pageSize, totalPages }
      if (data?.data) {
        return {
          items: Array.isArray(data.data) ? data.data : [],
          total: data.total ?? 0,
          page: data.page ?? page,
          size: data.pageSize ?? pageSize,
          totalPages: data.totalPages ?? 1,
        };
      }
      return { items: [], total: 0, page, size: pageSize, totalPages: 0 };
    } catch {
      return { items: [], total: 0, page, size: pageSize, totalPages: 0 };
    }
  },

  getBranchActivity: async (params?: { from?: string; to?: string; limit?: number }): Promise<BranchActivityData[]> => {
    const response = await apiClient.get('/monitoring/branch-activity', { params });
    const data = response.data.data;
    return Array.isArray(data) ? data : [];
  },

  getBranchUtilization: async (params?: { from?: string; to?: string; days?: number; limit?: number }): Promise<BranchUtilizationResponse> => {
    const response = await apiClient.get('/monitoring/branch-utilization', { params });
    return response.data.data;
  },

  getVaultStatusSummary: async (): Promise<VaultStatusSummaryData[]> => {
    const response = await apiClient.get('/monitoring/vault-status-summary');
    const data = response.data.data;
    return Array.isArray(data) ? data : [];
  },

  getRealtimeStats: async (params?: { hours?: number }): Promise<RealtimeStatsData[]> => {
    const response = await apiClient.get('/monitoring/realtime-stats', { params });
    const data = response.data.data;
    return Array.isArray(data) ? data : [];
  },

  getServerMetrics: async (params?: { from?: string; to?: string; limit?: number }): Promise<ServerMetricData[]> => {
    const response = await apiClient.get('/monitoring/server-metrics', { params });
    const data = response.data.data;
    return Array.isArray(data) ? data : [];
  },

  getServerHealth: async (): Promise<ServerHealthData> => {
    const response = await apiClient.get('/monitoring/server-health');
    return response.data.data;
  },
};
