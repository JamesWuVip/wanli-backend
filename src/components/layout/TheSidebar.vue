<template>
  <aside :class="sidebarClasses">
    <!-- 侧边栏头部 -->
    <div class="the-sidebar__header">
      <div class="the-sidebar__logo">
        <img
          v-if="logoUrl && !collapsed"
          :src="logoUrl"
          :alt="appName"
          class="the-sidebar__logo-image"
        />
        <div v-else-if="!collapsed" class="the-sidebar__logo-text">
          {{ appName }}
        </div>
        <div v-else class="the-sidebar__logo-collapsed">
          {{ appName.charAt(0) }}
        </div>
      </div>
      
      <button
        v-if="collapsible"
        type="button"
        class="the-sidebar__toggle"
        @click="toggleCollapse"
      >
        <ChevronLeftIcon v-if="!collapsed" class="w-4 h-4" />
        <ChevronRightIcon v-else class="w-4 h-4" />
      </button>
    </div>

    <!-- 导航菜单 -->
    <nav class="the-sidebar__nav">
      <ul class="the-sidebar__menu">
        <li
          v-for="item in menuItems"
          :key="item.key"
          class="the-sidebar__menu-item"
        >
          <!-- 有子菜单的项 -->
          <div v-if="item.children && item.children.length > 0">
            <button
              type="button"
              class="the-sidebar__menu-button"
              :class="{
                'the-sidebar__menu-button--active': isMenuActive(item),
                'the-sidebar__menu-button--expanded': expandedMenus.includes(item.key)
              }"
              @click="toggleSubmenu(item.key)"
            >
              <component v-if="item.icon" :is="item.icon" class="the-sidebar__menu-icon" />
              <span v-if="!collapsed" class="the-sidebar__menu-text">{{ item.label }}</span>
              <ChevronDownIcon
                v-if="!collapsed"
                class="the-sidebar__menu-arrow"
                :class="{ 'rotate-180': expandedMenus.includes(item.key) }"
              />
            </button>
            
            <!-- 子菜单 -->
            <Transition name="submenu">
              <ul
                v-if="!collapsed && expandedMenus.includes(item.key)"
                class="the-sidebar__submenu"
              >
                <li
                  v-for="child in item.children"
                  :key="child.key"
                  class="the-sidebar__submenu-item"
                >
                  <router-link
                    :to="child.path"
                    class="the-sidebar__submenu-link"
                    :class="{ 'the-sidebar__submenu-link--active': $route.path === child.path }"
                  >
                    <component v-if="child.icon" :is="child.icon" class="the-sidebar__submenu-icon" />
                    {{ child.label }}
                  </router-link>
                </li>
              </ul>
            </Transition>
          </div>
          
          <!-- 普通菜单项 -->
          <router-link
            v-else
            :to="item.path"
            class="the-sidebar__menu-link"
            :class="{ 'the-sidebar__menu-link--active': $route.path === item.path }"
          >
            <component v-if="item.icon" :is="item.icon" class="the-sidebar__menu-icon" />
            <span v-if="!collapsed" class="the-sidebar__menu-text">{{ item.label }}</span>
          </router-link>
        </li>
      </ul>
    </nav>

    <!-- 侧边栏底部 -->
    <div v-if="$slots.footer" class="the-sidebar__footer">
      <slot name="footer" />
    </div>
  </aside>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useRoute } from 'vue-router'
import {
  ChevronLeftIcon,
  ChevronRightIcon,
  ChevronDownIcon,
  HomeIcon,
  BookOpenIcon,
  UsersIcon,
  SettingsIcon,
  BarChart3Icon,
  FileTextIcon,
  StoreIcon,
  PlusIcon
} from 'lucide-vue-next'

export interface MenuItem {
  key: string
  label: string
  path?: string
  icon?: any
  children?: MenuItem[]
}

export interface TheSidebarProps {
  logoUrl?: string
  appName?: string
  collapsed?: boolean
  collapsible?: boolean
  width?: string
  collapsedWidth?: string
}

export interface TheSidebarEmits {
  'update:collapsed': [collapsed: boolean]
}

const props = withDefaults(defineProps<TheSidebarProps>(), {
  appName: '万里教育',
  collapsed: false,
  collapsible: true,
  width: '256px',
  collapsedWidth: '64px'
})

const emit = defineEmits<TheSidebarEmits>()
const route = useRoute()

const expandedMenus = ref<string[]>([])

// 菜单配置
const menuItems: MenuItem[] = [
  {
    key: 'dashboard',
    label: '仪表盘',
    path: '/admin/dashboard',
    icon: HomeIcon
  },
  {
    key: 'courses',
    label: '课程管理',
    icon: BookOpenIcon,
    children: [
      {
        key: 'course-list',
        label: '课程列表',
        path: '/admin/courses',
        icon: FileTextIcon
      },
      {
        key: 'course-create',
        label: '创建课程',
        path: '/admin/courses/create',
        icon: PlusIcon
      }
    ]
  },
  {
    key: 'users',
    label: '用户管理',
    path: '/admin/users',
    icon: UsersIcon
  },
  {
    key: 'stores',
    label: '门店管理',
    path: '/admin/stores',
    icon: StoreIcon
  },
  {
    key: 'analytics',
    label: '数据分析',
    path: '/admin/analytics',
    icon: BarChart3Icon
  },
  {
    key: 'settings',
    label: '系统设置',
    path: '/admin/settings',
    icon: SettingsIcon
  }
]

const sidebarClasses = computed(() => {
  const classes = ['the-sidebar']
  
  if (props.collapsed) {
    classes.push('the-sidebar--collapsed')
  }
  
  return classes
})

const sidebarStyle = computed(() => {
  return {
    width: props.collapsed ? props.collapsedWidth : props.width
  }
})

const toggleCollapse = () => {
  emit('update:collapsed', !props.collapsed)
}

const toggleSubmenu = (key: string) => {
  if (props.collapsed) return
  
  const index = expandedMenus.value.indexOf(key)
  if (index > -1) {
    expandedMenus.value.splice(index, 1)
  } else {
    expandedMenus.value.push(key)
  }
}

const isMenuActive = (item: MenuItem): boolean => {
  if (item.path && route.path === item.path) {
    return true
  }
  
  if (item.children) {
    return item.children.some(child => child.path === route.path)
  }
  
  return false
}

// 监听路由变化，自动展开对应的菜单
watch(
  () => route.path,
  (newPath) => {
    menuItems.forEach(item => {
      if (item.children) {
        const hasActiveChild = item.children.some(child => child.path === newPath)
        if (hasActiveChild && !expandedMenus.value.includes(item.key)) {
          expandedMenus.value.push(item.key)
        }
      }
    })
  },
  { immediate: true }
)

// 监听折叠状态变化，折叠时关闭所有子菜单
watch(
  () => props.collapsed,
  (collapsed) => {
    if (collapsed) {
      expandedMenus.value = []
    }
  }
)
</script>

<style scoped>
.the-sidebar {
  @apply bg-white border-r border-gray-200 h-full flex flex-col transition-all duration-300;
  width: v-bind('sidebarStyle.width');
}

.the-sidebar--collapsed {
  @apply overflow-hidden;
}

.the-sidebar__header {
  @apply flex items-center justify-between p-4 border-b border-gray-200;
}

.the-sidebar__logo {
  @apply flex items-center gap-2;
}

.the-sidebar__logo-image {
  @apply h-8 w-auto;
}

.the-sidebar__logo-text {
  @apply text-xl font-bold text-gray-900;
}

.the-sidebar__logo-collapsed {
  @apply w-8 h-8 bg-blue-600 text-white rounded-lg flex items-center justify-center font-bold;
}

.the-sidebar__toggle {
  @apply p-1 text-gray-500 hover:text-gray-700 hover:bg-gray-100 rounded transition-colors;
}

.the-sidebar__nav {
  @apply flex-1 overflow-y-auto py-4;
}

.the-sidebar__menu {
  @apply space-y-1 px-2;
}

.the-sidebar__menu-item {
  @apply list-none;
}

.the-sidebar__menu-link,
.the-sidebar__menu-button {
  @apply flex items-center gap-3 w-full px-3 py-2 text-sm font-medium text-gray-700 hover:text-blue-600 hover:bg-blue-50 rounded-lg transition-colors;
}

.the-sidebar__menu-link--active,
.the-sidebar__menu-button--active {
  @apply text-blue-600 bg-blue-50;
}

.the-sidebar__menu-icon {
  @apply w-5 h-5 flex-shrink-0;
}

.the-sidebar__menu-text {
  @apply flex-1 text-left;
}

.the-sidebar__menu-arrow {
  @apply w-4 h-4 transition-transform;
}

.the-sidebar__submenu {
  @apply mt-1 space-y-1;
}

.the-sidebar__submenu-item {
  @apply list-none;
}

.the-sidebar__submenu-link {
  @apply flex items-center gap-3 w-full px-3 py-2 ml-6 text-sm text-gray-600 hover:text-blue-600 hover:bg-blue-50 rounded-lg transition-colors;
}

.the-sidebar__submenu-link--active {
  @apply text-blue-600 bg-blue-50;
}

.the-sidebar__submenu-icon {
  @apply w-4 h-4;
}

.the-sidebar__footer {
  @apply p-4 border-t border-gray-200;
}

/* 子菜单动画 */
.submenu-enter-active,
.submenu-leave-active {
  transition: all 0.3s ease;
  overflow: hidden;
}

.submenu-enter-from,
.submenu-leave-to {
  max-height: 0;
  opacity: 0;
}

.submenu-enter-to,
.submenu-leave-from {
  max-height: 200px;
  opacity: 1;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .the-sidebar {
    @apply fixed left-0 top-0 z-30;
  }
}
</style>