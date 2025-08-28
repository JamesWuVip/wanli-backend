import axios from 'axios'
import type { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios'
import { useAuthStore } from '@/store/modules/auth'
import { useNotification } from '@/composables/useNotification'

// 创建 axios 实例
const apiClient: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
apiClient.interceptors.request.use(
  (config) => {
    // 添加认证 token
    const authStore = useAuthStore()
    if (authStore.token) {
      config.headers.Authorization = `Bearer ${authStore.token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
apiClient.interceptors.response.use(
  (response: AxiosResponse) => {
    return response
  },
  (error) => {
    const { showError } = useNotification()
    
    if (error.response?.status === 401) {
      // 未授权，清除 token 并跳转到登录页
      const authStore = useAuthStore()
      authStore.logout()
      window.location.href = '/auth/login'
    } else if (error.response?.status === 403) {
      showError('没有权限访问此资源')
    } else if (error.response?.status >= 500) {
      showError('服务器错误，请稍后重试')
    } else {
      showError(error.response?.data?.message || '请求失败')
    }
    
    return Promise.reject(error)
  }
)

// API 服务类
export class ApiService {
  static async get<T = any>(url: string, config?: AxiosRequestConfig): Promise<T> {
    const response = await apiClient.get<T>(url, config)
    return response.data
  }

  static async post<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
    const response = await apiClient.post<T>(url, data, config)
    return response.data
  }

  static async put<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
    const response = await apiClient.put<T>(url, data, config)
    return response.data
  }

  static async patch<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
    const response = await apiClient.patch<T>(url, data, config)
    return response.data
  }

  static async delete<T = any>(url: string, config?: AxiosRequestConfig): Promise<T> {
    const response = await apiClient.delete<T>(url, config)
    return response.data
  }
}

export default apiClient
export { apiClient }