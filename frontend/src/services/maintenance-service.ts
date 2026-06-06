import apiClient from './api-client';
import type { MaintenancePlan, MaintenanceLog, PageResponse } from '@/types';

export const maintenanceService = {
  getPlans: async (params?: { page?: number; size?: number }): Promise<PageResponse<MaintenancePlan>> => {
    const response = await apiClient.get('/maintenance/plans', { params });
    return response.data.data;
  },

  createPlan: async (data: {
    vaultId?: string;
    deviceId?: string;
    type: string;
    name: string;
    description?: string;
    intervalDays: number;
    nextDueAt?: string;
  }): Promise<MaintenancePlan> => {
    const payload = {
      vaultId: data.vaultId || null,
      deviceId: data.deviceId || null,
      type: data.type,
      name: data.name,
      description: data.description || null,
      intervalDays: data.intervalDays || 30,
      nextDueAt: data.nextDueAt ? new Date(data.nextDueAt).toISOString() : null,
    };
    const response = await apiClient.post('/maintenance/plans', payload);
    return response.data.data;
  },

  updatePlan: async (id: string, data: {
    vaultId?: string;
    deviceId?: string;
    type?: string;
    name?: string;
    description?: string;
    intervalDays?: number;
    nextDueAt?: string;
  }): Promise<MaintenancePlan> => {
    const payload = {
      ...data,
      nextDueAt: data.nextDueAt ? new Date(data.nextDueAt).toISOString() : undefined,
    };
    const response = await apiClient.put(`/maintenance/plans/${id}`, payload);
    return response.data.data;
  },

  deletePlan: async (id: string): Promise<void> => {
    await apiClient.delete(`/maintenance/plans/${id}`);
  },

  getLogs: async (params?: { page?: number; size?: number }): Promise<MaintenanceLog[]> => {
    const response = await apiClient.get('/maintenance/logs', { params });
    const data = response.data.data;
    if (data?.items) return data.items;
    if (Array.isArray(data)) return data;
    return [];
  },

  recordLog: async (data: {
    planId?: string;
    vaultId?: string;
    deviceId?: string;
    type: string;
    notes?: string;
    status: string;
  }): Promise<MaintenanceLog> => {
    const payload = {
      planId: data.planId || null,
      vaultId: data.vaultId || null,
      deviceId: data.deviceId || null,
      type: data.type,
      notes: data.notes || null,
      status: data.status || 'COMPLETED',
    };
    const response = await apiClient.post('/maintenance/logs', payload);
    return response.data.data;
  },
};
