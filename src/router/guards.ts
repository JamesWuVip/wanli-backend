import { Router } from 'vue-router'
import { useAuthStore } from '@/store/modules/auth'
import { useUserStore } from '@/store/modules/user'

/**
 * 设置路由守卫
 * @param router 路由实例
 */
export function setupRouterGuards(router: Router) {
  // 全局前置守卫
  router.beforeEach((to, from, next) => {
    // 延迟获取store，确保Pinia已初始化
    const authStore = useAuthStore()
    const userStore = useUserStore()
    
    // 设置页面标题
    if (to.meta?.title) {
      document.title = to.meta.title as string
    }
    
    // 检查是否需要认证
    if (to.meta?.requiresAuth) {
      if (!authStore.isAuthenticated) {
        // 未登录，重定向到登录页
        next({
          name: 'login',
          query: { redirect: to.fullPath }
        })
        return
      }
      
      // 检查角色权限
      if (to.meta?.roles) {
        const requiredRoles = to.meta.roles as string[]
        const userRoles = userStore.user?.roles || []
        
        const hasPermission = requiredRoles.some(role => 
          userRoles.includes(role)
        )
        
        if (!hasPermission) {
          // 权限不足，重定向到仪表板
          next({ name: 'dashboard' })
          return
        }
      }
    }
    
    // 检查已登录用户是否应该隐藏某些页面
    if (to.meta?.hideForAuth && authStore.isAuthenticated) {
      next({ name: 'dashboard' })
      return
    }
    
    next()
  })
  
  // 全局后置守卫
  router.afterEach((to) => {
    // 页面加载完成后的处理
    console.log(`导航到: ${to.path}`)
  })
}
