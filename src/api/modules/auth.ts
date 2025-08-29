import { api } from '../index'
import type { 
  LoginRequest, 
  LoginResponse, 
  RegisterRequest, 
  RegisterResponse,
  RefreshTokenRequest,
  RefreshTokenResponse,
  GetCurrentUserResponse
} from '@/types/api/auth'

/**
 * 认证相关API
 */
const authApi = {
  /**
   * 用户登录
   * @param credentials 登录凭据
   */
  login(credentials: LoginRequest) {
    return api.post<LoginResponse>('/auth/login', credentials)
  },

  /**
   * 用户注册
   * @param userData 注册数据
   */
  register(userData: RegisterRequest) {
    return api.post<RegisterResponse>('/auth/register', userData)
  },

  /**
   * 刷新访问令牌
   * @param refreshData 刷新令牌数据
   */
  refreshToken(refreshData: RefreshTokenRequest) {
    return api.post<RefreshTokenResponse>('/auth/refresh', refreshData)
  },

  /**
   * 用户登出
   */
  logout() {
    return api.post('/auth/logout')
  },

  /**
   * 获取当前用户信息
   */
  getCurrentUser() {
    return api.get<GetCurrentUserResponse>('/auth/me')
  },

  /**
   * 验证邮箱
   * @param token 验证令牌
   */
  verifyEmail(token: string) {
    return api.post('/auth/verify-email', { token })
  },

  /**
   * 发送密码重置邮件
   * @param email 邮箱地址
   */
  forgotPassword(email: string) {
    return api.post('/auth/forgot-password', { email })
  },

  /**
   * 重置密码
   * @param token 重置令牌
   * @param newPassword 新密码
   */
  resetPassword(token: string, newPassword: string) {
    return api.post('/auth/reset-password', { token, password: newPassword })
  },

  /**
   * 修改密码
   * @param currentPassword 当前密码
   * @param newPassword 新密码
   */
  changePassword(currentPassword: string, newPassword: string) {
    return api.post('/auth/change-password', {
      currentPassword,
      newPassword
    })
  }
}

export default authApi