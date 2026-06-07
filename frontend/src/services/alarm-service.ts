import apiClient from './api-client';
import type { AlarmLog, PageResponse } from '@/types';

export const alarmService = {
  getAll: async (params?: { page?: number; size?: number; unacknowledgedOnly?: boolean }): Promise<PageResponse<AlarmLog>> => {
    const response = await apiClient.get('/alarms', { params });
    const data = response.data.data;
    return {
      items: data?.items ?? data?.content ?? [],
      total: data?.total ?? data?.totalElements ?? 0,
      page: data?.page ?? data?.number ?? 0,
      size: data?.size ?? params?.size ?? 20,
      totalPages: data?.totalPages ?? 0,
    };
  },

  acknowledge: async (id: string): Promise<AlarmLog> => {
    const response = await apiClient.post(`/alarms/${id}/acknowledge`);
    return response.data.data;
  },
};
