# 万里后端系统部署配置指南

## 概述

本文档详细说明了万里后端系统在不同环境下的部署配置，包括本地开发环境、Railway Staging环境和Railway Production环境的完整配置方案。

## 环境概览

| 环境 | 部署平台 | 数据库 | 配置文件 | 访问地址 |
|------|----------|--------|----------|----------|
| 开发环境 | 本地 | PostgreSQL (本地) | application-dev.yml | http://localhost:8080 |
| 测试环境 | Railway | PostgreSQL (Railway) | application-staging.yml | https://wanli-backend-staging.railway.app |
| 生产环境 | Railway | PostgreSQL (Railway) | application-prod.yml | https://wanli-backend.railway.app |

## 1. 本地开发环境配置

### 1.1 数据库配置

**PostgreSQL 配置**:
- **版本**: PostgreSQL 15+
- **主机**: localhost
- **端口**: 5432
- **数据库名**: wanli_backend_dev
- **用户名**: dev_user
- **密码**: dev_password

### 1.2 application-dev.yml

```yaml
spring:
  application:
    name: wanli-backend
  profiles:
    active: dev
  
  datasource:
    url: jdbc:postgresql://localhost:5432/wanli_backend_dev
    username: ${DB_USERNAME:dev_user}
    password: ${DB_PASSWORD:dev_password}
    driver-class-name: org.postgresql.Driver
  
  jpa:
    database-platform: org.hibernate.dialect.PostgreSQLDialect
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        format_sql: true
        use_sql_comments: true
  
  jackson:
    time-zone: Asia/Shanghai
    date-format: yyyy-MM-dd HH:mm:ss

server:
  port: 8080

logging:
  level:
    com.wanli: DEBUG
    org.springframework.security: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"

# JWT配置
jwt:
  secret: ${JWT_SECRET:wanli-backend-dev-secret-key-2024}
  expiration: 86400000  # 24小时
```

## 2. Railway Staging环境配置

### 2.1 Railway服务信息

**PostgreSQL服务**:
- **版本**: PostgreSQL 15
- **主机**: Railway自动分配
- **端口**: Railway自动分配
- **数据库名**: railway
- **连接**: 使用Railway提供的DATABASE_URL

### 2.2 application-staging.yml

```yaml
spring:
  application:
    name: wanli-backend
  profiles:
    active: staging
  
  datasource:
    url: ${DATABASE_URL}
    driver-class-name: org.postgresql.Driver
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
  
  jpa:
    database-platform: org.hibernate.dialect.PostgreSQLDialect
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        format_sql: false
  
  jackson:
    time-zone: Asia/Shanghai
    date-format: yyyy-MM-dd HH:mm:ss

server:
  port: ${PORT:8080}

logging:
  level:
    com.wanli: INFO
    root: INFO
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"

# JWT配置
jwt:
  secret: ${JWT_SECRET}
  expiration: 86400000  # 24小时
```

### 2.3 Railway环境变量 (Staging)

```bash
# 应用配置
SPRING_PROFILES_ACTIVE=staging
PORT=${PORT:8080}
APP_VERSION=1.0.0-staging

# JWT配置
JWT_SECRET=wanli-backend-staging-jwt-secret-2024

# 数据库配置 (Railway自动提供)
DATABASE_URL=postgresql://postgres:password@host:port/railway
DATABASE_PRIVATE_URL=postgresql://postgres:password@private-host:port/railway

# 日志配置
LOGGING_LEVEL_ROOT=INFO
LOGGING_LEVEL_COM_WANLI=INFO
```

## 3. Railway Production环境配置

### 3.1 application-prod.yml

```yaml
spring:
  application:
    name: wanli-backend
  profiles:
    active: production
  
  datasource:
    url: ${DATABASE_URL}
    driver-class-name: org.postgresql.Driver
    hikari:
      maximum-pool-size: 30
      minimum-idle: 10
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
      leak-detection-threshold: 60000
  
  jpa:
    database-platform: org.hibernate.dialect.PostgreSQLDialect
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        format_sql: false
  
  jackson:
    time-zone: Asia/Shanghai
    date-format: yyyy-MM-dd HH:mm:ss

server:
  port: ${PORT:8080}

logging:
  level:
    com.wanli: WARN
    root: WARN
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"

# JWT配置
jwt:
  secret: ${JWT_SECRET}
  expiration: 86400000  # 24小时
```

### 3.2 Railway环境变量 (Production)

```bash
# 应用配置
SPRING_PROFILES_ACTIVE=production
PORT=${PORT:8080}
APP_VERSION=1.0.0

# JWT配置
JWT_SECRET=wanli-backend-production-jwt-secret-2024-ultra-secure

# 数据库配置 (Railway自动提供)
DATABASE_URL=postgresql://postgres:password@host:port/railway
DATABASE_PRIVATE_URL=postgresql://postgres:password@private-host:port/railway

# 日志配置
LOGGING_LEVEL_ROOT=WARN
LOGGING_LEVEL_COM_WANLI=WARN
```

## 4. 部署流程

### 4.1 开发部署流程

1. **本地环境准备**:
   ```bash
   # 启动PostgreSQL
   brew services start postgresql
   
   # 创建开发数据库
   createdb wanli_backend_dev
   
   # 运行应用
   mvn spring-boot:run -Dspring-boot.run.profiles=dev
   ```

2. **验证部署**:
   ```bash
   curl http://localhost:8080/api/health
   ```

### 4.2 Staging部署流程

1. **推送到staging分支**:
   ```bash
   git checkout staging
   git merge dev
   git push origin staging
   ```

2. **Railway自动部署**:
   - Railway检测到staging分支更新
   - 自动触发构建和部署
   - 使用application-staging.yml配置

3. **验证部署**:
   ```bash
   curl https://wanli-backend-staging.railway.app/api/health
   ```

### 4.3 生产部署流程

1. **合并到main分支**:
   ```bash
   git checkout main
   git merge staging
   git push origin main
   ```

2. **Railway自动部署**:
   - Railway检测到main分支更新
   - 自动触发生产环境构建和部署
   - 使用application-prod.yml配置

3. **验证部署**:
   ```bash
   curl https://wanli-backend.railway.app/api/health
   ```

## 5. 数据库配置对比

| 配置项 | 开发环境 | Staging环境 | 生产环境 |
|--------|----------|-------------|----------|
| 数据库类型 | PostgreSQL | PostgreSQL | PostgreSQL |
| 版本 | 15+ | 15 | 15 |
| 连接池大小 | 10 | 20 | 30 |
| 最小空闲连接 | 5 | 5 | 10 |
| DDL模式 | update | validate | validate |
| SQL日志 | true | false | false |
| 连接超时 | 30s | 30s | 30s |

## 6. 数据库操作命令

### 6.1 本地PostgreSQL操作

```bash
# 连接数据库
psql -h localhost -U dev_user -d wanli_backend_dev

# 查看表结构
\dt
\d users

# 备份数据库
pg_dump -h localhost -U dev_user wanli_backend_dev > backup.sql

# 恢复数据库
psql -h localhost -U dev_user wanli_backend_dev < backup.sql
```

### 6.2 Railway PostgreSQL操作

```bash
# 连接Railway数据库
railway connect Postgres

# 或使用psql直接连接
psql $DATABASE_URL

# 查看数据库信息
\l
\dt

# 执行SQL文件
psql $DATABASE_URL -f migration.sql
```

## 7. 环境变量管理

### 7.1 查看环境变量

```bash
# Railway CLI查看
railway variables

# 查看特定环境
railway environment staging
railway variables
```

### 7.2 设置环境变量

```bash
# 设置staging环境变量
railway environment staging
railway variables set JWT_SECRET="your-staging-secret"

# 设置production环境变量
railway environment production
railway variables set JWT_SECRET="your-production-secret"
```

### 7.3 常见环境变量问题

1. **JWT_SECRET未设置**:
   ```bash
   railway variables set JWT_SECRET="your-secure-secret-key"
   ```

2. **DATABASE_URL格式错误**:
   - Railway会自动提供正确的DATABASE_URL
   - 不要手动修改此变量

3. **PORT变量冲突**:
   - Railway会自动设置PORT变量
   - 应用应使用${PORT:8080}作为默认值

## 8. 部署验证清单

### 8.1 Staging环境验证

- [ ] 应用启动成功
- [ ] 数据库连接正常
- [ ] 健康检查接口返回正常
- [ ] JWT认证功能正常
- [ ] 用户注册/登录功能正常
- [ ] 日志输出正常
- [ ] 环境变量配置正确

### 8.2 Production环境验证

- [ ] 应用启动成功
- [ ] 数据库连接正常
- [ ] 健康检查接口返回正常
- [ ] JWT认证功能正常
- [ ] 所有API接口正常
- [ ] 性能指标正常
- [ ] 安全配置正确
- [ ] 监控告警正常

## 9. 故障排除

### 9.1 数据库连接失败

**症状**: 应用启动时数据库连接超时

**解决方案**:
1. 检查DATABASE_URL格式
2. 验证数据库服务状态
3. 检查网络连接
4. 查看Railway服务日志

### 9.2 PostgreSQL连接被拒绝

**症状**: `Connection refused` 错误

**解决方案**:
1. 确认PostgreSQL服务运行状态
2. 检查端口配置
3. 验证防火墙设置
4. 检查连接池配置

### 9.3 认证失败

**症状**: `authentication failed` 错误

**解决方案**:
1. 验证用户名密码
2. 检查数据库用户权限
3. 确认连接字符串格式
4. 查看数据库日志

### 9.4 数据库不存在

**症状**: `database does not exist` 错误

**解决方案**:
1. 创建对应的数据库
2. 检查数据库名称拼写
3. 验证用户访问权限
4. 运行数据库初始化脚本

## 10. 监控和维护

### 10.1 应用监控

- **健康检查**: `/api/health`
- **应用指标**: Railway Dashboard
- **日志监控**: Railway Logs
- **性能监控**: 响应时间、内存使用

### 10.2 数据库监控

- **连接数**: 监控活跃连接数
- **查询性能**: 慢查询日志
- **存储空间**: 数据库大小监控
- **备份状态**: 定期备份验证

### 10.3 定期维护任务

- [ ] 每周检查应用日志
- [ ] 每月更新依赖包
- [ ] 每季度性能优化
- [ ] 每半年安全审计

---

**文档版本**: v1.2.0  
**最后更新**: 2024-01-28  
**维护者**: 万里后端开发团队