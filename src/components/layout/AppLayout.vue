<template>
  <div class="app-layout" :class="layoutClasses">
    <!-- 侧边栏 -->
    <TheSidebar
      v-if="showSidebar"
      v-model:collapsed="sidebarCollapsed"
      :logo-url="logoUrl"
      :app-name="appName"
      :collapsible="sidebarCollapsible"
      class="app-layout__sidebar"
    >
      <template #footer>
        <slot name="sidebar-footer" />
      </template>
    </TheSidebar>

    <!-- 主内容区域 -->
    <div class="app-layout__main">
      <!-- 头部 -->
      <TheHeader
        v-if="showHeader"
        :logo-url="logoUrl"
        :app-name="appName"
        :show-navigation="headerShowNavigation"
        :show-search="headerShowSearch"
        :show-notifications="headerShowNotifications"
        class="app-layout__header"
      />

      <!-- 面包屑导航 -->
      <div v-if="showBreadcrumb" class="app-layout__breadcrumb">
        <nav class="app-layout__breadcrumb-nav">
          <ol class="app-layout__breadcrumb-list">
            <li
              v-for="(item, index) in breadcrumbItems"
              :key="item.path || index"
              class="app-layout__breadcrumb-item"
            >
              <router-link
                v-if="item.path && index < breadcrumbItems.length - 1"
                :to="item.path"
                class="app-layout__breadcrumb-link"
              >
                {{ item.label }}
              </router-link>
              <span v-else class="app-layout__breadcrumb-current">
                {{ item.label }}
              </span>
              <ChevronRightIcon
                v-if="index < breadcrumbItems.length - 1"
                class="app-layout__breadcrumb-separator"
              />
            </li>
          </ol>
        </nav>
      </div>

      <!-- 页面内容 -->
      <main class="app-layout__content">
        <div class="app-layout__content-wrapper">
          <!-- 页面标题区域 -->
          <div v-if="showPageHeader" class="app-layout__page-header">
            <div class="app-layout__page-title-wrapper">
              <h1 class="app-layout__page-title">{{ pageTitle }}</h1>
              <p v-if="pageDescription" class="app-layout__page-description">
                {{ pageDescription }}
              </p>
            </div>
            <div v-if="$slots['page-actions']" class="app-layout__page-actions">
              <slot name="page-actions" />
            </div>
          </div>

          <!-- 主要内容插槽 -->
          <div class="app-layout__page-content">
            <slot />
          </div>
        </div>
      </main>

      <!-- 页脚 -->
      <footer v-if="showFooter" class="app-layout__footer">
        <div class="app-layout__footer-content">
          <slot name="footer">
            <div class="app-layout__footer-default">
              <p class="app-layout__footer-text">
                © {{ currentYear }} {{ appName }}. 保留所有权利。
              </p>
            </div>
          </slot>
        </div>
      </footer>
    </div>

    <!-- 移动端遮罩层 -->
    <div
      v-if="showSidebar && !sidebarCollapsed && isMobile"
      class="app-layout__overlay"
      @click="sidebarCollapsed = true"
    />

    <!-- 全局加载遮罩 -->
    <div v-if="globalLoading" class="app-layout__loading">
      <AppLoading
        type="spinner"
        size="large"
        text="加载中..."
        overlay
        fullscreen
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import TheHeader from './TheHeader.vue'
import TheSidebar from './TheSidebar.vue'
import AppLoading from '@/components/common/AppLoading.vue'
import { ChevronRightIcon } from 'lucide-vue-next'

export interface BreadcrumbItem {
  label: string
  path?: string
}

export interface AppLayoutProps {
  logoUrl?: string
  appName?: string
  showSidebar?: boolean
  showHeader?: boolean
  showBreadcrumb?: boolean
  showPageHeader?: boolean
  showFooter?: boolean
  sidebarCollapsible?: boolean
  headerShowNavigation?: boolean
  headerShowSearch?: boolean
  headerShowNotifications?: boolean
  pageTitle?: string
  pageDescription?: string
  breadcrumbItems?: BreadcrumbItem[]
  globalLoading?: boolean
  layoutMode?: 'default' | 'fluid' | 'boxed'
}

const props = withDefaults(defineProps<AppLayoutProps>(), {
  appName: '万里教育',
  showSidebar: true,
  showHeader: true,
  showBreadcrumb: true,
  showPageHeader: true,
  showFooter: true,
  sidebarCollapsible: true,
  headerShowNavigation: false, // 有侧边栏时头部不显示导航
  headerShowSearch: true,
  headerShowNotifications: true,
  globalLoading: false,
  layoutMode: 'default'
})

const route = useRoute()

const sidebarCollapsed = ref(false)
const isMobile = ref(false)
const currentYear = new Date().getFullYear()

const layoutClasses = computed(() => {
  const classes = [
    `app-layout--${props.layoutMode}`,
    {
      'app-layout--sidebar-collapsed': sidebarCollapsed.value,
      'app-layout--no-sidebar': !props.showSidebar,
      'app-layout--mobile': isMobile.value
    }
  ]
  return classes
})

// 自动生成面包屑
const defaultBreadcrumbItems = computed(() => {
  const items: BreadcrumbItem[] = []
  const pathSegments = route.path.split('/').filter(Boolean)
  
  items.push({ label: '首页', path: '/' })
  
  let currentPath = ''
  pathSegments.forEach((segment, index) => {
    currentPath += `/${segment}`
    
    // 根据路径生成标签
    let label = segment
    switch (segment) {
      case 'dashboard':
        label = '仪表板'
        break
      case 'courses':
        label = '课程管理'
        break
      case 'create':
        label = '创建'
        break
      case 'edit':
        label = '编辑'
        break
      case 'users':
        label = '用户管理'
        break
      case 'settings':
        label = '设置'
        break
      default:
        // 如果是ID，尝试获取更友好的名称
        if (/^\d+$/.test(segment)) {
          label = `详情 #${segment}`
        }
    }
    
    items.push({
      label,
      path: index === pathSegments.length - 1 ? undefined : currentPath
    })
  })
  
  return items
})

const breadcrumbItems = computed(() => {
  return props.breadcrumbItems || defaultBreadcrumbItems.value
})

// 检测移动端
const checkMobile = () => {
  isMobile.value = window.innerWidth < 768
  if (isMobile.value) {
    sidebarCollapsed.value = true
  }
}

const handleResize = () => {
  checkMobile()
}

onMounted(() => {
  checkMobile()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.app-layout {
  @apply min-h-screen flex bg-gray-50;
}

.app-layout--default {
  @apply flex;
}

.app-layout--fluid {
  @apply flex;
}

.app-layout--boxed {
  @apply flex max-w-7xl mx-auto;
}

.app-layout--no-sidebar .app-layout__main {
  @apply w-full;
}

.app-layout--mobile .app-layout__sidebar {
  @apply fixed left-0 top-0 z-30 h-full;
}

.app-layout__sidebar {
  @apply flex-shrink-0;
}

.app-layout__main {
  @apply flex-1 flex flex-col min-w-0;
}

.app-layout__header {
  @apply flex-shrink-0;
}

.app-layout__breadcrumb {
  @apply bg-white border-b border-gray-200 px-4 py-2;
}

.app-layout__breadcrumb-nav {
  @apply max-w-7xl mx-auto;
}

.app-layout__breadcrumb-list {
  @apply flex items-center space-x-2 text-sm;
}

.app-layout__breadcrumb-item {
  @apply flex items-center;
}

.app-layout__breadcrumb-link {
  @apply text-gray-500 hover:text-gray-700 transition-colors;
}

.app-layout__breadcrumb-current {
  @apply text-gray-900 font-medium;
}

.app-layout__breadcrumb-separator {
  @apply w-4 h-4 text-gray-400 mx-2;
}

.app-layout__content {
  @apply flex-1 overflow-auto;
}

.app-layout__content-wrapper {
  @apply max-w-7xl mx-auto px-4 py-6;
}

.app-layout--fluid .app-layout__content-wrapper {
  @apply max-w-none px-6;
}

.app-layout__page-header {
  @apply flex items-start justify-between mb-6;
}

.app-layout__page-title-wrapper {
  @apply flex-1;
}

.app-layout__page-title {
  @apply text-2xl font-bold text-gray-900 mb-1;
}

.app-layout__page-description {
  @apply text-gray-600;
}

.app-layout__page-actions {
  @apply flex items-center gap-3 ml-4;
}

.app-layout__page-content {
  @apply space-y-6;
}

.app-layout__footer {
  @apply bg-white border-t border-gray-200 mt-auto;
}

.app-layout__footer-content {
  @apply max-w-7xl mx-auto px-4 py-4;
}

.app-layout__footer-default {
  @apply text-center;
}

.app-layout__footer-text {
  @apply text-sm text-gray-500;
}

.app-layout__overlay {
  @apply fixed inset-0 bg-black bg-opacity-50 z-20;
}

.app-layout__loading {
  @apply fixed inset-0 z-50;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .app-layout__content-wrapper {
    @apply px-4 py-4;
  }
  
  .app-layout__page-header {
    @apply flex-col items-start gap-4;
  }
  
  .app-layout__page-actions {
    @apply w-full ml-0;
  }
  
  .app-layout__breadcrumb {
    @apply px-4;
  }
}

@media (max-width: 640px) {
  .app-layout__page-title {
    @apply text-xl;
  }
  
  .app-layout__breadcrumb-list {
    @apply text-xs;
  }
}
</style>