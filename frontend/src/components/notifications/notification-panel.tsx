'use client';

import { useNotificationStore } from '@/stores';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { ScrollArea } from '@/components/ui/scroll-area';
import { X, Check, CheckCheck, Trash2, Bell } from 'lucide-react';
import { formatDate } from '@/lib/utils';
import { motion, AnimatePresence } from 'framer-motion';
import type { Notification } from '@/types';

export function NotificationPanel() {
  const { notifications, unreadCount, isOpen, setOpen, markAsRead, markAllAsRead, removeNotification, clearAll } = useNotificationStore();

  if (!isOpen) return null;

  const getTypeColor = (type: Notification['type']) => {
    switch (type) {
      case 'error': return 'bg-red-500/10 border-red-500/20 text-red-500';
      case 'warning': return 'bg-yellow-500/10 border-yellow-500/20 text-yellow-500';
      case 'success': return 'bg-emerald-500/10 border-emerald-500/20 text-emerald-500';
      default: return 'bg-blue-500/10 border-blue-500/20 text-blue-500';
    }
  };

  return (
    <AnimatePresence>
      <motion.div
        initial={{ opacity: 0 }}
        animate={{ opacity: 1 }}
        exit={{ opacity: 0 }}
        className="fixed inset-0 z-50 bg-background/80 backdrop-blur-sm"
        onClick={() => setOpen(false)}
      >
        <motion.div
          initial={{ x: 400 }}
          animate={{ x: 0 }}
          exit={{ x: 400 }}
          transition={{ type: 'spring', damping: 25, stiffness: 200 }}
          className="fixed right-0 top-0 h-full w-full sm:w-96 border-l border-border bg-background shadow-xl"
          onClick={(e) => e.stopPropagation()}
        >
          {/* Header */}
          <div className="flex items-center justify-between border-b border-border p-4">
            <div className="flex items-center gap-2">
              <Bell className="h-5 w-5" />
              <h2 className="text-lg font-semibold">Notifications</h2>
              {unreadCount > 0 && (
                <Badge variant="secondary" className="h-5 px-1.5 text-xs">
                  {unreadCount}
                </Badge>
              )}
            </div>
            <Button variant="ghost" size="icon" onClick={() => setOpen(false)}>
              <X className="h-5 w-5" />
            </Button>
          </div>

          {/* Actions */}
          {notifications.length > 0 && (
            <div className="flex items-center justify-between border-b border-border p-2 gap-2">
              <Button
                variant="ghost"
                size="sm"
                onClick={markAllAsRead}
                disabled={unreadCount === 0}
              >
                <CheckCheck className="h-4 w-4 mr-2" />
                Mark all read
              </Button>
              <Button variant="ghost" size="sm" onClick={clearAll}>
                <Trash2 className="h-4 w-4 mr-2" />
                Clear all
              </Button>
            </div>
          )}

          {/* List */}
          <ScrollArea className="h-[calc(100vh-120px)]">
            {notifications.length === 0 ? (
              <div className="flex flex-col items-center justify-center h-64 text-muted-foreground">
                <Bell className="h-12 w-12 mb-4 opacity-20" />
                <p className="text-sm">No notifications</p>
              </div>
            ) : (
              <div className="p-2 space-y-2">
                {notifications.map((notification) => (
                  <motion.div
                    key={notification.id}
                    layout
                    initial={{ opacity: 0, y: -10 }}
                    animate={{ opacity: 1, y: 0 }}
                    exit={{ opacity: 0, x: 100 }}
                    className={`relative rounded-lg border p-3 ${
                      notification.read ? 'bg-muted/30 opacity-60' : 'bg-card'
                    } ${getTypeColor(notification.type)}`}
                  >
                    <div className="flex items-start justify-between gap-2">
                      <div className="flex-1 space-y-1">
                        <p className="text-sm font-medium leading-tight">
                          {notification.title}
                        </p>
                        <p className="text-xs text-muted-foreground">
                          {notification.message}
                        </p>
                        <p className="text-xs text-muted-foreground">
                          {formatDate(notification.timestamp, 'relative')}
                        </p>
                      </div>
                      <div className="flex gap-1">
                        {!notification.read && (
                          <Button
                            variant="ghost"
                            size="icon"
                            className="h-6 w-6"
                            onClick={() => markAsRead(notification.id)}
                          >
                            <Check className="h-3 w-3" />
                          </Button>
                        )}
                        <Button
                          variant="ghost"
                          size="icon"
                          className="h-6 w-6"
                          onClick={() => removeNotification(notification.id)}
                        >
                          <X className="h-3 w-3" />
                        </Button>
                      </div>
                    </div>
                  </motion.div>
                ))}
              </div>
            )}
          </ScrollArea>
        </motion.div>
      </motion.div>
    </AnimatePresence>
  );
}
