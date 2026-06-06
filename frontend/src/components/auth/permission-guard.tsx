'use client';

import { useAuthStore } from '@/stores';
import { hasPermission, hasAnyPermission, hasRole } from '@/lib/rbac';
import type { RoleType } from '@/lib/rbac';

interface PermissionGuardProps {
  children: React.ReactNode;
  permission?: string;
  permissions?: string[];
  role?: RoleType;
  roles?: RoleType[];
  fallback?: React.ReactNode;
}

/**
 * Conditionally render children based on user permissions or roles
 */
export function PermissionGuard({
  children,
  permission,
  permissions,
  role,
  roles,
  fallback = null,
}: PermissionGuardProps) {
  const { user } = useAuthStore();

  if (!user) return <>{fallback}</>;

  // Check single permission
  if (permission && !hasPermission(user, permission)) {
    return <>{fallback}</>;
  }

  // Check multiple permissions (user needs at least one)
  if (permissions && !hasAnyPermission(user, permissions)) {
    return <>{fallback}</>;
  }

  // Check single role
  if (role && !hasRole(user, role)) {
    return <>{fallback}</>;
  }

  // Check multiple roles
  if (roles && !roles.some(r => user.roles?.includes(r))) {
    return <>{fallback}</>;
  }

  return <>{children}</>;
}
