# Staging环境部署配置文档

> **重要说明**: 本项目使用PostgreSQL数据库，不是MySQL。所有环境(开发、测试、预发布、生产)都使用PostgreSQL。

## 项目概述

- **项目名称**: wanli-backend
- **Railway项目**: beneficial-beauty
- **环境**: staging
- **服务名称**: wanli-backend-staging
- **部署域名**: https://wanli-backend-staging-staging.up.railway.app

## 技术栈

### 后端框架
- **Spring Boot**: 3.5.0
- **Java版本**: 17
- **构建工具**: Maven 3.11.0

### 核心依赖
- Spring Boot Starter Web
- Spring Boot Starter Data JPA
- Spring Boot Starter Validation
- Spring Boot Starter Security
- PostgreSQL Driver
- JWT (io.jsonwebtoken) 0.12.3
- Lombok 1.18.30
- SpringDoc OpenAPI 2.2.0

### 测试框架
- Spring Boot Starter Test
- Spring Security Test
- JaCoCo Code Coverage (0.8.12)
- Maven Surefire Plugin (3.0.0)
- Maven Failsafe Plugin (3.0.0)

## 环境变量配置

### 数据库配置
```yaml
DATABASE_URL: ${DATABASE_URL}
DATABASE_USERNAME: ${DATABASE_USERNAME:staging_user}
DATABASE_PASSWORD: ${DATABASE_PASSWORD:staging_password}
```

### 应用配置
```yaml
SPRING_PROFILES_ACTIVE: staging
RAILWAY_STATIC_URL: https://wanli-backend-staging-staging.up.railway.app
```

### Railway系统变量
```yaml
RAILWAY_VOLUME_ID: [系统生成]
RAILWAY_VOLUME_MOUNT_PATH: /app/data
RAILWAY_VOLUME_NAME: wanli-data
SSL_CERT_DAYS: 90
```

## 应用配置文件

### application.yml (主配置)
```yaml
server:
  port: 8080
  servlet:
    context-path: /api

spring:
  application:
    name: wanli-backend
  datasource:
    url: jdbc:postgresql://localhost:5432/wanli_backend_dev
    username: dev_user
    password: dev_password
    driver-class-name: org.postgresql.Driver
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5
      idle-timeout: 300000
      connection-timeout: 20000
      leak-detection-threshold: 60000
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        jdbc:
          time_zone: UTC
  profiles:
    active: dev
  jackson:
    date-format: yyyy-MM-dd HH:mm:ss
    time-zone: UTC

jwt:
  secret: mySecretKey
  access-token-expiration: 86400000
  refresh-token-expiration: 604800000

logging:
  level:
    com.wanli: INFO
    org.springframework.security: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
  file:
    name: logs/wanli-backend.log
```

### application-staging.yml (Staging环境配置)
```yaml
spring:
  datasource:
    url: ${DATABASE_URL:jdbc:postgresql://localhost:5432/wanli_backend_staging}
    username: ${DATABASE_USERNAME:staging_user}
    password: ${DATABASE_PASSWORD:staging_password}
    driver-class-name: org.postgresql.Driver
    hikari:
      maximum-pool-size: ${SPRING_DATASOURCE_HIKARI_MAXIMUM_POOL_SIZE:10}
      minimum-idle: ${SPRING_DATASOURCE_HIKARI_MINIMUM_IDLE:5}
      idle-timeout: ${SPRING_DATASOURCE_HIKARI_IDLE_TIMEOUT:600000}
      connection-timeout: ${SPRING_DATASOURCE_HIKARI_CONNECTION_TIMEOUT:30000}
      leak-detection-threshold: ${SPRING_DATASOURCE_HIKARI_LEAK_DETECTION_THRESHOLD:60000}
  jpa:
    hibernate:
      ddl-auto: ${SPRING_JPA_HIBERNATE_DDL_AUTO:update}
    show-sql: ${SPRING_JPA_SHOW_SQL:false}
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        jdbc:
          time_zone: ${SPRING_JPA_PROPERTIES_HIBERNATE_JDBC_TIME_ZONE:UTC}

jwt:
  secret: ${JWT_SECRET}
  access-token-expiration: ${JWT_ACCESS_TOKEN_EXPIRATION:3600000}
  refresh-token-expiration: ${JWT_REFRESH_TOKEN_EXPIRATION:86400000}

logging:
  level:
    com.wanli: ${LOGGING_LEVEL_COM_WANLI:INFO}
    org.springframework.security: ${LOGGING_LEVEL_ORG_SPRINGFRAMEWORK_SECURITY:INFO}
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
  file:
    name: ${LOGGING_FILE_NAME:logs/wanli-backend-staging.log}
```

## 部署流程

### 1. 环境准备
```bash
# 切换到staging环境
railway environment staging

# 选择staging服务
railway service wanli-backend-staging
```

### 2. 部署命令
```bash
# 部署到Railway
railway up
```

### 3. 查看部署状态
```bash
# 查看服务状态
railway status

# 查看部署日志
railway logs

# 查看域名
railway domain
```

## 数据库配置

### 本地开发环境 (Development)
- **数据库类型**: PostgreSQL (不是MySQL)
- **主机**: localhost
- **端口**: 5432
- **数据库名**: wanli_backend_dev
- **用户名**: dev_user
- **密码**: dev_password
- **JDBC URL**: `jdbc:postgresql://localhost:5432/wanli_backend_dev`

### 本地连接命令
```bash
# 连接到本地PostgreSQL开发数据库
psql -h localhost -U dev_user -d wanli_backend_dev
```

### Staging环境 (Railway部署)
- **数据库类型**: PostgreSQL
- **主机**: localhost (本地) / Railway (部署时)
- **端口**: 5432
- **数据库名**: wanli_backend_staging
- **用户名**: staging_user
- **密码**: staging_password
- **JDBC URL**: `jdbc:postgresql://localhost:5432/wanli_backend_staging`

### Railway PostgreSQL服务
- **服务名称**: Postgres-WALT
- **版本**: PostgreSQL 17.6
- **客户端版本**: psql 14.18
- **连接方式**: 通过Railway环境变量DATABASE_URL自动配置

### 连接数据库
```bash
# 连接到Railway PostgreSQL
railway connect Postgres-WALT

# 连接到本地staging数据库
psql -h localhost -U staging_user -d wanli_backend_staging
```

## 应用启动配置

### 服务器配置
- **端口**: 8080
- **上下文路径**: /api
- **启动时间**: 约22秒

### JPA配置
- **DDL模式**: update (自动更新表结构)
- **SQL显示**: false (生产环境关闭)
- **方言**: PostgreSQLDialect
- **时区**: UTC

### 连接池配置 (HikariCP)
- **最大连接数**: 10 (staging环境)
- **最小空闲连接**: 5 (staging环境)
- **空闲超时**: 600秒
- **连接超时**: 30秒
- **泄漏检测阈值**: 60秒

## 安全配置

### JWT配置
- **访问令牌过期时间**: 1小时 (3600000ms)
- **刷新令牌过期时间**: 24小时 (86400000ms)
- **密钥**: 通过环境变量JWT_SECRET配置

### Spring Security
- **认证过滤器**: JwtAuthenticationFilter
- **公开路径**: /api/health (健康检查)
- **日志级别**: INFO

## 监控和日志

### 日志配置
- **应用日志级别**: INFO
- **安全日志级别**: INFO
- **日志文件**: logs/wanli-backend-staging.log
- **日志格式**: 包含时间戳、线程、级别、类名和消息

### 健康检查
- **端点**: /api/health
- **状态**: 可通过域名访问验证服务运行状态

## 部署验证

### 1. 服务状态检查
```bash
# 检查服务是否运行
curl https://wanli-backend-staging-staging.up.railway.app/api/health
```

### 2. 日志监控
```bash
# 实时查看日志
railway logs
```

### 3. 数据库连接测试
```bash
# 连接数据库验证
railway connect Postgres-WALT
```

## 故障排除

### 常见问题
1. **服务未找到**: 确认已选择正确的环境和服务
2. **数据库连接失败**: 检查DATABASE_URL环境变量
3. **启动超时**: 检查依赖和配置文件
4. **内存不足**: 调整Railway服务配置
5. **PostgreSQL连接被拒绝**: 检查PostgreSQL服务是否启动
6. **认证失败**: 确认用户名和密码正确
7. **数据库不存在**: 确认数据库已创建

### PostgreSQL相关问题解决
```bash
# 检查PostgreSQL服务状态
brew services start postgresql

# 创建开发环境数据库和用户
psql -U postgres
CREATE USER dev_user WITH PASSWORD 'dev_password';
CREATE DATABASE wanli_backend_dev OWNER dev_user;
GRANT ALL PRIVILEGES ON DATABASE wanli_backend_dev TO dev_user;

# 创建staging环境数据库和用户
CREATE USER staging_user WITH PASSWORD 'staging_password';
CREATE DATABASE wanli_backend_staging OWNER staging_user;
GRANT ALL PRIVILEGES ON DATABASE wanli_backend_staging TO staging_user;
```

### 调试命令
```bash
# 查看环境变量
railway variables

# 查看服务状态
railway status

# 重新部署
railway up

# 测试本地数据库连接
psql -h localhost -U dev_user -d wanli_backend_dev
psql -h localhost -U staging_user -d wanli_backend_staging
```

## 最佳实践

1. **配置管理**: 使用环境变量管理敏感配置
2. **日志监控**: 定期检查应用日志
3. **健康检查**: 配置自动健康检查端点
4. **数据库备份**: 定期备份PostgreSQL数据
5. **安全更新**: 及时更新依赖版本
6. **性能监控**: 监控连接池和响应时间

## 版本信息

- **文档版本**: 1.1
- **创建日期**: 2025-01-28
- **最后更新**: 2025-01-28
- **适用环境**: Railway Staging Environment
- **数据库更正**: 明确使用PostgreSQL而非MySQL