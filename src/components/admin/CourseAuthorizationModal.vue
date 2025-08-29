<template>
  <div class="fixed inset-0 bg-gray-600 bg-opacity-50 overflow-y-auto h-full w-full z-50">
    <div class="relative top-10 mx-auto p-5 border w-11/12 md:w-4/5 lg:w-3/4 shadow-lg rounded-md bg-white">
      <div class="mt-3">
        <!-- 标题 -->
        <div class="flex items-center justify-between mb-6">
          <div>
            <h3 class="text-lg font-medium text-gray-900">课程授权管理</h3>
            <p class="text-sm text-gray-600 mt-1">管理课程 "{{ course.title }}" 的门店授权</p>
          </div>
          <button
            @click="$emit('close')"
            class="text-gray-400 hover:text-gray-600 focus:outline-none"
          >
            <X class="w-6 h-6" />
          </button>
        </div>

        <!-- 操作栏 -->
        <div class="flex items-center justify-between mb-6">
          <div class="flex items-center space-x-4">
            <!-- 搜索门店 -->
            <div class="relative">
              <Search class="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-4 h-4" />
              <input
                v-model="searchQuery"
                type="text"
                placeholder="搜索门店名称"
                class="pl-10 w-64 rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
              />
            </div>
            
            <!-- 授权状态筛选 -->
            <select
              v-model="authFilter"
              class="rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
            >
              <option value="">全部门店</option>
              <option value="authorized">已授权</option>
              <option value="unauthorized">未授权</option>
            </select>
          </div>
          
          <div class="flex items-center space-x-3">
            <button
              @click="refreshData"
              :disabled="loading.stores || loading.authorizations"
              class="inline-flex items-center px-3 py-2 border border-gray-300 rounded-md shadow-sm text-sm font-medium text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-50"
            >
              <RefreshCw :class="['w-4 h-4 mr-2', { 'animate-spin': loading.stores || loading.authorizations }]" />
              刷新
            </button>
            
            <button
              @click="showBatchModal = true"
              class="inline-flex items-center px-4 py-2 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
            >
              <Shield class="w-4 h-4 mr-2" />
              批量授权
            </button>
          </div>
        </div>

        <!-- 错误提示 -->
        <div v-if="error" class="bg-red-50 border border-red-200 rounded-md p-4 mb-6">
          <div class="flex">
            <AlertCircle class="h-5 w-5 text-red-400" />
            <div class="ml-3">
              <h3 class="text-sm font-medium text-red-800">操作失败</h3>
              <p class="mt-1 text-sm text-red-700">{{ error }}</p>
            </div>
          </div>
        </div>

        <!-- 门店列表 -->
        <div class="bg-white border border-gray-200 rounded-lg overflow-hidden">
          <div class="px-6 py-4 border-b border-gray-200 bg-gray-50">
            <div class="flex items-center justify-between">
              <h4 class="text-sm font-medium text-gray-900">门店授权列表</h4>
              <div class="text-sm text-gray-500">
                已授权 {{ authorizedCount }} / {{ filteredStores.length }} 个门店
              </div>
            </div>
          </div>
          
          <div v-if="loading.stores" class="p-6">
            <div class="space-y-4">
              <div v-for="i in 5" :key="i" class="animate-pulse">
                <div class="flex items-center space-x-4">
                  <div class="w-4 h-4 bg-gray-200 rounded"></div>
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
            <p class="text-gray-500">没有找到符合条件的门店</p>
          </div>
          
          <div v-else class="divide-y divide-gray-200 max-h-96 overflow-y-auto">
            <div
              v-for="store in paginatedStores"
              :key="store.id"
              class="p-4 hover:bg-gray-50 transition-colors"
            >
              <div class="flex items-center justify-between">
                <div class="flex items-center space-x-4">
                  <input
                    :id="`store-${store.id}`"
                    type="checkbox"
                    :checked="isStoreAuthorized(store.id)"
                    @change="toggleStoreAuthorization(store.id, $event)"
                    :disabled="processing.has(store.id)"
                    class="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded disabled:opacity-50"
                  />
                  <label :for="`store-${store.id}`" class="flex-1 cursor-pointer">
                    <div class="flex items-center space-x-3">
                      <div>
                        <h4 class="text-sm font-medium text-gray-900">{{ store.name }}</h4>
                        <div class="flex items-center space-x-4 text-xs text-gray-500 mt-1">
                          <span class="flex items-center">
                            <MapPin class="w-3 h-3 mr-1" />
                            {{ store.address }}
                          </span>
                          <span class="flex items-center">
                            <Phone class="w-3 h-3 mr-1" />
                            {{ store.phone }}
                          </span>
                          <span class="flex items-center">
                            <User class="w-3 h-3 mr-1" />
                            {{ store.managerName }}
                          </span>
                        </div>
                      </div>
                    </div>
                  </label>
                </div>
                
                <div class="flex items-center space-x-2">
                  <!-- 授权状态 -->
                  <StatusBadge
                    :status="isStoreAuthorized(store.id) ? 'healthy' : 'error'"
                    :text="isStoreAuthorized(store.id) ? '已授权' : '未授权'"
                  />
                  
                  <!-- 授权时间 -->
                  <div v-if="getAuthorizationInfo(store.id)" class="text-xs text-gray-500">
                    {{ formatDate(getAuthorizationInfo(store.id)!.createdAt) }}
                  </div>
                  
                  <!-- 加载状态 -->
                  <Loader2 v-if="processing.has(store.id)" class="w-4 h-4 animate-spin text-blue-500" />
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

        <!-- 操作按钮 -->
        <div class="flex items-center justify-end space-x-3 pt-6 border-t border-gray-200 mt-6">
          <button
            @click="$emit('close')"
            type="button"
            class="px-4 py-2 border border-gray-300 rounded-md shadow-sm text-sm font-medium text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
          >
            关闭
          </button>
        </div>
      </div>
    </div>
    
    <!-- 批量授权模态框 -->
    <BatchAuthorizationModal
      v-if="showBatchModal"
      :course="course"
      :stores="stores"
      :authorizations="authorizations"
      @close="showBatchModal = false"
      @updated="handleBatchUpdated"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useAdminStore } from '@/store/modules/admin'
import {
  X,
  Search,
  RefreshCw,
  Shield,
  AlertCircle,
  Store,
  MapPin,
  Phone,
  User,
  Loader2
} from 'lucide-vue-next'
import StatusBadge from '@/components/admin/StatusBadge.vue'
import BatchAuthorizationModal from '@/components/admin/BatchAuthorizationModal.vue'
import type { Course, Store as StoreType, CourseAuthorization } from '@/types/api/admin'

// Props
const props = defineProps<{
  course: Course
}>()

// Emits
defineEmits<{
  close: []
  updated: []
}>()

// Store
const adminStore = useAdminStore()

// 响应式数据
const searchQuery = ref('')
const authFilter = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const showBatchModal = ref(false)
const error = ref('')
const processing = ref(new Set<string>())

// 计算属性
const { stores, courseAuthorizations, loading } = adminStore

const authorizations = computed(() => {
  return courseAuthorizations.value.filter(auth => auth.courseId === props.course.id)
})

const filteredStores = computed(() => {
  let result = stores.value || []
  
  // 搜索过滤
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    result = result.filter(store => 
      store.name.toLowerCase().includes(query) ||
      store.address?.toLowerCase().includes(query)
    )
  }
  
  // 授权状态过滤
  if (authFilter.value === 'authorized') {
    result = result.filter(store => isStoreAuthorized(store.id))
  } else if (authFilter.value === 'unauthorized') {
    result = result.filter(store => !isStoreAuthorized(store.id))
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

const authorizedCount = computed(() => {
  return filteredStores.value.filter(store => isStoreAuthorized(store.id)).length
})

// 方法
const isStoreAuthorized = (storeId: string): boolean => {
  return authorizations.value.some(auth => auth.storeId === storeId)
}

const getAuthorizationInfo = (storeId: string): CourseAuthorization | undefined => {
  return authorizations.value.find(auth => auth.storeId === storeId)
}

const formatDate = (dateString: string): string => {
  return new Date(dateString).toLocaleDateString('zh-CN')
}

const toggleStoreAuthorization = async (storeId: string, event: Event) => {
  const target = event.target as HTMLInputElement
  const isAuthorized = target.checked
  
  processing.value.add(storeId)
  error.value = ''
  
  try {
    if (isAuthorized) {
      await adminStore.createCourseAuthorization({
        courseId: props.course.id,
        storeId
      })
    } else {
      const auth = getAuthorizationInfo(storeId)
      if (auth) {
        await adminStore.revokeCourseAuthorization({
          courseId: props.course.id,
          storeId
        })
      }
    }
    
    // 重新加载授权数据
    await adminStore.loadCourseAuthorizations({ courseId: props.course.id })
  } catch (err: any) {
    error.value = err.message || '操作失败，请重试'
    // 恢复复选框状态
    target.checked = !isAuthorized
  } finally {
    processing.value.delete(storeId)
  }
}

const refreshData = async () => {
  error.value = ''
  await Promise.all([
    adminStore.loadStores(),
    adminStore.loadCourseAuthorizations({ courseId: props.course.id })
  ])
}

const handleBatchUpdated = () => {
  showBatchModal.value = false
  refreshData()
}

// 监听搜索和筛选变化，重置分页
watch([searchQuery, authFilter], () => {
  currentPage.value = 1
})

// 生命周期
onMounted(async () => {
  await refreshData()
})
</script>