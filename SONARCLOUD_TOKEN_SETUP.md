# SonarCloud Token 配置指南

## 获取 SonarCloud Token

### 步骤 1: 登录 SonarCloud
1. 访问 [SonarCloud](https://sonarcloud.io) <mcreference link="https://medium.com/@rahulsharan512/integrating-sonarcloud-with-github-actions-for-secure-code-analysis-26a7fa206d40" index="2">2</mcreference>
2. 使用您的 GitHub 账户登录

### 步骤 2: 生成访问令牌
1. 点击右上角的头像
2. 选择 "My Account" <mcreference link="https://medium.com/@rahulsharan512/integrating-sonarcloud-with-github-actions-for-secure-code-analysis-26a7fa206d40" index="2">2</mcreference>
3. 点击 "Security" 标签页 <mcreference link="https://dev.to/remast/go-for-sonarcloud-with-github-actions-3pmn" index="3">3</mcreference>
4. 在 "Generate Tokens" 部分创建新的 token <mcreference link="https://medium.com/@rahulsharan512/integrating-sonarcloud-with-github-actions-26a7fa206d40" index="2">2</mcreference>
5. 输入 token 名称（例如："wanli-backend-github-actions"）
6. 点击 "Generate" 按钮
7. **重要**: 立即复制生成的 token（只显示一次）

## 配置 GitHub Secrets

### 步骤 1: 进入仓库设置
1. 打开 GitHub 仓库页面
2. 点击 "Settings" 标签页
3. 在左侧菜单中选择 "Secrets and variables" → "Actions" <mcreference link="https://dev.to/remast/go-for-sonarcloud-with-github-actions-3pmn" index="3">3</mcreference>

### 步骤 2: 添加 SONAR_TOKEN
1. 点击 "New repository secret" 按钮
2. 名称输入: `SONAR_TOKEN` <mcreference link="https://github.com/SonarSource/sonarcloud-github-action" index="1">1</mcreference>
3. 值输入: 从 SonarCloud 复制的 token
4. 点击 "Add secret" 按钮

## 验证配置

### 方法 1: 推送代码触发 GitHub Actions
```bash
# 创建一个小的更改来触发 Actions
echo "# SonarCloud 集成测试" >> README.md
git add README.md
git commit -m "test: 触发 SonarCloud 扫描测试"
git push origin main
```

### 方法 2: 手动触发工作流
1. 进入 GitHub 仓库的 "Actions" 标签页
2. 选择 "SonarQube" 工作流
3. 点击 "Run workflow" 按钮

## 本地开发环境配置（可选）

如果您需要在本地运行 SonarQube 扫描：

### 临时设置（当前会话有效）
```bash
export SONAR_TOKEN=your_sonar_token_here
```

### 永久设置
```bash
# 添加到 ~/.zshrc 或 ~/.bash_profile
echo 'export SONAR_TOKEN=your_sonar_token_here' >> ~/.zshrc
source ~/.zshrc
```

### 运行本地扫描
```bash
./mvnw clean verify sonar:sonar \
  -Dsonar.projectKey=JamesWuVip_wanli-backend \
  -Dsonar.organization=jameswuvip \
  -Dsonar.host.url=https://sonarcloud.io
```

## 故障排除

### 常见问题

1. **"Project not found" 错误**
   - 检查 `sonar.projectKey` 和 `sonar.organization` 是否正确
   - 确认 SONAR_TOKEN 已正确配置

2. **"Authentication failed" 错误**
   - 重新生成 SonarCloud token
   - 确认 GitHub Secrets 中的 SONAR_TOKEN 值正确

3. **外部 PR 无法运行扫描**
   - SonarCloud 不支持外部 PR 的分析 <mcreference link="https://community.sonarsource.com/t/sonar-token-permissions-in-pull-request-github-actions/90614" index="5">5</mcreference>
   - 只有组织成员或协作者的 PR 才能触发扫描

### 检查工作流状态
1. 进入 GitHub 仓库的 "Actions" 标签页
2. 查看最近的工作流运行状态
3. 点击失败的工作流查看详细日志

## 安全最佳实践

1. **Token 权限最小化**: 只授予必要的权限
2. **定期轮换**: 建议每 90 天更新一次 token
3. **监控使用**: 定期检查 token 的使用情况
4. **团队访问**: 使用组织级别的 secrets 而不是个人 token

## 相关文档

- [SonarCloud GitHub Actions 官方文档](https://docs.sonarsource.com/sonarqube-cloud/advanced-setup/ci-based-analysis/github-actions-for-sonarcloud/) <mcreference link="https://docs.sonarsource.com/sonarqube-cloud/advanced-setup/ci-based-analysis/github-actions-for-sonarcloud/" index="4">4</mcreference>
- [SonarCloud GitHub Action](https://github.com/SonarSource/sonarcloud-github-action) <mcreference link="https://github.com/SonarSource/sonarcloud-github-action" index="1">1</mcreference>
- [代码质量报告](./SONARCLOUD_CODE_QUALITY_REPORT.md)

---

**注意**: 完成 token 配置后，GitHub Actions 将自动在每次推送到 main 分支或创建 Pull Request 时运行 SonarCloud 扫描。