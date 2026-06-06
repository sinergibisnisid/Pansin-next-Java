import apiClient from './api-client';
import type { Device, PageResponse } from '@/types';

export const deviceService = {
  getAll: async (params?: {
    page?: number;
    size?: number;
    search?: string;
  }): Promise<PageResponse<Device>> => {
    const response = await apiClient.get('/devices', { params });
    return response.data.data;
  },

  getById: async (id: string): Promise<Device> => {
    const response = await apiClient.get(`/devices/${id}`);
    return response.data.data;
  },

  create: async (data: {
    branchId: string;
    vaultId?: string;
    deviceCode: string;
    name: string;
    type: string;
    ipAddress?: string;
    macAddress?: string;
    firmwareVersion?: string;
  }): Promise<Device> => {
    const response = await apiClient.post('/devices', data);
    return response.data.data;
  },

  update: async (id: string, data: {
    branchId?: string;
    vaultId?: string;
    deviceCode?: string;
    name?: string;
    type?: string;
    ipAddress?: string;
    macAddress?: string;
    firmwareVersion?: string;
  }): Promise<Device> => {
    const response = await apiClient.put(`/devices/${id}`, data);
    return response.data.data;
  },

  delete: async (id: string): Promise<void> => {
    await apiClient.delete(`/devices/${id}`);
  },
};
