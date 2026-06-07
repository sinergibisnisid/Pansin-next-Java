'use client';

import { useEffect, useState } from 'react';
import { motion } from 'framer-motion';
import {
  Cpu,
  HardDrive,
  MemoryStick,
  Network,
  Activity,
  CheckCircle,
  AlertCircle,
  XCircle,
  RefreshCw,
} from 'lucide-react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Progress } from '@/components/ui/progress';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { cn } from '@/lib/utils';
import { monitoringService } from '@/services';
import type { ServerHealthData, ServerMetricData } from '@/types';
import {
  AreaChart,
  Area,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
} from 'recharts';

type ServiceStatus = 'healthy' | 'degraded' | 'down';

const statusIcons = {
  healthy: CheckCircle,
  degraded: AlertCircle,
  down: XCircle,
};

const statusColors = {
  healthy: 'text-emerald-400 bg-emerald-500/20 border-emerald-500/30',
  degraded: 'text-amber-400 bg-amber-500/20 border-amber-500/30',
  down: 'text-red-400 bg-red-500/20 border-red-500/30',
};

function normalizeStatus(status?: string): ServiceStatus {
  const value = status?.toUpperCase();
  if (value === 'UP' || value === 'HEALTHY') return 'healthy';
  if (value === 'DEGRADED' || value === 'WARN') return 'degraded';
  return 'down';
}

function toNumber(value: number | undefined, fallback = 0) {
  return typeof value === 'number' && Number.isFinite(value) ? value : fallback;
}

export default function ServerPage() {
  const [metricData, setMetricData] = useState<{ time: string; cpu: number; ram: number; disk: number }[]>([]);
  const [health, setHealth] = useState<ServerHealthData | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchServerData = async () => {
    setError(null);
    try {
      const [metrics, healthData] = await Promise.all([
        monitoringService.getServerMetrics({ limit: 60 }),
        monitoringService.getServerHealth(),
      ]);
      setHealth(healthData);
      setMetricData(metrics.map((metric: ServerMetricData) => ({
        time: metric.timestamp ? new Date(metric.timestamp).toLocaleTimeString('id-ID', { hour: '2-digit', minute: '2-digit' }) : '-',
        cpu: toNumber(metric.cpuUsage),
        ram: toNumber(metric.memoryUsage),
        disk: toNumber(metric.diskUsage),
      })));
    } catch (err) {
      console.error('Failed to fetch server monitoring data:', err);
      setError('Gagal memuat data server monitoring dari backend.');
      setMetricData([]);
      setHealth(null);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    void Promise.resolve().then(fetchServerData);
    const interval = window.setInterval(fetchServerData, 60000);
    return () => window.clearInterval(interval);
  }, []);

  const cpuUsage = toNumber(health?.cpuUsage);
  const ramUsage = toNumber(health?.memoryUsage);
  const storageUsage = toNumber(health?.diskUsage);
  const websocketSessions = health?.websocketSessions ?? 0;

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
        <div>
          <h1 className="text-2xl font-bold tracking-tight">Server Monitoring</h1>
          <p className="text-sm text-muted-foreground mt-1">
            Realtime server resource usage and service health from backend metrics
          </p>
        </div>
        <Button variant="outline" size="sm" onClick={fetchServerData} disabled={isLoading} className="gap-1.5">
          <RefreshCw className={cn('h-3.5 w-3.5', isLoading && 'animate-spin')} />
          Refresh
        </Button>
      </div>

      {error && (
        <div className="rounded-lg border border-red-500/20 bg-red-500/10 px-4 py-3 text-sm text-red-400">
          {error}
        </div>
      )}

      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <motion.div initial={{ opacity: 0, y: 10 }} animate={{ opacity: 1, y: 0 }} className="rounded-xl border border-border/40 bg-card/50 p-4 space-y-3">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2"><Cpu className="h-4 w-4 text-blue-400" /><span className="text-xs font-medium text-muted-foreground">CPU Usage</span></div>
            <span className="text-lg font-bold">{cpuUsage.toFixed(1)}%</span>
          </div>
          <Progress value={cpuUsage} className="h-2" />
          <p className="text-[10px] text-muted-foreground">Latest backend sample</p>
        </motion.div>

        <motion.div initial={{ opacity: 0, y: 10 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: 0.1 }} className="rounded-xl border border-border/40 bg-card/50 p-4 space-y-3">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2"><MemoryStick className="h-4 w-4 text-cyan-400" /><span className="text-xs font-medium text-muted-foreground">RAM Usage</span></div>
            <span className="text-lg font-bold">{ramUsage.toFixed(1)}%</span>
          </div>
          <Progress value={ramUsage} className="h-2" />
          <p className="text-[10px] text-muted-foreground">Latest backend sample</p>
        </motion.div>

        <motion.div initial={{ opacity: 0, y: 10 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: 0.2 }} className="rounded-xl border border-border/40 bg-card/50 p-4 space-y-3">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2"><HardDrive className="h-4 w-4 text-emerald-400" /><span className="text-xs font-medium text-muted-foreground">Storage</span></div>
            <span className="text-lg font-bold">{storageUsage.toFixed(1)}%</span>
          </div>
          <Progress value={storageUsage} className="h-2" />
          <p className="text-[10px] text-muted-foreground">Latest backend sample</p>
        </motion.div>

        <motion.div initial={{ opacity: 0, y: 10 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: 0.3 }} className="rounded-xl border border-border/40 bg-card/50 p-4 space-y-3">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2"><Network className="h-4 w-4 text-amber-400" /><span className="text-xs font-medium text-muted-foreground">Connections</span></div>
            <Activity className="h-4 w-4 text-emerald-400" />
          </div>
          <div className="flex items-center justify-between text-xs">
            <span className="text-muted-foreground">MQTT: <span className="text-foreground font-medium">{health?.mqttConnected ? 'UP' : 'DOWN'}</span></span>
            <span className="text-muted-foreground">WS: <span className="text-foreground font-medium">{websocketSessions}</span></span>
          </div>
          <p className="text-[10px] text-muted-foreground">Live backend health</p>
        </motion.div>
      </div>

      <Card className="border-border/40 bg-card/50">
        <CardHeader className="pb-2"><CardTitle className="text-sm font-medium">Resource Usage</CardTitle></CardHeader>
        <CardContent>
          <div className="h-[280px]">
            {metricData.length === 0 && !isLoading ? (
              <div className="h-full flex items-center justify-center text-xs text-muted-foreground">No server metrics available yet.</div>
            ) : (
              <ResponsiveContainer width="100%" height="100%">
                <AreaChart data={metricData} margin={{ top: 5, right: 10, left: -10, bottom: 5 }}>
                  <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.05)" />
                  <XAxis dataKey="time" tick={{ fontSize: 10, fill: '#94a3b8' }} axisLine={false} tickLine={false} />
                  <YAxis tick={{ fontSize: 10, fill: '#94a3b8' }} axisLine={false} tickLine={false} domain={[0, 100]} />
                  <Tooltip contentStyle={{ backgroundColor: 'rgba(15, 23, 42, 0.9)', border: '1px solid rgba(255,255,255,0.1)', borderRadius: '8px', fontSize: '12px', color: '#e2e8f0' }} />
                  <Area type="monotone" dataKey="cpu" stroke="#3b82f6" strokeWidth={2} fill="#3b82f633" name="CPU %" />
                  <Area type="monotone" dataKey="ram" stroke="#06b6d4" strokeWidth={2} fill="#06b6d433" name="RAM %" />
                  <Area type="monotone" dataKey="disk" stroke="#10b981" strokeWidth={1.5} fill="#10b98133" name="Disk %" />
                </AreaChart>
              </ResponsiveContainer>
            )}
          </div>
        </CardContent>
      </Card>

      <Card className="border-border/40 bg-card/50">
        <CardHeader className="pb-3"><CardTitle className="text-sm font-medium">Service Health</CardTitle></CardHeader>
        <CardContent>
          <div className="grid gap-2 sm:grid-cols-2">
            {(health?.services ?? []).map((service) => {
              const status = normalizeStatus(service.status);
              const Icon = statusIcons[status];
              return (
                <div key={service.name} className="flex items-center justify-between rounded-lg border border-border/30 bg-muted/20 p-3 hover:bg-muted/30 transition-colors">
                  <div className="flex items-center gap-3">
                    <div className={cn('flex h-8 w-8 items-center justify-center rounded-lg border', statusColors[status])}><Icon className="h-4 w-4" /></div>
                    <div><p className="text-sm font-medium">{service.name}</p><p className="text-[10px] text-muted-foreground">{service.description ?? '-'}</p></div>
                  </div>
                  <Badge variant="outline" className={cn('text-[9px]', statusColors[status])}>{status}</Badge>
                </div>
              );
            })}
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
