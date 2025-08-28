# Wanli Backend 部署配置指南

## 项目概述

- **项目名称**: wanli-backend
- **部署平台**: Railway
- **数据库**: PostgreSQL
- **框架**: Spring Boot 3.5.0
- **Java版本**: 17

## 环境架构

### 分支与环境对应关系
- **dev分支** → 本地开发环境 (本地MySQL)
- **staging分支** → 测试环境 (Railway + PostgreSQL)
- **main分支** → 生产环境 (Railway + PostgreSQL)

### GitFlow规范
1. 开发在dev分支进行
2. 测试部署到staging分支
3. 生产环境部署到main分支
4. **禁止跳过staging直接合并到main**

---

## 配置文件结构

### 环境配置文件
- `application.yml` - 基础配置
- `application-staging.yml` - 测试环境配置
- `application-prod.yml` - 生产环境配置

### 配置说明
所有敏感信息（数据库连接、JWT密钥等）通过环境变量管理，不在代码中硬编码。

---

## 部署流程

### Staging环境部署
```bash
# 1. 切换到staging分支
git checkout staging
git pull origin staging

# 2. 连接Railway staging环境
railway environment staging
railway service [staging-service-name]

# 3. 部署应用
railway up

# 4. 验证部署
railway status
railway logs
```

### Production环境部署
```bash
# 1. 切换到main分支
git checkout main
git pull origin main

# 2. 连接Railway production环境
railway environment production
railway service [production-service-name]

# 3. 部署应用
railway up

# 4. 验证部署
railway status
railway logs
```

---

## 环境变量配置

### 必需的环境变量
```bash
# 数据库配置
SPRING_DATASOURCE_URL=jdbc:postgresql://[host]:[port]/[database]
SPRING_DATASOURCE_USERNAME=[username]
SPRING_DATASOURCE_PASSWORD=[password]

# 应用配置
SPRING_PROFILES_ACTIVE=[staging|production]
PORT=8080

# JWT配置
JWT_SECRET=[your-jwt-secret]
JWT_ACCESS_TOKEN_EXPIRATION=3600000
JWT_REFRESH_TOKEN_EXPIRATION=7200000
```

### 环境变量管理
```bash
# 查看环境变量
railway variables

# 设置环境变量
railway variables --set KEY=VALUE

# 删除环境变量
railway variables --remove KEY
```

---

## 部署验证清单

### Staging环境验证
- [ ] 应用成功启动
- [ ] 数据库连接正常
- [ ] Profile为staging
- [ ] 健康检查端点可访问
- [ ] JWT配置正确
- [ ] 日志输出正常

### Production环境验证
- [ ] 应用成功启动
- [ ] 数据库连接正常
- [ ] Profile为production
- [ ] 健康检查端点可访问
- [ ] JWT配置正确
- [ ] 监控端点可访问
- [ ] 安全配置生效

---

## 常见问题排查

### 数据库连接失败
1. 检查数据库URL格式是否正确（必须使用 `jdbc:postgresql://` 前缀）
2. 验证数据库凭据
3. 确认网络连接

### 应用启动失败
1. 查看详细日志：`railway logs`
2. 检查环境变量：`railway variables`
3. 验证配置文件语法

### Profile未正确激活
1. 检查 `SPRING_PROFILES_ACTIVE` 环境变量
2. 确认配置文件存在
3. 验证配置文件命名规范

---

## Railway CLI常用命令

```bash
# 项目管理
railway login
railway projects
railway link [project-id]

# 环境和服务管理
railway environments
railway environment [environment-name]
railway services
railway service [service-name]

# 部署和监控
railway up
railway redeploy
railway status
railway logs

# 环境变量管理
railway variables
railway variables --set KEY=VALUE
railway variables --remove KEY
```

---

## 最佳实践

### 安全配置
- 使用强JWT密钥
- 通过环境变量管理敏感信息
- 生产环境关闭调试信息
- 定期更新依赖版本

### 部署流程
- 严格遵循GitFlow规范
- 先在staging环境测试
- 部署前检查配置
- 部署后进行验证

### 监控维护
- 定期检查应用日志
- 监控数据库连接状态
- 设置健康检查
- 定期备份数据库

---

## 技术栈版本

- **Spring Boot**: 3.5.0
- **Java**: 17
- **Maven**: 3.11.0
- **PostgreSQL**: 17.6
- **JWT**: 0.12.3
- **Lombok**: 1.18.30
- **SpringDoc OpenAPI**: 2.2.0

---

## 注意事项

⚠️ **安全提醒**：
- 本文档不包含具体的敏感配置信息
- 详细的配置信息请参考项目内部文档
- 生产环境配置请联系项目管理员获取

📝 **文档版本**: 1.0  
📅 **最后更新**: 2025-01-28
