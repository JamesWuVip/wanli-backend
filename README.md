# 万里后端系统

基于Spring Boot的后端API系统，提供用户认证和管理功能。

## 功能特性

- 用户注册和登录
- JWT身份认证
- 用户信息管理
- 系统健康检查
- 完整的API文档

## 技术栈

- Spring Boot 3.x
- Spring Security
- JWT
- MySQL
- Maven

## 快速开始

1. 克隆项目
```bash
git clone https://github.com/JamesWuVip/wanli-backend.git
cd wanli-backend
```

2. 配置数据库
- 创建MySQL数据库
- 修改 `application.yml` 中的数据库配置

3. 运行项目
```bash
mvn spring-boot:run
```

4. 访问API
- 基础URL: http://localhost:8080
- API文档: 查看 `api_documentation.md`

## API接口

### 用户认证
- POST `/api/auth/register` - 用户注册
- POST `/api/auth/login` - 用户登录
- GET `/api/auth/me` - 获取当前用户信息
- POST `/api/auth/logout` - 用户登出

### 系统接口
- GET `/api/health` - 健康检查

## 开发规范

- 遵循GitFlow工作流
- 代码提交到dev分支
- 通过测试后合并到main分支
- 完整的接口测试和文档

## 许可证

MIT License