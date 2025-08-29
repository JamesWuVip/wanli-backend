/**
 * API响应基础接口
 */
export interface ApiResponse<T = any> {
  success: boolean
  data: T
  message?: string
  code?: number
}

/**
 * 分页响应接口
 */
export interface PaginatedResponse<T = any> {
  success: boolean
  data: T[]
  pagination: {
    page: number
    pageSize: number
    total: number
    totalPages: number
  }
  message?: string
  code?: number
}

/**
 * 错误响应接口
 */
export interface ErrorResponse {
  success: false
  message: string
  code?: number
  errors?: Record<string, string[]>
}

/**
 * 通用列表响应接口
 */
export interface ListResponse<T = any> {
  success: boolean
  data: T[]
  total?: number
  message?: string
  code?: number
}

export * from './auth'
export * from './course'
export * from './lesson'
export * from './admin'