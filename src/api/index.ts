import axios from 'axios'
import type { AxiosRequestConfig, AxiosResponse, InternalAxiosRequestConfig } from 'axios'
import { useAuthStore } from '@/store/modules/auth'
import router from '@/router'

// API基础配置
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'https://api.staging.wanli.ai'
const API_TIMEOUT = 10000 // 10秒超时

// 创建axios实例
const apiClient = axios.create({
  baseURL: API_BASE_URL,
  timeout: API_TIMEOUT,
  headers: {
    'Content-Type': 'application/json',
  },
})

// 请求拦截器
apiClient.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const authStore = useAuthStore()
    
    // 添加认证token
    if (authStore.token) {
      config.headers.Authorization = `Bearer ${authStore.token}`
    }

    // 添加请求ID
    config.headers['X-Request-ID'] = generateRequestId()
    
    // 开发环境下打印请求信息
    if (import.meta.env.DEV) {
      console.log('🚀 API Request:', {
        method: config.method?.toUpperCase(),
        url: config.url,
        data: config.data,
        params: config.params,
      })
    }
    
    return config
  },
  (error) => {
    console.error('❌ Request Error:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
apiClient.interceptors.response.use(
  (response: AxiosResponse) => {
    // 开发环境下打印响应信息
    if (import.meta.env.DEV) {
      console.log('✅ API Response:', {
        status: response.status,
        url: response.config.url,
        data: response.data,
      })
    }
    
    return response
  },
  async (error) => {
    const authStore = useAuthStore()
    
    // 处理响应错误
    if (error.response) {
      const { status, data } = error.response
      
      switch (status) {
        case 401:
          // Token失效或未认证
          console.warn('🔒 Unauthorized: Token expired or invalid')
          
          // 尝试刷新token
          if (authStore.refreshToken) {
            try {
              await authStore.refreshAccessToken()
              // 重新发送原请求
              return apiClient.request(error.config)
            } catch (refreshError) {
              // 刷新失败，清除认证信息并跳转登录
              authStore.logout()
              router.push({ name: 'login' })
            }
          } else {
            // 没有refresh token，直接跳转登录
            authStore.logout()
            router.push({ name: 'login' })
          }
          break
          
        case 403:
          // 权限不足
          console.warn('🚫 Forbidden: Insufficient permissions')
          // 可以显示权限不足的提示
          break
          
        case 404:
          // 资源不存在
          console.warn('🔍 Not Found: Resource does not exist')
          break
          
        case 422:
          // 表单验证错误
          console.warn('📝 Validation Error:', data)
          break
          
        case 429:
          // 请求过于频繁
          console.warn('⏰ Too Many Requests: Rate limit exceeded')
          break
          
        case 500:
        case 502:
        case 503:
        case 504:
          // 服务器错误
          console.error('🔥 Server Error:', status, data)
          break
          
        default:
          console.error('❌ API Error:', status, data)
      }
    } else if (error.request) {
      // 网络错误
      console.error('🌐 Network Error: No response received')
    } else {
      // 其他错误
      console.error('⚠️ Request Setup Error:', error.message)
    }
    
    return Promise.reject(error)
  }
)

// 生成请求ID
function generateRequestId(): string {
  return `req_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`
}

// 导出API客户端实例
export default apiClient

// 导出常用的HTTP方法封装
export const api = {
  get: <T = any>(url: string, config?: AxiosRequestConfig) => 
    apiClient.get<T>(url, config),
    
  post: <T = any>(url: string, data?: any, config?: AxiosRequestConfig) => 
    apiClient.post<T>(url, data, config),
    
  put: <T = any>(url: string, data?: any, config?: AxiosRequestConfig) => 
    apiClient.put<T>(url, data, config),
    
  patch: <T = any>(url: string, data?: any, config?: AxiosRequestConfig) => 
    apiClient.patch<T>(url, data, config),
    
  delete: <T = any>(url: string, config?: AxiosRequestConfig) => 
    apiClient.delete<T>(url, config),
}

// 导出类型
export type { AxiosRequestConfig, AxiosResponse }