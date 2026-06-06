'use client';

import { useState, useEffect } from 'react';
import { motion } from 'framer-motion';
import {
  Wifi,
  WifiOff,
  Send,
  RefreshCw,
  CheckCircle,
  XCircle,
  Loader2,
} from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Badge } from '@/components/ui/badge';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { cn } from '@/lib/utils';
import { mqttService } from '@/services/mqtt-service';
import type { MqttStatus } from '@/services/mqtt-service';

export default function MQTTPage() {
  const [broker, setBroker] = useState<MqttStatus | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [testTopic, setTestTopic] = useState('');
  const [testMessage, setTestMessage] = useState('');
  const [connectionTest, setConnectionTest] = useState<'idle' | 'testing' | 'success' | 'error'>('idle');

  const fetchStatus = async () => {
    setIsLoading(true);
    try {
      const data = await mqttService.getStatus();
      setBroker(data);
    } catch (error) {
      console.error('Failed to fetch MQTT status:', error);
      setBroker(null);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchStatus();
  }, []);

  const handleConnectionTest = async () => {
    setConnectionTest('testing');
    try {
      await mqttService.getStatus();
      setConnectionTest('success');
      setTimeout(() => setConnectionTest('idle'), 3000);
    } catch {
      setConnectionTest('error');
      setTimeout(() => setConnectionTest('idle'), 3000);
    }
  };

  const isConnected = broker?.connected ?? false;
  const brokerHost = broker?.brokerUrl?.replace(/^(tcp|ssl|mqtt|mqtts):?\/\//, '') ?? '-';
  const protocol = broker?.useTls ? 'MQTTS' : 'MQTT';

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold tracking-tight">MQTT Management</h1>
          <p className="text-sm text-muted-foreground mt-1">
            Manage MQTT broker connections and topics
          </p>
        </div>
        <Button variant="outline" size="sm" className="gap-2" onClick={handleConnectionTest} disabled={isLoading}>
          <RefreshCw className={cn('h-4 w-4', (isLoading || connectionTest === 'testing') && 'animate-spin')} />
          Test Connection
        </Button>
      </div>

      {/* Loading state */}
      {isLoading && (
        <div className="flex items-center justify-center py-12 text-muted-foreground gap-2">
          <Loader2 className="h-5 w-5 animate-spin" />
          <span className="text-sm">Loading broker status...</span>
        </div>
      )}

      {/* Broker Status */}
      {!isLoading && (
        <motion.div
          initial={{ opacity: 0, y: 10 }}
          animate={{ opacity: 1, y: 0 }}
          className="rounded-xl border border-border/40 bg-card/50 p-6"
        >
          <div className="flex items-start justify-between">
            <div className="flex items-center gap-4">
              <div className={cn(
                'flex h-12 w-12 items-center justify-center rounded-xl',
                isConnected
                  ? 'bg-emerald-500/20 border border-emerald-500/30'
                  : 'bg-red-500/20 border border-red-500/30'
              )}>
                {isConnected ? (
                  <Wifi className="h-6 w-6 text-emerald-400" />
                ) : (
                  <WifiOff className="h-6 w-6 text-red-400" />
                )}
              </div>
              <div>
                <h3 className="text-lg font-semibold">PANSIN MQTT Broker</h3>
                <p className="text-sm text-muted-foreground mt-0.5">{brokerHost}</p>
              </div>
            </div>
            <Badge
              variant="outline"
              className={cn(
                isConnected
                  ? 'bg-emerald-500/20 text-emerald-400 border-emerald-500/30'
                  : 'bg-red-500/20 text-red-400 border-red-500/30'
              )}
            >
              <span className="relative flex h-2 w-2 mr-1.5">
                {isConnected && (
                  <span className="absolute inline-flex h-full w-full animate-ping rounded-full bg-current opacity-75" />
                )}
                <span className="relative inline-flex h-2 w-2 rounded-full bg-current" />
              </span>
              {isConnected ? 'Connected' : 'Disconnected'}
            </Badge>
          </div>

          <div className="mt-4 grid grid-cols-2 sm:grid-cols-4 gap-4">
            <div className="rounded-lg bg-muted/30 p-3">
              <p className="text-[10px] text-muted-foreground uppercase tracking-wider">Protocol</p>
              <p className="text-sm font-medium mt-1">{protocol}</p>
            </div>
            <div className="rounded-lg bg-muted/30 p-3">
              <p className="text-[10px] text-muted-foreground uppercase tracking-wider">Topics</p>
              <p className="text-sm font-medium mt-1">{broker?.topics?.length ?? 0}</p>
            </div>
            <div className="rounded-lg bg-muted/30 p-3">
              <p className="text-[10px] text-muted-foreground uppercase tracking-wider">Username</p>
              <p className="text-sm font-medium mt-1">{broker?.username || '-'}</p>
            </div>
            <div className="rounded-lg bg-muted/30 p-3">
              <p className="text-[10px] text-muted-foreground uppercase tracking-wider">Keep Alive</p>
              <p className="text-sm font-medium mt-1">{broker?.keepAlive ?? '-'}s</p>
            </div>
          </div>

          {/* Connection Test Result */}
          {connectionTest === 'success' && (
            <motion.div
              initial={{ opacity: 0, y: -10 }}
              animate={{ opacity: 1, y: 0 }}
              className="mt-4 flex items-center gap-2 rounded-lg bg-emerald-500/10 border border-emerald-500/20 px-4 py-2"
            >
              <CheckCircle className="h-4 w-4 text-emerald-400" />
              <span className="text-sm text-emerald-400">Connection test successful</span>
            </motion.div>
          )}
          {connectionTest === 'error' && (
            <motion.div
              initial={{ opacity: 0, y: -10 }}
              animate={{ opacity: 1, y: 0 }}
              className="mt-4 flex items-center gap-2 rounded-lg bg-red-500/10 border border-red-500/20 px-4 py-2"
            >
              <XCircle className="h-4 w-4 text-red-400" />
              <span className="text-sm text-red-400">Connection test failed</span>
            </motion.div>
          )}
        </motion.div>
      )}

      {/* Topics & Publish */}
      {!isLoading && (
        <div className="grid gap-6 lg:grid-cols-3">
          {/* Topic List */}
          <div className="lg:col-span-2">
            <Card className="border-border/40 bg-card/50">
              <CardHeader className="flex flex-row items-center justify-between pb-3">
                <CardTitle className="text-sm font-medium">Subscribed Topics</CardTitle>
              </CardHeader>
              <CardContent className="space-y-2">
                {!broker?.topics?.length && (
                  <p className="text-sm text-muted-foreground text-center py-4">No topics configured.</p>
                )}
                {broker?.topics?.map((topic, i) => (
                  <div
                    key={i}
                    className="flex items-start justify-between rounded-lg border border-border/30 bg-muted/20 p-3 hover:bg-muted/30 transition-colors"
                  >
                    <div className="space-y-1">
                      <div className="flex items-center gap-2">
                        <code className="text-xs font-mono text-blue-400">{topic.topic}</code>
                        <Badge variant="outline" className="text-[9px] h-4">
                          QoS {topic.qos}
                        </Badge>
                      </div>
                      <p className="text-[11px] text-muted-foreground">{topic.description}</p>
                    </div>
                  </div>
                ))}
              </CardContent>
            </Card>
          </div>

          {/* Publish Test */}
          <div>
            <Card className="border-border/40 bg-card/50">
              <CardHeader className="pb-3">
                <CardTitle className="text-sm font-medium">Publish Message</CardTitle>
              </CardHeader>
              <CardContent className="space-y-3">
                <div className="space-y-1.5">
                  <label className="text-xs text-muted-foreground">Topic</label>
                  <Input
                    placeholder="vault/bdg01/test"
                    value={testTopic}
                    onChange={(e) => setTestTopic(e.target.value)}
                    className="bg-background/50 border-border/40 font-mono text-xs"
                  />
                </div>
                <div className="space-y-1.5">
                  <label className="text-xs text-muted-foreground">Message (JSON)</label>
                  <textarea
                    placeholder='{"key": "value"}'
                    value={testMessage}
                    onChange={(e) => setTestMessage(e.target.value)}
                    className="w-full h-24 rounded-md border border-border/40 bg-background/50 px-3 py-2 text-xs font-mono resize-none focus:outline-none focus:ring-2 focus:ring-ring/20"
                  />
                </div>
                <Button className="w-full gap-2" size="sm">
                  <Send className="h-3.5 w-3.5" />
                  Publish
                </Button>
              </CardContent>
            </Card>
          </div>
        </div>
      )}
    </div>
  );
}
