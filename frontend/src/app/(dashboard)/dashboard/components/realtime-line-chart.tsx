'use client';

import { useEffect, useState } from 'react';
import { AreaChart, Area, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';
import { monitoringService } from '@/services';

interface ChartDataPoint {
  time: string;
  events: number;
  alarms: number;
}

export function RealtimeLineChart() {
  const [data, setData] = useState<ChartDataPoint[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  const fetchData = async () => {
    try {
      const response = await monitoringService.getRealtimeStats({ hours: 24 });
      setData(response.map((item) => ({ time: item.time, events: item.events, alarms: item.alarms })));
    } catch (error) {
      console.error('Failed to fetch realtime stats:', error);
      setData([]);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    void Promise.resolve().then(fetchData);
    const interval = window.setInterval(fetchData, 30000);
    return () => window.clearInterval(interval);
  }, []);

  if (isLoading) {
    return <div className="h-[250px] flex items-center justify-center text-xs text-muted-foreground">Loading chart...</div>;
  }

  if (data.length === 0) {
    return <div className="h-[250px] flex items-center justify-center text-xs text-muted-foreground">No realtime activity data.</div>;
  }

  return (
    <div className="h-[250px]">
      <ResponsiveContainer width="100%" height="100%">
        <AreaChart data={data} margin={{ top: 5, right: 10, left: -10, bottom: 5 }}>
          <defs>
            <linearGradient id="colorEvents" x1="0" y1="0" x2="0" y2="1">
              <stop offset="5%" stopColor="#3b82f6" stopOpacity={0.3} />
              <stop offset="95%" stopColor="#3b82f6" stopOpacity={0} />
            </linearGradient>
            <linearGradient id="colorAlarms" x1="0" y1="0" x2="0" y2="1">
              <stop offset="5%" stopColor="#ef4444" stopOpacity={0.3} />
              <stop offset="95%" stopColor="#ef4444" stopOpacity={0} />
            </linearGradient>
          </defs>
          <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.05)" />
          <XAxis dataKey="time" tick={{ fontSize: 10, fill: '#94a3b8' }} axisLine={{ stroke: 'rgba(255,255,255,0.1)' }} tickLine={false} interval="preserveStartEnd" />
          <YAxis tick={{ fontSize: 11, fill: '#94a3b8' }} axisLine={{ stroke: 'rgba(255,255,255,0.1)' }} tickLine={false} />
          <Tooltip contentStyle={{ backgroundColor: 'rgba(15, 23, 42, 0.9)', border: '1px solid rgba(255,255,255,0.1)', borderRadius: '8px', fontSize: '12px', color: '#e2e8f0' }} />
          <Area type="monotone" dataKey="events" stroke="#3b82f6" strokeWidth={2} fillOpacity={1} fill="url(#colorEvents)" name="Events" />
          <Area type="monotone" dataKey="alarms" stroke="#ef4444" strokeWidth={2} fillOpacity={1} fill="url(#colorAlarms)" name="Alarms" />
        </AreaChart>
      </ResponsiveContainer>
    </div>
  );
}
