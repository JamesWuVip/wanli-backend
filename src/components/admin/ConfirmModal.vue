<template>
  <div class="fixed inset-0 bg-gray-600 bg-opacity-50 overflow-y-auto h-full w-full z-50">
    <div class="relative top-20 mx-auto p-5 border w-96 shadow-lg rounded-md bg-white">
      <div class="mt-3 text-center">
        <!-- 图标 -->
        <div class="mx-auto flex items-center justify-center h-12 w-12 rounded-full bg-red-100">
          <AlertTriangle class="h-6 w-6 text-red-600" />
        </div>
        
        <!-- 标题 -->
        <h3 class="text-lg leading-6 font-medium text-gray-900 mt-4">
          {{ title || '确认操作' }}
        </h3>
        
        <!-- 内容 -->
        <div class="mt-2 px-7 py-3">
          <p class="text-sm text-gray-500">
            {{ message || '此操作不可撤销，请确认是否继续？' }}
          </p>
        </div>
        
        <!-- 按钮 -->
        <div class="flex items-center justify-center space-x-3 px-4 py-3">
          <button
            @click="$emit('cancel')"
            type="button"
            class="px-4 py-2 bg-white border border-gray-300 rounded-md shadow-sm text-sm font-medium text-gray-700 hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500"
          >
            {{ cancelText || '取消' }}
          </button>
          <button
            @click="$emit('confirm')"
            :disabled="loading"
            type="button"
            :class="[
              'inline-flex items-center px-4 py-2 border border-transparent rounded-md shadow-sm text-sm font-medium text-white focus:outline-none focus:ring-2 focus:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed',
              dangerMode ? 'bg-red-600 hover:bg-red-700 focus:ring-red-500' : 'bg-blue-600 hover:bg-blue-700 focus:ring-blue-500'
            ]"
          >
            <Loader2 v-if="loading" class="w-4 h-4 mr-2 animate-spin" />
            {{ loading ? '处理中...' : (confirmText || '确认') }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { AlertTriangle, Loader2 } from 'lucide-vue-next'

// Props
defineProps<{
  title?: string
  message?: string
  confirmText?: string
  cancelText?: string
  loading?: boolean
  dangerMode?: boolean
}>()

// Emits
defineEmits<{
  confirm: []
  cancel: []
}>()
</script>