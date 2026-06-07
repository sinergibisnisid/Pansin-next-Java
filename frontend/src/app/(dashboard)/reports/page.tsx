'use client';

import { useState, useEffect, useCallback } from 'react';
import { FileText, Download, Calendar, Filter } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { DataTable } from '@/components/tables/data-table';
import { Badge } from '@/components/ui/badge';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import { type ColumnDef } from '@tanstack/react-table';
import { cn } from '@/lib/utils';
import type { AuditLog } from '@/types';
import { reportService } from '@/services';

const severityColors: Record<string, string> = {
  info: 'bg-blue-500/20 text-blue-400 border-blue-500/30',
  warning: 'bg-amber-500/20 text-amber-400 border-amber-500/30',
  critical: 'bg-red-500/20 text-red-400 border-red-500/30',
};

const categoryColors: Record<string, string> = {
  auth: 'bg-purple-500/20 text-purple-400',
  vault_access: 'bg-emerald-500/20 text-emerald-400',
  alarm: 'bg-red-500/20 text-red-400',
  device: 'bg-cyan-500/20 text-cyan-400',
  system: 'bg-slate-500/20 text-slate-400',
  maintenance: 'bg-amber-500/20 text-amber-400',
  configuration: 'bg-blue-500/20 text-blue-400',
};

const columns: ColumnDef<AuditLog>[] = [
  {
    accessorKey: 'timestamp',
    header: 'Time',
    cell: ({ row }) => (
      <span className="text-xs font-mono text-muted-foreground">
        {new Date(row.original.timestamp).toLocaleString('id-ID', {
          day: '2-digit',
          month: 'short',
          hour: '2-digit',
          minute: '2-digit',
          second: '2-digit',
        })}
      </span>
    ),
  },
  {
    accessorKey: 'severity',
    header: 'Level',
    cell: ({ row }) => (
      <Badge variant="outline" className={cn('text-[10px]', severityColors[row.original.severity])}>
        {row.original.severity}
      </Badge>
    ),
  },
  {
    accessorKey: 'action',
    header: 'Action',
    cell: ({ row }) => <span className="text-sm font-medium">{row.original.action}</span>,
  },
  {
    accessorKey: 'category',
    header: 'Category',
    cell: ({ row }) => (
      <span className={cn('text-[10px] px-2 py-0.5 rounded-full font-medium', categoryColors[row.original.category])}>
        {row.original.category.replace('_', ' ')}
      </span>
    ),
  },
  {
    accessorKey: 'userName',
    header: 'User',
    cell: ({ row }) => <span className="text-sm">{row.original.userName}</span>,
  },
  {
    accessorKey: 'branchName',
    header: 'Branch',
    cell: ({ row }) => <span className="text-xs text-muted-foreground">{row.original.branchName}</span>,
  },
  {
    accessorKey: 'details',
    header: 'Details',
    cell: ({ row }) => (
      <span className="text-xs text-muted-foreground max-w-[200px] truncate block">
        {row.original.details}
      </span>
    ),
  },
];

function getErrorMessage(error: unknown, fallback: string) {
  if (typeof error === 'object' && error !== null && 'response' in error) {
    const response = (error as { response?: { data?: { message?: string } } }).response;
    return response?.data?.message ?? fallback;
  }

  if (error instanceof Error) return error.message;

  return fallback;
}

export default function ReportsPage() {
  const [categoryFilter, setCategoryFilter] = useState('all');
  const [severityFilter, setSeverityFilter] = useState('all');
  const [isExporting, setIsExporting] = useState(false);
  const [auditLogs, setAuditLogs] = useState<AuditLog[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [total, setTotal] = useState(0);

  const fetchLogs = useCallback(async () => {
    setIsLoading(true);
    try {
      const response = await reportService.getLogs({
        category: categoryFilter !== 'all' ? categoryFilter : undefined,
        severity: severityFilter !== 'all' ? severityFilter : undefined,
      });
      setAuditLogs(response.items);
      setTotal(response.total);
    } catch (error) {
      console.error('Failed to fetch audit logs:', error);
      setAuditLogs([]);
    } finally {
      setIsLoading(false);
    }
  }, [categoryFilter, severityFilter]);

  useEffect(() => {
    void Promise.resolve().then(fetchLogs);
  }, [fetchLogs]);

  const downloadFile = (blob: Blob, filename: string) => {
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = filename;
    document.body.appendChild(a);
    a.click();
    window.URL.revokeObjectURL(url);
    document.body.removeChild(a);
  };

  const getFilename = (type: string) => {
    const date = new Date().toISOString().replace(/[-:]/g, '').replace(/\..+/, '').replace('T', '-');
    return `audit-log-${date}.${type}`;
  };

  const handleExport = async (type: 'csv' | 'excel' | 'pdf') => {
    if (isExporting) return;
    setIsExporting(true);
    try {
      const params = { category: categoryFilter, severity: severityFilter };
      let blob;
      if (type === 'csv') blob = await reportService.exportCSV(params);
      else if (type === 'excel') blob = await reportService.exportExcel(params);
      else blob = await reportService.exportPDF(params);
      downloadFile(blob, getFilename(type === 'excel' ? 'xlsx' : type));
    } catch (error: unknown) {
      console.error('Export failed:', error);
      alert(`Export failed: ${getErrorMessage(error, 'Unknown error')}`);
    } finally {
      setIsExporting(false);
    }
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3">
        <div className="min-w-0">
          <h1 className="text-xl sm:text-2xl font-bold tracking-tight">Reports & Audit Log</h1>
          <p className="text-xs sm:text-sm text-muted-foreground mt-1">
            System audit trail and activity reports
          </p>
        </div>
        <div className="flex items-center gap-2 flex-wrap">
          <Button variant="outline" size="sm" className="gap-1.5 text-xs" onClick={() => handleExport('pdf')} disabled={isExporting}>
            <Download className="h-3.5 w-3.5" />
            {isExporting ? 'Exporting...' : 'PDF'}
          </Button>
          <Button variant="outline" size="sm" className="gap-1.5 text-xs" onClick={() => handleExport('excel')} disabled={isExporting}>
            <Download className="h-3.5 w-3.5" />
            {isExporting ? 'Exporting...' : 'Excel'}
          </Button>
          <Button variant="outline" size="sm" className="gap-1.5 text-xs" onClick={() => handleExport('csv')} disabled={isExporting}>
            <Download className="h-3.5 w-3.5" />
            {isExporting ? 'Exporting...' : 'CSV'}
          </Button>
        </div>
      </div>

      {/* Filters */}
      <div className="flex flex-wrap items-center gap-2 sm:gap-3">
        <Select value={categoryFilter} onValueChange={(v) => setCategoryFilter(v ?? 'all')}>
          <SelectTrigger className="w-[140px] sm:w-[160px] bg-background/50 border-border/40 text-xs sm:text-sm">
            <Filter className="h-3.5 w-3.5 mr-1.5 text-muted-foreground" />
            <SelectValue placeholder="Category" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="all">All Categories</SelectItem>
            <SelectItem value="auth">Authentication</SelectItem>
            <SelectItem value="vault_access">Vault Access</SelectItem>
            <SelectItem value="alarm">Alarm</SelectItem>
            <SelectItem value="device">Device</SelectItem>
            <SelectItem value="system">System</SelectItem>
            <SelectItem value="maintenance">Maintenance</SelectItem>
            <SelectItem value="configuration">Configuration</SelectItem>
          </SelectContent>
        </Select>

        <Select value={severityFilter} onValueChange={(v) => setSeverityFilter(v ?? 'all')}>
          <SelectTrigger className="w-[120px] sm:w-[140px] bg-background/50 border-border/40 text-xs sm:text-sm">
            <SelectValue placeholder="Severity" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="all">All Levels</SelectItem>
            <SelectItem value="info">Info</SelectItem>
            <SelectItem value="warning">Warning</SelectItem>
            <SelectItem value="critical">Critical</SelectItem>
          </SelectContent>
        </Select>

        <Button variant="outline" size="sm" className="gap-1.5 border-border/40 text-xs">
          <Calendar className="h-3.5 w-3.5" />
          <span className="hidden sm:inline">Date Range</span>
        </Button>
      </div>

      {/* Table */}
      <DataTable
        columns={columns}
        data={auditLogs}
        searchKey="action"
        searchPlaceholder="Search audit logs..."
        isLoading={isLoading}
        emptyTitle="No audit logs found"
        emptyDescription="No audit logs match the selected filters."
      />
    </div>
  );
}
