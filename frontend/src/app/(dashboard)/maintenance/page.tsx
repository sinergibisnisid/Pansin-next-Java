'use client';

import { useState, useEffect } from 'react';
import { motion } from 'framer-motion';
import {
  Wrench,
  Calendar,
  CheckCircle,
  Clock,
  AlertCircle,
  Plus,
  Edit,
  Trash2,
} from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter } from '@/components/ui/dialog';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { cn } from '@/lib/utils';
import type { MaintenancePlan, MaintenanceLog } from '@/types';
import { maintenanceService } from '@/services';

const typeColors: Record<string, string> = {
  cleaning: 'bg-cyan-500/20 text-cyan-400',
  lubrication: 'bg-amber-500/20 text-amber-400',
  inspection: 'bg-blue-500/20 text-blue-400',
  repair: 'bg-red-500/20 text-red-400',
  calibration: 'bg-purple-500/20 text-purple-400',
};

const emptyForm = {
  vaultId: '',
  deviceId: '',
  type: 'inspection',
  name: '',
  description: '',
  intervalDays: 30,
  nextDueAt: '',
};

export default function MaintenancePage() {
  const [tab, setTab] = useState<'plans' | 'logs'>('plans');
  const [plans, setPlans] = useState<MaintenancePlan[]>([]);
  const [logs, setLogs] = useState<MaintenanceLog[]>([]);
  const [total, setTotal] = useState(0);
  const [isLoading, setIsLoading] = useState(true);

  const [isAddDialogOpen, setIsAddDialogOpen] = useState(false);
  const [formData, setFormData] = useState({ ...emptyForm });
  const [isSubmitting, setIsSubmitting] = useState(false);

  const [isEditDialogOpen, setIsEditDialogOpen] = useState(false);
  const [editingPlan, setEditingPlan] = useState<MaintenancePlan | null>(null);
  const [editFormData, setEditFormData] = useState({ ...emptyForm });

  const [deletePlanId, setDeletePlanId] = useState<string | null>(null);

  const fetchPlans = async () => {
    setIsLoading(true);
    try {
      const response = await maintenanceService.getPlans();
      setPlans(response.items);
      setTotal(response.total);
    } catch (error) {
      console.error('Failed to fetch maintenance plans:', error);
      setPlans([]);
    } finally {
      setIsLoading(false);
    }
  };

  const fetchLogs = async () => {
    try {
      const data = await maintenanceService.getLogs();
      setLogs(data);
    } catch (error) {
      console.error('Failed to fetch maintenance logs:', error);
      setLogs([]);
    }
  };

  useEffect(() => {
    fetchPlans();
    fetchLogs();
  }, []);

  const handleSubmit = async () => {
    if (!formData.name || !formData.type) return;
    setIsSubmitting(true);
    try {
      await maintenanceService.createPlan(formData);
      fetchPlans();
      setIsAddDialogOpen(false);
      setFormData({ ...emptyForm });
    } catch (error: any) {
      alert(error.response?.data?.message || 'Failed to create maintenance plan');
      console.error('Failed to create maintenance plan:', error);
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleEdit = (plan: MaintenancePlan) => {
    setEditingPlan(plan);
    setEditFormData({
      vaultId: plan.vault?.id || '',
      deviceId: plan.device?.id || '',
      type: plan.type,
      name: plan.name,
      description: plan.description || '',
      intervalDays: plan.intervalDays,
      nextDueAt: plan.nextDueAt
        ? new Date(plan.nextDueAt).toISOString().slice(0, 16)
        : '',
    });
    setIsEditDialogOpen(true);
  };

  const handleEditSubmit = async () => {
    if (!editingPlan) return;
    setIsSubmitting(true);
    try {
      await maintenanceService.updatePlan(editingPlan.id, editFormData);
      fetchPlans();
      setIsEditDialogOpen(false);
      setEditingPlan(null);
    } catch (error: any) {
      alert(error.response?.data?.message || 'Failed to update maintenance plan');
      console.error('Failed to update maintenance plan:', error);
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleDeleteConfirm = async () => {
    if (!deletePlanId) return;
    try {
      await maintenanceService.deletePlan(deletePlanId);
      fetchPlans();
      setDeletePlanId(null);
    } catch (error: any) {
      alert(error.response?.data?.message || 'Failed to delete maintenance plan');
      console.error('Failed to delete maintenance plan:', error);
    }
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold tracking-tight">Maintenance</h1>
          <p className="text-sm text-muted-foreground mt-1">
            Schedule and track vault maintenance activities
          </p>
        </div>
        <Button
          className="gap-2 bg-gradient-to-r from-blue-600 to-cyan-500 hover:from-blue-500 hover:to-cyan-400"
          onClick={() => setIsAddDialogOpen(true)}
        >
          <Plus className="h-4 w-4" />
          Schedule Maintenance
        </Button>
      </div>

      {/* Tab Toggle */}
      <div className="flex gap-2">
        <Button
          size="sm"
          variant={tab === 'plans' ? 'secondary' : 'ghost'}
          onClick={() => setTab('plans')}
        >
          Plans
          <Badge variant="outline" className="ml-2 h-5 min-w-[20px] text-[10px]">{total}</Badge>
        </Button>
        <Button
          size="sm"
          variant={tab === 'logs' ? 'secondary' : 'ghost'}
          onClick={() => setTab('logs')}
        >
          Logs
          <Badge variant="outline" className="ml-2 h-5 min-w-[20px] text-[10px]">{logs.length}</Badge>
        </Button>
      </div>

      {/* Plans List */}
      {tab === 'plans' && (
        <div className="space-y-3">
          {isLoading && (
            <p className="text-sm text-muted-foreground py-8 text-center">Loading...</p>
          )}
          {!isLoading && plans.length === 0 && (
            <p className="text-sm text-muted-foreground py-8 text-center">No maintenance plans found.</p>
          )}
          {plans.map((plan, index) => (
            <motion.div
              key={plan.id}
              initial={{ opacity: 0, x: -20 }}
              animate={{ opacity: 1, x: 0 }}
              transition={{ delay: index * 0.05 }}
              className="relative flex gap-4 rounded-xl border border-border/40 bg-card/50 p-4 transition-all hover:bg-muted/20"
            >
              {/* Icon */}
              <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-lg border border-border/40 bg-muted/30">
                <Wrench className="h-5 w-5 text-muted-foreground" />
              </div>

              {/* Content */}
              <div className="flex-1 min-w-0">
                <div className="flex items-start justify-between gap-2">
                  <div>
                    <p className="text-sm font-medium">{plan.name}</p>
                    <p className="text-xs text-muted-foreground mt-0.5">
                      {plan.vault?.name || 'No vault'}
                      {plan.device ? ` • ${plan.device.name || plan.device.deviceCode}` : ''}
                    </p>
                  </div>
                  <div className="flex items-center gap-1">
                    <Badge
                      variant="outline"
                      className={plan.active
                        ? 'text-[10px] bg-emerald-500/20 text-emerald-400 border-emerald-500/30'
                        : 'text-[10px] bg-slate-500/20 text-slate-400 border-slate-500/30'
                      }
                    >
                      {plan.active ? 'Active' : 'Inactive'}
                    </Badge>
                    <Button size="sm" variant="ghost" onClick={() => handleEdit(plan)}>
                      <Edit className="h-4 w-4" />
                    </Button>
                    <Button size="sm" variant="ghost" onClick={() => setDeletePlanId(plan.id)}>
                      <Trash2 className="h-4 w-4" />
                    </Button>
                  </div>
                </div>

                <div className="mt-2 flex flex-wrap items-center gap-3 text-xs text-muted-foreground">
                  <span className={cn('px-2 py-0.5 rounded-full font-medium', typeColors[plan.type] || 'bg-muted/50 text-muted-foreground')}>
                    {plan.type}
                  </span>
                  {plan.nextDueAt && (
                    <span className="flex items-center gap-1">
                      <Calendar className="h-3 w-3" />
                      Due: {new Date(plan.nextDueAt).toLocaleDateString('id-ID', {
                        day: 'numeric',
                        month: 'short',
                        year: 'numeric',
                      })}
                    </span>
                  )}
                  <span className="flex items-center gap-1">
                    <Clock className="h-3 w-3" />
                    Every {plan.intervalDays} days
                  </span>
                </div>

                {plan.description && (
                  <p className="mt-2 text-xs text-muted-foreground/80">{plan.description}</p>
                )}
              </div>
            </motion.div>
          ))}
        </div>
      )}

      {/* Logs List */}
      {tab === 'logs' && (
        <div className="space-y-3">
          {logs.length === 0 && (
            <p className="text-sm text-muted-foreground py-8 text-center">No maintenance logs found.</p>
          )}
          {logs.map((log, index) => (
            <motion.div
              key={log.id}
              initial={{ opacity: 0, x: -20 }}
              animate={{ opacity: 1, x: 0 }}
              transition={{ delay: index * 0.05 }}
              className="relative flex gap-4 rounded-xl border border-border/40 bg-card/50 p-4"
            >
              <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-lg border border-emerald-500/30 bg-emerald-500/10">
                <CheckCircle className="h-5 w-5 text-emerald-400" />
              </div>
              <div className="flex-1 min-w-0">
                <div className="flex items-start justify-between gap-2">
                  <div>
                    <p className="text-sm font-medium">
                      {log.vault?.name || log.plan?.name || 'Maintenance Log'}
                    </p>
                    <p className="text-xs text-muted-foreground mt-0.5">
                      {log.performedBy?.fullName || 'Unknown'}
                      {log.device ? ` • ${log.device.name}` : ''}
                    </p>
                  </div>
                  <Badge variant="outline" className="text-[10px] bg-emerald-500/20 text-emerald-400 border-emerald-500/30">
                    {log.status}
                  </Badge>
                </div>
                <div className="mt-2 flex flex-wrap items-center gap-3 text-xs text-muted-foreground">
                  <span className={cn('px-2 py-0.5 rounded-full font-medium', typeColors[log.type] || 'bg-muted/50 text-muted-foreground')}>
                    {log.type}
                  </span>
                  <span className="flex items-center gap-1">
                    <Calendar className="h-3 w-3" />
                    {new Date(log.performedAt).toLocaleDateString('id-ID', {
                      day: 'numeric',
                      month: 'short',
                      year: 'numeric',
                    })}
                  </span>
                </div>
                {log.notes && (
                  <p className="mt-2 text-xs text-muted-foreground/80">{log.notes}</p>
                )}
              </div>
            </motion.div>
          ))}
        </div>
      )}

      {/* Create Plan Dialog */}
      <Dialog open={isAddDialogOpen} onOpenChange={setIsAddDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Schedule Maintenance</DialogTitle>
          </DialogHeader>
          <div className="space-y-4">
            <div className="space-y-2">
              <Label>Plan Name *</Label>
              <Input placeholder="e.g. Monthly Inspection" value={formData.name} onChange={(e) => setFormData({...formData, name: e.target.value})} />
            </div>
            <div className="space-y-2">
              <Label>Type *</Label>
              <Select value={formData.type} onValueChange={(value) => setFormData({...formData, type: value as string})}>
                <SelectTrigger><SelectValue placeholder="Select type" /></SelectTrigger>
                <SelectContent>
                  <SelectItem value="inspection">Inspection</SelectItem>
                  <SelectItem value="cleaning">Cleaning</SelectItem>
                  <SelectItem value="repair">Repair</SelectItem>
                  <SelectItem value="lubrication">Lubrication</SelectItem>
                  <SelectItem value="calibration">Calibration</SelectItem>
                </SelectContent>
              </Select>
            </div>
            <div className="space-y-2">
              <Label>Vault ID</Label>
              <Input placeholder="Enter vault ID" value={formData.vaultId} onChange={(e) => setFormData({...formData, vaultId: e.target.value})} />
            </div>
            <div className="space-y-2">
              <Label>Device ID</Label>
              <Input placeholder="Enter device ID" value={formData.deviceId} onChange={(e) => setFormData({...formData, deviceId: e.target.value})} />
            </div>
            <div className="space-y-2">
              <Label>Description</Label>
              <Input placeholder="Optional description" value={formData.description} onChange={(e) => setFormData({...formData, description: e.target.value})} />
            </div>
            <div className="space-y-2">
              <Label>Interval (Days)</Label>
              <Input type="number" placeholder="30" value={formData.intervalDays} onChange={(e) => setFormData({...formData, intervalDays: parseInt(e.target.value) || 30})} />
            </div>
            <div className="space-y-2">
              <Label>Next Due Date</Label>
              <Input type="datetime-local" value={formData.nextDueAt} onChange={(e) => setFormData({...formData, nextDueAt: e.target.value})} />
            </div>
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setIsAddDialogOpen(false)}>Cancel</Button>
            <Button onClick={handleSubmit} disabled={isSubmitting}>
              {isSubmitting ? 'Scheduling...' : 'Schedule'}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* Edit Plan Dialog */}
      <Dialog open={isEditDialogOpen} onOpenChange={setIsEditDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Edit Maintenance Plan</DialogTitle>
          </DialogHeader>
          <div className="space-y-4">
            <div className="space-y-2">
              <Label>Plan Name</Label>
              <Input placeholder="e.g. Monthly Inspection" value={editFormData.name} onChange={(e) => setEditFormData({...editFormData, name: e.target.value})} />
            </div>
            <div className="space-y-2">
              <Label>Type</Label>
              <Select value={editFormData.type} onValueChange={(value) => setEditFormData({...editFormData, type: value as string})}>
                <SelectTrigger><SelectValue placeholder="Select type" /></SelectTrigger>
                <SelectContent>
                  <SelectItem value="inspection">Inspection</SelectItem>
                  <SelectItem value="cleaning">Cleaning</SelectItem>
                  <SelectItem value="repair">Repair</SelectItem>
                  <SelectItem value="lubrication">Lubrication</SelectItem>
                  <SelectItem value="calibration">Calibration</SelectItem>
                </SelectContent>
              </Select>
            </div>
            <div className="space-y-2">
              <Label>Vault ID</Label>
              <Input placeholder="Enter vault ID" value={editFormData.vaultId} onChange={(e) => setEditFormData({...editFormData, vaultId: e.target.value})} />
            </div>
            <div className="space-y-2">
              <Label>Device ID</Label>
              <Input placeholder="Enter device ID" value={editFormData.deviceId} onChange={(e) => setEditFormData({...editFormData, deviceId: e.target.value})} />
            </div>
            <div className="space-y-2">
              <Label>Description</Label>
              <Input placeholder="Optional description" value={editFormData.description} onChange={(e) => setEditFormData({...editFormData, description: e.target.value})} />
            </div>
            <div className="space-y-2">
              <Label>Interval (Days)</Label>
              <Input type="number" value={editFormData.intervalDays} onChange={(e) => setEditFormData({...editFormData, intervalDays: parseInt(e.target.value) || 30})} />
            </div>
            <div className="space-y-2">
              <Label>Next Due Date</Label>
              <Input type="datetime-local" value={editFormData.nextDueAt} onChange={(e) => setEditFormData({...editFormData, nextDueAt: e.target.value})} />
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
      <Dialog open={!!deletePlanId} onOpenChange={(open) => !open && setDeletePlanId(null)}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Delete Maintenance Plan</DialogTitle>
          </DialogHeader>
          <p className="text-sm text-muted-foreground">
            Are you sure you want to delete this maintenance plan? This action cannot be undone.
          </p>
          <DialogFooter>
            <Button variant="outline" onClick={() => setDeletePlanId(null)}>Cancel</Button>
            <Button variant="destructive" onClick={handleDeleteConfirm}>Delete</Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}
