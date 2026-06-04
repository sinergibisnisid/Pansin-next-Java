'use client';

import { useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { useAuthStore } from '@/stores';
import { hasPermission, hasAnyPermission } from '@/lib/rbac';

interface ProtectedRouteProps {
  children: React.ReactNode;
  requiredPermission?: string;
  requiredPermissions?: string[];
  fallbackPath?: string;
}

/**
 * Protected route wrapper - checks authentication and permissions
 */
export function ProtectedRoute({
  children,
  requiredPermission,
  requiredPermissions,
  fallbackPath = '/login',
}: ProtectedRouteProps) {
  const router = useRouter();
  const { user, isAuthenticated, isLoading } = useAuthStore();

  useEffect(() => {
    if (isLoading) return;

    if (!isAuthenticated || !user) {
      router.replace(fallbackPath);
      return;
    }

    // Check single permission
    if (requiredPermission && !hasPermission(user, requiredPermission)) {
      router.replace('/dashboard'); // Redirect to dashboard if no permission
      return;
    }

    // Check multiple permissions (user needs at least one)
    if (requiredPermissions && !hasAnyPermission(user, requiredPermissions)) {
      router.replace('/dashboard');
      return;
    }
  }, [isAuthenticated, user, isLoading, requiredPermission, requiredPermissions, router, fallbackPath]);

  if (isLoading) {
    return (
      <div className="flex h-screen items-center justify-center">
        <div className="text-center">
          <div className="h-8 w-8 animate-spin rounded-full border-4 border-primary border-t-transparent mx-auto" />
          <p className="mt-4 text-sm text-muted-foreground">Loading...</p>
        </div>
      </div>
    );
  }

  if (!isAuthenticated || !user) {
    return null;
  }

  return <>{children}</>;
}
