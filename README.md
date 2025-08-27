# 万里后端项目

基于Spring Boot 3.5的纯后端项目，采用标准的Maven项目结构。

## 项目结构

```
wanli-backend/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── wanli/
│   │   │           ├── WanliBackendApplication.java    # 主启动类
│   │   │           ├── controller/                     # 控制器层
│   │   │           │   ├── AuthController.java
│   │   │           │   ├── UserController.java
│   │   │           │   └── HealthController.java
│   │   │           ├── service/                        # 服务层
│   │   │           │   ├── AuthService.java
│   │   │           │   ├── UserService.java
│   │   │           │   └── impl/
│   │   │           │       ├── AuthServiceImpl.java
│   │   │           │       └── UserServiceImpl.java
│   │   │           ├── repository/                     # 数据访问层
│   │   │           │   └── UserRepository.java
│   │   │           ├── entity/                         # 实体类
│   │   │           │   ├── User.java
│   │   │           │   ├── UserRole.java
│   │   │           │   └── UserStatus.java
│   │   │           ├── config/                         # 配置类
│   │   │           │   ├── SecurityConfig.java
│   │   │           │   ├── JwtUtil.java
│   │   │           │   ├── WebConfig.java
│   │   │           │   └── JpaAuditConfig.java
│   │   │           ├── security/                       # 安全相关
│   │   │           │   ├── CustomUserDetailsService.java
│   │   │           │   ├── JwtAuthenticationEntryPoint.java
│   │   │           │   └── JwtAuthenticationFilter.java
│   │   │           ├── common/                         # 通用类
│   │   │           │   ├── ApiResponse.java
│   │   │           │   └── ErrorCode.java
│   │   │           ├── exception/                      # 异常处理
│   │   │           │   ├── BaseException.java
│   │   │           │   ├── BusinessException.java
│   │   │           │   ├── GlobalExceptionHandler.java
│   │   │           │   └── user/
│   │   │           │       ├── UserNotFoundException.java
│   │   │           │       ├── DuplicateUsernameException.java
│   │   │           │       ├── DuplicateEmailException.java
│   │   │           │       └── InvalidPasswordException.java
│   │   │           └── dto/                            # 数据传输对象
│   │   │               ├── auth/
│   │   │               │   ├── LoginRequestDto.java
│   │   │               │   ├── LoginResponseDto.java
│   │   │               │   └── UserRegistrationDto.java
│   │   │               └── user/
│   │   │                   ├── UserResponseDto.java
│   │   │                   ├── UserCreateDto.java
│   │   │                   └── UserUpdateDto.java
│   │   └── resources/
│   │       ├── application.yml                         # 主配置文件
│   │       ├── application-local.yml                   # 本地开发环境配置
│   │       ├── application-test.yml                    # 测试环境配置
│   │       └── application-prod.yml                    # 生产环境配置
│   └── test/
│       ├── java/                                       # 测试代码
│       └── resources/                                  # 测试资源
├── pom.xml                                             # Maven配置文件
└── README.md                                           # 项目说明
```

## 技术栈

- **Spring Boot**: 3.5.0
- **Java**: 17
- **Maven**: 项目构建工具
- **Spring Data JPA**: 数据持久化
- **PostgreSQL**: 数据库
- **Spring Security**: 安全框架
- **JWT**: 身份认证
- **Lombok**: 简化代码
- **Validation**: 参数校验
- **SpringDoc OpenAPI**: API文档
- **JaCoCo**: 代码覆盖率

## 快速开始

### 1. 环境要求

- JDK 17+
- Maven 3.6+
- PostgreSQL 12+

### 2. 数据库配置

创建数据库：
```sql
CREATE DATABASE wanli_db;
CREATE USER wanli_user WITH PASSWORD 'wanli_password';
GRANT ALL PRIVILEGES ON DATABASE wanli_db TO wanli_user;
```

### 3. 环境配置

项目支持多环境配置：
- `local`: 本地开发环境
- `test`: 测试环境
- `prod`: 生产环境

修改 `src/main/resources/application.yml` 中的 `spring.profiles.active` 来切换环境。

### 4. 运行项目

```bash
# 编译项目
mvn clean compile

# 运行测试
mvn test

# 运行项目
mvn spring-boot:run

# 或指定环境运行
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

### 5. 访问接口

项目启动后，可以通过以下地址访问：

- 健康检查：`GET http://localhost:8080/api/health`
- API文档：`http://localhost:8080/swagger-ui.html`
- 用户注册：`POST http://localhost:8080/api/auth/register`
- 用户登录：`POST http://localhost:8080/api/auth/login`

## API 接口

### 认证接口
- `POST /api/auth/register` - 用户注册
- `POST /api/auth/login` - 用户登录
- `GET /api/auth/me` - 获取当前用户信息
- `POST /api/auth/logout` - 用户登出

### 用户管理接口
- `POST /api/users` - 创建用户（管理员）
- `GET /api/users` - 获取所有用户（管理员）
- `GET /api/users/{id}` - 根据ID获取用户
- `GET /api/users/username/{username}` - 根据用户名获取用户
- `PUT /api/users/{id}` - 更新用户信息
- `DELETE /api/users/{id}` - 删除用户（管理员）
- `GET /api/users/check/username/{username}` - 检查用户名是否存在
- `GET /api/users/check/email/{email}` - 检查邮箱是否存在

### 系统接口
- `GET /api/health` - 服务健康检查
- `GET /api/info` - 系统信息

## 安全特性

- JWT Token认证
- 基于角色的访问控制（RBAC）
- 密码加密存储
- 跨域请求支持
- 统一异常处理
- 请求参数校验

## 用户角色

- `ADMIN`: 管理员，拥有所有权限
- `TEACHER`: 教师，拥有教学相关权限
- `STUDENT`: 学生，基础权限

## 开发规范

### 命名规则
1. 文件夹命名：小写字母，下划线分隔
2. 文件命名：小写字母，下划线分隔
3. 类命名：驼峰命名
4. 方法命名：驼峰命名
5. 变量命名：驼峰命名
6. 常量命名：大写字母，下划线分隔

### 代码规范
- 每个方法不超过200行
- 使用Lombok简化代码
- 统一异常处理
- 统一响应格式
- 完善的单元测试
- 代码覆盖率要求80%以上

## 测试

```bash
# 运行单元测试
mvn test

# 运行集成测试
mvn verify

# 生成覆盖率报告
mvn jacoco:report
```

覆盖率报告位置：`target/site/jacoco/index.html`

## 部署

### 本地部署
```bash
mvn clean package
java -jar target/wanli-backend-1.0.0.jar --spring.profiles.active=local
```

### 生产部署
```bash
mvn clean package -Pprod
java -jar target/wanli-backend-1.0.0.jar --spring.profiles.active=prod
```

## 项目特性

- ✅ 标准的Spring Boot 3.5项目结构
- ✅ 完整的用户认证和授权系统
- ✅ JWT Token认证
- ✅ 多环境配置支持
- ✅ 统一的响应格式封装
- ✅ 全局异常处理
- ✅ 参数校验
- ✅ 跨域配置
- ✅ JPA数据持久化和审计
- ✅ 完整的用户管理功能
- ✅ API文档自动生成
- ✅ 单元测试和集成测试
- ✅ 代码覆盖率检查
- ✅ 健康检查接口

## 许可证

MIT License