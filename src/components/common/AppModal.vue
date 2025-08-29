<template>
  <Teleport to="body">
    <Transition name="modal" appear>
      <div v-if="visible" class="app-modal" @click="handleMaskClick">
        <div :class="modalClasses" @click.stop>
          <!-- Header -->
          <div v-if="$slots.header || title" class="app-modal__header">
            <slot name="header">
              <h3 class="app-modal__title">{{ title }}</h3>
            </slot>
            <button
              v-if="closable"
              type="button"
              class="app-modal__close"
              @click="handleClose"
            >
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
              </svg>
            </button>
          </div>

          <!-- Body -->
          <div class="app-modal__body">
            <slot />
          </div>

          <!-- Footer -->
          <div v-if="$slots.footer || showDefaultFooter" class="app-modal__footer">
            <slot name="footer">
              <div v-if="showDefaultFooter" class="app-modal__actions">
                <AppButton
                  variant="outline"
                  @click="handleCancel"
                >
                  {{ cancelText }}
                </AppButton>
                <AppButton
                  type="primary"
                  :loading="confirmLoading"
                  @click="handleConfirm"
                >
                  {{ confirmText }}
                </AppButton>
              </div>
            </slot>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup lang="ts">
import { computed, watch, nextTick } from 'vue'
import AppButton from './AppButton.vue'

export interface AppModalProps {
  visible?: boolean
  title?: string
  width?: string | number
  closable?: boolean
  maskClosable?: boolean
  showDefaultFooter?: boolean
  confirmText?: string
  cancelText?: string
  confirmLoading?: boolean
  destroyOnClose?: boolean
  centered?: boolean
  size?: 'small' | 'medium' | 'large' | 'full'
}

export interface AppModalEmits {
  'update:visible': [visible: boolean]
  close: []
  cancel: []
  confirm: []
  afterEnter: []
  afterLeave: []
}

const props = withDefaults(defineProps<AppModalProps>(), {
  visible: false,
  closable: true,
  maskClosable: true,
  showDefaultFooter: false,
  confirmText: '确定',
  cancelText: '取消',
  confirmLoading: false,
  destroyOnClose: false,
  centered: true,
  size: 'medium'
})

const emit = defineEmits<AppModalEmits>()

const modalClasses = computed(() => {
  const classes = [
    'app-modal__content',
    `app-modal__content--${props.size}`
  ]

  if (props.centered) classes.push('app-modal__content--centered')

  return classes
})

const modalStyle = computed(() => {
  const style: Record<string, string> = {}

  if (props.width) {
    if (typeof props.width === 'number') {
      style.width = `${props.width}px`
    } else {
      style.width = props.width
    }
  }

  return style
})

const handleMaskClick = () => {
  if (props.maskClosable) {
    handleClose()
  }
}

const handleClose = () => {
  emit('update:visible', false)
  emit('close')
}

const handleCancel = () => {
  emit('cancel')
  handleClose()
}

const handleConfirm = () => {
  emit('confirm')
}

// 监听 visible 变化，处理 body 滚动
watch(
  () => props.visible,
  (newVisible) => {
    nextTick(() => {
      if (newVisible) {
        document.body.style.overflow = 'hidden'
        emit('afterEnter')
      } else {
        document.body.style.overflow = ''
        emit('afterLeave')
      }
    })
  },
  { immediate: true }
)

// 组件卸载时恢复 body 滚动
// onUnmounted(() => {
//   document.body.style.overflow = ''
// })
</script>

<style scoped>
.app-modal {
  @apply fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-50 p-4;
}

.app-modal__content {
  @apply bg-white rounded-lg shadow-xl max-h-full overflow-hidden flex flex-col;
  max-width: calc(100vw - 2rem);
}

.app-modal__content--small {
  @apply w-full max-w-md;
}

.app-modal__content--medium {
  @apply w-full max-w-lg;
}

.app-modal__content--large {
  @apply w-full max-w-4xl;
}

.app-modal__content--full {
  @apply w-full h-full max-w-none max-h-none rounded-none;
}

.app-modal__content--centered {
  @apply mx-auto;
}

.app-modal__header {
  @apply flex items-center justify-between px-6 py-4 border-b border-gray-200;
}

.app-modal__title {
  @apply text-lg font-semibold text-gray-900 m-0;
}

.app-modal__close {
  @apply text-gray-400 hover:text-gray-600 transition-colors duration-200 p-1 rounded;
}

.app-modal__close:hover {
  @apply bg-gray-100;
}

.app-modal__body {
  @apply flex-1 px-6 py-4 overflow-y-auto;
}

.app-modal__footer {
  @apply px-6 py-4 border-t border-gray-200 bg-gray-50;
}

.app-modal__actions {
  @apply flex items-center justify-end gap-3;
}

/* 动画效果 */
.modal-enter-active,
.modal-leave-active {
  transition: opacity 0.3s ease;
}

.modal-enter-active .app-modal__content,
.modal-leave-active .app-modal__content {
  transition: transform 0.3s ease;
}

.modal-enter-from,
.modal-leave-to {
  opacity: 0;
}

.modal-enter-from .app-modal__content,
.modal-leave-to .app-modal__content {
  transform: scale(0.9) translateY(-20px);
}

/* 响应式设计 */
@media (max-width: 640px) {
  .app-modal {
    @apply p-2;
  }
  
  .app-modal__content--small,
  .app-modal__content--medium,
  .app-modal__content--large {
    @apply w-full max-w-none;
  }
  
  .app-modal__header,
  .app-modal__body,
  .app-modal__footer {
    @apply px-4;
  }
}
</style>