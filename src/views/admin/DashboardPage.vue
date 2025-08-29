<template>
  <AppLayout>
    <div class="space-y-6">
      <!-- 页面标题 -->
      <div class="flex justify-between items-center">
        <div>
          <h1 class="text-3xl font-bold text-gray-900">系统管理员仪表盘</h1>
          <p class="mt-1 text-sm text-gray-500">欢迎回来，管理员</p>
        </div>
        <div class="flex items-center space-x-4">
          <button
            @click="refreshData"
            :disabled="loading"
            class="inline-flex items-center px-4 py-2 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-50"
          >
            <RefreshCw :class="['w-4 h-4 mr-2', loading && 'animate-spin']" />
            刷新数据
          </button>
        </div>
      </div>

      <!-- 错误提示 -->
      <div v-if="error" class="bg-red-50 border border-red-200 rounded-md p-4">
        <div class="flex">
          <AlertCircle class="h-5 w-5 text-red-400" />
          <div class="ml-3">
            <h3 class="text-sm font-medium text-red-800">加载数据时出错</h3>
            <div class="mt-2 text-sm text-red-700">
              <p>{{ error }}</p>
            </div>
          </div>
        </div>
      </div>

      <!-- 全局统计卡片 -->
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
      <StatCard
        title="总门店数"
        :value="globalStats?.totalStores || 0"
        :change="globalStats?.storeGrowth || 0"
        icon="Store"
        color="blue"
        :loading="loading"
      />
      <StatCard
        title="总课程数"
        :value="globalStats?.totalCourses || 0"
        :change="globalStats?.courseGrowth || 0"
        icon="BookOpen"
        color="green"
        :loading="loading"
      />
      <StatCard
        title="总学生数"
        :value="globalStats?.totalStudents || 0"
        :change="globalStats?.studentGrowth || 0"
        icon="Users"
        color="purple"
        :loading="loading"
      />
      <StatCard
        title="总收入"
        :value="formatCurrency(globalStats?.totalRevenue || 0)"
        :change="globalStats?.revenueGrowth || 0"
        icon="DollarSign"
        color="orange"
        :loading="loading"
      />
      </div>

      <!-- 门店业绩排行和热门课程 -->
      <div class="grid grid-cols-1 lg:grid-cols-2 gap-8">
      <!-- 门店业绩排行 -->
      <div class="bg-white rounded-lg shadow p-6">
        <div class="flex items-center justify-between mb-4">
          <h2 class="text-lg font-semibold text-gray-900">门店业绩排行</h2>
          <TrendingUp class="w-5 h-5 text-gray-400" />
        </div>
        
        <div v-if="loading" class="space-y-3">
          <div v-for="i in 5" :key="i" class="animate-pulse">
            <div class="flex items-center space-x-3">
              <div class="w-8 h-8 bg-gray-200 rounded-full"></div>
              <div class="flex-1">
                <div class="h-4 bg-gray-200 rounded w-3/4 mb-2"></div>
                <div class="h-3 bg-gray-200 rounded w-1/2"></div>
              </div>
            </div>
          </div>
        </div>
        
        <div v-else-if="storeRankings.length === 0" class="text-center py-8 text-gray-500">
          <Store class="w-12 h-12 mx-auto mb-3 text-gray-300" />
          <p>暂无门店数据</p>
        </div>
        
        <div v-else class="space-y-3">
          <div
            v-for="(store, index) in storeRankings"
            :key="store.id"
            class="flex items-center space-x-3 p-3 rounded-lg hover:bg-gray-50 transition-colors"
          >
            <div class="flex-shrink-0">
              <div
                :class="[
                  'w-8 h-8 rounded-full flex items-center justify-center text-sm font-semibold text-white',
                  index === 0 ? 'bg-yellow-500' : index === 1 ? 'bg-gray-400' : index === 2 ? 'bg-orange-500' : 'bg-blue-500'
                ]"
              >
                {{ index + 1 }}
              </div>
            </div>
            <div class="flex-1 min-w-0">
              <p class="text-sm font-medium text-gray-900 truncate">{{ store.name }}</p>
              <p class="text-sm text-gray-500">{{ formatCurrency(store.revenue) }}</p>
            </div>
            <div class="flex-shrink-0">
              <span
                :class="[
                  'inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium',
                  store.growth >= 0 ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'
                ]"
              >
                {{ store.growth >= 0 ? '+' : '' }}{{ store.growth }}%
              </span>
            </div>
          </div>
        </div>
      </div>

      <!-- 课程使用情况 -->
      <div class="bg-white rounded-lg shadow p-6">
        <div class="flex items-center justify-between mb-4">
          <h2 class="text-lg font-semibold text-gray-900">热门课程</h2>
          <BookOpen class="w-5 h-5 text-gray-400" />
        </div>
        
        <div v-if="loading" class="space-y-3">
          <div v-for="i in 5" :key="i" class="animate-pulse">
            <div class="flex items-center space-x-3">
              <div class="w-10 h-10 bg-gray-200 rounded"></div>
              <div class="flex-1">
                <div class="h-4 bg-gray-200 rounded w-3/4 mb-2"></div>
                <div class="h-3 bg-gray-200 rounded w-1/2"></div>
              </div>
            </div>
          </div>
        </div>
        
        <div v-else-if="courseUsageStats.length === 0" class="text-center py-8 text-gray-500">
          <BookOpen class="w-12 h-12 mx-auto mb-3 text-gray-300" />
          <p>暂无课程数据</p>
        </div>
        
        <div v-else class="space-y-4">
          <div
            v-for="course in courseUsageStats.slice(0, 5)"
            :key="course.id"
            class="flex items-center space-x-3"
          >
            <div class="flex-shrink-0">
              <div class="w-10 h-10 bg-gray-200 rounded flex items-center justify-center">
                <BookOpen class="w-5 h-5 text-gray-400" />
              </div>
            </div>
            <div class="flex-1 min-w-0">
              <p class="text-sm font-medium text-gray-900 truncate">{{ course.name }}</p>
              <div class="flex items-center space-x-4 text-xs text-gray-500">
                <span>{{ course.students }} 学生</span>
                <span>{{ course.completion }}% 完成率</span>
              </div>
            </div>
            <div class="flex-shrink-0">
              <div class="w-16 bg-gray-200 rounded-full h-2">
                <div
                  class="bg-blue-600 h-2 rounded-full transition-all duration-300"
                  :style="{ width: `${Math.min(course.completion, 100)}%` }"
                ></div>
              </div>
            </div>
          </div>
        </div>
      </div>
      </div>

      <!-- 系统状态和最近活动 -->
      <div class="grid grid-cols-1 lg:grid-cols-2 gap-8">
      <!-- 系统运行状态 -->
      <div class="bg-white rounded-lg shadow p-6">
        <div class="flex items-center justify-between mb-4">
          <h2 class="text-lg font-semibold text-gray-900">系统状态</h2>
          <Activity class="w-5 h-5 text-gray-400" />
        </div>
        
        <div v-if="loading" class="space-y-3">
          <div v-for="i in 4" :key="i" class="animate-pulse">
            <div class="flex justify-between items-center">
              <div class="h-4 bg-gray-200 rounded w-1/2"></div>
              <div class="h-6 bg-gray-200 rounded w-16"></div>
            </div>
          </div>
        </div>
        
        <div v-else-if="systemStatus" class="space-y-4">
          <div class="flex justify-between items-center">
            <span class="text-sm text-gray-600">API服务</span>
            <StatusBadge :status="systemStatus.apiStatus" />
          </div>
          <div class="flex justify-between items-center">
            <span class="text-sm text-gray-600">数据库</span>
            <StatusBadge :status="systemStatus.databaseStatus" />
          </div>
          <div class="flex justify-between items-center">
            <span class="text-sm text-gray-600">缓存服务</span>
            <StatusBadge :status="systemStatus.cacheStatus" />
          </div>
          <div class="flex justify-between items-center">
            <span class="text-sm text-gray-600">文件存储</span>
            <StatusBadge :status="systemStatus.storageStatus" />
          </div>
          
          <div class="pt-4 border-t border-gray-200">
            <div class="flex justify-between items-center text-sm">
              <span class="text-gray-600">CPU使用率</span>
              <span class="font-medium">{{ systemStatus.cpuUsage }}%</span>
            </div>
            <div class="w-full bg-gray-200 rounded-full h-2 mt-1">
              <div
                class="bg-blue-600 h-2 rounded-full transition-all duration-300"
                :style="{ width: `${systemStatus.cpuUsage}%` }"
              ></div>
            </div>
          </div>
          
          <div class="flex justify-between items-center text-sm">
            <span class="text-gray-600">内存使用率</span>
            <span class="font-medium">{{ systemStatus.memoryUsage }}%</span>
          </div>
          <div class="w-full bg-gray-200 rounded-full h-2 mt-1">
            <div
              class="bg-green-600 h-2 rounded-full transition-all duration-300"
              :style="{ width: `${systemStatus.memoryUsage}%` }"
            ></div>
          </div>
        </div>
      </div>

      <!-- 最近活动 -->
      <div class="bg-white rounded-lg shadow p-6">
        <div class="flex items-center justify-between mb-4">
          <h2 class="text-lg font-semibold text-gray-900">最近活动</h2>
          <button
            @click="loadRecentActivities"
            class="text-sm text-blue-600 hover:text-blue-800 font-medium"
          >
            查看全部
          </button>
        </div>
        
        <div v-if="loadingActivities" class="space-y-3">
          <div v-for="i in 5" :key="i" class="animate-pulse">
            <div class="flex space-x-3">
              <div class="w-8 h-8 bg-gray-200 rounded-full"></div>
              <div class="flex-1">
                <div class="h-4 bg-gray-200 rounded w-3/4 mb-2"></div>
                <div class="h-3 bg-gray-200 rounded w-1/2"></div>
              </div>
            </div>
          </div>
        </div>
        
        <div v-else-if="recentActivities.length === 0" class="text-center py-8 text-gray-500">
          <Activity class="w-12 h-12 mx-auto mb-3 text-gray-300" />
          <p>暂无活动记录</p>
        </div>
        
        <div v-else class="space-y-4">
          <div
            v-for="activity in recentActivities"
            :key="activity.id"
            class="flex space-x-3"
          >
            <div class="flex-shrink-0">
              <div
                :class="[
                  'w-8 h-8 rounded-full flex items-center justify-center',
                  getActivityIconClass(activity.type)
                ]"
              >
                <component :is="getActivityIcon(activity.type)" class="w-4 h-4" />
              </div>
            </div>
            <div class="flex-1 min-w-0">
              <p class="text-sm text-gray-900">{{ activity.description }}</p>
              <div class="flex items-center space-x-2 text-xs text-gray-500">
                <span>{{ activity.userName }}</span>
                <span>•</span>
                <span>{{ formatTime(activity.createdAt) }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
      </div>
    </div>
  </AppLayout>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import {
  Users,
  BookOpen,
  GraduationCap,
  DollarSign,
  TrendingUp,
  TrendingDown,
  RefreshCw,
  AlertCircle,
  Activity,
  Server,
  Database,
  Wifi,
  UserPlus,
  Store,
  Settings,
  FileText,
  Shield
} from 'lucide-vue-next'
import AppLayout from '@/components/layout/AppLayout.vue'
import StatCard from '@/components/common/StatCard.vue'
import StatusBadge from '@/components/common/StatusBadge.vue'

// 响应式数据
const loading = ref(false)
const error = ref('')

const loadingActivities = ref(false)
const recentActivities = ref([])

// 模拟数据
const globalStats = ref({
  totalStores: 25,
  totalCourses: 156,
  totalStudents: 2847,
  totalRevenue: 1250000
})

const storeRankings = ref([
  { id: 1, name: '北京朝阳店', revenue: 125000, growth: 12.5 },
  { id: 2, name: '上海浦东店', revenue: 118000, growth: 8.3 },
  { id: 3, name: '深圳南山店', revenue: 95000, growth: -2.1 }
])

const courseUsageStats = ref([
  { id: 1, name: 'JavaScript基础', students: 245, completion: 87 },
  { id: 2, name: 'Vue.js实战', students: 189, completion: 92 },
  { id: 3, name: 'Node.js开发', students: 156, completion: 78 }
])

const systemStatus = ref({
  cpuUsage: 45,
  memoryUsage: 68,
  diskUsage: 32,
  networkStatus: 'normal',
  apiStatus: 'normal',
  databaseStatus: 'normal',
  cacheStatus: 'normal',
  storageStatus: 'normal'
})

// 方法
const refreshData = async () => {
  loading.value = true
  try {
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 1000))
    // 这里可以添加实际的API调用
    error.value = ''
  } catch (err) {
    error.value = '刷新数据失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

const loadRecentActivities = async () => {
  loadingActivities.value = true
  try {
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 500))
    recentActivities.value = [
      {
        id: 1,
        type: 'user_login',
        description: '用户张三登录系统',
        userName: '张三',
        createdAt: new Date(Date.now() - 5 * 60 * 1000).toISOString()
      },
      {
        id: 2,
        type: 'course_create',
        description: '创建了新课程《React进阶》',
        userName: '李老师',
        createdAt: new Date(Date.now() - 15 * 60 * 1000).toISOString()
      },
      {
        id: 3,
        type: 'user_register',
        description: '新用户王五注册',
        userName: '系统',
        createdAt: new Date(Date.now() - 30 * 60 * 1000).toISOString()
      }
    ]
  } catch (error) {
    console.error('Failed to load recent activities:', error)
  } finally {
    loadingActivities.value = false
  }
}

const formatCurrency = (amount: number): string => {
  return new Intl.NumberFormat('zh-CN', {
    style: 'currency',
    currency: 'CNY',
    minimumFractionDigits: 0,
    maximumFractionDigits: 0
  }).format(amount)
}

const formatTime = (timestamp: string): string => {
  const date = new Date(timestamp)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  
  const minutes = Math.floor(diff / (1000 * 60))
  const hours = Math.floor(diff / (1000 * 60 * 60))
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))
  
  if (minutes < 60) {
    return `${minutes}分钟前`
  } else if (hours < 24) {
    return `${hours}小时前`
  } else if (days < 7) {
    return `${days}天前`
  } else {
    return date.toLocaleDateString('zh-CN')
  }
}

const getActivityIcon = (type: string) => {
  const iconMap: Record<string, any> = {
    user_login: Users,
    user_register: UserPlus,
    course_create: BookOpen,
    store_create: Store,
    system_config: Settings,
    data_export: FileText,
    security_alert: Shield
  }
  return iconMap[type] || Activity
}

const getActivityIconClass = (type: string): string => {
  const classMap: Record<string, string> = {
    user_login: 'bg-blue-100 text-blue-600',
    user_register: 'bg-green-100 text-green-600',
    course_create: 'bg-purple-100 text-purple-600',
    store_create: 'bg-orange-100 text-orange-600',
    system_config: 'bg-gray-100 text-gray-600',
    data_export: 'bg-indigo-100 text-indigo-600',
    security_alert: 'bg-red-100 text-red-600'
  }
  return classMap[type] || 'bg-gray-100 text-gray-600'
}

// 生命周期
onMounted(async () => {
  await Promise.all([
    refreshData(),
    loadRecentActivities()
  ])
})
</script>

<style scoped>
.admin-dashboard {
  min-height: calc(100vh - 4rem);
}
</style>