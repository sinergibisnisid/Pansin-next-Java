'use client';

import { useState, useEffect, useCallback } from 'react';
import { Plus, UserPlus, Shield, Mail, Edit, Trash2, MoreHorizontal } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { DataTable } from '@/components/tables/data-table';
import { StatusBadge } from '@/components/shared/status-badge';
import { Avatar, AvatarFallback } from '@/components/ui/avatar';
import { Badge } from '@/components/ui/badge';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter } from '@/components/ui/dialog';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { type ColumnDef } from '@tanstack/react-table';
import { getInitials } from '@/lib/utils';
import type { User } from '@/types';
import { userService } from '@/services';

const roleColors: Record<string, string> = {
  SUPER_ADMIN: 'bg-purple-500/20 text-purple-400 border-purple-500/30',
  ADMIN_PUSAT: 'bg-blue-500/20 text-blue-400 border-blue-500/30',
  ADMIN_CABANG: 'bg-cyan-500/20 text-cyan-400 border-cyan-500/30',
  OPERATOR: 'bg-emerald-500/20 text-emerald-400 border-emerald-500/30',
  SECURITY: 'bg-yellow-500/20 text-yellow-400 border-yellow-500/30',
  MAINTENANCE: 'bg-orange-500/20 text-orange-400 border-orange-500/30',
  VIEWER: 'bg-slate-500/20 text-slate-400 border-slate-500/30',
  VIEWER_CCTV: 'bg-indigo-500/20 text-indigo-400 border-indigo-500/30',
};

const roleLabels: Record<string, string> = {
  SUPER_ADMIN: 'Super Admin',
  ADMIN_PUSAT: 'Admin Pusat',
  ADMIN_CABANG: 'Admin Cabang',
  OPERATOR: 'Operator',
  SECURITY: 'Security',
  MAINTENANCE: 'Maintenance',
  VIEWER: 'Viewer',
  VIEWER_CCTV: 'Viewer CCTV',
};

const getUserStatus = (user: User): 'active' | 'inactive' | 'locked' => {
  if (user.locked) return 'locked';
  if (!user.enabled) return 'inactive';
  return 'active';
};

const columns: ColumnDef<User>[] = [
  {
    accessorKey: 'fullName',
    header: 'User',
    cell: ({ row }) => (
      <div className="flex items-center gap-3">
        <Avatar className="h-9 w-9 border border-border/40">
          <AvatarFallback className="bg-gradient-to-br from-blue-600/80 to-cyan-500/80 text-[10px] font-bold text-white">
            {getInitials(row.original.fullName)}
          </AvatarFallback>
        </Avatar>
        <div>
          <p className="text-sm font-medium">{row.original.fullName}</p>
          <p className="text-xs text-muted-foreground flex items-center gap-1">
            <Mail className="h-3 w-3" />
            {row.original.email}
          </p>
        </div>
      </div>
    ),
  },
  {
    accessorKey: 'roles',
    header: 'Role',
    cell: ({ row }) => {
      const primaryRole = row.original.roles[0] || 'VIEWER';
      return (
        <Badge variant="outline" className={roleColors[primaryRole]}>
          <Shield className="h-3 w-3 mr-1" />
          {roleLabels[primaryRole] || primaryRole}
        </Badge>
      );
    },
  },
  {
    accessorKey: 'status',
    header: 'Status',
    cell: ({ row }) => {
      const status = getUserStatus(row.original);
      return (
        <StatusBadge
          status={status}
          type="device"
          label={status}
          pulse={status === 'active'}
        />
      );
    },
  },
  {
    accessorKey: 'lastLoginAt',
    header: 'Last Login',
    cell: ({ row }) => (
      <span className="text-xs text-muted-foreground">
        {row.original.lastLoginAt
          ? new Date(row.original.lastLoginAt).toLocaleDateString('id-ID', {
              day: 'numeric',
              month: 'short',
              hour: '2-digit',
              minute: '2-digit',
            })
          : 'Never'}
      </span>
    ),
  },
];

const createColumns = (
  onEdit: (user: User) => void,
  onDelete: (id: string) => void
): ColumnDef<User>[] => [
  ...columns,
  {
    id: 'actions',
    header: 'Actions',
    cell: ({ row }) => (
      <div className="flex gap-2">
        <Button
          size="sm"
          variant="ghost"
          onClick={() => onEdit(row.original)}
        >
          <Edit className="h-4 w-4" />
        </Button>
        <Button
          size="sm"
          variant="ghost"
          onClick={() => onDelete(row.original.id)}
        >
          <Trash2 className="h-4 w-4" />
        </Button>
      </div>
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

export default function UsersPage() {
  const [users, setUsers] = useState<User[]>([]);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(20);
  const [isLoading, setIsLoading] = useState(true);
  const [isAddDialogOpen, setIsAddDialogOpen] = useState(false);
  const [formData, setFormData] = useState({ 
    fullName: '', 
    username: '', 
    email: '', 
    password: '', 
    roleCodes: ['OPERATOR'] as string[] 
  });
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [isEditDialogOpen, setIsEditDialogOpen] = useState(false);
  const [editingUser, setEditingUser] = useState<User | null>(null);
  const [editFormData, setEditFormData] = useState({
    email: '',
    fullName: '',
    phone: '',
    nik: '',
    employeeId: '',
    roleCodes: [] as string[],
    enabled: true,
  });
  const [deleteUserId, setDeleteUserId] = useState<string | null>(null);

  const fetchUsers = useCallback(async () => {
    setIsLoading(true);
    try {
      const response = await userService.getAll({ page, size: pageSize });
      setUsers(response.items);
      setTotal(response.total);
    } catch (error) {
      console.error('Failed to fetch users:', error);
      setUsers([]);
    } finally {
      setIsLoading(false);
    }
  }, [page, pageSize]);

  const handleSubmit = async () => {
    if (!formData.fullName || !formData.username || !formData.email || !formData.password) return;
    setIsSubmitting(true);
    try {
      await userService.create(formData);
      fetchUsers();
      setIsAddDialogOpen(false);
      setFormData({ fullName: '', username: '', email: '', password: '', roleCodes: ['OPERATOR'] });
    } catch (error: unknown) {
      alert(getErrorMessage(error, 'Failed to create user'));
      console.error('Failed to create user:', error);
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleEdit = (user: User) => {
    setEditingUser(user);
    setEditFormData({
      email: user.email,
      fullName: user.fullName,
      phone: user.phone || '',
      nik: user.nik || '',
      employeeId: user.employeeId || '',
      roleCodes: user.roles,
      enabled: user.enabled,
    });
    setIsEditDialogOpen(true);
  };

  const handleEditSubmit = async () => {
    if (!editingUser) return;
    setIsSubmitting(true);
    try {
      await userService.update(editingUser.id, editFormData);
      fetchUsers();
      setIsEditDialogOpen(false);
      setEditingUser(null);
    } catch (error: unknown) {
      alert(getErrorMessage(error, 'Failed to update user'));
      console.error('Failed to update user:', error);
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleDeleteConfirm = async () => {
    if (!deleteUserId) return;
    try {
      await userService.delete(deleteUserId);
      fetchUsers();
      setDeleteUserId(null);
    } catch (error: unknown) {
      alert(getErrorMessage(error, 'Failed to delete user'));
      console.error('Failed to delete user:', error);
    }
  };

  useEffect(() => {
    void Promise.resolve().then(fetchUsers);
  }, [fetchUsers]);

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between gap-2">
        <div className="min-w-0">
          <h1 className="text-xl sm:text-2xl font-bold tracking-tight">User Management</h1>
          <p className="text-xs sm:text-sm text-muted-foreground mt-1">
            Manage users, roles, and permissions
          </p>
        </div>
        <Button 
          size="sm" 
          className="gap-2 shrink-0 bg-gradient-to-r from-blue-600 to-cyan-500 hover:from-blue-500 hover:to-cyan-400"
          onClick={() => setIsAddDialogOpen(true)}
        >
          <UserPlus className="h-4 w-4" />
          <span className="hidden sm:inline">Add User</span>
        </Button>
      </div>

      {/* Stats */}
      <div className="grid gap-2 sm:gap-4 grid-cols-2 sm:grid-cols-4">
        <div className="rounded-xl border border-border/40 bg-card/50 p-3 sm:p-4">
          <p className="text-[10px] sm:text-xs text-muted-foreground">Total Users</p>
          <p className="text-xl sm:text-2xl font-bold mt-1">{total}</p>
        </div>
        <div className="rounded-xl border border-border/40 bg-card/50 p-3 sm:p-4">
          <p className="text-[10px] sm:text-xs text-muted-foreground">Active</p>
          <p className="text-xl sm:text-2xl font-bold mt-1 text-emerald-400">{users.filter((u) => getUserStatus(u) === 'active').length}</p>
        </div>
        <div className="rounded-xl border border-border/40 bg-card/50 p-3 sm:p-4">
          <p className="text-[10px] sm:text-xs text-muted-foreground">Operators</p>
          <p className="text-xl sm:text-2xl font-bold mt-1">{users.filter((u) => u.roles.includes('OPERATOR')).length}</p>
        </div>
        <div className="rounded-xl border border-border/40 bg-card/50 p-3 sm:p-4">
          <p className="text-[10px] sm:text-xs text-muted-foreground">Admins</p>
          <p className="text-xl sm:text-2xl font-bold mt-1">{users.filter((u) => u.roles.some(r => r.includes('ADMIN') || r === 'SUPER_ADMIN')).length}</p>
        </div>
      </div>

      {/* Table */}
      <DataTable
        columns={createColumns(handleEdit, (id) => setDeleteUserId(id))}
        data={users}
        searchKey="fullName"
        searchPlaceholder="Search users..."
        isLoading={isLoading}
        emptyTitle="No users found"
        emptyDescription="No users have been created yet or no results match your search."
      />

      {/* Add User Dialog */}
      <Dialog open={isAddDialogOpen} onOpenChange={setIsAddDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Add New User</DialogTitle>
          </DialogHeader>
          <div className="space-y-4">
            <div className="space-y-2">
              <Label>Full Name</Label>
              <Input 
                placeholder="Enter full name" 
                value={formData.fullName}
                onChange={(e) => setFormData({...formData, fullName: e.target.value})}
              />
            </div>
            <div className="space-y-2">
              <Label>Username</Label>
              <Input 
                placeholder="Enter username" 
                value={formData.username}
                onChange={(e) => setFormData({...formData, username: e.target.value})}
              />
            </div>
            <div className="space-y-2">
              <Label>Email</Label>
              <Input 
                type="email" 
                placeholder="Enter email" 
                value={formData.email}
                onChange={(e) => setFormData({...formData, email: e.target.value})}
              />
            </div>
            <div className="space-y-2">
              <Label>Password</Label>
              <Input 
                type="password" 
                placeholder="Enter password" 
                value={formData.password}
                onChange={(e) => setFormData({...formData, password: e.target.value})}
              />
            </div>
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setIsAddDialogOpen(false)}>Cancel</Button>
            <Button onClick={handleSubmit} disabled={isSubmitting}>
              {isSubmitting ? 'Adding...' : 'Add User'}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* Edit User Dialog */}
      <Dialog open={isEditDialogOpen} onOpenChange={setIsEditDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Edit User</DialogTitle>
          </DialogHeader>
          <div className="space-y-4">
            <div className="space-y-2">
              <Label>Full Name</Label>
              <Input 
                placeholder="Enter full name" 
                value={editFormData.fullName}
                onChange={(e) => setEditFormData({...editFormData, fullName: e.target.value})}
              />
            </div>
            <div className="space-y-2">
              <Label>Email</Label>
              <Input 
                type="email" 
                placeholder="Enter email" 
                value={editFormData.email}
                onChange={(e) => setEditFormData({...editFormData, email: e.target.value})}
              />
            </div>
            <div className="space-y-2">
              <Label>Phone</Label>
              <Input 
                placeholder="Enter phone" 
                value={editFormData.phone}
                onChange={(e) => setEditFormData({...editFormData, phone: e.target.value})}
              />
            </div>
            <div className="space-y-2">
              <Label>NIK</Label>
              <Input 
                placeholder="Enter NIK" 
                value={editFormData.nik}
                onChange={(e) => setEditFormData({...editFormData, nik: e.target.value})}
              />
            </div>
            <div className="space-y-2">
              <Label>Employee ID</Label>
              <Input 
                placeholder="Enter employee ID" 
                value={editFormData.employeeId}
                onChange={(e) => setEditFormData({...editFormData, employeeId: e.target.value})}
              />
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
      <Dialog open={!!deleteUserId} onOpenChange={(open) => !open && setDeleteUserId(null)}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Delete User</DialogTitle>
          </DialogHeader>
          <p className="text-sm text-muted-foreground">
            Are you sure you want to delete this user? This action cannot be undone.
          </p>
          <DialogFooter>
            <Button variant="outline" onClick={() => setDeleteUserId(null)}>Cancel</Button>
            <Button variant="destructive" onClick={handleDeleteConfirm}>
              Delete
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}
