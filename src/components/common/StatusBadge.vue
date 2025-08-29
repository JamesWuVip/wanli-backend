<template>
  <span :class="badgeClasses" class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium">
    <span :class="dotClasses" class="w-2 h-2 rounded-full mr-1.5"></span>
    {{ statusText }}
  </span>
</template>

<script setup lang="ts">
import { computed } from 'vue'

type Status = 'online' | 'offline' | 'warning' | 'error'

interface Props {
  status: Status
}

const props = defineProps<Props>()

const statusConfig = {
  online: {
    text: '正常',
    badgeClass: 'bg-green-100 text-green-800',
    dotClass: 'bg-green-400'
  },
  offline: {
    text: '离线',
    badgeClass: 'bg-gray-100 text-gray-800',
    dotClass: 'bg-gray-400'
  },
  warning: {
    text: '警告',
    badgeClass: 'bg-yellow-100 text-yellow-800',
    dotClass: 'bg-yellow-400'
  },
  error: {
    text: '错误',
    badgeClass: 'bg-red-100 text-red-800',
    dotClass: 'bg-red-400'
  }
}

const statusText = computed(() => statusConfig[props.status].text)
const badgeClasses = computed(() => statusConfig[props.status].badgeClass)
const dotClasses = computed(() => statusConfig[props.status].dotClass)
</script>