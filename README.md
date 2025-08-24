# 万里后端系统 (Wanli Backend)

## 🚀 最新更新
- ✅ SonarCloud代码质量检查已集成
- ✅ Railway自动部署已配置
- ✅ 修复staging环境数据库schema配置问题

## 项目简介

万里在线教育平台后端系统是一个基于Spring Boot框架开发的现代化教育管理平台。该系统为在线教育提供完整的后端服务支持，包括用户管理、课程管理、作业系统等核心功能。

## 🏗️ 技术架构

### 核心技术栈
- **框架**: Spring Boot 3.x
- **数据库**: MySQL 8.0
- **缓存**: Redis
- **构建工具**: Maven
- **Java版本**: JDK 17

### 架构特点
- 采用分层架构设计，清晰分离业务逻辑
- RESTful API设计，支持前后端分离
- 统一异常处理和响应格式
- 完善的参数验证和安全控制

## 📋 功能模块

### 1. 用户管理模块
- 用户注册、登录、认证
- 角色权限管理（学生、教师、管理员）
- 用户信息管理和维护

### 2. 课程管理模块
- 课程创建、编辑、发布
- 课程分类和标签管理
- 课程资源上传和管理

### 3. 作业系统模块
- 作业创建和发布
- 学生作业提交
- 作业批改和评分
- 作业统计和分析

## 🚀 快速开始

### 环境要求
- JDK 17+
- Maven 3.6+
- MySQL 8.0+
- Redis 6.0+

### 本地开发

1. **克隆项目**
```bash
git clone https://github.com/JamesWuVip/wanli-backend.git
cd wanli-backend
```

2. **配置数据库**
```bash
# 创建数据库
mysql -u root -p
CREATE DATABASE wanli_education;

# 导入初始化脚本
mysql -u root -p wanli_education < init-db.sql
```

3. **配置应用**
```bash
# 复制配置文件
cp src/main/resources/application-example.yml src/main/resources/application-dev.yml
# 编辑配置文件，设置数据库连接等信息
```

4. **启动应用**
```bash
# 使用Maven启动
./mvnw spring-boot:run

# 或者使用IDE直接运行WanliBackendApplication类
```

5. **验证启动**
```bash
# 访问健康检查接口
curl http://localhost:8080/actuator/health
```

## 📖 API文档

启动应用后，可以通过以下方式查看API文档：

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **API Docs**: http://localhost:8080/v3/api-docs

详细的API文档请参考：[API_DOCUMENTATION.md](API_DOCUMENTATION.md)

## 🧪 测试

### 运行单元测试
```bash
./mvnw test
```

### 运行集成测试
```bash
./mvnw verify
```

### 测试覆盖率
```bash
./mvnw jacoco:report
# 查看覆盖率报告：target/site/jacoco/index.html
```

## 🚀 部署

### Docker部署
```bash
# 构建镜像
docker build -t wanli-backend .

# 运行容器
docker-compose up -d
```

### 生产环境部署
详细的部署指南请参考：[DEPLOYMENT.md](DEPLOYMENT.md)

## 📁 项目结构

```
src/
├── main/
│   ├── java/
│   │   └── com/wanli/
│   │       ├── WanliBackendApplication.java    # 应用入口
│   │       ├── config/                         # 配置类
│   │       ├── controller/                     # 控制器层
│   │       ├── service/                        # 服务层
│   │       ├── repository/                     # 数据访问层
│   │       ├── entity/                         # 实体类
│   │       ├── dto/                           # 数据传输对象
│   │       ├── exception/                      # 异常处理
│   │       └── util/                          # 工具类
│   └── resources/
│       ├── application.yml                     # 应用配置
│       ├── application-dev.yml                 # 开发环境配置
│       ├── application-prod.yml                # 生产环境配置
│       └── db/migration/                       # 数据库迁移脚本
└── test/
    ├── java/                                   # 测试代码
    └── resources/                              # 测试资源
```

## 🔧 开发规范

### 代码规范
- 遵循阿里巴巴Java开发手册
- 使用统一的代码格式化配置
- 必须编写单元测试，覆盖率不低于80%
- 所有公共方法必须添加JavaDoc注释

### Git规范
- 使用语义化提交信息（Conventional Commits）
- 功能开发使用feature分支
- 代码合并前必须通过Code Review

详细的开发规范请参考：[ENGINEERING_STANDARDS.md](ENGINEERING_STANDARDS.md)

## 🔍 代码质量

项目集成了多种代码质量检查工具：

- **SonarCloud**: 代码质量和安全性分析
- **SpotBugs**: 静态代码分析
- **Checkstyle**: 代码风格检查
- **JaCoCo**: 测试覆盖率统计

## 📊 监控和日志

### 应用监控
- Spring Boot Actuator健康检查
- Micrometer指标收集
- 自定义业务指标监控

### 日志管理
- 使用Logback作为日志框架
- 结构化日志输出
- 不同环境的日志级别配置

## 🤝 贡献指南

1. Fork本项目
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 创建Pull Request

## 📄 许可证

本项目采用MIT许可证 - 查看 [LICENSE](LICENSE) 文件了解详情

## 📞 联系我们

- 项目维护者：James Wu
- 邮箱：james@wanli.edu
- 项目地址：https://github.com/JamesWuVip/wanli-backend

## 🙏 致谢

感谢所有为本项目做出贡献的开发者！