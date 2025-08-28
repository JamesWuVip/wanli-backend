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
      meta: {
        title: '登录 - 万里学习平台',
        requiresAuth: false
      }
    },
    {
      path: '/register',
      name: 'Register',
      component: () => import('../views/auth/RegisterPage.vue'),
      meta: {
        title: '注册 - 万里学习平台',
        requiresAuth: false
      }
    },
    {
      path: '/dashboard',
      name: 'Dashboard',
      component: () => import('../views/dashboard/DashboardPage.vue'),
      meta: {
        title: '仪表盘 - 万里学习平台',
        requiresAuth: true
      }
    },
    {
      path: '/courses',
      name: 'Courses',
      component: () => import('../views/courses/CoursesPage.vue'),
      meta: {
        title: '课程管理 - 万里学习平台',
        requiresAuth: true
      }
    },
    {
      path: '/profile',
      name: 'Profile',
      component: () => import('../views/profile/ProfilePage.vue'),
      meta: {
        title: '个人资料 - 万里学习平台',
        requiresAuth: true
      }
    }
  ]
})

// 全局前置守卫
router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()
  
  // 设置页面标题
  if (to.meta.title) {
    document.title = to.meta.title as string
  }
  
  // 检查路由是否需要认证
  if (to.meta.requiresAuth) {
    if (authStore.isAuthenticated) {
      next()
    } else {
      // 未登录，重定向到登录页
      next({
        name: 'Login',
        query: { redirect: to.fullPath }
      })
    }
  } else {
    // 不需要认证的路由
    if (authStore.isAuthenticated && (to.name === 'Login' || to.name === 'Register')) {
      // 已登录用户访问登录/注册页，重定向到仪表盘
      next({ name: 'Dashboard' })
    } else {
      next()
    }
  }
})

export default router