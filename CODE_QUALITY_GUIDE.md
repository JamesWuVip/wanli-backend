# 代码质量检查指南

## 概述

本指南为万里在线教育平台后端开发团队提供代码质量检查的完整使用说明，包括本地开发、CI/CD集成和问题修复的最佳实践。

## 代码质量工具栈

### 核心工具

| 工具 | 用途 | 配置文件 | 运行方式 |
|------|------|----------|----------|
| **SonarCloud** | 代码质量分析 | `sonar-project.properties` | CI/CD + 本地 |
| **JaCoCo** | 代码覆盖率 | `pom.xml` | Maven插件 |
| **Spotless** | 代码格式化 | `pom.xml` | Maven插件 |
| **OWASP Dependency Check** | 依赖安全扫描 | `pom.xml` + `owasp-suppressions.xml` | Maven插件 |
| **SonarLint** | IDE实时检查 | IDE插件 | 实时 |

### 质量门标准

| 指标 | 阈值 | 说明 |
|------|------|------|
| 代码覆盖率 | ≥ 80% | 单元测试覆盖率 |
| 重复代码率 | ≤ 3% | 代码重复度 |
| 圈复杂度 | ≤ 10 | 方法复杂度 |
| 维护性评级 | A | SonarCloud维护性 |
| 可靠性评级 | A | SonarCloud可靠性 |
| 安全性评级 | A | SonarCloud安全性 |
| 安全热点审查 | 100% | 安全问题处理率 |

## 本地开发工作流

### 1. 开发前准备

```bash
# 1. 拉取最新代码
git pull origin main

# 2. 创建功能分支
git checkout -b feature/your-feature-name

# 3. 安装依赖
./mvnw clean install
```

### 2. 开发过程中的质量检查

#### 实时代码检查（推荐）

**安装SonarLint插件：**

- **IntelliJ IDEA**: `File` → `Settings` → `Plugins` → 搜索 "SonarLint"
- **VS Code**: 扩展商店搜索 "SonarLint"

**配置SonarLint连接SonarCloud：**

1. 打开SonarLint设置
2. 添加SonarCloud连接
3. 输入组织和项目信息
4. 使用SonarCloud token认证

#### 代码格式化

```bash
# 自动格式化代码（Google Java Style）
./mvnw spotless:apply

# 检查格式是否符合规范
./mvnw spotless:check
```

#### 运行单元测试

```bash
# 运行所有测试
./mvnw test

# 运行测试并生成覆盖率报告
./mvnw clean test jacoco:report

# 查看覆盖率报告
open target/site/jacoco/index.html
```

### 3. 提交前检查

```bash
# 完整的质量检查流程
./mvnw clean verify

# 如果需要本地SonarCloud分析
./mvnw sonar:sonar \
  -Dsonar.projectKey=wanli-education-backend \
  -Dsonar.organization=your-organization \
  -Dsonar.host.url=https://sonarcloud.io \
  -Dsonar.login=$SONAR_TOKEN
```

**检查清单：**
- ✅ 所有测试通过
- ✅ 代码覆盖率 ≥ 80%
- ✅ 代码格式符合规范
- ✅ 无安全漏洞
- ✅ SonarLint无严重问题

### 4. 提交和推送

```bash
# 提交代码
git add .
git commit -m "feat: 添加用户认证功能"

# 推送到远程分支
git push origin feature/your-feature-name
```

## CI/CD 质量检查流程

### GitHub Actions 工作流

当您创建Pull Request时，会自动触发以下检查：

1. **代码编译** - 确保代码可以正常编译
2. **单元测试** - 运行所有测试用例
3. **代码覆盖率** - 检查测试覆盖率是否达标
4. **SonarCloud分析** - 全面的代码质量分析
5. **安全扫描** - OWASP依赖漏洞检查
6. **代码格式** - Spotless格式检查

### 质量门检查

**通过条件：**
- 所有测试用例通过
- 代码覆盖率 ≥ 80%
- SonarCloud质量门通过
- 无高危安全漏洞
- 代码格式符合规范

**失败处理：**
- 查看GitHub Actions日志
- 修复相关问题
- 重新推送代码

## 常见问题及解决方案

### 1. 代码覆盖率不足

**问题表现：**
```
[ERROR] Rule violated for bundle wanli-backend: 
instructions covered ratio is 0.75, but expected minimum is 0.80
```

**解决方案：**

1. **查看覆盖率报告**
   ```bash
   ./mvnw jacoco:report
   open target/site/jacoco/index.html
   ```

2. **添加缺失的测试**
   ```java
   @Test
   public void testUserRegistration() {
       // 添加测试用例
       UserDto user = new UserDto();
       user.setUsername("testuser");
       user.setEmail("test@example.com");
       
       UserDto result = userService.register(user);
       
       assertThat(result).isNotNull();
       assertThat(result.getUsername()).isEqualTo("testuser");
   }
   ```

3. **排除不需要测试的代码**
   ```java
   // 在类或方法上添加注解
   @Generated
   @ExcludeFromJacocoGeneratedReport
   public class ConfigurationClass {
       // 配置类代码
   }
   ```

### 2. SonarCloud质量问题

#### 代码异味（Code Smells）

**常见问题：**
- 方法过长
- 参数过多
- 重复代码
- 命名不规范

**解决示例：**

```java
// 问题：方法过长
public void processUser(User user) {
    // 50+ 行代码
}

// 解决：拆分方法
public void processUser(User user) {
    validateUser(user);
    enrichUserData(user);
    saveUser(user);
    sendNotification(user);
}

private void validateUser(User user) {
    // 验证逻辑
}

private void enrichUserData(User user) {
    // 数据丰富逻辑
}
```

#### 安全问题（Security Issues）

**常见问题：**
- SQL注入风险
- 密码硬编码
- 敏感信息泄露

**解决示例：**

```java
// 问题：SQL注入风险
String sql = "SELECT * FROM users WHERE name = '" + userName + "'";

// 解决：使用参数化查询
@Query("SELECT u FROM User u WHERE u.name = :name")
User findByName(@Param("name") String name);
```

#### Bug问题

**常见问题：**
- 空指针异常
- 资源未关闭
- 逻辑错误

**解决示例：**

```java
// 问题：潜在空指针
if (user.getName().equals("admin")) {
    // 处理逻辑
}

// 解决：空值检查
if (user != null && "admin".equals(user.getName())) {
    // 处理逻辑
}
```

### 3. 依赖安全漏洞

**问题表现：**
```
[ERROR] One or more dependencies were identified with vulnerabilities 
that have a CVSS score greater than or equal to 7.0
```

**解决方案：**

1. **查看漏洞报告**
   ```bash
   ./mvnw dependency-check:check
   open target/dependency-check-report.html
   ```

2. **更新依赖版本**
   ```xml
   <!-- 更新到安全版本 -->
   <dependency>
       <groupId>org.springframework.boot</groupId>
       <artifactId>spring-boot-starter-web</artifactId>
       <version>3.2.1</version>
   </dependency>
   ```

3. **添加抑制规则**（仅在确认安全的情况下）
   ```xml
   <!-- owasp-suppressions.xml -->
   <suppress>
       <notes>False positive - not applicable to our use case</notes>
       <packageUrl regex="true">^pkg:maven/org\.example/.*$</packageUrl>
       <cve>CVE-2023-12345</cve>
   </suppress>
   ```

### 4. 代码格式问题

**问题表现：**
```
[ERROR] The following files had format violations:
[ERROR] src/main/java/com/wanli/controller/UserController.java
```

**解决方案：**

```bash
# 自动修复格式问题
./mvnw spotless:apply

# 验证格式
./mvnw spotless:check
```

## IDE配置最佳实践

### IntelliJ IDEA配置

1. **代码风格设置**
   - `File` → `Settings` → `Editor` → `Code Style` → `Java`
   - 导入Google Java Style配置

2. **保存时自动格式化**
   - `File` → `Settings` → `Tools` → `Actions on Save`
   - 启用 "Reformat code" 和 "Optimize imports"

3. **SonarLint配置**
   - 连接到SonarCloud项目
   - 启用实时分析
   - 配置规则集

### VS Code配置

```json
// .vscode/settings.json
{
    "java.format.settings.url": "https://raw.githubusercontent.com/google/styleguide/gh-pages/eclipse-java-google-style.xml",
    "java.format.settings.profile": "GoogleStyle",
    "editor.formatOnSave": true,
    "sonarlint.connectedMode.project": {
        "connectionId": "sonarcloud",
        "projectKey": "wanli-education-backend"
    }
}
```

## 团队协作规范

### Code Review检查清单

**功能性检查：**
- ✅ 功能实现正确
- ✅ 边界条件处理
- ✅ 错误处理完善
- ✅ 性能考虑

**质量检查：**
- ✅ SonarCloud分析通过
- ✅ 测试覆盖率达标
- ✅ 代码风格一致
- ✅ 命名规范
- ✅ 注释清晰

**安全检查：**
- ✅ 无安全漏洞
- ✅ 输入验证
- ✅ 权限控制
- ✅ 敏感信息保护

### 质量问题处理流程

1. **发现问题**
   - CI/CD检查失败
   - Code Review发现
   - SonarCloud报告

2. **问题分类**
   - **阻塞性**: 安全漏洞、严重Bug
   - **重要**: 代码异味、测试覆盖率
   - **一般**: 格式问题、轻微优化

3. **处理优先级**
   - 阻塞性问题：立即修复
   - 重要问题：当前迭代修复
   - 一般问题：下次迭代优化

## 性能优化建议

### 本地开发优化

```bash
# 跳过非必要检查（开发阶段）
./mvnw compile -Dspotless.check.skip=true

# 并行测试执行
./mvnw test -T 4

# 增量编译
./mvnw compile -Dmaven.compiler.useIncrementalCompilation=true
```

### CI/CD优化

```yaml
# GitHub Actions缓存配置
- name: Cache Maven dependencies
  uses: actions/cache@v3
  with:
    path: ~/.m2
    key: ${{ runner.os }}-m2-${{ hashFiles('**/pom.xml') }}
```

## 监控和报告

### 质量趋势监控

1. **SonarCloud仪表板**
   - 定期查看项目概览
   - 关注质量趋势
   - 跟踪技术债务

2. **团队质量报告**
   - 每周质量总结
   - 问题修复进度
   - 最佳实践分享

### 质量指标

| 指标 | 目标值 | 当前值 | 趋势 |
|------|--------|--------|---------|
| 代码覆盖率 | ≥80% | 85% | ↗️ |
| 技术债务 | <1h | 45min | ↘️ |
| 代码重复率 | <3% | 2.1% | ↘️ |
| 安全评级 | A | A | ➡️ |

## 相关资源

### 文档链接

- [工程规范文档](./ENGINEERING_STANDARDS.md)
- [SonarCloud配置指南](./SONARCLOUD_SETUP_GUIDE.md)
- [分支保护设置](./branch-protection-setup.md)

### 外部资源

- [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)
- [SonarCloud规则文档](https://rules.sonarsource.com/java)
- [JaCoCo用户指南](https://www.jacoco.org/jacoco/trunk/doc/)
- [OWASP依赖检查](https://owasp.org/www-project-dependency-check/)

### 工具下载

- [SonarLint for IntelliJ](https://plugins.jetbrains.com/plugin/7973-sonarlint)
- [SonarLint for VS Code](https://marketplace.visualstudio.com/items?itemName=SonarSource.sonarlint-vscode)

## 支持和反馈

如果您在使用过程中遇到问题或有改进建议，请：

1. 查看相关文档和FAQ
2. 在团队群组中讨论
3. 创建GitHub Issue
4. 联系项目维护者

---

**记住：代码质量是团队的共同责任，每个人都应该为维护高质量的代码库贡献力量！**