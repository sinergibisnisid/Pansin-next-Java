'use client';

import { useEffect, useMemo, useState } from 'react';
import { KeyRound, RefreshCw } from 'lucide-react';
import { type ColumnDef } from '@tanstack/react-table';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { DataTable } from '@/components/tables/data-table';
import { permissionService } from '@/services';
import { cn } from '@/lib/utils';
import type { PermissionRecord } from '@/types';

export default function PermissionsPage() {
  const [permissions, setPermissions] = useState<PermissionRecord[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchPermissions = async () => {
    setIsLoading(true);
    setError(null);
    try {
      setPermissions(await permissionService.getAll());
    } catch (err) {
      console.error('Failed to fetch permissions:', err);
      setPermissions([]);
      setError('Gagal memuat permission dari backend.');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    void Promise.resolve().then(fetchPermissions);
  }, []);

  const columns = useMemo<ColumnDef<PermissionRecord>[]>(() => [
    {
      accessorKey: 'code',
      header: 'Code',
      cell: ({ row }) => <span className="text-xs font-mono font-medium">{row.original.code}</span>,
    },
    {
      accessorKey: 'name',
      header: 'Name',
      cell: ({ row }) => <span className="text-sm">{row.original.name ?? '-'}</span>,
    },
    {
      accessorKey: 'module',
      header: 'Module',
      cell: ({ row }) => <Badge variant="outline" className="text-[10px]">{row.original.module ?? 'GENERAL'}</Badge>,
    },
    {
      accessorKey: 'description',
      header: 'Description',
      cell: ({ row }) => <span className="text-xs text-muted-foreground max-w-[320px] block truncate">{row.original.description ?? '-'}</span>,
    },
  ], []);

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
        <div>
          <h1 className="text-2xl font-bold tracking-tight">Permissions</h1>
          <p className="text-sm text-muted-foreground mt-1">RBAC permission list from backend</p>
        </div>
        <Button variant="outline" size="sm" onClick={fetchPermissions} disabled={isLoading} className="gap-1.5">
          <RefreshCw className={cn('h-3.5 w-3.5', isLoading && 'animate-spin')} />
          Refresh
        </Button>
      </div>

      {error && <div className="rounded-lg border border-red-500/20 bg-red-500/10 px-4 py-3 text-sm text-red-400">{error}</div>}

      <DataTable
        columns={columns}
        data={permissions}
        searchKey="code"
        searchPlaceholder="Search permission code..."
        isLoading={isLoading}
        emptyTitle="No permissions found"
        emptyDescription="No permission records are available from the backend."
      />
    </div>
  );
}
