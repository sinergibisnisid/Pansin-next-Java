'use client';

import { useEffect, useMemo, useState } from 'react';
import { motion } from 'framer-motion';
import {
  Building2,
  Filter,
  Video,
  VideoOff,
  Maximize2,
  User,
  Clock,
  Thermometer,
  AlertTriangle,
  ChevronLeft,
  ChevronRight,
  RefreshCw,
} from 'lucide-react';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { cn } from '@/lib/utils';
import { livestreamService, monitoringService } from '@/services';
import type { LivestreamSession, VaultMonitor } from '@/types';

interface VaultStream {
  id: string;
  branchName: string;
  branchCode: string;
  organization: string;
  status: 'online' | 'offline';
  vaultStatus: 'open' | 'closed' | 'alarm' | 'locked' | 'maintenance';
  currentUser: string | null;
  temperature: number;
  duration: string | null;
  streamUrl?: string | null;
}

const vaultStatusConfig: Record<string, { label: string; color: string; bgColor: string }> = {
  open: { label: 'OPEN', color: 'text-emerald-400', bgColor: 'bg-emerald-500/20 border-emerald-500/30' },
  closed: { label: 'CLOSED', color: 'text-slate-400', bgColor: 'bg-slate-500/20 border-slate-500/30' },
  alarm: { label: 'ALARM', color: 'text-red-400', bgColor: 'bg-red-500/20 border-red-500/30' },
  locked: { label: 'LOCKED', color: 'text-blue-400', bgColor: 'bg-blue-500/20 border-blue-500/30' },
  maintenance: { label: 'MAINT', color: 'text-amber-400', bgColor: 'bg-amber-500/20 border-amber-500/30' },
};

const ITEMS_PER_PAGE = 8;

function formatDuration(startedAt?: string) {
  if (!startedAt) return null;
  const diffSeconds = Math.max(0, Math.floor((Date.now() - new Date(startedAt).getTime()) / 1000));
  const minutes = Math.floor(diffSeconds / 60).toString().padStart(2, '0');
  const seconds = (diffSeconds % 60).toString().padStart(2, '0');
  return `${minutes}:${seconds}`;
}

function mapSession(session: LivestreamSession, monitor?: VaultMonitor): VaultStream {
  const branch = session.vault?.branch;
  const isActive = session.status === 'ACTIVE';
  return {
    id: session.id,
    branchName: branch?.name ?? monitor?.branchName ?? session.vault?.name ?? 'Unknown Branch',
    branchCode: branch?.code ?? monitor?.branchCode ?? session.vault?.code ?? '-',
    organization: branch?.organization?.name ?? 'Unassigned',
    status: isActive ? 'online' : 'offline',
    vaultStatus: (monitor?.status ?? 'closed') as VaultStream['vaultStatus'],
    currentUser: session.user?.fullName ?? session.user?.username ?? monitor?.currentUser ?? null,
    temperature: monitor?.temperature ?? 0,
    duration: isActive ? formatDuration(session.startedAt) : null,
    streamUrl: session.streamUrl,
  };
}

export function LiveStreamGrid() {
  const [orgFilter, setOrgFilter] = useState('Semua Organisasi');
  const [branchHighlight, setBranchHighlight] = useState<string | null>(null);
  const [currentPage, setCurrentPage] = useState(1);
  const [streams, setStreams] = useState<VaultStream[]>([]);
  const [health, setHealth] = useState<string>('UNKNOWN');
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchStreams = async () => {
    setIsLoading(true);
    setError(null);
    try {
      const [sessions, vaults, mediaHealth] = await Promise.all([
        livestreamService.getSessions(),
        monitoringService.getVaults(),
        livestreamService.health().catch(() => 'UNKNOWN'),
      ]);
      const monitorByVaultId = new Map(vaults.map((vault) => [vault.id, vault]));
      setStreams(sessions.map((session) => mapSession(session, session.vault?.id ? monitorByVaultId.get(session.vault.id) : undefined)));
      setHealth(mediaHealth);
    } catch (err) {
      console.error('Failed to fetch livestream sessions:', err);
      setStreams([]);
      setHealth('UNKNOWN');
      setError('Gagal memuat livestream dari backend.');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    void Promise.resolve().then(fetchStreams);
  }, []);

  const organizations = useMemo(() => ['Semua Organisasi', ...Array.from(new Set(streams.map((stream) => stream.organization))).sort()], [streams]);

  const filteredStreams = streams.filter((stream) => {
    if (orgFilter !== 'Semua Organisasi' && stream.organization !== orgFilter) return false;
    return true;
  });

  const totalPages = Math.max(1, Math.ceil(filteredStreams.length / ITEMS_PER_PAGE));
  const paginatedStreams = useMemo(() => {
    const start = (currentPage - 1) * ITEMS_PER_PAGE;
    return filteredStreams.slice(start, start + ITEMS_PER_PAGE);
  }, [filteredStreams, currentPage]);

  const handleOrgFilter = (v: string | null) => {
    setOrgFilter(v ?? 'Semua Organisasi');
    setCurrentPage(1);
  };

  return (
    <div className="space-y-4">
      <div className="flex flex-col gap-3">
        <div className="flex flex-col sm:flex-row items-stretch sm:items-center gap-2 sm:gap-3">
          <Select value={orgFilter} onValueChange={handleOrgFilter}>
            <SelectTrigger className="w-full sm:w-[200px] bg-muted/50 border-border/60 text-xs">
              <Building2 className="h-3.5 w-3.5 mr-2 text-muted-foreground" />
              <SelectValue />
            </SelectTrigger>
            <SelectContent>{organizations.map((org) => <SelectItem key={org} value={org}>{org}</SelectItem>)}</SelectContent>
          </Select>

          <Select value={branchHighlight ?? 'all'} onValueChange={(v) => setBranchHighlight(v === 'all' ? null : (v ?? null))}>
            <SelectTrigger className="w-full sm:w-[180px] bg-muted/50 border-border/60 text-xs">
              <Filter className="h-3.5 w-3.5 mr-2 text-muted-foreground" />
              <SelectValue placeholder="Highlight Cabang" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="all">Semua Cabang</SelectItem>
              {filteredStreams.map((s) => <SelectItem key={s.id} value={s.id}>{s.branchName}</SelectItem>)}
            </SelectContent>
          </Select>

          <Button variant="outline" size="sm" className="gap-1.5 text-xs" onClick={fetchStreams} disabled={isLoading}>
            <RefreshCw className={cn('h-3.5 w-3.5', isLoading && 'animate-spin')} />
            Refresh
          </Button>
          <Badge variant="outline" className={cn('text-[10px]', health === 'UP' ? 'text-emerald-400 border-emerald-500/30' : 'text-amber-400 border-amber-500/30')}>MediaMTX {health}</Badge>
        </div>

        <div className="flex items-center gap-3 text-[10px]">
          <span className="flex items-center gap-1"><span className="h-2 w-2 rounded-full bg-emerald-500" /> OPEN</span>
          <span className="flex items-center gap-1"><span className="h-2 w-2 rounded-full bg-slate-500" /> CLOSED</span>
          <span className="flex items-center gap-1"><span className="h-2 w-2 rounded-full bg-blue-500" /> LOCKED</span>
          <span className="flex items-center gap-1"><span className="h-2 w-2 rounded-full bg-red-500" /> ALARM</span>
        </div>
      </div>

      {error && <div className="rounded-lg border border-red-500/20 bg-red-500/10 px-4 py-3 text-sm text-red-400">{error}</div>}
      {isLoading && <div className="h-40 flex items-center justify-center text-xs text-muted-foreground">Loading livestream...</div>}
      {!isLoading && filteredStreams.length === 0 && <div className="h-40 flex items-center justify-center text-xs text-muted-foreground">No livestream sessions available.</div>}

      {!isLoading && filteredStreams.length > 0 && (
        <div className="grid gap-2 sm:gap-3 grid-cols-1 xs:grid-cols-2 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
          {paginatedStreams.map((stream, index) => {
            const isHighlighted = branchHighlight === null || branchHighlight === stream.id;
            const statusCfg = vaultStatusConfig[stream.vaultStatus] ?? vaultStatusConfig.closed;
            const isAlarm = stream.vaultStatus === 'alarm';
            return (
              <motion.div key={stream.id} initial={{ opacity: 0, y: 10 }} animate={{ opacity: isHighlighted ? 1 : 0.3, y: 0 }} transition={{ duration: 0.3, delay: index * 0.03 }} className={cn('relative overflow-hidden rounded-xl border transition-all', isAlarm ? 'border-red-500/40 shadow-lg shadow-red-500/10' : isHighlighted ? 'border-border/60 hover:border-primary/30' : 'border-border/30', 'bg-card/50')}>
                {isAlarm && <motion.div animate={{ opacity: [0.05, 0.15, 0.05] }} transition={{ duration: 1.5, repeat: Infinity }} className="absolute inset-0 bg-red-500/10" />}
                <div className="relative h-28 sm:h-36 bg-muted dark:bg-slate-900/80 flex items-center justify-center">
                  {stream.status === 'online' ? (
                    <>
                      <div className="absolute inset-0 bg-gradient-to-br from-slate-800/50 to-slate-900/80" />
                      <Video className="h-8 w-8 text-muted-foreground/30" />
                      <div className="absolute top-2 left-2 flex items-center gap-1.5 rounded bg-black/60 dark:bg-black/60 px-1.5 py-0.5"><span className="relative flex h-1.5 w-1.5"><span className="absolute inline-flex h-full w-full animate-ping rounded-full bg-red-500 opacity-75" /><span className="relative inline-flex h-1.5 w-1.5 rounded-full bg-red-500" /></span><span className="text-[9px] font-medium text-white/80">LIVE</span></div>
                      <div className="absolute top-2 right-2"><Badge variant="outline" className={cn('text-[9px] px-1.5 py-0 border', statusCfg.bgColor, statusCfg.color)}>{statusCfg.label}</Badge></div>
                      {stream.streamUrl && <a href={stream.streamUrl} target="_blank" rel="noreferrer" className="absolute bottom-2 right-2"><Button variant="ghost" size="icon" className="h-6 w-6 text-white/60 hover:text-white bg-black/40 dark:bg-black/40"><Maximize2 className="h-3 w-3" /></Button></a>}
                      {isAlarm && <motion.div animate={{ opacity: [0.4, 0.8, 0.4] }} transition={{ duration: 1, repeat: Infinity }} className="absolute inset-0 flex items-center justify-center bg-red-900/30"><AlertTriangle className="h-10 w-10 text-red-400/80" /></motion.div>}
                    </>
                  ) : (
                    <div className="flex flex-col items-center gap-1"><VideoOff className="h-6 w-6 text-muted-foreground/40" /><span className="text-[9px] text-muted-foreground font-medium">OFFLINE</span></div>
                  )}
                </div>
                <div className="p-2.5 space-y-1.5">
                  <div className="flex items-center justify-between"><div><p className="text-xs font-medium leading-tight truncate">{stream.branchName}</p><p className="text-[10px] text-muted-foreground">{stream.branchCode} &bull; {stream.organization}</p></div><div className={cn('h-2 w-2 rounded-full', stream.status === 'online' ? 'bg-emerald-500' : 'bg-muted-foreground/40')} /></div>
                  {stream.currentUser && <div className="flex items-center justify-between rounded bg-muted/50 px-2 py-1"><span className="flex items-center gap-1.5 text-[10px]"><User className="h-3 w-3 text-blue-500 dark:text-blue-400" />{stream.currentUser}</span><span className="flex items-center gap-1 text-[10px] text-muted-foreground"><Clock className="h-2.5 w-2.5" />{stream.duration}</span></div>}
                  {stream.temperature > 0 && <div className="flex items-center gap-1 text-[10px] text-muted-foreground"><Thermometer className="h-3 w-3" />{stream.temperature}°C</div>}
                </div>
              </motion.div>
            );
          })}
        </div>
      )}

      {totalPages > 1 && (
        <div className="flex items-center justify-between pt-2">
          <p className="text-[10px] sm:text-xs text-muted-foreground">Menampilkan {(currentPage - 1) * ITEMS_PER_PAGE + 1}-{Math.min(currentPage * ITEMS_PER_PAGE, filteredStreams.length)} dari {filteredStreams.length} cabang</p>
          <div className="flex items-center gap-1.5"><Button variant="outline" size="icon" className="h-7 w-7 sm:h-8 sm:w-8 border-border/60" onClick={() => setCurrentPage((p) => Math.max(1, p - 1))} disabled={currentPage === 1}><ChevronLeft className="h-3.5 w-3.5" /></Button><span className="text-xs text-muted-foreground px-2">{currentPage}/{totalPages}</span><Button variant="outline" size="icon" className="h-7 w-7 sm:h-8 sm:w-8 border-border/60" onClick={() => setCurrentPage((p) => Math.min(totalPages, p + 1))} disabled={currentPage === totalPages}><ChevronRight className="h-3.5 w-3.5" /></Button></div>
        </div>
      )}
    </div>
  );
}
