<template>
  <div class="min-h-screen flex items-center justify-center bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
    <div class="max-w-md w-full space-y-8">
      <!-- 头部 -->
      <div>
        <h2 class="mt-6 text-center text-3xl font-extrabold text-gray-900">
          创建新账户
        </h2>
        <p class="mt-2 text-center text-sm text-gray-600">
          或者
          <router-link
            to="/login"
            class="font-medium text-indigo-600 hover:text-indigo-500"
          >
            登录已有账户
          </router-link>
        </p>
      </div>

      <!-- 注册表单 -->
      <AppCard>
        <template #content>
          <form class="space-y-6" @submit.prevent="handleRegister">
            <!-- 用户名 -->
            <div>
              <label for="username" class="sr-only">用户名</label>
              <AppInput
                id="username"
                v-model="formData.username"
                type="text"
                placeholder="用户名"
                :error-message="formErrors.username"
                @blur="validateUsername"
              />
            </div>

            <!-- 邮箱 -->
            <div>
              <label for="email" class="sr-only">邮箱地址</label>
              <AppInput
                id="email"
                v-model="formData.email"
                type="email"
                placeholder="邮箱地址"
                :error-message="formErrors.email"
                @blur="validateEmail"
              />
            </div>

            <!-- 密码 -->
            <div>
              <label for="password" class="sr-only">密码</label>
              <div class="relative">
                <AppInput
                  id="password"
                  v-model="formData.password"
                  :type="showPassword ? 'text' : 'password'"
                  placeholder="密码"
                  :error-message="formErrors.password"
                  @blur="validatePassword"
                />
                <button
                  type="button"
                  class="absolute inset-y-0 right-0 pr-3 flex items-center"
                  @click="showPassword = !showPassword"
                >
                  <svg
                    v-if="showPassword"
                    class="h-5 w-5 text-gray-400"
                    fill="none"
                    stroke="currentColor"
                    viewBox="0 0 24 24"
                  >
                    <path
                      stroke-linecap="round"
                      stroke-linejoin="round"
                      stroke-width="2"
                      d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.878 9.878L8.464 8.464m1.414 1.414L8.464 8.464m5.656 5.656l1.415 1.415m-1.415-1.415l1.415 1.415M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
                    />
                  </svg>
                  <svg
                    v-else
                    class="h-5 w-5 text-gray-400"
                    fill="none"
                    stroke="currentColor"
                    viewBox="0 0 24 24"
                  >
                    <path
                      stroke-linecap="round"
                      stroke-linejoin="round"
                      stroke-width="2"
                      d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"
                    />
                    <path
                      stroke-linecap="round"
                      stroke-linejoin="round"
                      stroke-width="2"
                      d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"
                    />
                  </svg>
                </button>
              </div>
              
              <!-- 密码强度指示器 -->
              <div v-if="formData.password" class="mt-2">
                <div class="flex items-center space-x-2">
                  <div class="flex-1 bg-gray-200 rounded-full h-2">
                    <div
                      class="h-2 rounded-full transition-all duration-300"
                      :class="passwordStrengthColor"
                      :style="{ width: passwordStrengthWidth }"
                    ></div>
                  </div>
                  <span class="text-xs" :class="passwordStrengthColor.replace('bg-', 'text-')">
                    {{ passwordStrengthText }}
                  </span>
                </div>
              </div>
            </div>

            <!-- 确认密码 -->
            <div>
              <label for="confirmPassword" class="sr-only">确认密码</label>
              <AppInput
                id="confirmPassword"
                v-model="formData.confirmPassword"
                :type="showConfirmPassword ? 'text' : 'password'"
                placeholder="确认密码"
                :error-message="formErrors.confirmPassword"
                @blur="validateConfirmPassword"
              />
              <button
                type="button"
                class="absolute inset-y-0 right-0 pr-3 flex items-center"
                @click="showConfirmPassword = !showConfirmPassword"
              >
                <svg
                  v-if="showConfirmPassword"
                  class="h-5 w-5 text-gray-400"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="2"
                    d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.878 9.878L8.464 8.464m1.414 1.414L8.464 8.464m5.656 5.656l1.415 1.415m-1.415-1.415l1.415 1.415M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
                  />
                </svg>
                <svg
                  v-else
                  class="h-5 w-5 text-gray-400"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="2"
                    d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"
                  />
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="2"
                    d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"
                  />
                </svg>
              </button>
            </div>

            <!-- 用户协议 -->
            <div class="flex items-center">
              <input
                id="agreeToTerms"
                v-model="formData.agreeToTerms"
                type="checkbox"
                class="h-4 w-4 text-indigo-600 focus:ring-indigo-500 border-gray-300 rounded"
                @change="validateAgreeToTerms"
              />
              <label for="agreeToTerms" class="ml-2 block text-sm text-gray-900">
                我同意
                <a href="#" class="text-indigo-600 hover:text-indigo-500">用户协议</a>
                和
                <a href="#" class="text-indigo-600 hover:text-indigo-500">隐私政策</a>
              </label>
            </div>
            <div v-if="formErrors.agreeToTerms" class="text-red-600 text-sm">
              {{ formErrors.agreeToTerms }}
            </div>

            <!-- 注册按钮 -->
            <div>
              <AppButton
                type="submit"
                class="w-full"
                :loading="isLoading"
                :disabled="!isFormValid"
              >
                创建账户
              </AppButton>
            </div>

            <!-- 分割线 -->
            <div class="mt-6">
              <div class="relative">
                <div class="absolute inset-0 flex items-center">
                  <div class="w-full border-t border-gray-300" />
                </div>
                <div class="relative flex justify-center text-sm">
                  <span class="px-2 bg-white text-gray-500">或者使用</span>
                </div>
              </div>
            </div>

            <!-- 第三方注册 -->
            <div class="mt-6 grid grid-cols-2 gap-3">
              <AppButton
                type="button"
                variant="outline"
                class="w-full"
                @click="handleGithubRegister"
              >
                <svg class="w-5 h-5 mr-2" fill="currentColor" viewBox="0 0 20 20">
                  <path
                    fill-rule="evenodd"
                    d="M10 0C4.477 0 0 4.484 0 10.017c0 4.425 2.865 8.18 6.839 9.504.5.092.682-.217.682-.483 0-.237-.008-.868-.013-1.703-2.782.605-3.369-1.343-3.369-1.343-.454-1.158-1.11-1.466-1.11-1.466-.908-.62.069-.608.069-.608 1.003.07 1.531 1.032 1.531 1.032.892 1.53 2.341 1.088 2.91.832.092-.647.35-1.088.636-1.338-2.22-.253-4.555-1.113-4.555-4.951 0-1.093.39-1.988 1.029-2.688-.103-.253-.446-1.272.098-2.65 0 0 .84-.27 2.75 1.026A9.564 9.564 0 0110 4.844c.85.004 1.705.115 2.504.337 1.909-1.296 2.747-1.027 2.747-1.027.546 1.379.203 2.398.1 2.651.64.7 1.028 1.595 1.028 2.688 0 3.848-2.339 4.695-4.566 4.942.359.31.678.921.678 1.856 0 1.338-.012 2.419-.012 2.747 0 .268.18.58.688.482A10.019 10.019 0 0020 10.017C20 4.484 15.522 0 10 0z"
                    clip-rule="evenodd"
                  />
                </svg>
                GitHub
              </AppButton>

              <AppButton
                type="button"
                variant="outline"
                class="w-full"
                @click="handleGoogleRegister"
              >
                <svg class="w-5 h-5 mr-2" viewBox="0 0 24 24">
                  <path
                    fill="#4285F4"
                    d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"
                  />
                  <path
                    fill="#34A853"
                    d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"
                  />
                  <path
                    fill="#FBBC05"
                    d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"
                  />
                  <path
                    fill="#EA4335"
                    d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"
                  />
                </svg>
                Google
              </AppButton>
            </div>
          </form>
        </template>
      </AppCard>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useNotification } from '@/composables/useNotification'
import { AppButton, AppCard, AppInput } from '@/components/ui/atoms'

interface FormData {
  username: string
  email: string
  password: string
  confirmPassword: string
  agreeToTerms: boolean
}

interface FormErrors {
  username: string | undefined
  email: string | undefined
  password: string | undefined
  confirmPassword: string | undefined
  agreeToTerms: string | undefined
}

const router = useRouter()
const authStore = useAuthStore()
const { success: showSuccess, error: showError } = useNotification()

// 表单数据
const formData = ref<FormData>({
  username: '',
  email: '',
  password: '',
  confirmPassword: '',
  agreeToTerms: false
})

// 表单错误
const formErrors = ref<FormErrors>({
  username: undefined,
  email: undefined,
  password: undefined,
  confirmPassword: undefined,
  agreeToTerms: undefined
})

// 状态
const isLoading = ref(false)
const showPassword = ref(false)
const showConfirmPassword = ref(false)

// 密码强度计算
const passwordStrength = computed(() => {
  const password = formData.value.password
  if (!password) return 0
  
  let strength = 0
  if (password.length >= 8) strength++
  if (/[a-z]/.test(password)) strength++
  if (/[A-Z]/.test(password)) strength++
  if (/[0-9]/.test(password)) strength++
  if (/[^A-Za-z0-9]/.test(password)) strength++
  
  return strength
})

const passwordStrengthText = computed(() => {
  const strength = passwordStrength.value
  if (strength === 0) return ''
  if (strength <= 2) return '弱'
  if (strength <= 3) return '中'
  if (strength <= 4) return '强'
  return '很强'
})

const passwordStrengthColor = computed(() => {
  const strength = passwordStrength.value
  if (strength <= 2) return 'bg-red-500'
  if (strength <= 3) return 'bg-yellow-500'
  if (strength <= 4) return 'bg-blue-500'
  return 'bg-green-500'
})

const passwordStrengthWidth = computed(() => {
  return `${(passwordStrength.value / 5) * 100}%`
})

// 表单验证
const validateUsername = () => {
  const username = formData.value.username.trim()
  if (!username) {
    formErrors.value.username = '请输入用户名'
    return false
  }
  if (username.length < 3) {
    formErrors.value.username = '用户名至少需要3个字符'
    return false
  }
  if (username.length > 20) {
    formErrors.value.username = '用户名不能超过20个字符'
    return false
  }
  if (!/^[a-zA-Z0-9_\u4e00-\u9fa5]+$/.test(username)) {
    formErrors.value.username = '用户名只能包含字母、数字、下划线和中文'
    return false
  }
  formErrors.value.username = undefined
  return true
}

const validateEmail = () => {
  const email = formData.value.email.trim()
  if (!email) {
    formErrors.value.email = '请输入邮箱地址'
    return false
  }
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!emailRegex.test(email)) {
    formErrors.value.email = '请输入有效的邮箱地址'
    return false
  }
  formErrors.value.email = undefined
  return true
}

const validatePassword = () => {
  const password = formData.value.password
  if (!password) {
    formErrors.value.password = '请输入密码'
    return false
  }
  if (password.length < 8) {
    formErrors.value.password = '密码至少需要8个字符'
    return false
  }
  if (password.length > 128) {
    formErrors.value.password = '密码不能超过128个字符'
    return false
  }
  formErrors.value.password = undefined
  return true
}

const validateConfirmPassword = () => {
  const confirmPassword = formData.value.confirmPassword
  if (!confirmPassword) {
    formErrors.value.confirmPassword = '请确认密码'
    return false
  }
  if (confirmPassword !== formData.value.password) {
    formErrors.value.confirmPassword = '两次输入的密码不一致'
    return false
  }
  formErrors.value.confirmPassword = undefined
  return true
}

const validateAgreeToTerms = () => {
  if (!formData.value.agreeToTerms) {
    formErrors.value.agreeToTerms = '请同意用户协议和隐私政策'
    return false
  }
  formErrors.value.agreeToTerms = undefined
  return true
}

// 表单整体验证
const isFormValid = computed(() => {
  return (
    validateUsername() &&
    validateEmail() &&
    validatePassword() &&
    validateConfirmPassword() &&
    validateAgreeToTerms()
  )
})

// 注册处理
const handleRegister = async () => {
  if (!isFormValid.value) {
    showError('请检查表单信息')
    return
  }

  isLoading.value = true
  try {
    await authStore.register({
      username: formData.value.username.trim(),
      email: formData.value.email.trim(),
      password: formData.value.password
    })
    
    showSuccess('注册成功！欢迎加入万里书院')
    router.push({ name: 'Dashboard' })
  } catch (error: any) {
    showError(error.message || '注册失败，请稍后重试')
  } finally {
    isLoading.value = false
  }
}

// GitHub注册
const handleGithubRegister = () => {
  // TODO: 实现GitHub OAuth注册
  showError('GitHub注册功能正在开发中')
}

// Google注册
const handleGoogleRegister = () => {
  // TODO: 实现Google OAuth注册
  showError('Google注册功能正在开发中')
}
</script>

<style scoped>
/* 组件特定样式 */
</style>