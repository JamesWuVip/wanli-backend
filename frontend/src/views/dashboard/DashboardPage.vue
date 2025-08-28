<template>
  <div class="min-h-screen bg-gray-50">
    <!-- 顶部导航栏 -->
    <header class="bg-white shadow-sm border-b border-gray-200">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="flex justify-between items-center h-16">
          <!-- Logo和标题 -->
          <div class="flex items-center">
            <div class="flex-shrink-0">
              <h1 class="text-xl font-bold text-gray-900">万里书院</h1>
            </div>
          </div>

          <!-- 用户菜单 -->
          <div class="flex items-center space-x-4">
            <div class="flex items-center space-x-2">
              <img
                v-if="authStore.user?.avatar"
                :src="authStore.user.avatar"
                :alt="authStore.user.name"
                class="w-8 h-8 rounded-full"
              />
              <span class="text-sm font-medium text-gray-700">
                {{ authStore.user?.name || '用户' }}
              </span>
            </div>
            <AppButton
              variant="outline"
              size="sm"
              @click="handleLogout"
            >
              退出登录
            </AppButton>
          </div>
        </div>
      </div>
    </header>

    <!-- 主要内容区域 -->
    <main class="max-w-7xl mx-auto py-6 px-4 sm:px-6 lg:px-8">
      <!-- 欢迎信息 -->
      <div class="mb-8">
        <h2 class="text-2xl font-bold text-gray-900 mb-2">
          欢迎回来，{{ authStore.user?.name || '用户' }}！
        </h2>
        <p class="text-gray-600">
          今天是 {{ currentDate }}，开始您的学习之旅吧。
        </p>
      </div>

      <!-- 统计卡片 -->
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
        <AppCard class="text-center">
          <template #content>
            <div class="p-6">
              <div class="text-3xl font-bold text-blue-600 mb-2">12</div>
              <div class="text-sm text-gray-600">进行中的课程</div>
            </div>
          </template>
        </AppCard>

        <AppCard class="text-center">
          <template #content>
            <div class="p-6">
              <div class="text-3xl font-bold text-green-600 mb-2">8</div>
              <div class="text-sm text-gray-600">已完成课程</div>
            </div>
          </template>
        </AppCard>

        <AppCard class="text-center">
          <template #content>
            <div class="p-6">
              <div class="text-3xl font-bold text-yellow-600 mb-2">24</div>
              <div class="text-sm text-gray-600">学习时长(小时)</div>
            </div>
          </template>
        </AppCard>

        <AppCard class="text-center">
          <template #content>
            <div class="p-6">
              <div class="text-3xl font-bold text-purple-600 mb-2">95%</div>
              <div class="text-sm text-gray-600">完成率</div>
            </div>
          </template>
        </AppCard>
      </div>

      <!-- 快速操作 -->
      <div class="grid grid-cols-1 lg:grid-cols-2 gap-8">
        <!-- 最近课程 -->
        <AppCard>
          <template #header>
            <h3 class="text-lg font-semibold text-gray-900">最近学习</h3>
          </template>
          <template #content>
            <div class="space-y-4">
              <div v-for="course in recentCourses" :key="course.id" class="flex items-center space-x-4 p-4 hover:bg-gray-50 rounded-lg transition-colors">
                <div class="w-12 h-12 bg-blue-100 rounded-lg flex items-center justify-center">
                  <svg class="w-6 h-6 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.746 0 3.332.477 4.5 1.253v13C19.832 18.477 18.246 18 16.5 18c-1.746 0-3.332.477-4.5 1.253" />
                  </svg>
                </div>
                <div class="flex-1">
                  <h4 class="text-sm font-medium text-gray-900">{{ course.title }}</h4>
                  <p class="text-sm text-gray-500">进度: {{ course.progress }}%</p>
                </div>
                <AppButton size="sm" variant="outline">
                  继续学习
                </AppButton>
              </div>
            </div>
          </template>
        </AppCard>

        <!-- 快速操作 -->
        <AppCard>
          <template #header>
            <h3 class="text-lg font-semibold text-gray-900">快速操作</h3>
          </template>
          <template #content>
            <div class="grid grid-cols-2 gap-4">
              <AppButton
                class="h-20 flex flex-col items-center justify-center space-y-2"
                variant="outline"
                @click="navigateTo('/courses')"
              >
                <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.746 0 3.332.477 4.5 1.253v13C19.832 18.477 18.246 18 16.5 18c-1.746 0-3.332.477-4.5 1.253" />
                </svg>
                <span class="text-sm">浏览课程</span>
              </AppButton>

              <AppButton
                class="h-20 flex flex-col items-center justify-center space-y-2"
                variant="outline"
                @click="navigateTo('/profile')"
              >
                <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                </svg>
                <span class="text-sm">个人资料</span>
              </AppButton>

              <AppButton
                class="h-20 flex flex-col items-center justify-center space-y-2"
                variant="outline"
                @click="navigateTo('/assignments')"
              >
                <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v10a2 2 0 002 2h8a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
                </svg>
                <span class="text-sm">作业管理</span>
              </AppButton>

              <AppButton
                class="h-20 flex flex-col items-center justify-center space-y-2"
                variant="outline"
                @click="navigateTo('/settings')"
              >
                <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z" />
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                </svg>
                <span class="text-sm">系统设置</span>
              </AppButton>
            </div>
          </template>
        </AppCard>
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useNotification } from '@/composables/useNotification'
import { AppButton, AppCard } from '@/components/ui/atoms'

interface Course {
  id: string
  title: string
  progress: number
}

const router = useRouter()
const authStore = useAuthStore()
const { success: showSuccess } = useNotification()

// 模拟最近课程数据
const recentCourses: Course[] = [
  { id: '1', title: 'Vue.js 基础教程', progress: 75 },
  { id: '2', title: 'TypeScript 进阶', progress: 45 },
  { id: '3', title: '前端工程化实践', progress: 90 }
]

// 当前日期
const currentDate = computed(() => {
  return new Date().toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    weekday: 'long'
  })
})

// 导航到指定页面
const navigateTo = (path: string) => {
  router.push(path)
}

// 退出登录
const handleLogout = async () => {
  try {
    await authStore.logout()
    showSuccess('已成功退出登录')
    router.push({ name: 'Login' })
  } catch (error) {
    console.error('退出登录失败:', error)
  }
}

// 初始化认证状态
onMounted(() => {
  authStore.initAuth()
})
</script>

<style scoped>
/* 组件特定样式 */
</style>