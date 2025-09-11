/**
 * 用户服务类
 * 处理用户相关的数据库操作和业务逻辑
 */
import { supabase, TABLES, UserStatus, UserRole } from '../config/supabase.js';
import { hashPassword, verifyPassword, validatePasswordStrength } from '../utils/password.js';
import { generateAccessToken, generateRefreshToken, JwtPayload } from '../utils/jwt.js';

// 用户数据接口
export interface User {
  id: string;
  username: string;
  email: string;
  full_name: string;
  phone?: string;
  status: UserStatus;
  created_at: string;
  updated_at: string;
  deleted_at?: string;
}

// 用户角色数据接口
export interface UserRoleData {
  id: string;
  user_id: string;
  role: UserRole;
  store_id?: string;
  created_at: string;
}

// 创建用户请求接口
export interface CreateUserRequest {
  username: string;
  email: string;
  password: string;
  full_name: string;
  phone?: string;
  roles?: UserRole[];
}

// 用户登录请求接口
export interface LoginRequest {
  username: string;
  password: string;
}

// 登录响应接口
export interface LoginResponse {
  user: Omit<User, 'password_hash'>;
  roles: Array<{
    id: string;
    userId: string;
    role: UserRole;
    name: string;
    storeId?: string;
    createdAt: string;
    permissions?: string[];
  }>;
  accessToken: string;
  refreshToken: string;
}

// 用户查询参数接口
export interface UserQueryParams {
  page?: number;
  pageSize?: number;
  status?: UserStatus;
  role?: UserRole;
  username?: string;
  storeId?: string;
}

// 更新用户请求接口
export interface UpdateUserRequest {
  full_name?: string;
  phone?: string;
  status?: UserStatus;
  roles?: UserRole[];
}

// 用户角色信息接口
export interface UserRoleInfo {
  role: UserRole;
  name: string;
  createdAt: string;
  permissions: string[];
}

/**
 * 用户服务类
 */
export class UserService {
  
  /**
   * 创建新用户
   * @param userData 用户数据
   * @returns 创建的用户信息
   */
  async createUser(request: CreateUserRequest): Promise<Omit<User, 'password_hash'>> {
    // 密码强度校验
    if (!validatePasswordStrength(request.password)) {
      throw new Error('密码强度不足，请使用至少8位包含大小写字母、数字和特殊字符的密码');
    }

    // 检查用户名是否已存在
    const { data: existingUser } = await supabase
      .from(TABLES.USERS)
      .select('id')
      .eq('username', request.username)
      .is('deleted_at', null)
      .single();

    if (existingUser) {
      throw new Error('用户名已存在');
    }

    // 检查邮箱是否已存在
    const { data: existingEmail } = await supabase
      .from(TABLES.USERS)
      .select('id')
      .eq('email', request.email)
      .is('deleted_at', null)
      .single();

    if (existingEmail) {
      throw new Error('邮箱已存在');
    }

    // 加密密码
    const passwordHash = await hashPassword(request.password);

    // 创建用户
    const { data: user, error: userError } = await supabase
      .from(TABLES.USERS)
      .insert({
        username: request.username,
        email: request.email,
        password_hash: passwordHash,
        full_name: request.full_name,
        phone: request.phone,
        status: UserStatus.ACTIVE,
        created_at: new Date().toISOString(),
        updated_at: new Date().toISOString()
      })
      .select('id, username, email, full_name, phone, status, created_at, updated_at')
      .single();

    if (userError) {
      throw new Error(`创建用户失败: ${userError.message}`);
    }

    // 如果指定了角色，创建用户角色关联
    if (request.roles && request.roles.length > 0) {
      const roleInserts = request.roles.map(role => ({
        user_id: user.id,
        role: role,
        created_at: new Date().toISOString()
      }));

      const { error: roleError } = await supabase
        .from(TABLES.USER_ROLES)
        .insert(roleInserts);

      if (roleError) {
        // 如果角色创建失败，删除已创建的用户
        await supabase
          .from(TABLES.USERS)
          .delete()
          .eq('id', user.id);
        throw new Error(`创建用户角色失败: ${roleError.message}`);
      }
    }

    return user;
  }

  /**
   * 用户登录
   * @param loginData 登录数据
   * @returns 登录响应
   */
  async login(loginData: LoginRequest): Promise<LoginResponse> {
    // 查找用户
    const { data: user, error: userError } = await supabase
      .from(TABLES.USERS)
      .select('id, username, email, full_name, phone, status, password_hash, created_at, updated_at')
      .eq('username', loginData.username)
      .eq('status', UserStatus.ACTIVE)
      .is('deleted_at', null)
      .single();

    if (userError || !user) {
      throw new Error('用户名或密码错误');
    }

    // 验证密码
    const isPasswordValid = await verifyPassword(loginData.password, user.password_hash);
    if (!isPasswordValid) {
      throw new Error('用户名或密码错误');
    }

    // 获取用户角色
    const { data: userRoles, error: rolesError } = await supabase
      .from(TABLES.USER_ROLES)
      .select('id, role, store_id, created_at')
      .eq('user_id', user.id);

    if (rolesError) {
      throw new Error('获取用户角色失败');
    }

    // 转换角色数据格式
    const roles = (userRoles || []).map(role => ({
      id: role.id,
      userId: user.id,
      role: role.role,
      name: this.getRoleName(role.role),
      storeId: role.store_id,
      createdAt: role.created_at,
      permissions: this.getRolePermissions(role.role)
    }));

    // 生成JWT令牌
    const payload: JwtPayload = {
      userId: user.id,
      username: user.username,
      roles: roles.map(r => r.role)
    };

    const accessToken = generateAccessToken(payload);
    const refreshToken = generateRefreshToken(payload);

    // 返回登录响应（排除密码哈希）
    const { password_hash, ...userWithoutPassword } = user;
    return {
      user: userWithoutPassword,
      roles,
      accessToken,
      refreshToken
    };
  }

  /**
   * 根据ID获取用户信息
   * @param userId 用户ID
   * @returns 用户信息
   */
  async getUserById(userId: string): Promise<User | null> {
    const { data: user, error } = await supabase
      .from(TABLES.USERS)
      .select('id, username, email, full_name, phone, status, created_at, updated_at')
      .eq('id', userId)
      .is('deleted_at', null)
      .single();

    if (error) {
      if (error.code === 'PGRST116') {
        return null; // 用户不存在
      }
      throw new Error(`获取用户信息失败: ${error.message}`);
    }

    return user;
  }

  /**
   * 获取用户列表
   * @param params 查询参数
   * @returns 用户列表和总数
   */
  async getUsers(params: UserQueryParams = {}): Promise<{ users: User[]; total: number }> {
    const {
      page = 0,
      pageSize = 20,
      status,
      role,
      username,
      storeId
    } = params;

    let query = supabase
      .from(TABLES.USERS)
      .select('id, username, email, full_name, phone, status, created_at, updated_at', { count: 'exact' })
      .is('deleted_at', null);

    // 添加过滤条件
    if (status) {
      query = query.eq('status', status);
    }

    if (username) {
      query = query.ilike('username', `%${username}%`);
    }

    // 如果需要按角色过滤，需要联表查询
    if (role || storeId) {
      // 先获取符合角色条件的用户ID
      let roleQuery = supabase
        .from(TABLES.USER_ROLES)
        .select('user_id');

      if (role) {
        roleQuery = roleQuery.eq('role', role);
      }

      if (storeId) {
        roleQuery = roleQuery.eq('store_id', storeId);
      }

      const { data: roleUsers, error: roleError } = await roleQuery;

      if (roleError) {
        throw new Error(`获取角色用户失败: ${roleError.message}`);
      }

      const userIds = roleUsers?.map(ru => ru.user_id) || [];
      if (userIds.length === 0) {
        return { users: [], total: 0 };
      }

      query = query.in('id', userIds);
    }

    // 添加分页
    query = query
      .range(page * pageSize, (page + 1) * pageSize - 1)
      .order('created_at', { ascending: false });

    const { data: users, error, count } = await query;

    if (error) {
      throw new Error(`获取用户列表失败: ${error.message}`);
    }

    return {
      users: users || [],
      total: count || 0
    };
  }

  /**
   * 更新用户信息
   * @param userId 用户ID
   * @param updateData 更新数据
   * @returns 更新后的用户信息
   */
  async updateUser(userId: string, updateData: UpdateUserRequest): Promise<User> {
    // 检查用户是否存在
    const existingUser = await this.getUserById(userId);
    if (!existingUser) {
      throw new Error('用户不存在');
    }

    // 更新用户基本信息
    const { roles, ...userUpdateData } = updateData;
    const { data: user, error: userError } = await supabase
      .from(TABLES.USERS)
      .update({
        ...userUpdateData,
        updated_at: new Date().toISOString()
      })
      .eq('id', userId)
      .select('id, username, email, full_name, phone, status, created_at, updated_at')
      .single();

    if (userError) {
      throw new Error(`更新用户失败: ${userError.message}`);
    }

    // 如果需要更新角色
    if (roles) {
      // 删除现有角色
      const { error: deleteError } = await supabase
        .from(TABLES.USER_ROLES)
        .delete()
        .eq('user_id', userId);

      if (deleteError) {
        throw new Error(`删除用户角色失败: ${deleteError.message}`);
      }

      // 添加新角色
      if (roles.length > 0) {
        const roleInserts = roles.map(role => ({
          user_id: userId,
          role: role,
          created_at: new Date().toISOString()
        }));

        const { error: insertError } = await supabase
          .from(TABLES.USER_ROLES)
          .insert(roleInserts);

        if (insertError) {
          throw new Error(`添加用户角色失败: ${insertError.message}`);
        }
      }
    }

    return user;
  }

  /**
   * 删除用户（软删除）
   * @param userId 用户ID
   */
  async deleteUser(userId: string): Promise<void> {
    // 检查用户是否存在
    const existingUser = await this.getUserById(userId);
    if (!existingUser) {
      throw new Error('用户不存在');
    }

    // 软删除用户
    const { error } = await supabase
      .from(TABLES.USERS)
      .update({
        deleted_at: new Date().toISOString(),
        updated_at: new Date().toISOString()
      })
      .eq('id', userId);

    if (error) {
      throw new Error(`删除用户失败: ${error.message}`);
    }
  }

  /**
   * 获取用户角色
   * @param userId 用户ID
   * @returns 用户角色列表
   */
  async getUserRoles(userId: string): Promise<UserRoleInfo[]> {
    const { data: roles, error } = await supabase
      .from(TABLES.USER_ROLES)
      .select('role, created_at')
      .eq('user_id', userId);

    if (error) {
      throw new Error(`获取用户角色失败: ${error.message}`);
    }

    return (roles || []).map(role => ({
      role: role.role,
      name: this.getRoleName(role.role),
      createdAt: role.created_at,
      permissions: this.getRolePermissions(role.role)
    }));
  }

  /**
   * 获取角色名称
   * @param role 角色枚举
   * @returns 角色名称
   */
  private getRoleName(role: UserRole): string {
    const roleNames = {
      [UserRole.HEADQUARTERS_ADMIN]: '总部管理员',
      [UserRole.STORE_ADMIN]: '门店管理员',
      [UserRole.TEACHER]: '教师',
      [UserRole.STUDENT]: '学生'
    };
    return roleNames[role] || '未知角色';
  }

  /**
   * 获取角色权限
   * @param role 角色枚举
   * @returns 权限列表
   */
  private getRolePermissions(role: UserRole): string[] {
    const permissions = {
      [UserRole.HEADQUARTERS_ADMIN]: [
        'user:create', 'user:read', 'user:update', 'user:delete',
        'store:create', 'store:read', 'store:update', 'store:delete',
        'course:create', 'course:read', 'course:update', 'course:delete',
        'assignment:create', 'assignment:read', 'assignment:update', 'assignment:delete',
        'report:read', 'system:manage'
      ],
      [UserRole.STORE_ADMIN]: [
        'user:create', 'user:read', 'user:update',
        'course:create', 'course:read', 'course:update',
        'assignment:create', 'assignment:read', 'assignment:update',
        'report:read'
      ],
      [UserRole.TEACHER]: [
        'course:read', 'assignment:create', 'assignment:read', 'assignment:update',
        'submission:read', 'submission:grade', 'student:read'
      ],
      [UserRole.STUDENT]: [
        'course:read', 'assignment:read', 'submission:create', 'submission:read'
      ]
    };
    return permissions[role] || [];
  }

  /**
   * 修改密码
   * @param userId 用户ID
   * @param oldPassword 旧密码
   * @param newPassword 新密码
   */
  async changePassword(userId: string, oldPassword: string, newPassword: string): Promise<void> {
    // 获取用户信息
    const { data: user, error: userError } = await supabase
      .from(TABLES.USERS)
      .select('password_hash')
      .eq('id', userId)
      .is('deleted_at', null)
      .single();

    if (userError || !user) {
      throw new Error('用户不存在');
    }

    // 验证旧密码
    const isOldPasswordValid = await verifyPassword(oldPassword, user.password_hash);
    if (!isOldPasswordValid) {
      throw new Error('旧密码错误');
    }

    // 验证新密码强度
    if (!validatePasswordStrength(newPassword)) {
      throw new Error('新密码强度不足，请使用至少8位包含大小写字母、数字和特殊字符的密码');
    }

    // 加密新密码
    const newPasswordHash = await hashPassword(newPassword);

    // 更新密码
    const { error: updateError } = await supabase
      .from(TABLES.USERS)
      .update({
        password_hash: newPasswordHash,
        updated_at: new Date().toISOString()
      })
      .eq('id', userId);

    if (updateError) {
      throw new Error(`修改密码失败: ${updateError.message}`);
    }
  }

  /**
   * 重置密码
   * @param userId 用户ID
   * @param newPassword 新密码
   */
  async resetPassword(userId: string, newPassword: string): Promise<void> {
    // 验证新密码强度
    if (!validatePasswordStrength(newPassword)) {
      throw new Error('新密码强度不足，请使用至少8位包含大小写字母、数字和特殊字符的密码');
    }

    // 加密新密码
    const newPasswordHash = await hashPassword(newPassword);

    // 更新密码
    const { error } = await supabase
      .from(TABLES.USERS)
      .update({
        password_hash: newPasswordHash,
        updated_at: new Date().toISOString()
      })
      .eq('id', userId)
      .is('deleted_at', null);

    if (error) {
      throw new Error(`重置密码失败: ${error.message}`);
    }
  }

  /**
   * 获取用户统计信息
   * @returns 用户统计数据
   */
  async getUserStats(): Promise<{
    totalUsers: number;
    activeUsers: number;
    inactiveUsers: number;
    usersByRole: Record<string, number>;
  }> {
    try {
      // 获取总用户数
      const { count: totalUsers, error: totalError } = await supabase
        .from(TABLES.USERS)
        .select('*', { count: 'exact', head: true })
        .is('deleted_at', null);

      if (totalError) {
        throw new Error('获取总用户数失败');
      }

      // 获取活跃用户数
      const { count: activeUsers, error: activeError } = await supabase
        .from(TABLES.USERS)
        .select('*', { count: 'exact', head: true })
        .eq('status', UserStatus.ACTIVE)
        .is('deleted_at', null);

      if (activeError) {
        throw new Error('获取活跃用户数失败');
      }

      // 获取非活跃用户数
      const { count: inactiveUsers, error: inactiveError } = await supabase
        .from(TABLES.USERS)
        .select('*', { count: 'exact', head: true })
        .eq('status', UserStatus.INACTIVE)
        .is('deleted_at', null);

      if (inactiveError) {
        throw new Error('获取非活跃用户数失败');
      }

      // 获取所有用户ID
      const { data: users, error: usersError } = await supabase
        .from(TABLES.USERS)
        .select('id')
        .is('deleted_at', null);

      if (usersError) {
        throw new Error('获取用户列表失败');
      }

      const userIds = users?.map(u => u.id) || [];

      // 获取按角色分组的用户数
      let usersByRole: Record<string, number> = {};
      
      if (userIds.length > 0) {
        const { data: roleData, error: roleError } = await supabase
          .from(TABLES.USER_ROLES)
          .select('role')
          .in('user_id', userIds);

        if (roleError) {
          throw new Error('获取用户角色统计失败');
        }

        roleData?.forEach(item => {
          usersByRole[item.role] = (usersByRole[item.role] || 0) + 1;
        });
      }

      return {
        totalUsers: totalUsers || 0,
        activeUsers: activeUsers || 0,
        inactiveUsers: inactiveUsers || 0,
        usersByRole
      };
    } catch (error) {
      console.error('获取用户统计失败:', error);
      throw new Error('获取用户统计失败');
    }
  }
}

// 导出用户服务实例
export const userService = new UserService();