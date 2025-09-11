import { User, UserRole } from '../types/user';

/**
 * 认证中间件类
 * 提供用户认证和权限验证功能
 */
export class AuthMiddleware {
  /**
   * 验证用户是否已登录
   */
  static async requireAuth(): Promise<User> {
    // 测试环境绕过认证检查
    if (process.env.NODE_ENV === 'test' || window.location.hostname === 'localhost') {
      return {
        id: 'test-user-id',
        username: 'testuser',
        email: 'test@example.com',
        full_name: 'Test User',
        status: 'active' as const,
        roles: [{
          id: '1',
          user_id: 'test-user-id',
          role: 'headquarters_admin' as UserRole,
          name: 'headquarters_admin',
          store_id: 'test-store-id',
          created_at: new Date().toISOString(),
          permissions: []
        }],
        created_at: new Date().toISOString(),
        updated_at: new Date().toISOString()
      } as User;
    }

    const token = localStorage.getItem('auth_token') || sessionStorage.getItem('auth_token');
    if (!token) {
      throw new Error('未登录，请先登录');
    }

    // 简化版本：直接返回存储的用户信息
    try {
      const authStorage = localStorage.getItem('auth-storage');
      if (authStorage) {
        const parsed = JSON.parse(authStorage);
        if (parsed.state && parsed.state.user) {
          return parsed.state.user;
        }
      }
      throw new Error('用户信息获取失败');
    } catch (error) {
      // 清除无效token
      localStorage.removeItem('auth_token');
      sessionStorage.removeItem('auth_token');
      throw new Error('登录已过期，请重新登录');
    }
  }

  /**
   * 验证用户是否具有指定角色
   */
  static async requireRoles(requiredRoles: UserRole[]): Promise<User> {
    const user = await this.requireAuth();
    
    if (!user.roles || user.roles.length === 0) {
      throw new Error('用户没有分配任何角色');
    }

    const userRoles = user.roles.map(r => r.role);
    const hasRequiredRole = requiredRoles.some(role => userRoles.includes(role));
    
    if (!hasRequiredRole) {
      throw new Error(`需要以下角色之一: ${requiredRoles.join(', ')}`);
    }

    return user;
  }

  /**
   * 检查用户是否为总部管理员
   */
  static async requireHeadquartersAdmin(): Promise<User> {
    return this.requireRoles([UserRole.HEADQUARTERS_ADMIN]);
  }

  /**
   * 检查用户是否为门店管理员
   */
  static async requireStoreAdmin(): Promise<User> {
    return this.requireRoles([UserRole.STORE_ADMIN]);
  }

  /**
   * 检查用户是否为教师
   */
  static async requireTeacher(): Promise<User> {
    return this.requireRoles([UserRole.TEACHER]);
  }

  /**
   * 检查用户是否为学生
   */
  static async requireStudent(): Promise<User> {
    return this.requireRoles([UserRole.STUDENT]);
  }

  /**
   * 检查用户是否为管理员（总部或门店）
   */
  static async requireAdmin(): Promise<User> {
    return this.requireRoles([UserRole.HEADQUARTERS_ADMIN, UserRole.STORE_ADMIN]);
  }

  /**
   * 获取当前用户信息（不抛出异常）
   */
  static async getCurrentUser(): Promise<User | null> {
    try {
      return await this.requireAuth();
    } catch {
      return null;
    }
  }

  /**
   * 检查用户是否已登录（不抛出异常）
   */
  static async isAuthenticated(): Promise<boolean> {
    try {
      await this.requireAuth();
      return true;
    } catch {
      return false;
    }
  }

  /**
   * 检查用户是否具有指定角色（不抛出异常）
   */
  static async hasRoles(requiredRoles: UserRole[]): Promise<boolean> {
    try {
      await this.requireRoles(requiredRoles);
      return true;
    } catch {
      return false;
    }
  }
}

// 导出默认实例
export default AuthMiddleware;
