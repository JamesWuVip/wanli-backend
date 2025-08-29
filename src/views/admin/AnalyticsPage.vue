<template>
  <div class="min-h-screen bg-gray-50 py-8">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <!-- 页面标题 -->
      <div class="mb-8">
        <h1 class="text-3xl font-bold text-gray-900">数据分析</h1>
        <p class="mt-2 text-gray-600">查看系统运营数据和业务分析报告</p>
      </div>

      <!-- 时间范围选择 -->
      <div class="mb-8 flex justify-between items-center">
        <div class="flex space-x-4">
          <button
            v-for="period in timePeriods"
            :key="period.value"
            @click="selectedPeriod = period.value"
            :class="[
              'px-4 py-2 text-sm font-medium rounded-md',
              selectedPeriod === period.value
                ? 'bg-blue-600 text-white'
                : 'bg-white text-gray-700 border border-gray-300 hover:bg-gray-50'
            ]"
          >
            {{ period.label }}
          </button>
        </div>
        <button
          @click="refreshData"
          class="inline-flex items-center px-4 py-2 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-blue-600 hover:bg-blue-700"
        >
          <RefreshCw class="-ml-1 mr-2 h-4 w-4" />
          刷新数据
        </button>
      </div>

      <!-- 核心指标卡片 -->
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
        <div class="bg-white rounded-lg shadow p-6">
          <div class="flex items-center">
            <div class="flex-shrink-0">
              <TrendingUp class="h-8 w-8 text-green-600" />
            </div>
            <div class="ml-5 w-0 flex-1">
              <dl>
                <dt class="text-sm font-medium text-gray-500 truncate">总收入</dt>
                <dd class="flex items-baseline">
                  <div class="text-2xl font-semibold text-gray-900">¥{{ formatNumber(analytics.totalRevenue) }}</div>
                  <div class="ml-2 flex items-baseline text-sm font-semibold text-green-600">
                    <ArrowUp class="self-center flex-shrink-0 h-4 w-4" />
                    <span class="sr-only">增长</span>
                    {{ analytics.revenueGrowth }}%
                  </div>
                </dd>
              </dl>
            </div>
          </div>
        </div>

        <div class="bg-white rounded-lg shadow p-6">
          <div class="flex items-center">
            <div class="flex-shrink-0">
              <Users class="h-8 w-8 text-blue-600" />
            </div>
            <div class="ml-5 w-0 flex-1">
              <dl>
                <dt class="text-sm font-medium text-gray-500 truncate">新增用户</dt>
                <dd class="flex items-baseline">
                  <div class="text-2xl font-semibold text-gray-900">{{ formatNumber(analytics.newUsers) }}</div>
                  <div class="ml-2 flex items-baseline text-sm font-semibold text-green-600">
                    <ArrowUp class="self-center flex-shrink-0 h-4 w-4" />
                    <span class="sr-only">增长</span>
                    {{ analytics.userGrowth }}%
                  </div>
                </dd>
              </dl>
            </div>
          </div>
        </div>

        <div class="bg-white rounded-lg shadow p-6">
          <div class="flex items-center">
            <div class="flex-shrink-0">
              <BookOpen class="h-8 w-8 text-purple-600" />
            </div>
            <div class="ml-5 w-0 flex-1">
              <dl>
                <dt class="text-sm font-medium text-gray-500 truncate">课程完成率</dt>
                <dd class="flex items-baseline">
                  <div class="text-2xl font-semibold text-gray-900">{{ analytics.completionRate }}%</div>
                  <div class="ml-2 flex items-baseline text-sm font-semibold text-green-600">
                    <ArrowUp class="self-center flex-shrink-0 h-4 w-4" />
                    <span class="sr-only">增长</span>
                    {{ analytics.completionGrowth }}%
                  </div>
                </dd>
              </dl>
            </div>
          </div>
        </div>

        <div class="bg-white rounded-lg shadow p-6">
          <div class="flex items-center">
            <div class="flex-shrink-0">
              <Activity class="h-8 w-8 text-orange-600" />
            </div>
            <div class="ml-5 w-0 flex-1">
              <dl>
                <dt class="text-sm font-medium text-gray-500 truncate">活跃度</dt>
                <dd class="flex items-baseline">
                  <div class="text-2xl font-semibold text-gray-900">{{ analytics.activeRate }}%</div>
                  <div class="ml-2 flex items-baseline text-sm font-semibold text-red-600">
                    <ArrowDown class="self-center flex-shrink-0 h-4 w-4" />
                    <span class="sr-only">下降</span>
                    {{ Math.abs(analytics.activeGrowth) }}%
                  </div>
                </dd>
              </dl>
            </div>
          </div>
        </div>
      </div>

      <!-- 图表区域 -->
      <div class="grid grid-cols-1 lg:grid-cols-2 gap-8 mb-8">
        <!-- 收入趋势图 -->
        <div class="bg-white rounded-lg shadow p-6">
          <h3 class="text-lg font-medium text-gray-900 mb-4">收入趋势</h3>
          <div class="h-64 flex items-center justify-center bg-gray-50 rounded-lg">
            <div class="text-center">
              <BarChart3 class="h-12 w-12 text-gray-400 mx-auto mb-2" />
              <p class="text-gray-500">收入趋势图表</p>
              <p class="text-sm text-gray-400">（需要集成图表库）</p>
            </div>
          </div>
        </div>

        <!-- 用户增长图 -->
        <div class="bg-white rounded-lg shadow p-6">
          <h3 class="text-lg font-medium text-gray-900 mb-4">用户增长</h3>
          <div class="h-64 flex items-center justify-center bg-gray-50 rounded-lg">
            <div class="text-center">
              <LineChart class="h-12 w-12 text-gray-400 mx-auto mb-2" />
              <p class="text-gray-500">用户增长图表</p>
              <p class="text-sm text-gray-400">（需要集成图表库）</p>
            </div>
          </div>
        </div>
      </div>

      <!-- 详细数据表格 -->
      <div class="grid grid-cols-1 lg:grid-cols-2 gap-8">
        <!-- 热门课程排行 -->
        <div class="bg-white rounded-lg shadow">
          <div class="px-6 py-4 border-b border-gray-200">
            <h3 class="text-lg font-medium text-gray-900">热门课程排行</h3>
          </div>
          <div class="p-6">
            <div class="space-y-4">
              <div v-for="(course, index) in topCourses" :key="course.id" class="flex items-center justify-between">
                <div class="flex items-center">
                  <div class="flex-shrink-0 w-8 h-8 bg-blue-100 rounded-full flex items-center justify-center">
                    <span class="text-sm font-medium text-blue-600">{{ index + 1 }}</span>
                  </div>
                  <div class="ml-3">
                    <p class="text-sm font-medium text-gray-900">{{ course.name }}</p>
                    <p class="text-sm text-gray-500">{{ course.students }} 名学生</p>
                  </div>
                </div>
                <div class="text-sm font-medium text-gray-900">¥{{ formatNumber(course.revenue) }}</div>
              </div>
            </div>
          </div>
        </div>

        <!-- 门店业绩排行 -->
        <div class="bg-white rounded-lg shadow">
          <div class="px-6 py-4 border-b border-gray-200">
            <h3 class="text-lg font-medium text-gray-900">门店业绩排行</h3>
          </div>
          <div class="p-6">
            <div class="space-y-4">
              <div v-for="(store, index) in topStores" :key="store.id" class="flex items-center justify-between">
                <div class="flex items-center">
                  <div class="flex-shrink-0 w-8 h-8 bg-green-100 rounded-full flex items-center justify-center">
                    <span class="text-sm font-medium text-green-600">{{ index + 1 }}</span>
                  </div>
                  <div class="ml-3">
                    <p class="text-sm font-medium text-gray-900">{{ store.name }}</p>
                    <p class="text-sm text-gray-500">{{ store.location }}</p>
                  </div>
                </div>
                <div class="text-sm font-medium text-gray-900">¥{{ formatNumber(store.revenue) }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import {
  TrendingUp,
  Users,
  BookOpen,
  Activity,
  ArrowUp,
  ArrowDown,
  RefreshCw,
  BarChart3,
  LineChart
} from 'lucide-vue-next'

interface Analytics {
  totalRevenue: number
  revenueGrowth: number
  newUsers: number
  userGrowth: number
  completionRate: number
  completionGrowth: number
  activeRate: number
  activeGrowth: number
}

interface Course {
  id: string
  name: string
  students: number
  revenue: number
}

interface Store {
  id: string
  name: string
  location: string
  revenue: number
}

const selectedPeriod = ref('7d')

const timePeriods = [
  { label: '7天', value: '7d' },
  { label: '30天', value: '30d' },
  { label: '90天', value: '90d' },
  { label: '1年', value: '1y' }
]

const analytics = ref<Analytics>({
  totalRevenue: 2456789,
  revenueGrowth: 12.5,
  newUsers: 1248,
  userGrowth: 8.3,
  completionRate: 87.5,
  completionGrowth: 3.2,
  activeRate: 73.8,
  activeGrowth: -2.1
})

const topCourses = ref<Course[]>([
  { id: '1', name: 'Vue.js 进阶开发', students: 456, revenue: 123456 },
  { id: '2', name: 'React 实战项目', students: 389, revenue: 98765 },
  { id: '3', name: 'Node.js 后端开发', students: 321, revenue: 87654 },
  { id: '4', name: 'Python 数据分析', students: 298, revenue: 76543 },
  { id: '5', name: 'Java Spring Boot', students: 267, revenue: 65432 }
])

const topStores = ref<Store[]>([
  { id: '1', name: '北京朝阳店', location: '朝阳区', revenue: 456789 },
  { id: '2', name: '上海浦东店', location: '浦东新区', revenue: 398765 },
  { id: '3', name: '深圳南山店', location: '南山区', revenue: 345678 },
  { id: '4', name: '广州天河店', location: '天河区', revenue: 298765 },
  { id: '5', name: '杭州西湖店', location: '西湖区', revenue: 234567 }
])

const formatNumber = (num: number) => {
  return num.toLocaleString('zh-CN')
}

const refreshData = () => {
  // 这里可以重新加载数据
  console.log('刷新数据...')
}

onMounted(() => {
  // 这里可以加载分析数据
})
</script>