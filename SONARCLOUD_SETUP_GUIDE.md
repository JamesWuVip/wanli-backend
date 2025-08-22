# SonarCloud 配置指南

## 概述

本指南将帮助您完成万里在线教育平台后端项目与SonarCloud的集成配置，实现自动化代码质量检查。

## 前提条件

- ✅ 已使用GitHub账号在 [SonarCloud](https://sonarcloud.io) 注册
- ✅ 拥有GitHub仓库的管理员权限
- ✅ 项目已推送到GitHub

## 配置步骤

### 第一步：在SonarCloud创建项目

1. **登录SonarCloud**
   - 访问 https://sonarcloud.io
   - 使用GitHub账号登录

2. **创建新项目**
   - 点击右上角的 `+` 按钮
   - 选择 `Analyze new project`
   - 选择您的GitHub组织或个人账户
   - 找到 `wanli-backend` 仓库并点击 `Set up`

3. **配置项目信息**
   - **Project Key**: `wanli-education-backend`
   - **Display Name**: `万里在线教育平台后端`
   - **Organization**: 选择您的SonarCloud组织

### 第二步：获取必要的配置信息

1. **获取组织Key**
   - 在SonarCloud项目页面，点击右上角的齿轮图标
   - 选择 `Administration` > `Organization`
   - 复制 `Organization Key`

2. **生成SonarCloud Token**
   - 点击右上角的用户头像
   - 选择 `My Account` > `Security`
   - 在 `Generate Tokens` 部分：
     - Name: `wanli-backend-github-actions`
     - Type: `User Token`
     - Expires in: `No expiration` 或选择合适的过期时间
   - 点击 `Generate` 并复制生成的token

### 第三步：配置GitHub Secrets

1. **访问GitHub仓库设置**
   - 进入 `wanli-backend` 仓库
   - 点击 `Settings` 选项卡
   - 在左侧菜单选择 `Secrets and variables` > `Actions`

2. **添加必要的Secrets**
   
   点击 `New repository secret` 并添加以下secrets：

   | Secret名称 | 值 | 说明 |
   |-----------|----|---------|
   | `SONAR_TOKEN` | 第二步生成的token | SonarCloud访问令牌 |
   | `SONAR_ORGANIZATION` | 您的组织key | SonarCloud组织标识 |
   | `SONAR_PROJECT_KEY` | `wanli-education-backend` | SonarCloud项目标识 |

### 第四步：更新项目配置

1. **更新sonar-project.properties**
   
   在项目根目录的 `sonar-project.properties` 文件中，取消注释并更新组织配置：
   
   ```properties
   # 将 your-sonarcloud-organization 替换为实际的组织key
   sonar.organization=your-sonarcloud-organization
   ```

2. **验证pom.xml配置**
   
   确认 `pom.xml` 中包含SonarCloud相关配置（已配置完成）：
   
   ```xml
   <properties>
       <sonar.projectKey>wanli-education-backend</sonar.projectKey>
       <sonar.projectName>万里在线教育平台后端</sonar.projectName>
       <!-- 其他配置... -->
   </properties>
   ```

### 第五步：测试集成

1. **创建测试分支**
   ```bash
   git checkout -b test-sonarcloud-integration
   echo "# SonarCloud测试" >> README.md
   git add README.md
   git commit -m "test: 测试SonarCloud集成"
   git push origin test-sonarcloud-integration
   ```

2. **创建Pull Request**
   - 在GitHub上创建从 `test-sonarcloud-integration` 到 `main` 的PR
   - 观察GitHub Actions是否自动触发
   - 检查SonarCloud分析是否成功运行

3. **验证结果**
   - 在PR页面查看状态检查
   - 访问SonarCloud项目页面查看分析结果
   - 确认质量门状态

## SonarCloud项目配置

### 质量门配置

在SonarCloud项目中配置质量门规则：

1. **访问质量门设置**
   - 进入SonarCloud项目
   - 点击 `Quality Gates` 选项卡
   - 选择 `Sonar way` 或创建自定义质量门

2. **推荐的质量门条件**
   
   | 指标 | 操作符 | 阈值 | 说明 |
   |------|--------|------|---------|
   | Coverage | is less than | 80% | 代码覆盖率不低于80% |
   | Duplicated Lines (%) | is greater than | 3% | 重复代码率不超过3% |
   | Maintainability Rating | is worse than | A | 维护性评级不低于A |
   | Reliability Rating | is worse than | A | 可靠性评级不低于A |
   | Security Rating | is worse than | A | 安全性评级不低于A |
   | Security Hotspots Reviewed | is less than | 100% | 安全热点100%审查 |

### 分支配置

1. **主分支设置**
   - 进入 `Administration` > `Branches and Pull Requests`
   - 设置 `main` 为主分支
   - 配置分支保护策略

2. **Pull Request分析**
   - 启用 `Analyze Pull Requests`
   - 配置PR装饰器显示
   - 设置PR质量门检查

## 常见问题排查

### 1. SonarCloud分析失败

**可能原因：**
- Token配置错误
- 组织或项目Key不匹配
- 网络连接问题

**解决方案：**
```bash
# 检查GitHub Secrets配置
# 验证SonarCloud项目设置
# 查看GitHub Actions日志
```

### 2. 质量门检查失败

**可能原因：**
- 代码覆盖率不足
- 代码质量问题
- 安全漏洞

**解决方案：**
```bash
# 运行本地测试
./mvnw clean test jacoco:report

# 查看覆盖率报告
open target/site/jacoco/index.html

# 修复代码质量问题
./mvnw spotless:apply
```

### 3. GitHub Actions权限问题

**可能原因：**
- GITHUB_TOKEN权限不足
- 仓库设置限制

**解决方案：**
- 检查仓库的Actions权限设置
- 确认工作流文件配置正确

## 本地开发集成

### 本地运行SonarCloud分析

```bash
# 设置环境变量
export SONAR_TOKEN="your-sonar-token"
export SONAR_ORGANIZATION="your-organization"

# 运行分析
./mvnw clean verify sonar:sonar \
  -Dsonar.projectKey=wanli-education-backend \
  -Dsonar.organization=$SONAR_ORGANIZATION \
  -Dsonar.host.url=https://sonarcloud.io \
  -Dsonar.login=$SONAR_TOKEN
```

### IDE集成

1. **IntelliJ IDEA**
   - 安装 `SonarLint` 插件
   - 连接到SonarCloud项目
   - 实时代码质量检查

2. **VS Code**
   - 安装 `SonarLint` 扩展
   - 配置SonarCloud连接
   - 启用实时分析

## 监控和维护

### 定期检查

1. **每周检查**
   - 查看SonarCloud项目仪表板
   - 检查代码质量趋势
   - 处理新发现的问题

2. **每月检查**
   - 更新质量门规则
   - 检查依赖安全漏洞
   - 优化分析配置

### 团队协作

1. **代码审查流程**
   - PR必须通过SonarCloud检查
   - 修复所有阻塞性问题
   - 讨论代码质量改进

2. **培训和文档**
   - 团队SonarCloud使用培训
   - 代码质量最佳实践
   - 问题修复指南

## 相关链接

- [SonarCloud官方文档](https://docs.sonarcloud.io/)
- [SonarCloud GitHub集成](https://docs.sonarcloud.io/getting-started/github/)
- [SonarQube Maven插件](https://docs.sonarqube.org/latest/analysis/scan/sonarscanner-for-maven/)
- [代码质量最佳实践](https://docs.sonarcloud.io/improving/overview/)

## 支持

如果在配置过程中遇到问题，请：

1. 查看GitHub Actions运行日志
2. 检查SonarCloud项目活动日志
3. 参考本项目的 `ENGINEERING_STANDARDS.md` 文档
4. 联系项目维护者获取帮助

---

**注意：** 请确保不要在代码中硬编码任何敏感信息（如tokens），始终使用GitHub Secrets或环境变量。