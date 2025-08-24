# GitHub 分支保护规则配置指南

## 概述

本文档说明如何为万里在线教育平台后端项目配置GitHub分支保护规则，确保代码质量和安全性。

## 分支保护策略

### 主要分支保护

#### 1. main 分支（生产分支）
- **保护级别**: 最高
- **合并要求**: 必须通过所有状态检查
- **审查要求**: 至少1个代码审查批准
- **限制推送**: 仅允许通过Pull Request合并

#### 2. staging 分支（测试分支）
- **保护级别**: 高
- **合并要求**: 必须通过所有状态检查
- **审查要求**: 至少1个代码审查批准
- **限制推送**: 仅允许通过Pull Request合并

## 配置步骤

### 方法一：通过GitHub Web界面配置

1. **访问仓库设置**
   - 进入GitHub仓库页面
   - 点击 `Settings` 选项卡
   - 在左侧菜单中选择 `Branches`

2. **添加分支保护规则**
   - 点击 `Add rule` 按钮
   - 在 `Branch name pattern` 中输入分支名称（如 `main`）

3. **配置保护选项**

   **基本保护设置：**
   - ✅ `Require a pull request before merging`
     - ✅ `Require approvals` (设置为 1)
     - ✅ `Dismiss stale PR approvals when new commits are pushed`
     - ✅ `Require review from code owners`
   
   **状态检查要求：**
   - ✅ `Require status checks to pass before merging`
     - ✅ `Require branches to be up to date before merging`
     - 添加必需的状态检查：
       - `SonarQube Quality Gate`
       - `Security Vulnerability Scan`
       - `Code Format Check`
       - `build` (如果有CI构建)
   
   **推送限制：**
   - ✅ `Restrict pushes that create files`
   - ✅ `Require signed commits`
   
   **其他设置：**
   - ✅ `Include administrators`
   - ✅ `Allow force pushes` (❌ 不勾选)
   - ✅ `Allow deletions` (❌ 不勾选)

### 方法二：通过GitHub CLI配置

```bash
# 安装GitHub CLI（如果尚未安装）
brew install gh

# 登录GitHub
gh auth login

# 配置main分支保护规则
gh api repos/:owner/:repo/branches/main/protection \
  --method PUT \
  --field required_status_checks='{"strict":true,"contexts":["SonarQube Quality Gate","Security Vulnerability Scan","Code Format Check"]}' \
  --field enforce_admins=true \
  --field required_pull_request_reviews='{"required_approving_review_count":1,"dismiss_stale_reviews":true,"require_code_owner_reviews":true}' \
  --field restrictions=null

# 配置staging分支保护规则
gh api repos/:owner/:repo/branches/staging/protection \
  --method PUT \
  --field required_status_checks='{"strict":true,"contexts":["SonarQube Quality Gate","Security Vulnerability Scan","Code Format Check"]}' \
  --field enforce_admins=true \
  --field required_pull_request_reviews='{"required_approving_review_count":1,"dismiss_stale_reviews":true,"require_code_owner_reviews":false}' \
  --field restrictions=null
```

### 方法三：通过GitHub API配置

创建配置脚本 `setup-branch-protection.sh`：

```bash
#!/bin/bash

# GitHub仓库信息
OWNER="your-username"  # 替换为实际的GitHub用户名或组织名
REPO="wanli-backend"   # 仓库名称
TOKEN="your-github-token"  # 替换为实际的GitHub Personal Access Token

# API基础URL
API_URL="https://api.github.com/repos/$OWNER/$REPO/branches"

# 配置main分支保护
curl -X PUT \
  -H "Authorization: token $TOKEN" \
  -H "Accept: application/vnd.github.v3+json" \
  "$API_URL/main/protection" \
  -d '{
    "required_status_checks": {
      "strict": true,
      "contexts": [
        "SonarQube Quality Gate",
        "Security Vulnerability Scan", 
        "Code Format Check"
      ]
    },
    "enforce_admins": true,
    "required_pull_request_reviews": {
      "required_approving_review_count": 1,
      "dismiss_stale_reviews": true,
      "require_code_owner_reviews": true
    },
    "restrictions": null
  }'

# 配置staging分支保护
curl -X PUT \
  -H "Authorization: token $TOKEN" \
  -H "Accept: application/vnd.github.v3+json" \
  "$API_URL/staging/protection" \
  -d '{
    "required_status_checks": {
      "strict": true,
      "contexts": [
        "SonarQube Quality Gate",
        "Security Vulnerability Scan",
        "Code Format Check"
      ]
    },
    "enforce_admins": true,
    "required_pull_request_reviews": {
      "required_approving_review_count": 1,
      "dismiss_stale_reviews": true,
      "require_code_owner_reviews": false
    },
    "restrictions": null
  }'

echo "分支保护规则配置完成！"
```

## CODEOWNERS 文件配置

创建 `.github/CODEOWNERS` 文件来指定代码审查者：

```
# 全局代码所有者
* @your-username

# 特定目录的代码所有者
/src/main/java/com/wanli/controller/ @backend-team-lead
/src/main/java/com/wanli/service/ @backend-team-lead
/src/main/java/com/wanli/security/ @security-team

# 配置文件
*.yml @devops-team
*.properties @devops-team
Dockerfile @devops-team

# 文档
*.md @tech-writer
/doc/ @tech-writer

# CI/CD配置
/.github/ @devops-team
```

## 状态检查配置

### 必需的状态检查

1. **SonarQube Quality Gate**
   - 检查项：代码质量、安全性、覆盖率
   - 失败条件：质量门未通过

2. **Security Vulnerability Scan**
   - 检查项：依赖安全漏洞
   - 失败条件：发现高危漏洞（CVSS ≥ 7.0）

3. **Code Format Check**
   - 检查项：代码格式规范
   - 失败条件：格式不符合规范

### 可选的状态检查

1. **Build Check**
   - 检查项：代码编译
   - 失败条件：编译失败

2. **Unit Tests**
   - 检查项：单元测试
   - 失败条件：测试失败或覆盖率不足

## 验证配置

### 1. 检查分支保护状态

```bash
# 查看main分支保护状态
gh api repos/:owner/:repo/branches/main/protection

# 查看staging分支保护状态
gh api repos/:owner/:repo/branches/staging/protection
```

### 2. 测试保护规则

1. **创建测试分支**
   ```bash
   git checkout -b test-branch-protection
   echo "test" > test-file.txt
   git add test-file.txt
   git commit -m "test: 测试分支保护规则"
   git push origin test-branch-protection
   ```

2. **创建Pull Request**
   - 尝试直接推送到main分支（应该被拒绝）
   - 创建PR到main分支
   - 验证状态检查是否正常运行

3. **验证审查要求**
   - 尝试在没有审查的情况下合并（应该被阻止）
   - 请求代码审查并批准
   - 验证合并是否成功

## 故障排除

### 常见问题

1. **状态检查未运行**
   - 检查GitHub Actions工作流配置
   - 验证SonarQube配置和令牌
   - 检查分支名称匹配

2. **无法合并PR**
   - 确认所有必需的状态检查已通过
   - 检查是否有足够的审查批准
   - 验证分支是否为最新

3. **管理员权限问题**
   - 确认 `Include administrators` 设置
   - 检查用户权限和角色

### 联系支持

如果遇到配置问题，请联系：
- DevOps团队：devops@wanli.edu
- 技术负责人：tech-lead@wanli.edu

## 更新日志

- 2024-01-XX: 初始版本
- 2024-01-XX: 添加CODEOWNERS配置
- 2024-01-XX: 更新状态检查要求