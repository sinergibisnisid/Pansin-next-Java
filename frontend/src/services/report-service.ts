import apiClient from './api-client';
import type { AuditLog, PageResponse } from '@/types';

export const reportService = {
  getLogs: async (params?: {
    page?: number;
    size?: number;
    category?: string;
    severity?: string;
    startDate?: string;
    endDate?: string;
  }): Promise<PageResponse<AuditLog>> => {
    try {
      const response = await apiClient.get('/reports/audit-logs', { params });
      const data = response.data.data;
      if (data?.items) return data;
      if (Array.isArray(data)) return { items: data, total: data.length, page: 0, size: data.length, totalPages: 1 };
      return { items: [], total: 0, page: 0, size: 20, totalPages: 0 };
    } catch {
      return { items: [], total: 0, page: 0, size: 20, totalPages: 0 };
    }
  },

  exportCSV: async (params: any) => {
    const response = await apiClient.get('/reports/export/csv', { params, responseType: 'blob' });
    return response.data;
  },

  exportExcel: async (params: any) => {
    const response = await apiClient.get('/reports/export/excel', { params, responseType: 'blob' });
    return response.data;
  },

  exportPDF: async (params: any) => {
    const response = await apiClient.get('/reports/export/pdf', { params, responseType: 'blob' });
    return response.data;
  },
};
