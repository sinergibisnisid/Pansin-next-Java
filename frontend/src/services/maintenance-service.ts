import apiClient from './api-client';

export const maintenanceService = {
  create: async (data: any) => {
    const payload = {
      type: data.type || 'cleaning',
      name: `Maintenance - ${data.type}`,
      description: `Scheduled maintenance for ${data.branchId}`,
      intervalDays: 30,
      nextDueAt: data.scheduledDate ? new Date(data.scheduledDate).toISOString() : null,
    };
    const response = await apiClient.post('/maintenance/plans', payload);
    return response.data.data;
  },

  getAll: async () => {
    const response = await apiClient.get('/maintenance');
    return Array.isArray(response.data.data) ? response.data.data : [];
  },

  updateStatus: async (id: string, status: string) => {
    const response = await apiClient.patch(`/maintenance/${id}/status`, { status });
    return response.data.data;
  },
};
