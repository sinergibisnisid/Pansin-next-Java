import type { User } from '@/types';

/**
 * Role hierarchy and permission checking utilities
 */

export const ROLES = {
  SUPER_ADMIN: 'SUPER_ADMIN',
  ADMIN_PUSAT: 'ADMIN_PUSAT',
  ADMIN_CABANG: 'ADMIN_CABANG',
  OPERATOR: 'OPERATOR',
  SECURITY: 'SECURITY',
  MAINTENANCE: 'MAINTENANCE',
  VIEWER: 'VIEWER',
  VIEWER_CCTV: 'VIEWER_CCTV',
} as const;

export type RoleType = keyof typeof ROLES;

/**
 * Check if user has specific permission
 */
export function hasPermission(user: User | null, permission: string): boolean {
  if (!user) return false;
  if (user.role === ROLES.SUPER_ADMIN) return true;
  return user.permissions?.includes(permission) ?? false;
}

/**
 * Check if user has any of the specified permissions
 */
export function hasAnyPermission(user: User | null, permissions: string[]): boolean {
  if (!user) return false;
  if (user.role === ROLES.SUPER_ADMIN) return true;
  return permissions.some(p => user.permissions?.includes(p));
}

/**
 * Check if user has all of the specified permissions
 */
export function hasAllPermissions(user: User | null, permissions: string[]): boolean {
  if (!user) return false;
  if (user.role === ROLES.SUPER_ADMIN) return true;
  return permissions.every(p => user.permissions?.includes(p));
}

/**
 * Check if user has specific role
 */
export function hasRole(user: User | null, role: RoleType): boolean {
  if (!user) return false;
  return user.role === role;
}

/**
 * Check if user has any of the specified roles
 */
export function hasAnyRole(user: User | null, roles: RoleType[]): boolean {
  if (!user) return false;
  return roles.includes(user.role as RoleType);
}

/**
 * Get redirect path based on user role after login
 */
export function getDefaultRedirectPath(user: User | null): string {
  if (!user) return '/login';
  
  switch (user.role) {
    case ROLES.VIEWER_CCTV:
      return '/monitoring'; // Redirect CCTV viewers directly to monitoring page
    case ROLES.SUPER_ADMIN:
    case ROLES.ADMIN_PUSAT:
    case ROLES.ADMIN_CABANG:
      return '/dashboard';
    default:
      return '/dashboard';
  }
}

/**
 * Check if user can access branch data
 */
export function canAccessBranch(user: User | null, branchId: string): boolean {
  if (!user) return false;
  if (user.role === ROLES.SUPER_ADMIN || user.role === ROLES.ADMIN_PUSAT) return true;
  return user.branchId === branchId;
}

/**
 * Check if user can access all branches
 */
export function canAccessAllBranches(user: User | null): boolean {
  if (!user) return false;
  return user.role === ROLES.SUPER_ADMIN || user.role === ROLES.ADMIN_PUSAT;
}
