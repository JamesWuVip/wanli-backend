<template>
  <div class="min-h-screen bg-gray-50 py-8">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <!-- 页面标题 -->
      <div class="mb-8">
        <h1 class="text-3xl font-bold text-gray-900">门店管理</h1>
        <p class="mt-2 text-gray-600">管理系统中的所有门店信息、统计数据和权限设置</p>
      </div>

      <!-- 统计卡片 -->
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
        <StatCard
          title="总门店数"
          :value="storeStats.totalStores"
          icon="Store"
          color="blue"
          :loading="loading.stats"
        />
        <StatCard
          title="活跃门店"
          :value="storeStats.activeStores"
          icon="CheckCircle"
          color="green"
          :loading="loading.stats"
        />
        <StatCard
          title="本月新增"
          :value="storeStats.newStoresThisMonth"
          icon="TrendingUp"
          color="purple"
          :loading="loading.stats"
        />
        <StatCard
          title="平均评分"
          :value="storeStats.averageRating"
          :precision="1"
          icon="Star"
          color="yellow"
          :loading="loading.stats"
        />
      </div>

      <!-- 操作栏 -->
      <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6 mb-6">
        <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between space-y-4 sm:space-y-0">
          <div class="flex flex-col sm:flex-row sm:items-center space-y-4 sm:space-y-0 sm:space-x-4">
            <!-- 搜索 -->
            <div class="relative">
              <Search class="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-4 h-4" />
              <input
                v-model="searchQuery"
                type="text"
                placeholder="搜索门店名称、地址或管理员"
                class="pl-10 w-64 rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
              />
            </div>
            
            <!-- 状态筛选 -->
            <select
              v-model="statusFilter"
              class="rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
            >
              <option value="">全部状态</option>
              <option value="active">活跃</option>
              <option value="inactive">停用</option>
              <option value="pending">待审核</option>
            </select>
            
            <!-- 地区筛选 -->
            <select
              v-model="regionFilter"
              class="rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
            >
              <option value="">全部地区</option>
              <option v-for="region in regions" :key="region" :value="region">
                {{ region }}
              </option>
            </select>
          </div>
          
          <div class="flex items-center space-x-3">
            <button
              @click="refreshData"
              :disabled="loading.stores"
              class="inline-flex items-center px-3 py-2 border border-gray-300 rounded-md shadow-sm text-sm font-medium text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-50"
            >
              <RefreshCw :class="['w-4 h-4 mr-2', { 'animate-spin': loading.stores }]" />
              刷新
            </button>
            
            <button
              @click="showCreateModal = true"
              class="inline-flex items-center px-4 py-2 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
            >
              <Plus class="w-4 h-4 mr-2" />
              新建门店
            </button>
          </div>
        </div>
      </div>

      <!-- 错误提示 -->
      <div v-if="error" class="bg-red-50 border border-red-200 rounded-md p-4 mb-6">
        <div class="flex">
          <AlertCircle class="h-5 w-5 text-red-400" />
          <div class="ml-3">
            <h3 class="text-sm font-medium text-red-800">加载失败</h3>
            <p class="mt-1 text-sm text-red-700">{{ error }}</p>
          </div>
        </div>
      </div>

      <!-- 门店列表 -->
      <div class="bg-white rounded-lg shadow-sm border border-gray-200 overflow-hidden">
        <div class="px-6 py-4 border-b border-gray-200 bg-gray-50">
          <h3 class="text-lg font-medium text-gray-900">门店列表</h3>
        </div>
        
        <div v-if="loading.stores" class="p-6">
          <div class="space-y-4">
            <div v-for="i in 5" :key="i" class="animate-pulse">
              <div class="flex items-center space-x-4">
                <div class="w-12 h-12 bg-gray-200 rounded-lg"></div>
                <div class="flex-1">
                  <div class="h-4 bg-gray-200 rounded w-1/3 mb-2"></div>
                  <div class="h-3 bg-gray-200 rounded w-1/2"></div>
                </div>
                <div class="w-20 h-8 bg-gray-200 rounded"></div>
              </div>
            </div>
          </div>
        </div>
        
        <div v-else-if="filteredStores.length === 0" class="p-12 text-center">
          <Store class="w-12 h-12 mx-auto mb-4 text-gray-300" />
          <h3 class="text-lg font-medium text-gray-900 mb-2">暂无门店</h3>
          <p class="text-gray-500 mb-4">没有找到符合条件的门店</p>
          <button
            @click="showCreateModal = true"
            class="inline-flex items-center px-4 py-2 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-blue-600 hover:bg-blue-700"
          >
            <Plus class="w-4 h-4 mr-2" />
            创建第一个门店
          </button>
        </div>
        
        <div v-else>
          <!-- 表格头部 -->
          <div class="bg-gray-50 px-6 py-3 border-b border-gray-200">
            <div class="grid grid-cols-12 gap-4 text-xs font-medium text-gray-500 uppercase tracking-wider">
              <div class="col-span-3">门店信息</div>
              <div class="col-span-2">联系方式</div>
              <div class="col-span-2">管理员</div>
              <div class="col-span-2">统计数据</div>
              <div class="col-span-1">状态</div>
              <div class="col-span-2">操作</div>
            </div>
          </div>
          
          <!-- 表格内容 -->
          <div class="divide-y divide-gray-200">
            <div
              v-for="store in paginatedStores"
              :key="store.id"
              class="px-6 py-4 hover:bg-gray-50 transition-colors"
            >
              <div class="grid grid-cols-12 gap-4 items-center">
                <!-- 门店信息 -->
                <div class="col-span-3">
                  <div class="flex items-center space-x-3">
                    <div class="w-10 h-10 bg-blue-100 rounded-lg flex items-center justify-center">
                      <Store class="w-5 h-5 text-blue-600" />
                    </div>
                    <div>
                      <h4 class="text-sm font-medium text-gray-900">{{ store.name }}</h4>
                      <p class="text-xs text-gray-500 flex items-center mt-1">
                        <MapPin class="w-3 h-3 mr-1" />
                        {{ store.address }}
                      </p>
                    </div>
                  </div>
                </div>
                
                <!-- 联系方式 -->
                <div class="col-span-2">
                  <div class="space-y-1">
                    <p class="text-sm text-gray-900 flex items-center">
                      <Phone class="w-3 h-3 mr-1 text-gray-400" />
                      {{ store.phone }}
                    </p>
                    <p class="text-xs text-gray-500 flex items-center">
                      <Mail class="w-3 h-3 mr-1 text-gray-400" />
                      {{ store.email }}
                    </p>
                  </div>
                </div>
                
                <!-- 管理员 -->
                <div class="col-span-2">
                  <div v-if="store.managerName" class="flex items-center space-x-2">
                    <div class="w-6 h-6 bg-gray-200 rounded-full flex items-center justify-center">
                      <User class="w-3 h-3 text-gray-600" />
                    </div>
                    <div>
                      <p class="text-sm font-medium text-gray-900">{{ store.managerName }}</p>
                      <p class="text-xs text-gray-500">{{ store.managerPhone }}</p>
                    </div>
                  </div>
                  <div v-else class="text-sm text-gray-400">
                    未分配管理员
                  </div>
                </div>
                
                <!-- 统计数据 -->
                <div class="col-span-2">
                  <div class="space-y-1">
                    <div class="flex items-center text-xs text-gray-600">
                      <Users class="w-3 h-3 mr-1" />
                      学生: {{ store.studentCount || 0 }}
                    </div>
                    <div class="flex items-center text-xs text-gray-600">
                      <BookOpen class="w-3 h-3 mr-1" />
                      课程: {{ store.courseCount || 0 }}
                    </div>
                  </div>
                </div>
                
                <!-- 状态 -->
                <div class="col-span-1">
                  <StatusBadge
                    :status="getStoreStatusType(store.status)"
                    :text="getStoreStatusText(store.status)"
                  />
                </div>
                
                <!-- 操作 -->
                <div class="col-span-2">
                  <div class="flex items-center space-x-2">
                    <button
                      @click="viewStoreDetails(store)"
                      class="text-blue-600 hover:text-blue-800 text-sm font-medium"
                    >
                      查看
                    </button>
                    <button
                      @click="editStore(store)"
                      class="text-green-600 hover:text-green-800 text-sm font-medium"
                    >
                      编辑
                    </button>
                    <button
                      @click="manageStoreManagers(store)"
                      class="text-purple-600 hover:text-purple-800 text-sm font-medium"
                    >
                      管理员
                    </button>
                    <button
                      @click="manageStoreCourses(store)"
                      class="text-orange-600 hover:text-orange-800 text-sm font-medium"
                    >
                      课程
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>
          
          <!-- 分页 -->
          <div v-if="totalPages > 1" class="px-6 py-4 border-t border-gray-200 bg-gray-50">
            <div class="flex items-center justify-between">
              <div class="text-sm text-gray-700">
                显示 {{ (currentPage - 1) * pageSize + 1 }} 到 {{ Math.min(currentPage * pageSize, filteredStores.length) }} 条，
                共 {{ filteredStores.length }} 条记录
              </div>
              <div class="flex items-center space-x-2">
                <button
                  @click="currentPage = Math.max(1, currentPage - 1)"
                  :disabled="currentPage === 1"
                  class="px-3 py-1 border border-gray-300 rounded-md text-sm font-medium text-gray-700 bg-white hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  上一页
                </button>
                <span class="px-3 py-1 text-sm text-gray-700">
                  {{ currentPage }} / {{ totalPages }}
                </span>
                <button
                  @click="currentPage = Math.min(totalPages, currentPage + 1)"
                  :disabled="currentPage === totalPages"
                  class="px-3 py-1 border border-gray-300 rounded-md text-sm font-medium text-gray-700 bg-white hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  下一页
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 模态框 -->
    <StoreCreateModal
      v-if="showCreateModal"
      @close="showCreateModal = false"
      @created="handleStoreCreated"
    />
    
    <StoreEditModal
      v-if="showEditModal && selectedStore"
      :store="selectedStore"
      @close="showEditModal = false"
      @updated="handleStoreUpdated"
    />
    
    <StoreDetailsModal
      v-if="showDetailsModal && selectedStore"
      :store="selectedStore"
      @close="showDetailsModal = false"
    />
    
    <StoreManagerModal
      v-if="showManagerModal && selectedStore"
      :store="selectedStore"
      @close="showManagerModal = false"
      @updated="handleManagerUpdated"
    />
    
    <StoreCourseModal
      v-if="showCourseModal && selectedStore"
      :store="selectedStore"
      @close="showCourseModal = false"
      @updated="handleCourseUpdated"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useAdminStore } from '@/store/modules/admin'
import {
  Search,
  RefreshCw,
  Plus,
  AlertCircle,
  Store,
  MapPin,
  Phone,
  Mail,
  User,
  Users,
  BookOpen,
  CheckCircle,
  TrendingUp,
  Star
} from 'lucide-vue-next'
import StatCard from '@/components/admin/StatCard.vue'
import StatusBadge from '@/components/admin/StatusBadge.vue'
import StoreCreateModal from '@/components/admin/StoreCreateModal.vue'
import StoreEditModal from '@/components/admin/StoreEditModal.vue'
import StoreDetailsModal from '@/components/admin/StoreDetailsModal.vue'
import StoreManagerModal from '@/components/admin/StoreManagerModal.vue'
import StoreCourseModal from '@/components/admin/StoreCourseModal.vue'
import type { Store as StoreType } from '@/types/api/admin'

// Store
const adminStore = useAdminStore()

// 响应式数据
const searchQuery = ref('')
const statusFilter = ref('')
const regionFilter = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const error = ref('')

// 模态框状态
const showCreateModal = ref(false)
const showEditModal = ref(false)
const showDetailsModal = ref(false)
const showManagerModal = ref(false)
const showCourseModal = ref(false)
const selectedStore = ref<StoreType | null>(null)

// 计算属性
const { stores, storeStats, loading } = adminStore

const regions = computed(() => {
  const regionSet = new Set<string>()
  stores.value?.forEach(store => {
    if (store.region) {
      regionSet.add(store.region)
    }
  })
  return Array.from(regionSet).sort()
})

const filteredStores = computed(() => {
  let result = stores.value || []
  
  // 搜索过滤
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    result = result.filter(store => 
      store.name.toLowerCase().includes(query) ||
      store.address?.toLowerCase().includes(query) ||
      store.managerName?.toLowerCase().includes(query)
    )
  }
  
  // 状态过滤
  if (statusFilter.value) {
    result = result.filter(store => store.status === statusFilter.value)
  }
  
  // 地区过滤
  if (regionFilter.value) {
    result = result.filter(store => store.region === regionFilter.value)
  }
  
  return result
})

const totalPages = computed(() => {
  return Math.ceil(filteredStores.value.length / pageSize.value)
})

const paginatedStores = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredStores.value.slice(start, end)
})

// 方法
const getStoreStatusType = (status: string) => {
  switch (status) {
    case 'active': return 'healthy'
    case 'inactive': return 'error'
    case 'pending': return 'warning'
    default: return 'unknown'
  }
}

const getStoreStatusText = (status: string) => {
  switch (status) {
    case 'active': return '活跃'
    case 'inactive': return '停用'
    case 'pending': return '待审核'
    default: return '未知'
  }
}

const viewStoreDetails = (store: StoreType) => {
  selectedStore.value = store
  showDetailsModal.value = true
}

const editStore = (store: StoreType) => {
  selectedStore.value = store
  showEditModal.value = true
}

const manageStoreManagers = (store: StoreType) => {
  selectedStore.value = store
  showManagerModal.value = true
}

const manageStoreCourses = (store: StoreType) => {
  selectedStore.value = store
  showCourseModal.value = true
}

const refreshData = async () => {
  error.value = ''
  try {
    await Promise.all([
      adminStore.loadStores(),
      adminStore.loadStoreStats()
    ])
  } catch (err: any) {
    error.value = err.message || '加载数据失败，请重试'
  }
}

const handleStoreCreated = () => {
  showCreateModal.value = false
  refreshData()
}

const handleStoreUpdated = () => {
  showEditModal.value = false
  selectedStore.value = null
  refreshData()
}

const handleManagerUpdated = () => {
  showManagerModal.value = false
  selectedStore.value = null
  refreshData()
}

const handleCourseUpdated = () => {
  showCourseModal.value = false
  selectedStore.value = null
  refreshData()
}

// 监听搜索和筛选变化，重置分页
watch([searchQuery, statusFilter, regionFilter], () => {
  currentPage.value = 1
})

// 生命周期
onMounted(async () => {
  await refreshData()
})
</script>