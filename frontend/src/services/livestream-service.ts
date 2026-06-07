import apiClient from './api-client';
import type { LivestreamSession, PageResponse, Snapshot } from '@/types';

export const livestreamService = {
  getSessions: async (): Promise<LivestreamSession[]> => {
    const response = await apiClient.get('/livestream');
    const data = response.data.data;
    return Array.isArray(data) ? data : [];
  },

  start: async (payload: { vaultId: string; deviceId?: string }): Promise<LivestreamSession> => {
    const response = await apiClient.post('/livestream/start', payload);
    return response.data.data;
  },

  stop: async (id: string): Promise<LivestreamSession> => {
    const response = await apiClient.post(`/livestream/${id}/stop`);
    return response.data.data;
  },

  health: async (): Promise<string> => {
    const response = await apiClient.get('/livestream/health');
    return response.data.data ?? 'UNKNOWN';
  },

  getSnapshots: async (params?: { vaultId?: string; page?: number; size?: number }): Promise<PageResponse<Snapshot>> => {
    const response = await apiClient.get('/snapshots', { params });
    const data = response.data.data;
    return {
      items: data?.items ?? data?.content ?? [],
      total: data?.total ?? data?.totalElements ?? 0,
      page: data?.page ?? data?.number ?? 0,
      size: data?.size ?? params?.size ?? 20,
      totalPages: data?.totalPages ?? 0,
    };
  },

  getSnapshotFileUrl: (id: string) => `/snapshots/${id}/file`,
};
