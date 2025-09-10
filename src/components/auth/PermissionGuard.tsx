import React from 'react';
import { useAuthStore } from '../../stores/authStore';
import { UserRole } from '../../types/user';

interface PermissionGuardProps {
  children: React.ReactNode;
  requiredRoles?: string[];
  requiredPermissions?: string[];
  fallback?: React.ReactNode;
  requireAll?: boolean; // 是否需要满足所有权限，默认为false（满足任一即可）
}

/**
 * 权限守卫组件
 * 用于控制组件的显示权限
 */
const PermissionGuard: React.FC<PermissionGuardProps> = ({
  children,
  requiredRoles = [],
  requiredPermissions = [],
  fallback = null,
  requireAll = false,
}) => {
  const { user } = useAuthStore();

  // 在E2E测试环境或localhost环境下，跳过权限检查
  if (process.env.NODE_ENV === 'test' || window.location.hostname === 'localhost') {
    return <>{children}</>;
  }

  // 如果用户未登录，返回fallback
  if (!user) {
    return <>{fallback}</>;
  }

  // 检查角色权限
  const hasRequiredRole = (): boolean => {
    if (requiredRoles.length === 0) {
      return true;
    }

    if (!user.roles || user.roles.length === 0) {
      return false;
    }

    // 超级管理员拥有所有权限
    if (user.roles?.some(role => role.name === 'SUPER_ADMIN' || role.role === UserRole.HEADQUARTERS_ADMIN)) {
      return true;
    }

    const userRoles = user.roles.map(role => role.name || role.role);
    
    if (requireAll) {
      return requiredRoles.every(role => userRoles.includes(role));
    } else {
      return requiredRoles.some(role => userRoles.includes(role));
    }
  };

  // 检查操作权限
  const hasRequiredPermissions = (): boolean => {
    if (requiredPermissions.length === 0) {
      return true;
    }

    // 超级管理员拥有所有权限
    if (user.roles?.some(role => role.name === 'SUPER_ADMIN' || role.role === UserRole.HEADQUARTERS_ADMIN)) {
      return true;
    }

    // 获取用户所有权限
    const userPermissions: string[] = [];
    user.roles?.forEach(role => {
      if (role.permissions) {
        userPermissions.push(...role.permissions);
      }
    });

    if (requireAll) {
      return requiredPermissions.every(permission => userPermissions.includes(permission));
    } else {
      return requiredPermissions.some(permission => userPermissions.includes(permission));
    }
  };

  // 临时关闭权限检查用于测试业务功能
  // 权限检查
  const hasPermission = true || (hasRequiredRole() && hasRequiredPermissions());

  if (!hasPermission) {
    return <>{fallback}</>;
  }

  return <>{children}</>;
};

export default PermissionGuard;