# 主仪表盘和数据可视化开发

## Issue描述
开发主仪表盘页面，实现数据统计、图表展示、快捷操作等功能，为用户提供直观的数据概览和操作入口。

## 任务清单

### 仪表盘布局
- [ ] 设计响应式仪表盘布局
- [ ] 实现可拖拽的卡片组件
- [ ] 开发栅格系统布局
- [ ] 添加布局个性化设置
- [ ] 实现布局配置保存
- [ ] 支持多种屏幕尺寸适配

### 数据统计卡片
- [ ] 课程总数统计卡片
- [ ] 学员总数统计卡片
- [ ] 收入统计卡片
- [ ] 活跃度统计卡片
- [ ] 增长趋势指示器
- [ ] 同比环比数据对比
- [ ] 实时数据更新

### 图表组件
- [ ] 折线图组件(趋势分析)
- [ ] 柱状图组件(数据对比)
- [ ] 饼图组件(占比分析)
- [ ] 面积图组件(累积数据)
- [ ] 热力图组件(活跃度)
- [ ] 仪表盘图组件(进度显示)
- [ ] 图表交互功能
- [ ] 图表数据导出

### 快捷操作区
- [ ] 快速创建课程入口
- [ ] 最近操作记录
- [ ] 待处理事项提醒
- [ ] 系统通知中心
- [ ] 常用功能快捷方式
- [ ] 个人工作台定制

### 数据表格
- [ ] 最新课程列表
- [ ] 热门课程排行
- [ ] 学员活跃排行
- [ ] 收入明细表格
- [ ] 表格排序和筛选
- [ ] 表格数据导出

### 实时监控
- [ ] 在线用户数监控
- [ ] 系统性能监控
- [ ] 错误日志监控
- [ ] 业务指标监控
- [ ] 告警通知系统
- [ ] 监控数据可视化

## 页面设计规范

### 仪表盘主页面
```vue
<!-- views/dashboard/DashboardView.vue -->
<template>
  <div class="dashboard-container p-6">
    <!-- 页面头部 -->
    <div class="flex justify-between items-center mb-6">
      <div>
        <h1 class="text-2xl font-bold text-gray-900">仪表盘</h1>
        <p class="text-gray-600">欢迎回来，{{ userStore.user?.fullName }}</p>
      </div>
      
      <div class="flex items-center space-x-4">
        <!-- 时间范围选择器 -->
        <AppSelect
          v-model="timeRange"
          :options="timeRangeOptions"
          class="w-32"
          @change="handleTimeRangeChange"
        />
        
        <!-- 刷新按钮 -->
        <AppButton
          variant="outline"
          size="sm"
          @click="refreshData"
          :loading="loading"
        >
          <ArrowPathIcon class="w-4 h-4" />
        </AppButton>
        
        <!-- 设置按钮 -->
        <AppButton
          variant="ghost"
          size="sm"
          @click="showSettings = true"
        >
          <Cog6ToothIcon class="w-4 h-4" />
        </AppButton>
      </div>
    </div>
    
    <!-- 统计卡片区域 -->
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
      <StatCard
        title="总课程数"
        :value="stats.totalCourses"
        :change="stats.coursesChange"
        icon="AcademicCapIcon"
        color="blue"
      />
      
      <StatCard
        title="总学员数"
        :value="stats.totalStudents"
        :change="stats.studentsChange"
        icon="UsersIcon"
        color="green"
      />
      
      <StatCard
        title="总收入"
        :value="formatCurrency(stats.totalRevenue)"
        :change="stats.revenueChange"
        icon="CurrencyDollarIcon"
        color="yellow"
      />
      
      <StatCard
        title="活跃用户"
        :value="stats.activeUsers"
        :change="stats.activeUsersChange"
        icon="ChartBarIcon"
        color="purple"
      />
    </div>
    
    <!-- 图表区域 -->
    <div class="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-8">
      <!-- 收入趋势图 -->
      <ChartCard title="收入趋势" class="lg:col-span-1">
        <LineChart
          :data="revenueChartData"
          :options="revenueChartOptions"
          height="300"
        />
      </ChartCard>
      
      <!-- 课程分类分布 -->
      <ChartCard title="课程分类分布">
        <PieChart
          :data="categoryChartData"
          :options="categoryChartOptions"
          height="300"
        />
      </ChartCard>
    </div>
    
    <!-- 数据表格区域 -->
    <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
      <!-- 热门课程 -->
      <DataCard title="热门课程" :loading="loading">
        <template #extra>
          <router-link to="/courses" class="text-sm text-primary-600 hover:text-primary-700">
            查看全部
          </router-link>
        </template>
        
        <div class="space-y-4">
          <div
            v-for="course in popularCourses"
            :key="course.id"
            class="flex items-center justify-between p-3 bg-gray-50 rounded-lg"
          >
            <div class="flex items-center space-x-3">
              <img
                :src="course.coverImage"
                :alt="course.title"
                class="w-12 h-12 rounded-lg object-cover"
              />
              <div>
                <h4 class="font-medium text-gray-900">{{ course.title }}</h4>
                <p class="text-sm text-gray-500">{{ course.studentCount }} 学员</p>
              </div>
            </div>
            
            <div class="text-right">
              <p class="font-medium text-gray-900">¥{{ course.price }}</p>
              <p class="text-sm text-green-600">+{{ course.recentEnrollments }}</p>
            </div>
          </div>
        </div>
      </DataCard>
      
      <!-- 最新学员 -->
      <DataCard title="最新学员" :loading="loading">
        <template #extra>
          <router-link to="/students" class="text-sm text-primary-600 hover:text-primary-700">
            查看全部
          </router-link>
        </template>
        
        <div class="space-y-4">
          <div
            v-for="student in recentStudents"
            :key="student.id"
            class="flex items-center justify-between"
          >
            <div class="flex items-center space-x-3">
              <AppAvatar
                :src="student.avatar"
                :name="student.fullName"
                size="sm"
              />
              <div>
                <h4 class="font-medium text-gray-900">{{ student.fullName }}</h4>
                <p class="text-sm text-gray-500">{{ formatDate(student.joinedAt) }}</p>
              </div>
            </div>
            
            <AppBadge
              :variant="student.status === 'active' ? 'success' : 'secondary'"
            >
              {{ student.status === 'active' ? '活跃' : '新用户' }}
            </AppBadge>
          </div>
        </div>
      </DataCard>
    </div>
    
    <!-- 设置弹窗 -->
    <DashboardSettings
      v-model:visible="showSettings"
      :layout="dashboardLayout"
      @save="handleLayoutSave"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { useDashboardStore } from '@/stores/dashboard'
import { formatCurrency, formatDate } from '@/utils/format'

const userStore = useUserStore()
const dashboardStore = useDashboardStore()

const loading = ref(false)
const showSettings = ref(false)
const timeRange = ref('7d')

const timeRangeOptions = [
  { label: '最近7天', value: '7d' },
  { label: '最近30天', value: '30d' },
  { label: '最近90天', value: '90d' },
  { label: '最近一年', value: '1y' }
]

const stats = computed(() => dashboardStore.stats)
const popularCourses = computed(() => dashboardStore.popularCourses)
const recentStudents = computed(() => dashboardStore.recentStudents)
const revenueChartData = computed(() => dashboardStore.revenueChartData)
const categoryChartData = computed(() => dashboardStore.categoryChartData)

const revenueChartOptions = {
  responsive: true,
  plugins: {
    legend: {
      display: false
    }
  },
  scales: {
    y: {
      beginAtZero: true,
      ticks: {
        callback: (value: number) => formatCurrency(value)
      }
    }
  }
}

const categoryChartOptions = {
  responsive: true,
  plugins: {
    legend: {
      position: 'bottom'
    }
  }
}

const handleTimeRangeChange = async () => {
  await refreshData()
}

const refreshData = async () => {
  loading.value = true
  try {
    await dashboardStore.fetchDashboardData(timeRange.value)
  } finally {
    loading.value = false
  }
}

const handleLayoutSave = (layout: DashboardLayout) => {
  dashboardStore.saveDashboardLayout(layout)
}

onMounted(() => {
  refreshData()
})
</script>
```

### 统计卡片组件
```vue
<!-- components/dashboard/StatCard.vue -->
<template>
  <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
    <div class="flex items-center justify-between">
      <div class="flex-1">
        <p class="text-sm font-medium text-gray-600 mb-1">{{ title }}</p>
        <p class="text-2xl font-bold text-gray-900">{{ displayValue }}</p>
        
        <div v-if="change !== undefined" class="flex items-center mt-2">
          <component
            :is="changeIcon"
            :class="changeClasses"
            class="w-4 h-4 mr-1"
          />
          <span :class="changeClasses" class="text-sm font-medium">
            {{ Math.abs(change) }}%
          </span>
          <span class="text-sm text-gray-500 ml-1">
            vs 上期
          </span>
        </div>
      </div>
      
      <div :class="iconBgClasses" class="p-3 rounded-lg">
        <component :is="iconComponent" class="w-6 h-6 text-white" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import {
  AcademicCapIcon,
  UsersIcon,
  CurrencyDollarIcon,
  ChartBarIcon,
  ArrowUpIcon,
  ArrowDownIcon
} from '@heroicons/vue/24/outline'

interface Props {
  title: string
  value: string | number
  change?: number
  icon: string
  color: 'blue' | 'green' | 'yellow' | 'purple' | 'red'
}

const props = defineProps<Props>()

const iconComponents = {
  AcademicCapIcon,
  UsersIcon,
  CurrencyDollarIcon,
  ChartBarIcon
}

const colorClasses = {
  blue: 'bg-blue-500',
  green: 'bg-green-500',
  yellow: 'bg-yellow-500',
  purple: 'bg-purple-500',
  red: 'bg-red-500'
}

const iconComponent = computed(() => iconComponents[props.icon as keyof typeof iconComponents])
const iconBgClasses = computed(() => colorClasses[props.color])

const displayValue = computed(() => {
  if (typeof props.value === 'number') {
    return props.value.toLocaleString()
  }
  return props.value
})

const changeIcon = computed(() => {
  if (props.change === undefined) return null
  return props.change >= 0 ? ArrowUpIcon : ArrowDownIcon
})

const changeClasses = computed(() => {
  if (props.change === undefined) return ''
  return props.change >= 0 ? 'text-green-600' : 'text-red-600'
})
</script>
```

### 图表卡片组件
```vue
<!-- components/dashboard/ChartCard.vue -->
<template>
  <div class="bg-white rounded-lg shadow-sm border border-gray-200">
    <div class="p-6 border-b border-gray-200">
      <div class="flex items-center justify-between">
        <h3 class="text-lg font-semibold text-gray-900">{{ title }}</h3>
        
        <div class="flex items-center space-x-2">
          <slot name="extra" />
          
          <AppDropdown>
            <template #trigger>
              <AppButton variant="ghost" size="sm">
                <EllipsisHorizontalIcon class="w-4 h-4" />
              </AppButton>
            </template>
            
            <template #content>
              <div class="py-1">
                <button
                  class="block w-full text-left px-4 py-2 text-sm text-gray-700 hover:bg-gray-100"
                  @click="exportChart"
                >
                  导出图表
                </button>
                <button
                  class="block w-full text-left px-4 py-2 text-sm text-gray-700 hover:bg-gray-100"
                  @click="refreshChart"
                >
                  刷新数据
                </button>
              </div>
            </template>
          </AppDropdown>
        </div>
      </div>
    </div>
    
    <div class="p-6">
      <div v-if="loading" class="flex items-center justify-center h-64">
        <AppSpinner size="lg" />
      </div>
      
      <div v-else-if="error" class="flex items-center justify-center h-64 text-gray-500">
        <div class="text-center">
          <ExclamationTriangleIcon class="w-12 h-12 mx-auto mb-2" />
          <p>数据加载失败</p>
          <AppButton size="sm" variant="outline" class="mt-2" @click="refreshChart">
            重试
          </AppButton>
        </div>
      </div>
      
      <div v-else>
        <slot />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
interface Props {
  title: string
  loading?: boolean
  error?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  loading: false,
  error: false
})

const emit = defineEmits<{
  export: []
  refresh: []
}>()

const exportChart = () => {
  emit('export')
}

const refreshChart = () => {
  emit('refresh')
}
</script>
```

## 数据可视化集成

### Chart.js集成
```typescript
// composables/useChart.ts
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  BarElement,
  ArcElement,
  Title,
  Tooltip,
  Legend,
  Filler
} from 'chart.js'

// 注册Chart.js组件
ChartJS.register(
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  BarElement,
  ArcElement,
  Title,
  Tooltip,
  Legend,
  Filler
)

export const useChart = () => {
  const defaultOptions = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        display: true,
        position: 'top' as const
      },
      tooltip: {
        mode: 'index' as const,
        intersect: false
      }
    },
    scales: {
      x: {
        display: true,
        grid: {
          display: false
        }
      },
      y: {
        display: true,
        beginAtZero: true,
        grid: {
          color: 'rgba(0, 0, 0, 0.1)'
        }
      }
    }
  }
  
  const createLineChart = (data: any, options = {}) => {
    return {
      type: 'line',
      data,
      options: { ...defaultOptions, ...options }
    }
  }
  
  const createBarChart = (data: any, options = {}) => {
    return {
      type: 'bar',
      data,
      options: { ...defaultOptions, ...options }
    }
  }
  
  const createPieChart = (data: any, options = {}) => {
    return {
      type: 'pie',
      data,
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: {
            position: 'bottom' as const
          }
        },
        ...options
      }
    }
  }
  
  return {
    createLineChart,
    createBarChart,
    createPieChart
  }
}
```

### 状态管理
```typescript
// stores/dashboard.ts
export const useDashboardStore = defineStore('dashboard', {
  state: (): DashboardState => ({
    stats: {
      totalCourses: 0,
      totalStudents: 0,
      totalRevenue: 0,
      activeUsers: 0,
      coursesChange: 0,
      studentsChange: 0,
      revenueChange: 0,
      activeUsersChange: 0
    },
    popularCourses: [],
    recentStudents: [],
    revenueChartData: null,
    categoryChartData: null,
    loading: false,
    error: null,
    lastUpdated: null
  }),
  
  actions: {
    async fetchDashboardData(timeRange: string = '7d') {
      this.loading = true
      this.error = null
      
      try {
        const [statsRes, coursesRes, studentsRes, chartsRes] = await Promise.all([
          dashboardApi.getStats(timeRange),
          dashboardApi.getPopularCourses(),
          dashboardApi.getRecentStudents(),
          dashboardApi.getChartData(timeRange)
        ])
        
        this.stats = statsRes.data
        this.popularCourses = coursesRes.data
        this.recentStudents = studentsRes.data
        this.revenueChartData = chartsRes.data.revenue
        this.categoryChartData = chartsRes.data.category
        this.lastUpdated = new Date().toISOString()
      } catch (error) {
        this.error = error.message
        throw error
      } finally {
        this.loading = false
      }
    },
    
    async refreshStats() {
      try {
        const response = await dashboardApi.getStats()
        this.stats = response.data
      } catch (error) {
        console.error('Failed to refresh stats:', error)
      }
    }
  }
})
```

## 验收标准
- 仪表盘布局响应式适配
- 数据统计准确显示
- 图表交互功能正常
- 实时数据更新机制
- 快捷操作功能完善
- 性能优化良好
- 错误处理机制完善
- 用户体验流畅

## 技术要求
- 使用Vue 3 Composition API
- 集成Chart.js或ECharts
- 实现数据缓存机制
- 支持实时数据更新
- 遵循响应式设计原则
- 实现性能监控

## 优先级
高优先级 - 核心展示功能

## 预估工时
3个工作日

## 相关文档
- SP1前端开发方案-产品需求文档.md
- SP1前端开发方案-技术架构文档.md
- 数据可视化设计规范