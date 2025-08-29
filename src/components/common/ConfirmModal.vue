<template>
  <Teleport to="body">
    <Transition name="modal" appear>
      <div v-if="visible" class="confirm-modal" @click="handleMaskClick">
        <div class="confirm-modal__content" @click.stop>
          <!-- Header -->
          <div class="confirm-modal__header">
            <div class="confirm-modal__icon">
              <svg class="w-6 h-6 text-orange-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.964-.833-2.732 0L3.732 16.5c-.77.833.192 2.5 1.732 2.5z" />
              </svg>
            </div>
            <div class="confirm-modal__text">
              <h3 class="confirm-modal__title">{{ title || '确认操作' }}</h3>
              <p class="confirm-modal__message">{{ message }}</p>
            </div>
          </div>

          <!-- Footer -->
          <div class="confirm-modal__footer">
            <button
              type="button"
              class="confirm-modal__button confirm-modal__button--cancel"
              @click="handleCancel"
            >
              {{ cancelText }}
            </button>
            <button
              type="button"
              class="confirm-modal__button confirm-modal__button--confirm"
              :disabled="loading"
              @click="handleConfirm"
            >
              <span v-if="loading" class="confirm-modal__loading"></span>
              {{ confirmText }}
            </button>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup lang="ts">
import { nextTick, watch } from 'vue'

export interface ConfirmModalProps {
  visible?: boolean
  title?: string
  message?: string
  confirmText?: string
  cancelText?: string
  loading?: boolean
  maskClosable?: boolean
}

export interface ConfirmModalEmits {
  'update:visible': [visible: boolean]
  confirm: []
  cancel: []
}

const props = withDefaults(defineProps<ConfirmModalProps>(), {
  visible: false,
  confirmText: '确定',
  cancelText: '取消',
  loading: false,
  maskClosable: true
})

const emit = defineEmits<ConfirmModalEmits>()

const handleMaskClick = () => {
  if (props.maskClosable) {
    handleCancel()
  }
}

const handleCancel = () => {
  emit('update:visible', false)
  emit('cancel')
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
      } else {
        document.body.style.overflow = ''
      }
    })
  },
  { immediate: true }
)
</script>

<style scoped>
.confirm-modal {
  @apply fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-50 p-4;
}

.confirm-modal__content {
  @apply bg-white rounded-lg shadow-xl w-full max-w-md;
}

.confirm-modal__header {
  @apply flex items-start gap-4 p-6 pb-4;
}

.confirm-modal__icon {
  @apply flex-shrink-0;
}

.confirm-modal__text {
  @apply flex-1;
}

.confirm-modal__title {
  @apply text-lg font-semibold text-gray-900 mb-2;
}

.confirm-modal__message {
  @apply text-sm text-gray-600 leading-relaxed;
}

.confirm-modal__footer {
  @apply flex justify-end gap-3 px-6 pb-6;
}

.confirm-modal__button {
  @apply px-4 py-2 text-sm font-medium rounded-md border transition-colors focus:outline-none focus:ring-2 focus:ring-offset-2;
}

.confirm-modal__button--cancel {
  @apply border-gray-300 text-gray-700 bg-white hover:bg-gray-50 focus:ring-gray-500;
}

.confirm-modal__button--confirm {
  @apply border-transparent text-white bg-red-600 hover:bg-red-700 focus:ring-red-500 disabled:opacity-50 disabled:cursor-not-allowed;
}

.confirm-modal__loading {
  @apply inline-block w-4 h-4 mr-2 border-2 border-white border-t-transparent rounded-full animate-spin;
}

/* 过渡动画 */
.modal-enter-active,
.modal-leave-active {
  transition: opacity 0.3s ease;
}

.modal-enter-active .confirm-modal__content,
.modal-leave-active .confirm-modal__content {
  transition: transform 0.3s ease;
}

.modal-enter-from,
.modal-leave-to {
  opacity: 0;
}

.modal-enter-from .confirm-modal__content,
.modal-leave-to .confirm-modal__content {
  transform: scale(0.9) translateY(-20px);
}
</style>