'use client';

import { useEffect, useMemo, useState } from 'react';
import { AlertTriangle, CheckCircle2, RefreshCw } from 'lucide-react';
import { type ColumnDef } from '@tanstack/react-table';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { DataTable } from '@/components/tables/data-table';
import { alarmService } from '@/services';
import { cn } from '@/lib/utils';
import type { AlarmLog } from '@/types';

const severityColors: Record<string, string> = {
  LOW: 'bg-slate-500/20 text-slate-400 border-slate-500/30',
  MEDIUM: 'bg-blue-500/20 text-blue-400 border-blue-500/30',
  HIGH: 'bg-amber-500/20 text-amber-400 border-amber-500/30',
  CRITICAL: 'bg-red-500/20 text-red-400 border-red-500/30',
};

export default function AlarmsPage() {
  const [alarms, setAlarms] = useState<AlarmLog[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isAcknowledging, setIsAcknowledging] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  const fetchAlarms = async () => {
    setIsLoading(true);
    setError(null);
    try {
      const response = await alarmService.getAll({ page: 0, size: 100 });
      setAlarms(response.items);
    } catch (err) {
      console.error('Failed to fetch alarms:', err);
      setError('Gagal memuat data alarm dari backend.');
      setAlarms([]);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    void Promise.resolve().then(fetchAlarms);
  }, []);

  const handleAcknowledge = async (id: string) => {
    setIsAcknowledging(id);
    try {
      const updated = await alarmService.acknowledge(id);
      setAlarms((prev) => prev.map((alarm) => (alarm.id === id ? updated : alarm)));
    } catch (err) {
      console.error('Failed to acknowledge alarm:', err);
      setError('Gagal acknowledge alarm.');
    } finally {
      setIsAcknowledging(null);
    }
  };

  const columns = useMemo<ColumnDef<AlarmLog>[]>(() => [
    {
      accessorKey: 'createdAt',
      header: 'Time',
      cell: ({ row }) => (
        <span className="text-xs font-mono text-muted-foreground">
          {new Date(row.original.createdAt).toLocaleString('id-ID')}
        </span>
      ),
    },
    {
      accessorKey: 'severity',
      header: 'Severity',
      cell: ({ row }) => {
        const severity = row.original.severity?.toUpperCase() || 'HIGH';
        return (
          <Badge variant="outline" className={cn('text-[10px]', severityColors[severity] ?? severityColors.HIGH)}>
            {severity}
          </Badge>
        );
      },
    },
    {
      accessorKey: 'type',
      header: 'Type',
      cell: ({ row }) => <span className="text-sm font-medium">{row.original.type}</span>,
    },
    {
      accessorKey: 'message',
      header: 'Message',
      cell: ({ row }) => <span className="text-xs text-muted-foreground max-w-[260px] block truncate">{row.original.message}</span>,
    },
    {
      id: 'vault',
      header: 'Vault / Branch',
      cell: ({ row }) => (
        <div className="text-xs">
          <p className="font-medium">{row.original.vault?.name ?? '-'}</p>
          <p className="text-muted-foreground">{row.original.vault?.branch?.name ?? row.original.vault?.branch?.code ?? '-'}</p>
        </div>
      ),
    },
    {
      accessorKey: 'acknowledged',
      header: 'Status',
      cell: ({ row }) => (
        row.original.acknowledged ? (
          <Badge variant="outline" className="bg-emerald-500/20 text-emerald-400 border-emerald-500/30 text-[10px]">ACK</Badge>
        ) : (
          <Badge variant="outline" className="bg-red-500/20 text-red-400 border-red-500/30 text-[10px]">OPEN</Badge>
        )
      ),
    },
    {
      id: 'actions',
      header: 'Action',
      cell: ({ row }) => (
        <Button
          size="sm"
          variant="outline"
          className="h-8 gap-1.5 text-xs"
          disabled={row.original.acknowledged || isAcknowledging === row.original.id}
          onClick={() => handleAcknowledge(row.original.id)}
        >
          <CheckCircle2 className="h-3.5 w-3.5" />
          {isAcknowledging === row.original.id ? 'Processing...' : 'Acknowledge'}
        </Button>
      ),
    },
  ], [isAcknowledging]);

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3">
        <div>
          <h1 className="text-2xl font-bold tracking-tight">Alarm Management</h1>
          <p className="text-sm text-muted-foreground mt-1">Monitor and acknowledge real alarm events from backend</p>
        </div>
        <Button variant="outline" size="sm" className="gap-1.5" onClick={fetchAlarms} disabled={isLoading}>
          <RefreshCw className={cn('h-3.5 w-3.5', isLoading && 'animate-spin')} />
          Refresh
        </Button>
      </div>

      {error && (
        <div className="rounded-lg border border-red-500/20 bg-red-500/10 px-4 py-3 text-sm text-red-400 flex items-center gap-2">
          <AlertTriangle className="h-4 w-4" />
          {error}
        </div>
      )}

      <DataTable
        columns={columns}
        data={alarms}
        searchKey="message"
        searchPlaceholder="Search alarms..."
        isLoading={isLoading}
        emptyTitle="No alarms found"
        emptyDescription="No alarm events are currently available."
      />
    </div>
  );
}
