import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { lazyRouteFactory, smartPreloader, createNetworkAwarePreloader } from '@/utils/lazy-routes'
import { useAuthStore } from '@/store/modules/auth'
import { useNotification } from '@/composables/useNotification'
import { authGuard, adminGuard, guestGuard, combineGuards } from './guards'

// 网络感知预加载器
const networkPreloader = createNetworkAwarePreloader()

// 懒加载页面组件
const HomePage = () => import('@/views/HomePage.vue')
const LoginPage = () => import('@/views/auth/LoginPage.vue')
const RegisterPage = () => import('@/views/auth/RegisterPage.vue')
const DashboardPage = () => import('@/views/dashboard/DashboardPage.vue')
const CoursesPage = () => import('@/views/courses/CoursesPage.vue')
const CourseDetailPage = () => import('@/views/courses/CourseDetailPage.vue')
const CreateCoursePage = () => import('@/views/courses/CreateCoursePage.vue')
const ProfilePage = () => import('@/views/profile/ProfilePage.vue')
const AdminDashboard = () => import('@/views/admin/AdminDashboard.vue')
const AdminUsers = () => import('@/views/admin/AdminUsers.vue')
const AdminCourses = () => import('@/views/admin/AdminCourses.vue')
const NotFoundPage = () => import('@/views/NotFoundPage.vue')

const routes: RouteRecordRaw[] = [
  lazyRouteFactory.create('/', HomePage, {
    name: 'Home',
    meta: {
      title: '首页',
      requiresAuth: false,
      preload: true // 首页优先预加载
    },
    lazy: {
      priority: 'high'
    }
  }),
  {
    path: '/auth',
    children: [
      {
        path: 'login',
        name: 'Login',
        component: LoginPage,
        meta: {
          title: '登录',
          requiresAuth: false,
          hideForAuth: true
        }
      },
      {
        path: 'register',
        name: 'Register',
        component: RegisterPage,
        meta: {
          title: '注册',
          requiresAuth: false,
          hideForAuth: true
        }
      }
    ]
  },
  lazyRouteFactory.create('/dashboard', DashboardPage, {
    name: 'Dashboard',
    meta: {
      title: '仪表板',
      requiresAuth: true,
      preload: true // 认证后常用页面
    }
  }),
  lazyRouteFactory.create('/courses', CoursesPage, {
    name: 'Courses',
    meta: {
      title: '课程',
      requiresAuth: false,
      preload: true // 核心功能页面
    }
  }),
  lazyRouteFactory.create('/courses/:id', CourseDetailPage, {
    name: 'CourseDetail',
    meta: {
      title: '课程详情',
      requiresAuth: false
    }
  }),
  lazyRouteFactory.create('/courses/create', CreateCoursePage, {
    name: 'CreateCourse',
    meta: {
      title: '创建课程',
      requiresAuth: true
    }
  }),
  lazyRouteFactory.create('/profile', ProfilePage, {
    name: 'Profile',
    meta: {
      title: '个人资料',
      requiresAuth: true
    }
  }),
  lazyRouteFactory.createNested('/admin', AdminDashboard, [
    {
      path: 'users',
      name: 'AdminUsers',
      component: lazyRouteFactory.create('', AdminUsers).component,
      meta: {
        title: '用户管理',
        requiresAuth: true,
        requiresAdmin: true
      }
    },
    {
      path: 'courses',
      name: 'AdminCourses',
      component: lazyRouteFactory.create('', AdminCourses).component,
      meta: {
        title: '课程管理',
        requiresAuth: true,
        requiresAdmin: true
      }
    }
  ], {
    name: 'Admin',
    meta: {
      title: '管理后台',
      requiresAuth: true,
      requiresAdmin: true
    },
    lazy: {
      delay: 100 // 管理页面延迟加载
    }
  }),
  lazyRouteFactory.create('/:pathMatch(.*)*', NotFoundPage, {
    name: 'NotFound',
    meta: {
      title: '页面未找到'
    }
  })
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
  scrollBehavior(to, from, savedPosition) {
    // 路由切换时的滚动行为
    if (savedPosition) {
      return savedPosition
    } else {
      return { top: 0 }
    }
  }
})

// 智能预加载逻辑
function setupSmartPreloading() {
  // 根据网络状况决定预加载策略
  const strategy = networkPreloader.getPreloadStrategy()
  
  if (strategy === 'disabled') {
    return
  }

  // 预加载核心页面
  const coreRoutes = routes.filter(route => route.meta?.preload)
  const importFunctions = coreRoutes.map(route => {
    const component = route.component as () => Promise<any>
    return component
  }).filter(Boolean)

  if (strategy === 'aggressive') {
    // 积极预加载策略：立即预加载所有核心页面
    importFunctions.forEach(importFn => {
      smartPreloader.add(importFn)
    })
    smartPreloader.start()
  } else {
    // 保守预加载策略：延迟预加载
    setTimeout(() => {
      importFunctions.slice(0, 2).forEach(importFn => {
        smartPreloader.add(importFn)
      })
      smartPreloader.start()
    }, 2000)
  }
}

// 路由守卫
router.beforeEach(async (to, from, next) => {
  // 设置页面标题
  if (to.meta?.title) {
    document.title = `${to.meta.title} - 万里学院`
  } else {
    document.title = '万里学院'
  }

  // 使用组合守卫来处理认证、权限和游客状态检查
  const combinedGuard = combineGuards(guestGuard, authGuard, adminGuard)
  await combinedGuard(to, from, next)
})

// 路由加载完成后的处理
router.afterEach((to, from) => {
  // 预加载相关页面
  if (to.name === 'Home') {
    // 在首页时预加载课程页面
    smartPreloader.add(() => import('@/views/courses/CoursesPage.vue'))
  } else if (to.name === 'Login') {
    // 在登录页时预加载仪表板
    smartPreloader.add(() => import('@/views/dashboard/DashboardPage.vue'))
  } else if (to.name === 'Courses') {
    // 在课程列表页时预加载课程详情页
    smartPreloader.add(() => import('@/views/courses/CourseDetailPage.vue'))
  }
  
  // 启动预加载
  if (networkPreloader.shouldPreload()) {
    smartPreloader.start()
  }
})

// 初始化智能预加载
if (typeof window !== 'undefined') {
  // 页面加载完成后启动预加载
  if (document.readyState === 'complete') {
    setupSmartPreloading()
  } else {
    window.addEventListener('load', setupSmartPreloading)
  }
}



export default router;