import apiClient from './api-client';

export interface MqttTopicInfo {
  topic: string;
  description: string;
  qos: number;
}

export interface MqttStatus {
  connected: boolean;
  brokerUrl: string;
  clientId: string;
  username: string;
  useTls: boolean;
  qos: number;
  keepAlive: number;
  topics: MqttTopicInfo[];
}

export const mqttService = {
  getStatus: async (): Promise<MqttStatus> => {
    try {
      const response = await apiClient.get('/mqtt/status');
      return response.data.data;
    } catch {
      return {
        connected: false,
        brokerUrl: '',
        clientId: '',
        username: '',
        useTls: false,
        qos: 1,
        keepAlive: 30,
        topics: [],
      };
    }
  },
};
