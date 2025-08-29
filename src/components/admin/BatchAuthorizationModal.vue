<template>
  <div class="fixed inset-0 bg-gray-600 bg-opacity-50 overflow-y-auto h-full w-full z-60">
    <div class="relative top-10 mx-auto p-5 border w-11/12 md:w-3/5 lg:w-1/2 shadow-lg rounded-md bg-white">
      <div class="mt-3">
        <!-- 标题 -->
        <div class="flex items-center justify-between mb-6">
          <div>
            <h3 class="text-lg font-medium text-gray-900">批量授权管理</h3>
            <p class="text-sm text-gray-600 mt-1">批量管理课程 "{{ course.title }}" 的门店授权</p>
          </div>
          <button
            @click="$emit('close')"
            class="text-gray-400 hover:text-gray-600 focus:outline-none"
          >
            <X class="w-6 h-6" />
          </button>
        </div>

        <!-- 操作选项 -->
        <div class="space-y-4 mb-6">
          <div class="bg-blue-50 border border-blue-200 rounded-lg p-4">
            <h4 class="text-sm font-medium text-blue-900 mb-3">批量操作选项</h4>
            <div class="space-y-3">
              <label class="flex items-center">
                <input
                  v-model="operationType"
                  type="radio"
                  value="authorize_all"
                  class="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300"
                />
                <span class="ml-3 text-sm text-gray-700">
                  <span class="font-medium">授权所有门店</span>
                  <span class="text-gray-500 block">为所有门店授权此课程</span>
                </span>
              </label>
              
              <label class="flex items-center">
                <input
                  v-model="operationType"
                  type="radio"
                  value="revoke_all"
                  class="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300"
                />
                <span class="ml-3 text-sm text-gray-700">
                  <span class="font-medium">撤销所有授权</span>
                  <span class="text-gray-500 block">撤销所有门店对此课程的授权</span>
                </span>
              </label>
              
              <label class="flex items-center">
                <input
                  v-model="operationType"
                  type="radio"
                  value="authorize_selected"
                  class="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300"
                />
                <span class="ml-3 text-sm text-gray-700">
                  <span class="font-medium">授权选中门店</span>
                  <span class="text-gray-500 block">为选中的门店授权此课程</span>
                </span>
              </label>
              
              <label class="flex items-center">
                <input
                  v-model="operationType"
                  type="radio"
                  value="revoke_selected"
                  class="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300"
                />
                <span class="ml-3 text-sm text-gray-700">
                  <span class="font-medium">撤销选中门店授权</span>
                  <span class="text-gray-500 block">撤销选中门店对此课程的授权</span>
                </span>
              </label>
            </div>
          </div>
        </div>

        <!-- 门店选择 -->
        <div v-if="operationType === 'authorize_selected' || operationType === 'revoke_selected'" class="mb-6">
          <div class="bg-gray-50 border border-gray-200 rounded-lg p-4">
            <div class="flex items-center justify-between mb-4">
              <h4 class="text-sm font-medium text-gray-900">选择门店</h4>
              <div class="flex items-center space-x-2">
                <button
                  @click="selectAll"
                  class="text-sm text-blue-600 hover:text-blue-800"
                >
                  全选
                </button>
                <span class="text-gray-300">|</span>
                <button
                  @click="clearSelection"
                  class="text-sm text-blue-600 hover:text-blue-800"
                >
                  清空
                </button>
              </div>
            </div>
            
            <!-- 搜索 -->
            <div class="relative mb-4">
              <Search class="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-4 h-4" />
              <input
                v-model="storeSearchQuery"
                type="text"
                placeholder="搜索门店名称"
                class="pl-10 w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
              />
            </div>
            
            <!-- 门店列表 -->
            <div class="max-h-64 overflow-y-auto space-y-2">
              <div
                v-for="store in filteredStoresForSelection"
                :key="store.id"
                class="flex items-center p-3 border border-gray-200 rounded-md hover:bg-white transition-colors"
              >
                <input
                  :id="`batch-store-${store.id}`"
                  v-model="selectedStoreIds"
                  :value="store.id"
                  type="checkbox"
                  class="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
                />
                <label :for="`batch-store-${store.id}`" class="ml-3 flex-1 cursor-pointer">
                  <div class="flex items-center justify-between">
                    <div>
                      <h5 class="text-sm font-medium text-gray-900">{{ store.name }}</h5>
                      <p class="text-xs text-gray-500">{{ store.address }}</p>
                    </div>
                    <StatusBadge
                      :status="isStoreCurrentlyAuthorized(store.id) ? 'healthy' : 'error'"
                      :text="isStoreCurrentlyAuthorized(store.id) ? '已授权' : '未授权'"
                    />
                  </div>
                </label>
              </div>
            </div>
            
            <div v-if="filteredStoresForSelection.length === 0" class="text-center py-8">
              <Store class="w-8 h-8 mx-auto mb-2 text-gray-300" />
              <p class="text-sm text-gray-500">没有找到符合条件的门店</p>
            </div>
          </div>
        </div>

        <!-- 操作预览 -->
        <div v-if="operationType" class="mb-6">
          <div class="bg-yellow-50 border border-yellow-200 rounded-lg p-4">
            <div class="flex items-start">
              <AlertTriangle class="h-5 w-5 text-yellow-400 mt-0.5" />
              <div class="ml-3">
                <h4 class="text-sm font-medium text-yellow-800">操作预览</h4>
                <div class="mt-2 text-sm text-yellow-700">
                  <p v-if="operationType === 'authorize_all'">
                    将为 <strong>{{ stores.length }}</strong> 个门店授权课程 "{{ course.title }}"
                  </p>
                  <p v-else-if="operationType === 'revoke_all'">
                    将撤销 <strong>{{ currentAuthorizedStores.length }}</strong> 个门店对课程 "{{ course.title }}" 的授权
                  </p>
                  <p v-else-if="operationType === 'authorize_selected'">
                    将为 <strong>{{ selectedStoreIds.length }}</strong> 个选中门店授权课程 "{{ course.title }}"
                  </p>
                  <p v-else-if="operationType === 'revoke_selected'">
                    将撤销 <strong>{{ selectedAuthorizedStores.length }}</strong> 个选中门店对课程 "{{ course.title }}" 的授权
                  </p>
                </div>
              </div>
            </div>
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

        <!-- 操作按钮 -->
        <div class="flex items-center justify-end space-x-3 pt-6 border-t border-gray-200">
          <button
            @click="$emit('close')"
            type="button"
            class="px-4 py-2 border border-gray-300 rounded-md shadow-sm text-sm font-medium text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
          >
            取消
          </button>
          <button
            @click="executeBatchOperation"
            :disabled="!operationType || loading || (needsSelection && selectedStoreIds.length === 0)"
            class="inline-flex items-center px-4 py-2 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            <Loader2 v-if="loading" class="w-4 h-4 mr-2 animate-spin" />
            <Shield v-else class="w-4 h-4 mr-2" />
            {{ loading ? '处理中...' : '执行操作' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useAdminStore } from '@/store/modules/admin'
import {
  X,
  Search,
  AlertTriangle,
  AlertCircle,
  Store,
  Shield,
  Loader2
} from 'lucide-vue-next'
import StatusBadge from '@/components/admin/StatusBadge.vue'
import type { Course, Store as StoreType, CourseAuthorization } from '@/types/api/admin'

// Props
const props = defineProps<{
  course: Course
  stores: StoreType[]
  authorizations: CourseAuthorization[]
}>()

// Emits
defineEmits<{
  close: []
  updated: []
}>()

// Store
const adminStore = useAdminStore()

// 响应式数据
const operationType = ref('')
const selectedStoreIds = ref<string[]>([])
const storeSearchQuery = ref('')
const loading = ref(false)
const error = ref('')

// 计算属性
const currentAuthorizedStores = computed(() => {
  return props.authorizations.map(auth => auth.storeId)
})

const needsSelection = computed(() => {
  return operationType.value === 'authorize_selected' || operationType.value === 'revoke_selected'
})

const filteredStoresForSelection = computed(() => {
  let result = props.stores
  
  if (storeSearchQuery.value) {
    const query = storeSearchQuery.value.toLowerCase()
    result = result.filter(store => 
      store.name.toLowerCase().includes(query) ||
      store.address?.toLowerCase().includes(query)
    )
  }
  
  return result
})

const selectedAuthorizedStores = computed(() => {
  return selectedStoreIds.value.filter(storeId => 
    currentAuthorizedStores.value.includes(storeId)
  )
})

// 方法
const isStoreCurrentlyAuthorized = (storeId: string): boolean => {
  return currentAuthorizedStores.value.includes(storeId)
}

const selectAll = () => {
  selectedStoreIds.value = filteredStoresForSelection.value.map(store => store.id)
}

const clearSelection = () => {
  selectedStoreIds.value = []
}

const executeBatchOperation = async () => {
  if (!operationType.value) return
  
  loading.value = true
  error.value = ''
  
  try {
    let operations: Promise<any>[] = []
    
    switch (operationType.value) {
      case 'authorize_all':
        // 为所有未授权的门店授权
        operations = props.stores
          .filter(store => !isStoreCurrentlyAuthorized(store.id))
          .map(store => 
            adminStore.createCourseAuthorization({
              courseId: props.course.id,
              storeId: store.id
            })
          )
        break
        
      case 'revoke_all':
        // 撤销所有已授权门店的授权
        operations = props.authorizations.map(auth => 
          adminStore.revokeCourseAuthorization({
            courseId: props.course.id,
            storeId: auth.storeId
          })
        )
        break
        
      case 'authorize_selected':
        // 为选中的未授权门店授权
        operations = selectedStoreIds.value
          .filter(storeId => !isStoreCurrentlyAuthorized(storeId))
          .map(storeId => 
            adminStore.createCourseAuthorization({
              courseId: props.course.id,
              storeId
            })
          )
        break
        
      case 'revoke_selected':
        // 撤销选中的已授权门店的授权
        operations = selectedStoreIds.value
          .filter(storeId => isStoreCurrentlyAuthorized(storeId))
          .map(storeId => 
            adminStore.revokeCourseAuthorization({
              courseId: props.course.id,
              storeId
            })
          )
        break
    }
    
    // 执行所有操作
    await Promise.all(operations)
    
    // 通知父组件更新
    $emit('updated')
    
  } catch (err: any) {
    error.value = err.message || '批量操作失败，请重试'
  } finally {
    loading.value = false
  }
}
</script>