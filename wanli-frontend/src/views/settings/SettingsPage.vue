<template>
  <div class="settings-page">
    <!-- 页面头部 -->
    <div class="bg-white shadow-sm border-b">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="py-6">
          <div class="flex justify-between items-center">
            <div>
              <h1 class="text-2xl font-bold text-gray-900">系统设置</h1>
              <p class="mt-1 text-sm text-gray-500">管理系统配置和个人偏好设置</p>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <div class="grid grid-cols-1 lg:grid-cols-4 gap-8">
        <!-- 侧边导航 -->
        <div class="lg:col-span-1">
          <nav class="bg-white rounded-lg shadow">
            <div class="p-4">
              <h3 class="text-sm font-medium text-gray-900 mb-4">设置分类</h3>
              <ul class="space-y-2">
                <li>
                  <button
                    @click="activeTab = 'general'"
                    :class="[
                      'w-full text-left px-3 py-2 rounded-md text-sm font-medium transition-colors',
                      activeTab === 'general'
                        ? 'bg-blue-100 text-blue-700'
                        : 'text-gray-600 hover:text-gray-900 hover:bg-gray-50'
                    ]"
                  >
                    <svg class="w-4 h-4 inline mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z" />
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                    </svg>
                    通用设置
                  </button>
                </li>
                <li>
                  <button
                    @click="activeTab = 'appearance'"
                    :class="[
                      'w-full text-left px-3 py-2 rounded-md text-sm font-medium transition-colors',
                      activeTab === 'appearance'
                        ? 'bg-blue-100 text-blue-700'
                        : 'text-gray-600 hover:text-gray-900 hover:bg-gray-50'
                    ]"
                  >
                    <svg class="w-4 h-4 inline mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 21a4 4 0 01-4-4V5a2 2 0 012-2h4a2 2 0 012 2v12a4 4 0 01-4 4zM21 5a2 2 0 00-2-2h-4a2 2 0 00-2 2v12a4 4 0 004 4h4a2 2 0 002-2V5z" />
                    </svg>
                    外观设置
                  </button>
                </li>
                <li>
                  <button
                    @click="activeTab = 'notifications'"
                    :class="[
                      'w-full text-left px-3 py-2 rounded-md text-sm font-medium transition-colors',
                      activeTab === 'notifications'
                        ? 'bg-blue-100 text-blue-700'
                        : 'text-gray-600 hover:text-gray-900 hover:bg-gray-50'
                    ]"
                  >
                    <svg class="w-4 h-4 inline mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 17h5l-5 5v-5zM4.868 19.718c.64.64 1.536 1.032 2.524 1.032s1.884-.392 2.524-1.032l8.484-8.484c.64-.64 1.032-1.536 1.032-2.524s-.392-1.884-1.032-2.524L9.916 2.702c-.64-.64-1.536-1.032-2.524-1.032s-1.884.392-2.524 1.032L.384 7.186c-.64.64-1.032 1.536-1.032 2.524s.392 1.884 1.032 2.524l4.484 4.484z" />
                    </svg>
                    通知设置
                  </button>
                </li>
                <li>
                  <button
                    @click="activeTab = 'security'"
                    :class="[
                      'w-full text-left px-3 py-2 rounded-md text-sm font-medium transition-colors',
                      activeTab === 'security'
                        ? 'bg-blue-100 text-blue-700'
                        : 'text-gray-600 hover:text-gray-900 hover:bg-gray-50'
                    ]"
                  >
                    <svg class="w-4 h-4 inline mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
                    </svg>
                    安全设置
                  </button>
                </li>
                <li>
                  <button
                    @click="activeTab = 'about'"
                    :class="[
                      'w-full text-left px-3 py-2 rounded-md text-sm font-medium transition-colors',
                      activeTab === 'about'
                        ? 'bg-blue-100 text-blue-700'
                        : 'text-gray-600 hover:text-gray-900 hover:bg-gray-50'
                    ]"
                  >
                    <svg class="w-4 h-4 inline mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                    </svg>
                    关于系统
                  </button>
                </li>
              </ul>
            </div>
          </nav>
        </div>

        <!-- 设置内容 -->
        <div class="lg:col-span-3">
          <!-- 通用设置 -->
          <div v-if="activeTab === 'general'" class="bg-white rounded-lg shadow">
            <div class="px-6 py-4 border-b border-gray-200">
              <h3 class="text-lg font-medium text-gray-900">通用设置</h3>
              <p class="mt-1 text-sm text-gray-500">配置系统的基本设置和偏好</p>
            </div>
            <div class="p-6 space-y-6">
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">
                  默认语言
                </label>
                <select
                  v-model="settings.general.language"
                  @change="saveSettings"
                  class="w-full max-w-xs px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                >
                  <option value="zh-CN">简体中文</option>
                  <option value="zh-TW">繁体中文</option>
                  <option value="en-US">English</option>
                </select>
              </div>

              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">
                  时区设置
                </label>
                <select
                  v-model="settings.general.timezone"
                  @change="saveSettings"
                  class="w-full max-w-xs px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                >
                  <option value="Asia/Shanghai">北京时间 (UTC+8)</option>
                  <option value="Asia/Hong_Kong">香港时间 (UTC+8)</option>
                  <option value="Asia/Taipei">台北时间 (UTC+8)</option>
                  <option value="UTC">协调世界时 (UTC)</option>
                </select>
              </div>

              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">
                  每页显示条数
                </label>
                <select
                  v-model.number="settings.general.pageSize"
                  @change="saveSettings"
                  class="w-full max-w-xs px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                >
                  <option :value="10">10 条/页</option>
                  <option :value="20">20 条/页</option>
                  <option :value="50">50 条/页</option>
                  <option :value="100">100 条/页</option>
                </select>
              </div>

              <div class="flex items-center">
                <input
                  id="autoSave"
                  v-model="settings.general.autoSave"
                  @change="saveSettings"
                  type="checkbox"
                  class="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
                >
                <label for="autoSave" class="ml-2 block text-sm text-gray-900">
                  启用自动保存
                </label>
              </div>
            </div>
          </div>

          <!-- 外观设置 -->
          <div v-if="activeTab === 'appearance'" class="bg-white rounded-lg shadow">
            <div class="px-6 py-4 border-b border-gray-200">
              <h3 class="text-lg font-medium text-gray-900">外观设置</h3>
              <p class="mt-1 text-sm text-gray-500">自定义界面外观和主题</p>
            </div>
            <div class="p-6 space-y-6">
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">
                  主题模式
                </label>
                <div class="grid grid-cols-3 gap-3 max-w-md">
                  <label class="cursor-pointer">
                    <input
                      v-model="settings.appearance.theme"
                      @change="saveSettings"
                      type="radio"
                      value="light"
                      class="sr-only"
                    >
                    <div :class="[
                      'p-3 border-2 rounded-lg text-center transition-colors',
                      settings.appearance.theme === 'light'
                        ? 'border-blue-500 bg-blue-50'
                        : 'border-gray-300 hover:border-gray-400'
                    ]">
                      <svg class="w-6 h-6 mx-auto mb-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 3v1m0 16v1m9-9h-1M4 12H3m15.364 6.364l-.707-.707M6.343 6.343l-.707-.707m12.728 0l-.707.707M6.343 17.657l-.707.707M16 12a4 4 0 11-8 0 4 4 0 018 0z" />
                      </svg>
                      <span class="text-sm font-medium">浅色</span>
                    </div>
                  </label>
                  <label class="cursor-pointer">
                    <input
                      v-model="settings.appearance.theme"
                      @change="saveSettings"
                      type="radio"
                      value="dark"
                      class="sr-only"
                    >
                    <div :class="[
                      'p-3 border-2 rounded-lg text-center transition-colors',
                      settings.appearance.theme === 'dark'
                        ? 'border-blue-500 bg-blue-50'
                        : 'border-gray-300 hover:border-gray-400'
                    ]">
                      <svg class="w-6 h-6 mx-auto mb-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20.354 15.354A9 9 0 018.646 3.646 9.003 9.003 0 0012 21a9.003 9.003 0 008.354-5.646z" />
                      </svg>
                      <span class="text-sm font-medium">深色</span>
                    </div>
                  </label>
                  <label class="cursor-pointer">
                    <input
                      v-model="settings.appearance.theme"
                      @change="saveSettings"
                      type="radio"
                      value="auto"
                      class="sr-only"
                    >
                    <div :class="[
                      'p-3 border-2 rounded-lg text-center transition-colors',
                      settings.appearance.theme === 'auto'
                        ? 'border-blue-500 bg-blue-50'
                        : 'border-gray-300 hover:border-gray-400'
                    ]">
                      <svg class="w-6 h-6 mx-auto mb-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9.75 17L9 20l-1 1h8l-1-1-.75-3M3 13h18M5 17h14a2 2 0 002-2V5a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" />
                      </svg>
                      <span class="text-sm font-medium">自动</span>
                    </div>
                  </label>
                </div>
              </div>

              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">
                  字体大小
                </label>
                <select
                  v-model="settings.appearance.fontSize"
                  @change="saveSettings"
                  class="w-full max-w-xs px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                >
                  <option value="small">小号</option>
                  <option value="medium">中号</option>
                  <option value="large">大号</option>
                </select>
              </div>

              <div class="flex items-center">
                <input
                  id="compactMode"
                  v-model="settings.appearance.compactMode"
                  @change="saveSettings"
                  type="checkbox"
                  class="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
                >
                <label for="compactMode" class="ml-2 block text-sm text-gray-900">
                  紧凑模式
                </label>
              </div>
            </div>
          </div>

          <!-- 通知设置 -->
          <div v-if="activeTab === 'notifications'" class="bg-white rounded-lg shadow">
            <div class="px-6 py-4 border-b border-gray-200">
              <h3 class="text-lg font-medium text-gray-900">通知设置</h3>
              <p class="mt-1 text-sm text-gray-500">管理系统通知和提醒设置</p>
            </div>
            <div class="p-6 space-y-6">
              <div class="space-y-4">
                <div class="flex items-center justify-between">
                  <div>
                    <h4 class="text-sm font-medium text-gray-900">邮件通知</h4>
                    <p class="text-sm text-gray-500">接收重要系统通知邮件</p>
                  </div>
                  <input
                    v-model="settings.notifications.email"
                    @change="saveSettings"
                    type="checkbox"
                    class="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
                  >
                </div>

                <div class="flex items-center justify-between">
                  <div>
                    <h4 class="text-sm font-medium text-gray-900">浏览器通知</h4>
                    <p class="text-sm text-gray-500">在浏览器中显示桌面通知</p>
                  </div>
                  <input
                    v-model="settings.notifications.browser"
                    @change="saveSettings"
                    type="checkbox"
                    class="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
                  >
                </div>

                <div class="flex items-center justify-between">
                  <div>
                    <h4 class="text-sm font-medium text-gray-900">作业提醒</h4>
                    <p class="text-sm text-gray-500">作业截止日期前的提醒通知</p>
                  </div>
                  <input
                    v-model="settings.notifications.assignments"
                    @change="saveSettings"
                    type="checkbox"
                    class="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
                  >
                </div>

                <div class="flex items-center justify-between">
                  <div>
                    <h4 class="text-sm font-medium text-gray-900">课程更新</h4>
                    <p class="text-sm text-gray-500">课程内容更新时的通知</p>
                  </div>
                  <input
                    v-model="settings.notifications.courses"
                    @change="saveSettings"
                    type="checkbox"
                    class="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
                  >
                </div>
              </div>
            </div>
          </div>

          <!-- 安全设置 -->
          <div v-if="activeTab === 'security'" class="bg-white rounded-lg shadow">
            <div class="px-6 py-4 border-b border-gray-200">
              <h3 class="text-lg font-medium text-gray-900">安全设置</h3>
              <p class="mt-1 text-sm text-gray-500">管理账户安全和隐私设置</p>
            </div>
            <div class="p-6 space-y-6">
              <div>
                <h4 class="text-sm font-medium text-gray-900 mb-4">密码设置</h4>
                <AppButton
                  variant="outline"
                  @click="showChangePasswordModal = true"
                >
                  修改密码
                </AppButton>
              </div>

              <div>
                <h4 class="text-sm font-medium text-gray-900 mb-4">登录安全</h4>
                <div class="space-y-3">
                  <div class="flex items-center justify-between">
                    <div>
                      <span class="text-sm text-gray-900">双因素认证</span>
                      <p class="text-sm text-gray-500">为账户添加额外的安全保护</p>
                    </div>
                    <AppButton
                      variant="outline"
                      size="sm"
                      @click="toggle2FA"
                    >
                      {{ settings.security.twoFactorEnabled ? '禁用' : '启用' }}
                    </AppButton>
                  </div>
                </div>
              </div>

              <div>
                <h4 class="text-sm font-medium text-gray-900 mb-4">会话管理</h4>
                <div class="space-y-3">
                  <div class="flex items-center justify-between">
                    <div>
                      <span class="text-sm text-gray-900">自动登出时间</span>
                      <p class="text-sm text-gray-500">无操作后自动登出的时间</p>
                    </div>
                    <select
                      v-model.number="settings.security.sessionTimeout"
                      @change="saveSettings"
                      class="px-3 py-1 border border-gray-300 rounded-md text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                    >
                      <option :value="30">30分钟</option>
                      <option :value="60">1小时</option>
                      <option :value="120">2小时</option>
                      <option :value="480">8小时</option>
                      <option :value="0">永不</option>
                    </select>
                  </div>
                  <AppButton
                    variant="outline"
                    @click="logoutAllSessions"
                  >
                    登出所有设备
                  </AppButton>
                </div>
              </div>
            </div>
          </div>

          <!-- 关于系统 -->
          <div v-if="activeTab === 'about'" class="bg-white rounded-lg shadow">
            <div class="px-6 py-4 border-b border-gray-200">
              <h3 class="text-lg font-medium text-gray-900">关于系统</h3>
              <p class="mt-1 text-sm text-gray-500">系统信息和版本详情</p>
            </div>
            <div class="p-6 space-y-6">
              <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div>
                  <h4 class="text-sm font-medium text-gray-900 mb-3">系统信息</h4>
                  <dl class="space-y-2">
                    <div class="flex justify-between">
                      <dt class="text-sm text-gray-500">系统名称</dt>
                      <dd class="text-sm text-gray-900">万里学习平台</dd>
                    </div>
                    <div class="flex justify-between">
                      <dt class="text-sm text-gray-500">版本号</dt>
                      <dd class="text-sm text-gray-900">v1.0.0</dd>
                    </div>
                    <div class="flex justify-between">
                      <dt class="text-sm text-gray-500">构建时间</dt>
                      <dd class="text-sm text-gray-900">2024-01-15</dd>
                    </div>
                    <div class="flex justify-between">
                      <dt class="text-sm text-gray-500">技术栈</dt>
                      <dd class="text-sm text-gray-900">Vue 3 + TypeScript</dd>
                    </div>
                  </dl>
                </div>

                <div>
                  <h4 class="text-sm font-medium text-gray-900 mb-3">支持信息</h4>
                  <dl class="space-y-2">
                    <div class="flex justify-between">
                      <dt class="text-sm text-gray-500">开发团队</dt>
                      <dd class="text-sm text-gray-900">万里书院技术团队</dd>
                    </div>
                    <div class="flex justify-between">
                      <dt class="text-sm text-gray-500">联系邮箱</dt>
                      <dd class="text-sm text-gray-900">support@wanli.edu</dd>
                    </div>
                    <div class="flex justify-between">
                      <dt class="text-sm text-gray-500">官方网站</dt>
                      <dd class="text-sm text-gray-900">
                        <a href="#" class="text-blue-600 hover:text-blue-500">www.wanli.edu</a>
                      </dd>
                    </div>
                  </dl>
                </div>
              </div>

              <div class="pt-6 border-t border-gray-200">
                <div class="flex space-x-4">
                  <AppButton variant="outline">
                    检查更新
                  </AppButton>
                  <AppButton variant="outline">
                    用户手册
                  </AppButton>
                  <AppButton variant="outline">
                    意见反馈
                  </AppButton>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 修改密码模态框 -->
    <div v-if="showChangePasswordModal" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div class="bg-white rounded-lg max-w-md w-full mx-4">
        <div class="px-6 py-4 border-b border-gray-200">
          <h3 class="text-lg font-medium text-gray-900">修改密码</h3>
        </div>
        
        <form @submit.prevent="changePassword" class="p-6 space-y-4">
          <div>
            <label for="currentPassword" class="block text-sm font-medium text-gray-700 mb-2">
              当前密码
            </label>
            <input
              id="currentPassword"
              v-model="passwordForm.currentPassword"
              type="password"
              required
              class="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
            >
          </div>

          <div>
            <label for="newPassword" class="block text-sm font-medium text-gray-700 mb-2">
              新密码
            </label>
            <input
              id="newPassword"
              v-model="passwordForm.newPassword"
              type="password"
              required
              minlength="8"
              class="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
            >
          </div>

          <div>
            <label for="confirmPassword" class="block text-sm font-medium text-gray-700 mb-2">
              确认新密码
            </label>
            <input
              id="confirmPassword"
              v-model="passwordForm.confirmPassword"
              type="password"
              required
              class="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
            >
          </div>

          <div class="flex justify-end space-x-4 pt-4">
            <AppButton
              type="button"
              variant="outline"
              @click="cancelChangePassword"
            >
              取消
            </AppButton>
            <AppButton
              type="submit"
              :loading="isChangingPassword"
            >
              确认修改
            </AppButton>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useNotification } from '../../composables/useNotification'
import AppButton from '../../components/common/AppButton.vue'

interface Settings {
  general: {
    language: string
    timezone: string
    pageSize: number
    autoSave: boolean
  }
  appearance: {
    theme: 'light' | 'dark' | 'auto'
    fontSize: 'small' | 'medium' | 'large'
    compactMode: boolean
  }
  notifications: {
    email: boolean
    browser: boolean
    assignments: boolean
    courses: boolean
  }
  security: {
    twoFactorEnabled: boolean
    sessionTimeout: number
  }
}

interface PasswordForm {
  currentPassword: string
  newPassword: string
  confirmPassword: string
}

const { success: showSuccess, error: showError } = useNotification()

// 响应式数据
const activeTab = ref('general')
const showChangePasswordModal = ref(false)
const isChangingPassword = ref(false)

const settings = reactive<Settings>({
  general: {
    language: 'zh-CN',
    timezone: 'Asia/Shanghai',
    pageSize: 20,
    autoSave: true
  },
  appearance: {
    theme: 'light',
    fontSize: 'medium',
    compactMode: false
  },
  notifications: {
    email: true,
    browser: true,
    assignments: true,
    courses: true
  },
  security: {
    twoFactorEnabled: false,
    sessionTimeout: 60
  }
})

const passwordForm = reactive<PasswordForm>({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 方法
const loadSettings = async () => {
  try {
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 500))
    
    // 从localStorage加载设置
    const savedSettings = localStorage.getItem('userSettings')
    if (savedSettings) {
      Object.assign(settings, JSON.parse(savedSettings))
    }
  } catch (error) {
    console.error('加载设置失败:', error)
  }
}

const saveSettings = async () => {
  try {
    // 保存到localStorage
    localStorage.setItem('userSettings', JSON.stringify(settings))
    
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 300))
    
    showSuccess('设置已保存')
  } catch (error) {
    console.error('保存设置失败:', error)
    showError('保存设置失败，请稍后重试')
  }
}

const toggle2FA = async () => {
  try {
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    settings.security.twoFactorEnabled = !settings.security.twoFactorEnabled
    await saveSettings()
    
    showSuccess(
      settings.security.twoFactorEnabled 
        ? '双因素认证已启用' 
        : '双因素认证已禁用'
    )
  } catch (error) {
    console.error('切换双因素认证失败:', error)
    showError('操作失败，请稍后重试')
  }
}

const logoutAllSessions = async () => {
  if (!confirm('确定要登出所有设备吗？这将强制所有设备重新登录。')) {
    return
  }
  
  try {
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    showSuccess('已成功登出所有设备')
  } catch (error) {
    console.error('登出所有设备失败:', error)
    showError('操作失败，请稍后重试')
  }
}

const changePassword = async () => {
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    showError('新密码和确认密码不匹配')
    return
  }
  
  if (passwordForm.newPassword.length < 8) {
    showError('新密码长度至少为8位')
    return
  }
  
  isChangingPassword.value = true
  
  try {
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 1500))
    
    showSuccess('密码修改成功')
    cancelChangePassword()
  } catch (error) {
    console.error('修改密码失败:', error)
    showError('修改密码失败，请稍后重试')
  } finally {
    isChangingPassword.value = false
  }
}

const cancelChangePassword = () => {
  showChangePasswordModal.value = false
  passwordForm.currentPassword = ''
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
}

// 生命周期
onMounted(() => {
  loadSettings()
})
</script>

<style scoped>
.settings-page {
  min-height: 100vh;
  background-color: #f9fafb;
}
</style>