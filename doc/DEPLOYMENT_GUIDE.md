# 万里后端项目部署指南

## 1. 项目概述

万里后端项目是一个基于 Spring Boot 框架的现代化 Web 应用程序，采用微服务架构设计。本指南将帮助您在免费的云服务平台上部署和运行该项目。

### 1.1 技术栈
- **后端框架**: Spring Boot + Java 17
- **数据库**: MySQL (主数据库) + Redis (缓存)
- **ORM**: Spring Data JPA + Hibernate
- **认证**: Spring Security + JWT
- **构建工具**: Maven
- **部署平台**: Railway (主要) + Heroku (备选)
- **版本控制**: GitHub
- **CI/CD**: GitHub Actions
- **监控**: UptimeRobot + Sentry
- **代码质量**: Checkstyle + SpotBugs + PMD

### 1.2 分支管理策略
- **main**: 生产环境分支
- **staging**: 测试环境分支  
- **dev**: 开发环境分支
- **feature/***: 功能开发分支

## 2. 免费云服务资源

### 2.1 Railway (推荐)
- **免费额度**: $5/月 + 500小时运行时间
- **包含服务**: 
  - Web应用部署
  - MySQL数据库
  - Redis缓存
  - 自动SSL证书
  - 自定义域名
- **优势**: 简单易用，GitHub集成，自动部署

### 2.2 Heroku (备选)
- **免费额度**: 已取消免费计划，最低$7/月
- **包含服务**: Web应用部署，PostgreSQL数据库
- **限制**: 需要付费使用

### 2.3 其他免费服务
- **UptimeRobot**: 免费监控服务 (50个监控点)
- **Sentry**: 免费错误追踪 (5K错误/月)
- **Codecov**: 免费代码覆盖率 (开源项目)
- **GitHub Actions**: 免费CI/CD (2000分钟/月)

## 3. 环境配置

### 3.1 本地开发环境

#### 必需软件
- Java 17+
- Maven 3.6+
- MySQL 8.0+
- Redis 6.0+
- Git

#### 环境变量配置
```env
# 数据库配置
DATABASE_URL=mysql://username:password@localhost:3306/wanli_dev
REDIS_URL=redis://localhost:6379

# JWT配置
JWT_SECRET=your-super-secret-jwt-key-for-development
JWT_EXPIRATION=86400

# 应用配置
SPRING_PROFILES_ACTIVE=dev
SERVER_PORT=8080

# 第三方服务
SENTRY_DSN=your-sentry-dsn
```

### 3.2 Railway部署配置

#### 初始化Railway项目
```bash
# 安装Railway CLI
npm install -g @railway/cli

# 登录Railway
railway login

# 初始化项目
railway init

# 添加MySQL数据库
railway add mysql

# 添加Redis
railway add redis
```

#### 环境变量配置

**开发环境 (dev)**
```env
SPRING_PROFILES_ACTIVE=dev
DATABASE_URL=${{MySQL.DATABASE_URL}}
REDIS_URL=${{Redis.REDIS_URL}}
JWT_SECRET=dev-jwt-secret
SERVER_PORT=8080
CORS_ALLOWED_ORIGINS=http://localhost:3000
SPRING_DATASOURCE_URL=${{MySQL.DATABASE_URL}}
SPRING_DATA_REDIS_URL=${{Redis.REDIS_URL}}
SENTRY_DSN=your-dev-sentry-dsn
```

**测试环境 (staging)**
```env
SPRING_PROFILES_ACTIVE=staging
DATABASE_URL=${{MySQL.DATABASE_URL}}
REDIS_URL=${{Redis.REDIS_URL}}
JWT_SECRET=staging-jwt-secret
SERVER_PORT=8080
CORS_ALLOWED_ORIGINS=https://staging.wanli.com
SPRING_DATASOURCE_URL=${{MySQL.DATABASE_URL}}
SPRING_DATA_REDIS_URL=${{Redis.REDIS_URL}}
SENTRY_DSN=your-staging-sentry-dsn
```

**生产环境 (main)**
```env
SPRING_PROFILES_ACTIVE=prod
DATABASE_URL=${{MySQL.DATABASE_URL}}
REDIS_URL=${{Redis.REDIS_URL}}
JWT_SECRET=prod-jwt-secret
SERVER_PORT=8080
CORS_ALLOWED_ORIGINS=https://wanli.com
SPRING_DATASOURCE_URL=${{MySQL.DATABASE_URL}}
SPRING_DATA_REDIS_URL=${{Redis.REDIS_URL}}
SENTRY_DSN=your-prod-sentry-dsn
```

## 4. CI/CD流程配置

### 4.1 GitHub Actions配置

#### 自动化测试 (.github/workflows/test.yml)
```yaml
name: Test Suite

on:
  push:
    branches: [ main, dev, staging ]
  pull_request:
    branches: [ main, dev ]

jobs:
  test:
    runs-on: ubuntu-latest
    
    services:
      mysql:
        image: mysql:8.0
        env:
          MYSQL_ROOT_PASSWORD: root
          MYSQL_DATABASE: test_db
          MYSQL_USER: test
          MYSQL_PASSWORD: test
        options: >-
          --health-cmd "mysqladmin ping -h localhost"
          --health-interval 10s
          --health-timeout 5s
          --health-retries 5
        ports:
          - 3306:3306
      
      redis:
        image: redis:6-alpine
        options: >-
          --health-cmd "redis-cli ping"
          --health-interval 10s
          --health-timeout 5s
          --health-retries 5
        ports:
          - 6379:6379
    
    steps:
     - uses: actions/checkout@v4
     
     - name: Setup Java
       uses: actions/setup-java@v4
       with:
         java-version: '17'
         distribution: 'temurin'
         cache: 'maven'
     
     - name: Compile project
       run: mvn clean compile
     
     - name: Run code quality checks
       run: |
         mvn checkstyle:check
         mvn spotbugs:check
         mvn pmd:check
     
     - name: Run unit tests
       run: mvn test
       env:
         DATABASE_URL: mysql://test:test@localhost:3306/test_db
         REDIS_URL: redis://localhost:6379
     
     - name: Run integration tests
       run: mvn failsafe:integration-test
       env:
         DATABASE_URL: mysql://test:test@localhost:3306/test_db
         REDIS_URL: redis://localhost:6379
     
     - name: Generate test coverage
       run: mvn jacoco:report
     
     - name: Upload coverage to Codecov
       uses: codecov/codecov-action@v3
       with:
         token: ${{ secrets.CODECOV_TOKEN }}
         file: ./target/site/jacoco/jacoco.xml

  security-scan:
    runs-on: ubuntu-latest
    steps:
     - uses: actions/checkout@v4
     
     - name: Setup Java
       uses: actions/setup-java@v4
       with:
         java-version: '17'
         distribution: 'temurin'
     
     - name: Run Snyk to check for vulnerabilities
       uses: snyk/actions/maven@master
       env:
         SNYK_TOKEN: ${{ secrets.SNYK_TOKEN }}
     
     - name: Initialize CodeQL
       uses: github/codeql-action/init@v2
       with:
         languages: java
     
     - name: Perform CodeQL Analysis
       uses: github/codeql-action/analyze@v2
```

#### 部署流程 (.github/workflows/deploy.yml)
```yaml
name: Deploy

on:
  push:
    branches: [ main, dev, staging ]

jobs:
  deploy-dev:
    if: github.ref == 'refs/heads/dev'
    runs-on: ubuntu-latest
    steps:
     - uses: actions/checkout@v4
     
     - name: Setup Java
       uses: actions/setup-java@v4
       with:
         java-version: '17'
         distribution: 'temurin'
         cache: 'maven'
     
     - name: Build JAR
       run: mvn clean package -DskipTests
     
     - name: Deploy to Railway (Dev)
       uses: railway-app/railway-action@v1
       with:
         railway_token: ${{ secrets.RAILWAY_TOKEN_DEV }}
         service: wanli-backend-dev
     
     - name: Run database migrations
       run: |
         railway run --service wanli-backend-dev mvn flyway:migrate
       env:
         RAILWAY_TOKEN: ${{ secrets.RAILWAY_TOKEN_DEV }}
     
     - name: Notify Slack
       uses: 8398a7/action-slack@v3
       with:
         status: ${{ job.status }}
         webhook_url: ${{ secrets.SLACK_WEBHOOK }}

  deploy-staging:
    if: github.ref == 'refs/heads/staging'
    runs-on: ubuntu-latest
    needs: [test, security-scan]
    steps:
     - uses: actions/checkout@v4
     
     - name: Setup Java
       uses: actions/setup-java@v4
       with:
         java-version: '17'
         distribution: 'temurin'
         cache: 'maven'
     
     - name: Build JAR
       run: mvn clean package -DskipTests
     
     - name: Deploy to Railway (Staging)
       uses: railway-app/railway-action@v1
       with:
         railway_token: ${{ secrets.RAILWAY_TOKEN_STAGING }}
         service: wanli-backend-staging
     
     - name: Run database migrations
       run: |
         railway run --service wanli-backend-staging mvn flyway:migrate
       env:
         RAILWAY_TOKEN: ${{ secrets.RAILWAY_TOKEN_STAGING }}
     
     - name: Run E2E tests
       run: mvn test -Dtest=**/*E2ETest
       env:
         API_BASE_URL: https://wanli-backend-staging.railway.app
     
     - name: Performance testing
       run: mvn test -Dtest=**/*PerformanceTest
       env:
         API_BASE_URL: https://wanli-backend-staging.railway.app

  deploy-prod:
    if: github.ref == 'refs/heads/main'
    runs-on: ubuntu-latest
    needs: [test, security-scan]
    environment: production
    steps:
     - uses: actions/checkout@v4
     
     - name: Setup Java
       uses: actions/setup-java@v4
       with:
         java-version: '17'
         distribution: 'temurin'
         cache: 'maven'
     
     - name: Build JAR
       run: mvn clean package -DskipTests
     
     - name: Deploy to Railway (Production)
       uses: railway-app/railway-action@v1
       with:
         railway_token: ${{ secrets.RAILWAY_TOKEN_PROD }}
         service: wanli-backend-prod
     
     - name: Run database migrations
       run: |
         railway run --service wanli-backend-prod mvn flyway:migrate
       env:
         RAILWAY_TOKEN: ${{ secrets.RAILWAY_TOKEN_PROD }}
```

### 4.2 GitHub Secrets配置

在GitHub仓库设置中添加以下Secrets：

```
RAILWAY_TOKEN_DEV=your-dev-railway-token
RAILWAY_TOKEN_STAGING=your-staging-railway-token  
RAILWAY_TOKEN_PROD=your-prod-railway-token
SNYK_TOKEN=your-snyk-token
CODECOV_TOKEN=your-codecov-token
SLACK_WEBHOOK=your-slack-webhook-url
```

## 5. 部署步骤

### 5.1 Git分支管理

```bash
# 克隆仓库
git clone https://github.com/JamesWuVip/wanli-backend.git
cd wanli-backend

# 创建开发分支
git checkout -b dev
git push -u origin dev

# 创建测试分支
git checkout -b staging
git push -u origin staging
```

### 5.2 Railway项目配置

#### 登录和初始化
```bash
# 登录Railway
railway login

# 连接GitHub仓库
railway init

# 添加数据库服务
railway add mysql
railway add redis
```

#### 创建多环境服务
```bash
# 开发环境
railway create wanli-backend-dev
railway connect --service wanli-backend-dev

# 测试环境  
railway create wanli-backend-staging
railway connect --service wanli-backend-staging

# 生产环境
railway create wanli-backend-prod
railway connect --service wanli-backend-prod
```

### 5.3 数据库初始化

#### JPA实体配置
```java
// User.java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    @Column(unique = true, nullable = false)
    private String username;
    
    @Column(nullable = false)
    private String password;
    
    @Column(name = "full_name")
    private String fullName;
    
    private String avatar;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
    
    @OneToMany(mappedBy = "creator", cascade = CascadeType.ALL)
    private List<Course> courses = new ArrayList<>();
    
    // getters and setters
}

// Course.java
@Entity
@Table(name = "courses")
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String title;
    
    private String description;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal price;
    
    @Column(name = "is_published")
    private Boolean isPublished = false;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id")
    private User creator;
    
    // getters and setters
}
```

#### 数据库迁移 (Flyway)
```sql
-- V1__Create_users_table.sql
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(255),
    avatar VARCHAR(500),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL
);

-- V2__Create_courses_table.sql
CREATE TABLE courses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10,2),
    is_published BOOLEAN DEFAULT FALSE,
    creator_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,
    FOREIGN KEY (creator_id) REFERENCES users(id)
);
```

### 5.4 应用配置文件

#### Railway配置 (railway.json)
```json
{
  "$schema": "https://railway.app/railway.schema.json",
  "build": {
    "builder": "DOCKERFILE",
    "dockerfilePath": "Dockerfile"
  },
  "deploy": {
    "numReplicas": 1,
    "sleepApplication": false,
    "restartPolicyType": "ON_FAILURE"
  }
}
```

#### Docker配置 (Dockerfile)
```dockerfile
# 多阶段构建
FROM openjdk:17-jdk-slim AS builder

WORKDIR /app

# 安装Maven
RUN apt-get update && apt-get install -y maven

# 复制Maven配置文件
COPY pom.xml ./
COPY src ./src/

# 构建应用
RUN mvn clean package -DskipTests

# 生产镜像
FROM openjdk:17-jre-slim AS production

WORKDIR /app

# 创建非root用户
RUN groupadd -r spring && useradd -r -g spring spring

# 安装curl用于健康检查
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# 复制JAR文件
COPY --from=builder --chown=spring:spring /app/target/*.jar app.jar

# 切换到非root用户
USER spring

# 暴露端口
EXPOSE 8080

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:8080/health || exit 1

# 启动应用
CMD ["java", "-jar", "-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:dev}", "app.jar"]
```

## 6. 监控和维护

### 6.1 应用监控

#### UptimeRobot配置
1. 注册 [UptimeRobot](https://uptimerobot.com) 账户
2. 添加HTTP监控：
   - **开发环境**: `https://wanli-backend-dev.railway.app/health`
   - **测试环境**: `https://wanli-backend-staging.railway.app/health`  
   - **生产环境**: `https://wanli-backend-prod.railway.app/health`
3. 设置告警通知（邮件/Slack）

#### 健康检查端点
```java
// HealthController.java
@RestController
@RequestMapping("/health")
public class HealthController {
    
    @Autowired
    private DataSource dataSource;
    
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    
    @GetMapping
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 检查数据库连接
            try (Connection connection = dataSource.getConnection()) {
                connection.createStatement().execute("SELECT 1");
                response.put("database", "connected");
            }
            
            // 检查Redis连接
            redisTemplate.opsForValue().get("health-check");
            response.put("redis", "connected");
            
            response.put("status", "healthy");
            response.put("timestamp", Instant.now().toString());
            response.put("uptime", ManagementFactory.getRuntimeMXBean().getUptime());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("status", "unhealthy");
            response.put("timestamp", Instant.now().toString());
            response.put("error", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
        }
    }
}
```

### 6.2 错误追踪

#### Sentry集成
```java
// SentryConfig.java
@Configuration
public class SentryConfig {
    
    @Bean
    public SentryProperties sentryProperties() {
        SentryProperties properties = new SentryProperties();
        properties.setDsn(System.getenv("SENTRY_DSN"));
        properties.setEnvironment(System.getenv("SPRING_PROFILES_ACTIVE"));
        return properties;
    }
}
```

### 6.3 日志管理

#### Logback配置 (logback-spring.xml)
```xml
<configuration>
    <springProfile name="!prod">
        <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
            <encoder>
                <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
            </encoder>
        </appender>
        <root level="INFO">
            <appender-ref ref="CONSOLE" />
        </root>
    </springProfile>
    
    <springProfile name="prod">
        <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
            <file>logs/application.log</file>
            <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
                <fileNamePattern>logs/application.%d{yyyy-MM-dd}.log</fileNamePattern>
                <maxHistory>30</maxHistory>
            </rollingPolicy>
            <encoder>
                <pattern>%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n</pattern>
            </encoder>
        </appender>
        <root level="WARN">
            <appender-ref ref="FILE" />
        </root>
    </springProfile>
</configuration>
```

## 7. 故障排除

### 7.1 常见问题

#### 常见问题

**1. 部署失败**
```bash
# 检查构建日志
railway logs --service your-service-name

# 重新部署
railway up --service your-service-name

# 检查环境变量
railway variables

# 本地测试构建
mvn clean package -DskipTests
java -jar target/*.jar
```

**2. 数据库连接问题**
```bash
# 检查数据库状态
railway run mysql -h hostname -u username -p database_name

# 重置数据库连接
railway restart --service your-service-name

# 检查数据库URL格式
echo $DATABASE_URL

# 测试数据库连接
mvn test -Dtest=DatabaseConnectionTest
```

**3. 环境变量问题**
```bash
# 查看所有环境变量
railway variables

# 设置环境变量
railway variables set SPRING_PROFILES_ACTIVE=prod
railway variables set JWT_SECRET=your-secret

# 删除环境变量
railway variables unset KEY

# 验证Spring Boot配置
java -jar target/*.jar --spring.config.location=classpath:/application.yml
```

### 7.2 性能优化

#### 数据库优化
```sql
-- 创建索引
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_courses_creator_id ON courses(creator_id);
CREATE INDEX idx_courses_created_at ON courses(created_at);

-- 分析查询性能
EXPLAIN SELECT * FROM users WHERE email = 'user@example.com';
```

#### 缓存策略
```java
// CacheConfig.java
@Configuration
@EnableCaching
public class CacheConfig {
    
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(10))
            .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));
        
        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(config)
            .build();
    }
}
```

## 8. 成本优化建议

### 8.1 Railway成本控制
- 合理使用睡眠模式（开发环境）
- 监控资源使用情况
- 定期清理无用的服务和数据
- 使用共享数据库（非生产环境）

### 8.2 免费服务最大化利用
- GitHub Actions: 2000分钟/月免费额度
- UptimeRobot: 50个监控点免费
- Sentry: 5000错误/月免费
- Codecov: 开源项目免费

### 8.3 GitHub Actions优化
```yaml
# 缓存依赖减少构建时间
- name: Cache Maven dependencies
  uses: actions/cache@v3
  with:
    path: ~/.m2
    key: ${{ runner.os }}-m2-${{ hashFiles('**/pom.xml') }}
    restore-keys: ${{ runner.os }}-m2
```

### 8.4 服务整合
- 开发和测试环境共享Redis实例
- 使用Railway的内置监控替代部分第三方服务
- 合并非关键环境的数据库

## 9. 扩展计划

### 9.1 流量增长应对
- **0-1000 DAU**: 当前免费方案足够
- **1000-10000 DAU**: 升级Railway Pro计划 ($20/月)
- **10000+ DAU**: 考虑迁移到AWS/GCP

### 9.2 成本预算
- **第一年**: $0-60 (主要是Railway升级费用)
- **第二年**: $100-300 (根据用户增长)
- **第三年**: $300-1000 (可能需要专业云服务)

---

## 总结

本部署方案充分利用了免费云服务资源，为万里后端项目提供了完整的CI/CD流程和多环境部署方案。通过合理的成本控制和性能优化，可以在项目初期实现零成本或低成本运营，为项目发展提供了良好的基础。

随着项目的发展和用户增长，可以根据实际需求逐步升级服务配置，确保系统的稳定性和可扩展性。