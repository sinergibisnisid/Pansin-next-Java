import apiClient from './api-client';

export const reportService = {
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
