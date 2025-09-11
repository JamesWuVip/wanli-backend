import { supabase, TABLES, UserStatus, UserRole } from '../config/supabase.js';
import { hashPassword, verifyPassword, validatePasswordStrength } from '../utils/password.js';
import { generateAccessToken, generateRefreshToken, JwtPayload } from '../utils/jwt.js';

// 用户接口定义
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

// 登录请求接口
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
 * 处理用户相关的业务逻辑
 */
export class UserService {
  
  /**
   * 创建新用户
   * @param request 创建用户请求数据
   * @returns 创建的用户信息（不包含密码）
   */
  async createUser(request: CreateUserRequest): Promise<Omit<User, 'password_hash'>> {
    const { username, email, password, full_name, phone, roles = [] } = request;
    
    // 验证密码强度
    if (!validatePasswordStrength(password)) {
      throw new Error('密码强度不足，密码必须包含至少8个字符，包括大小写字母、数字和特殊字符');
    }
    
    // 检查用户名是否已存在
    const { data: existingUser } = await supabase
      .from(TABLES.USERS)
      .select('id')
      .eq('username', username)
      .single();
    
    if (existingUser) {
      throw new Error('用户名已存在');
    }
    
    // 检查邮箱是否已存在
    const { data: existingEmail } = await supabase
      .from(TABLES.USERS)
      .select('id')
      .eq('email', email)
      .single();
    
    if (existingEmail) {
      throw new Error('邮箱已存在');
    }
    
    // 创建用户
    const hashedPassword = await hashPassword(password);
    const { data: user, error } = await supabase
      .from(TABLES.USERS)
      .insert({
        username,
        email,
        password_hash: hashedPassword,
        full_name,
        phone,
        status: UserStatus.ACTIVE
      })
      .select('id, username, email, full_name, phone, status, created_at, updated_at')
      .single();
    
    if (error) {
      throw new Error(`创建用户失败: ${error.message}`);
    }
    
    // 如果指定了角色，创建用户角色关联
    if (roles.length > 0) {
      const roleInserts = roles.map(role => ({
        user_id: user.id,
        role
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
   * @returns 登录响应，包含用户信息、角色和令牌
   */
  async login(loginData: LoginRequest): Promise<LoginResponse> {
    const { username, password } = loginData;
    
    // 查找用户
    const { data: user, error } = await supabase
      .from(TABLES.USERS)
      .select('*')
      .eq('username', username)
      .eq('status', UserStatus.ACTIVE)
      .single();
    
    if (error || !user) {
      throw new Error('用户名或密码错误');
    }
    
    // 验证密码
    const isValidPassword = await verifyPassword(password, user.password_hash);
    if (!isValidPassword) {
      throw new Error('用户名或密码错误');
    }
    
    // 获取用户角色
    const { data: userRoles, error: roleError } = await supabase
      .from(TABLES.USER_ROLES)
      .select('*')
      .eq('user_id', user.id);
    
    if (roleError) {
      throw new Error(`获取用户角色失败: ${roleError.message}`);
    }
    
    // 转换角色数据格式
    const roles = (userRoles || []).map(role => ({
      id: role.id,
      userId: role.user_id,
      role: role.role,
      name: this.getRoleName(role.role),
      storeId: role.store_id,
      createdAt: role.created_at,
      permissions: this.getRolePermissions(role.role)
    }));
    
    // 生成令牌
    const payload: JwtPayload = {
      userId: user.id,
      username: user.username,
      roles: roles.map(r => r.role)
    };
    
    const accessToken = generateAccessToken(payload);
    const refreshToken = generateRefreshToken(payload);
    
    // 返回用户信息（不包含密码）
    const { password_hash, ...userWithoutPassword } = user;
    
    return {
      user: userWithoutPassword,
      roles,
      accessToken,
      refreshToken
    };
  }
  
  /**
   * 根据ID查找用户
   * @param userId 用户ID
   * @returns 用户信息（不包含密码）
   */
  async findById(userId: string): Promise<Omit<User, 'password_hash'> | null> {
    const { data: user, error } = await supabase
      .from(TABLES.USERS)
      .select('id, username, email, full_name, phone, status, created_at, updated_at')
      .eq('id', userId)
      .single();
    
    if (error) {
      if (error.code === 'PGRST116') {
        return null; // 用户不存在
      }
      throw new Error(`查找用户失败: ${error.message}`);
    }
    
    return user;
  }
  
  /**
   * 根据用户名查找用户
   * @param username 用户名
   * @returns 用户信息（不包含密码）
   */
  async findByUsername(username: string): Promise<Omit<User, 'password_hash'> | null> {
    const { data: user, error } = await supabase
      .from(TABLES.USERS)
      .select('id, username, email, full_name, phone, status, created_at, updated_at')
      .eq('username', username)
      .single();
    
    if (error) {
      if (error.code === 'PGRST116') {
        return null; // 用户不存在
      }
      throw new Error(`查找用户失败: ${error.message}`);
    }
    
    return user;
  }
  
  /**
   * 获取用户角色信息
   * @param userId 用户ID
   * @returns 用户角色列表
   */
  async getUserRoles(userId: string): Promise<UserRoleInfo[]> {
    const { data: userRoles, error } = await supabase
      .from(TABLES.USER_ROLES)
      .select('*')
      .eq('user_id', userId);
    
    if (error) {
      throw new Error(`获取用户角色失败: ${error.message}`);
    }
    
    return (userRoles || []).map(role => ({
      role: role.role,
      name: this.getRoleName(role.role),
      createdAt: role.created_at,
      permissions: this.getRolePermissions(role.role)
    }));
  }
  
  /**
   * 查询用户列表
   * @param params 查询参数
   * @returns 用户列表和总数
   */
  async findUsers(params: UserQueryParams = {}): Promise<{
    users: Array<Omit<User, 'password_hash'>>;
    total: number;
  }> {
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
      .select('id, username, email, full_name, phone, status, created_at, updated_at', { count: 'exact' });
    
    // 添加过滤条件
    if (status) {
      query = query.eq('status', status);
    }
    
    if (username) {
      query = query.eq('username', username);
    }
    
    // 如果需要按角色过滤，需要关联查询
    if (role) {
      const { data: userIds, error: roleError } = await supabase
        .from(TABLES.USER_ROLES)
        .select('user_id')
        .eq('role', role);
      
      if (roleError) {
        throw new Error(`查询用户角色失败: ${roleError.message}`);
      }
      
      if (userIds && userIds.length > 0) {
        const ids = userIds.map(item => item.user_id);
        
        // Supabase的in查询有限制，如果数组太大会导致Bad Request
        // 当数组超过100个元素时，使用分批查询
        if (ids.length > 100) {
          // 分批查询，每批100个
          const batchSize = 100;
          const batchResults: Array<{
            data: any[] | null;
            error: any;
            count: number | null;
          }> = [];
          
          for (let i = 0; i < ids.length; i += batchSize) {
            const batchIds = ids.slice(i, i + batchSize);
            let batchQuery = supabase
              .from(TABLES.USERS)
              .select('id, username, email, full_name, phone, status, created_at, updated_at', { count: 'exact' })
              .in('id', batchIds);
            
            // 应用其他过滤条件
            if (status) {
              batchQuery = batchQuery.eq('status', status);
            }
            if (storeId) {
              batchQuery = batchQuery.eq('store_id', storeId);
            }
            
            // 执行查询并收集结果
            const result = await batchQuery;
            batchResults.push(result);
          }
          
          // 合并结果
          let allUsers: any[] = [];
          let totalCount = 0;
          
          for (const result of batchResults) {
            if (result.error) {
              throw new Error(`查询用户列表失败: ${result.error.message}`);
            }
            if (result.data) {
              allUsers = allUsers.concat(result.data);
              totalCount += result.count || 0;
            }
          }
          
          // 应用分页
          const startIndex = page * pageSize;
          const endIndex = startIndex + pageSize;
          const paginatedUsers = allUsers.slice(startIndex, endIndex);
          
          return {
            users: paginatedUsers,
            total: totalCount
          };
        }
        
        query = query.in('id', ids);
      } else {
        // 如果没有找到对应角色的用户，返回空结果
        return { users: [], total: 0 };
      }
    }
    
    // 添加分页
    query = query
      .range(page * pageSize, (page + 1) * pageSize - 1)
      .order('created_at', { ascending: false });
    
    const { data: users, error, count } = await query;
    
    if (error) {
      throw new Error(`查询用户列表失败: ${error.message}`);
    }
    
    return {
      users: users || [],
      total: count || 0
    };
  }
  
  /**
   * 更新用户状态
   * @param userId 用户ID
   * @param status 新状态
   * @returns 更新后的用户信息
   */
  async updateUserStatus(userId: string, status: UserStatus): Promise<Omit<User, 'password_hash'>> {
    const { data: user, error } = await supabase
      .from(TABLES.USERS)
      .update({ status, updated_at: new Date().toISOString() })
      .eq('id', userId)
      .select('id, username, email, full_name, phone, status, created_at, updated_at')
      .single();
    
    if (error) {
      throw new Error(`更新用户状态失败: ${error.message}`);
    }
    
    return user;
  }
  
  /**
   * 更新用户信息
   * @param userId 用户ID
   * @param updateData 更新数据
   * @returns 更新后的用户信息
   */
  async updateUser(userId: string, updateData: UpdateUserRequest): Promise<Omit<User, 'password_hash'>> {
    const { roles, ...userUpdateData } = updateData;
    
    // 更新用户基本信息
    const { data: user, error } = await supabase
      .from(TABLES.USERS)
      .update({ ...userUpdateData, updated_at: new Date().toISOString() })
      .eq('id', userId)
      .select('id, username, email, full_name, phone, status, created_at, updated_at')
      .single();
    
    if (error) {
      throw new Error(`更新用户信息失败: ${error.message}`);
    }
    
    return user;
  }
  
  /**
   * 删除用户（软删除）
   * @param userId 用户ID
   */
  async deleteUser(userId: string): Promise<void> {
    const { error } = await supabase
      .from(TABLES.USERS)
      .update({ 
        status: UserStatus.INACTIVE,
        deleted_at: new Date().toISOString(),
        updated_at: new Date().toISOString()
      })
      .eq('id', userId);
    
    if (error) {
      throw new Error(`删除用户失败: ${error.message}`);
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
        .select('*', { count: 'exact', head: true });
      
      if (totalError) {
        throw new Error(`获取总用户数失败: ${totalError.message}`);
      }
      
      // 获取活跃用户数
      const { count: activeUsers, error: activeError } = await supabase
        .from(TABLES.USERS)
        .select('*', { count: 'exact', head: true })
        .eq('status', UserStatus.ACTIVE);
      
      if (activeError) {
        throw new Error(`获取活跃用户数失败: ${activeError.message}`);
      }
      
      // 获取非活跃用户数
      const { count: inactiveUsers, error: inactiveError } = await supabase
        .from(TABLES.USERS)
        .select('*', { count: 'exact', head: true })
        .eq('status', UserStatus.INACTIVE);
      
      if (inactiveError) {
        throw new Error(`获取非活跃用户数失败: ${inactiveError.message}`);
      }
      
      // 获取所有用户ID用于角色统计
      const { data: userIds, error: userIdsError } = await supabase
        .from(TABLES.USERS)
        .select('id');
      
      if (userIdsError) {
        throw new Error(`获取用户ID列表失败: ${userIdsError.message}`);
      }
      
      // 初始化角色统计
      let usersByRole: Record<string, number> = {};
      
      // 只有当存在用户时才查询角色统计
      if (userIds && userIds.length > 0) {
        const ids = userIds.map(user => user.id);
        
        // 获取按角色分组的用户数
        const { data: roleStats, error: roleError } = await supabase
          .from(TABLES.USER_ROLES)
          .select('role')
          .in('user_id', ids);
        
        if (roleError) {
          throw new Error(`获取用户角色统计失败: ${roleError.message}`);
        }
        
        // 统计每个角色的用户数
        if (roleStats) {
          usersByRole = roleStats.reduce((acc: Record<string, number>, item: any) => {
            acc[item.role] = (acc[item.role] || 0) + 1;
            return acc;
          }, {});
        }
      }
      
      return {
        totalUsers: totalUsers || 0,
        activeUsers: activeUsers || 0,
        inactiveUsers: inactiveUsers || 0,
        usersByRole
      };
    } catch (error) {
      console.error('获取用户统计失败:', error);
      throw error;
    }
  }
  
  /**
   * 获取角色名称
   * @param role 角色枚举
   * @returns 角色名称
   */
  private getRoleName(role: UserRole): string {
    const roleNames = {
      [UserRole.SUPER_ADMIN]: '超级管理员',
      [UserRole.HEADQUARTERS_ADMIN]: '总部管理员',
      [UserRole.STORE_MANAGER]: '门店经理',
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
      [UserRole.SUPER_ADMIN]: ['*'], // 所有权限
      [UserRole.HEADQUARTERS_ADMIN]: [
        'user:read', 'user:write', 'user:delete',
        'store:read', 'store:write', 'store:delete',
        'course:read', 'course:write', 'course:delete'
      ],
      [UserRole.STORE_MANAGER]: [
        'user:read', 'user:write',
        'store:read', 'store:write',
        'course:read', 'course:write'
      ],
      [UserRole.TEACHER]: [
        'course:read', 'course:write',
        'student:read'
      ],
      [UserRole.STUDENT]: [
        'course:read'
      ]
    };
    
    return permissions[role] || [];
  }
}

export const userService = new UserService();