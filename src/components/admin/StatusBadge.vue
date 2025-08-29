<template>
  <span
    :class="[
      'inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium',
      statusClass
    ]"
  >
    <component :is="statusIcon" class="w-3 h-3 mr-1" />
    {{ statusText }}
  </span>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { CheckCircle, AlertCircle, XCircle, Clock } from 'lucide-vue-next'

type StatusType = 'healthy' | 'warning' | 'error' | 'unknown'

interface Props {
  status: StatusType
}

const props = defineProps<Props>()

// 状态配置
const statusConfig: Record<StatusType, {
  text: string
  class: string
  icon: any
}> = {
  healthy: {
    text: '正常',
    class: 'bg-green-100 text-green-800',
    icon: CheckCircle
  },
  warning: {
    text: '警告',
    class: 'bg-yellow-100 text-yellow-800',
    icon: AlertCircle
  },
  error: {
    text: '异常',
    class: 'bg-red-100 text-red-800',
    icon: XCircle
  },
  unknown: {
    text: '未知',
    class: 'bg-gray-100 text-gray-800',
    icon: Clock
  }
}

// 计算属性
const statusClass = computed(() => {
  return statusConfig[props.status]?.class || statusConfig.unknown.class
})

const statusText = computed(() => {
  return statusConfig[props.status]?.text || statusConfig.unknown.text
})

const statusIcon = computed(() => {
  return statusConfig[props.status]?.icon || statusConfig.unknown.icon
})
</script>