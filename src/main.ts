import { createApp } from 'vue'
import './style.css'
import App from './App.vue'
import router, { setupRouterGuards } from './router'
import pinia from './store'

// 创建Vue应用实例
const app = createApp(App)

// 使用Pinia状态管理
app.use(pinia)

// 使用路由
app.use(router)

// 在Pinia初始化后设置路由守卫
setupRouterGuards(router)

// 挂载应用
app.mount('#app')
