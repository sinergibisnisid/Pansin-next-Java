import apiClient from './api-client';
import type { AppSetting } from '@/types';

export const settingsService = {
  getAll: async (): Promise<AppSetting[]> => {
    const response = await apiClient.get('/settings');
    const data = response.data.data;
    return Array.isArray(data) ? data : [];
  },

  getByKey: async (key: string): Promise<AppSetting> => {
    const response = await apiClient.get(`/settings/${key}`);
    return response.data.data;
  },

  update: async (key: string, value: Record<string, unknown>, options?: { description?: string; publicSetting?: boolean }): Promise<AppSetting> => {
    const response = await apiClient.put(`/settings/${key}`, {
      value,
      description: options?.description,
      publicSetting: options?.publicSetting,
    });
    return response.data.data;
  },
};
