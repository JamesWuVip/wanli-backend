# Wanli Backend

万里后端服务项目

## 项目概述

这是一个基于Spring Boot的后端服务项目，集成了监控、测试覆盖率和错误追踪等功能。

## 技术栈

- Java 17
- Spring Boot 3.2.0
- Spring Security
- Spring Data JPA
- MySQL
- Maven
- JaCoCo (代码覆盖率)
- Sentry (错误追踪)
- Codecov (覆盖率报告)

## 快速开始

### 环境要求

- Java 17+
- Maven 3.6+
- MySQL 8.0+

### 配置监控服务

运行监控服务配置脚本：

```bash
./setup-monitoring.sh
```

该脚本将自动配置：
- Codecov token
- Sentry DSN
- 生成测试覆盖率报告
- 验证服务连接

### 运行应用

```bash
# 开发环境
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# 生产环境
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

### 测试端点

应用启动后，可以访问以下测试端点：

- `GET /api/test/health` - 健康检查
- `GET /api/test/monitoring-info` - 监控配置信息
- `GET /api/test/sentry-error` - 测试Sentry错误报告
- `GET /api/test/sentry-message` - 测试Sentry消息报告

## 开发规范

### Git Flow

- `main` - 生产环境分支
- `staging` - 测试环境分支  
- `dev` - 开发分支
- `feature/*` - 功能分支
- `fix/*` - 修复分支

### 提交规范

使用 Conventional Commits 规范：

```
feat(scope): 添加新功能
fix(scope): 修复bug
docs(scope): 文档更新
style(scope): 代码格式调整
refactor(scope): 代码重构
test(scope): 测试相关
chore(scope): 构建过程或辅助工具的变动
```

## 监控和质量保证

### 代码覆盖率

使用 JaCoCo 生成覆盖率报告：

```bash
mvn clean test jacoco:report
```

### 错误追踪

集成 Sentry 进行实时错误监控和性能追踪。

### 持续集成

- 自动运行测试
- 生成覆盖率报告
- 上传到 Codecov
- Sentry 错误监控

## 许可证

MIT License
