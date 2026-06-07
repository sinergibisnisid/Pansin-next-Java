'use client';

import { useEffect, useState } from 'react';
import { Settings, User, Shield, Palette, Globe, Save, RefreshCw } from 'lucide-react';
import { useTheme } from 'next-themes';
import { toast } from 'sonner';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Button } from '@/components/ui/button';
import { Switch } from '@/components/ui/switch';
import { Separator } from '@/components/ui/separator';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { authService, settingsService } from '@/services';
import { cn } from '@/lib/utils';
import type { User as AppUser } from '@/types';

interface GeneralSettings {
  appName: string;
  organizationName: string;
  timezone: string;
  language: string;
}

interface SecuritySettings {
  sessionTimeoutMinutes: number;
  otpExpiryMinutes: number;
  maxLoginAttempts: number;
  passwordMinLength: number;
}

interface NotificationSettings {
  emailEnabled: boolean;
  whatsappEnabled: boolean;
  alarmNotification: boolean;
  maintenanceReminder: boolean;
}

interface SystemSettings {
  maintenanceMode: boolean;
  metricsRefreshSeconds: number;
  apiBaseUrl: string;
  websocketUrl: string;
}

const defaultGeneral: GeneralSettings = {
  appName: 'PANSIN ACCESS',
  organizationName: 'Bank BJB',
  timezone: 'Asia/Jakarta',
  language: 'id',
};

const defaultSecurity: SecuritySettings = {
  sessionTimeoutMinutes: 30,
  otpExpiryMinutes: 5,
  maxLoginAttempts: 5,
  passwordMinLength: 8,
};

const defaultNotification: NotificationSettings = {
  emailEnabled: true,
  whatsappEnabled: true,
  alarmNotification: true,
  maintenanceReminder: true,
};

const defaultSystem: SystemSettings = {
  maintenanceMode: false,
  metricsRefreshSeconds: 60,
  apiBaseUrl: '/api/v1',
  websocketUrl: '/ws',
};

function AppearanceSettings() {
  const { theme, setTheme } = useTheme();
  const [compactMode, setCompactMode] = useState(false);
  const [animations, setAnimations] = useState(true);
  const [realtimeIndicators, setRealtimeIndicators] = useState(true);

  return (
    <Card className="border-border/40 bg-card/50">
      <CardHeader className="pb-3">
        <CardTitle className="text-sm font-medium flex items-center gap-2">
          <Palette className="h-4 w-4 text-blue-400" />
          Appearance
        </CardTitle>
      </CardHeader>
      <CardContent className="space-y-4">
        <div className="space-y-3">
          <div className="flex items-center justify-between">
            <div><p className="text-sm font-medium">Dark Mode</p><p className="text-xs text-muted-foreground">Use dark theme</p></div>
            <Switch checked={theme === 'dark'} onCheckedChange={(checked) => setTheme(checked ? 'dark' : 'light')} />
          </div>
          <div className="flex items-center justify-between">
            <div><p className="text-sm font-medium">Compact Mode</p><p className="text-xs text-muted-foreground">Reduce spacing for dense display</p></div>
            <Switch checked={compactMode} onCheckedChange={setCompactMode} />
          </div>
          <div className="flex items-center justify-between">
            <div><p className="text-sm font-medium">Animations</p><p className="text-xs text-muted-foreground">Enable smooth transitions</p></div>
            <Switch checked={animations} onCheckedChange={setAnimations} />
          </div>
          <div className="flex items-center justify-between">
            <div><p className="text-sm font-medium">Realtime Indicators</p><p className="text-xs text-muted-foreground">Show live data pulse indicators</p></div>
            <Switch checked={realtimeIndicators} onCheckedChange={setRealtimeIndicators} />
          </div>
        </div>
      </CardContent>
    </Card>
  );
}

export default function SettingsPage() {
  const [general, setGeneral] = useState<GeneralSettings>(defaultGeneral);
  const [security, setSecurity] = useState<SecuritySettings>(defaultSecurity);
  const [notification, setNotification] = useState<NotificationSettings>(defaultNotification);
  const [system, setSystem] = useState<SystemSettings>(defaultSystem);
  const [profile, setProfile] = useState<AppUser | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isSaving, setIsSaving] = useState<string | null>(null);

  const loadSettings = async () => {
    setIsLoading(true);
    try {
      const [settings, user] = await Promise.all([
        settingsService.getAll(),
        authService.getProfile().catch(() => null),
      ]);
      const byKey = Object.fromEntries(settings.map((item) => [item.key, item.value]));
      setGeneral({ ...defaultGeneral, ...(byKey['app.general'] as Partial<GeneralSettings> | undefined) });
      setSecurity({ ...defaultSecurity, ...(byKey['app.security'] as Partial<SecuritySettings> | undefined) });
      setNotification({ ...defaultNotification, ...(byKey['app.notification'] as Partial<NotificationSettings> | undefined) });
      setSystem({ ...defaultSystem, ...(byKey['app.system'] as Partial<SystemSettings> | undefined) });
      setProfile(user);
    } catch (error) {
      console.error('Failed to load settings:', error);
      toast.error('Gagal memuat settings dari backend');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    void Promise.resolve().then(loadSettings);
  }, []);

  const saveSetting = async (key: string, value: Record<string, unknown>, publicSetting?: boolean) => {
    setIsSaving(key);
    try {
      await settingsService.update(key, value, { publicSetting });
      toast.success('Settings berhasil disimpan');
    } catch (error) {
      console.error('Failed to save setting:', error);
      toast.error('Gagal menyimpan settings');
    } finally {
      setIsSaving(null);
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
        <div>
          <h1 className="text-xl sm:text-2xl font-bold tracking-tight">Settings</h1>
          <p className="text-xs sm:text-sm text-muted-foreground mt-1">System configuration loaded from backend</p>
        </div>
        <Button variant="outline" size="sm" onClick={loadSettings} disabled={isLoading} className="gap-1.5">
          <RefreshCw className={cn('h-3.5 w-3.5', isLoading && 'animate-spin')} />
          Refresh
        </Button>
      </div>

      <Tabs defaultValue="general" className="space-y-4 sm:space-y-6">
        <TabsList className="bg-muted/30 border border-border/40 w-full sm:w-auto grid grid-cols-4 sm:flex">
          <TabsTrigger value="general" className="gap-1.5 sm:gap-2 text-[10px] sm:text-xs px-2 sm:px-3"><Settings className="h-3 w-3 sm:h-3.5 sm:w-3.5" /><span className="hidden sm:inline">General</span></TabsTrigger>
          <TabsTrigger value="profile" className="gap-1.5 sm:gap-2 text-[10px] sm:text-xs px-2 sm:px-3"><User className="h-3 w-3 sm:h-3.5 sm:w-3.5" /><span className="hidden sm:inline">Profile</span></TabsTrigger>
          <TabsTrigger value="security" className="gap-1.5 sm:gap-2 text-[10px] sm:text-xs px-2 sm:px-3"><Shield className="h-3 w-3 sm:h-3.5 sm:w-3.5" /><span className="hidden sm:inline">Security</span></TabsTrigger>
          <TabsTrigger value="appearance" className="gap-1.5 sm:gap-2 text-[10px] sm:text-xs px-2 sm:px-3"><Palette className="h-3 w-3 sm:h-3.5 sm:w-3.5" /><span className="hidden sm:inline">Theme</span></TabsTrigger>
        </TabsList>

        <TabsContent value="general" className="space-y-4">
          <Card className="border-border/40 bg-card/50">
            <CardHeader className="pb-3"><CardTitle className="text-sm font-medium flex items-center gap-2"><Globe className="h-4 w-4 text-blue-400" />System Configuration</CardTitle></CardHeader>
            <CardContent className="space-y-4">
              <div className="grid gap-4 sm:grid-cols-2">
                <div className="space-y-1.5"><label className="text-xs text-muted-foreground">System Name</label><Input value={general.appName} onChange={(e) => setGeneral({ ...general, appName: e.target.value })} className="bg-background/50 border-border/40" /></div>
                <div className="space-y-1.5"><label className="text-xs text-muted-foreground">Organization</label><Input value={general.organizationName} onChange={(e) => setGeneral({ ...general, organizationName: e.target.value })} className="bg-background/50 border-border/40" /></div>
                <div className="space-y-1.5"><label className="text-xs text-muted-foreground">Timezone</label><Input value={general.timezone} onChange={(e) => setGeneral({ ...general, timezone: e.target.value })} className="bg-background/50 border-border/40" /></div>
                <div className="space-y-1.5"><label className="text-xs text-muted-foreground">Language</label><Input value={general.language} onChange={(e) => setGeneral({ ...general, language: e.target.value })} className="bg-background/50 border-border/40" /></div>
              </div>
              <Separator className="opacity-40" />
              <div className="grid gap-4 sm:grid-cols-2">
                <div className="space-y-1.5"><label className="text-xs text-muted-foreground">API Base URL</label><Input value={system.apiBaseUrl} onChange={(e) => setSystem({ ...system, apiBaseUrl: e.target.value })} className="bg-background/50 border-border/40 font-mono text-xs" /></div>
                <div className="space-y-1.5"><label className="text-xs text-muted-foreground">WebSocket URL</label><Input value={system.websocketUrl} onChange={(e) => setSystem({ ...system, websocketUrl: e.target.value })} className="bg-background/50 border-border/40 font-mono text-xs" /></div>
              </div>
              <div className="flex flex-wrap gap-2">
                <Button onClick={() => saveSetting('app.general', general as unknown as Record<string, unknown>, true)} disabled={isSaving === 'app.general'} className="gap-1.5"><Save className="h-3.5 w-3.5" />Save General</Button>
                <Button variant="outline" onClick={() => saveSetting('app.system', system as unknown as Record<string, unknown>, false)} disabled={isSaving === 'app.system'} className="gap-1.5"><Save className="h-3.5 w-3.5" />Save System</Button>
              </div>
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="profile" className="space-y-4">
          <Card className="border-border/40 bg-card/50">
            <CardHeader className="pb-3"><CardTitle className="text-sm font-medium flex items-center gap-2"><User className="h-4 w-4 text-blue-400" />Profile Information</CardTitle></CardHeader>
            <CardContent className="space-y-4">
              <div className="grid gap-4 sm:grid-cols-2">
                <div className="space-y-1.5"><label className="text-xs text-muted-foreground">Full Name</label><Input value={profile?.fullName ?? ''} readOnly className="bg-background/50 border-border/40" /></div>
                <div className="space-y-1.5"><label className="text-xs text-muted-foreground">Username</label><Input value={profile?.username ?? ''} readOnly className="bg-background/50 border-border/40" /></div>
                <div className="space-y-1.5"><label className="text-xs text-muted-foreground">Email</label><Input value={profile?.email ?? ''} readOnly className="bg-background/50 border-border/40" /></div>
                <div className="space-y-1.5"><label className="text-xs text-muted-foreground">Branch ID</label><Input value={profile?.branchId ?? '-'} readOnly className="bg-background/50 border-border/40" /></div>
              </div>
              <p className="text-xs text-muted-foreground">Profile data is read from `GET /auth/profile`.</p>
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="security" className="space-y-4">
          <Card className="border-border/40 bg-card/50">
            <CardHeader className="pb-3"><CardTitle className="text-sm font-medium flex items-center gap-2"><Shield className="h-4 w-4 text-blue-400" />Security & Notifications</CardTitle></CardHeader>
            <CardContent className="space-y-4">
              <div className="grid gap-4 sm:grid-cols-2">
                <div className="space-y-1.5"><label className="text-xs text-muted-foreground">Session Timeout (minutes)</label><Input type="number" value={security.sessionTimeoutMinutes} onChange={(e) => setSecurity({ ...security, sessionTimeoutMinutes: Number(e.target.value) })} className="bg-background/50 border-border/40" /></div>
                <div className="space-y-1.5"><label className="text-xs text-muted-foreground">OTP Expiry (minutes)</label><Input type="number" value={security.otpExpiryMinutes} onChange={(e) => setSecurity({ ...security, otpExpiryMinutes: Number(e.target.value) })} className="bg-background/50 border-border/40" /></div>
                <div className="space-y-1.5"><label className="text-xs text-muted-foreground">Max Login Attempts</label><Input type="number" value={security.maxLoginAttempts} onChange={(e) => setSecurity({ ...security, maxLoginAttempts: Number(e.target.value) })} className="bg-background/50 border-border/40" /></div>
                <div className="space-y-1.5"><label className="text-xs text-muted-foreground">Password Min Length</label><Input type="number" value={security.passwordMinLength} onChange={(e) => setSecurity({ ...security, passwordMinLength: Number(e.target.value) })} className="bg-background/50 border-border/40" /></div>
              </div>
              <Separator className="opacity-40" />
              <div className="space-y-3">
                {[
                  ['emailEnabled', 'Email Notification'],
                  ['whatsappEnabled', 'WhatsApp Notification'],
                  ['alarmNotification', 'Alarm Notification'],
                  ['maintenanceReminder', 'Maintenance Reminder'],
                ].map(([key, label]) => (
                  <div key={key} className="flex items-center justify-between"><div><p className="text-sm font-medium">{label}</p><p className="text-xs text-muted-foreground">Persisted in app.notification</p></div><Switch checked={Boolean(notification[key as keyof NotificationSettings])} onCheckedChange={(checked) => setNotification({ ...notification, [key]: checked })} /></div>
                ))}
              </div>
              <div className="flex flex-wrap gap-2">
                <Button onClick={() => saveSetting('app.security', security as unknown as Record<string, unknown>, false)} disabled={isSaving === 'app.security'} className="gap-1.5"><Save className="h-3.5 w-3.5" />Save Security</Button>
                <Button variant="outline" onClick={() => saveSetting('app.notification', notification as unknown as Record<string, unknown>, false)} disabled={isSaving === 'app.notification'} className="gap-1.5"><Save className="h-3.5 w-3.5" />Save Notification</Button>
              </div>
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="appearance" className="space-y-4"><AppearanceSettings /></TabsContent>
      </Tabs>
    </div>
  );
}
