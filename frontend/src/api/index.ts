import axios from 'axios'
import type { AxiosInstance, InternalAxiosRequestConfig, AxiosResponse, AxiosRequestConfig } from 'axios'

import { notification } from '@/composables'
import { useAuthStore } from '@/stores/auth'

export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
  success: boolean
  timestamp?: string
}

export interface PaginatedResponse<T = any> {
  items: T[]
  total: number
  page: number
  pageSize: number
  totalPages: number
}

export interface ApiError {
  code: number
  message: string
  details?: any
  timestamp: string
}

// 创建 axios 实例
const apiClient: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
apiClient.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    // 添加认证 token
    const authStore = useAuthStore()
    const token = authStore.token
    
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }

    // 添加请求 ID 用于追踪
    config.headers['X-Request-ID'] = crypto.randomUUID()

    // 开发环境下打印请求信息
    if (import.meta.env.DEV) {
      console.log('🚀 API Request:', {
        method: config.method?.toUpperCase(),
        url: config.url,
        data: config.data,
        params: config.params
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
  (response: AxiosResponse<ApiResponse<unknown>>) => {
    const { data } = response

    // 开发环境下打印响应信息
    if (import.meta.env.DEV) {
      console.log('✅ API Response:', {
        url: response.config.url,
        status: response.status,
        data: data
      })
    }

    // 检查业务状态码
    if (data.code !== 200 && data.code !== 0) {
      const error = new Error(data.message || '请求失败')
      ;(_error as unknown as { code: number; response: AxiosResponse }).code = data.code
      ;(_error as unknown as { code: number; response: AxiosResponse }).response = response
      throw error
    }

    return response
  },
  async (error) => {
    // 统一错误处理
    const axiosError = error as { response?: { status?: number; data?: { message?: string } }; config?: { url?: string }; message?: string; code?: string }
    
    console.error('❌ API Error:', {
      status: axiosError.response?.status,
      url: axiosError.config?.url,
      message: axiosError.message,
      data: axiosError.response?.data
    })

    const status = axiosError.response?.status
    const message = axiosError.response?.data?.message || axiosError.message

    // 处理不同的 HTTP 状态码
    switch (status) {
      case 401:
        // Token 过期或无效，尝试刷新 token
        const authStore = useAuthStore()
        const { getRefreshToken } = authStore
        
        if (getRefreshToken() && !axiosError.config?.url?.includes('/auth/refresh')) {
          try {
            await authStore.refreshToken()
            // 重新发送原请求
            if (axiosError.config) {
              const config = axiosError.config as InternalAxiosRequestConfig
              config.headers.Authorization = `Bearer ${authStore.token}`
              return apiClient.request(config)
            }
          } catch (refreshError) {
            // 刷新失败，跳转到登录页
            authStore.logout()
            window.location.href = '/auth/login'
            return Promise.reject(refreshError)
          }
        } else {
          // 没有 refresh token 或者是刷新接口本身失败
          authStore.logout()
          window.location.href = '/auth/login'
        }
        break
        
      case 403:
        notification.error('权限不足，请联系管理员')
        break
        
      case 404:
        notification.error('请求的资源不存在')
        break
        
      case 422:
        notification.error(message || '请求参数验证失败')
        break
        
      case 429:
        notification.error('请求过于频繁，请稍后再试')
        break
        
      case 500:
        notification.error('服务器内部错误，请稍后再试')
        break
        
      case 502:
      case 503:
      case 504:
        notification.error('服务暂时不可用，请稍后再试')
        break
        
      default:
        // 处理网络错误
        if (axiosError.code === 'ECONNABORTED') {
          notification.error('请求超时，请检查网络连接')
        } else if (axiosError.code === 'ERR_NETWORK') {
          notification.error('网络连接失败，请检查网络设置')
        } else {
          notification.error(message || '网络请求失败')
        }
    }

    return Promise.reject(error)
  }
)

// 通用请求方法
export const request = {
  get: <T = any>(
    url: string,
    params?: any,
    config?: AxiosRequestConfig
  ): Promise<ApiResponse<T>> => {
    return apiClient.get(url, { params, ...config }).then(res => res.data)
  },

  post: <T = any>(
    url: string,
    data?: any,
    config?: AxiosRequestConfig
  ): Promise<ApiResponse<T>> => {
    return apiClient.post(url, data, config).then(res => res.data)
  },

  put: <T = any>(
    url: string,
    data?: any,
    config?: AxiosRequestConfig
  ): Promise<ApiResponse<T>> => {
    return apiClient.put(url, data, config).then(res => res.data)
  },

  delete: <T = any>(
    url: string,
    config?: AxiosRequestConfig
  ): Promise<ApiResponse<T>> => {
    return apiClient.delete(url, config).then(res => res.data)
  },

  patch: <T = any>(
    url: string,
    data?: any,
    config?: AxiosRequestConfig
  ): Promise<ApiResponse<T>> => {
    return apiClient.patch(url, data, config).then(res => res.data)
  }
}

// 文件上传方法
export const upload = (
  url: string,
  file: File,
  onUploadProgress?: (progressEvent: { loaded: number; total?: number }) => void
): Promise<ApiResponse> => {
  const formData = new FormData()
  formData.append('file', file)

  const config: AxiosRequestConfig = {
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    onUploadProgress
  }

  return apiClient.post(url, formData, config).then(res => res.data)
}

// 导出 axios 实例供特殊需求使用
export { apiClient }
export default request
