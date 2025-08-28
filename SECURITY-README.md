# 安全配置文档说明

## 重要提醒 ⚠️

本项目包含敏感配置信息，已采取以下安全措施：

### 敏感文档列表

以下文档包含生产环境密码、JWT密钥等敏感信息，**仅保存在本地**，不会推送到GitHub：

- `deployment-configuration-guide.md` - 完整部署配置指南
- `internal-deployment-guide.md` - 内部部署配置（包含完整密码）
- `staging-deployment-guide.md` - 测试环境部署指南
- `.trae/documents/deployment-configuration-guide.md` - Trae文档目录中的配置指南

### 安全措施

1. **Git忽略配置**：已在 `.gitignore` 中添加以下规则：
   ```
   # Sensitive configuration documents
   deployment-configuration-guide.md
   internal-deployment-guide.md
   staging-deployment-guide.md
   *-deployment-guide.md
   
   # Sensitive configuration files
   *-prod.yml
   *-staging.yml
   *-production.yml
   ```

2. **文档访问控制**：
   - 敏感文档仅存储在本地开发环境
   - 团队成员需要通过安全渠道获取配置信息
   - 定期更新密码和密钥

### 团队协作规范

#### 获取敏感配置
1. 新团队成员需要联系项目负责人获取敏感配置文档
2. 通过加密渠道（如企业内部系统）传输敏感信息
3. 收到文档后，确保保存在本地安全目录

#### 配置更新流程
1. 敏感配置变更需要项目负责人审批
2. 更新后通知所有相关团队成员
3. 确保所有环境配置保持同步

#### 安全检查清单
- [ ] 确认 `.gitignore` 包含所有敏感文件模式
- [ ] 验证敏感文档未被git跟踪：`git ls-files | grep -E '(deployment|staging).*guide'`
- [ ] 检查提交历史中是否包含敏感信息
- [ ] 定期审查访问权限

### 环境配置概览

| 环境 | 分支 | 部署平台 | 数据库 | 配置文件 |
|------|------|----------|--------|-----------|
| 开发 | dev | 本地 | PostgreSQL (本地) | application.yml |
| 测试 | staging | Railway | PostgreSQL (Railway) | application-staging.yml |
| 生产 | main | Railway | PostgreSQL (Railway) | application-prod.yml |

### 紧急联系

如发现安全问题或配置泄露，请立即联系：
- 项目负责人：[联系方式]
- 技术负责人：[联系方式]

### 相关文档

- [项目README](./README.md)
- [API文档](./api-documentation.md)
- GitFlow规范：遵循标准GitFlow流程

---

**最后更新**：2025-01-28  
**文档版本**：1.0  
**维护人员**：项目团队
