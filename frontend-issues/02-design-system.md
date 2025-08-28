# 设计系统和基础UI组件开发

## Issue描述
实现万里书院前端设计系统，包括设计令牌、基础UI组件和响应式布局系统。

## 任务清单

### 设计令牌系统
- [ ] 定义颜色系统(主色调、辅助色、状态色)
- [ ] 定义字体系统(字号、行高、字重)
- [ ] 定义间距系统(margin、padding规范)
- [ ] 定义阴影和圆角系统
- [ ] 配置TailwindCSS自定义主题

### 基础原子组件
- [ ] AppButton组件(多种样式变体)
- [ ] AppInput组件(文本、密码、搜索等类型)
- [ ] AppCard组件(卡片容器)
- [ ] AppModal组件(模态框)
- [ ] AppBadge组件(标签徽章)
- [ ] AppAvatar组件(用户头像)
- [ ] LoadingSpinner组件(加载动画)

### 分子组件
- [ ] SearchBox组件(搜索框)
- [ ] Pagination组件(分页器)
- [ ] FormField组件(表单字段)
- [ ] Dropdown组件(下拉菜单)
- [ ] Breadcrumb组件(面包屑导航)

### 布局组件
- [ ] MainLayout组件(主布局)
- [ ] AppHeader组件(顶部导航)
- [ ] AppSidebar组件(侧边栏)
- [ ] AppFooter组件(页脚)

### 响应式系统
- [ ] 移动端适配(320px-768px)
- [ ] 平板端适配(768px-1024px)
- [ ] 桌面端适配(1024px+)
- [ ] 响应式断点配置

## 设计规范

### 颜色系统
```css
:root {
  /* 主色调 */
  --primary-50: #eff6ff;
  --primary-500: #3b82f6;
  --primary-600: #2563eb;
  --primary-700: #1d4ed8;
  
  /* 状态色 */
  --success-500: #10b981;
  --warning-500: #f59e0b;
  --error-500: #ef4444;
  
  /* 中性色 */
  --gray-50: #f9fafb;
  --gray-100: #f3f4f6;
  --gray-500: #6b7280;
  --gray-900: #111827;
}
```

### 组件规范
- 所有组件必须支持TypeScript
- 使用Composition API语法
- 遵循Vue 3最佳实践
- 支持主题切换(亮色/暗色)
- 组件必须可访问性友好

## 验收标准
- 所有组件通过单元测试
- 组件在Storybook中正确展示
- 响应式布局在各设备正常显示
- 设计令牌系统完整可用
- 组件API文档完整

## 技术要求
- 使用Vue 3 + TypeScript
- 遵循原子设计理论
- 组件props必须有类型定义
- 支持v-model双向绑定
- 事件命名遵循Vue规范

## 优先级
高优先级 - 后续页面开发依赖

## 预估工时
2个工作日

## 相关文档
- SP1前端开发方案-产品需求文档.md
- SP1前端开发方案-技术架构文档.md