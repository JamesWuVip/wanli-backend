<template>
  <div :class="cardClasses" @click="handleClick">
    <div v-if="$slots.header || title" class="app-card__header">
      <slot name="header">
        <h3 v-if="title" class="app-card__title">{{ title }}</h3>
      </slot>
      <div v-if="$slots.extra" class="app-card__extra">
        <slot name="extra" />
      </div>
    </div>
    
    <div v-if="$slots.default" class="app-card__body">
      <slot />
    </div>
    
    <div v-if="$slots.footer" class="app-card__footer">
      <slot name="footer" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

export interface AppCardProps {
  title?: string
  shadow?: 'never' | 'hover' | 'always'
  bordered?: boolean
  hoverable?: boolean
  loading?: boolean
  size?: 'small' | 'medium' | 'large'
  bodyPadding?: boolean
}

export interface AppCardEmits {
  click: [event: MouseEvent]
}

const props = withDefaults(defineProps<AppCardProps>(), {
  shadow: 'hover',
  bordered: true,
  hoverable: false,
  loading: false,
  size: 'medium',
  bodyPadding: true
})

const emit = defineEmits<AppCardEmits>()

const cardClasses = computed(() => {
  const classes = [
    'app-card',
    `app-card--${props.size}`,
    `app-card--shadow-${props.shadow}`
  ]

  if (props.bordered) classes.push('app-card--bordered')
  if (props.hoverable) classes.push('app-card--hoverable')
  if (props.loading) classes.push('app-card--loading')
  if (!props.bodyPadding) classes.push('app-card--no-padding')

  return classes
})

const handleClick = (event: MouseEvent) => {
  if (props.hoverable) {
    emit('click', event)
  }
}
</script>

<style scoped>
.app-card {
  @apply bg-white rounded-lg overflow-hidden transition-all duration-200;
}

.app-card--bordered {
  @apply border border-gray-200;
}

.app-card--shadow-never {
  @apply shadow-none;
}

.app-card--shadow-hover {
  @apply shadow-sm hover:shadow-md;
}

.app-card--shadow-always {
  @apply shadow-md;
}

.app-card--hoverable {
  @apply cursor-pointer;
}

.app-card--hoverable:hover {
  @apply transform -translate-y-0.5 shadow-lg;
}

.app-card--loading {
  @apply opacity-60 pointer-events-none;
}

.app-card--loading::after {
  content: '';
  @apply absolute inset-0 bg-white bg-opacity-50;
  background-image: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.4), transparent);
  animation: loading 1.5s infinite;
}

@keyframes loading {
  0% {
    transform: translateX(-100%);
  }
  100% {
    transform: translateX(100%);
  }
}

.app-card__header {
  @apply flex items-center justify-between;
}

.app-card--small .app-card__header {
  @apply px-3 py-2 border-b border-gray-100;
}

.app-card--medium .app-card__header {
  @apply px-4 py-3 border-b border-gray-100;
}

.app-card--large .app-card__header {
  @apply px-6 py-4 border-b border-gray-100;
}

.app-card__title {
  @apply font-semibold text-gray-900 m-0;
}

.app-card--small .app-card__title {
  @apply text-sm;
}

.app-card--medium .app-card__title {
  @apply text-base;
}

.app-card--large .app-card__title {
  @apply text-lg;
}

.app-card__extra {
  @apply flex items-center gap-2;
}

.app-card__body {
  @apply flex-1;
}

.app-card--small .app-card__body {
  @apply px-3 py-2;
}

.app-card--medium .app-card__body {
  @apply px-4 py-3;
}

.app-card--large .app-card__body {
  @apply px-6 py-4;
}

.app-card--no-padding .app-card__body {
  @apply p-0;
}

.app-card__footer {
  @apply border-t border-gray-100;
}

.app-card--small .app-card__footer {
  @apply px-3 py-2;
}

.app-card--medium .app-card__footer {
  @apply px-4 py-3;
}

.app-card--large .app-card__footer {
  @apply px-6 py-4;
}
</style>