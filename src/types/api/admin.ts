// 系统管理员相关的API类型定义

// 管理员课程相关类型（扩展自基础Course类型）
export interface AdminCourseStats {
  authorizedStores: number
  totalStudents: number
  completionRate: number
}

// 门店相关类型
export interface Store {
  id: string
  name: string
  address: string
  phone: string
  email: string
  managerId?: string
  managerName?: string
  status: 'active' | 'inactive'
  createdAt: string
  updatedAt: string
}

export interface CreateStoreRequest {
  name: string
  address: string
  phone: string
  email: string
  managerId?: string
}

export interface UpdateStoreRequest extends Partial<CreateStoreRequest> {
  id: string
}

// 门店统计数据
export interface StoreStats {
  storeId: string
  storeName: string
  totalClasses: number
  totalTeachers: number
  totalStudents: number
  totalCourses: number
  activeStudents: number
  completedClasses: number
  averageRating: number
  completionRate: number
  revenue: number
  monthlyRevenue: number
  totalRevenue: number
  lastMonthRevenue: number
  revenueGrowth: number
  growthRate: number
  lastActivityAt: string
}

// 全局统计数据
export interface GlobalStats {
  totalStores: number
  totalCourses: number
  totalStudents: number
  totalTeachers: number
  totalClasses: number
  activeUsers: number
  systemUptime: number
  avgCompletionRate: number
}

// 门店排名
export interface StoreRanking {
  rank: number
  storeId: string
  storeName: string
  score: number
  completionRate: number
  studentCount: number
  revenue: number
  growth: number
}

// 课程使用统计
export interface CourseUsageStats {
  courseId: string
  courseName: string
  totalEnrollments: number
  activeEnrollments: number
  completionRate: number
  avgScore: number
  authorizedStores: number
  lastAccessDate: string
}

// 系统状态
export interface SystemStatus {
  status: 'healthy' | 'warning' | 'error'
  uptime: number
  cpuUsage: number
  memoryUsage: number
  diskUsage: number
  activeConnections: number
  responseTime: number
  errorRate: number
  lastUpdated: string
}

// 活动日志
export interface ActivityLog {
  id: string
  userId: string
  userName: string
  userRole: string
  action: string
  resource: string
  resourceId?: string
  details: string
  ipAddress: string
  userAgent: string
  timestamp: string
}

// 课程授权
export interface CourseAuthorization {
  id: string
  courseId: string
  courseName: string
  storeId: string
  storeName: string
  authorizedAt: string
  authorizedBy: string
  expiresAt?: string
  status: 'active' | 'expired' | 'revoked'
}

export interface CreateCourseAuthorizationRequest {
  courseId: string
  storeIds: string[]
  expiresAt?: string
}

export interface RevokeCourseAuthorizationRequest {
  courseId: string
  storeId: string
  courseIds: string[]
  storeIds: string[]
}

// 门店管理员分配
export interface StoreManagerAssignment {
  id: string
  storeId: string
  storeName: string
  managerId: string
  managerName: string
  managerEmail: string
  assignedAt: string
  assignedBy: string
  status: 'active' | 'inactive'
}

export interface AssignStoreManagerRequest {
  storeId: string
  managerId: string
}

// API响应类型
export interface StoreListResponse {
  stores: Store[]
  total: number
  page: number
  size: number
}

export interface StoreStatsResponse {
  stats: Record<string, StoreStats>
  storeStats: Record<string, StoreStats>
  totalStores: number
  activeStores: number
  totalRevenue: number
  averageRating: number
}

export interface DashboardDataResponse {
  globalStats: GlobalStats
  storeRankings: StoreRanking[]
  courseUsageStats: CourseUsageStats[]
  systemStatus: SystemStatus
  recentActivities: ActivityLog[]
}

export interface CourseAuthorizationListResponse {
  authorizations: CourseAuthorization[]
  total: number
  page: number
  size: number
}

// 查询参数类型
export interface StoreListQuery {
  page?: number
  size?: number
  search?: string
  status?: 'active' | 'inactive'
  sortBy?: 'name' | 'createdAt' | 'studentCount'
  sortOrder?: 'asc' | 'desc'
}

export interface ActivityLogQuery {
  page?: number
  size?: number
  userId?: string
  action?: string
  resource?: string
  startDate?: string
  endDate?: string
}

export interface CourseAuthorizationQuery {
  page?: number
  size?: number
  courseId?: string
  storeId?: string
  status?: 'active' | 'expired' | 'revoked'
}