'use client';

import { useEffect, useRef, useCallback } from 'react';
import { io, type Socket } from 'socket.io-client';
import { WS_URL, RECONNECT_INTERVAL, MAX_RECONNECT_ATTEMPTS } from '@/constants';
import { useWebSocketStore } from '@/stores';
import { useMonitoringStore } from '@/stores';
import { useNotificationStore } from '@/stores';
import type { VaultStatusUpdate, AlarmEvent, Notification } from '@/types';
import { generateId } from '@/lib/utils';

export function useSocket() {
  const socketRef = useRef<Socket | null>(null);
  const reconnectTimerRef = useRef<NodeJS.Timeout | null>(null);
  const reconnectAttemptsRef = useRef(0);
  const shouldReconnectRef = useRef(true);

  const { setStatus, setReconnectAttempts, setLastConnected, setError } = useWebSocketStore();
  const { updateVaultStatus, addAlarm } = useMonitoringStore();
  const { addNotification } = useNotificationStore();

  const connectRef = useRef<() => void>(() => {});

  const attemptReconnect = useCallback(() => {
    if (!shouldReconnectRef.current) return;

    if (reconnectAttemptsRef.current >= MAX_RECONNECT_ATTEMPTS) {
      setError('Max reconnection attempts reached');
      return;
    }

    if (reconnectTimerRef.current) clearTimeout(reconnectTimerRef.current);
    reconnectTimerRef.current = setTimeout(() => {
      reconnectTimerRef.current = null;
      if (!shouldReconnectRef.current) return;

      reconnectAttemptsRef.current += 1;
      setReconnectAttempts(reconnectAttemptsRef.current);
      connectRef.current();
    }, RECONNECT_INTERVAL);
  }, [setReconnectAttempts, setError]);

  const connect = useCallback(() => {
    if (socketRef.current?.connected || socketRef.current?.active) return;

    shouldReconnectRef.current = true;
    setStatus('connecting');

    const socket = io(WS_URL, {
      transports: ['websocket'],
      reconnection: false,
      timeout: 10000,
    });

    socket.on('connect', () => {
      reconnectAttemptsRef.current = 0;
      setStatus('connected');
      setLastConnected(new Date().toISOString());
      setReconnectAttempts(0);
      setError(null);
    });

    socket.on('disconnect', () => {
      setStatus('disconnected');
      socket.removeAllListeners();
      if (socketRef.current === socket) {
        socketRef.current = null;
      }
      attemptReconnect();
    });

    socket.on('connect_error', (error) => {
      setStatus('error');
      setError(error.message);
      socket.removeAllListeners();
      if (socketRef.current === socket) {
        socketRef.current = null;
      }
      socket.disconnect();
      attemptReconnect();
    });

    socket.on('vault:status', (data: VaultStatusUpdate) => updateVaultStatus(data));

    socket.on('alarm:triggered', (data: AlarmEvent) => {
      addAlarm(data);
      addNotification({
        id: generateId(),
        title: 'Alarm Triggered',
        message: `${data.alarmType} at ${data.branchName}`,
        type: 'error',
        category: 'alarm',
        read: false,
        timestamp: data.timestamp,
      });
    });

    socket.on('notification', (data: Notification) => addNotification(data));

    socketRef.current = socket;
  }, [setStatus, setLastConnected, setReconnectAttempts, setError, updateVaultStatus, addAlarm, addNotification, attemptReconnect]);

  useEffect(() => {
    connectRef.current = connect;
  }, [connect]);

  const disconnect = useCallback(() => {
    shouldReconnectRef.current = false;
    if (reconnectTimerRef.current) clearTimeout(reconnectTimerRef.current);
    reconnectTimerRef.current = null;
    reconnectAttemptsRef.current = 0;
    setReconnectAttempts(0);
    if (socketRef.current) {
      socketRef.current.removeAllListeners();
      socketRef.current.disconnect();
      socketRef.current = null;
    }
    setStatus('disconnected');
  }, [setStatus, setReconnectAttempts]);

  const emit = useCallback((event: string, data?: unknown) => {
    socketRef.current?.emit(event, data);
  }, []);

  const on = useCallback((event: string, handler: (...args: unknown[]) => void) => {
    socketRef.current?.on(event, handler);
    return () => socketRef.current?.off(event, handler);
  }, []);

  const getSocket = useCallback(() => socketRef.current, []);

  useEffect(() => () => disconnect(), [disconnect]);

  return { connect, disconnect, emit, on, getSocket };
}
