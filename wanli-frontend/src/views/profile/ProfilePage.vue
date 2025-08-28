<template>
  <div class="profile-page">
    <!-- 页面头部 -->
    <div class="bg-white shadow-sm border-b">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="py-6">
          <h1 class="text-2xl font-bold text-gray-900">个人资料</h1>
          <p class="mt-1 text-sm text-gray-500">管理您的个人信息和偏好设置</p>
        </div>
      </div>
    </div>

    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
        <!-- 左侧：头像和基本信息 -->
        <div class="lg:col-span-1">
          <div class="bg-white shadow rounded-lg p-6">
            <div class="text-center">
              <div class="relative inline-block">
                <img
                  :src="userProfile?.avatar || defaultAvatar"
                  :alt="userProfile?.name || '用户头像'"
                  class="w-24 h-24 rounded-full object-cover border-4 border-white shadow-lg"
                >
                <button
                  @click="showAvatarUpload = true"
                  class="absolute bottom-0 right-0 bg-blue-600 text-white rounded-full p-2 hover:bg-blue-700 transition-colors"
                >
                  <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 9a2 2 0 012-2h.93a2 2 0 001.664-.89l.812-1.22A2 2 0 0110.07 4h3.86a2 2 0 011.664.89l.812 1.22A2 2 0 0018.07 7H19a2 2 0 012 2v9a2 2 0 01-2 2H5a2 2 0 01-2-2V9z" />
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 13a3 3 0 11-6 0 3 3 0 016 0z" />
                  </svg>
                </button>
              </div>
              <h2 class="mt-4 text-xl font-semibold text-gray-900">{{ userProfile?.name || '未设置姓名' }}</h2>
              <p class="text-gray-500">{{ userProfile?.email }}</p>
              <p class="text-sm text-gray-400 mt-2">加入时间：{{ formatDate(userProfile?.joinDate) }}</p>
            </div>

            <!-- 统计信息 -->
            <div class="mt-6 grid grid-cols-2 gap-4">
              <div class="text-center">
                <div class="text-2xl font-bold text-blue-600">{{ userStats.coursesEnrolled }}</div>
                <div class="text-sm text-gray-500">已报名课程</div>
              </div>
              <div class="text-center">
                <div class="text-2xl font-bold text-green-600">{{ userStats.coursesCompleted }}</div>
                <div class="text-sm text-gray-500">已完成课程</div>
              </div>
              <div class="text-center">
                <div class="text-2xl font-bold text-purple-600">{{ Math.floor(userStats.totalStudyTime / 60) }}</div>
                <div class="text-sm text-gray-500">学习小时</div>
              </div>
              <div class="text-center">
                <div class="text-2xl font-bold text-orange-600">{{ userStats.certificates }}</div>
                <div class="text-sm text-gray-500">获得证书</div>
              </div>
            </div>
          </div>
        </div>

        <!-- 右侧：详细信息表单 -->
        <div class="lg:col-span-2">
          <div class="bg-white shadow rounded-lg">
            <div class="px-6 py-4 border-b border-gray-200">
              <h3 class="text-lg font-medium text-gray-900">个人信息</h3>
            </div>
            
            <form @submit.prevent="handleSubmit" class="p-6 space-y-6">
              <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div>
                  <label for="name" class="block text-sm font-medium text-gray-700 mb-2">
                    姓名 *
                  </label>
                  <input
                    id="name"
                    v-model="formData.name"
                    type="text"
                    required
                    class="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                    :class="{ 'border-red-500': errors.name }"
                  >
                  <p v-if="errors.name" class="mt-1 text-sm text-red-600">{{ errors.name }}</p>
                </div>

                <div>
                  <label for="email" class="block text-sm font-medium text-gray-700 mb-2">
                    邮箱 *
                  </label>
                  <input
                    id="email"
                    v-model="formData.email"
                    type="email"
                    required
                    class="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                    :class="{ 'border-red-500': errors.email }"
                  >
                  <p v-if="errors.email" class="mt-1 text-sm text-red-600">{{ errors.email }}</p>
                </div>

                <div>
                  <label for="phone" class="block text-sm font-medium text-gray-700 mb-2">
                    手机号码
                  </label>
                  <input
                    id="phone"
                    v-model="formData.phone"
                    type="tel"
                    class="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                    :class="{ 'border-red-500': errors.phone }"
                  >
                  <p v-if="errors.phone" class="mt-1 text-sm text-red-600">{{ errors.phone }}</p>
                </div>

                <div>
                  <label for="location" class="block text-sm font-medium text-gray-700 mb-2">
                    所在地区
                  </label>
                  <input
                    id="location"
                    v-model="formData.location"
                    type="text"
                    class="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                  >
                </div>
              </div>

              <div>
                <label for="bio" class="block text-sm font-medium text-gray-700 mb-2">
                  个人简介
                </label>
                <textarea
                  id="bio"
                  v-model="formData.bio"
                  rows="4"
                  class="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                  placeholder="介绍一下您自己..."
                ></textarea>
              </div>

              <div class="flex justify-end space-x-4">
                <AppButton
                  type="button"
                  variant="outline"
                  @click="resetForm"
                  :disabled="isLoading"
                >
                  重置
                </AppButton>
                <AppButton
                  type="submit"
                  :loading="isLoading"
                  :disabled="!isFormValid"
                >
                  保存更改
                </AppButton>
              </div>
            </form>
          </div>
        </div>
      </div>
    </div>

    <!-- 头像上传模态框 -->
    <div v-if="showAvatarUpload" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div class="bg-white rounded-lg p-6 max-w-md w-full mx-4">
        <h3 class="text-lg font-medium text-gray-900 mb-4">更换头像</h3>
        <div class="space-y-4">
          <div>
            <input
              ref="fileInput"
              type="file"
              accept="image/*"
              @change="handleFileSelect"
              class="hidden"
            >
            <button
              @click="$refs.fileInput?.click()"
              class="w-full px-4 py-2 border border-gray-300 rounded-md text-sm font-medium text-gray-700 hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-blue-500"
            >
              选择图片文件
            </button>
          </div>
          <div v-if="selectedFile" class="text-sm text-gray-600">
            已选择：{{ selectedFile.name }}
          </div>
        </div>
        <div class="mt-6 flex justify-end space-x-3">
          <AppButton
            variant="outline"
            @click="cancelAvatarUpload"
          >
            取消
          </AppButton>
          <AppButton
            @click="uploadAvatar"
            :loading="isUploading"
            :disabled="!selectedFile"
          >
            上传
          </AppButton>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, reactive } from 'vue'
import { useUserStore, type UserProfile } from '../../stores/user'
import { useNotification } from '../../composables/useNotification'
import AppButton from '../../components/common/AppButton.vue'

interface FormData {
  name: string
  email: string
  phone: string
  location: string
  bio: string
}

interface FormErrors {
  name?: string
  email?: string
  phone?: string
}

const userStore = useUserStore()
const { success: showSuccess, error: showError } = useNotification()

// 响应式数据
const isLoading = ref(false)
const isUploading = ref(false)
const showAvatarUpload = ref(false)
const selectedFile = ref<File | null>(null)
const fileInput = ref<HTMLInputElement>()

const formData = reactive<FormData>({
  name: '',
  email: '',
  phone: '',
  location: '',
  bio: ''
})

const errors = reactive<FormErrors>({})

// 计算属性
const userProfile = computed(() => userStore.profile)
const userStats = computed(() => userStore.stats)

const defaultAvatar = computed(() => {
  const name = userProfile.value?.name || 'User'
  return `https://ui-avatars.com/api/?name=${encodeURIComponent(name)}&size=96&background=3b82f6&color=ffffff`
})

const isFormValid = computed(() => {
  return formData.name.trim() && formData.email.trim() && !Object.keys(errors).length
})

// 方法
const formatDate = (dateString?: string) => {
  if (!dateString) return '未知'
  return new Date(dateString).toLocaleDateString('zh-CN')
}

const initForm = () => {
  if (userProfile.value) {
    formData.name = userProfile.value.name || ''
    formData.email = userProfile.value.email || ''
    formData.phone = userProfile.value.phone || ''
    formData.location = userProfile.value.location || ''
    formData.bio = userProfile.value.bio || ''
  }
}

const validateForm = (): boolean => {
  // 清空之前的错误
  Object.keys(errors).forEach(key => {
    delete errors[key as keyof FormErrors]
  })

  let isValid = true

  // 验证姓名
  if (!formData.name.trim()) {
    errors.name = '请输入姓名'
    isValid = false
  } else if (formData.name.trim().length < 2) {
    errors.name = '姓名至少需要2个字符'
    isValid = false
  }

  // 验证邮箱
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!formData.email.trim()) {
    errors.email = '请输入邮箱地址'
    isValid = false
  } else if (!emailRegex.test(formData.email)) {
    errors.email = '请输入有效的邮箱地址'
    isValid = false
  }

  // 验证手机号（可选）
  if (formData.phone && formData.phone.trim()) {
    const phoneRegex = /^1[3-9]\d{9}$/
    if (!phoneRegex.test(formData.phone.trim())) {
      errors.phone = '请输入有效的手机号码'
      isValid = false
    }
  }

  return isValid
}

const handleSubmit = async () => {
  if (!validateForm()) {
    showError('请检查表单信息')
    return
  }

  isLoading.value = true

  try {
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 1000))

    // 更新用户资料
    const updatedProfile: Partial<UserProfile> = {
      name: formData.name.trim(),
      email: formData.email.trim(),
      phone: formData.phone.trim() || undefined,
      location: formData.location.trim() || undefined,
      bio: formData.bio.trim() || undefined
    }

    userStore.updateProfile(updatedProfile)
    showSuccess('个人资料更新成功')
  } catch (error) {
    console.error('更新个人资料失败:', error)
    showError('更新失败，请稍后重试')
  } finally {
    isLoading.value = false
  }
}

const resetForm = () => {
  initForm()
  // 清空错误
  Object.keys(errors).forEach(key => {
    delete errors[key as keyof FormErrors]
  })
}

const handleFileSelect = (event: Event) => {
  const target = event.target as HTMLInputElement
  const file = target.files?.[0]
  
  if (file) {
    // 验证文件类型
    if (!file.type.startsWith('image/')) {
      showError('请选择图片文件')
      return
    }
    
    // 验证文件大小（限制为5MB）
    if (file.size > 5 * 1024 * 1024) {
      showError('图片文件大小不能超过5MB')
      return
    }
    
    selectedFile.value = file
  }
}

const uploadAvatar = async () => {
  if (!selectedFile.value) return

  isUploading.value = true

  try {
    // 模拟文件上传
    await new Promise(resolve => setTimeout(resolve, 2000))
    
    // 创建预览URL（实际项目中应该是服务器返回的URL）
    const avatarUrl = URL.createObjectURL(selectedFile.value)
    
    // 更新用户头像
    userStore.updateProfile({ avatar: avatarUrl })
    
    showSuccess('头像更新成功')
    cancelAvatarUpload()
  } catch (error) {
    console.error('头像上传失败:', error)
    showError('头像上传失败，请稍后重试')
  } finally {
    isUploading.value = false
  }
}

const cancelAvatarUpload = () => {
  showAvatarUpload.value = false
  selectedFile.value = null
  if (fileInput.value) {
    fileInput.value.value = ''
  }
}

// 生命周期
onMounted(() => {
  // 初始化用户数据
  userStore.initUserData()
  initForm()
})
</script>

<style scoped>
.profile-page {
  min-height: 100vh;
  background-color: #f9fafb;
}
</style>