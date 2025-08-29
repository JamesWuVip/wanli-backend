import apiClient from '../index'
import type {
  // 门店相关
  Store,
  CreateStoreRequest,
  UpdateStoreRequest,
  StoreListResponse,
  StoreListQuery,
  StoreStats,
  StoreStatsResponse,
  
  // 统计数据
  GlobalStats,
  DashboardDataResponse,
  StoreRanking,
  CourseUsageStats,
  SystemStatus,
  ActivityLog,
  ActivityLogQuery,
  
  // 课程授权
  CourseAuthorization,
  CourseAuthorizationListResponse,
  CourseAuthorizationQuery,
  CreateCourseAuthorizationRequest,
  RevokeCourseAuthorizationRequest,
  
  // 门店管理员分配
  StoreManagerAssignment,
  AssignStoreManagerRequest,
  
  ApiResponse,
  PaginatedResponse
} from '@/types/api'

/**
 * 系统管理员API服务
 */
export const adminApi = {
  // ==================== 仪表盘数据 ====================
  
  /**
   * 获取管理员仪表盘数据
   */
  async getDashboardData(): Promise<DashboardDataResponse> {
    const response = await apiClient.get<ApiResponse<DashboardDataResponse>>('/admin/dashboard')
    return response.data.data
  },
  
  /**
   * 获取全局统计数据
   */
  async getGlobalStats(): Promise<GlobalStats> {
    const response = await apiClient.get<ApiResponse<GlobalStats>>('/admin/stats/global')
    return response.data.data
  },
  
  /**
   * 获取门店业绩排行
   */
  async getStoreRankings(limit = 10): Promise<StoreRanking[]> {
    const response = await apiClient.get<ApiResponse<StoreRanking[]>>('/admin/stats/store-rankings', {
      params: { limit }
    })
    return response.data.data
  },
  
  /**
   * 获取课程使用情况统计
   */
  async getCourseUsageStats(limit = 10): Promise<CourseUsageStats[]> {
    const response = await apiClient.get<ApiResponse<CourseUsageStats[]>>('/admin/stats/course-usage', {
      params: { limit }
    })
    return response.data.data
  },
  
  /**
   * 获取系统运行状态
   */
  async getSystemStatus(): Promise<SystemStatus> {
    const response = await apiClient.get<ApiResponse<SystemStatus>>('/admin/system/status')
    return response.data.data
  },
  
  /**
   * 获取活动日志
   */
  async getActivityLogs(query: ActivityLogQuery = {}): Promise<PaginatedResponse<ActivityLog>> {
    const response = await apiClient.get<PaginatedResponse<ActivityLog>>('/admin/logs/activities', {
      params: query
    })
    return response.data
  },
  
  // ==================== 门店管理 ====================
  
  /**
   * 获取门店列表
   */
  async getStores(query: StoreListQuery = {}): Promise<StoreListResponse> {
    const response = await apiClient.get<ApiResponse<StoreListResponse>>('/admin/stores', {
      params: query
    })
    return response.data.data
  },
  
  /**
   * 获取门店详情
   */
  async getStore(storeId: string): Promise<Store> {
    const response = await apiClient.get<ApiResponse<Store>>(`/admin/stores/${storeId}`)
    return response.data.data
  },
  
  /**
   * 创建门店
   */
  async createStore(data: CreateStoreRequest): Promise<Store> {
    const response = await apiClient.post<ApiResponse<Store>>('/admin/stores', data)
    return response.data.data
  },
  
  /**
   * 更新门店信息
   */
  async updateStore(data: UpdateStoreRequest): Promise<Store> {
    const response = await apiClient.put<ApiResponse<Store>>(`/admin/stores/${data.id}`, data)
    return response.data.data
  },
  
  /**
   * 删除门店
   */
  async deleteStore(storeId: string): Promise<void> {
    await apiClient.delete(`/admin/stores/${storeId}`)
  },
  
  /**
   * 获取门店统计数据
   */
  async getStoreStats(storeIds?: string[]): Promise<StoreStatsResponse> {
    const response = await apiClient.get<ApiResponse<StoreStatsResponse>>('/admin/stores/stats', {
      params: storeIds ? { storeIds: storeIds.join(',') } : {}
    })
    return response.data.data
  },
  
  // ==================== 门店管理员分配 ====================
  
  /**
   * 获取门店管理员分配列表
   */
  async getStoreManagerAssignments(): Promise<StoreManagerAssignment[]> {
    const response = await apiClient.get<ApiResponse<StoreManagerAssignment[]>>('/admin/store-managers')
    return response.data.data
  },
  
  /**
   * 分配门店管理员
   */
  async assignStoreManager(data: AssignStoreManagerRequest): Promise<StoreManagerAssignment> {
    const response = await apiClient.post<ApiResponse<StoreManagerAssignment>>('/admin/store-managers', data)
    return response.data.data
  },
  
  /**
   * 取消门店管理员分配
   */
  async unassignStoreManager(assignmentId: string): Promise<void> {
    await apiClient.delete(`/admin/store-managers/${assignmentId}`)
  },
  
  // ==================== 课程授权管理 ====================
  
  /**
   * 获取课程授权列表
   */
  async getCourseAuthorizations(query: CourseAuthorizationQuery = {}): Promise<CourseAuthorizationListResponse> {
    const response = await apiClient.get<ApiResponse<CourseAuthorizationListResponse>>('/admin/course-authorizations', {
      params: query
    })
    return response.data.data
  },
  
  /**
   * 创建课程授权
   */
  async createCourseAuthorization(data: CreateCourseAuthorizationRequest): Promise<CourseAuthorization[]> {
    const response = await apiClient.post<ApiResponse<CourseAuthorization[]>>('/admin/course-authorizations', data)
    return response.data.data
  },
  
  /**
   * 撤销课程授权
   */
  async revokeCourseAuthorization(data: RevokeCourseAuthorizationRequest): Promise<void> {
    await apiClient.post('/admin/course-authorizations/revoke', data)
  },
  
  /**
   * 获取课程的授权门店列表
   */
  async getCourseAuthorizedStores(courseId: string): Promise<Store[]> {
    const response = await apiClient.get<ApiResponse<Store[]>>(`/admin/courses/${courseId}/authorized-stores`)
    return response.data.data
  },
  
  /**
   * 获取门店的授权课程列表
   */
  async getStoreAuthorizedCourses(storeId: string): Promise<any[]> {
    const response = await apiClient.get<ApiResponse<any[]>>(`/admin/stores/${storeId}/authorized-courses`)
    return response.data.data
  },
  
  // ==================== 课程管理（系统管理员专用） ====================
  
  /**
   * 获取所有课程（包含授权信息）
   */
  async getAllCoursesWithAuth(query: any = {}): Promise<any> {
    const response = await apiClient.get<ApiResponse<any>>('/admin/courses', {
      params: query
    })
    return response.data.data
  },
  
  /**
   * 批量授权课程给门店
   */
  async batchAuthorizeCourses(courseIds: string[], storeIds: string[]): Promise<void> {
    await apiClient.post('/admin/courses/batch-authorize', {
      courseIds,
      storeIds
    })
  },
  
  /**
   * 批量撤销课程授权
   */
  async batchRevokeCourseAuth(courseIds: string[], storeIds: string[]): Promise<void> {
    await apiClient.post('/admin/courses/batch-revoke', {
      courseIds,
      storeIds
    })
  },
  
  // ==================== 用户管理 ====================
  
  /**
   * 获取所有用户列表（分页）
   */
  async getAllUsers(query: any = {}): Promise<any> {
    const response = await apiClient.get<ApiResponse<any>>('/admin/users', {
      params: query
    })
    return response.data.data
  },
  
  /**
   * 创建门店管理员账号
   */
  async createStoreManager(data: any): Promise<any> {
    const response = await apiClient.post<ApiResponse<any>>('/admin/users/store-managers', data)
    return response.data.data
  },
  
  /**
   * 禁用/启用用户账号
   */
  async toggleUserStatus(userId: string, status: 'active' | 'inactive'): Promise<void> {
    await apiClient.patch(`/admin/users/${userId}/status`, { status })
  },
  
  /**
   * 重置用户密码
   */
  async resetUserPassword(userId: string): Promise<{ temporaryPassword: string }> {
    const response = await apiClient.post<ApiResponse<{ temporaryPassword: string }>>(`/admin/users/${userId}/reset-password`)
    return response.data.data
  }
}

export default adminApi