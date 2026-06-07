'use client';

import { useEffect, useState } from 'react';
import { cn } from '@/lib/utils';
import { Progress } from '@/components/ui/progress';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { CheckCircle, AlertCircle, XCircle } from 'lucide-react';
import { monitoringService } from '@/services';
import type { ServerHealthData } from '@/types';

type HealthStatus = 'healthy' | 'degraded' | 'down';

const statusIcons = {
  healthy: CheckCircle,
  degraded: AlertCircle,
  down: XCircle,
};

const statusColors = {
  healthy: 'text-emerald-400',
  degraded: 'text-amber-400',
  down: 'text-red-400',
};

function normalizeStatus(status?: string): HealthStatus {
  const value = status?.toUpperCase();
  if (value === 'UP' || value === 'HEALTHY') return 'healthy';
  if (value === 'DEGRADED' || value === 'WARN') return 'degraded';
  return 'down';
}

export function SystemHealth() {
  const [health, setHealth] = useState<ServerHealthData | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const fetchHealth = async () => {
      try {
        setHealth(await monitoringService.getServerHealth());
      } catch (error) {
        console.error('Failed to fetch system health:', error);
        setHealth(null);
      } finally {
        setIsLoading(false);
      }
    };
    fetchHealth();
    const interval = window.setInterval(fetchHealth, 60000);
    return () => window.clearInterval(interval);
  }, []);

  const metrics = [
    { label: 'CPU Usage', value: health?.cpuUsage ?? 0 },
    { label: 'RAM Usage', value: health?.memoryUsage ?? 0 },
    { label: 'Storage', value: health?.diskUsage ?? 0 },
    { label: 'WebSocket Sessions', value: health?.websocketSessions ?? 0, suffix: '' },
  ];

  return (
    <Card className="border-border/40 bg-card/50 backdrop-blur-sm">
      <CardHeader className="pb-3">
        <CardTitle className="text-sm font-medium">System Health</CardTitle>
      </CardHeader>
      <CardContent className="space-y-4">
        {isLoading ? (
          <div className="py-8 text-center text-xs text-muted-foreground">Loading system health...</div>
        ) : !health ? (
          <div className="py-8 text-center text-xs text-muted-foreground">System health data unavailable.</div>
        ) : (
          <>
            <div className="space-y-3">
              {metrics.map((metric) => (
                <div key={metric.label} className="space-y-1.5">
                  <div className="flex items-center justify-between">
                    <span className="text-xs text-muted-foreground">{metric.label}</span>
                    <span className="text-xs font-medium">{metric.value}{metric.suffix ?? '%'}</span>
                  </div>
                  <Progress value={metric.suffix === '' ? Math.min(metric.value, 100) : metric.value} className="h-1.5" />
                </div>
              ))}
            </div>

            <div className="border-t border-border/40 pt-3">
              <p className="text-xs font-medium text-muted-foreground mb-2">Services</p>
              <div className="space-y-2">
                {health.services.map((service) => {
                  const status = normalizeStatus(service.status);
                  const Icon = statusIcons[status];
                  return (
                    <div key={service.name} className="flex items-center justify-between rounded-md px-2 py-1.5 hover:bg-muted/30 transition-colors">
                      <div className="flex items-center gap-2">
                        <Icon className={cn('h-3.5 w-3.5', statusColors[status])} />
                        <span className="text-xs">{service.name}</span>
                      </div>
                      <span className="text-[10px] text-muted-foreground">{status}</span>
                    </div>
                  );
                })}
              </div>
            </div>
          </>
        )}
      </CardContent>
    </Card>
  );
}
