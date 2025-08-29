import type { Router } from 'vue-router'
import { useAuthStore } from '@/store/modules/auth'

/**
 * 设置路由守卫
 * @param router 路由实例
 */
export function setupRouterGuards(router: Router) {
  // 全局前置守卫
  router.beforeEach((to, from, next) => {
    const authStore = useAuthStore()
    
    // 设置页面标题
    if (to.meta?.title) {
      document.title = `${to.meta.title} - 万里教育管理系统`
    }
    
    // 检查是否需要认证
    if (to.meta?.requiresAuth && !authStore.isAuthenticated) {
      // 需要认证但未登录，跳转到登录页
      next({
        name: 'login',
        query: { redirect: to.fullPath }
      })
      return
    }
    
    // 检查已登录用户是否应该隐藏某些页面
    if (to.meta?.hideForAuth && authStore.isAuthenticated) {
      // 已登录用户访问登录/注册页，跳转到仪表板
      next({ name: 'dashboard' })
      return
    }
    
    // 检查用户权限（如果需要特定角色）
    if (to.meta?.roles && authStore.isAuthenticated) {
      const userRole = authStore.userRole
      const requiredRoles = Array.isArray(to.meta.roles) ? to.meta.roles : [to.meta.roles]
      
      if (!requiredRoles.includes(userRole)) {
        // 权限不足，跳转到403页面或仪表板
        next({ name: 'dashboard' })
        return
      }
    }
    
    next()
  })
  
  // 全局后置钩子
  router.afterEach((to, from) => {
    // 页面访问统计（开发环境下输出日志）
    if (import.meta.env.DEV) {
      console.log(`路由跳转: ${from.path} -> ${to.path}`)
    }
    
    // 可以在这里添加页面访问统计、埋点等逻辑
    // trackPageView(to.path, to.name)
  })
  
  // 全局解析守卫（可选）
  router.beforeResolve((to, from, next) => {
    // 在导航被确认之前，同时在所有组件内守卫和异步路由组件被解析之后调用
    // 可以在这里处理一些全局的异步操作
    next()
  })
}

/**
 * 路由元信息类型扩展
 */
declare module 'vue-router' {
  interface RouteMeta {
    /** 页面标题 */
    title?: string
    /** 是否需要认证 */
    requiresAuth?: boolean
    /** 已登录用户是否隐藏此页面 */
    hideForAuth?: boolean
    /** 需要的用户角色 */
    roles?: string | string[]
    /** 页面图标 */
    icon?: string
    /** 是否在菜单中隐藏 */
    hideInMenu?: boolean
    /** 面包屑导航 */
    breadcrumb?: boolean
  }
}