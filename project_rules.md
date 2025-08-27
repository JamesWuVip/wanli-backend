# 万里后端项目开发规范

## 代码规范

### 命名规则
1. **文件夹命名**：小写字母，下划线分隔
2. **文件命名**：小写字母，下划线分隔
3. **类命名**：驼峰命名（PascalCase）
4. **方法命名**：驼峰命名（camelCase）
5. **变量命名**：驼峰命名（camelCase）
6. **常量命名**：大写字母，下划线分隔

### 代码质量
1. 每个JavaScript方法不要超过200行
2. 同样的错误连续重复3次，要反思换思路处理
3. 调试过程中发现大量错误，应当对错误分类，同类型错误应当先对单个问题进行实验，成功后用自动化批量处理脚本的方式处理

## Git工作流规范

### 分支管理
- **main分支**：生产环境分支，只接受来自staging分支的合并
- **staging分支**：测试环境分支，用于预发布测试
- **dev分支**：开发分支，日常开发工作在此分支进行
- **feature分支**：功能分支，从dev分支创建，完成后合并回dev分支

### 提交规范
- 使用规范的commit message格式
- 及时提交代码到dev分支
- 禁止跳过测试环境直接合并到main分支

### 环境配置
- **开发环境**：使用application.yml
- **测试环境**：使用application-test.yml
- **预发布环境**：使用application-staging.yml
- **生产环境**：使用application-prod.yml

## 项目结构

```
src/
├── main/
│   ├── java/com/wanli/
│   │   ├── config/          # 配置类
│   │   ├── controller/      # 控制器
│   │   ├── dto/            # 数据传输对象
│   │   ├── entity/         # 实体类
│   │   ├── exception/      # 异常处理
│   │   ├── repository/     # 数据访问层
│   │   ├── security/       # 安全相关
│   │   ├── service/        # 业务逻辑层
│   │   └── common/         # 公共类
│   └── resources/
│       ├── application.yml
│       ├── application-test.yml
│       ├── application-staging.yml
│       └── application-prod.yml
└── test/
    └── java/com/wanli/     # 测试代码
```

## 开发指南

### API设计
- 使用RESTful API设计原则
- 统一使用ApiResponse包装返回结果
- 合理使用HTTP状态码
- 提供完整的API文档

### 安全规范
- 使用JWT进行身份认证
- 敏感信息加密存储
- 输入参数验证
- SQL注入防护

### 测试规范
- 单元测试覆盖率不低于60%
- 集成测试覆盖主要业务流程
- 使用MockMvc进行控制器测试
- 使用@MockBean进行服务层测试

### 日志规范
- 使用SLF4J进行日志记录
- 合理设置日志级别
- 敏感信息不记录到日志中
- 生产环境日志文件轮转

## 部署规范

### 环境要求
- JDK 17+
- MySQL 8.0+
- Redis 6.0+（如需要）

### 配置管理
- 使用环境变量管理敏感配置
- 不同环境使用不同的配置文件
- 数据库连接池合理配置

### 监控告警
- 使用Spring Boot Actuator进行健康检查
- 配置应用性能监控
- 设置关键指标告警
