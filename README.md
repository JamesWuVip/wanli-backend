# 万里加盟连锁门店作业管理系统 - 后端

## 项目概述

这是一个基于Spring Boot的后端系统，为万里加盟连锁门店作业管理系统提供API服务。

## 最新更新

### 2025-09-10
- 修复管理员登录问题
- 添加数据库连接测试脚本
- 完善E2E测试套件
- 验证登录API功能正常

## 技术栈

- Spring Boot 3.x
- Java 17
- Supabase (PostgreSQL)
- JWT认证
- bcrypt密码加密

## 快速开始

### 环境要求

- Java 17+
- Node.js 18+ (用于测试脚本)
- Supabase账户

### 配置

1. 复制环境配置文件：
```bash
cp .env.example .env
```

2. 配置Supabase连接信息：
```
SUPABASE_URL=your_supabase_url
SUPABASE_SERVICE_ROLE_KEY=your_service_role_key
```

### 运行

```bash
# 开发环境
npm run dev:backend

# 生产环境
npm run build
npm start
```

### 测试

```bash
# 运行E2E测试
npx playwright test

# 测试数据库连接
node test-db.js

# 更新管理员密码
node update-admin-password.js
```

## API文档

### 认证接口

- `POST /api/auth/login` - 用户登录
- `POST /api/auth/logout` - 用户登出
- `POST /api/auth/refresh` - 刷新token

### 用户管理

- `GET /api/users` - 获取用户列表
- `POST /api/users` - 创建用户
- `PUT /api/users/:id` - 更新用户
- `DELETE /api/users/:id` - 删除用户

## 默认账户

- 用户名：admin
- 密码：admin123

## 开发规范

请参考项目根目录下的开发规范文档。

## 许可证

MIT License
