# SP1 后端自动化验收测试指南

## 概述

本文档为 Sprint 1 的后端自动化验收测试提供完整的配置和使用指南。测试脚本基于测试驱动开发（TDD）原则，确保所有后端验收标准得到满足。

## 测试架构

### 后端测试
- **框架**: Spring Boot Test + JUnit 5 + MockMvc
- **数据库**: H2 内存数据库（测试专用）
- **覆盖范围**: AC-BE-1.1 到 AC-BE-1.5
- **测试类型**: 集成测试，覆盖完整的API端点和业务逻辑

## 环境准备

### 系统要求
- Java 17+
- Maven 3.6+

### 后端测试环境

1. **依赖配置**
   ```xml
   <!-- 已在 pom.xml 中配置 -->
   <dependency>
       <groupId>com.h2database</groupId>
       <artifactId>h2</artifactId>
       <scope>test</scope>
   </dependency>
   ```

2. **测试配置文件**
   - 位置: `src/test/resources/application-test.yml`
   - 配置: H2 内存数据库、JWT 密钥、日志级别

3. **测试框架**
   - Spring Boot Test: 提供完整的Spring上下文
   - JUnit 5: 测试执行框架
   - MockMvc: HTTP请求模拟
   - H2 Database: 内存数据库，测试隔离

## 测试文件结构

```
wanli-backend/
├── src/test/java/com/wanli/
│   └── SP1AcceptanceTest.java          # 后端验收测试
├── src/test/resources/
│   └── application-test.yml            # 测试配置
├── run-acceptance-tests.sh             # 测试运行器
├── SP1-自动化验收测试指南.md           # 本文档
└── README-SP1-测试.md                  # 快速开始指南
```

## 验收标准覆盖

### 后端验收标准 (AC-BE)

| 标准 | 描述 | 测试方法 |
|------|------|----------|
| AC-BE-1.1 | 用户注册功能 | `testUserRegistration()` |
| AC-BE-1.2 | 用户登录功能 | `testUserLogin()` |
| AC-BE-1.3 | 无权限访问保护 | `testUnauthorizedAccess()` |
| AC-BE-1.4 | 有权限访问验证 | `testAuthorizedAccess()` |
| AC-BE-1.5 | 数据关联验证 | `testDataAssociation()` |

### 测试覆盖说明

每个验收标准都有对应的测试方法，确保功能完整性：
- **用户认证体系**: 注册、登录、权限验证
- **内容管理基础**: 课程和课时的CRUD操作
- **数据关联**: 课程与课时的关联关系验证
- **安全性**: JWT令牌验证和访问控制

## 运行测试

### 方式一：测试运行器（推荐）

```bash
# 运行后端验收测试
./run-acceptance-tests.sh

# 查看测试报告
open test-reports/acceptance-test-report.html
```

### 方式二：直接运行Maven测试

```bash
# 运行后端验收测试
mvn test -Dtest=SP1AcceptanceTest -Dspring.profiles.active=test

# 运行所有测试
mvn test -Dspring.profiles.active=test

# 生成测试报告
mvn surefire-report:report
```

## 测试配置说明

### 后端测试配置

**application-test.yml 关键配置:**
```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true

jwt:
  secret: test-secret-key-for-sp1-acceptance-testing
  expiration: 3600000
```

### Maven 测试配置

**pom.xml 关键配置:**
```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <configuration>
        <includes>
            <include>**/*Test.java</include>
        </includes>
    </configuration>
</plugin>
```

## 测试数据管理

### 测试数据管理
- **数据库**: 使用 H2 内存数据库，每次测试自动创建和销毁
- **测试用户**: `testuser@example.com` / `password123`
- **测试管理员**: `admin@example.com` / `admin123`
- **数据隔离**: 每个测试方法独立运行，测试数据自动清理
- **测试课程**: 动态创建测试课程和课时数据
- **JWT令牌**: 测试专用的JWT密钥和配置

## 故障排除

### 常见问题

1. **测试失败**
   - 检查 Java 版本是否为 17+
   - 确认 Maven 依赖已正确安装
   - 查看测试日志中的具体错误信息
   - 确认 H2 数据库依赖已添加到 pom.xml

2. **数据库连接问题**
   - 检查 `application-test.yml` 配置是否正确
   - 确认 H2 数据库 URL 格式正确
   - 查看 JPA 建表日志是否有错误

3. **权限问题**
   ```bash
   # 给测试运行器添加执行权限
   chmod +x run-acceptance-tests.sh
   ```

4. **JWT 相关错误**
   - 检查测试配置中的 JWT 密钥设置
   - 确认令牌生成和验证逻辑正确

### 调试模式

**启用详细日志**
```bash
# 启用详细日志
mvn test -Dtest=SP1AcceptanceTest -Dspring.profiles.active=test -Dlogging.level.com.wanli=DEBUG

# 查看 SQL 执行日志
mvn test -Dtest=SP1AcceptanceTest -Dspring.profiles.active=test -Dlogging.level.org.hibernate.SQL=DEBUG
```

**单独运行特定测试**
```bash
# 运行单个测试方法
mvn test -Dtest=SP1AcceptanceTest#testUserRegistration -Dspring.profiles.active=test
```

## 持续集成

### GitHub Actions 集成

建议在 `.github/workflows/` 中添加以下配置:

```yaml
name: SP1 Acceptance Tests
on: [push, pull_request]
jobs:
  acceptance-tests:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
      - name: Set up Node.js
        uses: actions/setup-node@v3
        with:
          node-version: '16'
      - name: Run Acceptance Tests
        run: ./run-acceptance-tests.sh
```

## 测试报告

测试运行器会生成以下报告:
- `test-reports/acceptance-test-report.html`: 综合测试报告
- `target/surefire-reports/`: Maven 测试报告
- `test-results/`: 前端测试结果

## 最佳实践

1. **测试隔离**: 每个测试方法独立运行，不依赖其他测试
2. **数据清理**: 测试结束后自动清理测试数据
3. **断言明确**: 使用清晰的断言消息，便于问题定位
4. **环境一致**: 测试环境配置与开发环境保持一致
5. **定期运行**: 在每次代码提交前运行验收测试

## 联系支持

如遇到测试相关问题，请:
1. 查看本文档的故障排除部分
2. 检查测试日志和错误信息
3. 联系测试团队获取支持

---

**注意**: 本测试套件基于 SP1 任务说明书的验收标准设计，确保所有功能在开发完成前都有明确的验收目标。