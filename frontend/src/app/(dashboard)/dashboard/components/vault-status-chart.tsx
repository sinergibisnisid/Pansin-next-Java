'use client';

import { useEffect, useState } from 'react';
import { PieChart, Pie, Cell, ResponsiveContainer, Tooltip, Legend } from 'recharts';
import { monitoringService } from '@/services';

const statusColors: Record<string, string> = {
  CLOSED: '#64748b',
  OPEN: '#10b981',
  LOCKED: '#3b82f6',
  ALARM: '#ef4444',
  MAINTENANCE: '#f59e0b',
  UNKNOWN: '#94a3b8',
};

interface ChartPoint {
  name: string;
  value: number;
  color: string;
}

export function VaultStatusChart() {
  const [data, setData] = useState<ChartPoint[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const fetchData = async () => {
      try {
        const response = await monitoringService.getVaultStatusSummary();
        setData(response.map((item) => ({
          name: item.status,
          value: item.count,
          color: statusColors[item.status] ?? statusColors.UNKNOWN,
        })));
      } catch (error) {
        console.error('Failed to fetch vault status summary:', error);
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
    return <div className="h-[250px] flex items-center justify-center text-xs text-muted-foreground">No vault status data.</div>;
  }

  return (
    <div className="h-[250px]">
      <ResponsiveContainer width="100%" height="100%">
        <PieChart>
          <Pie data={data} cx="50%" cy="50%" innerRadius={60} outerRadius={90} paddingAngle={4} dataKey="value" stroke="none">
            {data.map((entry, index) => (
              <Cell key={`cell-${index}`} fill={entry.color} />
            ))}
          </Pie>
          <Tooltip contentStyle={{ backgroundColor: 'rgba(15, 23, 42, 0.9)', border: '1px solid rgba(255,255,255,0.1)', borderRadius: '8px', fontSize: '12px', color: '#e2e8f0' }} />
          <Legend verticalAlign="bottom" height={36} formatter={(value) => <span className="text-xs text-muted-foreground">{value}</span>} />
        </PieChart>
      </ResponsiveContainer>
    </div>
  );
}
