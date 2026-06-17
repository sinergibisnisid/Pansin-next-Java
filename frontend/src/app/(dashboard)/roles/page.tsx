'use client';

import { useState, useEffect, useCallback, useMemo } from 'react';
import { Shield, Plus, Edit, Trash2, RefreshCw } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { DataTable } from '@/components/tables/data-table';
import { Badge } from '@/components/ui/badge';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter } from '@/components/ui/dialog';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import { Checkbox } from '@/components/ui/checkbox';
import { Tooltip, TooltipContent, TooltipProvider, TooltipTrigger } from '@/components/ui/tooltip';
import { type ColumnDef } from '@tanstack/react-table';
import type { Role, PermissionRecord } from '@/types';
import { roleService, permissionService } from '@/services';
import { toast } from 'sonner';
import { cn } from '@/lib/utils';

function getErrorMessage(error: unknown, fallback: string) {
  if (typeof error === 'object' && error !== null && 'response' in error) {
    const response = (error as { response?: { data?: { message?: string } } }).response;
    return response?.data?.message ?? fallback;
  }
  if (error instanceof Error) return error.message;
  return fallback;
}

export default function RolesPage() {
  const [roles, setRoles] = useState<Role[]>([]);
  const [permissions, setPermissions] = useState<PermissionRecord[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isAddDialogOpen, setIsAddDialogOpen] = useState(false);
  const [isEditDialogOpen, setIsEditDialogOpen] = useState(false);
  const [editingRole, setEditingRole] = useState<Role | null>(null);
  const [deleteRoleId, setDeleteRoleId] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);
  
  const [formData, setFormData] = useState({
    code: '',
    name: '',
    description: '',
    permissionCodes: [] as string[],
  });

  const [editFormData, setEditFormData] = useState({
    code: '',
    name: '',
    description: '',
    permissionCodes: [] as string[],
  });

  const fetchRoles = useCallback(async () => {
    setIsLoading(true);
    try {
      const rolesData = await roleService.getAll();
      setRoles(rolesData);
    } catch (error) {
      console.error('Failed to fetch roles:', error);
      setRoles([]);
    } finally {
      setIsLoading(false);
    }
  }, []);

  const fetchPermissions = useCallback(async () => {
    try {
      const permsData = await permissionService.getAll();
      setPermissions(permsData);
    } catch (error) {
      console.error('Failed to fetch permissions:', error);
    }
  }, []);

  const handleSubmit = async () => {
    if (!formData.code || !formData.name) return;
    setIsSubmitting(true);
    try {
      await roleService.create(formData);
      fetchRoles();
      toast.success('Role berhasil dibuat');
      setIsAddDialogOpen(false);
      setFormData({ code: '', name: '', description: '', permissionCodes: [] });
    } catch (error: unknown) {
      toast.error(getErrorMessage(error, 'Failed to create role'));
      console.error('Failed to create role:', error);
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleEdit = (role: Role) => {
    setEditingRole(role);
    setEditFormData({
      code: role.code,
      name: role.name,
      description: role.description || '',
      permissionCodes: role.permissions,
    });
    setIsEditDialogOpen(true);
  };

  const handleEditSubmit = async () => {
    if (!editingRole) return;
    setIsSubmitting(true);
    try {
      await roleService.update(editingRole.id, editFormData);
      fetchRoles();
      toast.success('Role berhasil diperbarui');
      setIsEditDialogOpen(false);
      setEditingRole(null);
    } catch (error: unknown) {
      toast.error(getErrorMessage(error, 'Failed to update role'));
      console.error('Failed to update role:', error);
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleDeleteConfirm = async () => {
    if (!deleteRoleId) return;
    try {
      await roleService.delete(deleteRoleId);
      fetchRoles();
      toast.success('Role berhasil dihapus');
      setDeleteRoleId(null);
    } catch (error: unknown) {
      toast.error(getErrorMessage(error, 'Failed to delete role'));
      console.error('Failed to delete role:', error);
    }
  };

  const columns = useMemo<ColumnDef<Role>[]>(() => [
    {
      accessorKey: 'code',
      header: 'Code',
      cell: ({ row }) => (
        <div className="flex items-center gap-2">
          <Shield className="h-4 w-4 text-muted-foreground" />
          <span className="font-mono text-xs font-medium">{row.original.code}</span>
        </div>
      ),
    },
    {
      accessorKey: 'name',
      header: 'Name',
      cell: ({ row }) => <span className="font-medium">{row.original.name}</span>,
    },
    {
      accessorKey: 'description',
      header: 'Description',
      cell: ({ row }) => (
        <span className="text-xs text-muted-foreground max-w-[320px] block truncate">
          {row.original.description || '-'}
        </span>
      ),
    },
    {
      accessorKey: 'system',
      header: 'Type',
      cell: ({ row }) => (
        <Badge variant={row.original.system ? 'default' : 'outline'} className="text-[10px]">
          {row.original.system ? 'System' : 'Custom'}
        </Badge>
      ),
    },
    {
      accessorKey: 'permissions',
      header: 'Permissions',
      cell: ({ row }) => (
        <span className="text-xs text-muted-foreground">
          {row.original.permissions.length} permission(s)
        </span>
      ),
    },
    {
      id: 'actions',
      header: 'Actions',
      cell: ({ row }) => {
        const isSystemRole = row.original.system;
        const disabledMessage = 'Peran sistem tidak dapat diubah atau dihapus';

        return (
          <TooltipProvider>
            <div className="flex gap-2">
              <Tooltip>
                <TooltipTrigger>
                  <span className="inline-flex">
                    <Button
                      size="sm"
                      variant="ghost"
                      onClick={() => handleEdit(row.original)}
                      disabled={isSystemRole}
                      className="disabled:cursor-not-allowed disabled:opacity-40"
                      aria-label={isSystemRole ? disabledMessage : 'Edit role'}
                    >
                      <Edit className="h-4 w-4" />
                    </Button>
                  </span>
                </TooltipTrigger>
                <TooltipContent>
                  {isSystemRole ? disabledMessage : 'Edit role'}
                </TooltipContent>
              </Tooltip>

              <Tooltip>
                <TooltipTrigger>
                  <span className="inline-flex">
                    <Button
                      size="sm"
                      variant="ghost"
                      onClick={() => setDeleteRoleId(row.original.id)}
                      disabled={isSystemRole}
                      className="disabled:cursor-not-allowed disabled:opacity-40"
                      aria-label={isSystemRole ? disabledMessage : 'Delete role'}
                    >
                      <Trash2 className="h-4 w-4" />
                    </Button>
                  </span>
                </TooltipTrigger>
                <TooltipContent>
                  {isSystemRole ? disabledMessage : 'Delete role'}
                </TooltipContent>
              </Tooltip>
            </div>
          </TooltipProvider>
        );
      },
    },
  ], []);

  useEffect(() => {
    void Promise.resolve().then(fetchRoles);
    void Promise.resolve().then(fetchPermissions);
  }, [fetchRoles, fetchPermissions]);

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between gap-2">
        <div className="min-w-0">
          <h1 className="text-xl sm:text-2xl font-bold tracking-tight">Role Management</h1>
          <p className="text-xs sm:text-sm text-muted-foreground mt-1">
            Manage roles and their permissions
          </p>
        </div>
        <div className="flex gap-2">
          <Button
            size="sm"
            variant="outline"
            onClick={fetchRoles}
            disabled={isLoading}
          >
            <RefreshCw className={cn('h-4 w-4', isLoading && 'animate-spin')} />
          </Button>
          <Button
            size="sm"
            className="gap-2 shrink-0 bg-gradient-to-r from-blue-600 to-cyan-500 hover:from-blue-500 hover:to-cyan-400"
            onClick={() => setIsAddDialogOpen(true)}
          >
            <Plus className="h-4 w-4" />
            <span className="hidden sm:inline">Add Role</span>
          </Button>
        </div>
      </div>

      {/* Stats */}
      <div className="grid gap-2 sm:gap-4 grid-cols-2 sm:grid-cols-3">
        <div className="rounded-xl border border-border/40 bg-card/50 p-3 sm:p-4">
          <p className="text-[10px] sm:text-xs text-muted-foreground">Total Roles</p>
          <p className="text-xl sm:text-2xl font-bold mt-1">{roles.length}</p>
        </div>
        <div className="rounded-xl border border-border/40 bg-card/50 p-3 sm:p-4">
          <p className="text-[10px] sm:text-xs text-muted-foreground">System Roles</p>
          <p className="text-xl sm:text-2xl font-bold mt-1 text-blue-400">
            {roles.filter((r) => r.system).length}
          </p>
        </div>
        <div className="rounded-xl border border-border/40 bg-card/50 p-3 sm:p-4">
          <p className="text-[10px] sm:text-xs text-muted-foreground">Custom Roles</p>
          <p className="text-xl sm:text-2xl font-bold mt-1 text-emerald-400">
            {roles.filter((r) => !r.system).length}
          </p>
        </div>
      </div>

      {/* Table */}
      <DataTable
        columns={columns}
        data={roles}
        searchKey="name"
        searchPlaceholder="Search roles..."
        isLoading={isLoading}
        emptyTitle="No roles found"
        emptyDescription="No roles have been created yet or no results match your search."
      />

      {/* Add Role Dialog */}
      <Dialog open={isAddDialogOpen} onOpenChange={setIsAddDialogOpen}>
        <DialogContent className="max-w-2xl">
          <DialogHeader>
            <DialogTitle>Add New Role</DialogTitle>
          </DialogHeader>
          <div className="space-y-4 max-h-[60vh] overflow-y-auto">
            <div className="space-y-2">
              <Label>Code</Label>
              <Input
                placeholder="e.g., CUSTOM_ROLE"
                value={formData.code}
                onChange={(e) => setFormData({ ...formData, code: e.target.value.toUpperCase() })}
              />
            </div>
            <div className="space-y-2">
              <Label>Name</Label>
              <Input
                placeholder="e.g., Custom Role"
                value={formData.name}
                onChange={(e) => setFormData({ ...formData, name: e.target.value })}
              />
            </div>
            <div className="space-y-2">
              <Label>Description (Optional)</Label>
              <Textarea
                placeholder="Enter role description"
                value={formData.description}
                onChange={(e) => setFormData({ ...formData, description: e.target.value })}
              />
            </div>
            <div className="space-y-2">
              <Label>Permissions</Label>
              <div className="border rounded-lg p-4 max-h-[240px] overflow-y-auto space-y-2">
                {permissions.map((perm) => (
                  <div key={perm.id} className="flex items-start space-x-2">
                    <Checkbox
                      id={`perm-${perm.id}`}
                      checked={formData.permissionCodes.includes(perm.code)}
                      onCheckedChange={(checked) => {
                        if (checked) {
                          setFormData({
                            ...formData,
                            permissionCodes: [...formData.permissionCodes, perm.code],
                          });
                        } else {
                          setFormData({
                            ...formData,
                            permissionCodes: formData.permissionCodes.filter((code) => code !== perm.code),
                          });
                        }
                      }}
                    />
                    <label
                      htmlFor={`perm-${perm.id}`}
                      className="text-sm font-medium leading-none peer-disabled:cursor-not-allowed peer-disabled:opacity-70 cursor-pointer"
                    >
                      <div className="font-mono text-xs">{perm.code}</div>
                      <div className="text-xs text-muted-foreground">{perm.description || perm.name}</div>
                    </label>
                  </div>
                ))}
              </div>
            </div>
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setIsAddDialogOpen(false)}>
              Cancel
            </Button>
            <Button onClick={handleSubmit} disabled={isSubmitting}>
              {isSubmitting ? 'Adding...' : 'Add Role'}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* Edit Role Dialog */}
      <Dialog open={isEditDialogOpen} onOpenChange={setIsEditDialogOpen}>
        <DialogContent className="max-w-2xl">
          <DialogHeader>
            <DialogTitle>Edit Role</DialogTitle>
          </DialogHeader>
          <div className="space-y-4 max-h-[60vh] overflow-y-auto">
            <div className="space-y-2">
              <Label>Code</Label>
              <Input
                placeholder="Enter role code"
                value={editFormData.code}
                disabled
              />
            </div>
            <div className="space-y-2">
              <Label>Name</Label>
              <Input
                placeholder="Enter role name"
                value={editFormData.name}
                onChange={(e) => setEditFormData({ ...editFormData, name: e.target.value })}
              />
            </div>
            <div className="space-y-2">
              <Label>Description (Optional)</Label>
              <Textarea
                placeholder="Enter role description"
                value={editFormData.description}
                onChange={(e) => setEditFormData({ ...editFormData, description: e.target.value })}
              />
            </div>
            <div className="space-y-2">
              <Label>Permissions</Label>
              <div className="border rounded-lg p-4 max-h-[240px] overflow-y-auto space-y-2">
                {permissions.map((perm) => (
                  <div key={perm.id} className="flex items-start space-x-2">
                    <Checkbox
                      id={`edit-perm-${perm.id}`}
                      checked={editFormData.permissionCodes.includes(perm.code)}
                      onCheckedChange={(checked) => {
                        if (checked) {
                          setEditFormData({
                            ...editFormData,
                            permissionCodes: [...editFormData.permissionCodes, perm.code],
                          });
                        } else {
                          setEditFormData({
                            ...editFormData,
                            permissionCodes: editFormData.permissionCodes.filter((code) => code !== perm.code),
                          });
                        }
                      }}
                    />
                    <label
                      htmlFor={`edit-perm-${perm.id}`}
                      className="text-sm font-medium leading-none peer-disabled:cursor-not-allowed peer-disabled:opacity-70 cursor-pointer"
                    >
                      <div className="font-mono text-xs">{perm.code}</div>
                      <div className="text-xs text-muted-foreground">{perm.description || perm.name}</div>
                    </label>
                  </div>
                ))}
              </div>
            </div>
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setIsEditDialogOpen(false)}>
              Cancel
            </Button>
            <Button onClick={handleEditSubmit} disabled={isSubmitting}>
              {isSubmitting ? 'Saving...' : 'Save Changes'}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* Delete Confirmation Dialog */}
      <Dialog open={!!deleteRoleId} onOpenChange={(open) => !open && setDeleteRoleId(null)}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Delete Role</DialogTitle>
          </DialogHeader>
          <p className="text-sm text-muted-foreground">
            Are you sure you want to delete this role? This action cannot be undone.
          </p>
          <DialogFooter>
            <Button variant="outline" onClick={() => setDeleteRoleId(null)}>
              Cancel
            </Button>
            <Button variant="destructive" onClick={handleDeleteConfirm}>
              Delete
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}
