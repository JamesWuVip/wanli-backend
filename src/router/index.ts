import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: '/dashboard'
    },
    {
      path: '/login',
      name: 'Login',
      component: () => import('../views/auth/LoginPage.vue'),
      meta: { requiresGuest: true, title: '登录' }
    },
    {
      path: '/register',
      name: 'Register',
      component: () => import('../views/auth/RegisterPage.vue'),
      meta: { requiresGuest: true, title: '注册' }
    },
    {
      path: '/dashboard',
      name: 'Dashboard',
      component: () => import('../views/dashboard/DashboardPage.vue'),
      meta: { requiresAuth: true, title: '仪表盘' }
    },
    {
      path: '/courses',
      name: 'Courses',
      component: () => import('../views/courses/CoursesPage.vue'),
      meta: { requiresAuth: true, title: '课程管理' }
    },
    {
      path: '/profile',
      name: 'Profile',
      component: () => import('../views/profile/ProfilePage.vue'),
      meta: { requiresAuth: true, title: '个人资料' }
    },
    {
      path: '/assignments',
      name: 'Assignments',
      component: () => import('../views/assignments/AssignmentsPage.vue'),
      meta: { requiresAuth: true, title: '作业管理' }
    }
  ]
})

// 全局前置守卫
router.beforeEach(async (to, from, next) => {
  const authStore = useAuthStore()
  
  // 设置页面标题
  if (to.meta.title) {
    document.title = `${to.meta.title} - 万里学习平台`
  }
  
  // 检查认证状态
  if (!authStore.isAuthenticated && authStore.token) {
    try {
      await authStore.checkAuth()
    } catch (error) {
      console.error('认证检查失败:', error)
      authStore.logout()
    }
  }
  
  // 路由守卫逻辑
  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    // 需要认证但未登录，跳转到登录页
    next({ name: 'Login', query: { redirect: to.fullPath } })
  } else if (to.meta.requiresGuest && authStore.isAuthenticated) {
    // 需要游客状态但已登录，跳转到仪表盘
    next({ name: 'Dashboard' })
  } else {
    next()
  }
})

export default router