# GitHub Secrets 配置指南

本文档说明了CI/CD流水线所需的GitHub Secrets配置。

## 必需的Secrets

### 1. 代码质量和监控

#### CODECOV_TOKEN
- **用途**: 上传代码覆盖率报告到Codecov
- **获取方式**: 
  1. 访问 [Codecov.io](https://codecov.io/)
  2. 使用GitHub账号登录
  3. 添加你的仓库
  4. 复制Repository Upload Token
- **设置路径**: Settings → Secrets and variables → Actions → New repository secret

#### SENTRY_DSN
- **用途**: 错误监控和性能追踪
- **获取方式**:
  1. 访问 [Sentry.io](https://sentry.io/)
  2. 创建新项目或使用现有项目
  3. 在项目设置中找到DSN
- **格式**: `https://[key]@[organization].ingest.sentry.io/[project-id]`

#### SONAR_TOKEN
- **用途**: SonarCloud代码质量分析
- **获取方式**:
  1. 访问 [SonarCloud.io](https://sonarcloud.io/)
  2. 使用GitHub账号登录
  3. 创建新组织和项目
  4. 生成Token: My Account → Security → Generate Tokens

### 2. 部署相关

#### RAILWAY_TOKEN
- **用途**: 部署到Railway平台
- **获取方式**:
  1. 访问 [Railway.app](https://railway.app/)
  2. 登录并创建项目
  3. 在项目设置中生成Deploy Token

#### RAILWAY_PROJECT_ID
- **用途**: 指定Railway项目ID
- **获取方式**: 在Railway项目URL中找到项目ID

### 3. 数据库和缓存（生产环境）

#### DB_HOST
- **用途**: 生产环境数据库主机地址
- **示例**: `mysql.railway.internal`

#### DB_PORT
- **用途**: 数据库端口
- **默认值**: `3306`

#### DB_NAME
- **用途**: 数据库名称
- **示例**: `wanli_prod`

#### DB_USERNAME
- **用途**: 数据库用户名

#### DB_PASSWORD
- **用途**: 数据库密码

#### REDIS_HOST
- **用途**: Redis主机地址
- **示例**: `redis.railway.internal`

#### REDIS_PORT
- **用途**: Redis端口
- **默认值**: `6379`

#### REDIS_PASSWORD
- **用途**: Redis密码（如果需要）

### 4. 应用配置

#### JWT_SECRET
- **用途**: JWT令牌签名密钥
- **要求**: 至少32个字符的随机字符串
- **生成方式**: 使用在线生成器或命令 `openssl rand -base64 32`

## 配置步骤

1. **访问仓库设置**
   - 进入GitHub仓库
   - 点击 Settings 标签
   - 选择 Secrets and variables → Actions

2. **添加Secret**
   - 点击 "New repository secret"
   - 输入Secret名称（必须与上述名称完全匹配）
   - 输入Secret值
   - 点击 "Add secret"

3. **验证配置**
   - 所有必需的Secrets都应该在列表中显示
   - 确保名称拼写正确（区分大小写）

## 环境特定配置

### 开发环境 (dev)
- 使用本地或开发数据库
- 可以使用测试用的Sentry DSN

### 测试环境 (staging)
- 使用独立的测试数据库
- 使用生产级别的监控配置

### 生产环境 (main)
- 使用生产数据库和Redis
- 使用生产级别的所有配置

## 安全注意事项

1. **不要在代码中硬编码敏感信息**
2. **定期轮换密钥和令牌**
3. **使用最小权限原则**
4. **监控Secret的使用情况**
5. **不要在日志中输出敏感信息**

## 故障排除

### 常见问题

1. **CI/CD失败，提示缺少Secret**
   - 检查Secret名称是否正确
   - 确认Secret已正确添加到仓库

2. **Codecov上传失败**
   - 验证CODECOV_TOKEN是否有效
   - 检查仓库是否已添加到Codecov

3. **Sentry错误报告不工作**
   - 验证SENTRY_DSN格式是否正确
   - 检查Sentry项目配置

4. **部署失败**
   - 验证Railway相关配置
   - 检查数据库连接信息

### 测试配置

可以通过以下方式测试配置：

1. **推送代码到dev分支**触发CI/CD流水线
2. **查看Actions日志**确认各个步骤是否成功
3. **检查外部服务**（Codecov、Sentry等）确认数据是否正确上传

---

**注意**: 本文档包含敏感信息配置指南，请确保只有授权人员可以访问相关的Secret值。