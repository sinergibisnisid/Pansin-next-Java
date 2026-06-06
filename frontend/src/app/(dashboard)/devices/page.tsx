'use client';

import { useState, useEffect } from 'react';
import { Plus, Cpu, Wifi, WifiOff, Signal, Edit, Trash2 } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { DataTable } from '@/components/tables/data-table';
import { StatCard } from '@/components/cards/stat-card';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter } from '@/components/ui/dialog';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Badge } from '@/components/ui/badge';
import { type ColumnDef } from '@tanstack/react-table';
import { Progress } from '@/components/ui/progress';
import type { Device } from '@/types';
import { deviceService } from '@/services';

const statusColors: Record<string, string> = {
  ONLINE: 'bg-emerald-500/20 text-emerald-400 border-emerald-500/30',
  OFFLINE: 'bg-slate-500/20 text-slate-400 border-slate-500/30',
  DEGRADED: 'bg-yellow-500/20 text-yellow-400 border-yellow-500/30',
  MAINTENANCE: 'bg-blue-500/20 text-blue-400 border-blue-500/30',
  DECOMMISSIONED: 'bg-red-500/20 text-red-400 border-red-500/30',
};

const createColumns = (
  onEdit: (device: Device) => void,
  onDelete: (id: string) => void
): ColumnDef<Device>[] => [
  {
    accessorKey: 'name',
    header: 'Device',
    cell: ({ row }) => (
      <div className="flex items-center gap-3">
        <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-cyan-500/10 border border-cyan-500/20">
          <Cpu className="h-4 w-4 text-cyan-400" />
        </div>
        <div>
          <p className="text-sm font-medium">{row.original.name}</p>
          <p className="text-xs text-muted-foreground font-mono">{row.original.deviceCode}</p>
        </div>
      </div>
    ),
  },
  {
    accessorKey: 'type',
    header: 'Type',
    cell: ({ row }) => (
      <span className="text-xs capitalize px-2 py-1 rounded-md bg-muted/50">
        {row.original.type.replace(/_/g, ' ')}
      </span>
    ),
  },
  {
    accessorKey: 'branch',
    header: 'Branch',
    cell: ({ row }) => (
      <span className="text-sm">{row.original.branch?.name || '-'}</span>
    ),
  },
  {
    accessorKey: 'vault',
    header: 'Vault',
    cell: ({ row }) => (
      <span className="text-sm">{row.original.vault?.name || '-'}</span>
    ),
  },
  {
    accessorKey: 'ipAddress',
    header: 'IP Address',
    cell: ({ row }) => (
      <span className="text-xs font-mono text-muted-foreground">{row.original.ipAddress || '-'}</span>
    ),
  },
  {
    accessorKey: 'firmwareVersion',
    header: 'Firmware',
    cell: ({ row }) => (
      <span className="text-xs text-muted-foreground">{row.original.firmwareVersion || '-'}</span>
    ),
  },
  {
    accessorKey: 'signalQuality',
    header: 'Signal',
    cell: ({ row }) => (
      <div className="flex items-center gap-2 w-24">
        <Progress value={row.original.signalQuality ?? 0} className="h-1.5" />
        <span className="text-xs text-muted-foreground">{row.original.signalQuality ?? 0}%</span>
      </div>
    ),
  },
  {
    accessorKey: 'status',
    header: 'Status',
    cell: ({ row }) => (
      <Badge variant="outline" className={statusColors[row.original.status] || statusColors.OFFLINE}>
        {row.original.status}
      </Badge>
    ),
  },
  {
    accessorKey: 'lastHeartbeat',
    header: 'Last Heartbeat',
    cell: ({ row }) => (
      <span className="text-xs text-muted-foreground">
        {row.original.lastHeartbeat
          ? new Date(row.original.lastHeartbeat).toLocaleDateString('id-ID', {
              day: 'numeric',
              month: 'short',
              hour: '2-digit',
              minute: '2-digit',
            })
          : 'Never'}
      </span>
    ),
  },
  {
    id: 'actions',
    header: 'Actions',
    cell: ({ row }) => (
      <div className="flex gap-2">
        <Button size="sm" variant="ghost" onClick={() => onEdit(row.original)}>
          <Edit className="h-4 w-4" />
        </Button>
        <Button size="sm" variant="ghost" onClick={() => onDelete(row.original.id)}>
          <Trash2 className="h-4 w-4" />
        </Button>
      </div>
    ),
  },
];

export default function DevicesPage() {
  const [devices, setDevices] = useState<Device[]>([]);
  const [total, setTotal] = useState(0);
  const [isLoading, setIsLoading] = useState(true);
  const [isAddDialogOpen, setIsAddDialogOpen] = useState(false);
  const [formData, setFormData] = useState({
    branchId: '',
    vaultId: '',
    deviceCode: '',
    name: '',
    type: 'CONTROLLER',
    ipAddress: '',
    macAddress: '',
    firmwareVersion: '',
  });
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [isEditDialogOpen, setIsEditDialogOpen] = useState(false);
  const [editingDevice, setEditingDevice] = useState<Device | null>(null);
  const [editFormData, setEditFormData] = useState({
    branchId: '',
    vaultId: '',
    deviceCode: '',
    name: '',
    type: '',
    ipAddress: '',
    macAddress: '',
    firmwareVersion: '',
  });
  const [deleteDeviceId, setDeleteDeviceId] = useState<string | null>(null);

  const fetchDevices = async () => {
    setIsLoading(true);
    try {
      const response = await deviceService.getAll();
      setDevices(response?.items ?? []);
      setTotal(response?.total ?? 0);
    } catch (error) {
      console.error('Failed to fetch devices:', error);
      setDevices([]);
      setTotal(0);
    } finally {
      setIsLoading(false);
    }
  };

  const handleSubmit = async () => {
    if (!formData.name || !formData.deviceCode || !formData.branchId) return;
    setIsSubmitting(true);
    try {
      await deviceService.create(formData);
      fetchDevices();
      setIsAddDialogOpen(false);
      setFormData({ branchId: '', vaultId: '', deviceCode: '', name: '', type: 'CONTROLLER', ipAddress: '', macAddress: '', firmwareVersion: '' });
    } catch (error: any) {
      alert(error.response?.data?.message || 'Failed to create device');
      console.error('Failed to create device:', error);
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleEdit = (device: Device) => {
    setEditingDevice(device);
    setEditFormData({
      branchId: device.branch?.id || '',
      vaultId: device.vault?.id || '',
      deviceCode: device.deviceCode,
      name: device.name,
      type: device.type,
      ipAddress: device.ipAddress || '',
      macAddress: device.macAddress || '',
      firmwareVersion: device.firmwareVersion || '',
    });
    setIsEditDialogOpen(true);
  };

  const handleEditSubmit = async () => {
    if (!editingDevice) return;
    setIsSubmitting(true);
    try {
      await deviceService.update(editingDevice.id, editFormData);
      fetchDevices();
      setIsEditDialogOpen(false);
      setEditingDevice(null);
    } catch (error: any) {
      alert(error.response?.data?.message || 'Failed to update device');
      console.error('Failed to update device:', error);
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleDeleteConfirm = async () => {
    if (!deleteDeviceId) return;
    try {
      await deviceService.delete(deleteDeviceId);
      fetchDevices();
      setDeleteDeviceId(null);
    } catch (error: any) {
      alert(error.response?.data?.message || 'Failed to delete device');
      console.error('Failed to delete device:', error);
    }
  };

  useEffect(() => {
    fetchDevices();
  }, []);

  const onlineCount = devices.filter((d) => d.status === 'ONLINE').length;
  const offlineCount = devices.filter((d) => d.status === 'OFFLINE').length;
  const degradedCount = devices.filter((d) => d.status === 'DEGRADED').length;

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between gap-2">
        <div className="min-w-0">
          <h1 className="text-xl sm:text-2xl font-bold tracking-tight">Device Management</h1>
          <p className="text-xs sm:text-sm text-muted-foreground mt-1">
            Monitor and manage all connected devices
          </p>
        </div>
        <Button
          size="sm"
          className="gap-2 shrink-0 bg-gradient-to-r from-blue-600 to-cyan-500 hover:from-blue-500 hover:to-cyan-400"
          onClick={() => setIsAddDialogOpen(true)}
        >
          <Plus className="h-4 w-4" />
          <span className="hidden sm:inline">Add Device</span>
        </Button>
      </div>

      {/* Stats */}
      <div className="grid gap-3 grid-cols-2 lg:grid-cols-4">
        <StatCard title="Total Devices" value={total} icon={Cpu} variant="info" subtitle="Registered devices" />
        <StatCard title="Online" value={onlineCount} icon={Wifi} variant="success" pulse subtitle="Connected & active" />
        <StatCard title="Offline" value={offlineCount} icon={WifiOff} variant="danger" subtitle="Not responding" />
        <StatCard title="Degraded" value={degradedCount} icon={Signal} variant="warning" subtitle="Needs attention" />
      </div>

      {/* Table */}
      <DataTable
        columns={createColumns(handleEdit, (id) => setDeleteDeviceId(id))}
        data={devices}
        searchKey="name"
        searchPlaceholder="Search devices..."
        isLoading={isLoading}
        emptyTitle="No devices found"
        emptyDescription="No devices have been registered yet or no results match your search."
      />

      {/* Add Device Dialog */}
      <Dialog open={isAddDialogOpen} onOpenChange={setIsAddDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Add New Device</DialogTitle>
          </DialogHeader>
          <div className="space-y-4">
            <div className="space-y-2">
              <Label>Device Name *</Label>
              <Input placeholder="Enter device name" value={formData.name} onChange={(e) => setFormData({...formData, name: e.target.value})} />
            </div>
            <div className="space-y-2">
              <Label>Device Code *</Label>
              <Input placeholder="Enter device code" value={formData.deviceCode} onChange={(e) => setFormData({...formData, deviceCode: e.target.value})} />
            </div>
            <div className="space-y-2">
              <Label>Branch ID *</Label>
              <Input placeholder="Enter branch ID" value={formData.branchId} onChange={(e) => setFormData({...formData, branchId: e.target.value})} />
            </div>
            <div className="space-y-2">
              <Label>Vault ID</Label>
              <Input placeholder="Enter vault ID" value={formData.vaultId} onChange={(e) => setFormData({...formData, vaultId: e.target.value})} />
            </div>
            <div className="space-y-2">
              <Label>Type</Label>
              <Input placeholder="e.g. CONTROLLER, CAMERA" value={formData.type} onChange={(e) => setFormData({...formData, type: e.target.value})} />
            </div>
            <div className="space-y-2">
              <Label>IP Address</Label>
              <Input placeholder="e.g. 192.168.1.100" value={formData.ipAddress} onChange={(e) => setFormData({...formData, ipAddress: e.target.value})} />
            </div>
            <div className="space-y-2">
              <Label>MAC Address</Label>
              <Input placeholder="e.g. AA:BB:CC:DD:EE:FF" value={formData.macAddress} onChange={(e) => setFormData({...formData, macAddress: e.target.value})} />
            </div>
            <div className="space-y-2">
              <Label>Firmware Version</Label>
              <Input placeholder="e.g. 1.0.0" value={formData.firmwareVersion} onChange={(e) => setFormData({...formData, firmwareVersion: e.target.value})} />
            </div>
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setIsAddDialogOpen(false)}>Cancel</Button>
            <Button onClick={handleSubmit} disabled={isSubmitting}>
              {isSubmitting ? 'Adding...' : 'Add Device'}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* Edit Device Dialog */}
      <Dialog open={isEditDialogOpen} onOpenChange={setIsEditDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Edit Device</DialogTitle>
          </DialogHeader>
          <div className="space-y-4">
            <div className="space-y-2">
              <Label>Device Name</Label>
              <Input placeholder="Enter device name" value={editFormData.name} onChange={(e) => setEditFormData({...editFormData, name: e.target.value})} />
            </div>
            <div className="space-y-2">
              <Label>Device Code</Label>
              <Input placeholder="Enter device code" value={editFormData.deviceCode} onChange={(e) => setEditFormData({...editFormData, deviceCode: e.target.value})} />
            </div>
            <div className="space-y-2">
              <Label>Branch ID</Label>
              <Input placeholder="Enter branch ID" value={editFormData.branchId} onChange={(e) => setEditFormData({...editFormData, branchId: e.target.value})} />
            </div>
            <div className="space-y-2">
              <Label>Vault ID</Label>
              <Input placeholder="Enter vault ID" value={editFormData.vaultId} onChange={(e) => setEditFormData({...editFormData, vaultId: e.target.value})} />
            </div>
            <div className="space-y-2">
              <Label>IP Address</Label>
              <Input placeholder="e.g. 192.168.1.100" value={editFormData.ipAddress} onChange={(e) => setEditFormData({...editFormData, ipAddress: e.target.value})} />
            </div>
            <div className="space-y-2">
              <Label>MAC Address</Label>
              <Input placeholder="e.g. AA:BB:CC:DD:EE:FF" value={editFormData.macAddress} onChange={(e) => setEditFormData({...editFormData, macAddress: e.target.value})} />
            </div>
            <div className="space-y-2">
              <Label>Firmware Version</Label>
              <Input placeholder="e.g. 1.0.0" value={editFormData.firmwareVersion} onChange={(e) => setEditFormData({...editFormData, firmwareVersion: e.target.value})} />
            </div>
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setIsEditDialogOpen(false)}>Cancel</Button>
            <Button onClick={handleEditSubmit} disabled={isSubmitting}>
              {isSubmitting ? 'Saving...' : 'Save Changes'}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* Delete Confirmation Dialog */}
      <Dialog open={!!deleteDeviceId} onOpenChange={(open) => !open && setDeleteDeviceId(null)}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Delete Device</DialogTitle>
          </DialogHeader>
          <p className="text-sm text-muted-foreground">
            Are you sure you want to delete this device? This action cannot be undone.
          </p>
          <DialogFooter>
            <Button variant="outline" onClick={() => setDeleteDeviceId(null)}>Cancel</Button>
            <Button variant="destructive" onClick={handleDeleteConfirm}>
              Delete
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}
