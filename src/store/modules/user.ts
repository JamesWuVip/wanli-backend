import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

// 用户详细信息接口
interface UserProfile {
  id: string
  username: string
  email: string
  fullName: string
  avatar?: string
  phone?: string
  role: string
  createdAt: string
  updatedAt: string
}

// 用户偏好设置接口
interface UserPreferences {
  theme: 'light' | 'dark' | 'auto'
  language: 'zh-CN' | 'en-US'
  notifications: {
    email: boolean
    push: boolean
    sms: boolean
  }
}

export const useUserStore = defineStore('user', () => {
  // 状态
  const profile = ref<UserProfile | null>(null)
  const preferences = ref<UserPreferences>({
    theme: 'light',
    language: 'zh-CN',
    notifications: {
      email: true,
      push: true,
      sms: false
    }
  })
  const loading = ref(false)

  // 计算属性
  const displayName = computed(() => {
    return profile.value?.fullName || profile.value?.username || '未知用户'
  })

  const avatarUrl = computed(() => {
    return profile.value?.avatar || '/default-avatar.png'
  })

  const isAdmin = computed(() => {
    return profile.value?.role === 'admin'
  })

  // Actions
  const fetchUserProfile = async (userId: string) => {
    loading.value = true
    try {
      // TODO: 调用获取用户信息 API
      // const response = await userApi.getProfile(userId)
      // profile.value = response.data
      
      // 临时模拟数据
      console.log('Fetching user profile for:', userId)
      profile.value = {
        id: userId,
        username: 'testuser',
        email: 'test@example.com',
        fullName: '测试用户',
        role: 'user',
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString()
      }
    } catch (error) {
      console.error('Failed to fetch user profile:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  const updateProfile = async (updates: Partial<UserProfile>) => {
    loading.value = true
    try {
      // TODO: 调用更新用户信息 API
      // const response = await userApi.updateProfile(updates)
      // profile.value = { ...profile.value, ...response.data }
      
      // 临时模拟更新
      if (profile.value) {
        profile.value = { ...profile.value, ...updates }
      }
      console.log('Profile updated:', updates)
    } catch (error) {
      console.error('Failed to update profile:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  const updatePreferences = async (newPreferences: Partial<UserPreferences>) => {
    try {
      // TODO: 调用更新用户偏好 API
      // await userApi.updatePreferences(newPreferences)
      
      preferences.value = { ...preferences.value, ...newPreferences }
      
      // 保存到本地存储
      localStorage.setItem('userPreferences', JSON.stringify(preferences.value))
      
      console.log('Preferences updated:', newPreferences)
    } catch (error) {
      console.error('Failed to update preferences:', error)
      throw error
    }
  }

  const loadPreferences = () => {
    try {
      const saved = localStorage.getItem('userPreferences')
      if (saved) {
        preferences.value = { ...preferences.value, ...JSON.parse(saved) }
      }
    } catch (error) {
      console.error('Failed to load preferences:', error)
    }
  }

  const clearUserData = () => {
    profile.value = null
    preferences.value = {
      theme: 'light',
      language: 'zh-CN',
      notifications: {
        email: true,
        push: true,
        sms: false
      }
    }
    localStorage.removeItem('userPreferences')
  }

  // 初始化时加载偏好设置
  loadPreferences()

  return {
    // 状态
    profile,
    preferences,
    loading,
    // 计算属性
    displayName,
    avatarUrl,
    isAdmin,
    // Actions
    fetchUserProfile,
    updateProfile,
    updatePreferences,
    loadPreferences,
    clearUserData
  }
})