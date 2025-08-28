<template>
  <div class="w-full">
    <!-- 标签 -->
    <label
      v-if="label"
      :for="inputId"
      class="block text-sm font-medium text-gray-700 mb-1"
    >
      {{ label }}
      <span v-if="required" class="text-red-500 ml-1">*</span>
    </label>

    <!-- 输入框容器 -->
    <div class="relative">
      <!-- 前缀图标 -->
      <div
        v-if="$slots.prefix"
        class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none"
      >
        <slot name="prefix" />
      </div>

      <!-- 输入框 -->
      <input
        :id="inputId"
        :type="computedType"
        :value="modelValue"
        :placeholder="placeholder"
        :disabled="disabled"
        :readonly="readonly"
        :class="inputClasses"
        :autocomplete="autocomplete"
        @input="handleInput"
        @blur="handleBlur"
        @focus="handleFocus"
        @keydown.enter="handleEnter"
      />

      <!-- 后缀图标/操作 -->
      <div
        v-if="$slots.suffix || showPasswordToggle"
        class="absolute inset-y-0 right-0 pr-3 flex items-center"
      >
        <!-- 密码显示切换 -->
        <button
          v-if="showPasswordToggle"
          type="button"
          class="text-gray-400 hover:text-gray-600 focus:outline-none"
          @click="togglePasswordVisibility"
        >
          <svg
            v-if="showPassword"
            class="w-5 h-5"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
              d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"
            />
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
              d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"
            />
          </svg>
          <svg
            v-else
            class="w-5 h-5"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
              d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.878 9.878L3 3m6.878 6.878L21 21"
            />
          </svg>
        </button>
        
        <!-- 自定义后缀 -->
        <slot v-else name="suffix" />
      </div>
    </div>

    <!-- 帮助文本 -->
    <p
      v-if="helpText && !errorMessage"
      class="mt-1 text-sm text-gray-500"
    >
      {{ helpText }}
    </p>

    <!-- 错误信息 -->
    <p
      v-if="errorMessage"
      class="mt-1 text-sm text-red-600"
    >
      {{ errorMessage }}
    </p>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, nextTick } from 'vue'

export interface AppInputProps {
  modelValue?: string | number
  type?: 'text' | 'email' | 'password' | 'number' | 'tel' | 'url' | 'search'
  label?: string
  placeholder?: string
  helpText?: string
  errorMessage?: string | undefined
  disabled?: boolean
  readonly?: boolean
  required?: boolean
  size?: 'sm' | 'md' | 'lg'
  variant?: 'default' | 'filled'
  autocomplete?: string
}

const props = withDefaults(defineProps<AppInputProps>(), {
  modelValue: '',
  type: 'text',
  disabled: false,
  readonly: false,
  required: false,
  size: 'md',
  variant: 'default'
})

const emit = defineEmits<{
  'update:modelValue': [value: string | number]
  blur: [event: FocusEvent]
  focus: [event: FocusEvent]
  enter: [event: KeyboardEvent]
}>()

// 生成唯一ID
const inputId = `input-${Math.random().toString(36).substr(2, 9)}`

// 密码显示状态
const showPassword = ref(false)

// 是否显示密码切换按钮
const showPasswordToggle = computed(() => props.type === 'password')

// 计算实际的input type
const computedType = computed(() => {
  if (props.type === 'password') {
    return showPassword.value ? 'text' : 'password'
  }
  return props.type
})

// 计算输入框样式类
const inputClasses = computed(() => {
  const baseClasses = [
    'block',
    'w-full',
    'border',
    'rounded-md',
    'shadow-sm',
    'transition-colors',
    'duration-200',
    'focus:outline-none',
    'focus:ring-2',
    'focus:ring-offset-0',
    'disabled:bg-gray-50',
    'disabled:text-gray-500',
    'disabled:cursor-not-allowed'
  ]

  // 尺寸样式
  const sizeClasses = {
    sm: ['px-3', 'py-1.5', 'text-sm'],
    md: ['px-3', 'py-2', 'text-sm'],
    lg: ['px-4', 'py-3', 'text-base']
  }

  // 变体样式
  const variantClasses = {
    default: ['bg-white'],
    filled: ['bg-gray-50']
  }

  // 状态样式
  const stateClasses = props.errorMessage
    ? [
        'border-red-300',
        'text-red-900',
        'placeholder-red-300',
        'focus:ring-red-500',
        'focus:border-red-500'
      ]
    : [
        'border-gray-300',
        'text-gray-900',
        'placeholder-gray-400',
        'focus:ring-blue-500',
        'focus:border-blue-500'
      ]

  // 前缀/后缀间距
  const spacingClasses = []
  if (props.$slots?.prefix) {
    spacingClasses.push('pl-10')
  }
  if (props.$slots?.suffix || showPasswordToggle.value) {
    spacingClasses.push('pr-10')
  }

  return [
    ...baseClasses,
    ...sizeClasses[props.size],
    ...variantClasses[props.variant],
    ...stateClasses,
    ...spacingClasses
  ]
})

// 切换密码显示
const togglePasswordVisibility = () => {
  showPassword.value = !showPassword.value
}

// 输入事件处理
const handleInput = (event: Event) => {
  const target = event.target as HTMLInputElement
  const value = props.type === 'number' ? Number(target.value) : target.value
  emit('update:modelValue', value)
}

// 失焦事件处理
const handleBlur = (event: FocusEvent) => {
  emit('blur', event)
}

// 聚焦事件处理
const handleFocus = (event: FocusEvent) => {
  emit('focus', event)
}

// 回车事件处理
const handleEnter = (event: KeyboardEvent) => {
  emit('enter', event)
}
</script>

<style scoped>
/* 组件特定样式 */
</style>