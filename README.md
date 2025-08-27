# 万里后端系统

基于Spring Boot的现代化后端API系统，提供用户管理、身份认证等核心功能。

## 技术栈

- **框架**: Spring Boot 3.2.0
- **安全**: Spring Security + JWT
- **数据库**: MySQL 8.0 / H2 (测试)
- **ORM**: Spring Data JPA
- **构建工具**: Maven
- **测试**: JUnit 5 + Mockito
- **代码覆盖率**: JaCoCo

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.6+
- MySQL 8.0+ (生产环境)

### 本地开发

1. 克隆项目
```bash
git clone https://github.com/JamesWuVip/wanli-backend.git
cd wanli-backend
```

2. 配置数据库
```bash
# 创建数据库
mysql -u root -p
CREATE DATABASE wanli_dev CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

3. 修改配置文件
```yaml
# src/main/resources/application.yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/wanli_dev
    username: your_username
    password: your_password
```

4. 运行应用
```bash
mvn spring-boot:run
```

应用将在 http://localhost:8080 启动

### 测试

```bash
# 运行所有测试
mvn test

# 生成测试覆盖率报告
mvn jacoco:report
```

测试覆盖率报告位于 `target/site/jacoco/index.html`

## API 文档

### 认证相关

#### 用户注册
```http
POST /api/auth/register
Content-Type: application/json

{
  "username": "testuser",
  "password": "password123",
  "email": "test@example.com"
}
```

#### 用户登录
```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "testuser",
  "password": "password123"
}
```

### 用户管理

#### 获取当前用户信息
```http
GET /api/users/me
Authorization: Bearer <jwt_token>
```

#### 获取所有用户 (需要管理员权限)
```http
GET /api/users
Authorization: Bearer <jwt_token>
```

#### 根据ID获取用户
```http
GET /api/users/{id}
Authorization: Bearer <jwt_token>
```

### 健康检查

```http
GET /api/health
```

## 项目结构

```
src/
├── main/
│   ├── java/com/wanli/
│   │   ├── WanliBackendApplication.java    # 应用入口
│   │   ├── config/                        # 配置类
│   │   │   ├── JpaAuditConfig.java       # JPA审计配置
│   │   │   ├── SecurityConfig.java       # Spring Security配置
│   │   │   └── WebConfig.java            # Web配置
│   │   ├── controller/                    # REST控制器
│   │   │   ├── AuthController.java       # 认证控制器
│   │   │   ├── HealthController.java     # 健康检查控制器
│   │   │   └── UserController.java       # 用户控制器
│   │   ├── dto/                          # 数据传输对象
│   │   ├── entity/                       # JPA实体
│   │   ├── exception/                    # 异常处理
│   │   ├── repository/                   # 数据访问层
│   │   ├── security/                     # 安全相关
│   │   ├── service/                      # 业务逻辑层
│   │   └── common/                       # 公共类
│   └── resources/
│       ├── application.yml               # 主配置文件
│       ├── application-test.yml          # 测试环境配置
│       ├── application-staging.yml       # 预发布环境配置
│       └── application-prod.yml          # 生产环境配置
└── test/                                 # 测试代码
```

## 代码质量

- **测试覆盖率**: 60%+
- **单元测试**: 170个测试用例全部通过
- **代码规范**: 遵循阿里巴巴Java开发手册
- **安全扫描**: 无高危漏洞

## 部署

### 开发环境
```bash
mvn spring-boot:run
```

### 测试环境
```bash
mvn clean package -Ptest
java -jar target/wanli-backend-1.0.0.jar --spring.profiles.active=test
```

### 预发布环境
```bash
mvn clean package -Pstaging
java -jar target/wanli-backend-1.0.0.jar --spring.profiles.active=staging
```

### 生产环境
```bash
mvn clean package -Pprod
java -jar target/wanli-backend-1.0.0.jar --spring.profiles.active=prod
```

## 环境变量

| 变量名 | 描述 | 默认值 |
|--------|------|--------|
| DB_HOST | 数据库主机 | localhost |
| DB_PORT | 数据库端口 | 3306 |
| DB_NAME | 数据库名称 | wanli_dev |
| DB_USERNAME | 数据库用户名 | root |
| DB_PASSWORD | 数据库密码 | |
| JWT_SECRET | JWT密钥 | |
| JWT_EXPIRATION | JWT过期时间(毫秒) | 86400000 |

## 贡献指南

1. Fork 项目
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 创建 Pull Request

## 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情

## 联系方式

- 项目维护者: JamesWu
- 邮箱: james@example.com
- 项目地址: https://github.com/JamesWuVip/wanli-backend
