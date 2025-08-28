import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

// 路由配置
const routes: RouteRecordRaw[] = [
  {
    path: '/',
    redirect: '/dashboard'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/LoginPage.vue'),
    meta: {
      requiresAuth: false,
      title: '登录 - 万里书院'
    }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/auth/RegisterPage.vue'),
    meta: {
      requiresAuth: false,
      title: '注册 - 万里书院'
    }
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('@/views/dashboard/DashboardPage.vue'),
    meta: {
      requiresAuth: true,
      title: '仪表盘 - 万里书院'
    }
  },
  {
    path: '/courses',
    name: 'Courses',
    component: () => import('@/views/courses/CoursesPage.vue'),
    meta: {
      requiresAuth: true,
      title: '课程 - 万里书院'
    }
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('@/views/profile/ProfilePage.vue'),
    meta: {
      requiresAuth: true,
      title: '个人资料 - 万里书院'
    }
  },
  {
    path: '/assignments',
    name: 'Assignments',
    component: () => import('@/views/assignments/AssignmentsPage.vue'),
    meta: {
      requiresAuth: true,
      title: '作业管理 - 万里书院'
    }
  },
  {
    path: '/settings',
    name: 'Settings',
    component: () => import('@/views/settings/SettingsPage.vue'),
    meta: {
      requiresAuth: true,
      title: '系统设置 - 万里书院'
    }
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/error/NotFoundPage.vue'),
    meta: {
      requiresAuth: false,
      title: '页面未找到 - 万里书院'
    }
  }
]

// 创建路由实例
const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition
    } else {
      return { top: 0 }
    }
  }
})

// 全局前置守卫
router.beforeEach(async (to, from, next) => {
  const authStore = useAuthStore()
  
  // 初始化认证状态
  if (!authStore.user && !authStore.token) {
    authStore.initAuth()
  }
  
  // 设置页面标题
  if (to.meta.title) {
    document.title = to.meta.title as string
  }
  
  // 检查路由是否需要认证
  const requiresAuth = to.meta.requiresAuth !== false
  const isAuthenticated = authStore.isAuthenticated
  
  if (requiresAuth && !isAuthenticated) {
    // 需要认证但未登录，跳转到登录页
    next({
      name: 'Login',
      query: { redirect: to.fullPath }
    })
  } else if (!requiresAuth && isAuthenticated && (to.name === 'Login' || to.name === 'Register')) {
    // 已登录用户访问登录/注册页，跳转到仪表盘
    next({ name: 'Dashboard' })
  } else {
    // 正常访问
    next()
  }
})

// 全局后置钩子
router.afterEach((to, from) => {
  // 可以在这里添加页面访问统计等逻辑
  console.log(`路由跳转: ${from.path} -> ${to.path}`)
})

export default router