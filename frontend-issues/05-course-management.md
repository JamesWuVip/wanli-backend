# 课程管理系统开发

## Issue描述
实现完整的课程管理系统，包括课程列表、课程创建、课程编辑、课程详情、课时管理等核心功能。

## 任务清单

### 课程列表页面
- [ ] 实现课程列表UI布局
- [ ] 开发课程卡片组件
- [ ] 实现分页功能
- [ ] 添加搜索和筛选功能
- [ ] 实现排序功能(按时间、热度等)
- [ ] 添加课程状态标识
- [ ] 实现批量操作功能

### 课程创建页面
- [ ] 设计课程创建表单
- [ ] 实现基本信息填写
- [ ] 添加课程封面上传
- [ ] 实现富文本编辑器
- [ ] 添加课程分类选择
- [ ] 实现课程标签管理
- [ ] 添加课程定价设置
- [ ] 实现表单验证和提交

### 课程编辑页面
- [ ] 复用创建页面组件
- [ ] 实现数据预填充
- [ ] 添加编辑历史记录
- [ ] 实现草稿保存功能
- [ ] 添加版本控制
- [ ] 实现预览功能

### 课程详情页面
- [ ] 设计课程详情布局
- [ ] 实现课程信息展示
- [ ] 添加课时列表组件
- [ ] 实现学员统计显示
- [ ] 添加评价和反馈
- [ ] 实现相关课程推荐

### 课时管理功能
- [ ] 实现课时列表管理
- [ ] 添加课时创建功能
- [ ] 实现课时编辑功能
- [ ] 添加课时排序功能
- [ ] 实现课时删除功能
- [ ] 添加课时预览功能
- [ ] 实现批量课时操作

### 高级功能
- [ ] 实现课程数据统计
- [ ] 添加课程分析报告
- [ ] 实现课程导入导出
- [ ] 添加课程模板功能
- [ ] 实现课程复制功能
- [ ] 添加课程归档功能

## 页面设计规范

### 课程列表页面
```vue
<!-- CourseListView.vue -->
<template>
  <div class="course-list-container">
    <!-- 页面头部 -->
    <div class="flex justify-between items-center mb-6">
      <div>
        <h1 class="text-2xl font-bold text-gray-900">课程管理</h1>
        <p class="text-gray-600">管理您的所有课程内容</p>
      </div>
      <router-link 
        to="/courses/create" 
        class="btn btn-primary"
      >
        <PlusIcon class="w-5 h-5 mr-2" />
        创建课程
      </router-link>
    </div>
    
    <!-- 搜索和筛选 -->
    <CourseFilters 
      v-model:search="searchQuery"
      v-model:category="selectedCategory"
      v-model:status="selectedStatus"
      @filter="handleFilter"
    />
    
    <!-- 课程列表 -->
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
      <CourseCard 
        v-for="course in courses" 
        :key="course.id"
        :course="course"
        @edit="handleEdit"
        @delete="handleDelete"
        @view="handleView"
      />
    </div>
    
    <!-- 分页 -->
    <AppPagination 
      v-model:current-page="currentPage"
      :total="totalCourses"
      :page-size="pageSize"
      @change="handlePageChange"
    />
  </div>
</template>
```

### 课程创建表单
```vue
<!-- CourseCreateView.vue -->
<template>
  <div class="course-create-container">
    <div class="max-w-4xl mx-auto">
      <div class="bg-white shadow-sm rounded-lg">
        <div class="px-6 py-4 border-b border-gray-200">
          <h2 class="text-xl font-semibold text-gray-900">
            {{ isEdit ? '编辑课程' : '创建新课程' }}
          </h2>
        </div>
        
        <form @submit.prevent="handleSubmit" class="p-6 space-y-6">
          <!-- 基本信息 -->
          <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
            <AppInput
              v-model="form.title"
              label="课程标题"
              placeholder="请输入课程标题"
              required
              :error="errors.title"
            />
            
            <AppSelect
              v-model="form.categoryId"
              label="课程分类"
              :options="categories"
              placeholder="请选择课程分类"
              required
              :error="errors.categoryId"
            />
          </div>
          
          <!-- 课程封面 -->
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">
              课程封面
            </label>
            <ImageUpload
              v-model="form.coverImage"
              :max-size="2"
              accept="image/*"
              @upload="handleImageUpload"
            />
          </div>
          
          <!-- 课程描述 -->
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">
              课程描述
            </label>
            <RichTextEditor
              v-model="form.description"
              placeholder="请输入课程详细描述"
              :min-height="200"
            />
          </div>
          
          <!-- 课程设置 -->
          <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
            <AppInput
              v-model="form.price"
              type="number"
              label="课程价格"
              placeholder="0.00"
              step="0.01"
              min="0"
            />
            
            <AppInput
              v-model="form.duration"
              type="number"
              label="课程时长(小时)"
              placeholder="0"
              min="0"
            />
            
            <AppSelect
              v-model="form.difficulty"
              label="难度等级"
              :options="difficultyOptions"
              placeholder="请选择难度"
            />
          </div>
          
          <!-- 提交按钮 -->
          <div class="flex justify-end space-x-4">
            <AppButton
              type="button"
              variant="outline"
              @click="saveDraft"
              :loading="saving"
            >
              保存草稿
            </AppButton>
            
            <AppButton
              type="submit"
              :loading="submitting"
            >
              {{ isEdit ? '更新课程' : '创建课程' }}
            </AppButton>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>
```

## API接口对接

### 课程管理API
```typescript
// api/modules/courses.ts
export const coursesApi = {
  // 获取课程列表
  async getCourses(params: CourseListParams): Promise<ApiResponse<PaginatedResponse<Course>>> {
    return await apiClient.get('/courses', { params })
  },
  
  // 获取课程详情
  async getCourse(id: string): Promise<ApiResponse<Course>> {
    return await apiClient.get(`/courses/${id}`)
  },
  
  // 创建课程
  async createCourse(data: CreateCourseData): Promise<ApiResponse<Course>> {
    return await apiClient.post('/courses', data)
  },
  
  // 更新课程
  async updateCourse(id: string, data: UpdateCourseData): Promise<ApiResponse<Course>> {
    return await apiClient.put(`/courses/${id}`, data)
  },
  
  // 删除课程
  async deleteCourse(id: string): Promise<ApiResponse<void>> {
    return await apiClient.delete(`/courses/${id}`)
  },
  
  // 获取课程统计
  async getCourseStats(id: string): Promise<ApiResponse<CourseStats>> {
    return await apiClient.get(`/courses/${id}/stats`)
  },
  
  // 上传课程封面
  async uploadCover(file: File): Promise<ApiResponse<{ url: string }>> {
    const formData = new FormData()
    formData.append('file', file)
    return await apiClient.post('/courses/upload-cover', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  }
}
```

### 状态管理
```typescript
// stores/courses.ts
export const useCoursesStore = defineStore('courses', {
  state: (): CoursesState => ({
    courses: [],
    currentCourse: null,
    loading: false,
    error: null,
    pagination: {
      current: 1,
      pageSize: 12,
      total: 0
    },
    filters: {
      search: '',
      category: '',
      status: '',
      sortBy: 'createdAt',
      sortOrder: 'desc'
    }
  }),
  
  actions: {
    async fetchCourses(params?: Partial<CourseListParams>) {
      this.loading = true
      this.error = null
      
      try {
        const response = await coursesApi.getCourses({
          page: this.pagination.current,
          pageSize: this.pagination.pageSize,
          ...this.filters,
          ...params
        })
        
        this.courses = response.data.items
        this.pagination.total = response.data.total
      } catch (error) {
        this.error = error.message
        throw error
      } finally {
        this.loading = false
      }
    },
    
    async createCourse(data: CreateCourseData) {
      try {
        const response = await coursesApi.createCourse(data)
        this.courses.unshift(response.data)
        return response.data
      } catch (error) {
        this.error = error.message
        throw error
      }
    },
    
    async updateCourse(id: string, data: UpdateCourseData) {
      try {
        const response = await coursesApi.updateCourse(id, data)
        const index = this.courses.findIndex(c => c.id === id)
        if (index !== -1) {
          this.courses[index] = response.data
        }
        return response.data
      } catch (error) {
        this.error = error.message
        throw error
      }
    }
  }
})
```

## 组件设计

### 课程卡片组件
```vue
<!-- components/CourseCard.vue -->
<template>
  <div class="bg-white rounded-lg shadow-sm border border-gray-200 overflow-hidden hover:shadow-md transition-shadow">
    <!-- 课程封面 -->
    <div class="aspect-video bg-gray-100 relative">
      <img 
        v-if="course.coverImage" 
        :src="course.coverImage" 
        :alt="course.title"
        class="w-full h-full object-cover"
      />
      <div v-else class="flex items-center justify-center h-full text-gray-400">
        <PhotoIcon class="w-12 h-12" />
      </div>
      
      <!-- 状态标识 -->
      <div class="absolute top-2 right-2">
        <span 
          :class="statusClasses[course.status]"
          class="px-2 py-1 text-xs font-medium rounded-full"
        >
          {{ statusLabels[course.status] }}
        </span>
      </div>
    </div>
    
    <!-- 课程信息 -->
    <div class="p-4">
      <h3 class="font-semibold text-gray-900 mb-2 line-clamp-2">
        {{ course.title }}
      </h3>
      
      <p class="text-sm text-gray-600 mb-3 line-clamp-2">
        {{ course.description }}
      </p>
      
      <!-- 课程统计 -->
      <div class="flex items-center justify-between text-sm text-gray-500 mb-4">
        <div class="flex items-center space-x-4">
          <span class="flex items-center">
            <UsersIcon class="w-4 h-4 mr-1" />
            {{ course.studentCount || 0 }}
          </span>
          <span class="flex items-center">
            <ClockIcon class="w-4 h-4 mr-1" />
            {{ course.duration }}h
          </span>
        </div>
        
        <span class="font-medium text-primary-600">
          ¥{{ course.price || '免费' }}
        </span>
      </div>
      
      <!-- 操作按钮 -->
      <div class="flex space-x-2">
        <AppButton
          size="sm"
          variant="outline"
          @click="$emit('view', course)"
          class="flex-1"
        >
          查看
        </AppButton>
        
        <AppButton
          size="sm"
          @click="$emit('edit', course)"
          class="flex-1"
        >
          编辑
        </AppButton>
        
        <AppButton
          size="sm"
          variant="danger"
          @click="$emit('delete', course)"
        >
          <TrashIcon class="w-4 h-4" />
        </AppButton>
      </div>
    </div>
  </div>
</template>
```

## 验收标准
- 课程CRUD操作完整可用
- 列表分页和搜索功能正常
- 表单验证规则正确生效
- 文件上传功能正常工作
- 富文本编辑器功能完善
- 响应式设计适配各设备
- 数据状态管理正确
- 错误处理机制完善

## 技术要求
- 使用Vue 3 Composition API
- 集成Pinia状态管理
- 支持TypeScript类型检查
- 实现组件复用和模块化
- 遵循无障碍访问标准
- 实现性能优化(虚拟滚动等)

## 优先级
高优先级 - 核心业务功能

## 预估工时
3个工作日

## 相关文档
- SP1前端开发方案-产品需求文档.md
- SP1前端开发方案-技术架构文档.md
- 后端API接口文档