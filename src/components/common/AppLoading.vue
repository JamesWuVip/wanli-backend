<template>
  <div :class="loadingClasses">
    <div class="app-loading__spinner">
      <div v-if="type === 'spinner'" class="app-loading__spinner-circle">
        <svg class="animate-spin" fill="none" viewBox="0 0 24 24">
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
            d="m4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
          />
        </svg>
      </div>
      
      <div v-else-if="type === 'dots'" class="app-loading__dots">
        <div class="app-loading__dot" />
        <div class="app-loading__dot" />
        <div class="app-loading__dot" />
      </div>
      
      <div v-else-if="type === 'pulse'" class="app-loading__pulse">
        <div class="app-loading__pulse-circle" />
      </div>
      
      <div v-else-if="type === 'bars'" class="app-loading__bars">
        <div class="app-loading__bar" />
        <div class="app-loading__bar" />
        <div class="app-loading__bar" />
        <div class="app-loading__bar" />
      </div>
    </div>
    
    <div v-if="text" class="app-loading__text">
      {{ text }}
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

export interface AppLoadingProps {
  type?: 'spinner' | 'dots' | 'pulse' | 'bars'
  size?: 'small' | 'medium' | 'large'
  color?: 'primary' | 'secondary' | 'success' | 'warning' | 'danger' | 'info'
  text?: string
  overlay?: boolean
  fullscreen?: boolean
}

const props = withDefaults(defineProps<AppLoadingProps>(), {
  type: 'spinner',
  size: 'medium',
  color: 'primary',
  overlay: false,
  fullscreen: false
})

const loadingClasses = computed(() => {
  const classes = [
    'app-loading',
    `app-loading--${props.size}`,
    `app-loading--${props.color}`
  ]

  if (props.overlay) classes.push('app-loading--overlay')
  if (props.fullscreen) classes.push('app-loading--fullscreen')

  return classes
})
</script>

<style scoped>
.app-loading {
  @apply flex flex-col items-center justify-center;
}

.app-loading--overlay {
  @apply absolute inset-0 bg-white bg-opacity-80 z-10;
}

.app-loading--fullscreen {
  @apply fixed inset-0 bg-white bg-opacity-90 z-50;
}

/* 尺寸 */
.app-loading--small .app-loading__spinner {
  @apply w-4 h-4;
}

.app-loading--medium .app-loading__spinner {
  @apply w-6 h-6;
}

.app-loading--large .app-loading__spinner {
  @apply w-8 h-8;
}

/* 颜色 */
.app-loading--primary {
  @apply text-blue-600;
}

.app-loading--secondary {
  @apply text-gray-600;
}

.app-loading--success {
  @apply text-green-600;
}

.app-loading--warning {
  @apply text-yellow-600;
}

.app-loading--danger {
  @apply text-red-600;
}

.app-loading--info {
  @apply text-cyan-600;
}

/* Spinner 样式 */
.app-loading__spinner-circle {
  @apply w-full h-full;
}

/* Dots 样式 */
.app-loading__dots {
  @apply flex items-center gap-1;
}

.app-loading__dot {
  @apply w-2 h-2 bg-current rounded-full;
  animation: dot-bounce 1.4s ease-in-out infinite both;
}

.app-loading__dot:nth-child(1) {
  animation-delay: -0.32s;
}

.app-loading__dot:nth-child(2) {
  animation-delay: -0.16s;
}

@keyframes dot-bounce {
  0%, 80%, 100% {
    transform: scale(0);
  }
  40% {
    transform: scale(1);
  }
}

/* Pulse 样式 */
.app-loading__pulse {
  @apply w-full h-full relative;
}

.app-loading__pulse-circle {
  @apply w-full h-full bg-current rounded-full;
  animation: pulse-scale 1s ease-in-out infinite;
}

@keyframes pulse-scale {
  0% {
    transform: scale(0);
    opacity: 1;
  }
  100% {
    transform: scale(1);
    opacity: 0;
  }
}

/* Bars 样式 */
.app-loading__bars {
  @apply flex items-end gap-1 h-full;
}

.app-loading__bar {
  @apply w-1 bg-current;
  animation: bar-stretch 1.2s ease-in-out infinite;
}

.app-loading__bar:nth-child(1) {
  animation-delay: -1.1s;
}

.app-loading__bar:nth-child(2) {
  animation-delay: -1.0s;
}

.app-loading__bar:nth-child(3) {
  animation-delay: -0.9s;
}

.app-loading__bar:nth-child(4) {
  animation-delay: -0.8s;
}

@keyframes bar-stretch {
  0%, 40%, 100% {
    height: 20%;
  }
  20% {
    height: 100%;
  }
}

/* 文本样式 */
.app-loading__text {
  @apply mt-2 text-sm text-gray-600;
}

.app-loading--small .app-loading__text {
  @apply text-xs;
}

.app-loading--large .app-loading__text {
  @apply text-base;
}

/* 响应式调整 */
@media (max-width: 640px) {
  .app-loading--large .app-loading__spinner {
    @apply w-6 h-6;
  }
  
  .app-loading--large .app-loading__text {
    @apply text-sm;
  }
}
</style>