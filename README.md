# 万里加盟连锁门店作业管理系统

## 项目概述

万里加盟连锁门店作业管理系统是一个专为教育培训机构设计的全栈Web应用，支持总部、门店、教师和学生的多角色协作管理。

## 技术栈

### 前端
- React 18 + TypeScript
- Ant Design 5.x (UI组件库)
- Zustand (状态管理)
- React Router (路由管理)
- Vite (构建工具)
- Tailwind CSS (样式框架)

### 后端
- Node.js + Express
- Supabase (数据库 + 认证)
- TypeScript

### 测试
- Vitest (单元测试)
- Playwright (E2E测试)

## 功能模块

### 1. 用户管理
- 多角色用户系统（总部管理员、门店管理员、教师、学生）
- 用户注册、登录、权限管理
- 用户信息维护

### 2. 门店管理
- 门店信息管理
- 门店用户分配
- 门店数据统计

### 3. 课程管理
- 课程创建和编辑
- 课程分类管理
- 课程发布和下架

### 4. 作业管理
- 作业创建和分发
- 学生作业提交
- 教师批改和评分
- 作业统计分析

### 5. 班级管理
- 班级创建和管理
- 学生分班
- 班级作业分配

## 项目结构

```
├── api/                    # 后端API服务
├── src/                    # 前端源码
│   ├── components/         # React组件
│   ├── pages/             # 页面组件
│   ├── services/          # API服务
│   ├── stores/            # 状态管理
│   ├── types/             # TypeScript类型定义
│   └── utils/             # 工具函数
├── supabase/              # 数据库迁移文件
├── tests/                 # 测试文件
└── public/                # 静态资源
```

## 开发环境搭建

### 1. 克隆项目
```bash
git clone https://github.com/JamesWuVip/wanli-backend.git
cd wanli-backend
```

### 2. 安装依赖
```bash
npm install
```

### 3. 环境配置
复制 `.env.example` 到 `.env` 并配置相关环境变量：
```bash
cp .env.example .env
```

### 4. 启动开发服务器
```bash
# 启动前端和后端服务
npm run dev
```

### 5. 运行测试
```bash
# 单元测试
npm test

# 集成测试
npm run test:integration

# E2E测试
npm run test:e2e
```

## 部署说明

### 开发环境
- 分支：`dev`
- 自动部署到测试环境

### 测试环境
- 分支：`staging`
- 用于功能测试和验收

### 生产环境
- 分支：`main`
- 生产环境部署

## 贡献指南

1. 从 `dev` 分支创建功能分支
2. 完成开发并通过测试
3. 提交 Pull Request 到 `dev` 分支
4. 代码审查通过后合并
5. 测试通过后合并到 `staging` 分支
6. 最终合并到 `main` 分支发布

## 许可证

MIT License
