'use client';

import { type ReactNode, useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { motion } from 'framer-motion';
import { Loader2 } from 'lucide-react';
import { Sidebar } from './sidebar';
import { Topbar } from './topbar';
import { NotificationPanel } from '@/components/notifications/notification-panel';
import { useAuthStore, useSidebarStore } from '@/stores';
import { useHydration } from '@/hooks';
import { websocketService } from '@/services/websocket-service';

interface DashboardLayoutProps {
  children: ReactNode;
}

export function DashboardLayout({ children }: DashboardLayoutProps) {
  const router = useRouter();
  const { isCollapsed } = useSidebarStore();
  const { isAuthenticated } = useAuthStore();
  const hydrated = useHydration();
  const [isMobile, setIsMobile] = useState(false);

  useEffect(() => {
    const checkMobile = () => setIsMobile(window.innerWidth < 1024);
    checkMobile();
    window.addEventListener('resize', checkMobile);
    return () => window.removeEventListener('resize', checkMobile);
  }, []);

  // Auth guard: redirect to login if not authenticated after hydration
  useEffect(() => {
    if (hydrated && !isAuthenticated) {
      router.replace('/login');
    }
  }, [hydrated, isAuthenticated, router]);

  // Connect WebSocket on mount (only if authenticated)
  useEffect(() => {
    if (hydrated && isAuthenticated) {
      websocketService.connect();
      return () => websocketService.disconnect();
    }
  }, [hydrated, isAuthenticated]);

  // Show loading spinner while checking auth
  if (!hydrated || !isAuthenticated) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-background">
        <div className="flex flex-col items-center gap-3">
          <Loader2 className="h-8 w-8 animate-spin text-blue-500" />
          <p className="text-sm text-muted-foreground">Loading...</p>
        </div>
      </div>
    );
  }

  // Prevent layout shift during hydration
  const sidebarWidth = isCollapsed ? 72 : 260;

  return (
    <div className="min-h-screen bg-background">
      {/* Sidebar */}
      <Sidebar />

      {/* Main Content */}
      <motion.div
        initial={false}
        animate={{ marginLeft: isMobile ? 0 : sidebarWidth }}
        transition={{ duration: 0.2, ease: 'easeInOut' }}
        className="flex min-h-screen flex-col"
      >
        {/* Topbar */}
        <Topbar />

        {/* Page Content */}
        <main className="flex-1 p-3 sm:p-4 lg:p-6">
          <motion.div
            initial={{ opacity: 0, y: 10 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.3 }}
          >
            {children}
          </motion.div>
        </main>
      </motion.div>

      {/* Notification Panel */}
      <NotificationPanel />
    </div>
  );
}
