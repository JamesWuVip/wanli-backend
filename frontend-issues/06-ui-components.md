# UI组件库开发

## Issue描述
开发完整的UI组件库，包括基础原子组件、复合组件、布局组件和业务组件，确保设计一致性和可复用性。

## 任务清单

### 基础原子组件
- [ ] AppButton - 按钮组件
- [ ] AppInput - 输入框组件
- [ ] AppTextarea - 文本域组件
- [ ] AppSelect - 选择器组件
- [ ] AppCheckbox - 复选框组件
- [ ] AppRadio - 单选框组件
- [ ] AppSwitch - 开关组件
- [ ] AppSlider - 滑块组件
- [ ] AppProgress - 进度条组件
- [ ] AppBadge - 徽章组件
- [ ] AppTag - 标签组件
- [ ] AppAvatar - 头像组件
- [ ] AppIcon - 图标组件
- [ ] AppSpinner - 加载动画组件

### 反馈组件
- [ ] AppAlert - 警告提示组件
- [ ] AppMessage - 消息提示组件
- [ ] AppNotification - 通知组件
- [ ] AppModal - 模态框组件
- [ ] AppDrawer - 抽屉组件
- [ ] AppPopover - 气泡卡片组件
- [ ] AppTooltip - 文字提示组件
- [ ] AppConfirm - 确认对话框组件

### 导航组件
- [ ] AppTabs - 标签页组件
- [ ] AppBreadcrumb - 面包屑组件
- [ ] AppPagination - 分页组件
- [ ] AppSteps - 步骤条组件
- [ ] AppMenu - 菜单组件
- [ ] AppDropdown - 下拉菜单组件

### 数据展示组件
- [ ] AppTable - 表格组件
- [ ] AppList - 列表组件
- [ ] AppCard - 卡片组件
- [ ] AppCollapse - 折叠面板组件
- [ ] AppCarousel - 轮播图组件
- [ ] AppTimeline - 时间轴组件
- [ ] AppTree - 树形控件组件
- [ ] AppCalendar - 日历组件

### 表单组件
- [ ] AppForm - 表单容器组件
- [ ] AppFormItem - 表单项组件
- [ ] AppDatePicker - 日期选择器
- [ ] AppTimePicker - 时间选择器
- [ ] AppColorPicker - 颜色选择器
- [ ] AppUpload - 文件上传组件
- [ ] AppEditor - 富文本编辑器
- [ ] AppSearch - 搜索框组件

### 布局组件
- [ ] AppLayout - 页面布局组件
- [ ] AppHeader - 页头组件
- [ ] AppSidebar - 侧边栏组件
- [ ] AppFooter - 页脚组件
- [ ] AppContainer - 容器组件
- [ ] AppGrid - 栅格系统组件
- [ ] AppSpace - 间距组件
- [ ] AppDivider - 分割线组件

## 组件设计规范

### 基础按钮组件
```vue
<!-- components/common/AppButton.vue -->
<template>
  <button
    :class="buttonClasses"
    :disabled="disabled || loading"
    :type="htmlType"
    @click="handleClick"
  >
    <AppSpinner v-if="loading" :size="spinnerSize" class="mr-2" />
    <slot name="icon" v-if="!loading && $slots.icon" />
    <span v-if="$slots.default" :class="{ 'ml-2': $slots.icon && !loading }">
      <slot />
    </span>
  </button>
</template>

<script setup lang="ts">
interface Props {
  variant?: 'primary' | 'secondary' | 'outline' | 'ghost' | 'danger'
  size?: 'xs' | 'sm' | 'md' | 'lg' | 'xl'
  disabled?: boolean
  loading?: boolean
  block?: boolean
  round?: boolean
  htmlType?: 'button' | 'submit' | 'reset'
}

const props = withDefaults(defineProps<Props>(), {
  variant: 'primary',
  size: 'md',
  disabled: false,
  loading: false,
  block: false,
  round: false,
  htmlType: 'button'
})

const emit = defineEmits<{
  click: [event: MouseEvent]
}>()

const buttonClasses = computed(() => {
  const baseClasses = [
    'inline-flex',
    'items-center',
    'justify-center',
    'font-medium',
    'transition-colors',
    'focus:outline-none',
    'focus:ring-2',
    'focus:ring-offset-2',
    'disabled:opacity-50',
    'disabled:cursor-not-allowed'
  ]
  
  // 尺寸样式
  const sizeClasses = {
    xs: ['px-2', 'py-1', 'text-xs', 'rounded'],
    sm: ['px-3', 'py-1.5', 'text-sm', 'rounded'],
    md: ['px-4', 'py-2', 'text-sm', 'rounded-md'],
    lg: ['px-6', 'py-3', 'text-base', 'rounded-md'],
    xl: ['px-8', 'py-4', 'text-lg', 'rounded-lg']
  }
  
  // 变体样式
  const variantClasses = {
    primary: [
      'bg-primary-600',
      'text-white',
      'hover:bg-primary-700',
      'focus:ring-primary-500'
    ],
    secondary: [
      'bg-gray-600',
      'text-white',
      'hover:bg-gray-700',
      'focus:ring-gray-500'
    ],
    outline: [
      'border',
      'border-gray-300',
      'text-gray-700',
      'bg-white',
      'hover:bg-gray-50',
      'focus:ring-primary-500'
    ],
    ghost: [
      'text-gray-700',
      'hover:bg-gray-100',
      'focus:ring-primary-500'
    ],
    danger: [
      'bg-red-600',
      'text-white',
      'hover:bg-red-700',
      'focus:ring-red-500'
    ]
  }
  
  const classes = [
    ...baseClasses,
    ...sizeClasses[props.size],
    ...variantClasses[props.variant]
  ]
  
  if (props.block) {
    classes.push('w-full')
  }
  
  if (props.round) {
    classes.push('rounded-full')
  }
  
  return classes
})

const spinnerSize = computed(() => {
  const sizeMap = {
    xs: 'xs',
    sm: 'sm',
    md: 'sm',
    lg: 'md',
    xl: 'lg'
  }
  return sizeMap[props.size]
})

const handleClick = (event: MouseEvent) => {
  if (!props.disabled && !props.loading) {
    emit('click', event)
  }
}
</script>
```

### 输入框组件
```vue
<!-- components/common/AppInput.vue -->
<template>
  <div class="app-input">
    <label v-if="label" :for="inputId" class="block text-sm font-medium text-gray-700 mb-1">
      {{ label }}
      <span v-if="required" class="text-red-500 ml-1">*</span>
    </label>
    
    <div class="relative">
      <div v-if="$slots.prefix" class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
        <slot name="prefix" />
      </div>
      
      <input
        :id="inputId"
        ref="inputRef"
        :type="type"
        :value="modelValue"
        :placeholder="placeholder"
        :disabled="disabled"
        :readonly="readonly"
        :maxlength="maxlength"
        :class="inputClasses"
        @input="handleInput"
        @change="handleChange"
        @focus="handleFocus"
        @blur="handleBlur"
      />
      
      <div v-if="$slots.suffix || clearable" class="absolute inset-y-0 right-0 pr-3 flex items-center">
        <button
          v-if="clearable && modelValue && !disabled"
          type="button"
          class="text-gray-400 hover:text-gray-600"
          @click="handleClear"
        >
          <XMarkIcon class="w-4 h-4" />
        </button>
        <slot name="suffix" />
      </div>
    </div>
    
    <div v-if="error || hint" class="mt-1">
      <p v-if="error" class="text-sm text-red-600">{{ error }}</p>
      <p v-else-if="hint" class="text-sm text-gray-500">{{ hint }}</p>
    </div>
  </div>
</template>

<script setup lang="ts">
interface Props {
  modelValue?: string | number
  type?: 'text' | 'password' | 'email' | 'number' | 'tel' | 'url'
  label?: string
  placeholder?: string
  disabled?: boolean
  readonly?: boolean
  required?: boolean
  clearable?: boolean
  maxlength?: number
  size?: 'sm' | 'md' | 'lg'
  error?: string
  hint?: string
}

const props = withDefaults(defineProps<Props>(), {
  type: 'text',
  disabled: false,
  readonly: false,
  required: false,
  clearable: false,
  size: 'md'
})

const emit = defineEmits<{
  'update:modelValue': [value: string | number]
  change: [value: string | number]
  focus: [event: FocusEvent]
  blur: [event: FocusEvent]
  clear: []
}>()

const inputRef = ref<HTMLInputElement>()
const inputId = `input-${Math.random().toString(36).substr(2, 9)}`

const inputClasses = computed(() => {
  const baseClasses = [
    'block',
    'w-full',
    'border',
    'rounded-md',
    'shadow-sm',
    'focus:outline-none',
    'focus:ring-1',
    'disabled:bg-gray-50',
    'disabled:text-gray-500',
    'disabled:cursor-not-allowed'
  ]
  
  const sizeClasses = {
    sm: ['px-3', 'py-1.5', 'text-sm'],
    md: ['px-3', 'py-2', 'text-sm'],
    lg: ['px-4', 'py-3', 'text-base']
  }
  
  const stateClasses = props.error
    ? [
        'border-red-300',
        'text-red-900',
        'placeholder-red-300',
        'focus:border-red-500',
        'focus:ring-red-500'
      ]
    : [
        'border-gray-300',
        'placeholder-gray-400',
        'focus:border-primary-500',
        'focus:ring-primary-500'
      ]
  
  const paddingClasses = []
  if (props.$slots?.prefix) {
    paddingClasses.push('pl-10')
  }
  if (props.$slots?.suffix || props.clearable) {
    paddingClasses.push('pr-10')
  }
  
  return [
    ...baseClasses,
    ...sizeClasses[props.size],
    ...stateClasses,
    ...paddingClasses
  ]
})

const handleInput = (event: Event) => {
  const target = event.target as HTMLInputElement
  emit('update:modelValue', target.value)
}

const handleChange = (event: Event) => {
  const target = event.target as HTMLInputElement
  emit('change', target.value)
}

const handleFocus = (event: FocusEvent) => {
  emit('focus', event)
}

const handleBlur = (event: FocusEvent) => {
  emit('blur', event)
}

const handleClear = () => {
  emit('update:modelValue', '')
  emit('clear')
  inputRef.value?.focus()
}

const focus = () => {
  inputRef.value?.focus()
}

const blur = () => {
  inputRef.value?.blur()
}

defineExpose({
  focus,
  blur
})
</script>
```

### 模态框组件
```vue
<!-- components/common/AppModal.vue -->
<template>
  <Teleport to="body">
    <Transition
      enter-active-class="transition-opacity duration-300"
      enter-from-class="opacity-0"
      enter-to-class="opacity-100"
      leave-active-class="transition-opacity duration-300"
      leave-from-class="opacity-100"
      leave-to-class="opacity-0"
    >
      <div
        v-if="visible"
        class="fixed inset-0 z-50 overflow-y-auto"
        @click="handleMaskClick"
      >
        <!-- 遮罩层 -->
        <div class="fixed inset-0 bg-black bg-opacity-50 transition-opacity" />
        
        <!-- 模态框容器 -->
        <div class="flex min-h-full items-center justify-center p-4">
          <Transition
            enter-active-class="transition-all duration-300"
            enter-from-class="opacity-0 scale-95"
            enter-to-class="opacity-100 scale-100"
            leave-active-class="transition-all duration-300"
            leave-from-class="opacity-100 scale-100"
            leave-to-class="opacity-0 scale-95"
          >
            <div
              v-if="visible"
              :class="modalClasses"
              @click.stop
            >
              <!-- 头部 -->
              <div v-if="$slots.header || title" class="flex items-center justify-between p-6 border-b border-gray-200">
                <div class="flex-1">
                  <slot name="header">
                    <h3 class="text-lg font-semibold text-gray-900">
                      {{ title }}
                    </h3>
                  </slot>
                </div>
                
                <button
                  v-if="closable"
                  type="button"
                  class="text-gray-400 hover:text-gray-600 transition-colors"
                  @click="handleClose"
                >
                  <XMarkIcon class="w-6 h-6" />
                </button>
              </div>
              
              <!-- 内容 -->
              <div class="p-6">
                <slot />
              </div>
              
              <!-- 底部 -->
              <div v-if="$slots.footer" class="flex justify-end space-x-3 p-6 border-t border-gray-200">
                <slot name="footer" />
              </div>
            </div>
          </Transition>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup lang="ts">
interface Props {
  visible?: boolean
  title?: string
  width?: string | number
  closable?: boolean
  maskClosable?: boolean
  destroyOnClose?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  visible: false,
  closable: true,
  maskClosable: true,
  destroyOnClose: false
})

const emit = defineEmits<{
  'update:visible': [visible: boolean]
  close: []
  open: []
}>()

const modalClasses = computed(() => {
  const baseClasses = [
    'relative',
    'bg-white',
    'rounded-lg',
    'shadow-xl',
    'max-w-full',
    'max-h-full',
    'overflow-hidden'
  ]
  
  const widthClass = typeof props.width === 'number'
    ? `w-[${props.width}px]`
    : props.width || 'w-full max-w-md'
  
  return [...baseClasses, widthClass]
})

const handleClose = () => {
  emit('update:visible', false)
  emit('close')
}

const handleMaskClick = () => {
  if (props.maskClosable) {
    handleClose()
  }
}

// 监听ESC键关闭
const handleKeydown = (event: KeyboardEvent) => {
  if (event.key === 'Escape' && props.visible && props.closable) {
    handleClose()
  }
}

onMounted(() => {
  document.addEventListener('keydown', handleKeydown)
})

onUnmounted(() => {
  document.removeEventListener('keydown', handleKeydown)
})

// 监听visible变化
watch(() => props.visible, (newVisible) => {
  if (newVisible) {
    emit('open')
    // 禁止body滚动
    document.body.style.overflow = 'hidden'
  } else {
    // 恢复body滚动
    document.body.style.overflow = ''
  }
})

// 组件卸载时恢复body滚动
onUnmounted(() => {
  document.body.style.overflow = ''
})
</script>
```

## 设计令牌系统

### 颜色系统
```typescript
// tokens/colors.ts
export const colors = {
  // 主色调
  primary: {
    50: '#eff6ff',
    100: '#dbeafe',
    200: '#bfdbfe',
    300: '#93c5fd',
    400: '#60a5fa',
    500: '#3b82f6',
    600: '#2563eb',
    700: '#1d4ed8',
    800: '#1e40af',
    900: '#1e3a8a'
  },
  
  // 中性色
  gray: {
    50: '#f9fafb',
    100: '#f3f4f6',
    200: '#e5e7eb',
    300: '#d1d5db',
    400: '#9ca3af',
    500: '#6b7280',
    600: '#4b5563',
    700: '#374151',
    800: '#1f2937',
    900: '#111827'
  },
  
  // 功能色
  success: {
    50: '#ecfdf5',
    500: '#10b981',
    600: '#059669'
  },
  
  warning: {
    50: '#fffbeb',
    500: '#f59e0b',
    600: '#d97706'
  },
  
  error: {
    50: '#fef2f2',
    500: '#ef4444',
    600: '#dc2626'
  }
}
```

### 间距系统
```typescript
// tokens/spacing.ts
export const spacing = {
  0: '0px',
  1: '4px',
  2: '8px',
  3: '12px',
  4: '16px',
  5: '20px',
  6: '24px',
  8: '32px',
  10: '40px',
  12: '48px',
  16: '64px',
  20: '80px',
  24: '96px',
  32: '128px'
}
```

## 验收标准
- 所有组件API设计合理
- 组件样式一致性良好
- 支持主题定制
- 无障碍访问支持
- TypeScript类型完整
- 组件文档完善
- 单元测试覆盖
- 响应式设计适配

## 技术要求
- 使用Vue 3 Composition API
- 支持完整的TypeScript类型
- 遵循无障碍访问标准
- 实现主题系统
- 支持按需加载
- 提供完整的API文档

## 优先级
中优先级 - 基础设施组件

## 预估工时
4个工作日

## 相关文档
- SP1前端开发方案-产品需求文档.md
- SP1前端开发方案-技术架构文档.md
- 设计系统规范文档