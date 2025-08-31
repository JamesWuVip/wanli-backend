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

### 1.2 分支管理策略
- `main`: 生产环境分支
- `staging`: 测试环境分支
- `dev`: 开发环境分支
- `feature/*`: 功能开发分支
- `fix/*`: 修复分支

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

### 3.1 本地开发环境

#### 必需工具
```bash
# Node.js (推荐使用nvm管理版本)
curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.39.0/install.sh | bash
nvm install 18
nvm use 18

# Git
brew install git  # macOS

# Docker (可选，用于本地数据库)
brew install --cask docker  # macOS

# Railway CLI
npm install -g @railway/cli
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
SPRING_MAIL_HOST=smtp.gmail.com
SPRING_MAIL_PORT=587
SPRING_MAIL_USERNAME=your-email@gmail.com
SPRING_MAIL_PASSWORD=your-app-password

# 监控配置
MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE=health,info,metrics
SENTRY_DSN=your-sentry-dsn
```

### 3.2 Railway项目配置

#### 创建Railway项目
```bash
# 登录Railway
railway login

# 创建新项目
railway init

# 添加MySQL数据库
railway add mysql

# 添加Redis
railway add redis
```

#### 环境变量配置
在Railway Dashboard中配置环境变量：

**开发环境 (dev)**
```env
SPRING_PROFILES_ACTIVE=dev
SPRING_DATASOURCE_URL=jdbc:mysql://${{MySQL.MYSQL_HOST}}:${{MySQL.MYSQL_PORT}}/${{MySQL.MYSQL_DATABASE}}
SPRING_DATASOURCE_USERNAME=${{MySQL.MYSQL_USER}}
SPRING_DATASOURCE_PASSWORD=${{MySQL.MYSQL_PASSWORD}}
SPRING_DATA_REDIS_HOST=${{Redis.REDIS_HOST}}
SPRING_DATA_REDIS_PORT=${{Redis.REDIS_PORT}}
SPRING_DATA_REDIS_PASSWORD=${{Redis.REDIS_PASSWORD}}
JWT_SECRET=dev-jwt-secret-key-change-in-production
JWT_EXPIRATION=86400000
SERVER_PORT=8080
CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:3001
SPRING_MAIL_HOST=smtp.gmail.com
SPRING_MAIL_PORT=587
SPRING_MAIL_USERNAME=your-email@gmail.com
SPRING_MAIL_PASSWORD=your-app-password
MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE=health,info,metrics
SENTRY_DSN=your-dev-sentry-dsn
```

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
SPRING_MAIL_HOST=smtp.gmail.com
SPRING_MAIL_PORT=587
SPRING_MAIL_USERNAME=your-staging-email@gmail.com
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
SPRING_MAIL_HOST=smtp.gmail.com
SPRING_MAIL_PORT=587
SPRING_MAIL_USERNAME=your-prod-email@gmail.com
SPRING_MAIL_PASSWORD=your-prod-app-password
MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE=health,info,metrics
SENTRY_DSN=your-prod-sentry-dsn
```