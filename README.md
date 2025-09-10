# 万里加盟连锁门店作业管理系统

## 项目概述

万里加盟连锁门店作业管理系统是一个专为教育培训机构设计的综合管理平台，支持总部、门店、教师和学生的全方位管理需求。

## 技术栈

### 前端
- React 18
- TypeScript
- Ant Design
- Zustand (状态管理)
- React Router
- Axios

### 后端
- Node.js
- Express
- TypeScript
- Prisma ORM
- PostgreSQL
- JWT认证

### 测试
- Playwright (E2E测试)
- Jest (单元测试)

## 功能模块

### 1. 用户管理
- 多角色用户系统（管理员、教师、学生）
- 用户注册、登录、权限管理
- 个人信息管理

### 2. 门店管理
- 门店信息管理
- 门店员工管理
- 门店课程管理

### 3. 课程管理
- 课程创建与编辑
- 课程分类管理
- 课程资源管理

### 4. 教师工作台
- 课程管理
- 作业批改
- 学生管理
- 教学统计

### 5. 学生学习平台
- 课程学习
- 作业提交
- 成绩查看
- 学习进度跟踪

## 项目结构

```
wanli-backend/
├── api/                    # 后端API代码
│   ├── controllers/        # 控制器
│   ├── middleware/         # 中间件
│   ├── models/            # 数据模型
│   ├── routes/            # 路由定义
│   └── utils/             # 工具函数
├── frontend/              # 前端代码
│   ├── src/
│   │   ├── components/    # 组件
│   │   ├── pages/         # 页面
│   │   ├── services/      # API服务
│   │   ├── stores/        # 状态管理
│   │   └── types/         # 类型定义
│   └── public/            # 静态资源
├── tests/                 # 测试文件
│   ├── e2e/              # E2E测试
│   └── unit/             # 单元测试
├── prisma/               # 数据库模式
└── docs/                 # 文档
```

## 开发环境设置

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
```bash
cp .env.example .env
# 编辑 .env 文件，配置数据库连接等信息
```

### 4. 数据库设置
```bash
npx prisma migrate dev
npx prisma generate
```

### 5. 启动开发服务器
```bash
# 启动后端服务
npm run dev:backend

# 启动前端服务
npm run dev:frontend
```

## 测试

### 运行E2E测试
```bash
npm run test:e2e
```

### 运行单元测试
```bash
npm test
```

## 部署

### 开发环境
- 分支：`dev`
- 自动部署到测试环境

### 测试环境
- 分支：`staging`
- 用于功能测试和用户验收测试

### 生产环境
- 分支：`main`
- 生产环境部署

## Git工作流

本项目采用GitFlow工作流：

1. `main` - 生产环境分支
2. `staging` - 测试环境分支
3. `dev` - 开发分支
4. `feature/*` - 功能开发分支
5. `fix/*` - 问题修复分支

## 贡献指南

1. Fork 项目
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 创建 Pull Request

## 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。

## 联系方式

- 项目维护者：James Wu
- 邮箱：james@wanli.com
- 项目链接：https://github.com/JamesWuVip/wanli-backend
