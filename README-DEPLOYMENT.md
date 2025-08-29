# 万里教育后端系统 - 一键部署方案

## 概述

本文档提供了万里教育后端系统的一键部署方案，支持 `staging` 和 `production` 环境的自动化部署。

## 部署脚本

### 1. Staging 环境部署

```bash
# 执行 staging 环境部署
./scripts/deploy-staging.sh
```

**功能说明：**
- 自动切换到 `staging` 分支
- 合并 `dev` 分支的最新代码
- 推送到远程仓库触发 Railway 自动部署
- 提供部署状态检查提示

### 2. Production 环境部署

```bash
# 执行生产环境部署
./scripts/deploy-production.sh
```

**功能说明：**
- 需要用户确认才能继续部署
- 自动切换到 `main` 分支
- 合并 `staging` 分支的测试通过代码
- 推送到远程仓库触发 Railway 自动部署
- 自动创建版本标签

### 3. 数据库迁移验证

```bash
# 验证数据库迁移状态
export DATABASE_URL="postgresql://username:password@host:port/database"
./scripts/verify-db-migration.sh
```

**功能说明：**
- 检查数据库表结构
- 验证 `franchises` 表删除状态
- 确认数据完整性
- 检查外键约束

## 部署流程

### Staging 环境部署流程

1. **开发完成** → `dev` 分支
2. **执行部署脚本** → `./scripts/deploy-staging.sh`
3. **自动合并** → `dev` → `staging`
4. **自动部署** → Railway Staging 环境
5. **验证测试** → 功能测试、接口测试

### Production 环境部署流程

1. **Staging 测试通过** → `staging` 分支
2. **执行部署脚本** → `./scripts/deploy-production.sh`
3. **用户确认** → 手动确认部署
4. **自动合并** → `staging` → `main`
5. **自动部署** → Railway Production 环境
6. **版本标签** → 自动创建版本标签

## 环境配置

### Railway 环境变量

#### Staging 环境
```
SPRING_PROFILES_ACTIVE=staging
DATABASE_URL=postgresql://...
JWT_SECRET=staging_secret
```

#### Production 环境
```
SPRING_PROFILES_ACTIVE=production
DATABASE_URL=postgresql://...
JWT_SECRET=production_secret
```

### 配置文件管理

- `application-dev.yml` → 开发环境配置
- `application-staging.yml` → 测试环境配置
- `application-production.yml` → 生产环境配置

## 使用说明

### 首次使用

1. **赋予脚本执行权限**
```bash
chmod +x scripts/deploy-staging.sh
chmod +x scripts/deploy-production.sh
chmod +x scripts/verify-db-migration.sh
```

2. **配置环境变量**
```bash
# 设置数据库连接
export DATABASE_URL="postgresql://username:password@host:port/database"
```

### 日常部署

#### 部署到 Staging
```bash
# 确保在项目根目录
cd /path/to/wanli-backend

# 执行部署
./scripts/deploy-staging.sh
```

#### 部署到 Production
```bash
# 确保 staging 环境测试通过
./scripts/verify-db-migration.sh

# 执行生产部署
./scripts/deploy-production.sh
```

## 回滚策略

### Staging 环境回滚
```bash
# 回滚到上一个版本
git checkout staging
git reset --hard HEAD~1
git push origin staging --force
```

### Production 环境回滚
```bash
# 查看版本标签
git tag -l

# 回滚到指定版本
git checkout main
git reset --hard v2024.01.15-1430
git push origin main --force
```

## 监控和日志

### Railway 部署日志
- 访问 Railway 控制台查看部署日志
- 监控应用启动状态
- 检查数据库连接状态

### 应用日志
```bash
# 查看应用日志
railway logs --tail
```

## 故障排查

### 常见问题

1. **部署失败**
   - 检查 Railway 控制台错误信息
   - 验证环境变量配置
   - 确认数据库连接

2. **数据库迁移失败**
   - 运行 `verify-db-migration.sh` 检查状态
   - 手动执行迁移脚本
   - 检查数据库权限

3. **分支合并冲突**
   - 手动解决冲突
   - 重新执行部署脚本

### 紧急联系

- 开发团队：[联系方式]
- 运维团队：[联系方式]

## 版本历史

- v1.0.0 - 初始版本，支持基本部署功能
- v1.1.0 - 添加数据库迁移验证
- v1.2.0 - 完善回滚策略和监控

---

**注意：请严格按照 GitFlow 规范执行部署，禁止跳过 staging 环境直接部署到 production。**