# 本地 GitFlow 工作流程指南

## 概述

本项目采用本地验证 + 远程自动化的 GitFlow 工作流程，确保代码质量和部署安全。

## 分支结构

- `main`: 生产环境分支（受保护）
- `staging`: 测试环境分支（受保护）
- `dev`: 开发环境分支
- `feature/*`: 功能开发分支
- `hotfix/*`: 紧急修复分支

## 本地开发流程

### 1. 功能开发

```bash
# 从 dev 分支创建功能分支
git checkout dev
git pull origin dev
git checkout -b feature/your-feature-name

# 开发完成后运行本地验证
npm run validate:quick  # 快速验证
npm run validate:full   # 完整验证

# 验证通过后提交代码
git add .
git commit -m "feat: your feature description"
```

### 2. 合并到开发分支

```bash
# 切换到 dev 分支
git checkout dev
git pull origin dev

# 合并功能分支
git merge feature/your-feature-name

# 再次运行完整验证
npm run validate:full

# 推送到远程
git push origin dev
```

## 验证脚本说明

### 快速验证 (`npm run validate:quick`)

- 代码格式检查 (Prettier)
- ESLint 代码质量检查
- TypeScript 类型检查
- 单元测试
- 快速构建验证

### 完整验证 (`npm run validate:full`)

- 包含快速验证的所有项目
- E2E 端到端测试
- 多环境构建测试
- 性能基准测试

## 自动化流程

### dev → staging

- 自动触发：当 dev 分支有新提交时
- 自动运行：完整测试套件
- 自动部署：测试环境
- 自动切换：测试环境配置文件

### staging → main

- 手动触发：通过 Pull Request
- 自动运行：生产环境验证
- 自动部署：生产环境
- 自动切换：生产环境配置文件

## 配置文件管理

项目包含多环境配置文件：

- `.env.development` - 开发环境
- `.env.test` - 测试环境
- `.env.production` - 生产环境

自动化流程会根据目标分支自动切换相应的配置文件。

## 分支保护规则

### main 分支

- 禁止直接推送
- 必须通过 Pull Request
- 必须通过所有状态检查
- 必须有代码审查批准

### staging 分支

- 禁止直接推送
- 只接受来自 dev 分支的自动合并
- 必须通过所有测试

## 紧急修复流程

```bash
# 从 main 分支创建热修复分支
git checkout main
git pull origin main
git checkout -b hotfix/urgent-fix-name

# 修复完成后
npm run validate:full
git add .
git commit -m "fix: urgent fix description"

# 推送并创建 PR 到 main
git push origin hotfix/urgent-fix-name
```

## 最佳实践

1. **提交前验证**：始终在提交前运行 `npm run validate:quick`
2. **小步提交**：保持提交粒度小，便于回滚和审查
3. **清晰命名**：使用语义化的分支名和提交信息
4. **及时同步**：定期从上游分支拉取最新代码
5. **测试覆盖**：为新功能编写相应的测试用例

## 故障排除

### 验证脚本失败

1. 检查代码格式：`npm run format`
2. 修复 ESLint 错误：`npm run lint`
3. 检查类型错误：`npm run type-check`
4. 运行单元测试：`npm run test:unit`

### 合并冲突

1. 拉取最新代码：`git pull origin dev`
2. 解决冲突后重新验证：`npm run validate:full`
3. 提交解决方案：`git commit -m "resolve: merge conflicts"`

## 联系支持

如遇到工作流程问题，请联系开发团队或查看项目文档。