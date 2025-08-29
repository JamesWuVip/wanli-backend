<template>
  <div class="courses-page">
    <!-- 页面标题 -->
    <div class="flex items-center justify-between mb-6">
      <h1 class="text-2xl font-bold text-gray-900">课程管理</h1>
      <div class="flex items-center space-x-3">
        <button
          @click="showCreateModal = true"
          class="inline-flex items-center px-4 py-2 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
        >
          <Plus class="w-4 h-4 mr-2" />
          创建课程
        </button>
        <button
          @click="refreshCourses"
          :disabled="loading"
          class="inline-flex items-center px-4 py-2 border border-gray-300 rounded-md shadow-sm text-sm font-medium text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-50 disabled:cursor-not-allowed"
        >
          <RefreshCw class="w-4 h-4 mr-2" :class="{ 'animate-spin': loading }" />
          刷新
        </button>
      </div>
    </div>

    <!-- 搜索和筛选 -->
    <div class="bg-white rounded-lg shadow mb-6">
      <div class="p-6">
        <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
          <!-- 搜索框 -->
          <div class="relative">
            <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
              <Search class="h-5 w-5 text-gray-400" />
            </div>
            <input
              v-model="searchQuery"
              type="text"
              placeholder="搜索课程名称..."
              class="block w-full pl-10 pr-3 py-2 border border-gray-300 rounded-md leading-5 bg-white placeholder-gray-500 focus:outline-none focus:placeholder-gray-400 focus:ring-1 focus:ring-blue-500 focus:border-blue-500"
            />
          </div>
          
          <!-- 状态筛选 -->
          <div>
            <select
              v-model="statusFilter"
              class="block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
            >
              <option value="">全部状态</option>
              <option value="active">已发布</option>
              <option value="draft">草稿</option>
              <option value="archived">已归档</option>
            </select>
          </div>
          
          <!-- 类型筛选 -->
          <div>
            <select
              v-model="typeFilter"
              class="block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
            >
              <option value="">全部类型</option>
              <option value="video">视频课程</option>
              <option value="live">直播课程</option>
              <option value="text">图文课程</option>
            </select>
          </div>
          
          <!-- 重置按钮 -->
          <div>
            <button
              @click="resetFilters"
              class="w-full px-4 py-2 border border-gray-300 rounded-md shadow-sm text-sm font-medium text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
            >
              重置筛选
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 错误提示 -->
    <div v-if="errors.courses" class="mb-6">
      <div class="bg-red-50 border border-red-200 rounded-md p-4">
        <div class="flex">
          <AlertCircle class="h-5 w-5 text-red-400" />
          <div class="ml-3">
            <h3 class="text-sm font-medium text-red-800">加载失败</h3>
            <div class="mt-2 text-sm text-red-700">
              {{ errors.courses }}
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 课程列表 -->
    <div class="bg-white shadow rounded-lg">
      <!-- 加载状态 -->
      <div v-if="loading" class="p-8 text-center">
        <RefreshCw class="w-8 h-8 animate-spin mx-auto text-gray-400 mb-4" />
        <p class="text-gray-500">加载中...</p>
      </div>
      
      <!-- 无课程提示 -->
      <div v-else-if="!paginatedCourses.length" class="p-8 text-center">
        <BookOpen class="w-12 h-12 mx-auto text-gray-400 mb-4" />
        <h3 class="text-lg font-medium text-gray-900 mb-2">暂无课程</h3>
        <p class="text-gray-500 mb-4">
          {{ searchQuery || statusFilter || typeFilter ? '没有找到符合条件的课程' : '还没有创建任何课程' }}
        </p>
        <button
          v-if="!searchQuery && !statusFilter && !typeFilter"
          @click="showCreateModal = true"
          class="inline-flex items-center px-4 py-2 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-blue-600 hover:bg-blue-700"
        >
          <Plus class="w-4 h-4 mr-2" />
          创建第一个课程
        </button>
      </div>
      
      <!-- 课程列表 -->
      <div v-else class="divide-y divide-gray-200">
        <div
          v-for="course in paginatedCourses"
          :key="course.id"
          class="p-6 hover:bg-gray-50 transition-colors duration-200"
        >
          <div class="flex items-start justify-between">
            <div class="flex-1 min-w-0">
              <!-- 课程标题和状态 -->
              <div class="flex items-center mb-2">
                <h3 class="text-lg font-medium text-gray-900 truncate mr-3">
                  {{ course.title }}
                </h3>
                <StatusBadge :status="getStatusType(course.status)" />
                <span class="ml-2 inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-gray-100 text-gray-800">
                  {{ getTypeLabel(course.type) }}
                </span>
              </div>
              
              <!-- 课程描述 -->
              <p v-if="course.description" class="text-sm text-gray-600 mb-3 line-clamp-2">
                {{ course.description }}
              </p>
              
              <!-- 课程统计信息 -->
              <div class="flex items-center space-x-6 text-sm text-gray-500">
                <div class="flex items-center">
                  <Users class="w-4 h-4 mr-1" />
                  <span>{{ course.studentCount || 0 }} 学生</span>
                </div>
                <div class="flex items-center">
                  <Clock class="w-4 h-4 mr-1" />
                  <span>{{ course.duration || 0 }} 分钟</span>
                </div>
                <div class="flex items-center">
                  <Calendar class="w-4 h-4 mr-1" />
                  <span>{{ formatDate(course.createdAt) }}</span>
                </div>
                <div class="flex items-center">
                  <User class="w-4 h-4 mr-1" />
                  <span>{{ course.creator?.name || '未知' }}</span>
                </div>
              </div>
            </div>
            
            <!-- 操作按钮 -->
            <div class="flex items-center space-x-2 ml-4">
              <button
                @click="showAuthorizationModal(course)"
                class="inline-flex items-center px-3 py-1.5 border border-gray-300 rounded-md text-sm font-medium text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
              >
                <Shield class="w-4 h-4 mr-1" />
                授权管理
              </button>
              <button
                @click="editCourse(course)"
                class="inline-flex items-center px-3 py-1.5 border border-gray-300 rounded-md text-sm font-medium text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
              >
                <Edit class="w-4 h-4 mr-1" />
                编辑
              </button>
              <button
                @click="confirmDelete(course)"
                class="inline-flex items-center px-3 py-1.5 border border-red-300 rounded-md text-sm font-medium text-red-700 bg-white hover:bg-red-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-red-500"
              >
                <Trash2 class="w-4 h-4 mr-1" />
                删除
              </button>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 分页 -->
      <div v-if="totalPages > 1" class="px-6 py-4 border-t border-gray-200">
        <div class="flex items-center justify-between">
          <div class="text-sm text-gray-700">
            显示 {{ (currentPage - 1) * pageSize + 1 }} 到 {{ Math.min(currentPage * pageSize, filteredCourses.length) }} 条，
            共 {{ filteredCourses.length }} 条记录
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

    <!-- 创建课程模态框 -->
    <CourseCreateModal
      v-if="showCreateModal"
      @close="showCreateModal = false"
      @created="handleCourseCreated"
    />
    
    <!-- 编辑课程模态框 -->
    <CourseEditModal
      v-if="showEditModal && selectedCourse"
      :course="selectedCourse"
      @close="showEditModal = false"
      @updated="handleCourseUpdated"
    />
    
    <!-- 课程授权模态框 -->
    <CourseAuthorizationModal
      v-if="showAuthModal && selectedCourse"
      :course="selectedCourse"
      @close="showAuthModal = false"
      @updated="handleAuthorizationUpdated"
    />
    
    <!-- 删除确认模态框 -->
    <ConfirmModal
      v-if="showDeleteModal"
      title="删除课程"
      :message="deleteMessage"
      confirm-text="删除"
      confirm-type="danger"
      @confirm="handleDelete"
      @cancel="showDeleteModal = false"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useAdminStore } from '@/store/modules/admin'
import {
  Plus,
  RefreshCw,
  Search,
  AlertCircle,
  BookOpen,
  Users,
  Clock,
  Calendar,
  User,
  Shield,
  Edit,
  Trash2
} from 'lucide-vue-next'
import StatusBadge from '@/components/admin/StatusBadge.vue'
import CourseCreateModal from '@/components/admin/CourseCreateModal.vue'
import CourseEditModal from '@/components/admin/CourseEditModal.vue'
import CourseAuthorizationModal from '@/components/admin/CourseAuthorizationModal.vue'
import ConfirmModal from '@/components/common/ConfirmModal.vue'
import type { Course } from '@/types/api/admin'

// Store
const adminStore = useAdminStore()

// 响应式数据
const searchQuery = ref('')
const statusFilter = ref('')
const typeFilter = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const showCreateModal = ref(false)
const showEditModal = ref(false)
const showAuthModal = ref(false)
const showDeleteModal = ref(false)
const selectedCourse = ref<Course | null>(null)

// 计算属性
const { courses, loading, errors } = adminStore

const filteredCourses = computed(() => {
  let result = courses.value || []
  
  // 搜索过滤
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    result = result.filter(course => 
      course.title.toLowerCase().includes(query) ||
      course.description?.toLowerCase().includes(query)
    )
  }
  
  // 状态过滤
  if (statusFilter.value) {
    result = result.filter(course => course.status === statusFilter.value)
  }
  
  // 类型过滤
  if (typeFilter.value) {
    result = result.filter(course => course.type === typeFilter.value)
  }
  
  return result
})

const totalPages = computed(() => {
  return Math.ceil(filteredCourses.value.length / pageSize.value)
})

const paginatedCourses = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredCourses.value.slice(start, end)
})

const deleteMessage = computed(() => {
  return selectedCourse.value 
    ? `确定要删除课程 "${selectedCourse.value.title}" 吗？此操作不可撤销。`
    : '确定要删除此课程吗？此操作不可撤销。'
})

// 方法
const refreshCourses = async () => {
  await adminStore.loadCourses()
}

const resetFilters = () => {
  searchQuery.value = ''
  statusFilter.value = ''
  typeFilter.value = ''
  currentPage.value = 1
}

const getStatusType = (status: string) => {
  const statusMap: Record<string, 'healthy' | 'warning' | 'error' | 'unknown'> = {
    active: 'healthy',
    draft: 'warning',
    archived: 'error'
  }
  return statusMap[status] || 'unknown'
}

const getTypeLabel = (type: string): string => {
  const typeMap: Record<string, string> = {
    video: '视频课程',
    live: '直播课程',
    text: '图文课程'
  }
  return typeMap[type] || type
}

const formatDate = (dateString: string): string => {
  return new Date(dateString).toLocaleDateString('zh-CN')
}

const editCourse = (course: Course) => {
  selectedCourse.value = course
  showEditModal.value = true
}

const showAuthorizationModal = (course: Course) => {
  selectedCourse.value = course
  showAuthModal.value = true
}

const confirmDelete = (course: Course) => {
  selectedCourse.value = course
  showDeleteModal.value = true
}

const handleCourseCreated = (course: Course) => {
  showCreateModal.value = false
  refreshCourses()
}

const handleCourseUpdated = (course: Course) => {
  showEditModal.value = false
  selectedCourse.value = null
  refreshCourses()
}

const handleAuthorizationUpdated = () => {
  showAuthModal.value = false
  selectedCourse.value = null
}

const handleDelete = async () => {
  if (!selectedCourse.value) return
  
  try {
    await adminStore.deleteCourse(selectedCourse.value.id)
    showDeleteModal.value = false
    selectedCourse.value = null
    await refreshCourses()
  } catch (error) {
    console.error('Failed to delete course:', error)
  }
}

// 监听搜索和筛选变化，重置分页
watch([searchQuery, statusFilter, typeFilter], () => {
  currentPage.value = 1
})

// 生命周期
onMounted(async () => {
  await refreshCourses()
})
</script>

<style scoped>
.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>