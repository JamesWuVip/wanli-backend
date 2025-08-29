import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { LoginCredentials, RegisterData, UserInfo } from '@/types/api/auth'

// 认证状态接口
interface AuthState {
  token: string | null
  refreshToken: string | null
  isAuthenticated: boolean
}

export const useAuthStore = defineStore('auth', () => {
  // 状态
  const token = ref<string | null>(localStorage.getItem('token'))
  const refreshToken = ref<string | null>(localStorage.getItem('refreshToken'))
  const userInfo = ref<UserInfo | null>(null)

  // 用户信息初始化（从localStorage恢复或保持为null）

  // 计算属性
  const isAuthenticated = computed(() => !!token.value)
  const userRole = computed(() => userInfo.value?.role || '')

  // Actions
  const setToken = (newToken: string) => {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  const setRefreshToken = (newRefreshToken: string) => {
    refreshToken.value = newRefreshToken
    localStorage.setItem('refreshToken', newRefreshToken)
  }

  const setUserInfo = (info: UserInfo) => {
    userInfo.value = info
  }

  const login = async (credentials: { username: string; password: string }) => {
    try {
      // TODO: 调用登录 API
      // const response = await authApi.login(credentials)
      // setToken(response.token)
      // setRefreshToken(response.refreshToken)
      // setUserInfo(response.user)
      
      // 临时模拟登录
      console.log('Login with:', credentials)
      return { success: true }
    } catch (error) {
      console.error('Login failed:', error)
      throw error
    }
  }

  // 注册
  const register = async (data: RegisterData) => {
    try {
      // TODO: 调用注册 API
      // const response = await authApi.register(data)
      // setToken(response.data.token)
      // setRefreshToken(response.data.refreshToken)
      // setUserInfo(response.data.user)
      console.log('Register with:', data)
    } catch (error) {
      console.error('Register failed:', error)
      throw error
    }
  }

  const logout = () => {
    token.value = null
    refreshToken.value = null
    userInfo.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('refreshToken')
  }

  const refreshAccessToken = async () => {
    try {
      if (!refreshToken.value) {
        throw new Error('No refresh token available')
      }
      
      // TODO: 调用刷新 token API
      // const response = await authApi.refreshToken(refreshToken.value)
      // setToken(response.token)
      
      console.log('Token refreshed')
    } catch (error) {
      console.error('Token refresh failed:', error)
      logout()
      throw error
    }
  }

  // 检查权限
  const hasPermission = (permission: string): boolean => {
    // TODO: 实现权限检查逻辑
    // 这里可以根据用户角色和权限列表进行检查
    console.log('Checking permission:', permission)
    return true // 临时返回true
  }

  // 检查角色
  const hasRole = (role: string): boolean => {
    return userRole.value === role
  }

  // 获取用户信息
  const fetchUserInfo = async () => {
    try {
      // TODO: 调用获取用户信息 API
      // const response = await authApi.getCurrentUser()
      // setUserInfo(response.data)
      console.log('Fetching user info')
    } catch (error) {
      console.error('Failed to fetch user info:', error)
      throw error
    }
  }

  // 更新用户信息
  const updateUser = async (userData: Partial<UserInfo>) => {
    try {
      // TODO: 调用更新用户信息 API
      // const response = await authApi.updateUser(userData)
      // setUserInfo(response.data)
      console.log('Updating user:', userData)
    } catch (error) {
      console.error('Failed to update user:', error)
      throw error
    }
  }

  // 修改密码
  const changePassword = async (oldPassword: string, newPassword: string) => {
    try {
      // TODO: 调用修改密码 API
      // await authApi.changePassword({ oldPassword, newPassword })
      console.log('Changing password')
    } catch (error) {
      console.error('Failed to change password:', error)
      throw error
    }
  }

  // 重置密码
  const resetPassword = async (email: string) => {
    try {
      // TODO: 调用重置密码 API
      // await authApi.resetPassword({ email })
      console.log('Resetting password for:', email)
    } catch (error) {
      console.error('Failed to reset password:', error)
      throw error
    }
  }

  return {
    // 状态
    token,
    refreshToken,
    userInfo,
    // 计算属性
    isAuthenticated,
    userRole,
    // Actions
    setToken,
    setRefreshToken,
    setUserInfo,
    login,
    register,
    logout,
    refreshAccessToken,
    hasPermission,
    hasRole,
    fetchUserInfo,
    updateUser,
    changePassword,
    resetPassword
  }
})