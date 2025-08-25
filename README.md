# 万里书院后端项目

## GitFlow 工作流程

本项目采用GitFlow工作流程，包含以下分支：

- `main`: 生产环境分支
- `staging`: 测试环境分支  
- `dev`: 开发环境分支
- `feature/*`: 功能开发分支

## 本地开发验证

在推送到远程分支前，请运行本地验证脚本：

```bash
# 快速验证
npm run validate:quick

# 完整验证
npm run validate:full
```

## 分支合并规则

1. 功能开发完成后，合并到 `dev` 分支
2. `dev` 分支通过自动化测试后，合并到 `staging` 分支
3. `staging` 分支测试通过后，合并到 `main` 分支

严格禁止跳过测试环境直接合并到生产环境。