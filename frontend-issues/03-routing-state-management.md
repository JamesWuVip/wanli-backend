# 路由系统和状态管理架构

## Issue描述
实现Vue Router路由系统和Pinia状态管理架构，包括路由守卫、权限控制和API客户端配置。

## 任务清单

### Vue Router配置
- [ ] 配置路由实例和基础路由
- [ ] 实现路由懒加载
- [ ] 配置路由元信息(meta)
- [ ] 实现嵌套路由结构
- [ ] 配置路由别名和重定向

### 路由守卫系统
- [ ] 实现全局前置守卫(beforeEach)
- [ ] 实现认证检查守卫
- [ ] 实现权限控制守卫
- [ ] 实现页面标题设置
- [ ] 实现路由跳转进度条

### Pinia状态管理
- [ ] 配置Pinia实例
- [ ] 创建认证状态store(auth)
- [ ] 创建用户状态store(user)
- [ ] 创建课程状态store(course)
- [ ] 创建应用状态store(app)
- [ ] 实现状态持久化

### API客户端配置
- [ ] 配置Axios实例
- [ ] 实现请求拦截器(添加Token)
- [ ] 实现响应拦截器(错误处理)
- [ ] 实现API缓存机制
- [ ] 配置请求超时和重试

### 类型定义
- [ ] 定义路由类型接口
- [ ] 定义状态管理类型
- [ ] 定义API请求响应类型
- [ ] 定义用户权限类型

## 路由结构
```typescript
const routes = [
  {
    path: '/',
    redirect: '/dashboard',
    meta: { requiresAuth: true }
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/LoginView.vue'),
    meta: { public: true, title: '登录' }
  },
  {
    path: '/register',
    name: 'Register', 
    component: () => import('@/views/auth/RegisterView.vue'),
    meta: { public: true, title: '注册' }
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('@/views/dashboard/DashboardView.vue'),
    meta: { requiresAuth: true, title: '仪表盘' }
  },
  {
    path: '/courses',
    name: 'Courses',
    component: () => import('@/views/courses/CourseListView.vue'),
    meta: { requiresAuth: true, title: '课程管理' }
  },
  {
    path: '/courses/create',
    name: 'CreateCourse',
    component: () => import('@/views/courses/CreateCourseView.vue'),
    meta: { requiresAuth: true, title: '创建课程' }
  },
  {
    path: '/courses/:id',
    name: 'CourseDetail',
    component: () => import('@/views/courses/CourseDetailView.vue'),
    meta: { requiresAuth: true, title: '课程详情' }
  }
]
```

## 状态管理架构
```typescript
// stores/auth.ts
export const useAuthStore = defineStore('auth', {
  state: (): AuthState => ({
    user: null,
    token: localStorage.getItem('token'),
    isAuthenticated: false,
    loading: false,
    error: null
  }),
  getters: {
    isLoggedIn: (state) => !!state.token && !!state.user,
    userRole: (state) => state.user?.role,
    userName: (state) => state.user?.fullName || state.user?.username
  },
  actions: {
    async login(credentials: LoginCredentials) { /* 实现 */ },
    async logout() { /* 实现 */ },
    async refreshToken() { /* 实现 */ }
  }
})
```

## API拦截器配置
```typescript
// 请求拦截器
axios.interceptors.request.use(
  (config) => {
    const authStore = useAuthStore()
    if (authStore.token) {
      config.headers.Authorization = `Bearer ${authStore.token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// 响应拦截器
axios.interceptors.response.use(
  (response) => response,
  async (error) => {
    if (error.response?.status === 401) {
      const authStore = useAuthStore()
      await authStore.logout()
      router.push('/login')
    }
    return Promise.reject(error)
  }
)
```

## 验收标准
- 路由跳转正常，懒加载生效
- 认证守卫正确拦截未登录用户
- 权限控制按角色正确工作
- 状态管理数据流正确
- API拦截器正确处理Token和错误
- 页面刷新后状态正确恢复

## 技术要求
- 使用Vue Router 4最新版本
- 使用Pinia作为状态管理
- 所有异步操作要有loading状态
- 错误处理要用户友好
- 支持TypeScript类型检查

## 优先级
高优先级 - 核心架构组件

## 预估工时
1个工作日

## 相关文档
- SP1前端开发方案-技术架构文档.md
- Vue Router官方文档
- Pinia官方文档