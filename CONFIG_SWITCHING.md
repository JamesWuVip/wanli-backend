# 配置文件自动切换机制

本项目实现了基于Git分支的配置文件自动切换机制，确保不同环境使用正确的配置参数。

## 环境配置文件

### Spring Boot配置文件
- `application.yml` - 主配置文件，使用环境变量`SPRING_PROFILES_ACTIVE`控制激活的配置
- `application-dev.yml` - 开发环境配置
- `application-staging.yml` - 测试环境配置  
- `application-prod.yml` - 生产环境配置

### Railway部署配置文件
- `nixpacks-dev.toml` - 开发环境Railway配置
- `nixpacks-staging.toml` - 测试环境Railway配置
- `nixpacks-prod.toml` - 生产环境Railway配置
- `nixpacks.toml` - 当前激活的Railway配置（由脚本自动生成）

## 自动切换机制

### 1. 本地开发
使用`deploy.sh`脚本根据当前Git分支自动切换配置：
```bash
./deploy.sh
```

### 2. GitHub Actions部署
GitHub Actions会在部署时自动根据分支切换配置：
- `main`分支 → 生产环境配置
- `staging`分支 → 测试环境配置
- `dev`分支 → 开发环境配置

## 环境变量配置

### 开发环境 (dev)
- `SPRING_PROFILES_ACTIVE=dev`
- 本地PostgreSQL数据库
- JWT密钥：硬编码
- CORS：localhost:3000

### 测试环境 (staging)
- `SPRING_PROFILES_ACTIVE=staging`
- Railway PostgreSQL数据库
- JWT密钥：从环境变量获取
- CORS：staging.wanli.ai

### 生产环境 (prod)
- `SPRING_PROFILES_ACTIVE=prod`
- Railway PostgreSQL数据库
- JWT密钥：从环境变量获取
- CORS：wanli.ai

## 使用流程

### 开发流程
1. 在`dev`分支开发功能
2. 提交到`staging`分支进行测试
3. 测试通过后合并到`main`分支部署生产

### 配置更新
1. 修改对应环境的配置文件
2. 运行`./deploy.sh`更新当前配置
3. 提交并推送到对应分支

## 注意事项

1. **禁止跳过测试环境**：不允许直接从`dev`分支合并到`main`分支
2. **环境变量安全**：生产环境的敏感信息必须通过Railway环境变量配置
3. **配置一致性**：确保各环境配置文件的结构保持一致
4. **数据库隔离**：不同环境使用不同的数据库schema

## 故障排除

如果部署失败，检查以下项目：
1. 环境变量是否正确设置
2. 数据库连接是否正常
3. JWT密钥是否配置
4. CORS设置是否正确