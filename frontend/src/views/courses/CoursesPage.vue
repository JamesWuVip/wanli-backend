<template>
  <div class="courses-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">课程管理</h1>
        <p class="page-description">管理和浏览所有课程内容</p>
      </div>
      <div class="header-actions">
        <AppButton
          variant="primary"
          @click="showCreateCourseModal = true"
        >
          <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
          </svg>
          创建课程
        </AppButton>
      </div>
    </div>

    <!-- 搜索和筛选 -->
    <div class="filters-section">
      <div class="search-bar">
        <div class="search-input-wrapper">
          <svg class="search-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
          </svg>
          <input
            v-model="searchQuery"
            type="text"
            placeholder="搜索课程名称、描述或标签..."
            class="search-input"
            @input="handleSearch"
          />
        </div>
      </div>
      
      <div class="filter-controls">
        <select v-model="selectedCategory" class="filter-select" @change="handleFilter">
          <option value="">所有分类</option>
          <option value="programming">编程开发</option>
          <option value="design">设计创意</option>
          <option value="business">商业管理</option>
          <option value="language">语言学习</option>
          <option value="science">科学技术</option>
        </select>
        
        <select v-model="selectedStatus" class="filter-select" @change="handleFilter">
          <option value="">所有状态</option>
          <option value="active">进行中</option>
          <option value="completed">已完成</option>
          <option value="draft">草稿</option>
        </select>
        
        <select v-model="sortBy" class="filter-select" @change="handleSort">
          <option value="created_at">创建时间</option>
          <option value="updated_at">更新时间</option>
          <option value="name">课程名称</option>
          <option value="students_count">学生数量</option>
        </select>
      </div>
    </div>

    <!-- 课程统计 -->
    <div class="stats-section">
      <div class="stat-card">
        <div class="stat-icon">
          <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.746 0 3.332.477 4.5 1.253v13C19.832 18.477 18.246 18 16.5 18c-1.746 0-3.332.477-4.5 1.253" />
          </svg>
        </div>
        <div class="stat-content">
          <div class="stat-number">{{ totalCourses }}</div>
          <div class="stat-label">总课程数</div>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-icon">
          <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z" />
          </svg>
        </div>
        <div class="stat-content">
          <div class="stat-number">{{ totalStudents }}</div>
          <div class="stat-label">总学生数</div>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-icon">
          <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
        </div>
        <div class="stat-content">
          <div class="stat-number">{{ activeCourses }}</div>
          <div class="stat-label">进行中课程</div>
        </div>
      </div>
    </div>

    <!-- 课程列表 -->
    <div class="courses-grid">
      <div
        v-for="course in filteredCourses"
        :key="course.id"
        class="course-card"
      >
        <div class="course-image">
          <img :src="course.thumbnail || '/placeholder-course.jpg'" :alt="course.name" />
          <div class="course-status" :class="course.status">
            {{ getStatusText(course.status) }}
          </div>
        </div>
        
        <div class="course-content">
          <div class="course-header">
            <h3 class="course-title">{{ course.name }}</h3>
            <div class="course-category">{{ getCategoryText(course.category) }}</div>
          </div>
          
          <p class="course-description">{{ course.description }}</p>
          
          <div class="course-meta">
            <div class="meta-item">
              <svg class="meta-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z" />
              </svg>
              <span>{{ course.studentsCount }} 学生</span>
            </div>
            
            <div class="meta-item">
              <svg class="meta-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
              <span>{{ course.duration }} 小时</span>
            </div>
            
            <div class="meta-item">
              <svg class="meta-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3a4 4 0 118 0v4m-4 8a4 4 0 11-8 0v-1a4 4 0 014-4h4a4 4 0 014 4v1a4 4 0 11-8 0z" />
              </svg>
              <span>{{ formatDate(course.createdAt) }}</span>
            </div>
          </div>
          
          <div class="course-actions">
            <AppButton
              variant="outline"
              size="small"
              @click="viewCourse(course.id)"
            >
              查看详情
            </AppButton>
            
            <AppButton
              variant="outline"
              size="small"
              @click="editCourse(course.id)"
            >
              编辑
            </AppButton>
            
            <AppButton
              variant="danger"
              size="small"
              @click="deleteCourse(course.id)"
            >
              删除
            </AppButton>
          </div>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-if="filteredCourses.length === 0" class="empty-state">
      <div class="empty-icon">
        <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.746 0 3.332.477 4.5 1.253v13C19.832 18.477 18.246 18 16.5 18c-1.746 0-3.332.477-4.5 1.253" />
        </svg>
      </div>
      <h3 class="empty-title">暂无课程</h3>
      <p class="empty-description">还没有创建任何课程，点击上方按钮开始创建第一个课程吧！</p>
      <AppButton
        variant="primary"
        @click="showCreateCourseModal = true"
      >
        创建第一个课程
      </AppButton>
    </div>

    <!-- 分页 -->
    <div v-if="totalPages > 1" class="pagination">
      <AppButton
        variant="outline"
        size="small"
        :disabled="currentPage === 1"
        @click="goToPage(currentPage - 1)"
      >
        上一页
      </AppButton>
      
      <div class="page-numbers">
        <button
          v-for="page in visiblePages"
          :key="page"
          class="page-number"
          :class="{ active: page === currentPage }"
          @click="goToPage(page)"
        >
          {{ page }}
        </button>
      </div>
      
      <AppButton
        variant="outline"
        size="small"
        :disabled="currentPage === totalPages"
        @click="goToPage(currentPage + 1)"
      >
        下一页
      </AppButton>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import AppButton from '@/components/ui/atoms/AppButton/AppButton.vue'
import { useNotification } from '@/composables/useNotification'

// 路由
const router = useRouter()

// 通知
const { success: showSuccess, error: showError, confirm } = useNotification()

// 响应式数据
const searchQuery = ref('')
const selectedCategory = ref('')
const selectedStatus = ref('')
const sortBy = ref('created_at')
const currentPage = ref(1)
const pageSize = ref(12)
const showCreateCourseModal = ref(false)

// 模拟课程数据
const courses = ref([
  {
    id: '1',
    name: 'Vue.js 3 完整教程',
    description: '从零开始学习Vue.js 3，包含Composition API、TypeScript等现代开发技术',
    category: 'programming',
    status: 'active',
    thumbnail: '/course-vue.jpg',
    studentsCount: 156,
    duration: 24,
    createdAt: '2024-01-15T10:00:00Z',
    updatedAt: '2024-01-20T15:30:00Z'
  },
  {
    id: '2',
    name: 'UI/UX 设计基础',
    description: '学习现代UI/UX设计原理，掌握Figma等设计工具的使用',
    category: 'design',
    status: 'active',
    thumbnail: '/course-design.jpg',
    studentsCount: 89,
    duration: 18,
    createdAt: '2024-01-10T09:00:00Z',
    updatedAt: '2024-01-18T14:20:00Z'
  },
  {
    id: '3',
    name: '项目管理实战',
    description: '学习敏捷开发、Scrum框架等现代项目管理方法',
    category: 'business',
    status: 'completed',
    thumbnail: '/course-pm.jpg',
    studentsCount: 234,
    duration: 32,
    createdAt: '2023-12-01T08:00:00Z',
    updatedAt: '2024-01-05T16:45:00Z'
  },
  {
    id: '4',
    name: 'Python 数据分析',
    description: '使用Python进行数据分析，包含pandas、numpy、matplotlib等库的使用',
    category: 'programming',
    status: 'draft',
    thumbnail: '/course-python.jpg',
    studentsCount: 0,
    duration: 28,
    createdAt: '2024-01-25T11:00:00Z',
    updatedAt: '2024-01-25T11:00:00Z'
  }
])

// 计算属性
const filteredCourses = computed(() => {
  let filtered = courses.value

  // 搜索过滤
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    filtered = filtered.filter(course => 
      course.name.toLowerCase().includes(query) ||
      course.description.toLowerCase().includes(query)
    )
  }

  // 分类过滤
  if (selectedCategory.value) {
    filtered = filtered.filter(course => course.category === selectedCategory.value)
  }

  // 状态过滤
  if (selectedStatus.value) {
    filtered = filtered.filter(course => course.status === selectedStatus.value)
  }

  // 排序
  filtered.sort((a, b) => {
    switch (sortBy.value) {
      case 'name':
        return a.name.localeCompare(b.name)
      case 'students_count':
        return b.studentsCount - a.studentsCount
      case 'updated_at':
        return new Date(b.updatedAt).getTime() - new Date(a.updatedAt).getTime()
      case 'created_at':
      default:
        return new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
    }
  })

  // 分页
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filtered.slice(start, end)
})

const totalCourses = computed(() => courses.value.length)
const totalStudents = computed(() => courses.value.reduce((sum, course) => sum + course.studentsCount, 0))
const activeCourses = computed(() => courses.value.filter(course => course.status === 'active').length)
const totalPages = computed(() => Math.ceil(courses.value.length / pageSize.value))

const visiblePages = computed(() => {
  const pages = []
  const start = Math.max(1, currentPage.value - 2)
  const end = Math.min(totalPages.value, currentPage.value + 2)
  
  for (let i = start; i <= end; i++) {
    pages.push(i)
  }
  
  return pages
})

// 方法
const handleSearch = () => {
  currentPage.value = 1
}

const handleFilter = () => {
  currentPage.value = 1
}

const handleSort = () => {
  currentPage.value = 1
}

const goToPage = (page: number) => {
  currentPage.value = page
}

const viewCourse = (courseId: string) => {
  router.push(`/courses/${courseId}`)
}

const editCourse = (courseId: string) => {
  router.push(`/courses/${courseId}/edit`)
}

const deleteCourse = async (courseId: string) => {
  const course = courses.value.find(c => c.id === courseId)
  if (!course) return

  const confirmed = await confirm(
    '确认删除',
    `确定要删除课程「${course.name}」吗？此操作不可撤销。`
  )

  if (confirmed) {
    courses.value = courses.value.filter(c => c.id !== courseId)
    showSuccess('课程删除成功')
  }
}

const getStatusText = (status: string) => {
  const statusMap: Record<string, string> = {
    active: '进行中',
    completed: '已完成',
    draft: '草稿'
  }
  return statusMap[status] || status
}

const getCategoryText = (category: string) => {
  const categoryMap: Record<string, string> = {
    programming: '编程开发',
    design: '设计创意',
    business: '商业管理',
    language: '语言学习',
    science: '科学技术'
  }
  return categoryMap[category] || category
}

const formatDate = (dateString: string) => {
  return new Date(dateString).toLocaleDateString('zh-CN')
}

// 生命周期
onMounted(() => {
  // 这里可以调用API获取课程数据
  console.log('课程页面已加载')
})
</script>

<style scoped>
.courses-page {
  padding: 24px;
  max-width: 1200px;
  margin: 0 auto;
}

/* 页面头部 */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 32px;
  padding-bottom: 24px;
  border-bottom: 1px solid #e5e7eb;
}

.header-content {
  flex: 1;
}

.page-title {
  font-size: 32px;
  font-weight: 700;
  color: #111827;
  margin: 0 0 8px 0;
}

.page-description {
  font-size: 16px;
  color: #6b7280;
  margin: 0;
}

.header-actions {
  display: flex;
  gap: 12px;
}

/* 搜索和筛选 */
.filters-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-bottom: 32px;
  padding: 24px;
  background: #f9fafb;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
}

.search-bar {
  display: flex;
  gap: 16px;
}

.search-input-wrapper {
  position: relative;
  flex: 1;
}

.search-icon {
  position: absolute;
  left: 12px;
  top: 50%;
  transform: translateY(-50%);
  width: 20px;
  height: 20px;
  color: #9ca3af;
}

.search-input {
  width: 100%;
  padding: 12px 12px 12px 44px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  font-size: 14px;
  background: white;
  transition: all 0.2s;
}

.search-input:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.filter-controls {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}

.filter-select {
  padding: 8px 12px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 14px;
  background: white;
  min-width: 120px;
}

.filter-select:focus {
  outline: none;
  border-color: #3b82f6;
}

/* 统计卡片 */
.stats-section {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 32px;
}

.stat-card {
  display: flex;
  align-items: center;
  padding: 20px;
  background: white;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 10px;
  background: #eff6ff;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
}

.stat-icon svg {
  width: 24px;
  height: 24px;
  color: #3b82f6;
}

.stat-content {
  flex: 1;
}

.stat-number {
  font-size: 24px;
  font-weight: 700;
  color: #111827;
  line-height: 1;
}

.stat-label {
  font-size: 14px;
  color: #6b7280;
  margin-top: 4px;
}

/* 课程网格 */
.courses-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 24px;
  margin-bottom: 32px;
}

.course-card {
  background: white;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  overflow: hidden;
  transition: all 0.2s;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.course-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.course-image {
  position: relative;
  height: 200px;
  overflow: hidden;
}

.course-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.course-status {
  position: absolute;
  top: 12px;
  right: 12px;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
  color: white;
}

.course-status.active {
  background: #10b981;
}

.course-status.completed {
  background: #6b7280;
}

.course-status.draft {
  background: #f59e0b;
}

.course-content {
  padding: 20px;
}

.course-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}

.course-title {
  font-size: 18px;
  font-weight: 600;
  color: #111827;
  margin: 0;
  flex: 1;
  margin-right: 12px;
}

.course-category {
  padding: 4px 8px;
  background: #eff6ff;
  color: #3b82f6;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
  white-space: nowrap;
}

.course-description {
  font-size: 14px;
  color: #6b7280;
  line-height: 1.5;
  margin: 0 0 16px 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.course-meta {
  display: flex;
  gap: 16px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #6b7280;
}

.meta-icon {
  width: 14px;
  height: 14px;
}

.course-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 64px 24px;
}

.empty-icon {
  width: 64px;
  height: 64px;
  margin: 0 auto 24px;
  color: #d1d5db;
}

.empty-icon svg {
  width: 100%;
  height: 100%;
}

.empty-title {
  font-size: 20px;
  font-weight: 600;
  color: #374151;
  margin: 0 0 8px 0;
}

.empty-description {
  font-size: 16px;
  color: #6b7280;
  margin: 0 0 24px 0;
}

/* 分页 */
.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 8px;
  margin-top: 32px;
}

.page-numbers {
  display: flex;
  gap: 4px;
}

.page-number {
  padding: 8px 12px;
  border: 1px solid #d1d5db;
  background: white;
  color: #374151;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
}

.page-number:hover {
  background: #f3f4f6;
}

.page-number.active {
  background: #3b82f6;
  color: white;
  border-color: #3b82f6;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .courses-page {
    padding: 16px;
  }
  
  .page-header {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }
  
  .filters-section {
    padding: 16px;
  }
  
  .filter-controls {
    flex-direction: column;
  }
  
  .filter-select {
    min-width: auto;
  }
  
  .courses-grid {
    grid-template-columns: 1fr;
  }
  
  .stats-section {
    grid-template-columns: 1fr;
  }
  
  .course-actions {
    justify-content: stretch;
  }
  
  .course-actions > * {
    flex: 1;
  }
}
</style>