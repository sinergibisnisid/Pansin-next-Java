import apiClient from './api-client';

interface VerifyPinResponse {
  sessionToken: string;
}

export const pinGateService = {
  verifyPin: async (pin: string): Promise<boolean> => {
    try {
      const response = await apiClient.post<{ success: boolean; data: string }>('/public/verify-pin', {
        pin,
      });
      // Backend returns success: true and data: sessionToken as string
      return response.data.success === true && response.data.data !== undefined;
    } catch (error) {
      return false;
    }
  },
};
