# 更新日志

## [未发布] - 2024-01-20

### 修复
- 修复Ant Design组件兼容性问题
  - 修复Tabs组件：将TabPane子元素替换为items属性格式
  - 修复Card组件：将bodyStyle属性替换为styles.body格式
  - 确保所有Upload组件正确使用fileList属性而非value属性

### 影响的文件
- src/components/forms/ClassDetail.tsx
- src/components/layouts/AuthLayout.tsx
- src/pages/teacher/Students.tsx
- 以及其他相关组件文件
