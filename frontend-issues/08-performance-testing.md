# 性能优化和测试开发

## Issue描述
实现前端应用的性能优化策略，建立完善的测试体系，确保应用的稳定性、性能和用户体验。

## 任务清单

### 性能优化
- [ ] 代码分割和懒加载实现
- [ ] 组件级别的懒加载
- [ ] 路由级别的代码分割
- [ ] 第三方库按需加载
- [ ] 图片懒加载和优化
- [ ] 资源预加载策略
- [ ] Bundle分析和优化
- [ ] Tree Shaking配置

### 缓存策略
- [ ] HTTP缓存配置
- [ ] Service Worker实现
- [ ] 本地存储优化
- [ ] API响应缓存
- [ ] 静态资源缓存
- [ ] 离线缓存策略
- [ ] 缓存失效机制
- [ ] 缓存性能监控

### 渲染优化
- [ ] 虚拟滚动实现
- [ ] 组件渲染优化
- [ ] 防抖和节流处理
- [ ] 内存泄漏检测
- [ ] DOM操作优化
- [ ] 事件处理优化
- [ ] 响应式数据优化
- [ ] 计算属性优化

### 单元测试
- [ ] 测试环境配置
- [ ] 组件单元测试
- [ ] 工具函数测试
- [ ] API服务测试
- [ ] Store状态测试
- [ ] 路由测试
- [ ] 测试覆盖率配置
- [ ] 测试报告生成

### 集成测试
- [ ] 端到端测试配置
- [ ] 用户流程测试
- [ ] API集成测试
- [ ] 跨浏览器测试
- [ ] 响应式测试
- [ ] 性能测试
- [ ] 可访问性测试
- [ ] 安全测试

### 监控和分析
- [ ] 性能监控集成
- [ ] 错误监控配置
- [ ] 用户行为分析
- [ ] 页面加载性能
- [ ] 运行时性能监控
- [ ] 内存使用监控
- [ ] 网络请求监控
- [ ] 用户体验指标

## 性能优化实现

### Vite配置优化
```typescript
// vite.config.ts
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'
import { visualizer } from 'rollup-plugin-visualizer'
import { compression } from 'vite-plugin-compression'

export default defineConfig({
  plugins: [
    vue(),
    // Bundle分析
    visualizer({
      filename: 'dist/stats.html',
      open: true,
      gzipSize: true
    }),
    // Gzip压缩
    compression({
      algorithm: 'gzip'
    })
  ],
  
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src')
    }
  },
  
  build: {
    // 代码分割
    rollupOptions: {
      output: {
        manualChunks: {
          // 将Vue相关库打包到vendor chunk
          vendor: ['vue', 'vue-router', 'pinia'],
          // 将UI库单独打包
          ui: ['@headlessui/vue', '@heroicons/vue'],
          // 将工具库单独打包
          utils: ['axios', 'dayjs', 'lodash-es']
        }
      }
    },
    
    // 压缩配置
    minify: 'terser',
    terserOptions: {
      compress: {
        drop_console: true,
        drop_debugger: true
      }
    },
    
    // 资源内联阈值
    assetsInlineLimit: 4096,
    
    // 启用CSS代码分割
    cssCodeSplit: true
  },
  
  // 开发服务器优化
  server: {
    hmr: {
      overlay: false
    }
  },
  
  // 预构建优化
  optimizeDeps: {
    include: [
      'vue',
      'vue-router',
      'pinia',
      'axios'
    ]
  }
})
```

### 路由懒加载
```typescript
// router/index.ts
import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/HomeView.vue'),
    meta: {
      title: '首页'
    }
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/LoginView.vue'),
    meta: {
      title: '登录',
      requiresGuest: true
    }
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('@/views/dashboard/DashboardView.vue'),
    meta: {
      title: '仪表盘',
      requiresAuth: true
    }
  },
  {
    path: '/courses',
    name: 'Courses',
    component: () => import('@/views/courses/CoursesView.vue'),
    meta: {
      title: '课程管理',
      requiresAuth: true
    },
    children: [
      {
        path: '',
        name: 'CoursesList',
        component: () => import('@/views/courses/CoursesList.vue')
      },
      {
        path: 'create',
        name: 'CourseCreate',
        component: () => import('@/views/courses/CourseCreate.vue')
      },
      {
        path: ':id',
        name: 'CourseDetail',
        component: () => import('@/views/courses/CourseDetail.vue')
      },
      {
        path: ':id/edit',
        name: 'CourseEdit',
        component: () => import('@/views/courses/CourseEdit.vue')
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition
    }
    return { top: 0 }
  }
})

// 路由性能监控
router.beforeEach((to, from, next) => {
  const start = performance.now()
  
  // 设置页面标题
  if (to.meta?.title) {
    document.title = `${to.meta.title} - 万里教育平台`
  }
  
  next()
  
  // 记录路由切换时间
  router.afterEach(() => {
    const end = performance.now()
    console.log(`Route navigation took ${end - start} milliseconds`)
  })
})

export default router
```

### 组件懒加载
```vue
<!-- components/LazyComponent.vue -->
<template>
  <div>
    <!-- 加载状态 -->
    <div v-if="loading" class="flex items-center justify-center p-8">
      <AppSpinner size="lg" />
      <span class="ml-2 text-gray-600">加载中...</span>
    </div>
    
    <!-- 错误状态 -->
    <div v-else-if="error" class="text-center p-8">
      <ExclamationTriangleIcon class="w-12 h-12 mx-auto text-red-500 mb-2" />
      <p class="text-gray-600 mb-4">组件加载失败</p>
      <AppButton @click="retry" variant="outline" size="sm">
        重试
      </AppButton>
    </div>
    
    <!-- 组件内容 -->
    <component v-else :is="component" v-bind="$attrs" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, defineAsyncComponent } from 'vue'
import type { Component } from 'vue'

interface Props {
  loader: () => Promise<Component>
  delay?: number
  timeout?: number
}

const props = withDefaults(defineProps<Props>(), {
  delay: 200,
  timeout: 10000
})

const loading = ref(false)
const error = ref(false)
const component = ref<Component | null>(null)

const loadComponent = async () => {
  loading.value = true
  error.value = false
  
  try {
    // 延迟显示加载状态
    const delayTimer = setTimeout(() => {
      loading.value = true
    }, props.delay)
    
    // 超时处理
    const timeoutTimer = setTimeout(() => {
      throw new Error('Component load timeout')
    }, props.timeout)
    
    const loadedComponent = await props.loader()
    
    clearTimeout(delayTimer)
    clearTimeout(timeoutTimer)
    
    component.value = loadedComponent.default || loadedComponent
  } catch (err) {
    console.error('Failed to load component:', err)
    error.value = true
  } finally {
    loading.value = false
  }
}

const retry = () => {
  loadComponent()
}

onMounted(() => {
  loadComponent()
})
</script>
```

### 虚拟滚动实现
```vue
<!-- components/VirtualList.vue -->
<template>
  <div
    ref="containerRef"
    class="virtual-list-container"
    :style="{ height: `${containerHeight}px` }"
    @scroll="handleScroll"
  >
    <!-- 总高度占位 -->
    <div :style="{ height: `${totalHeight}px`, position: 'relative' }">
      <!-- 可见项目 -->
      <div
        v-for="item in visibleItems"
        :key="getItemKey(item.data)"
        :style="{
          position: 'absolute',
          top: `${item.top}px`,
          left: 0,
          right: 0,
          height: `${itemHeight}px`
        }"
        class="virtual-list-item"
      >
        <slot :item="item.data" :index="item.index" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'

interface Props {
  items: any[]
  itemHeight: number
  containerHeight: number
  overscan?: number
  getItemKey: (item: any) => string | number
}

const props = withDefaults(defineProps<Props>(), {
  overscan: 5
})

const containerRef = ref<HTMLElement>()
const scrollTop = ref(0)

// 计算总高度
const totalHeight = computed(() => props.items.length * props.itemHeight)

// 计算可见范围
const visibleRange = computed(() => {
  const containerHeight = props.containerHeight
  const itemHeight = props.itemHeight
  
  const startIndex = Math.floor(scrollTop.value / itemHeight)
  const endIndex = Math.min(
    startIndex + Math.ceil(containerHeight / itemHeight),
    props.items.length - 1
  )
  
  // 添加overscan
  const overscanStart = Math.max(0, startIndex - props.overscan)
  const overscanEnd = Math.min(props.items.length - 1, endIndex + props.overscan)
  
  return {
    start: overscanStart,
    end: overscanEnd
  }
})

// 计算可见项目
const visibleItems = computed(() => {
  const { start, end } = visibleRange.value
  const items = []
  
  for (let i = start; i <= end; i++) {
    items.push({
      index: i,
      data: props.items[i],
      top: i * props.itemHeight
    })
  }
  
  return items
})

// 滚动处理
const handleScroll = (event: Event) => {
  const target = event.target as HTMLElement
  scrollTop.value = target.scrollTop
}

// 滚动到指定项目
const scrollToItem = (index: number) => {
  if (containerRef.value) {
    const top = index * props.itemHeight
    containerRef.value.scrollTop = top
  }
}

// 滚动到顶部
const scrollToTop = () => {
  scrollToItem(0)
}

// 滚动到底部
const scrollToBottom = () => {
  scrollToItem(props.items.length - 1)
}

defineExpose({
  scrollToItem,
  scrollToTop,
  scrollToBottom
})
</script>

<style scoped>
.virtual-list-container {
  overflow-y: auto;
  overflow-x: hidden;
}

.virtual-list-item {
  box-sizing: border-box;
}
</style>
```

## 测试配置

### Vitest配置
```typescript
// vitest.config.ts
import { defineConfig } from 'vitest/config'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

export default defineConfig({
  plugins: [vue()],
  
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src')
    }
  },
  
  test: {
    environment: 'jsdom',
    globals: true,
    setupFiles: ['./src/test/setup.ts'],
    
    // 覆盖率配置
    coverage: {
      provider: 'v8',
      reporter: ['text', 'json', 'html'],
      exclude: [
        'node_modules/',
        'src/test/',
        '**/*.d.ts',
        '**/*.config.*',
        'dist/'
      ],
      thresholds: {
        global: {
          branches: 80,
          functions: 80,
          lines: 80,
          statements: 80
        }
      }
    },
    
    // 测试文件匹配
    include: [
      'src/**/*.{test,spec}.{js,ts,vue}',
      'tests/**/*.{test,spec}.{js,ts}'
    ],
    
    // 测试超时
    testTimeout: 10000
  }
})
```

### 测试工具配置
```typescript
// src/test/setup.ts
import { vi } from 'vitest'
import { config } from '@vue/test-utils'

// 全局测试配置
config.global.stubs = {
  // 路由组件存根
  'router-link': true,
  'router-view': true,
  
  // 图标组件存根
  'heroicons': true
}

// Mock全局对象
Object.defineProperty(window, 'matchMedia', {
  writable: true,
  value: vi.fn().mockImplementation(query => ({
    matches: false,
    media: query,
    onchange: null,
    addListener: vi.fn(),
    removeListener: vi.fn(),
    addEventListener: vi.fn(),
    removeEventListener: vi.fn(),
    dispatchEvent: vi.fn()
  }))
})

// Mock IntersectionObserver
class MockIntersectionObserver {
  observe = vi.fn()
  disconnect = vi.fn()
  unobserve = vi.fn()
}

Object.defineProperty(window, 'IntersectionObserver', {
  writable: true,
  configurable: true,
  value: MockIntersectionObserver
})

Object.defineProperty(global, 'IntersectionObserver', {
  writable: true,
  configurable: true,
  value: MockIntersectionObserver
})

// Mock ResizeObserver
class MockResizeObserver {
  observe = vi.fn()
  disconnect = vi.fn()
  unobserve = vi.fn()
}

Object.defineProperty(window, 'ResizeObserver', {
  writable: true,
  configurable: true,
  value: MockResizeObserver
})
```

### 组件测试示例
```typescript
// src/components/__tests__/AppButton.test.ts
import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import AppButton from '../AppButton.vue'

describe('AppButton', () => {
  it('renders correctly with default props', () => {
    const wrapper = mount(AppButton, {
      slots: {
        default: 'Click me'
      }
    })
    
    expect(wrapper.text()).toBe('Click me')
    expect(wrapper.classes()).toContain('btn')
    expect(wrapper.classes()).toContain('btn-primary')
  })
  
  it('applies variant classes correctly', () => {
    const wrapper = mount(AppButton, {
      props: {
        variant: 'secondary'
      },
      slots: {
        default: 'Button'
      }
    })
    
    expect(wrapper.classes()).toContain('btn-secondary')
  })
  
  it('applies size classes correctly', () => {
    const wrapper = mount(AppButton, {
      props: {
        size: 'lg'
      },
      slots: {
        default: 'Button'
      }
    })
    
    expect(wrapper.classes()).toContain('btn-lg')
  })
  
  it('shows loading state correctly', () => {
    const wrapper = mount(AppButton, {
      props: {
        loading: true
      },
      slots: {
        default: 'Button'
      }
    })
    
    expect(wrapper.classes()).toContain('btn-loading')
    expect(wrapper.find('.spinner').exists()).toBe(true)
    expect(wrapper.attributes('disabled')).toBeDefined()
  })
  
  it('emits click event when clicked', async () => {
    const wrapper = mount(AppButton, {
      slots: {
        default: 'Button'
      }
    })
    
    await wrapper.trigger('click')
    
    expect(wrapper.emitted('click')).toHaveLength(1)
  })
  
  it('does not emit click when disabled', async () => {
    const wrapper = mount(AppButton, {
      props: {
        disabled: true
      },
      slots: {
        default: 'Button'
      }
    })
    
    await wrapper.trigger('click')
    
    expect(wrapper.emitted('click')).toBeUndefined()
  })
  
  it('does not emit click when loading', async () => {
    const wrapper = mount(AppButton, {
      props: {
        loading: true
      },
      slots: {
        default: 'Button'
      }
    })
    
    await wrapper.trigger('click')
    
    expect(wrapper.emitted('click')).toBeUndefined()
  })
})
```

### API测试示例
```typescript
// src/api/__tests__/auth.test.ts
import { describe, it, expect, vi, beforeEach } from 'vitest'
import axios from 'axios'
import { authApi } from '../auth'

// Mock axios
vi.mock('axios')
const mockedAxios = vi.mocked(axios)

describe('authApi', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })
  
  describe('login', () => {
    it('should login successfully', async () => {
      const mockResponse = {
        data: {
          success: true,
          data: {
            token: 'mock-token',
            user: {
              id: '1',
              email: 'test@example.com',
              fullName: 'Test User'
            }
          }
        }
      }
      
      mockedAxios.post.mockResolvedValueOnce(mockResponse)
      
      const credentials = {
        email: 'test@example.com',
        password: 'password123'
      }
      
      const result = await authApi.login(credentials)
      
      expect(mockedAxios.post).toHaveBeenCalledWith('/auth/login', credentials)
      expect(result.data.token).toBe('mock-token')
      expect(result.data.user.email).toBe('test@example.com')
    })
    
    it('should handle login error', async () => {
      const mockError = {
        response: {
          data: {
            success: false,
            message: 'Invalid credentials'
          }
        }
      }
      
      mockedAxios.post.mockRejectedValueOnce(mockError)
      
      const credentials = {
        email: 'test@example.com',
        password: 'wrong-password'
      }
      
      await expect(authApi.login(credentials)).rejects.toThrow()
    })
  })
  
  describe('register', () => {
    it('should register successfully', async () => {
      const mockResponse = {
        data: {
          success: true,
          data: {
            user: {
              id: '1',
              email: 'newuser@example.com',
              fullName: 'New User'
            }
          }
        }
      }
      
      mockedAxios.post.mockResolvedValueOnce(mockResponse)
      
      const userData = {
        email: 'newuser@example.com',
        password: 'password123',
        fullName: 'New User'
      }
      
      const result = await authApi.register(userData)
      
      expect(mockedAxios.post).toHaveBeenCalledWith('/auth/register', userData)
      expect(result.data.user.email).toBe('newuser@example.com')
    })
  })
})
```

### E2E测试配置
```typescript
// playwright.config.ts
import { defineConfig, devices } from '@playwright/test'

export default defineConfig({
  testDir: './e2e',
  
  // 并行运行测试
  fullyParallel: true,
  
  // 失败时重试
  retries: process.env.CI ? 2 : 0,
  
  // 并发worker数量
  workers: process.env.CI ? 1 : undefined,
  
  // 报告配置
  reporter: [
    ['html'],
    ['json', { outputFile: 'test-results/results.json' }]
  ],
  
  use: {
    // 基础URL
    baseURL: 'http://localhost:5173',
    
    // 截图配置
    screenshot: 'only-on-failure',
    
    // 视频录制
    video: 'retain-on-failure',
    
    // 追踪配置
    trace: 'on-first-retry'
  },
  
  // 项目配置
  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] }
    },
    {
      name: 'firefox',
      use: { ...devices['Desktop Firefox'] }
    },
    {
      name: 'webkit',
      use: { ...devices['Desktop Safari'] }
    },
    {
      name: 'Mobile Chrome',
      use: { ...devices['Pixel 5'] }
    },
    {
      name: 'Mobile Safari',
      use: { ...devices['iPhone 12'] }
    }
  ],
  
  // 开发服务器
  webServer: {
    command: 'npm run dev',
    url: 'http://localhost:5173',
    reuseExistingServer: !process.env.CI
  }
})
```

## 性能监控

### Web Vitals集成
```typescript
// src/utils/performance.ts
import { getCLS, getFID, getFCP, getLCP, getTTFB } from 'web-vitals'

interface PerformanceMetric {
  name: string
  value: number
  rating: 'good' | 'needs-improvement' | 'poor'
  timestamp: number
}

class PerformanceMonitor {
  private metrics: PerformanceMetric[] = []
  private observers: PerformanceObserver[] = []
  
  init() {
    // 监控Core Web Vitals
    getCLS(this.handleMetric.bind(this))
    getFID(this.handleMetric.bind(this))
    getFCP(this.handleMetric.bind(this))
    getLCP(this.handleMetric.bind(this))
    getTTFB(this.handleMetric.bind(this))
    
    // 监控自定义指标
    this.observeNavigationTiming()
    this.observeResourceTiming()
    this.observeLongTasks()
  }
  
  private handleMetric(metric: any) {
    const performanceMetric: PerformanceMetric = {
      name: metric.name,
      value: metric.value,
      rating: metric.rating,
      timestamp: Date.now()
    }
    
    this.metrics.push(performanceMetric)
    
    // 发送到监控服务
    this.sendMetric(performanceMetric)
  }
  
  private observeNavigationTiming() {
    const observer = new PerformanceObserver((list) => {
      for (const entry of list.getEntries()) {
        if (entry.entryType === 'navigation') {
          const navEntry = entry as PerformanceNavigationTiming
          
          // DOM加载时间
          this.handleMetric({
            name: 'dom-content-loaded',
            value: navEntry.domContentLoadedEventEnd - navEntry.domContentLoadedEventStart,
            rating: 'good'
          })
          
          // 页面加载完成时间
          this.handleMetric({
            name: 'load-complete',
            value: navEntry.loadEventEnd - navEntry.loadEventStart,
            rating: 'good'
          })
        }
      }
    })
    
    observer.observe({ entryTypes: ['navigation'] })
    this.observers.push(observer)
  }
  
  private observeResourceTiming() {
    const observer = new PerformanceObserver((list) => {
      for (const entry of list.getEntries()) {
        if (entry.entryType === 'resource') {
          const resourceEntry = entry as PerformanceResourceTiming
          
          // 资源加载时间
          if (resourceEntry.duration > 1000) {
            this.handleMetric({
              name: 'slow-resource',
              value: resourceEntry.duration,
              rating: 'poor'
            })
          }
        }
      }
    })
    
    observer.observe({ entryTypes: ['resource'] })
    this.observers.push(observer)
  }
  
  private observeLongTasks() {
    if ('PerformanceObserver' in window) {
      const observer = new PerformanceObserver((list) => {
        for (const entry of list.getEntries()) {
          this.handleMetric({
            name: 'long-task',
            value: entry.duration,
            rating: 'poor'
          })
        }
      })
      
      observer.observe({ entryTypes: ['longtask'] })
      this.observers.push(observer)
    }
  }
  
  private sendMetric(metric: PerformanceMetric) {
    // 发送到分析服务
    if (process.env.NODE_ENV === 'production') {
      // 实际项目中可以发送到Google Analytics、Sentry等
      console.log('Performance metric:', metric)
    }
  }
  
  getMetrics() {
    return this.metrics
  }
  
  destroy() {
    this.observers.forEach(observer => observer.disconnect())
    this.observers = []
    this.metrics = []
  }
}

export const performanceMonitor = new PerformanceMonitor()
```

## 验收标准
- 首屏加载时间 < 2秒
- 代码分割正确实现
- 缓存策略有效
- 单元测试覆盖率 > 80%
- E2E测试覆盖主要用户流程
- 性能监控正常工作
- 内存泄漏检测通过
- 跨浏览器兼容性测试通过

## 技术要求
- 使用Vite构建优化
- 集成Vitest单元测试
- 配置Playwright E2E测试
- 实现Web Vitals监控
- 遵循性能最佳实践
- 建立CI/CD测试流程

## 优先级
高优先级 - 应用稳定性保障

## 预估工时
4个工作日

## 相关文档
- SP1前端开发方案-技术架构文档.md
- 性能优化最佳实践
- 测试策略文档