<template>
  <div class="app-input">
    <label v-if="label" :for="inputId" class="app-input__label">
      {{ label }}
      <span v-if="required" class="app-input__required">*</span>
    </label>
    
    <div class="app-input__wrapper" :class="wrapperClasses">
      <span v-if="prefixIcon" class="app-input__prefix-icon">
        <component :is="prefixIcon" />
      </span>
      
      <input
        :id="inputId"
        ref="inputRef"
        :type="type"
        :value="modelValue"
        :placeholder="placeholder"
        :disabled="disabled"
        :readonly="readonly"
        :maxlength="maxlength"
        :minlength="minlength"
        :max="max"
        :min="min"
        :step="step"
        :autocomplete="autocomplete"
        :class="inputClasses"
        @input="handleInput"
        @change="handleChange"
        @focus="handleFocus"
        @blur="handleBlur"
        @keydown="handleKeydown"
        @keyup="handleKeyup"
      />
      
      <span v-if="suffixIcon || showClearButton" class="app-input__suffix">
        <button
          v-if="showClearButton"
          type="button"
          class="app-input__clear-button"
          @click="handleClear"
        >
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
          </svg>
        </button>
        
        <span v-if="suffixIcon" class="app-input__suffix-icon">
          <component :is="suffixIcon" />
        </span>
      </span>
    </div>
    
    <div v-if="showWordCount" class="app-input__word-count">
      {{ currentLength }}/{{ maxlength }}
    </div>
    
    <div v-if="errorMessage" class="app-input__error">
      {{ errorMessage }}
    </div>
    
    <div v-if="helpText && !errorMessage" class="app-input__help">
      {{ helpText }}
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, nextTick } from 'vue'
import { generateId } from '@/utils/common'

export interface AppInputProps {
  modelValue?: string | number
  type?: 'text' | 'password' | 'email' | 'number' | 'tel' | 'url' | 'search'
  label?: string
  placeholder?: string
  disabled?: boolean
  readonly?: boolean
  required?: boolean
  clearable?: boolean
  size?: 'small' | 'medium' | 'large'
  maxlength?: number
  minlength?: number
  max?: number | string
  min?: number | string
  step?: number | string
  autocomplete?: string
  prefixIcon?: any
  suffixIcon?: any
  errorMessage?: string
  helpText?: string
  showWordCount?: boolean
  validateEvent?: boolean
}

export interface AppInputEmits {
  'update:modelValue': [value: string | number]
  input: [value: string | number, event: Event]
  change: [value: string | number, event: Event]
  focus: [event: FocusEvent]
  blur: [event: FocusEvent]
  clear: []
  keydown: [event: KeyboardEvent]
  keyup: [event: KeyboardEvent]
}

const props = withDefaults(defineProps<AppInputProps>(), {
  type: 'text',
  disabled: false,
  readonly: false,
  required: false,
  clearable: false,
  size: 'medium',
  validateEvent: true
})

const emit = defineEmits<AppInputEmits>()

const inputRef = ref<HTMLInputElement>()
const focused = ref(false)
const inputId = generateId('app-input')

const currentLength = computed(() => {
  return String(props.modelValue || '').length
})

const showClearButton = computed(() => {
  return props.clearable && !props.disabled && !props.readonly && props.modelValue && focused.value
})

const wrapperClasses = computed(() => {
  const classes = [
    'app-input__wrapper--base',
    `app-input__wrapper--${props.size}`
  ]

  if (focused.value) classes.push('app-input__wrapper--focused')
  if (props.disabled) classes.push('app-input__wrapper--disabled')
  if (props.readonly) classes.push('app-input__wrapper--readonly')
  if (props.errorMessage) classes.push('app-input__wrapper--error')
  if (props.prefixIcon) classes.push('app-input__wrapper--has-prefix')
  if (props.suffixIcon || showClearButton.value) classes.push('app-input__wrapper--has-suffix')

  return classes
})

const inputClasses = computed(() => {
  const classes = [
    'app-input__input',
    `app-input__input--${props.size}`
  ]

  return classes
})

const handleInput = (event: Event) => {
  const target = event.target as HTMLInputElement
  let value: string | number = target.value

  if (props.type === 'number') {
    value = target.valueAsNumber || 0
  }

  emit('update:modelValue', value)
  emit('input', value, event)
}

const handleChange = (event: Event) => {
  const target = event.target as HTMLInputElement
  let value: string | number = target.value

  if (props.type === 'number') {
    value = target.valueAsNumber || 0
  }

  emit('change', value, event)
}

const handleFocus = (event: FocusEvent) => {
  focused.value = true
  emit('focus', event)
}

const handleBlur = (event: FocusEvent) => {
  focused.value = false
  emit('blur', event)
}

const handleClear = () => {
  emit('update:modelValue', '')
  emit('clear')
  nextTick(() => {
    inputRef.value?.focus()
  })
}

const handleKeydown = (event: KeyboardEvent) => {
  emit('keydown', event)
}

const handleKeyup = (event: KeyboardEvent) => {
  emit('keyup', event)
}

// 暴露方法给父组件
defineExpose({
  focus: () => inputRef.value?.focus(),
  blur: () => inputRef.value?.blur(),
  select: () => inputRef.value?.select()
})
</script>

<style scoped>
.app-input {
  @apply w-full;
}

.app-input__label {
  @apply block text-sm font-medium text-gray-700 mb-1;
}

.app-input__required {
  @apply text-red-500 ml-1;
}

.app-input__wrapper {
  @apply relative;
}

.app-input__wrapper--base {
  @apply flex items-center border border-gray-300 rounded-md bg-white transition-all duration-200;
}

.app-input__wrapper--small {
  @apply text-sm;
}

.app-input__wrapper--medium {
  @apply text-base;
}

.app-input__wrapper--large {
  @apply text-lg;
}

.app-input__wrapper--focused {
  @apply border-blue-500 ring-1 ring-blue-500;
}

.app-input__wrapper--disabled {
  @apply bg-gray-50 border-gray-200 cursor-not-allowed;
}

.app-input__wrapper--readonly {
  @apply bg-gray-50 border-gray-200;
}

.app-input__wrapper--error {
  @apply border-red-500 ring-1 ring-red-500;
}

.app-input__wrapper--has-prefix .app-input__input {
  @apply pl-10;
}

.app-input__wrapper--has-suffix .app-input__input {
  @apply pr-10;
}

.app-input__input {
  @apply flex-1 border-0 bg-transparent outline-none placeholder-gray-400;
}

.app-input__input--small {
  @apply px-3 py-1.5 text-sm;
}

.app-input__input--medium {
  @apply px-3 py-2 text-base;
}

.app-input__input--large {
  @apply px-4 py-3 text-lg;
}

.app-input__input:disabled {
  @apply cursor-not-allowed;
}

.app-input__prefix-icon {
  @apply absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 pointer-events-none;
}

.app-input__suffix {
  @apply absolute right-3 top-1/2 transform -translate-y-1/2 flex items-center gap-1;
}

.app-input__suffix-icon {
  @apply text-gray-400 pointer-events-none;
}

.app-input__clear-button {
  @apply text-gray-400 hover:text-gray-600 transition-colors duration-200 p-0.5 rounded;
}

.app-input__clear-button:hover {
  @apply bg-gray-100;
}

.app-input__word-count {
  @apply text-xs text-gray-500 mt-1 text-right;
}

.app-input__error {
  @apply text-sm text-red-600 mt-1;
}

.app-input__help {
  @apply text-sm text-gray-500 mt-1;
}

/* 图标尺寸 */
.app-input__wrapper--small .app-input__prefix-icon,
.app-input__wrapper--small .app-input__suffix-icon {
  @apply w-4 h-4;
}

.app-input__wrapper--medium .app-input__prefix-icon,
.app-input__wrapper--medium .app-input__suffix-icon {
  @apply w-5 h-5;
}

.app-input__wrapper--large .app-input__prefix-icon,
.app-input__wrapper--large .app-input__suffix-icon {
  @apply w-6 h-6;
}
</style>