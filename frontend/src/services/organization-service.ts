import apiClient from './api-client';
import type { Organization } from '@/types';

export const organizationService = {
  create: async (data: {
    name: string;
    code: string;
    type: string;
    address?: string;
    city?: string;
    province?: string;
    phone?: string;
    email?: string;
  }): Promise<Organization> => {
    const payload = {
      code: data.code,
      name: data.name,
      description: `${data.type} - ${data.city || ''}`,
      address: data.address || '',
      phone: data.phone || '',
      email: data.email || '',
    };
    const response = await apiClient.post('/organizations', payload);
    return response.data.data;
  },

  getAll: async (): Promise<Organization[]> => {
    const response = await apiClient.get('/organizations');
    return Array.isArray(response.data.data) ? response.data.data : [];
  },
};
