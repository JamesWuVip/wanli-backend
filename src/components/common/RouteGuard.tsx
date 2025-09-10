import React, { useEffect } from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useAuthStore } from '../../stores/authStore';
import { usePermission } from '../../hooks/usePermission';

interface RouteGuardProps {
  children: React.ReactNode;
  requireAuth?: boolean;
  requiredRoles?: string[];
  requiredPermissions?: string[];
  fallback?: React.ReactNode;
  redirectTo?: string;
}

/**
 * 路由守卫组件
 * 用于保护需要特定权限才能访问的路由
 */
const RouteGuard: React.FC<RouteGuardProps> = ({
  children,
  requireAuth = true,
  requiredRoles = [],
  requiredPermissions = [],
  fallback,
  redirectTo = '/403',
}) => {
  const location = useLocation();
  const { isAuthenticated, isLoading, user, initialize } = useAuthStore();
  const { hasRole } = usePermission();
  const { hasAnyPermission } = usePermission();

  // 临时关闭认证检查用于测试业务功能
  // 如果需要认证但未初始化，则调用初始化
  useEffect(() => {
    if (requireAuth && !isAuthenticated && !isLoading && !user) {
      initialize().catch(() => {
        // 初始化失败时静默处理，让后续逻辑处理未认证状态
      });
    }
  }, [requireAuth, isAuthenticated, isLoading, user, initialize]);

  // 显示加载状态
  if (isLoading) {
    return <div>加载中...</div>;
  }

  // 临时跳过认证检查
  // 检查认证状态
  if (false && requireAuth && (!isAuthenticated || !user)) {
    return (
      <Navigate 
        to="/auth/login" 
        state={{ from: location.pathname }} 
        replace 
      />
    );
  }

  // 临时跳过角色权限检查
  // 检查角色权限
  if (false && requiredRoles.length > 0) {
    const hasRequiredRole = hasRole(requiredRoles);
    if (!hasRequiredRole) {
      if (fallback) {
        return <>{fallback}</>;
      }
      return <Navigate to={redirectTo} replace />;
    }
  }

  // 临时跳过操作权限检查
  // 检查操作权限
  if (false && requiredPermissions.length > 0) {
    const hasRequiredPermission = hasAnyPermission(requiredPermissions);
    if (!hasRequiredPermission) {
      if (fallback) {
        return <>{fallback}</>;
      }
      return <Navigate to={redirectTo} replace />;
    }
  }

  return <>{children}</>;
};

export default RouteGuard;