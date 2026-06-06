// ============================================================
// PANSIN ACCESS - Smart Vault Monitoring System
// Type Definitions
// ============================================================

// Auth Types
export interface User {
  id: string;
  username: string;
  email: string;
  fullName: string;
  phone?: string;
  nik?: string;
  employeeId?: string;
  avatarUrl?: string;
  organizationId?: string;
  branchId?: string;
  enabled: boolean;
  locked: boolean;
  roles: string[];
  permissions: string[];
  lastLoginAt?: string;
  createdAt: string;
}

export type UserRole = 
  | 'SUPER_ADMIN'
  | 'ADMIN_PUSAT'
  | 'ADMIN_CABANG'
  | 'OPERATOR'
  | 'SECURITY'
  | 'MAINTENANCE'
  | 'VIEWER'
  | 'VIEWER_CCTV';

export type UserStatus = 'active' | 'inactive' | 'locked';

// Pagination Types
export interface PageResponse<T> {
  items: T[];
  total: number;
  page: number;
  size: number;
  totalPages: number;
}

export type Permission =
  | 'dashboard.view'
  | 'monitoring.view'
  | 'monitoring.control'
  | 'organization.view'
  | 'organization.manage'
  | 'users.view'
  | 'users.manage'
  | 'devices.view'
  | 'devices.manage'
  | 'reports.view'
  | 'reports.export'
  | 'maintenance.view'
  | 'maintenance.manage'
  | 'mqtt.view'
  | 'mqtt.manage'
  | 'notifications.view'
  | 'notifications.manage'
  | 'settings.view'
  | 'settings.manage'
  | 'server.view';

export interface AuthTokens {
  accessToken: string;
  refreshToken: string;
  expiresAt: number;
}

export interface LoginCredentials {
  username: string;
  password: string;
  rememberMe?: boolean;
}

export interface OTPVerification {
  userId: string;
  otp: string;
  method: 'whatsapp' | 'email';
}

// Organization Types (Frontend - deprecated, use Backend types)
export interface Organization {
  id: string;
  name: string;
  code: string;
  type: 'headquarters' | 'branch';
  parentId?: string;
  address: string;
  city: string;
  province: string;
  phone: string;
  email: string;
  status: 'active' | 'inactive';
  totalVaults: number;
  totalDevices: number;
  createdAt: string;
  updatedAt: string;
}

export interface Branch extends Organization {
  type: 'branch';
  parentId: string;
  headName: string;
  latitude?: number;
  longitude?: number;
}

// Backend Organization Types (matches backend DTOs exactly)
export interface BackendOrganization {
  id: string;
  code: string;
  name: string;
  description?: string;
  address: string;
  phone: string;
  email: string;
  active: boolean;
}

export interface BackendBranch {
  id: string;
  organizationId: string;
  code: string;
  name: string;
  address: string;
  city: string;
  province: string;
  postalCode: string;
  phone: string;
  email: string;
  latitude?: number;
  longitude?: number;
  timezone: string;
  active: boolean;
}

// Vault & Monitoring Types
export type VaultStatus = 'open' | 'closed' | 'locked' | 'alarm' | 'maintenance';
export type DeviceStatus = 'ONLINE' | 'OFFLINE' | 'DEGRADED' | 'MAINTENANCE' | 'DECOMMISSIONED';
export type AlarmStatus = 'active' | 'acknowledged' | 'resolved' | 'silenced';

export interface VaultMonitor {
  id: string;
  branchId: string;
  branchName: string;
  branchCode: string;
  vaultName: string;
  status: VaultStatus;
  deviceStatus: DeviceStatus;
  alarmStatus: AlarmStatus | null;
  currentUser: string | null;
  entryTime: string | null;
  duration: number | null;
  temperature: number;
  humidity: number;
  lastActivity: string;
  streamUrl: string | null;
  snapshotUrl: string | null;
  devices: VaultDevice[];
}

export interface VaultDevice {
  id: string;
  name: string;
  type: DeviceType;
  status: DeviceStatus;
  ipAddress: string;
  lastHeartbeat: string;
  signalQuality: number;
}

export type DeviceType =
  | 'controller'
  | 'fingerprint'
  | 'camera'
  | 'sensor_door'
  | 'sensor_motion'
  | 'sensor_temperature'
  | 'alarm'
  | 'lock';

// Device Management Types
export interface DeviceBranch {
  id: string;
  name: string;
  code: string;
}

export interface DeviceVault {
  id: string;
  name: string;
}

export interface Device {
  id: string;
  deviceCode: string;
  name: string;
  type: DeviceType;
  ipAddress: string;
  macAddress: string;
  firmwareVersion: string;
  signalQuality: number;
  status: DeviceStatus;
  active: boolean;
  lastHeartbeat: string | null;
  branch?: DeviceBranch;
  vault?: DeviceVault;
  createdAt: string;
  updatedAt: string;
}

// Report Types
export interface AuditLog {
  id: string;
  timestamp: string;
  userId: string;
  userName: string;
  branchId: string;
  branchName: string;
  action: string;
  category: AuditCategory;
  details: string;
  ipAddress: string;
  severity: 'info' | 'warning' | 'critical';
}

export type AuditCategory =
  | 'auth'
  | 'vault_access'
  | 'device'
  | 'alarm'
  | 'system'
  | 'maintenance'
  | 'configuration';

// Notification Types
export interface Notification {
  id: string;
  title: string;
  message: string;
  type: 'info' | 'warning' | 'error' | 'success';
  category: string;
  read: boolean;
  timestamp: string;
  actionUrl?: string;
}

export interface NotificationConfig {
  whatsappEnabled: boolean;
  whatsappNumber: string;
  emailEnabled: boolean;
  emailAddress: string;
  dailyReport: boolean;
  weeklyReport: boolean;
  monthlyReport: boolean;
  alarmNotification: boolean;
  maintenanceReminder: boolean;
}

// MQTT Types
export interface MQTTBroker {
  id: string;
  name: string;
  host: string;
  port: number;
  username: string;
  useTLS: boolean;
  status: 'connected' | 'disconnected' | 'error';
  lastConnected: string;
  topics: MQTTTopic[];
}

export interface MQTTTopic {
  id: string;
  topic: string;
  description: string;
  qos: 0 | 1 | 2;
  retained: boolean;
  lastMessage: string | null;
  lastReceived: string | null;
}

// Maintenance Types
export interface MaintenanceRelation {
  id: string;
  name: string;
}

export interface MaintenanceUserRef {
  id: string;
  fullName: string;
  username: string;
}

export interface MaintenancePlan {
  id: string;
  vault?: MaintenanceRelation;
  device?: MaintenanceRelation & { deviceCode?: string };
  type: string;
  name: string;
  description?: string;
  intervalDays: number;
  nextDueAt?: string;
  lastDoneAt?: string;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface MaintenanceLog {
  id: string;
  plan?: MaintenancePlan;
  vault?: MaintenanceRelation;
  device?: MaintenanceRelation & { deviceCode?: string };
  performedBy?: MaintenanceUserRef;
  type: string;
  notes?: string;
  status: string;
  performedAt: string;
  createdAt: string;
}

/** @deprecated Use MaintenancePlan instead */
export interface MaintenanceSchedule {
  id: string;
  type: string;
  status: string;
  notes?: string;
  createdAt: string;
}

export type MaintenanceType = 'cleaning' | 'lubrication' | 'inspection' | 'repair' | 'calibration';

// Server Monitoring Types
export interface ServerMetrics {
  cpuUsage: number;
  ramUsage: number;
  ramTotal: number;
  storageUsage: number;
  storageTotal: number;
  networkIn: number;
  networkOut: number;
  uptime: number;
  services: ServiceHealth[];
}

export interface ServiceHealth {
  name: string;
  status: 'healthy' | 'degraded' | 'down';
  responseTime: number;
  lastCheck: string;
}

// Table & Pagination Types
export interface PaginatedResponse<T> {
  data: T[];
  total: number;
  page: number;
  pageSize: number;
  totalPages: number;
}

export interface TableFilter {
  field: string;
  value: string | string[];
  operator: 'eq' | 'contains' | 'in' | 'between' | 'gt' | 'lt';
}

export interface TableSort {
  field: string;
  direction: 'asc' | 'desc';
}

// Dashboard Stats
export interface DashboardStats {
  totalBranches: number;
  activeVaults: number;
  activeAlarms: number;
  onlineDevices: number;
  totalDevices: number;
  activeUsers: number;
  todayActivities: number;
  mqttConnections: number;
  serverStatus: 'healthy' | 'degraded' | 'down';
}

// Activity Timeline
export interface ActivityEvent {
  id: string;
  timestamp: string;
  type: 'vault_open' | 'vault_close' | 'alarm' | 'device_offline' | 'login' | 'maintenance';
  title: string;
  description: string;
  branchName: string;
  severity: 'info' | 'warning' | 'critical';
  userId?: string;
  userName?: string;
}

// WebSocket Event Types
export interface WSEvent {
  type: string;
  payload: unknown;
  timestamp: string;
}

export interface VaultStatusUpdate {
  vaultId: string;
  status: VaultStatus;
  deviceStatus: DeviceStatus;
  currentUser: string | null;
  timestamp: string;
}

export interface AlarmEvent {
  vaultId: string;
  branchName: string;
  alarmType: string;
  severity: 'warning' | 'critical';
  message: string;
  timestamp: string;
}
