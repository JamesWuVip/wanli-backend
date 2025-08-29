<template>
  <button
    :class="buttonClasses"
    :disabled="disabled || loading"
    :type="htmlType"
    @click="handleClick"
  >
    <span v-if="loading" class="app-button__loading">
      <svg class="animate-spin h-4 w-4" fill="none" viewBox="0 0 24 24">
        <circle
          class="opacity-25"
          cx="12"
          cy="12"
          r="10"
          stroke="currentColor"
          stroke-width="4"
        />
        <path
          class="opacity-75"
          fill="currentColor"
          d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
        />
      </svg>
    </span>
    
    <span v-if="icon && !loading" :class="iconClasses">
      <component :is="icon" />
    </span>
    
    <span v-if="$slots.default" class="app-button__content">
      <slot />
    </span>
  </button>
</template>

<script setup lang="ts">
import { computed } from 'vue'

export interface AppButtonProps {
  type?: 'primary' | 'secondary' | 'success' | 'warning' | 'danger' | 'info' | 'text'
  size?: 'small' | 'medium' | 'large'
  variant?: 'solid' | 'outline' | 'ghost'
  disabled?: boolean
  loading?: boolean
  block?: boolean
  round?: boolean
  icon?: any
  iconPosition?: 'left' | 'right'
  htmlType?: 'button' | 'submit' | 'reset'
}

export interface AppButtonEmits {
  click: [event: MouseEvent]
}

const props = withDefaults(defineProps<AppButtonProps>(), {
  type: 'primary',
  size: 'medium',
  variant: 'solid',
  disabled: false,
  loading: false,
  block: false,
  round: false,
  iconPosition: 'left',
  htmlType: 'button'
})

const emit = defineEmits<AppButtonEmits>()

const buttonClasses = computed(() => {
  const classes = [
    'app-button',
    `app-button--${props.type}`,
    `app-button--${props.size}`,
    `app-button--${props.variant}`
  ]

  if (props.disabled) classes.push('app-button--disabled')
  if (props.loading) classes.push('app-button--loading')
  if (props.block) classes.push('app-button--block')
  if (props.round) classes.push('app-button--round')

  return classes
})

const iconClasses = computed(() => {
  const classes = ['app-button__icon']
  
  if (props.iconPosition === 'right') {
    classes.push('app-button__icon--right')
  }
  
  return classes
})

const handleClick = (event: MouseEvent) => {
  if (!props.disabled && !props.loading) {
    emit('click', event)
  }
}
</script>

<style scoped>
.app-button {
  @apply inline-flex items-center justify-center font-medium transition-all duration-200 focus:outline-none focus:ring-2 focus:ring-offset-2 cursor-pointer;
}

/* 尺寸样式 */
.app-button--small {
  @apply px-3 py-1.5 text-sm gap-1.5;
}

.app-button--medium {
  @apply px-4 py-2 text-base gap-2;
}

.app-button--large {
  @apply px-6 py-3 text-lg gap-2.5;
}

/* 类型样式 - Solid 变体 */
.app-button--primary.app-button--solid {
  @apply bg-blue-600 text-white border border-blue-600 hover:bg-blue-700 focus:ring-blue-500;
}

.app-button--secondary.app-button--solid {
  @apply bg-gray-600 text-white border border-gray-600 hover:bg-gray-700 focus:ring-gray-500;
}

.app-button--success.app-button--solid {
  @apply bg-green-600 text-white border border-green-600 hover:bg-green-700 focus:ring-green-500;
}

.app-button--warning.app-button--solid {
  @apply bg-yellow-600 text-white border border-yellow-600 hover:bg-yellow-700 focus:ring-yellow-500;
}

.app-button--danger.app-button--solid {
  @apply bg-red-600 text-white border border-red-600 hover:bg-red-700 focus:ring-red-500;
}

.app-button--info.app-button--solid {
  @apply bg-cyan-600 text-white border border-cyan-600 hover:bg-cyan-700 focus:ring-cyan-500;
}

.app-button--text.app-button--solid {
  @apply bg-transparent text-gray-700 border-transparent hover:bg-gray-100 focus:ring-gray-500;
}

/* 类型样式 - Outline 变体 */
.app-button--primary.app-button--outline {
  @apply bg-transparent text-blue-600 border border-blue-600 hover:bg-blue-50 focus:ring-blue-500;
}

.app-button--secondary.app-button--outline {
  @apply bg-transparent text-gray-600 border border-gray-600 hover:bg-gray-50 focus:ring-gray-500;
}

.app-button--success.app-button--outline {
  @apply bg-transparent text-green-600 border border-green-600 hover:bg-green-50 focus:ring-green-500;
}

.app-button--warning.app-button--outline {
  @apply bg-transparent text-yellow-600 border border-yellow-600 hover:bg-yellow-50 focus:ring-yellow-500;
}

.app-button--danger.app-button--outline {
  @apply bg-transparent text-red-600 border border-red-600 hover:bg-red-50 focus:ring-red-500;
}

.app-button--info.app-button--outline {
  @apply bg-transparent text-cyan-600 border border-cyan-600 hover:bg-cyan-50 focus:ring-cyan-500;
}

.app-button--text.app-button--outline {
  @apply bg-transparent text-gray-700 border border-gray-300 hover:bg-gray-50 focus:ring-gray-500;
}

/* 类型样式 - Ghost 变体 */
.app-button--primary.app-button--ghost {
  @apply bg-transparent text-blue-600 border-transparent hover:bg-blue-50 focus:ring-blue-500;
}

.app-button--secondary.app-button--ghost {
  @apply bg-transparent text-gray-600 border-transparent hover:bg-gray-50 focus:ring-gray-500;
}

.app-button--success.app-button--ghost {
  @apply bg-transparent text-green-600 border-transparent hover:bg-green-50 focus:ring-green-500;
}

.app-button--warning.app-button--ghost {
  @apply bg-transparent text-yellow-600 border-transparent hover:bg-yellow-50 focus:ring-yellow-500;
}

.app-button--danger.app-button--ghost {
  @apply bg-transparent text-red-600 border-transparent hover:bg-red-50 focus:ring-red-500;
}

.app-button--info.app-button--ghost {
  @apply bg-transparent text-cyan-600 border-transparent hover:bg-cyan-50 focus:ring-cyan-500;
}

.app-button--text.app-button--ghost {
  @apply bg-transparent text-gray-700 border-transparent hover:bg-gray-50 focus:ring-gray-500;
}

/* 状态样式 */
.app-button--disabled {
  @apply opacity-50 cursor-not-allowed;
}

.app-button--loading {
  @apply cursor-wait;
}

.app-button--block {
  @apply w-full;
}

.app-button--round {
  @apply rounded-full;
}

.app-button:not(.app-button--round) {
  @apply rounded-md;
}

/* 图标样式 */
.app-button__icon {
  @apply flex-shrink-0;
}

.app-button__icon--right {
  @apply order-1;
}

.app-button__loading {
  @apply flex-shrink-0;
}

.app-button__content {
  @apply flex-1;
}

/* 小尺寸图标 */
.app-button--small .app-button__icon,
.app-button--small .app-button__loading {
  @apply w-4 h-4;
}

/* 中等尺寸图标 */
.app-button--medium .app-button__icon,
.app-button--medium .app-button__loading {
  @apply w-5 h-5;
}

/* 大尺寸图标 */
.app-button--large .app-button__icon,
.app-button--large .app-button__loading {
  @apply w-6 h-6;
}
</style>