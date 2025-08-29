<template>
  <header class="the-header">
    <div class="the-header__container">
      <!-- Logo 和标题 -->
      <div class="the-header__brand">
        <router-link to="/" class="the-header__logo">
          <img
            v-if="logoUrl"
            :src="logoUrl"
            :alt="appName"
            class="the-header__logo-image"
          />
          <div v-else class="the-header__logo-text">
            {{ appName }}
          </div>
        </router-link>
      </div>

      <!-- 导航菜单 -->
      <nav v-if="showNavigation" class="the-header__nav">
        <ul class="the-header__nav-list">
          <li
            v-for="item in navigationItems"
            :key="item.path"
            class="the-header__nav-item"
          >
            <router-link
              :to="item.path"
              class="the-header__nav-link"
              :class="{ 'the-header__nav-link--active': $route.path === item.path }"
            >
              <component v-if="item.icon" :is="item.icon" class="the-header__nav-icon" />
              {{ item.label }}
            </router-link>
          </li>
        </ul>
      </nav>

      <!-- 右侧操作区 -->
      <div class="the-header__actions">
        <!-- 搜索框 -->
        <div v-if="showSearch" class="the-header__search">
          <AppInput
            v-model="searchQuery"
            type="search"
            placeholder="搜索课程..."
            size="small"
            :prefix-icon="SearchIcon"
            @keyup.enter="handleSearch"
          />
        </div>

        <!-- 通知 -->
        <button
          v-if="showNotifications"
          type="button"
          class="the-header__notification"
          @click="handleNotificationClick"
        >
          <BellIcon class="w-5 h-5" />
          <span v-if="unreadCount > 0" class="the-header__notification-badge">
            {{ unreadCount > 99 ? '99+' : unreadCount }}
          </span>
        </button>

        <!-- 用户菜单 -->
        <div v-if="isAuthenticated" class="the-header__user" @click="toggleUserMenu">
          <div class="the-header__user-avatar">
            <img
              v-if="user?.avatar"
              :src="user.avatar"
              :alt="user.name"
              class="the-header__avatar-image"
            />
            <div v-else class="the-header__avatar-placeholder">
              {{ user?.name?.charAt(0)?.toUpperCase() || 'U' }}
            </div>
          </div>
          <span class="the-header__user-name">{{ user?.name }}</span>
          <ChevronDownIcon class="w-4 h-4 transition-transform" :class="{ 'rotate-180': showUserMenu }" />
          
          <!-- 用户下拉菜单 -->
          <div v-if="showUserMenu" class="the-header__user-menu">
            <router-link
              v-for="item in userMenuItems"
              :key="item.path"
              :to="item.path"
              class="the-header__user-menu-item"
              @click="showUserMenu = false"
            >
              <component v-if="item.icon" :is="item.icon" class="w-4 h-4" />
              {{ item.label }}
            </router-link>
            <hr class="the-header__user-menu-divider" />
            <button
              type="button"
              class="the-header__user-menu-item the-header__user-menu-item--logout"
              @click="handleLogout"
            >
              <LogOutIcon class="w-4 h-4" />
              退出登录
            </button>
          </div>
        </div>

        <!-- 登录按钮 -->
        <div v-else class="the-header__auth">
          <AppButton
            variant="outline"
            size="small"
            @click="$router.push('/login')"
          >
            登录
          </AppButton>
          <AppButton
            type="primary"
            size="small"
            @click="$router.push('/register')"
          >
            注册
          </AppButton>
        </div>

        <!-- 移动端菜单按钮 -->
        <button
          type="button"
          class="the-header__mobile-menu"
          @click="toggleMobileMenu"
        >
          <MenuIcon v-if="!showMobileMenu" class="w-6 h-6" />
          <XIcon v-else class="w-6 h-6" />
        </button>
      </div>
    </div>

    <!-- 移动端导航菜单 -->
    <div v-if="showMobileMenu" class="the-header__mobile-nav">
      <nav class="the-header__mobile-nav-content">
        <router-link
          v-for="item in navigationItems"
          :key="item.path"
          :to="item.path"
          class="the-header__mobile-nav-item"
          @click="showMobileMenu = false"
        >
          <component v-if="item.icon" :is="item.icon" class="w-5 h-5" />
          {{ item.label }}
        </router-link>
      </nav>
    </div>
  </header>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/store/modules/auth'
import AppButton from '@/components/common/AppButton.vue'
import AppInput from '@/components/common/AppInput.vue'
import {
  SearchIcon,
  BellIcon,
  ChevronDownIcon,
  MenuIcon,
  XIcon,
  LogOutIcon,
  UserIcon,
  SettingsIcon,
  BookOpenIcon,
  HomeIcon
} from 'lucide-vue-next'

export interface TheHeaderProps {
  logoUrl?: string
  appName?: string
  showNavigation?: boolean
  showSearch?: boolean
  showNotifications?: boolean
}

const props = withDefaults(defineProps<TheHeaderProps>(), {
  appName: '万里教育',
  showNavigation: true,
  showSearch: true,
  showNotifications: true
})

const router = useRouter()
const authStore = useAuthStore()

const searchQuery = ref('')
const showUserMenu = ref(false)
const showMobileMenu = ref(false)
const unreadCount = ref(0)

const isAuthenticated = computed(() => authStore.isAuthenticated)
const user = computed(() => authStore.user)

// 导航菜单项
const navigationItems = [
  { path: '/', label: '首页', icon: HomeIcon },
  { path: '/courses', label: '课程管理', icon: BookOpenIcon }
]

// 用户菜单项
const userMenuItems = [
  { path: '/profile', label: '个人资料', icon: UserIcon },
  { path: '/settings', label: '设置', icon: SettingsIcon }
]

const handleSearch = () => {
  if (searchQuery.value.trim()) {
    router.push({
      path: '/courses',
      query: { search: searchQuery.value.trim() }
    })
  }
}

const handleNotificationClick = () => {
  // TODO: 实现通知功能
  console.log('打开通知')
}

const toggleUserMenu = () => {
  showUserMenu.value = !showUserMenu.value
}

const toggleMobileMenu = () => {
  showMobileMenu.value = !showMobileMenu.value
}

const handleLogout = async () => {
  try {
    await authStore.logout()
    showUserMenu.value = false
    router.push('/login')
  } catch (error) {
    console.error('退出登录失败:', error)
  }
}

// 点击外部关闭菜单
const handleClickOutside = (event: Event) => {
  const target = event.target as HTMLElement
  if (!target.closest('.the-header__user')) {
    showUserMenu.value = false
  }
}

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
})
</script>

<style scoped>
.the-header {
  @apply bg-white border-b border-gray-200 sticky top-0 z-40;
}

.the-header__container {
  @apply max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 flex items-center justify-between h-16;
}

.the-header__brand {
  @apply flex items-center;
}

.the-header__logo {
  @apply flex items-center gap-2 text-xl font-bold text-gray-900 hover:text-blue-600 transition-colors;
}

.the-header__logo-image {
  @apply h-8 w-auto;
}

.the-header__logo-text {
  @apply text-xl font-bold;
}

.the-header__nav {
  @apply hidden md:block;
}

.the-header__nav-list {
  @apply flex items-center gap-8;
}

.the-header__nav-item {
  @apply list-none;
}

.the-header__nav-link {
  @apply flex items-center gap-2 px-3 py-2 text-sm font-medium text-gray-700 hover:text-blue-600 transition-colors;
}

.the-header__nav-link--active {
  @apply text-blue-600;
}

.the-header__nav-icon {
  @apply w-4 h-4;
}

.the-header__actions {
  @apply flex items-center gap-4;
}

.the-header__search {
  @apply hidden sm:block w-64;
}

.the-header__notification {
  @apply relative p-2 text-gray-600 hover:text-gray-900 transition-colors;
}

.the-header__notification-badge {
  @apply absolute -top-1 -right-1 bg-red-500 text-white text-xs rounded-full h-5 w-5 flex items-center justify-center;
}

.the-header__user {
  @apply relative flex items-center gap-2 p-2 cursor-pointer hover:bg-gray-50 rounded-lg transition-colors;
}

.the-header__user-avatar {
  @apply w-8 h-8 rounded-full overflow-hidden;
}

.the-header__avatar-image {
  @apply w-full h-full object-cover;
}

.the-header__avatar-placeholder {
  @apply w-full h-full bg-blue-500 text-white flex items-center justify-center text-sm font-medium;
}

.the-header__user-name {
  @apply hidden sm:block text-sm font-medium text-gray-700;
}

.the-header__user-menu {
  @apply absolute top-full right-0 mt-2 w-48 bg-white border border-gray-200 rounded-lg shadow-lg py-1;
}

.the-header__user-menu-item {
  @apply flex items-center gap-2 w-full px-4 py-2 text-sm text-gray-700 hover:bg-gray-50 transition-colors;
}

.the-header__user-menu-item--logout {
  @apply text-red-600 hover:bg-red-50;
}

.the-header__user-menu-divider {
  @apply my-1 border-gray-200;
}

.the-header__auth {
  @apply flex items-center gap-2;
}

.the-header__mobile-menu {
  @apply md:hidden p-2 text-gray-600 hover:text-gray-900 transition-colors;
}

.the-header__mobile-nav {
  @apply md:hidden border-t border-gray-200 bg-white;
}

.the-header__mobile-nav-content {
  @apply px-4 py-2 space-y-1;
}

.the-header__mobile-nav-item {
  @apply flex items-center gap-3 px-3 py-2 text-base font-medium text-gray-700 hover:text-blue-600 hover:bg-gray-50 rounded-lg transition-colors;
}
</style>