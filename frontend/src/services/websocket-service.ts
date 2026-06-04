import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { useWebSocketStore } from '@/stores/websocket-store';
import { useNotificationStore } from '@/stores/notification-store';
import type { VaultStatusUpdate, AlarmEvent, Notification } from '@/types';

class WebSocketService {
  private client: Client | null = null;
  private reconnectTimer: NodeJS.Timeout | null = null;
  private maxReconnectAttempts = 5;
  private reconnectDelay = 3000;

  connect() {
    const wsUrl = process.env.NEXT_PUBLIC_WS_URL || 'http://localhost:8000';
    const store = useWebSocketStore.getState();

    store.setStatus('connecting');

    this.client = new Client({
      webSocketFactory: () => new SockJS(`${wsUrl}/ws`),
      
      onConnect: () => {
        console.log('[WebSocket] Connected');
        store.setStatus('connected');
        store.setLastConnected(new Date().toISOString());
        store.setReconnectAttempts(0);
        store.setError(null);
        
        this.subscribeToTopics();
      },

      onDisconnect: () => {
        console.log('[WebSocket] Disconnected');
        store.setStatus('disconnected');
        this.scheduleReconnect();
      },

      onStompError: (frame) => {
        console.error('[WebSocket] Error:', frame);
        store.setStatus('error');
        store.setError(frame.headers.message || 'Unknown error');
        this.scheduleReconnect();
      },

      reconnectDelay: this.reconnectDelay,
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,
    });

    this.client.activate();
  }

  private subscribeToTopics() {
    if (!this.client) return;

    // Subscribe to vault updates
    this.client.subscribe('/topic/vault', (message) => {
      const data: VaultStatusUpdate = JSON.parse(message.body);
      this.handleVaultUpdate(data);
    });

    // Subscribe to device updates
    this.client.subscribe('/topic/device', (message) => {
      const data = JSON.parse(message.body);
      this.handleDeviceUpdate(data);
    });

    // Subscribe to alarm events
    this.client.subscribe('/topic/alarm', (message) => {
      const data: AlarmEvent = JSON.parse(message.body);
      this.handleAlarmEvent(data);
    });
  }

  private handleVaultUpdate(data: VaultStatusUpdate) {
    console.log('[WebSocket] Vault update:', data);
    
    const notification: Notification = {
      id: `vault-${data.vaultId}-${Date.now()}`,
      type: data.status === 'alarm' ? 'error' : 'info',
      category: 'vault',
      title: 'Vault Status Update',
      message: `Vault status changed to ${data.status}`,
      timestamp: data.timestamp,
      read: false,
    };

    useNotificationStore.getState().addNotification(notification);
  }

  private handleDeviceUpdate(data: any) {
    console.log('[WebSocket] Device update:', data);
    
    const notification: Notification = {
      id: `device-${data.deviceId}-${Date.now()}`,
      type: data.status === 'offline' ? 'warning' : 'info',
      category: 'device',
      title: 'Device Update',
      message: data.message || 'Device status changed',
      timestamp: data.timestamp || new Date().toISOString(),
      read: false,
    };

    useNotificationStore.getState().addNotification(notification);
  }

  private handleAlarmEvent(data: AlarmEvent) {
    console.log('[WebSocket] Alarm event:', data);
    
    const notification: Notification = {
      id: `alarm-${data.vaultId}-${Date.now()}`,
      type: data.severity === 'critical' ? 'error' : 'warning',
      category: 'alarm',
      title: `🚨 Alarm: ${data.branchName}`,
      message: data.message,
      timestamp: data.timestamp,
      read: false,
    };

    useNotificationStore.getState().addNotification(notification);
  }

  private scheduleReconnect() {
    const store = useWebSocketStore.getState();
    
    if (store.reconnectAttempts >= this.maxReconnectAttempts) {
      console.log('[WebSocket] Max reconnect attempts reached');
      return;
    }

    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer);
    }

    this.reconnectTimer = setTimeout(() => {
      console.log('[WebSocket] Attempting reconnect...');
      store.setReconnectAttempts(store.reconnectAttempts + 1);
      this.connect();
    }, this.reconnectDelay);
  }

  disconnect() {
    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer);
      this.reconnectTimer = null;
    }

    if (this.client) {
      this.client.deactivate();
      this.client = null;
    }

    useWebSocketStore.getState().reset();
  }

  isConnected() {
    return this.client?.connected || false;
  }
}

export const websocketService = new WebSocketService();
