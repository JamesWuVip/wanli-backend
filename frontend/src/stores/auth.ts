import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

interface User {
  id: string
  name: string
  email: string
  avatar?: string
}

interface LoginCredentials {
  email: string
  password: string
  rememberMe?: boolean
}

interface RegisterCredentials {
  username: string
  email: string
  password: string
}

export const useAuthStore = defineStore('auth', () => {
  // 状态
  const user = ref<User | null>(null)
  const token = ref<string | null>(null)
  const isLoading = ref(false)

  // 计算属性
  const isAuthenticated = computed(() => {
    return !!token.value && !!user.value
  })

  // 初始化认证状态
  const initAuth = () => {
    const savedToken = localStorage.getItem('auth_token')
    const savedUser = localStorage.getItem('auth_user')
    
    if (savedToken && savedUser) {
      token.value = savedToken
      try {
        user.value = JSON.parse(savedUser)
      } catch (error) {
        console.error('解析用户信息失败:', error)
        clearAuth()
      }
    }
  }

  // 清除认证信息
  const clearAuth = () => {
    user.value = null
    token.value = null
    localStorage.removeItem('auth_token')
    localStorage.removeItem('auth_user')
  }

  // 登录
  const login = async (credentials: LoginCredentials) => {
    isLoading.value = true
    try {
      // 模拟API调用
      await new Promise(resolve => setTimeout(resolve, 1000))
      
      // 模拟登录成功
      const mockUser: User = {
        id: '1',
        name: '测试用户',
        email: credentials.email,
        avatar: 'https://via.placeholder.com/40'
      }
      
      const mockToken = 'mock_jwt_token_' + Date.now()
      
      user.value = mockUser
      token.value = mockToken
      
      // 保存到本地存储
      if (credentials.rememberMe) {
        localStorage.setItem('auth_token', mockToken)
        localStorage.setItem('auth_user', JSON.stringify(mockUser))
      }
      
      return { user: mockUser, token: mockToken }
    } catch (error) {
      clearAuth()
      throw new Error('登录失败，请检查邮箱和密码')
    } finally {
      isLoading.value = false
    }
  }

  // 注册
  const register = async (credentials: RegisterCredentials) => {
    isLoading.value = true
    try {
      // 模拟API调用
      await new Promise(resolve => setTimeout(resolve, 1500))
      
      // 模拟注册成功
      const mockUser: User = {
        id: '1',
        name: credentials.username,
        email: credentials.email,
        avatar: 'https://via.placeholder.com/40'
      }
      
      const mockToken = 'mock_jwt_token_' + Date.now()
      
      user.value = mockUser
      token.value = mockToken
      
      // 保存到本地存储
      localStorage.setItem('auth_token', mockToken)
      localStorage.setItem('auth_user', JSON.stringify(mockUser))
      
      return { user: mockUser, token: mockToken }
    } catch (error) {
      clearAuth()
      throw new Error('注册失败，请稍后重试')
    } finally {
      isLoading.value = false
    }
  }

  // 登出
  const logout = async () => {
    isLoading.value = true
    try {
      // 模拟API调用
      await new Promise(resolve => setTimeout(resolve, 500))
      clearAuth()
    } catch (error) {
      console.error('登出失败:', error)
      // 即使API调用失败，也要清除本地认证信息
      clearAuth()
    } finally {
      isLoading.value = false
    }
  }

  // 刷新token
  const refreshToken = async () => {
    if (!token.value) {
      throw new Error('没有有效的token')
    }
    
    try {
      // 模拟API调用
      await new Promise(resolve => setTimeout(resolve, 500))
      
      const newToken = 'refreshed_token_' + Date.now()
      token.value = newToken
      localStorage.setItem('auth_token', newToken)
      
      return newToken
    } catch (error) {
      clearAuth()
      throw new Error('token刷新失败')
    }
  }

  return {
    // 状态
    user,
    token,
    isLoading,
    
    // 计算属性
    isAuthenticated,
    
    // 方法
    initAuth,
    login,
    register,
    logout,
    refreshToken,
    clearAuth
  }
})