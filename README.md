# 万里在线教育平台后端

基于Spring Boot 3.2.1的在线教育平台后端服务。

## 技术栈

- **框架**: Spring Boot 3.2.1
- **数据库**: MySQL 8.0
- **ORM**: MyBatis Plus 3.5.5
- **安全**: Spring Security + JWT
- **缓存**: Redis
- **文档**: Knife4j (Swagger)
- **构建工具**: Maven
- **Java版本**: 17

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.6+
- MySQL 8.0+
- Redis 6.0+

### 运行应用

```bash
# 克隆项目
git clone https://github.com/JamesWuVip/wanli-backend.git
cd wanli-backend

# 编译项目
mvn clean compile

# 运行测试
mvn test

# 启动应用
mvn spring-boot:run
```

### 配置文件

- `application.yml` - 基础配置
- `application-staging.yml` - 测试环境配置
- `application-production.yml` - 生产环境配置

## API文档

启动应用后访问: http://localhost:8080/doc.html

## 项目结构

```
src/
├── main/
│   ├── java/
│   │   └── com/
│   │       └── wanli/
│   │           └── backend/
│   └── resources/
│       ├── application.yml
│       ├── application-staging.yml
│       ├── application-production.yml
│       └── db/
└── test/
    └── java/
```

## 代码质量

项目集成了以下代码质量工具:

- **SonarQube**: 代码质量分析
- **JaCoCo**: 代码覆盖率
- **OWASP Dependency Check**: 安全漏洞检测

```bash
# 运行代码质量检查
mvn clean verify sonar:sonar

# 生成测试覆盖率报告
mvn clean test jacoco:report

# 安全漏洞检测
mvn dependency-check:check
```