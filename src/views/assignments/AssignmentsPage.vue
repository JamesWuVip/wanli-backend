<template>
  <div class="assignments-page">
    <!-- 页面头部 -->
    <div class="bg-white shadow-sm border-b">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="py-6">
          <div class="flex justify-between items-center">
            <div>
              <h1 class="text-2xl font-bold text-gray-900">作业管理</h1>
              <p class="mt-1 text-sm text-gray-500">管理和查看所有作业任务</p>
            </div>
            <AppButton @click="showCreateModal = true">
              <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
              </svg>
              创建作业
            </AppButton>
          </div>
        </div>
      </div>
    </div>

    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <!-- 统计卡片 -->
      <div class="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8">
        <div class="bg-white rounded-lg shadow p-6">
          <div class="flex items-center">
            <div class="flex-shrink-0">
              <div class="w-8 h-8 bg-blue-100 rounded-md flex items-center justify-center">
                <svg class="w-5 h-5 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                </svg>
              </div>
            </div>
            <div class="ml-4">
              <p class="text-sm font-medium text-gray-500">总作业数</p>
              <p class="text-2xl font-semibold text-gray-900">{{ stats.total }}</p>
            </div>
          </div>
        </div>

        <div class="bg-white rounded-lg shadow p-6">
          <div class="flex items-center">
            <div class="flex-shrink-0">
              <div class="w-8 h-8 bg-yellow-100 rounded-md flex items-center justify-center">
                <svg class="w-5 h-5 text-yellow-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
              </div>
            </div>
            <div class="ml-4">
              <p class="text-sm font-medium text-gray-500">进行中</p>
              <p class="text-2xl font-semibold text-gray-900">{{ stats.active }}</p>
            </div>
          </div>
        </div>

        <div class="bg-white rounded-lg shadow p-6">
          <div class="flex items-center">
            <div class="flex-shrink-0">
              <div class="w-8 h-8 bg-green-100 rounded-md flex items-center justify-center">
                <svg class="w-5 h-5 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
              </div>
            </div>
            <div class="ml-4">
              <p class="text-sm font-medium text-gray-500">已完成</p>
              <p class="text-2xl font-semibold text-gray-900">{{ stats.completed }}</p>
            </div>
          </div>
        </div>

        <div class="bg-white rounded-lg shadow p-6">
          <div class="flex items-center">
            <div class="flex-shrink-0">
              <div class="w-8 h-8 bg-red-100 rounded-md flex items-center justify-center">
                <svg class="w-5 h-5 text-red-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.964-.833-2.732 0L3.732 16.5c-.77.833.192 2.5 1.732 2.5z" />
                </svg>
              </div>
            </div>
            <div class="ml-4">
              <p class="text-sm font-medium text-gray-500">已逾期</p>
              <p class="text-2xl font-semibold text-gray-900">{{ stats.overdue }}</p>
            </div>
          </div>
        </div>
      </div>

      <!-- 筛选和搜索 -->
      <div class="bg-white rounded-lg shadow mb-6">
        <div class="p-6">
          <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">搜索作业</label>
              <input
                v-model="searchQuery"
                type="text"
                placeholder="输入作业标题或描述..."
                class="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
              >
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">状态筛选</label>
              <select
                v-model="statusFilter"
                class="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
              >
                <option value="">全部状态</option>
                <option value="draft">草稿</option>
                <option value="published">已发布</option>
                <option value="completed">已完成</option>
                <option value="overdue">已逾期</option>
              </select>
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">课程筛选</label>
              <select
                v-model="courseFilter"
                class="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
              >
                <option value="">全部课程</option>
                <option v-for="course in courses" :key="course.id" :value="course.id">
                  {{ course.name }}
                </option>
              </select>
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">排序方式</label>
              <select
                v-model="sortBy"
                class="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
              >
                <option value="dueDate">截止日期</option>
                <option value="createdAt">创建时间</option>
                <option value="title">标题</option>
                <option value="status">状态</option>
              </select>
            </div>
          </div>
        </div>
      </div>

      <!-- 作业列表 -->
      <div class="bg-white rounded-lg shadow">
        <div class="px-6 py-4 border-b border-gray-200">
          <h3 class="text-lg font-medium text-gray-900">作业列表</h3>
        </div>
        
        <div v-if="isLoading" class="p-8 text-center">
          <div class="inline-flex items-center px-4 py-2 font-semibold leading-6 text-sm shadow rounded-md text-gray-500 bg-white">
            <svg class="animate-spin -ml-1 mr-3 h-5 w-5 text-gray-500" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
              <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
              <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
            </svg>
            加载中...
          </div>
        </div>

        <div v-else-if="filteredAssignments.length === 0" class="p-8 text-center text-gray-500">
          <svg class="mx-auto h-12 w-12 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
          </svg>
          <p class="mt-2">暂无作业数据</p>
        </div>

        <div v-else class="divide-y divide-gray-200">
          <div
            v-for="assignment in paginatedAssignments"
            :key="assignment.id"
            class="p-6 hover:bg-gray-50 transition-colors"
          >
            <div class="flex items-center justify-between">
              <div class="flex-1">
                <div class="flex items-center space-x-3">
                  <h4 class="text-lg font-medium text-gray-900">{{ assignment.title }}</h4>
                  <span
                    :class="getStatusBadgeClass(assignment.status)"
                    class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium"
                  >
                    {{ getStatusText(assignment.status) }}
                  </span>
                  <span
                    v-if="isOverdue(assignment.dueDate)"
                    class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-red-100 text-red-800"
                  >
                    已逾期
                  </span>
                </div>
                <p class="mt-1 text-sm text-gray-600">{{ assignment.description }}</p>
                <div class="mt-2 flex items-center space-x-4 text-sm text-gray-500">
                  <span>课程：{{ getCourseNameById(assignment.courseId) }}</span>
                  <span>截止时间：{{ formatDate(assignment.dueDate) }}</span>
                  <span>提交数：{{ assignment.submissionCount }}/{{ assignment.totalStudents }}</span>
                </div>
              </div>
              <div class="flex items-center space-x-2">
                <AppButton
                  variant="outline"
                  size="sm"
                  @click="viewAssignment(assignment)"
                >
                  查看
                </AppButton>
                <AppButton
                  variant="outline"
                  size="sm"
                  @click="editAssignment(assignment)"
                >
                  编辑
                </AppButton>
                <AppButton
                  variant="outline"
                  size="sm"
                  @click="deleteAssignment(assignment)"
                  class="text-red-600 hover:text-red-700"
                >
                  删除
                </AppButton>
              </div>
            </div>
          </div>
        </div>

        <!-- 分页 -->
        <div v-if="totalPages > 1" class="px-6 py-4 border-t border-gray-200">
          <div class="flex items-center justify-between">
            <div class="text-sm text-gray-700">
              显示第 {{ (currentPage - 1) * pageSize + 1 }} - {{ Math.min(currentPage * pageSize, filteredAssignments.length) }} 条，
              共 {{ filteredAssignments.length }} 条记录
            </div>
            <div class="flex space-x-2">
              <AppButton
                variant="outline"
                size="sm"
                :disabled="currentPage === 1"
                @click="currentPage--"
              >
                上一页
              </AppButton>
              <span class="px-3 py-1 text-sm text-gray-700">
                {{ currentPage }} / {{ totalPages }}
              </span>
              <AppButton
                variant="outline"
                size="sm"
                :disabled="currentPage === totalPages"
                @click="currentPage++"
              >
                下一页
              </AppButton>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 创建/编辑作业模态框 -->
    <div v-if="showCreateModal || showEditModal" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div class="bg-white rounded-lg max-w-2xl w-full mx-4 max-h-[90vh] overflow-y-auto">
        <div class="px-6 py-4 border-b border-gray-200">
          <h3 class="text-lg font-medium text-gray-900">
            {{ showCreateModal ? '创建作业' : '编辑作业' }}
          </h3>
        </div>
        
        <form @submit.prevent="handleSubmit" class="p-6 space-y-6">
          <div>
            <label for="title" class="block text-sm font-medium text-gray-700 mb-2">
              作业标题 *
            </label>
            <input
              id="title"
              v-model="formData.title"
              type="text"
              required
              class="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
            >
          </div>

          <div>
            <label for="courseId" class="block text-sm font-medium text-gray-700 mb-2">
              所属课程 *
            </label>
            <select
              id="courseId"
              v-model="formData.courseId"
              required
              class="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
            >
              <option value="">请选择课程</option>
              <option v-for="course in courses" :key="course.id" :value="course.id">
                {{ course.name }}
              </option>
            </select>
          </div>

          <div>
            <label for="description" class="block text-sm font-medium text-gray-700 mb-2">
              作业描述
            </label>
            <textarea
              id="description"
              v-model="formData.description"
              rows="4"
              class="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
              placeholder="请输入作业描述和要求..."
            ></textarea>
          </div>

          <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
              <label for="dueDate" class="block text-sm font-medium text-gray-700 mb-2">
                截止日期 *
              </label>
              <input
                id="dueDate"
                v-model="formData.dueDate"
                type="datetime-local"
                required
                class="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
              >
            </div>

            <div>
              <label for="maxScore" class="block text-sm font-medium text-gray-700 mb-2">
                满分分数
              </label>
              <input
                id="maxScore"
                v-model.number="formData.maxScore"
                type="number"
                min="1"
                class="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
              >
            </div>
          </div>

          <div>
            <label for="status" class="block text-sm font-medium text-gray-700 mb-2">
              状态
            </label>
            <select
              id="status"
              v-model="formData.status"
              class="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
            >
              <option value="draft">草稿</option>
              <option value="published">已发布</option>
            </select>
          </div>

          <div class="flex justify-end space-x-4">
            <AppButton
              type="button"
              variant="outline"
              @click="cancelModal"
            >
              取消
            </AppButton>
            <AppButton
              type="submit"
              :loading="isSubmitting"
            >
              {{ showCreateModal ? '创建' : '保存' }}
            </AppButton>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, reactive } from 'vue'
import { useNotification } from '../../composables/useNotification'
import AppButton from '../../components/common/AppButton.vue'

interface Assignment {
  id: string
  title: string
  description: string
  courseId: string
  dueDate: string
  maxScore: number
  status: 'draft' | 'published' | 'completed' | 'overdue'
  submissionCount: number
  totalStudents: number
  createdAt: string
  updatedAt: string
}

interface Course {
  id: string
  name: string
}

interface AssignmentFormData {
  title: string
  description: string
  courseId: string
  dueDate: string
  maxScore: number
  status: 'draft' | 'published'
}

interface AssignmentStats {
  total: number
  active: number
  completed: number
  overdue: number
}

const { success: showSuccess, error: showError } = useNotification()

// 响应式数据
const isLoading = ref(false)
const isSubmitting = ref(false)
const showCreateModal = ref(false)
const showEditModal = ref(false)
const editingAssignment = ref<Assignment | null>(null)

const searchQuery = ref('')
const statusFilter = ref('')
const courseFilter = ref('')
const sortBy = ref('dueDate')
const currentPage = ref(1)
const pageSize = ref(10)

const assignments = ref<Assignment[]>([])
const courses = ref<Course[]>([])

const formData = reactive<AssignmentFormData>({
  title: '',
  description: '',
  courseId: '',
  dueDate: '',
  maxScore: 100,
  status: 'draft'
})

// 计算属性
const stats = computed((): AssignmentStats => {
  const now = new Date()
  return {
    total: assignments.value.length,
    active: assignments.value.filter(a => a.status === 'published' && new Date(a.dueDate) > now).length,
    completed: assignments.value.filter(a => a.status === 'completed').length,
    overdue: assignments.value.filter(a => new Date(a.dueDate) < now && a.status !== 'completed').length
  }
})

const filteredAssignments = computed(() => {
  let filtered = assignments.value

  // 搜索过滤
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    filtered = filtered.filter(assignment => 
      assignment.title.toLowerCase().includes(query) ||
      assignment.description.toLowerCase().includes(query)
    )
  }

  // 状态过滤
  if (statusFilter.value) {
    filtered = filtered.filter(assignment => assignment.status === statusFilter.value)
  }

  // 课程过滤
  if (courseFilter.value) {
    filtered = filtered.filter(assignment => assignment.courseId === courseFilter.value)
  }

  // 排序
  filtered.sort((a, b) => {
    switch (sortBy.value) {
      case 'dueDate':
        return new Date(a.dueDate).getTime() - new Date(b.dueDate).getTime()
      case 'createdAt':
        return new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
      case 'title':
        return a.title.localeCompare(b.title)
      case 'status':
        return a.status.localeCompare(b.status)
      default:
        return 0
    }
  })

  return filtered
})

const totalPages = computed(() => Math.ceil(filteredAssignments.value.length / pageSize.value))

const paginatedAssignments = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredAssignments.value.slice(start, end)
})

// 方法
const formatDate = (dateString: string) => {
  return new Date(dateString).toLocaleString('zh-CN')
}

const isOverdue = (dueDate: string) => {
  return new Date(dueDate) < new Date()
}

const getStatusText = (status: string) => {
  const statusMap = {
    draft: '草稿',
    published: '已发布',
    completed: '已完成',
    overdue: '已逾期'
  }
  return statusMap[status as keyof typeof statusMap] || status
}

const getStatusBadgeClass = (status: string) => {
  const classMap = {
    draft: 'bg-gray-100 text-gray-800',
    published: 'bg-blue-100 text-blue-800',
    completed: 'bg-green-100 text-green-800',
    overdue: 'bg-red-100 text-red-800'
  }
  return classMap[status as keyof typeof classMap] || 'bg-gray-100 text-gray-800'
}

const getCourseNameById = (courseId: string) => {
  const course = courses.value.find(c => c.id === courseId)
  return course?.name || '未知课程'
}

const loadAssignments = async () => {
  isLoading.value = true
  try {
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    // 模拟数据
    assignments.value = [
      {
        id: '1',
        title: 'JavaScript基础练习',
        description: '完成JavaScript基础语法练习题，包括变量、函数、对象等内容。',
        courseId: '1',
        dueDate: '2024-02-15T23:59:59',
        maxScore: 100,
        status: 'published',
        submissionCount: 25,
        totalStudents: 30,
        createdAt: '2024-01-15T10:00:00',
        updatedAt: '2024-01-15T10:00:00'
      },
      {
        id: '2',
        title: 'Vue.js项目实战',
        description: '使用Vue.js开发一个简单的待办事项应用，要求实现增删改查功能。',
        courseId: '2',
        dueDate: '2024-02-20T23:59:59',
        maxScore: 150,
        status: 'published',
        submissionCount: 18,
        totalStudents: 25,
        createdAt: '2024-01-20T14:30:00',
        updatedAt: '2024-01-20T14:30:00'
      },
      {
        id: '3',
        title: 'React组件设计',
        description: '设计并实现一个可复用的React组件库，包含按钮、输入框、模态框等组件。',
        courseId: '3',
        dueDate: '2024-01-25T23:59:59',
        maxScore: 120,
        status: 'completed',
        submissionCount: 20,
        totalStudents: 20,
        createdAt: '2024-01-10T09:15:00',
        updatedAt: '2024-01-25T16:45:00'
      }
    ]
  } catch (error) {
    console.error('加载作业列表失败:', error)
    showError('加载作业列表失败')
  } finally {
    isLoading.value = false
  }
}

const loadCourses = async () => {
  try {
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 500))
    
    // 模拟数据
    courses.value = [
      { id: '1', name: 'JavaScript基础教程' },
      { id: '2', name: 'Vue.js实战开发' },
      { id: '3', name: 'React进阶指南' },
      { id: '4', name: 'Node.js后端开发' }
    ]
  } catch (error) {
    console.error('加载课程列表失败:', error)
  }
}

const resetForm = () => {
  formData.title = ''
  formData.description = ''
  formData.courseId = ''
  formData.dueDate = ''
  formData.maxScore = 100
  formData.status = 'draft'
}

const handleSubmit = async () => {
  isSubmitting.value = true
  
  try {
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    if (showCreateModal.value) {
      // 创建新作业
      const newAssignment: Assignment = {
        id: Date.now().toString(),
        ...formData,
        submissionCount: 0,
        totalStudents: 30, // 模拟数据
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString()
      }
      assignments.value.unshift(newAssignment)
      showSuccess('作业创建成功')
    } else if (showEditModal.value && editingAssignment.value) {
      // 更新作业
      const index = assignments.value.findIndex(a => a.id === editingAssignment.value!.id)
      if (index !== -1) {
        assignments.value[index] = {
          ...assignments.value[index],
          ...formData,
          updatedAt: new Date().toISOString()
        }
      }
      showSuccess('作业更新成功')
    }
    
    cancelModal()
  } catch (error) {
    console.error('保存作业失败:', error)
    showError('保存作业失败，请稍后重试')
  } finally {
    isSubmitting.value = false
  }
}

const viewAssignment = (assignment: Assignment) => {
  // 跳转到作业详情页面
  console.log('查看作业:', assignment)
  showSuccess(`查看作业：${assignment.title}`)
}

const editAssignment = (assignment: Assignment) => {
  editingAssignment.value = assignment
  formData.title = assignment.title
  formData.description = assignment.description
  formData.courseId = assignment.courseId
  formData.dueDate = assignment.dueDate
  formData.maxScore = assignment.maxScore
  formData.status = assignment.status === 'completed' || assignment.status === 'overdue' ? 'published' : assignment.status
  showEditModal.value = true
}

const deleteAssignment = async (assignment: Assignment) => {
  if (!confirm(`确定要删除作业「${assignment.title}」吗？此操作不可撤销。`)) {
    return
  }
  
  try {
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 500))
    
    const index = assignments.value.findIndex(a => a.id === assignment.id)
    if (index !== -1) {
      assignments.value.splice(index, 1)
    }
    
    showSuccess('作业删除成功')
  } catch (error) {
    console.error('删除作业失败:', error)
    showError('删除作业失败，请稍后重试')
  }
}

const cancelModal = () => {
  showCreateModal.value = false
  showEditModal.value = false
  editingAssignment.value = null
  resetForm()
}

// 生命周期
onMounted(() => {
  loadAssignments()
  loadCourses()
})
</script>

<style scoped>
.assignments-page {
  min-height: 100vh;
  background-color: #f9fafb;
}
</style>