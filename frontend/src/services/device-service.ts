import apiClient from './api-client';
import type { Device } from '@/types';

export const deviceService = {
  create: async (data: {
    name: string;
    serialNumber: string;
    type: string;
    ipAddress?: string;
    branchId?: string;
  }): Promise<Device> => {
    const payload = {
      branchId: data.branchId || null,
      deviceCode: data.serialNumber,
      name: data.name,
      type: data.type,
      ipAddress: data.ipAddress || '',
    };
    const response = await apiClient.post('/devices', payload);
    return response.data.data;
  },

  getAll: async (): Promise<Device[]> => {
    const response = await apiClient.get('/devices');
    return Array.isArray(response.data.data) ? response.data.data : [];
  },
};
