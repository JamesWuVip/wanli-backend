# Wanli Backend 部署配置完整指南

## 项目概述

- **项目名称**: wanli-backend
- **Railway项目**: beneficial-beauty
- **Git仓库**: JamesWuVip/wanli-backend
- **部署平台**: Railway
- **数据库**: PostgreSQL

## 环境架构

### 分支与环境对应关系
- **dev分支** → 本地开发环境 (本地MySQL)
- **staging分支** → 测试环境 (Railway + PostgreSQL)
- **main分支** → 生产环境 (Railway + PostgreSQL)

### GitFlow规范
1. 开发在dev分支进行
2. 测试部署到staging分支
3. 生产部署到main分支
4. **禁止跳过staging直接合并到main**

---

## Staging环境配置

### 基本信息
- **环境名称**: staging
- **服务名称**: wanli-backend-staging
- **部署域名**: https://wanli-backend-staging-staging.up.railway.app
- **配置文件**: application-staging.yml
- **Profile**: staging

### Railway环境变量配置
```bash
# 数据库配置
SPRING_DATASOURCE_URL=jdbc:postgresql://[host]:[port]/railway
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=[Railway自动生成]

# 应用配置
SPRING_PROFILES_ACTIVE=staging
RAILWAY_STATIC_URL=https://wanli-backend-staging-staging.up.railway.app

# JWT配置
JWT_SECRET=[staging环境密钥]
JWT_ACCESS_TOKEN_EXPIRATION=3600000
JWT_REFRESH_TOKEN_EXPIRATION=86400000

# 日志配置
LOGGING_LEVEL_COM_WANLI=INFO
LOGGING_LEVEL_ORG_SPRINGFRAMEWORK_SECURITY=INFO
LOGGING_FILE_NAME=logs/wanli-backend-staging.log
```

### application-staging.yml配置
```yaml
spring:
  datasource:
    url: ${SPRING_DATASOURCE_URL}
    username: ${SPRING_DATASOURCE_USERNAME}
    password: ${SPRING_DATASOURCE_PASSWORD}
    driver-class-name: org.postgresql.Driver
    hikari:
      maximum-pool-size: ${SPRING_DATASOURCE_HIKARI_MAXIMUM_POOL_SIZE:20}
      minimum-idle: ${SPRING_DATASOURCE_HIKARI_MINIMUM_IDLE:10}
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

### Staging部署流程
```bash
# 1. 切换到staging分支
git checkout staging
git pull origin staging

# 2. 连接Railway staging环境
railway environment staging
railway service wanli-backend-staging

# 3. 部署应用
railway up

# 4. 验证部署
railway status
railway logs
curl https://wanli-backend-staging-staging.up.railway.app/api/health
```

---

## Production环境配置

### 基本信息
- **环境名称**: production
- **服务名称**: wanli-backend
- **部署域名**: [Railway自动生成]
- **配置文件**: application-prod.yml
- **Profile**: production

### Railway环境变量配置
```bash
# 数据库配置
SPRING_DATASOURCE_URL=jdbc:postgresql://gondola.proxy.rlwy.net:55880/railway
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=[Railway自动生成]

# 应用配置
SPRING_PROFILES_ACTIVE=production
PORT=8080
APP_VERSION=1.0.0

# JWT配置
JWT_SECRET=productionSecretKeyForWanliEducationBackendSystem2024
JWT_ACCESS_TOKEN_EXPIRATION=3600000
JWT_REFRESH_TOKEN_EXPIRATION=7200000
```

### application-prod.yml配置
```yaml
# Production环境配置
server:
  port: ${PORT:8080}
  servlet:
    context-path: /api
  error:
    include-message: never
    include-binding-errors: never
    include-stacktrace: never

spring:
  application:
    name: wanli-backend-production
  
  # 数据源配置 - Railway PostgreSQL Production
  datasource:
    url: ${SPRING_DATASOURCE_URL:jdbc:postgresql://localhost:5432/wanli_backend_prod}
    username: ${SPRING_DATASOURCE_USERNAME:prod_user}
    password: ${SPRING_DATASOURCE_PASSWORD:prod_password}
    driver-class-name: org.postgresql.Driver
    hikari:
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
      maximum-pool-size: 30
      minimum-idle: 10
      leak-detection-threshold: 60000
      connection-test-query: SELECT 1
      
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: false
        jdbc:
          time_zone: Asia/Shanghai
          batch_size: 20
        connection:
          provider_disables_autocommit: true
        cache:
          use_second_level_cache: true
          use_query_cache: true
  
  # Jackson配置
  jackson:
    date-format: yyyy-MM-dd HH:mm:ss
    time-zone: Asia/Shanghai
    default-property-inclusion: NON_NULL
    serialization:
      write-dates-as-timestamps: false

# JWT配置
jwt:
  secret: ${JWT_SECRET:productionSecretKeyForWanliEducationBackendSystem2024}
  access-token-expiration: ${JWT_ACCESS_TOKEN_EXPIRATION:3600000}
  refresh-token-expiration: ${JWT_REFRESH_TOKEN_EXPIRATION:7200000}

# 日志配置
logging:
  level:
    com.wanli: info
    org.springframework.web: warn
    org.springframework.security: warn
    org.hibernate.SQL: warn
    org.hibernate.type.descriptor.sql.BasicBinder: warn
    org.springframework.boot.autoconfigure: warn
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
  file:
    name: /tmp/wanli-backend-production.log
    max-size: 100MB
    max-history: 30

# Railway特定配置
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
      base-path: /actuator
  endpoint:
    health:
      show-details: when-authorized
    metrics:
      enabled: true
  metrics:
    export:
      prometheus:
        enabled: true

# 环境标识
app:
  environment: production
  version: ${APP_VERSION:1.0.0}
  railway:
    deployment: true

# 安全配置
security:
  require-ssl: false
  headers:
    frame-options: DENY
    content-type-options: nosniff
    xss-protection: 1; mode=block
```

### Production部署流程
```bash
# 1. 切换到main分支
git checkout main
git pull origin main

# 2. 连接Railway production环境
railway environment production
railway service wanli-backend

# 3. 部署应用
railway up

# 4. 验证部署
railway status
railway logs
```

---

## 数据库配置

### PostgreSQL服务信息
- **服务名称**: Postgres (在Railway中)
- **版本**: PostgreSQL 17.6
- **客户端版本**: psql 14.18
- **连接方式**: 通过Railway环境变量DATABASE_URL自动配置

### 数据库连接池配置对比

| 配置项 | Staging | Production |
|--------|---------|------------|
| 最大连接数 | 20 | 30 |
| 最小空闲连接 | 10 | 10 |
| 连接超时 | 30秒 | 30秒 |
| 空闲超时 | 600秒 | 600秒 |
| 最大生命周期 | - | 1800秒 |
| 泄漏检测阈值 | 60秒 | 60秒 |

### 数据库操作命令
```bash
# 连接到PostgreSQL (staging)
railway environment staging
railway connect Postgres

# 连接到PostgreSQL (production)
railway environment production
railway connect Postgres
```

---

## 环境变量管理

### 查看环境变量
```bash
# 查看所有环境变量
railway variables

# 查看特定变量
railway variables | grep SPRING_DATASOURCE_URL
```

### 设置环境变量
```bash
# 设置单个变量
railway variables --set KEY=VALUE

# 设置数据库URL (重要：必须是完整的JDBC URL)
railway variables --set SPRING_DATASOURCE_URL=jdbc:postgresql://host:port/database
```

### 常见环境变量问题
1. **数据库URL格式错误**：必须使用 `jdbc:postgresql://` 前缀
2. **变量值截断**：Railway可能显示截断的值，但实际值是完整的
3. **变量更新后需要重新部署**：使用 `railway redeploy` 或 `railway up`

---

## 部署验证清单

### Staging环境验证
- [ ] 应用成功启动 (`railway logs`)
- [ ] 数据库连接正常
- [ ] Profile为staging
- [ ] 健康检查端点可访问 (`/api/health`)
- [ ] JWT配置正确
- [ ] 日志输出正常

### Production环境验证
- [ ] 应用成功启动 (`railway logs`)
- [ ] 数据库连接正常
- [ ] Profile为production
- [ ] 健康检查端点可访问 (`/api/actuator/health`)
- [ ] JWT配置正确
- [ ] 监控端点可访问 (`/api/actuator/metrics`)
- [ ] 安全配置生效
- [ ] 日志文件正常写入

---

## 故障排除指南

### 常见问题及解决方案

#### 1. 数据库连接失败
**症状**: `Driver org.postgresql.Driver claims to not accept jdbcUrl`
**原因**: 数据库URL格式错误
**解决方案**:
```bash
# 检查当前URL
railway variables | grep SPRING_DATASOURCE_URL

# 修正URL格式
railway variables --set SPRING_DATASOURCE_URL=jdbc:postgresql://host:port/database

# 重新部署
railway redeploy
```

#### 2. 应用启动失败
**症状**: 应用无法启动或启动超时
**排查步骤**:
```bash
# 查看详细日志
railway logs

# 检查环境变量
railway variables

# 检查服务状态
railway status
```

#### 3. Profile未正确激活
**症状**: 使用了错误的配置文件
**解决方案**:
```bash
# 检查SPRING_PROFILES_ACTIVE变量
railway variables | grep SPRING_PROFILES_ACTIVE

# 设置正确的profile
railway variables --set SPRING_PROFILES_ACTIVE=staging  # 或 production
```

#### 4. 部署无响应
**症状**: `railway up` 或 `railway redeploy` 无响应
**解决方案**:
```bash
# 停止当前部署
Ctrl+C

# 重新选择服务
railway service

# 重新部署
railway up
```

### 调试命令集合
```bash
# 基本信息
railway whoami
railway status
railway service
railway environment

# 日志和监控
railway logs
railway logs --tail 50

# 环境变量
railway variables
railway variables --set KEY=VALUE

# 部署操作
railway up
railway redeploy

# 数据库连接
railway connect Postgres
```

---

## 最佳实践

### 1. 配置管理
- 使用环境变量管理敏感配置
- 为不同环境设置不同的JWT密钥
- 生产环境关闭SQL日志和调试信息
- 使用合适的连接池配置

### 2. 部署流程
- 严格遵循GitFlow规范
- 先在staging环境测试，再部署到production
- 部署前检查配置文件和环境变量
- 部署后进行完整的功能验证

### 3. 监控和维护
- 定期检查应用日志
- 监控数据库连接池状态
- 设置健康检查和监控告警
- 定期备份数据库

### 4. 安全考虑
- 生产环境使用强JWT密钥
- 关闭不必要的错误信息暴露
- 启用安全头配置
- 定期更新依赖版本

### 5. 性能优化
- 合理配置连接池参数
- 启用JPA二级缓存（生产环境）
- 使用批处理优化数据库操作
- 监控应用性能指标

---

## 版本信息

- **文档版本**: 2.0
- **创建日期**: 2025-01-28
- **最后更新**: 2025-01-28
- **适用版本**: Spring Boot 3.5.0, Java 17
- **部署平台**: Railway
- **数据库**: PostgreSQL 17.6

---

## 附录

### A. 完整的application.yml配置
```yaml
server:
  port: 8080
  servlet:
    context-path: /api

spring:
  application:
    name: wanli-backend
  datasource:
    url: jdbc:postgresql://localhost:5432/wanli_db
    username: postgres
    password: password
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
    active: ${SPRING_PROFILES_ACTIVE:dev}
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

### B. Railway CLI常用命令
```bash
# 登录和项目管理
railway login
railway logout
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
railway logs --tail [number]

# 环境变量管理
railway variables
railway variables --set KEY=VALUE
railway variables --remove KEY

# 数据库连接
railway connect [service-name]

# 域名管理
railway domain
railway domain add [domain]
```

### C. 技术栈版本信息
- **Spring Boot**: 3.5.0
- **Java**: 17
- **Maven**: 3.11.0
- **PostgreSQL**: 17.6
- **JWT**: 0.12.3
- **Lombok**: 1.18.30
- **SpringDoc OpenAPI**: 2.2.0
- **JaCoCo**: 0.8.12
