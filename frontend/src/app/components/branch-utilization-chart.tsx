'use client';

import { useEffect, useState } from 'react';
import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  PieChart,
  Pie,
  Cell,
  Legend,
  AreaChart,
  Area,
} from 'recharts';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import { Button } from '@/components/ui/button';
import { TrendingUp, Calendar, ChevronLeft, ChevronRight, RefreshCw } from 'lucide-react';
import { monitoringService } from '@/services';
import { cn } from '@/lib/utils';
import type { BranchUtilizationResponse } from '@/types';

const periods = [
  { label: 'Hari Ini', days: 1 },
  { label: 'Minggu Ini', days: 7 },
  { label: 'Bulan Ini', days: 30 },
];

const statusColors: Record<string, string> = {
  OPEN: '#10b981',
  CLOSED: '#64748b',
  LOCKED: '#3b82f6',
  ALARM: '#ef4444',
  MAINTENANCE: '#f59e0b',
  UNKNOWN: '#94a3b8',
};

export function BranchUtilizationChart() {
  const [period, setPeriod] = useState('Minggu Ini');
  const [currentPage, setCurrentPage] = useState(1);
  const [data, setData] = useState<BranchUtilizationResponse | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const totalPages = 2;
  const selectedDays = periods.find((item) => item.label === period)?.days ?? 7;

  const fetchData = async () => {
    setIsLoading(true);
    setError(null);
    try {
      setData(await monitoringService.getBranchUtilization({ days: selectedDays, limit: 20 }));
    } catch (err) {
      console.error('Failed to fetch branch utilization:', err);
      setError('Gagal memuat utilisasi cabang dari backend.');
      setData(null);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    void Promise.resolve().then(fetchData);
  }, [period]);

  const branchChartData = (data?.branches ?? []).map((item) => ({
    branch: item.branchCode,
    name: item.branchName,
    akses: item.accessCount,
    durasi: item.averageDurationMinutes,
    alarm: item.alarmCount,
  }));

  const weeklyTrendData = (data?.weeklyTrend ?? []).map((item) => ({
    day: new Date(item.date).toLocaleDateString('id-ID', { weekday: 'short' }),
    akses: item.accessCount,
    alarm: item.alarmCount,
  }));

  const statusDistribution = (data?.statusDistribution ?? []).map((item) => ({
    name: item.status,
    value: item.count,
    color: statusColors[item.status] ?? statusColors.UNKNOWN,
  }));

  const summary = data?.summary;

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3">
        <div>
          <h2 className="text-lg font-bold">Utilisasi Kantor Cabang</h2>
          <p className="text-xs text-muted-foreground mt-0.5">Statistik penggunaan vault per cabang</p>
        </div>
        <div className="flex items-center gap-2">
          <Select value={period} onValueChange={(v) => setPeriod(v ?? 'Minggu Ini')}>
            <SelectTrigger className="w-[150px] bg-muted/50 border-border/60 text-xs">
              <Calendar className="h-3.5 w-3.5 mr-2 text-muted-foreground" />
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              {periods.map((p) => <SelectItem key={p.label} value={p.label}>{p.label}</SelectItem>)}
            </SelectContent>
          </Select>
          <Button variant="outline" size="icon" className="h-9 w-9" onClick={fetchData} disabled={isLoading}>
            <RefreshCw className={cn('h-3.5 w-3.5', isLoading && 'animate-spin')} />
          </Button>
        </div>
      </div>

      {error && <div className="rounded-lg border border-red-500/20 bg-red-500/10 px-4 py-3 text-sm text-red-400">{error}</div>}
      {isLoading && <div className="h-[260px] flex items-center justify-center text-xs text-muted-foreground">Loading utilization...</div>}
      {!isLoading && !data && !error && <div className="h-[260px] flex items-center justify-center text-xs text-muted-foreground">No utilization data.</div>}

      {!isLoading && data && currentPage === 1 && (
        <>
          <div className="grid gap-2 sm:gap-3 grid-cols-2 lg:grid-cols-4">
            <div className="rounded-xl border border-border/60 bg-card/50 p-3 sm:p-4"><p className="text-[10px] text-muted-foreground uppercase tracking-wider">Total Akses</p><p className="text-xl sm:text-2xl font-bold mt-1">{summary?.totalAccess ?? 0}</p><p className="text-[10px] text-emerald-500 dark:text-emerald-400 mt-0.5 flex items-center gap-1"><TrendingUp className="h-3 w-3" /> Real data</p></div>
            <div className="rounded-xl border border-border/60 bg-card/50 p-3 sm:p-4"><p className="text-[10px] text-muted-foreground uppercase tracking-wider">Rata-rata Durasi</p><p className="text-xl sm:text-2xl font-bold mt-1">{summary?.averageDurationMinutes ?? 0}<span className="text-xs sm:text-sm text-muted-foreground">min</span></p><p className="text-[10px] text-muted-foreground mt-0.5">Per sesi akses</p></div>
            <div className="rounded-xl border border-border/60 bg-card/50 p-3 sm:p-4"><p className="text-[10px] text-muted-foreground uppercase tracking-wider">Total Alarm</p><p className="text-xl sm:text-2xl font-bold text-red-500 dark:text-red-400 mt-1">{summary?.totalAlarms ?? 0}</p><p className="text-[10px] text-red-500/70 dark:text-red-400/70 mt-0.5">Dari backend</p></div>
            <div className="rounded-xl border border-border/60 bg-card/50 p-3 sm:p-4"><p className="text-[10px] text-muted-foreground uppercase tracking-wider">Cabang Aktif</p><p className="text-xl sm:text-2xl font-bold mt-1">{summary?.activeBranches ?? 0}<span className="text-xs sm:text-sm text-muted-foreground">/{summary?.totalBranches ?? 0}</span></p><p className="text-[10px] text-emerald-500 dark:text-emerald-400 mt-0.5">Vault aktif</p></div>
          </div>

          <div className="grid gap-4 lg:grid-cols-3">
            <div className="lg:col-span-2 rounded-xl border border-border/60 bg-card/50 p-3 sm:p-4">
              <h3 className="text-xs sm:text-sm font-medium mb-3 sm:mb-4">Frekuensi Akses per Cabang</h3>
              <div className="h-[220px] sm:h-[300px]">
                {branchChartData.length === 0 ? <div className="h-full flex items-center justify-center text-xs text-muted-foreground">No branch data.</div> : (
                  <ResponsiveContainer width="100%" height="100%">
                    <BarChart data={branchChartData} margin={{ top: 5, right: 10, left: -10, bottom: 5 }}>
                      <CartesianGrid strokeDasharray="3 3" className="stroke-border/40" />
                      <XAxis dataKey="branch" tick={{ fontSize: 10 }} className="text-muted-foreground" axisLine={false} tickLine={false} />
                      <YAxis tick={{ fontSize: 10 }} className="text-muted-foreground" axisLine={false} tickLine={false} />
                      <Tooltip contentStyle={{ backgroundColor: 'hsl(var(--card))', border: '1px solid hsl(var(--border))', borderRadius: '8px', fontSize: '11px', color: 'hsl(var(--foreground))' }} />
                      <Bar dataKey="akses" fill="#3b82f6" radius={[4, 4, 0, 0]} name="Akses" />
                      <Bar dataKey="alarm" fill="#ef4444" radius={[4, 4, 0, 0]} name="Alarm" />
                    </BarChart>
                  </ResponsiveContainer>
                )}
              </div>
            </div>

            <div className="rounded-xl border border-border/60 bg-card/50 p-3 sm:p-4">
              <h3 className="text-xs sm:text-sm font-medium mb-3 sm:mb-4">Distribusi Status Vault</h3>
              <div className="h-[250px] sm:h-[300px]">
                {statusDistribution.length === 0 ? <div className="h-full flex items-center justify-center text-xs text-muted-foreground">No status data.</div> : (
                  <ResponsiveContainer width="100%" height="100%">
                    <PieChart>
                      <Pie data={statusDistribution} cx="50%" cy="45%" innerRadius={55} outerRadius={85} paddingAngle={4} dataKey="value" stroke="none">
                        {statusDistribution.map((entry, index) => <Cell key={`cell-${index}`} fill={entry.color} />)}
                      </Pie>
                      <Tooltip contentStyle={{ backgroundColor: 'hsl(var(--card))', border: '1px solid hsl(var(--border))', borderRadius: '8px', fontSize: '11px', color: 'hsl(var(--foreground))' }} />
                      <Legend verticalAlign="bottom" height={36} formatter={(value) => <span className="text-[10px] text-muted-foreground">{value}</span>} />
                    </PieChart>
                  </ResponsiveContainer>
                )}
              </div>
            </div>
          </div>
        </>
      )}

      {!isLoading && data && currentPage === 2 && (
        <div className="rounded-xl border border-border/60 bg-card/50 p-3 sm:p-4">
          <h3 className="text-xs sm:text-sm font-medium mb-3 sm:mb-4">Tren Akses</h3>
          <div className="h-[300px] sm:h-[400px]">
            {weeklyTrendData.length === 0 ? <div className="h-full flex items-center justify-center text-xs text-muted-foreground">No trend data.</div> : (
              <ResponsiveContainer width="100%" height="100%">
                <AreaChart data={weeklyTrendData} margin={{ top: 5, right: 10, left: -10, bottom: 5 }}>
                  <CartesianGrid strokeDasharray="3 3" className="stroke-border/40" />
                  <XAxis dataKey="day" tick={{ fontSize: 11 }} className="text-muted-foreground" axisLine={false} tickLine={false} />
                  <YAxis tick={{ fontSize: 11 }} className="text-muted-foreground" axisLine={false} tickLine={false} />
                  <Tooltip contentStyle={{ backgroundColor: 'hsl(var(--card))', border: '1px solid hsl(var(--border))', borderRadius: '8px', fontSize: '11px', color: 'hsl(var(--foreground))' }} />
                  <Area type="monotone" dataKey="akses" stroke="#3b82f6" strokeWidth={2} fill="#3b82f633" name="Akses" />
                  <Area type="monotone" dataKey="alarm" stroke="#ef4444" strokeWidth={1.5} fill="none" name="Alarm" />
                </AreaChart>
              </ResponsiveContainer>
            )}
          </div>
        </div>
      )}

      <div className="flex items-center justify-between pt-2">
        <p className="text-[10px] sm:text-xs text-muted-foreground">{currentPage === 1 ? 'Ringkasan & Distribusi' : 'Tren'} &bull; Halaman {currentPage}/{totalPages}</p>
        <div className="flex items-center gap-1.5">
          <Button variant="outline" size="icon" className="h-7 w-7 sm:h-8 sm:w-8 border-border/60" onClick={() => setCurrentPage((p) => Math.max(1, p - 1))} disabled={currentPage === 1}><ChevronLeft className="h-3.5 w-3.5" /></Button>
          <span className="text-xs text-muted-foreground px-2">{currentPage}/{totalPages}</span>
          <Button variant="outline" size="icon" className="h-7 w-7 sm:h-8 sm:w-8 border-border/60" onClick={() => setCurrentPage((p) => Math.min(totalPages, p + 1))} disabled={currentPage === totalPages}><ChevronRight className="h-3.5 w-3.5" /></Button>
        </div>
      </div>
    </div>
  );
}
