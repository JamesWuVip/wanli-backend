# 万里后端项目部署指导方案

## 目录
- [1. 项目概述](#1-项目概述)
- [2. 免费云服务资源选择](#2-免费云服务资源选择)
- [3. 环境配置](#3-环境配置)
- [4. CI/CD流程配置](#4-cicd流程配置)
- [5. 部署步骤](#5-部署步骤)
- [6. 监控和维护](#6-监控和维护)
- [7. 故障排除](#7-故障排除)
- [8. 成本优化建议](#8-成本优化建议)

## 1. 项目概述

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

### 1.2 分支管理策略和部署环境
- `main`: 生产环境分支 (部署到Railway)
- `staging`: 测试环境分支 (部署到Railway)
- `dev`: 开发环境分支 (本地Tomcat运行)
- `feature/*`: 功能开发分支
- `fix/*`: 修复分支

### 1.3 部署环境说明
- **开发环境 (dev)**: 本地运行，使用内嵌Tomcat服务器，连接本地MySQL和Redis
- **测试环境 (staging)**: 部署到Railway平台，使用Railway提供的MySQL和Redis服务
- **生产环境 (production)**: 部署到Railway平台，使用Railway提供的MySQL和Redis服务

## 2. 免费云服务资源选择

### 2.1 核心服务

#### Railway (主要部署平台)
- **免费额度**: $5/月免费额度
- **包含服务**: 
  - Web应用部署
  - MySQL数据库
  - Redis缓存
  - 自动SSL证书
  - 自定义域名

#### GitHub (代码托管和CI/CD)
- **免费额度**: 
  - 无限公共仓库
  - 私有仓库 (个人账户)
  - GitHub Actions: 2000分钟/月
  - GitHub Packages: 500MB存储

### 2.2 辅助服务

#### 监控和日志
- **UptimeRobot**: 免费网站监控 (50个监控点)
- **LogRocket**: 免费日志分析 (1000会话/月)
- **Sentry**: 免费错误追踪 (5000错误/月)

#### 邮件服务
- **SendGrid**: 免费邮件发送 (100封/天)
- **Mailgun**: 免费邮件服务 (5000封/月前3个月)

#### 文件存储
- **Cloudinary**: 免费图片/视频处理 (25GB存储)
- **AWS S3**: 免费层 (5GB存储，12个月)

#### DNS和CDN
- **Cloudflare**: 免费CDN和DNS
- **Vercel**: 免费静态资源托管

## 3. 环境配置

### 3.1 本地开发环境 (dev分支)

#### 必需工具
```bash
# Java 17 (推荐使用SDKMAN管理版本)
curl -s "https://get.sdkman.io" | bash
sdk install java 17.0.8-oracle
sdk use java 17.0.8-oracle

# Maven
sdk install maven 3.9.4

# Git
brew install git  # macOS

# MySQL (本地数据库)
brew install mysql  # macOS
brew services start mysql

# Redis (本地缓存)
brew install redis  # macOS
brew services start redis

# Railway CLI (用于部署到staging/production)
npm install -g @railway/cli
```

#### 本地服务启动
```bash
# 启动Spring Boot应用 (使用内嵌Tomcat)
mvn spring-boot:run

# 或者使用IDE运行主类
# com.wanli.WanliBackendApplication
```

#### 环境变量配置
创建 `.env.example` 文件：
```env
# 数据库配置
DB_HOST=localhost
DB_PORT=3306
DB_NAME=wanli_dev
DB_USERNAME=root
DB_PASSWORD=password

# Redis配置
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=

# 应用配置
SERVER_PORT=8080
SPRING_PROFILES_ACTIVE=dev
JWT_SECRET=dev-jwt-secret-key-change-in-production
JWT_EXPIRATION=86400000

# 邮件服务配置
SPRING_MAIL_HOST=smtp.exmail.qq.com
SPRING_MAIL_PORT=465
SPRING_MAIL_USERNAME=your-email@wanli.com
SPRING_MAIL_PASSWORD=your-app-password

# 监控配置
MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE=health,info,metrics
SENTRY_DSN=your-sentry-dsn
```

### 3.2 Railway项目配置 (staging和production环境)

#### 创建Railway项目
```bash
# 登录Railway
railway login

# 为staging环境创建项目
railway init wanli-backend-staging

# 为production环境创建项目
railway init wanli-backend-production

# 在每个项目中添加MySQL数据库
railway add mysql

# 在每个项目中添加Redis
railway add redis
```

#### 环境变量配置
在Railway Dashboard中为不同环境配置环境变量：

**注意**: dev环境在本地运行，不需要Railway配置

**测试环境 (staging)**
```env
SPRING_PROFILES_ACTIVE=staging
SPRING_DATASOURCE_URL=jdbc:mysql://${{MySQL.MYSQL_HOST}}:${{MySQL.MYSQL_PORT}}/${{MySQL.MYSQL_DATABASE}}
SPRING_DATASOURCE_USERNAME=${{MySQL.MYSQL_USER}}
SPRING_DATASOURCE_PASSWORD=${{MySQL.MYSQL_PASSWORD}}
SPRING_DATA_REDIS_HOST=${{Redis.REDIS_HOST}}
SPRING_DATA_REDIS_PORT=${{Redis.REDIS_PORT}}
SPRING_DATA_REDIS_PASSWORD=${{Redis.REDIS_PASSWORD}}
JWT_SECRET=staging-jwt-secret-key-change-in-production
JWT_EXPIRATION=86400000
SERVER_PORT=8080
CORS_ALLOWED_ORIGINS=https://staging.wanli.com
SPRING_MAIL_HOST=smtp.exmail.qq.com
SPRING_MAIL_PORT=465
SPRING_MAIL_USERNAME=your-staging-email@wanli.com
SPRING_MAIL_PASSWORD=your-staging-app-password
MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE=health,info,metrics
SENTRY_DSN=your-staging-sentry-dsn
```

**生产环境 (main)**
```env
SPRING_PROFILES_ACTIVE=prod
SPRING_DATASOURCE_URL=jdbc:mysql://${{MySQL.MYSQL_HOST}}:${{MySQL.MYSQL_PORT}}/${{MySQL.MYSQL_DATABASE}}
SPRING_DATASOURCE_USERNAME=${{MySQL.MYSQL_USER}}
SPRING_DATASOURCE_PASSWORD=${{MySQL.MYSQL_PASSWORD}}
SPRING_DATA_REDIS_HOST=${{Redis.REDIS_HOST}}
SPRING_DATA_REDIS_PORT=${{Redis.REDIS_PORT}}
SPRING_DATA_REDIS_PASSWORD=${{Redis.REDIS_PASSWORD}}
JWT_SECRET=prod-jwt-secret-key-must-be-strong
JWT_EXPIRATION=86400000
SERVER_PORT=8080
CORS_ALLOWED_ORIGINS=https://wanli.com
SPRING_MAIL_HOST=smtp.exmail.qq.com
SPRING_MAIL_PORT=465
SPRING_MAIL_USERNAME=your-prod-email@wanli.com
SPRING_MAIL_PASSWORD=your-prod-app-password
MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE=health,info,metrics
SENTRY_DSN=your-prod-sentry-dsn
```

## 4. CI/CD流程配置

### 4.1 GitHub Actions工作流

创建 `.github/workflows/ci-cd.yml`：

```yaml
name: CI/CD Pipeline

on:
  push:
    branches: [main, staging, dev]
  pull_request:
    branches: [main, staging, dev]

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
        image: redis:7
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
    
    - name: Run CodeQL Analysis
      uses: github/codeql-action/init@v2
      with:
        languages: java
    
    - name: Perform CodeQL Analysis
      uses: github/codeql-action/analyze@v2

  # dev环境在本地运行，不需要自动部署
  # 开发者在本地使用 mvn spring-boot:run 启动应用

  deploy-staging:
    needs: [test, security-scan]
    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/staging' && github.event_name == 'push'
    
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
        token: ${{ secrets.RAILWAY_TOKEN_STAGING }}
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

  deploy-production:
    needs: [test, security-scan]
    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/main' && github.event_name == 'push'
    
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
        token: ${{ secrets.RAILWAY_TOKEN_PROD }}
        service: wanli-backend-prod
    
    - name: Run database migrations
      run: |
        railway run --service wanli-backend-prod mvn flyway:migrate
      env:
        RAILWAY_TOKEN: ${{ secrets.RAILWAY_TOKEN_PROD }}
    
    - name: Health check
      run: |
        sleep 30
        curl -f https://wanli-backend.railway.app/health || exit 1
    
    - name: Notify deployment
      uses: 8398a7/action-slack@v3
      with:
        status: ${{ job.status }}
        channel: '#deployments'
        webhook_url: ${{ secrets.SLACK_WEBHOOK }}
```

### 4.2 自动化测试配置

#### 测试脚本 (package.json)
```json
{
  "scripts": {
    "test": "jest",
    "test:unit": "jest --testPathPattern=unit",
    "test:integration": "jest --testPathPattern=integration",
    "test:e2e": "jest --testPathPattern=e2e",
    "test:coverage": "jest --coverage",
    "test:performance": "artillery run tests/performance/load-test.yml",
    "lint": "eslint src/**/*.js",
    "lint:fix": "eslint src/**/*.js --fix",
    "type-check": "tsc --noEmit",
    "db:migrate": "npx prisma migrate deploy",
    "db:seed": "npx prisma db seed"
  }
}
```

#### Jest配置 (jest.config.js)
```javascript
module.exports = {
  testEnvironment: 'node',
  setupFilesAfterEnv: ['<rootDir>/tests/setup.js'],
  testMatch: [
    '<rootDir>/tests/**/*.test.js'
  ],
  collectCoverageFrom: [
    'src/**/*.js',
    '!src/**/*.test.js',
    '!src/index.js'
  ],
  coverageThreshold: {
    global: {
      branches: 80,
      functions: 80,
      lines: 80,
      statements: 80
    }
  },
  testTimeout: 30000
};
```

## 5. 部署步骤

### 5.1 环境部署概览

**开发环境 (dev分支)**:
- 本地运行，使用内嵌Tomcat服务器
- 连接本地MySQL和Redis服务
- 使用 `mvn spring-boot:run` 启动

**测试环境 (staging分支)**:
- 自动部署到Railway平台
- 使用Railway提供的MySQL和Redis服务
- 通过GitHub Actions自动部署

**生产环境 (main分支)**:
- 自动部署到Railway平台
- 使用Railway提供的MySQL和Redis服务
- 通过GitHub Actions自动部署

### 5.2 初始化项目

#### 步骤1: 创建GitHub仓库
```bash
# 创建本地仓库
git init
git add .
git commit -m "feat: 初始化万里后端项目"

# 关联远程仓库
git remote add origin https://github.com/JamesWuVip/wanli-backend.git
git branch -M main
git push -u origin main

# 创建开发分支
git checkout -b dev
git push -u origin dev

# 创建测试分支
git checkout -b staging
git push -u origin staging
```

#### 步骤2: 配置本地开发环境
```bash
# 安装并启动MySQL
brew install mysql
brew services start mysql

# 安装并启动Redis
brew install redis
brew services start redis

# 创建开发数据库
mysql -u root -p
CREATE DATABASE wanli_dev;
```

#### 步骤3: 配置Railway项目 (仅用于staging和production)
```bash
# 登录Railway
railway login

# 为staging环境创建项目
railway init wanli-backend-staging
railway add mysql
railway add redis

# 为production环境创建项目
railway init wanli-backend-production
railway add mysql
railway add redis
```

#### 步骤4: 配置环境变量
- **本地开发**: 在 `.env` 文件中配置
- **Railway环境**: 在Railway Dashboard中配置

### 5.3 配置GitHub Secrets

在GitHub仓库设置中添加以下Secrets：

```
# Railway部署tokens (仅用于staging和production)
RAILWAY_TOKEN_STAGING=your-staging-railway-token
RAILWAY_TOKEN_PROD=your-prod-railway-token

# 代码质量和监控
SNYK_TOKEN=your-snyk-token
CODECOV_TOKEN=your-codecov-token
SLACK_WEBHOOK=your-slack-webhook-url
```

**注意**: dev环境在本地运行，不需要Railway token

### 5.4 数据库初始化

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