# GitHub Secrets 配置指南

## 概述

本指南将帮助您完成万里在线教育平台后端项目的GitHub Secrets配置，这是SonarCloud集成的最后一步。

## 必需的Secrets配置

### 第一步：访问GitHub仓库设置

1. 打开GitHub仓库：https://github.com/JamesWuVip/wanli-backend
2. 点击 `Settings` 选项卡
3. 在左侧菜单选择 `Secrets and variables` → `Actions`

### 第二步：添加SonarCloud相关Secrets

点击 `New repository secret` 按钮，依次添加以下secrets：

#### 1. SONAR_TOKEN

**获取方式：**
1. 访问 [SonarCloud](https://sonarcloud.io)
2. 使用GitHub账号登录
3. 点击右上角用户头像 → `My Account`
4. 选择 `Security` 选项卡
5. 在 `Generate Tokens` 部分：
   - **Name**: `wanli-backend-github-actions`
   - **Type**: `User Token`
   - **Expires in**: `No expiration` 或选择合适的过期时间
6. 点击 `Generate` 并复制生成的token

**在GitHub中添加：**
- **Name**: `SONAR_TOKEN`
- **Secret**: 粘贴刚才复制的token

#### 2. SONAR_ORGANIZATION

**获取方式：**
1. 在SonarCloud中，进入您的组织页面
2. 点击右上角齿轮图标 → `Administration`
3. 选择 `Organization` 选项卡
4. 复制 `Organization Key`

**在GitHub中添加：**
- **Name**: `SONAR_ORGANIZATION`
- **Secret**: 您的SonarCloud组织key

#### 3. SONAR_PROJECT_KEY

**在GitHub中添加：**
- **Name**: `SONAR_PROJECT_KEY`
- **Secret**: `wanli-education-backend`

### 第三步：在SonarCloud创建项目

如果还没有在SonarCloud创建项目，请按以下步骤操作：

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

### 第四步：更新本地配置文件

更新 `sonar-project.properties` 文件中的组织配置：

```properties
# 取消注释并替换为您的实际组织key
sonar.organization=your-actual-organization-key
```

### 第五步：测试配置

1. **创建测试分支**
   ```bash
   git checkout -b test-sonarcloud-setup
   echo "# SonarCloud配置测试" >> README.md
   git add README.md
   git commit -m "test: 测试SonarCloud配置"
   git push origin test-sonarcloud-setup
   ```

2. **创建Pull Request**
   - 在GitHub上创建从 `test-sonarcloud-setup` 到 `main` 的PR
   - 观察GitHub Actions是否自动触发
   - 检查所有检查项是否通过

3. **验证结果**
   - 查看PR页面的状态检查
   - 访问SonarCloud项目页面查看分析结果
   - 确认质量门状态为通过

## 配置验证清单

### GitHub Secrets检查
- [ ] `SONAR_TOKEN` 已添加且有效
- [ ] `SONAR_ORGANIZATION` 已添加且正确
- [ ] `SONAR_PROJECT_KEY` 已添加为 `wanli-education-backend`

### SonarCloud项目检查
- [ ] 项目已在SonarCloud创建
- [ ] 项目key为 `wanli-education-backend`
- [ ] 组织配置正确
- [ ] 可以访问项目仪表板

### GitHub Actions检查
- [ ] 工作流文件存在于 `.github/workflows/sonarqube.yml`
- [ ] 推送代码时自动触发检查
- [ ] 所有检查步骤正常执行
- [ ] SonarCloud分析成功运行

## 常见问题排查

### 1. SonarCloud Token无效

**错误信息：**
```
Error: You're not authorized to run analysis. Please contact the project administrator.
```

**解决方案：**
- 检查SONAR_TOKEN是否正确复制
- 确认token没有过期
- 重新生成token并更新GitHub Secret

### 2. 组织或项目Key不匹配

**错误信息：**
```
Error: Project 'wanli-education-backend' not found
```

**解决方案：**
- 检查SONAR_ORGANIZATION是否正确
- 确认项目在SonarCloud中已创建
- 验证项目key是否为 `wanli-education-backend`

### 3. GitHub Actions权限问题

**错误信息：**
```
Error: Resource not accessible by integration
```

**解决方案：**
- 检查仓库的Actions权限设置
- 确认工作流文件中的权限配置正确
- 验证GITHUB_TOKEN权限

### 4. 质量门检查失败

**可能原因：**
- 代码覆盖率不足（< 80%）
- 存在代码质量问题
- 安全漏洞未修复

**解决方案：**
```bash
# 本地运行完整检查
./mvnw clean verify

# 查看覆盖率报告
open target/site/jacoco/index.html

# 修复代码格式
./mvnw spotless:apply

# 本地SonarCloud分析
./mvnw sonar:sonar -Dsonar.login=$SONAR_TOKEN
```

## 下一步操作

配置完成后，建议进行以下操作：

1. **启用分支保护**
   - 在GitHub仓库设置中启用分支保护规则
   - 要求状态检查通过才能合并
   - 要求代码审查

2. **团队培训**
   - 分享SonarCloud使用指南
   - 培训代码质量最佳实践
   - 建立代码审查流程

3. **持续监控**
   - 定期查看SonarCloud项目仪表板
   - 跟踪代码质量趋势
   - 及时处理新发现的问题

## 支持资源

- [SonarCloud官方文档](https://docs.sonarcloud.io/)
- [GitHub Actions文档](https://docs.github.com/en/actions)
- [项目工程规范](./ENGINEERING_STANDARDS.md)
- [代码质量指南](./CODE_QUALITY_GUIDE.md)
- [SonarCloud设置指南](./SONARCLOUD_SETUP_GUIDE.md)

---

**注意：** 请确保不要在代码中硬编码任何敏感信息（如tokens），始终使用GitHub Secrets或环境变量。

**配置完成后，请删除测试分支并关闭测试PR。**