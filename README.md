# 万里前端项目 (Wanli Frontend)

基于 Vue 3 + TypeScript + Vite 构建的现代化前端项目，采用企业级开发规范和最佳实践。

## 🚀 技术栈

- **框架**: Vue 3 (Composition API)
- **语言**: TypeScript
- **构建工具**: Vite
- **路由**: Vue Router 4
- **状态管理**: Pinia
- **HTTP客户端**: Axios
- **代码规范**: ESLint + Prettier
- **Git钩子**: Husky + lint-staged
- **样式**: CSS3 + 响应式设计

## 📁 项目结构

```
src/
├── api/                # API请求服务层
│   ├── modules/        # 按业务模块划分
│   └── index.ts        # Axios实例配置
├── assets/             # 静态资源
├── components/         # 全局通用组件
│   ├── common/         # 基础原子组件
│   └── layout/         # 布局组件
├── composables/        # Vue组合式函数
├── router/             # 路由配置
├── store/              # 状态管理(Pinia)
├── types/              # TypeScript类型定义
├── utils/              # 工具函数
├── views/              # 页面组件
├── App.vue             # 根组件
└── main.ts             # 应用入口
```

## 🛠️ 开发环境要求

- Node.js >= 18.0.0
- npm >= 8.0.0

## 📦 安装依赖

```bash
npm install
```

## 🚀 启动开发服务器

```bash
npm run dev
```

## 🏗️ 构建项目

```bash
# 开发环境构建
npm run build:dev

# 测试环境构建
npm run build:test

# 生产环境构建
npm run build:prod
```

## 🧪 测试

```bash
# 运行单元测试
npm run test

# 运行E2E测试
npm run test:e2e

# 运行所有测试
npm run test:all
```

## 📋 代码规范

```bash
# 代码检查
npm run lint

# 代码格式化
npm run format

# 类型检查
npm run type-check
```

## 🔧 环境配置

项目支持多环境配置：

- `.env.development` - 开发环境
- `.env.test` - 测试环境
- `.env.production` - 生产环境

## 📝 开发规范

### 命名规则

1. **文件夹命名**: 小写字母，下划线分隔
2. **文件命名**: 小写字母，下划线分隔
3. **类命名**: 驼峰命名
4. **方法命名**: 驼峰命名
5. **变量命名**: 驼峰命名
6. **常量命名**: 大写字母，下划线分隔

### Git 提交规范

遵循 Conventional Commits 规范：

- `feat`: 新功能
- `fix`: Bug修复
- `docs`: 文档更新
- `style`: 代码格式化
- `refactor`: 代码重构
- `test`: 测试相关
- `chore`: 构建过程或辅助工具的变动

## 🚀 部署

### 本地预览

```bash
# 测试环境预览
npm run preview:test

# 生产环境预览
npm run preview:prod
```

### GitFlow 工作流

项目采用 GitFlow 工作流：

1. `main` - 生产分支
2. `staging` - 测试分支
3. `dev` - 开发分支
4. `feature/*` - 功能分支

## 📊 项目状态

- ✅ 基础架构搭建完成
- ✅ 开发环境配置完成
- ✅ 代码规范配置完成
- ✅ 测试环境配置完成
- ✅ GitFlow 工作流配置完成

## 🤝 贡献指南

1. Fork 项目
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'feat: Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 创建 Pull Request

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。