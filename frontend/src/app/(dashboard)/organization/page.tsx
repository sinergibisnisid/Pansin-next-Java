'use client';

import { useState, useEffect } from 'react';
import { motion } from 'framer-motion';
import { Plus, Building2, Search, MapPin, Pencil, Trash2 } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter } from '@/components/ui/dialog';
import { DataTable } from '@/components/tables/data-table';
import { StatusBadge } from '@/components/shared/status-badge';
import { type ColumnDef } from '@tanstack/react-table';
import type { BackendBranch, BackendOrganization } from '@/types';
import { branchService, organizationService } from '@/services';


export default function OrganizationPage() {
  const [search, setSearch] = useState('');
  const [isAddDialogOpen, setIsAddDialogOpen] = useState(false);
  const [isEditDialogOpen, setIsEditDialogOpen] = useState(false);
  const [branches, setBranches] = useState<BackendBranch[]>([]);
  const [organizations, setOrganizations] = useState<BackendOrganization[]>([]);
  const [formData, setFormData] = useState({ 
    organizationId: '', 
    name: '', 
    code: '', 
    city: '', 
    province: '', 
    postalCode: '', 
    address: '', 
    phone: '', 
    email: '', 
    timezone: 'Asia/Jakarta' 
  });
  const [editingBranch, setEditingBranch] = useState<BackendBranch | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [isLoading, setIsLoading] = useState(true);

  const handleSubmit = async () => {
    if (!formData.name || !formData.code || !formData.organizationId) return;
    setIsSubmitting(true);
    try {
      const newBranch = await branchService.create(formData);
      setBranches([...branches, newBranch]);
      setIsAddDialogOpen(false);
      setFormData({ organizationId: '', name: '', code: '', city: '', province: '', postalCode: '', address: '', phone: '', email: '', timezone: 'Asia/Jakarta' });
    } catch (error: any) {
      alert(error.response?.data?.message || 'Failed to create branch. Code may already exist.');
      console.error('Failed to create branch:', error);
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleEdit = (branch: BackendBranch) => {
    setEditingBranch(branch);
    setFormData({
      organizationId: branch.organizationId,
      name: branch.name,
      code: branch.code,
      city: branch.city || '',
      province: branch.province || '',
      postalCode: branch.postalCode || '',
      address: branch.address || '',
      phone: branch.phone || '',
      email: branch.email || '',
      timezone: branch.timezone || 'Asia/Jakarta',
    });
    setIsEditDialogOpen(true);
  };

  const handleUpdate = async () => {
    if (!editingBranch || !formData.name || !formData.code || !formData.organizationId) return;
    setIsSubmitting(true);
    try {
      const updated = await branchService.update(editingBranch.id, formData);
      setBranches(branches.map(b => b.id === editingBranch.id ? updated : b));
      setIsEditDialogOpen(false);
      setEditingBranch(null);
      setFormData({ organizationId: '', name: '', code: '', city: '', province: '', postalCode: '', address: '', phone: '', email: '', timezone: 'Asia/Jakarta' });
    } catch (error: any) {
      alert(error.response?.data?.message || 'Failed to update branch.');
      console.error('Failed to update branch:', error);
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleDelete = async (id: string) => {
    if (!confirm('Are you sure you want to delete this branch?')) return;
    try {
      await branchService.delete(id);
      setBranches(branches.filter(b => b.id !== id));
    } catch (error: any) {
      alert(error.response?.data?.message || 'Failed to delete branch.');
      console.error('Failed to delete branch:', error);
    }
  };

  const columns: ColumnDef<BackendBranch>[] = [
    {
      accessorKey: 'name',
      header: 'Organization',
      cell: ({ row }) => (
        <div className="flex items-center gap-3">
          <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-blue-500/10 border border-blue-500/20">
            <Building2 className="h-4 w-4 text-blue-400" />
          </div>
          <div>
            <p className="text-sm font-medium">{row.original.name}</p>
            <p className="text-xs text-muted-foreground">{row.original.code}</p>
          </div>
        </div>
      ),
    },

    {
      accessorKey: 'city',
      header: 'City',
      cell: ({ row }) => (
        <div className="flex items-center gap-1.5 text-sm">
          <MapPin className="h-3.5 w-3.5 text-muted-foreground" />
          {row.original.city}
        </div>
      ),
    },
    {
      accessorKey: 'province',
      header: 'Province',
      cell: ({ row }) => <span className="text-sm">{row.original.province}</span>,
    },
    {
      accessorKey: 'organizationId',
      header: 'Organization',
      cell: ({ row }) => {
        const org = organizations.find(o => o.id === row.original.organizationId);
        return <span className="text-sm">{org?.name || '-'}</span>;
      },
    },
    {
      accessorKey: 'totalVaults',
      header: 'Vaults',
      cell: ({ row }) => <span className="text-sm">{(row.original as any).totalVaults || 0}</span>,
    },
    {
      accessorKey: 'totalDevices',
      header: 'Devices',
      cell: ({ row }) => <span className="text-sm">{(row.original as any).totalDevices || 0}</span>,
    },
    {
      accessorKey: 'active',
      header: 'Status',
      cell: ({ row }) => (
        <StatusBadge status={row.original.active ? 'active' : 'inactive'} />
      ),
    },
    {
      id: 'actions',
      header: 'Actions',
      cell: ({ row }) => (
        <div className="flex items-center gap-2">
          <Button
            size="sm"
            variant="ghost"
            className="h-8 w-8 p-0"
            onClick={() => handleEdit(row.original)}
          >
            <Pencil className="h-4 w-4" />
          </Button>
          <Button
            size="sm"
            variant="ghost"
            className="h-8 w-8 p-0 text-destructive hover:text-destructive"
            onClick={() => handleDelete(row.original.id)}
          >
            <Trash2 className="h-4 w-4" />
          </Button>
        </div>
      ),
    },
  ];

  useEffect(() => {
    const fetchData = async () => {
      setIsLoading(true);
      try {
        const [branchData, orgData] = await Promise.all([
          branchService.getAll(),
          organizationService.getAll()
        ]);
        setBranches(Array.isArray(branchData) ? branchData : []);
        setOrganizations(Array.isArray(orgData) ? orgData as any : []);
      } catch (error) {
        console.error('Failed to fetch data:', error);
        setBranches([]);
        setOrganizations([]);
      } finally {
        setIsLoading(false);
      }
    };
    fetchData();
  }, []);

  const filtered = branches.filter(
    (org) =>
      org.name.toLowerCase().includes(search.toLowerCase()) ||
      org.code.toLowerCase().includes(search.toLowerCase()) ||
      org.city.toLowerCase().includes(search.toLowerCase())
  );

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between gap-2">
        <div className="min-w-0">
          <h1 className="text-xl sm:text-2xl font-bold tracking-tight">Organization</h1>
          <p className="text-xs sm:text-sm text-muted-foreground mt-1">
            Manage headquarters and branch offices
          </p>
        </div>
        <Button 
          size="sm" 
          className="gap-2 shrink-0 bg-gradient-to-r from-blue-600 to-cyan-500 hover:from-blue-500 hover:to-cyan-400"
          onClick={() => setIsAddDialogOpen(true)}
        >
          <Plus className="h-4 w-4" />
          <span className="hidden sm:inline">Add Branch</span>
        </Button>
      </div>

      {/* Stats */}


      {/* Search */}
      <div className="relative w-full sm:max-w-sm">
        <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
        <Input
          placeholder="Search organization..."
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          className="pl-9 bg-background/50 border-border/40"
        />
      </div>

      {/* Table */}
      <DataTable columns={columns} data={filtered} searchKey="name" searchPlaceholder="Filter by name..." />

      {/* Add Branch Dialog */}
      <Dialog open={isAddDialogOpen} onOpenChange={setIsAddDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Add New Branch</DialogTitle>
          </DialogHeader>
          <div className="space-y-4">
            <div className="space-y-2">
              <Label>Organization *</Label>
              <Select
                value={formData.organizationId}
                onValueChange={(value) => setFormData({...formData, organizationId: value as string})}
              >
                <SelectTrigger>
                  <SelectValue placeholder="Select organization" />
                </SelectTrigger>
                <SelectContent>
                  {organizations.map((org) => (
                    <SelectItem key={org.id} value={org.id}>
                      {org.name}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
            <div className="space-y-2">
              <Label>Branch Name</Label>
              <Input 
                placeholder="Enter branch name" 
                value={formData.name}
                onChange={(e) => setFormData({...formData, name: e.target.value})}
              />
            </div>
            <div className="space-y-2">
              <Label>Branch Code</Label>
              <Input 
                placeholder="Enter branch code" 
                value={formData.code}
                onChange={(e) => setFormData({...formData, code: e.target.value})}
              />
            </div>
            <div className="space-y-2">
              <Label>City</Label>
              <Input 
                placeholder="Enter city" 
                value={formData.city}
                onChange={(e) => setFormData({...formData, city: e.target.value})}
              />
            </div>
            <div className="space-y-2">
              <Label>Province</Label>
              <Input 
                placeholder="Enter province" 
                value={formData.province}
                onChange={(e) => setFormData({...formData, province: e.target.value})}
              />
            </div>
            <div className="space-y-2">
              <Label>Postal Code</Label>
              <Input 
                placeholder="Enter postal code" 
                value={formData.postalCode}
                onChange={(e) => setFormData({...formData, postalCode: e.target.value})}
              />
            </div>
            <div className="space-y-2">
              <Label>Address</Label>
              <Input 
                placeholder="Enter address" 
                value={formData.address}
                onChange={(e) => setFormData({...formData, address: e.target.value})}
              />
            </div>
            <div className="space-y-2">
              <Label>Phone</Label>
              <Input 
                placeholder="Enter phone" 
                value={formData.phone}
                onChange={(e) => setFormData({...formData, phone: e.target.value})}
              />
            </div>
            <div className="space-y-2">
              <Label>Email</Label>
              <Input 
                placeholder="Enter email" 
                value={formData.email}
                onChange={(e) => setFormData({...formData, email: e.target.value})}
              />
            </div>
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setIsAddDialogOpen(false)}>Cancel</Button>
            <Button onClick={handleSubmit} disabled={isSubmitting}>
              {isSubmitting ? 'Adding...' : 'Add Branch'}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* Edit Branch Dialog */}
      <Dialog open={isEditDialogOpen} onOpenChange={setIsEditDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Edit Branch</DialogTitle>
          </DialogHeader>
          <div className="space-y-4">
            <div className="space-y-2">
              <Label>Organization *</Label>
              <Select
                value={formData.organizationId}
                onValueChange={(value) => setFormData({...formData, organizationId: value as string})}
              >
                <SelectTrigger>
                  <SelectValue placeholder="Select organization" />
                </SelectTrigger>
                <SelectContent>
                  {organizations.map((org) => (
                    <SelectItem key={org.id} value={org.id}>
                      {org.name}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
            <div className="space-y-2">
              <Label>Branch Name</Label>
              <Input 
                placeholder="Enter branch name" 
                value={formData.name}
                onChange={(e) => setFormData({...formData, name: e.target.value})}
              />
            </div>
            <div className="space-y-2">
              <Label>Branch Code</Label>
              <Input 
                placeholder="Enter branch code" 
                value={formData.code}
                onChange={(e) => setFormData({...formData, code: e.target.value})}
                disabled
              />
            </div>
            <div className="space-y-2">
              <Label>City</Label>
              <Input 
                placeholder="Enter city" 
                value={formData.city}
                onChange={(e) => setFormData({...formData, city: e.target.value})}
              />
            </div>
            <div className="space-y-2">
              <Label>Province</Label>
              <Input 
                placeholder="Enter province" 
                value={formData.province}
                onChange={(e) => setFormData({...formData, province: e.target.value})}
              />
            </div>
            <div className="space-y-2">
              <Label>Postal Code</Label>
              <Input 
                placeholder="Enter postal code" 
                value={formData.postalCode}
                onChange={(e) => setFormData({...formData, postalCode: e.target.value})}
              />
            </div>
            <div className="space-y-2">
              <Label>Address</Label>
              <Input 
                placeholder="Enter address" 
                value={formData.address}
                onChange={(e) => setFormData({...formData, address: e.target.value})}
              />
            </div>
            <div className="space-y-2">
              <Label>Phone</Label>
              <Input 
                placeholder="Enter phone" 
                value={formData.phone}
                onChange={(e) => setFormData({...formData, phone: e.target.value})}
              />
            </div>
            <div className="space-y-2">
              <Label>Email</Label>
              <Input 
                placeholder="Enter email" 
                value={formData.email}
                onChange={(e) => setFormData({...formData, email: e.target.value})}
              />
            </div>
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setIsEditDialogOpen(false)}>Cancel</Button>
            <Button onClick={handleUpdate} disabled={isSubmitting}>
              {isSubmitting ? 'Updating...' : 'Update Branch'}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}
