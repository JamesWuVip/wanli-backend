# 部署配置说明

## 环境隔离架构

本项目支持多环境部署，包括生产环境(production)和测试环境(staging)的完全隔离。

### 分支策略

- **main分支**: 生产环境代码，自动部署到生产服务
- **staging分支**: 测试环境代码，自动部署到测试服务

### Railway平台配置

#### 1. 创建两个独立的Railway服务

**生产环境服务:**
- 服务名称: `wanli-backend-production`
- 连接分支: `main`
- 环境变量:
  ```
  SPRING_PROFILES_ACTIVE=production
  DATABASE_URL=<生产数据库URL>
  DB_USERNAME=<生产数据库用户名>
  DB_PASSWORD=<生产数据库密码>
  CORS_ALLOWED_ORIGINS=https://wanli.ai
  ```

**测试环境服务:**
- 服务名称: `wanli-backend-staging`
- 连接分支: `staging`
- 环境变量:
  ```
  SPRING_PROFILES_ACTIVE=staging
  DATABASE_URL=<测试数据库URL>
  DB_USERNAME=<测试数据库用户名>
  DB_PASSWORD=<测试数据库密码>
  DB_SCHEMA=staging
  CORS_ALLOWED_ORIGINS=http://localhost:3000,https://staging.wanli.ai
  ```

#### 2. GitHub Secrets配置

在GitHub仓库的Settings > Secrets and variables > Actions中添加:

```
RAILWAY_TOKEN=<Railway API Token>
RAILWAY_PRODUCTION_SERVICE_ID=<生产环境服务ID>
RAILWAY_STAGING_SERVICE_ID=<测试环境服务ID>
```

#### 3. 自定义域名配置(可选)

**生产环境:**
- 域名: `api.wanli.ai`
- 在Railway服务设置中添加自定义域名
- 配置DNS CNAME记录指向Railway提供的域名

**测试环境:**
- 域名: `api-staging.wanli.ai`
- 在Railway服务设置中添加自定义域名
- 配置DNS CNAME记录指向Railway提供的域名

### CI/CD工作流程

1. **推送到main分支**:
   - 触发GitHub Actions
   - 自动部署到生产环境Railway服务
   - 使用生产环境配置和数据库

2. **推送到staging分支**:
   - 触发GitHub Actions
   - 自动部署到测试环境Railway服务
   - 使用测试环境配置和数据库

### 环境验证

**生产环境健康检查:**
```bash
curl https://api.wanli.ai/api/health
# 或当前临时地址
curl https://wanli-backend-production.up.railway.app/api/health
```

**测试环境健康检查:**
```bash
curl https://api-staging.wanli.ai/api/health
# 或当前临时地址
curl https://wanli-backend-staging.up.railway.app/api/health
```

### 数据库隔离

- 生产环境和测试环境使用完全独立的数据库实例
- 测试环境使用独立的schema: `staging`
- 确保数据完全隔离，避免测试数据污染生产环境

### 注意事项

1. **环境变量管理**: 确保生产和测试环境的敏感信息完全分离
2. **数据库备份**: 定期备份生产环境数据库
3. **监控告警**: 为生产环境配置监控和告警
4. **访问控制**: 限制生产环境的访问权限
5. **日志管理**: 区分不同环境的日志输出

### 故障排除

如果部署失败，检查以下项目:
1. GitHub Secrets是否正确配置
2. Railway服务ID是否匹配
3. 环境变量是否完整
4. 数据库连接是否正常
5. Railway服务是否有足够的资源配额