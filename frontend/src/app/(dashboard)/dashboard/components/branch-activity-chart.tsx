'use client';

import { useEffect, useState } from 'react';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';
import { monitoringService } from '@/services';

interface ChartPoint {
  branch: string;
  access: number;
  alarm: number;
}

export function BranchActivityChart() {
  const [data, setData] = useState<ChartPoint[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const fetchData = async () => {
      try {
        const response = await monitoringService.getBranchActivity({ limit: 10 });
        setData(response.map((item) => ({
          branch: item.branchCode || item.branchName,
          access: item.accessCount,
          alarm: item.alarmCount,
        })));
      } catch (error) {
        console.error('Failed to fetch branch activity:', error);
        setData([]);
      } finally {
        setIsLoading(false);
      }
    };
    fetchData();
  }, []);

  if (isLoading) {
    return <div className="h-[250px] flex items-center justify-center text-xs text-muted-foreground">Loading chart...</div>;
  }

  if (data.length === 0) {
    return <div className="h-[250px] flex items-center justify-center text-xs text-muted-foreground">No branch activity data.</div>;
  }

  return (
    <div className="h-[250px]">
      <ResponsiveContainer width="100%" height="100%">
        <BarChart data={data} margin={{ top: 5, right: 10, left: -10, bottom: 5 }}>
          <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.05)" />
          <XAxis dataKey="branch" tick={{ fontSize: 11, fill: '#94a3b8' }} axisLine={{ stroke: 'rgba(255,255,255,0.1)' }} tickLine={false} />
          <YAxis tick={{ fontSize: 11, fill: '#94a3b8' }} axisLine={{ stroke: 'rgba(255,255,255,0.1)' }} tickLine={false} />
          <Tooltip contentStyle={{ backgroundColor: 'rgba(15, 23, 42, 0.9)', border: '1px solid rgba(255,255,255,0.1)', borderRadius: '8px', fontSize: '12px', color: '#e2e8f0' }} />
          <Bar dataKey="access" fill="#3b82f6" radius={[4, 4, 0, 0]} name="Access" />
          <Bar dataKey="alarm" fill="#ef4444" radius={[4, 4, 0, 0]} name="Alarm" />
        </BarChart>
      </ResponsiveContainer>
    </div>
  );
}
