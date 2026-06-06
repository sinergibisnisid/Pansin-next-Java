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
} from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter } from '@/components/ui/dialog';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { cn } from '@/lib/utils';
import type { MaintenanceSchedule } from '@/types';
import { maintenanceService, organizationService } from '@/services';



const statusConfig: Record<string, { color: string; icon: typeof CheckCircle; label: string }> = {
  scheduled: { color: 'bg-blue-500/20 text-blue-400 border-blue-500/30', icon: Calendar, label: 'Scheduled' },
  in_progress: { color: 'bg-amber-500/20 text-amber-400 border-amber-500/30', icon: Clock, label: 'In Progress' },
  completed: { color: 'bg-emerald-500/20 text-emerald-400 border-emerald-500/30', icon: CheckCircle, label: 'Completed' },
  overdue: { color: 'bg-red-500/20 text-red-400 border-red-500/30', icon: AlertCircle, label: 'Overdue' },
  cancelled: { color: 'bg-slate-500/20 text-slate-400 border-slate-500/30', icon: AlertCircle, label: 'Cancelled' },
};

const typeColors: Record<string, string> = {
  cleaning: 'bg-cyan-500/20 text-cyan-400',
  lubrication: 'bg-amber-500/20 text-amber-400',
  inspection: 'bg-blue-500/20 text-blue-400',
  repair: 'bg-red-500/20 text-red-400',
  calibration: 'bg-purple-500/20 text-purple-400',
};

export default function MaintenancePage() {
  const [filter, setFilter] = useState<string>('all');
  const [isAddDialogOpen, setIsAddDialogOpen] = useState(false);
  const [schedules, setSchedules] = useState<MaintenanceSchedule[]>([]);
  const [formData, setFormData] = useState({ 
    vaultId: '', 
    type: 'cleaning', 
    name: '',
    description: '',
    intervalDays: 30,
    nextDueAt: '' 
  });
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [branches, setBranches] = useState<any[]>([]);
  const [isBranchesLoading, setIsBranchesLoading] = useState(false);

  const handleSubmit = async () => {
    if (!formData.vaultId || !formData.name || !formData.type) return;
    setIsSubmitting(true);
    try {
      await maintenanceService.createPlan(formData);
      // Refresh logs after creating plan
      const logs = await maintenanceService.getLogs();
      setSchedules(Array.isArray(logs) ? logs : []);
      setIsAddDialogOpen(false);
      setFormData({ 
        vaultId: '', 
        type: 'cleaning', 
        name: '',
        description: '',
        intervalDays: 30,
        nextDueAt: '' 
      });
    } catch (error: any) {
      alert(error.response?.data?.message || 'Failed to create maintenance plan');
      console.error('Failed to create maintenance plan:', error);
    } finally {
      setIsSubmitting(false);
    }
  };

  useEffect(() => {
    maintenanceService.getLogs()
      .then(data => setSchedules(Array.isArray(data) ? data : []))
      .catch(err => {
        console.error('Failed to fetch maintenance logs:', err);
        setSchedules([]);
      });
  }, []);

  useEffect(() => {
    const fetchBranches = async () => {
      setIsBranchesLoading(true);
      try {
        const orgs = await organizationService.getAll();
        setBranches(Array.isArray(orgs) ? orgs.sort((a, b) => a.name.localeCompare(b.name)) : []);
      } catch (error) {
        console.error('Failed to load branches:', error);
      } finally {
        setIsBranchesLoading(false);
      }
    };
    fetchBranches();
  }, []);

  const filtered = filter === 'all' ? schedules : schedules.filter((s) => s.status === filter);

  const counts = {
    all: schedules.length,
    scheduled: schedules.filter((s) => s.status === 'scheduled').length,
    in_progress: schedules.filter((s) => s.status === 'in_progress').length,
    completed: schedules.filter((s) => s.status === 'completed').length,
    overdue: schedules.filter((s) => s.status === 'overdue').length,
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

      {/* Filter Tabs */}
      <div className="flex items-center gap-2 overflow-x-auto pb-1">
        {Object.entries(counts).map(([key, count]) => (
          <Button
            key={key}
            variant={filter === key ? 'secondary' : 'ghost'}
            size="sm"
            className="gap-2 whitespace-nowrap"
            onClick={() => setFilter(key)}
          >
            {key === 'all' ? 'All' : key.replace('_', ' ')}
            <Badge variant="outline" className="h-5 min-w-[20px] text-[10px]">
              {count}
            </Badge>
          </Button>
        ))}
      </div>

      {/* Timeline */}
      <div className="space-y-3">
        {filtered.map((schedule, index) => {
          const config = statusConfig[schedule.status] || statusConfig.scheduled;
          const StatusIcon = config.icon;

          return (
            <motion.div
              key={schedule.id}
              initial={{ opacity: 0, x: -20 }}
              animate={{ opacity: 1, x: 0 }}
              transition={{ delay: index * 0.05 }}
              className={cn(
                'relative flex gap-4 rounded-xl border p-4 transition-all hover:bg-muted/20',
                schedule.status === 'overdue'
                  ? 'border-red-500/30 bg-red-500/5'
                  : 'border-border/40 bg-card/50'
              )}
            >
              {/* Status Icon */}
              <div className={cn('flex h-10 w-10 shrink-0 items-center justify-center rounded-lg border', config.color)}>
                <StatusIcon className="h-5 w-5" />
              </div>

              {/* Content */}
              <div className="flex-1 min-w-0">
                <div className="flex items-start justify-between gap-2">
                  <div>
                    <p className="text-sm font-medium">{schedule.branchName}</p>
                    <p className="text-xs text-muted-foreground mt-0.5">{schedule.vaultName}</p>
                  </div>
                  <Badge variant="outline" className={cn('text-[10px] shrink-0', config.color)}>
                    {config.label}
                  </Badge>
                </div>

                <div className="mt-2 flex flex-wrap items-center gap-3 text-xs text-muted-foreground">
                  <span className={cn('px-2 py-0.5 rounded-full font-medium', typeColors[schedule.type])}>
                    {schedule.type}
                  </span>
                  <span className="flex items-center gap-1">
                    <Calendar className="h-3 w-3" />
                    {new Date(schedule.scheduledDate).toLocaleDateString('id-ID', {
                      day: 'numeric',
                      month: 'short',
                      year: 'numeric',
                    })}
                  </span>
                  <span className="flex items-center gap-1">
                    <Wrench className="h-3 w-3" />
                    {schedule.assignedTo}
                  </span>
                </div>

                {schedule.notes && (
                  <p className="mt-2 text-xs text-muted-foreground/80">{schedule.notes}</p>
                )}
              </div>
            </motion.div>
          );
        })}
      </div>

      {/* Add Maintenance Dialog */}
      <Dialog open={isAddDialogOpen} onOpenChange={setIsAddDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Schedule Maintenance</DialogTitle>
          </DialogHeader>
          <div className="space-y-4">
            <div className="space-y-2">
              <Label>Vault ID</Label>
              <Input 
                placeholder="Enter vault ID" 
                value={formData.vaultId} 
                onChange={(e) => setFormData({...formData, vaultId: e.target.value})} 
              />
            </div>
            <div className="space-y-2">
              <Label>Maintenance Name</Label>
              <Input 
                placeholder="e.g. Monthly Inspection" 
                value={formData.name} 
                onChange={(e) => setFormData({...formData, name: e.target.value})} 
              />
            </div>
            <div className="space-y-2">
              <Label>Type</Label>
              <Select 
                value={formData.type} 
                onValueChange={(value) => setFormData({...formData, type: value as string})}
              >
                <SelectTrigger>
                  <SelectValue placeholder="Select maintenance type" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="cleaning">Cleaning</SelectItem>
                  <SelectItem value="inspection">Inspection</SelectItem>
                  <SelectItem value="repair">Repair</SelectItem>
                  <SelectItem value="lubrication">Lubrication</SelectItem>
                  <SelectItem value="calibration">Calibration</SelectItem>
                </SelectContent>
              </Select>
            </div>
            <div className="space-y-2">
              <Label>Description</Label>
              <Input 
                placeholder="Optional description" 
                value={formData.description} 
                onChange={(e) => setFormData({...formData, description: e.target.value})} 
              />
            </div>
            <div className="space-y-2">
              <Label>Interval (Days)</Label>
              <Input 
                type="number" 
                placeholder="30" 
                value={formData.intervalDays} 
                onChange={(e) => setFormData({...formData, intervalDays: parseInt(e.target.value) || 30})} 
              />
            </div>
            <div className="space-y-2">
              <Label>Next Due Date</Label>
              <Input 
                type="datetime-local" 
                value={formData.nextDueAt} 
                onChange={(e) => setFormData({...formData, nextDueAt: e.target.value})} 
              />
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
    </div>
  );
}
