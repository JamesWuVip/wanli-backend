import type { UserRole, UserStatus } from '../index'

/**
 * 认证相关API类型定义
 */

// 用户信息接口
export interface User {
  id: string
  username: string
  email: string
  fullName: string
  avatar?: string
  role: UserRole
  status: UserStatus
  phone?: string
  department?: string
  position?: string
  lastLoginAt?: string
  createdAt: string
  updatedAt: string
}

// 兼容性别名
export interface UserInfo {
  id: string
  username: string
  email: string
  fullName?: string
  name?: string
  avatar?: string
  role: UserRole
  status?: UserStatus
  phone?: string
  department?: string
  position?: string
  lastLoginAt?: string
  createdAt?: string
  updatedAt?: string
}

// 登录凭据类型（用于组合式函数）
export interface LoginCredentials {
  username: string
  password: string
  rememberMe?: boolean
}

// 注册数据类型（用于组合式函数）
export interface RegisterData {
  username: string
  email: string
  password: string
  confirmPassword: string
  fullName: string
  phone?: string
}

// 登录请求
export interface LoginRequest {
  username: string
  password: string
  rememberMe?: boolean
  captcha?: string
  captchaId?: string
}

// 登录响应
export interface LoginResponse {
  accessToken: string
  refreshToken: string
  expiresIn: number
  tokenType: string
  user: UserInfo
}

// 注册请求
export interface RegisterRequest {
  username: string
  email: string
  password: string
  confirmPassword: string
  fullName: string
  phone?: string
  inviteCode?: string
  captcha?: string
  captchaId?: string
}

// 注册响应
export interface RegisterResponse {
  user: UserInfo
  message: string
  requiresEmailVerification?: boolean
}

// 刷新令牌请求
export interface RefreshTokenRequest {
  refreshToken: string
}

// 刷新令牌响应
export interface RefreshTokenResponse {
  accessToken: string
  refreshToken: string
  expiresIn: number
  tokenType: string
}

// 登出请求
export interface LogoutRequest {
  refreshToken?: string
}

// 获取当前用户响应
export interface GetCurrentUserResponse {
  user: UserInfo
  permissions: string[]
  menus: Array<{
    id: string
    name: string
    path: string
    icon?: string
    children?: Array<{
      id: string
      name: string
      path: string
      icon?: string
    }>
  }>
}

// 邮箱验证请求
export interface VerifyEmailRequest {
  token: string
  email: string
}

// 邮箱验证响应
export interface VerifyEmailResponse {
  success: boolean
  message: string
}

// 发送密码重置请求
export interface SendPasswordResetRequest {
  email: string
  captcha?: string
  captchaId?: string
}

// 发送密码重置响应
export interface SendPasswordResetResponse {
  message: string
  success: boolean
}

// 重置密码请求
export interface ResetPasswordRequest {
  token: string
  email: string
  newPassword: string
  confirmPassword: string
}

// 重置密码响应
export interface ResetPasswordResponse {
  success: boolean
  message: string
}

// 修改密码请求
export interface ChangePasswordRequest {
  currentPassword: string
  newPassword: string
  confirmPassword: string
}

// 修改密码响应
export interface ChangePasswordResponse {
  success: boolean
  message: string
}

// 验证码响应
export interface CaptchaResponse {
  captchaId: string
  captchaImage: string // base64 encoded image
  expiresIn: number
}

// 权限检查请求
export interface CheckPermissionRequest {
  resource: string
  action: string
}

// 权限检查响应
export interface CheckPermissionResponse {
  hasPermission: boolean
  reason?: string
}

// 会话信息
export interface SessionInfo {
  sessionId: string
  userId: string
  userAgent: string
  ipAddress: string
  loginAt: string
  lastActiveAt: string
  expiresAt: string
  isActive: boolean
}

// 获取用户会话响应
export interface GetUserSessionsResponse {
  sessions: SessionInfo[]
  currentSessionId: string
}

// 终止会话请求
export interface TerminateSessionRequest {
  sessionId: string
}

// 终止会话响应
export interface TerminateSessionResponse {
  success: boolean
  message: string
}