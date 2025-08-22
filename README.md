# 万里后端系统 (Wanli Backend)

## 🚀 最新更新
- ✅ SonarCloud代码质量检查已集成
- ✅ Railway自动部署已配置

## 项目简介

万里后端系统是一个基于 Java Spring Boot 框架开发的现代化后端应用程序。该项目采用了最新的技术栈和最佳实践，为前端应用提供稳定、高效的 API 服务。

## 技术栈

- **Java 17** - 编程语言
- **Spring Boot 3.2.0** - 应用框架
- **Spring Data JPA** - 数据访问层
- **MySQL 8.0** - 数据库
- **Maven** - 项目构建工具
- **Lombok** - 代码简化工具
- **JUnit 5** - 单元测试框架

## 项目结构

```
wanli-backend/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── wanli/
│   │   │           └── backend/
│   │   │               ├── WanliBackendApplication.java
│   │   │               └── controller/
│   │   │                   └── HealthController.java
│   │   └── resources/
│   │       └── application.yml
│   └── test/
│       └── java/
│           └── com/
│               └── wanli/
│                   └── backend/
│                       └── WanliBackendApplicationTests.java
├── pom.xml
└── README.md
```

## 快速开始

### 环境要求

- Java 17 或更高版本
- Maven 3.6 或更高版本
- MySQL 8.0 或更高版本

### 安装步骤

1. **克隆项目**
   ```bash
   git clone https://github.com/JamesWuVip/wanli-backend.git
   cd wanli-backend
   ```

2. **配置数据库**
   - 创建数据库：`CREATE DATABASE wanli_db;`
   - 修改 `src/main/resources/application.yml` 中的数据库连接信息

3. **编译项目**
   ```bash
   mvn clean compile
   ```

4. **运行测试**
   ```bash
   mvn test
   ```

5. **启动应用**
   ```bash
   mvn spring-boot:run
   ```

### 访问应用

应用启动后，可以通过以下地址访问：

- **健康检查**: http://localhost:8080/api/health
- **欢迎页面**: http://localhost:8080/api/welcome

## API 文档

### 健康检查接口

**GET** `/api/health`

返回应用的健康状态信息。

**响应示例：**
```json
{
  "status": "UP",
  "timestamp": "2024-01-01T12:00:00",
  "service": "wanli-backend",
  "version": "1.0.0"
}
```

### 欢迎接口

**GET** `/api/welcome`

返回欢迎信息。

**响应示例：**
```json
{
  "message": "欢迎使用万里后端系统！",
  "description": "这是一个基于Spring Boot的Java后端项目"
}
```

## 开发指南

### 代码规范

- 使用 Java 17 语法特性
- 遵循 Spring Boot 最佳实践
- 使用 Lombok 简化代码
- 编写单元测试
- 添加适当的注释和文档

### 分支管理

- `main` - 主分支，用于生产环境
- `develop` - 开发分支
- `feature/*` - 功能分支
- `hotfix/*` - 热修复分支

## 部署

### 构建 JAR 包

```bash
mvn clean package
```

### 运行 JAR 包

```bash
java -jar target/wanli-backend-1.0.0.jar
```

## 贡献指南

1. Fork 本项目
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 创建 Pull Request

## 许可证

本项目采用 MIT 许可证。详情请参阅 [LICENSE](LICENSE) 文件。

## 联系方式

- 项目维护者：wanli-team
- 项目地址：https://github.com/JamesWuVip/wanli-backend

---

**万里后端系统** - 让后端开发更简单！