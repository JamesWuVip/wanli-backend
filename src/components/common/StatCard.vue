<template>
  <div class="bg-white rounded-lg shadow p-6">
    <div class="flex items-center justify-between">
      <div>
        <p class="text-sm font-medium text-gray-600">{{ title }}</p>
        <p class="text-2xl font-bold text-gray-900 mt-1">{{ value }}</p>
      </div>
      <div :class="iconClasses">
        <component :is="iconComponent" class="w-6 h-6" />
      </div>
    </div>
    
    <div v-if="change !== undefined" class="mt-4 flex items-center">
      <span :class="changeClasses" class="text-sm font-medium">
        {{ change >= 0 ? '+' : '' }}{{ change }}%
      </span>
      <span class="text-sm text-gray-500 ml-2">较上月</span>
    </div>
    
    <div v-if="loading" class="absolute inset-0 bg-white bg-opacity-75 flex items-center justify-center rounded-lg">
      <div class="animate-spin rounded-full h-6 w-6 border-b-2 border-blue-600"></div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { 
  Store, 
  BookOpen, 
  Users, 
  DollarSign,
  TrendingUp,
  TrendingDown
} from 'lucide-vue-next'

interface Props {
  title: string
  value: string | number
  change?: number
  icon: string
  color: 'blue' | 'green' | 'purple' | 'orange'
  loading?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  loading: false
})

const iconComponent = computed(() => {
  const iconMap: Record<string, any> = {
    Store,
    BookOpen,
    Users,
    DollarSign
  }
  return iconMap[props.icon] || Store
})

const iconClasses = computed(() => {
  const colorMap = {
    blue: 'bg-blue-100 text-blue-600',
    green: 'bg-green-100 text-green-600',
    purple: 'bg-purple-100 text-purple-600',
    orange: 'bg-orange-100 text-orange-600'
  }
  return `p-3 rounded-full ${colorMap[props.color]}`
})

const changeClasses = computed(() => {
  if (props.change === undefined) return ''
  return props.change >= 0 ? 'text-green-600' : 'text-red-600'
})
</script>

<style scoped>
.relative {
  position: relative;
}

.absolute {
  position: absolute;
}

.inset-0 {
  top: 0;
  right: 0;
  bottom: 0;
  left: 0;
}
</style>