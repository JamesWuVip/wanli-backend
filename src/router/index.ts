import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { setupRouterGuards } from './guards'

// 定义路由配置
const routes: RouteRecordRaw[] = [
  {
    path: '/',
    redirect: '/dashboard'
  },
  {
    path: '/login',
    name: 'login',
    component: () => import('@/views/auth/LoginPage.vue'),
    meta: {
      title: '登录',
      requiresAuth: false,
      hideForAuth: true // 已登录用户隐藏
    }
  },
  {
    path: '/register',
    name: 'register',
    component: () => import('@/views/auth/RegisterPage.vue'),
    meta: {
      title: '注册',
      requiresAuth: false,
      hideForAuth: true
    }
  },
  {
    path: '/dashboard',
    name: 'dashboard',
    component: () => import('@/views/dashboard/DashboardPage.vue'),
    meta: {
      title: '仪表板',
      requiresAuth: false
    }
  },
  {
    path: '/profile',
    name: 'profile',
    component: () => import('@/views/user/ProfilePage.vue'),
    meta: {
      title: '个人资料',
      requiresAuth: true
    }
  },
  // 系统管理员路由
  {
    path: '/admin',
    name: 'admin',
    redirect: '/admin/dashboard',
    meta: {
      title: '系统管理',
      requiresAuth: true,
      roles: ['admin']
    }
  },
  {
    path: '/admin/dashboard',
    name: 'adminDashboard',
    component: () => import('@/views/admin/DashboardPage.vue'),
    meta: {
      title: '管理员仪表盘',
      requiresAuth: true,
      roles: ['admin']
    }
  },
  {
    path: '/admin/courses',
    name: 'adminCourses',
    component: () => import('@/views/admin/CoursesPage.vue'),
    meta: {
      title: '课程管理',
      requiresAuth: true,
      roles: ['admin']
    }
  },
  {    path: '/admin/stores',    name: 'adminStores',    component: () => import('@/views/admin/StoresPage.vue'),    meta: {      title: '门店管理',      requiresAuth: true,      roles: ['admin']    }  },  {    path: '/admin/users',    name: 'adminUsers',    component: () => import('@/views/admin/UsersPage.vue'),    meta: {      title: '用户管理',      requiresAuth: true,      roles: ['admin']    }  },  {    path: '/admin/analytics',    name: 'adminAnalytics',    component: () => import('@/views/admin/AnalyticsPage.vue'),    meta: {      title: '数据分析',      requiresAuth: true,      roles: ['admin']    }  },  {    path: '/admin/settings',    name: 'adminSettings',    component: () => import('@/views/admin/SettingsPage.vue'),    meta: {      title: '系统设置',      requiresAuth: true,      roles: ['admin']    }  },
  // 课程管理路由
  {
    path: '/courses',
    name: 'courses',
    component: () => import('@/views/courses/CoursesPage.vue'),
    meta: {
      title: '课程管理',
      requiresAuth: true
    }
  },
  {
    path: '/courses/create',
    name: 'courseCreate',
    component: () => import('@/views/courses/CourseCreatePage.vue'),
    meta: {
      title: '创建课程',
      requiresAuth: true
    }
  },
  {
    path: '/courses/:id',
    name: 'courseDetail',
    component: () => import('@/views/courses/CourseDetailPage.vue'),
    meta: {
      title: '课程详情',
      requiresAuth: true
    }
  },
  {
    path: '/courses/:id/edit',
    name: 'courseEdit',
    component: () => import('@/views/courses/CourseEditPage.vue'),
    meta: {
      title: '编辑课程',
      requiresAuth: true
    }
  },
  {
    path: '/courses/:courseId/lessons',
    name: 'lessons',
    component: () => import('@/views/lessons/LessonsPage.vue'),
    meta: {
      title: '课时管理',
      requiresAuth: true
    }
  },
  {
    path: '/courses/:courseId/lessons/create',
    name: 'lessonCreate',
    component: () => import('@/views/lessons/LessonCreatePage.vue'),
    meta: {
      title: '创建课时',
      requiresAuth: true
    }
  },
  {
    path: '/courses/:courseId/lessons/:id/edit',
    name: 'lessonEdit',
    component: () => import('@/views/lessons/LessonEditPage.vue'),
    meta: {
      title: '编辑课时',
      requiresAuth: true
    }
  },

  {
    path: '/404',
    name: 'notFound',
    component: {
      template: '<div class="text-center text-xl p-8">404 - 页面未找到</div>'
    },
    meta: {
      title: '页面未找到'
    }
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/404'
  }
]

// 创建路由实例
const router = createRouter({
  history: createWebHistory(),
  routes
})

// 设置路由守卫
setupRouterGuards(router)

export default router