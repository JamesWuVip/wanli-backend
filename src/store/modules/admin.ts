import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { adminApi } from '@/api/modules/admin'
import type {
  Store,
  CreateStoreRequest,
  UpdateStoreRequest,
  StoreListQuery,
  StoreStats,
  GlobalStats,
  StoreRanking,
  CourseUsageStats,
  SystemStatus,
  ActivityLog,
  ActivityLogQuery,
  CourseAuthorization,
  CourseAuthorizationQuery,
  CreateCourseAuthorizationRequest,
  RevokeCourseAuthorizationRequest,
  StoreManagerAssignment,
  AssignStoreManagerRequest
} from '@/types/api'

/**
 * 系统管理员状态管理
 */
export const useAdminStore = defineStore('admin', () => {
  // ==================== 状态定义 ====================
  
  // 仪表盘数据
  const globalStats = ref<GlobalStats | null>(null)
  const storeRankings = ref<StoreRanking[]>([])
  const courseUsageStats = ref<CourseUsageStats[]>([])
  const systemStatus = ref<SystemStatus | null>(null)
  const activityLogs = ref<ActivityLog[]>([])
  
  // 门店管理
  const stores = ref<Store[]>([])
  const storeStats = ref<Record<string, StoreStats>>({})
  const currentStore = ref<Store | null>(null)
  const storeManagerAssignments = ref<StoreManagerAssignment[]>([])
  
  // 课程授权
  const courseAuthorizations = ref<CourseAuthorization[]>([])
  const authorizedStores = ref<Record<string, Store[]>>({})
  const authorizedCourses = ref<Record<string, any[]>>({})
  
  // 加载状态
  const loading = ref({
    dashboard: false,
    stores: false,
    courseAuth: false,
    storeManagers: false
  })
  
  // 错误状态
  const errors = ref<Record<string, string | null>>({})
  
  // ==================== 计算属性 ====================
  
  const totalStores = computed(() => stores.value.length)
  const activeStores = computed(() => stores.value.filter(store => store.status === 'active').length)
  const totalRevenue = computed(() => {
    return Object.values(storeStats.value).reduce((sum, stats) => sum + (stats.totalRevenue || 0), 0)
  })
  
  const topPerformingStores = computed(() => {
    return storeRankings.value.slice(0, 5)
  })
  
  // ==================== 仪表盘相关操作 ====================
  
  /**
   * 加载仪表盘数据
   */
  const loadDashboardData = async () => {
    loading.value.dashboard = true
    errors.value.dashboard = null
    
    try {
      const [statsData, rankingsData, usageData, statusData] = await Promise.all([
        adminApi.getGlobalStats(),
        adminApi.getStoreRankings(10),
        adminApi.getCourseUsageStats(10),
        adminApi.getSystemStatus()
      ])
      
      globalStats.value = statsData
      storeRankings.value = rankingsData
      courseUsageStats.value = usageData
      systemStatus.value = statusData
    } catch (error: any) {
      errors.value.dashboard = error.message || '加载仪表盘数据失败'
      console.error('Failed to load dashboard data:', error)
    } finally {
      loading.value.dashboard = false
    }
  }
  
  /**
   * 加载活动日志
   */
  const loadActivityLogs = async (query: ActivityLogQuery = {}) => {
    try {
      const response = await adminApi.getActivityLogs(query)
      activityLogs.value = response.data
      return response
    } catch (error: any) {
      errors.value.activityLogs = error.message || '加载活动日志失败'
      throw error
    }
  }
  
  // ==================== 门店管理相关操作 ====================
  
  /**
   * 加载门店列表
   */
  const loadStores = async (query: StoreListQuery = {}) => {
    loading.value.stores = true
    errors.value.stores = null
    
    try {
      const response = await adminApi.getStores(query)
      stores.value = response.stores
      return response
    } catch (error: any) {
      errors.value.stores = error.message || '加载门店列表失败'
      throw error
    } finally {
      loading.value.stores = false
    }
  }
  
  /**
   * 获取门店详情
   */
  const getStore = async (storeId: string) => {
    try {
      const store = await adminApi.getStore(storeId)
      currentStore.value = store
      return store
    } catch (error: any) {
      errors.value.storeDetail = error.message || '获取门店详情失败'
      throw error
    }
  }
  
  /**
   * 创建门店
   */
  const createStore = async (data: CreateStoreRequest) => {
    try {
      const newStore = await adminApi.createStore(data)
      stores.value.push(newStore)
      return newStore
    } catch (error: any) {
      errors.value.createStore = error.message || '创建门店失败'
      throw error
    }
  }
  
  /**
   * 更新门店
   */
  const updateStore = async (data: UpdateStoreRequest) => {
    try {
      const updatedStore = await adminApi.updateStore(data)
      const index = stores.value.findIndex(store => store.id === data.id)
      if (index !== -1) {
        stores.value[index] = updatedStore
      }
      if (currentStore.value?.id === data.id) {
        currentStore.value = updatedStore
      }
      return updatedStore
    } catch (error: any) {
      errors.value.updateStore = error.message || '更新门店失败'
      throw error
    }
  }
  
  /**
   * 删除门店
   */
  const deleteStore = async (storeId: string) => {
    try {
      await adminApi.deleteStore(storeId)
      stores.value = stores.value.filter(store => store.id !== storeId)
      if (currentStore.value?.id === storeId) {
        currentStore.value = null
      }
    } catch (error: any) {
      errors.value.deleteStore = error.message || '删除门店失败'
      throw error
    }
  }
  
  /**
   * 加载门店统计数据
   */
  const loadStoreStats = async (storeIds?: string[]) => {
    try {
      const response = await adminApi.getStoreStats(storeIds)
      // 更新门店统计数据
      Object.entries(response.storeStats).forEach(([storeId, stats]) => {
        storeStats.value[storeId] = stats
      })
      return response
    } catch (error: any) {
      errors.value.storeStats = error.message || '加载门店统计数据失败'
      throw error
    }
  }
  
  // ==================== 门店管理员分配相关操作 ====================
  
  /**
   * 加载门店管理员分配列表
   */
  const loadStoreManagerAssignments = async () => {
    loading.value.storeManagers = true
    errors.value.storeManagers = null
    
    try {
      const assignments = await adminApi.getStoreManagerAssignments()
      storeManagerAssignments.value = assignments
      return assignments
    } catch (error: any) {
      errors.value.storeManagers = error.message || '加载门店管理员分配失败'
      throw error
    } finally {
      loading.value.storeManagers = false
    }
  }
  
  /**
   * 分配门店管理员
   */
  const assignStoreManager = async (data: AssignStoreManagerRequest) => {
    try {
      const assignment = await adminApi.assignStoreManager(data)
      storeManagerAssignments.value.push(assignment)
      return assignment
    } catch (error: any) {
      errors.value.assignManager = error.message || '分配门店管理员失败'
      throw error
    }
  }
  
  /**
   * 取消门店管理员分配
   */
  const unassignStoreManager = async (assignmentId: string) => {
    try {
      await adminApi.unassignStoreManager(assignmentId)
      storeManagerAssignments.value = storeManagerAssignments.value.filter(
        assignment => assignment.id !== assignmentId
      )
    } catch (error: any) {
      errors.value.unassignManager = error.message || '取消门店管理员分配失败'
      throw error
    }
  }
  
  // ==================== 课程授权相关操作 ====================
  
  /**
   * 加载课程授权列表
   */
  const loadCourseAuthorizations = async (query: CourseAuthorizationQuery = {}) => {
    loading.value.courseAuth = true
    errors.value.courseAuth = null
    
    try {
      const response = await adminApi.getCourseAuthorizations(query)
      courseAuthorizations.value = response.authorizations
      return response
    } catch (error: any) {
      errors.value.courseAuth = error.message || '加载课程授权列表失败'
      throw error
    } finally {
      loading.value.courseAuth = false
    }
  }
  
  /**
   * 创建课程授权
   */
  const createCourseAuthorization = async (data: CreateCourseAuthorizationRequest) => {
    try {
      const newAuthorizations = await adminApi.createCourseAuthorization(data)
      courseAuthorizations.value.push(...newAuthorizations)
      return newAuthorizations
    } catch (error: any) {
      errors.value.createAuth = error.message || '创建课程授权失败'
      throw error
    }
  }
  
  /**
   * 撤销课程授权
   */
  const revokeCourseAuthorization = async (data: RevokeCourseAuthorizationRequest) => {
    try {
      await adminApi.revokeCourseAuthorization(data)
      // 从列表中移除被撤销的授权
      courseAuthorizations.value = courseAuthorizations.value.filter(auth => {
        return !(data.courseIds.includes(auth.courseId) && data.storeIds.includes(auth.storeId))
      })
    } catch (error: any) {
      errors.value.revokeAuth = error.message || '撤销课程授权失败'
      throw error
    }
  }
  
  /**
   * 获取课程的授权门店列表
   */
  const getCourseAuthorizedStores = async (courseId: string) => {
    try {
      const stores = await adminApi.getCourseAuthorizedStores(courseId)
      authorizedStores.value[courseId] = stores
      return stores
    } catch (error: any) {
      errors.value.authorizedStores = error.message || '获取课程授权门店失败'
      throw error
    }
  }
  
  /**
   * 获取门店的授权课程列表
   */
  const getStoreAuthorizedCourses = async (storeId: string) => {
    try {
      const courses = await adminApi.getStoreAuthorizedCourses(storeId)
      authorizedCourses.value[storeId] = courses
      return courses
    } catch (error: any) {
      errors.value.authorizedCourses = error.message || '获取门店授权课程失败'
      throw error
    }
  }
  
  // ==================== 工具方法 ====================
  
  /**
   * 清除错误状态
   */
  const clearError = (key: string) => {
    errors.value[key] = null
  }
  
  /**
   * 清除所有错误
   */
  const clearAllErrors = () => {
    errors.value = {}
  }
  
  /**
   * 重置状态
   */
  const resetState = () => {
    globalStats.value = null
    storeRankings.value = []
    courseUsageStats.value = []
    systemStatus.value = null
    activityLogs.value = []
    stores.value = []
    storeStats.value = {}
    currentStore.value = null
    storeManagerAssignments.value = []
    courseAuthorizations.value = []
    authorizedStores.value = {}
    authorizedCourses.value = {}
    loading.value = {
      dashboard: false,
      stores: false,
      courseAuth: false,
      storeManagers: false
    }
    errors.value = {}
  }
  
  return {
    // 状态
    globalStats,
    storeRankings,
    courseUsageStats,
    systemStatus,
    activityLogs,
    stores,
    storeStats,
    currentStore,
    storeManagerAssignments,
    courseAuthorizations,
    authorizedStores,
    authorizedCourses,
    loading,
    errors,
    
    // 计算属性
    totalStores,
    activeStores,
    totalRevenue,
    topPerformingStores,
    
    // 仪表盘操作
    loadDashboardData,
    loadActivityLogs,
    
    // 门店管理操作
    loadStores,
    getStore,
    createStore,
    updateStore,
    deleteStore,
    loadStoreStats,
    
    // 门店管理员操作
    loadStoreManagerAssignments,
    assignStoreManager,
    unassignStoreManager,
    
    // 课程授权操作
    loadCourseAuthorizations,
    createCourseAuthorization,
    revokeCourseAuthorization,
    getCourseAuthorizedStores,
    getStoreAuthorizedCourses,
    
    // 工具方法
    clearError,
    clearAllErrors,
    resetState
  }
})

export default useAdminStore