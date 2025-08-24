# SP1 后端自动化验收测试 - 快速开始

## 🚀 一键运行测试

```bash
# 克隆项目后，直接运行后端验收测试
./run-acceptance-tests.sh
```

## 📋 测试覆盖范围

### 后端 API 测试 (Spring Boot Test)
- ✅ AC-BE-1.1: 用户注册功能
- ✅ AC-BE-1.2: 用户登录功能  
- ✅ AC-BE-1.3: 无权限访问保护
- ✅ AC-BE-1.4: 有权限访问验证
- ✅ AC-BE-1.5: 数据关联验证

### 🎯 测试特点
- **完整集成测试**: 覆盖完整的API端点和业务逻辑
- **数据库隔离**: 使用H2内存数据库，测试间完全隔离
- **安全验证**: JWT令牌生成、验证和权限控制
- **数据关联**: 课程与课时的完整CRUD和关联测试

## 🛠️ 环境要求

- Java 17+
- Maven 3.6+

## 📁 关键文件

```
├── src/test/java/com/wanli/SP1AcceptanceTest.java    # 后端验收测试
├── src/test/resources/application-test.yml          # 测试配置
├── run-acceptance-tests.sh                          # 测试运行器
├── SP1-自动化验收测试指南.md                        # 详细文档
└── README-SP1-测试.md                               # 本文件
```

## 🔧 其他运行方式

**直接运行Maven测试:**
```bash
# 运行SP1验收测试
mvn test -Dtest=SP1AcceptanceTest -Dspring.profiles.active=test

# 运行所有测试
mvn test -Dspring.profiles.active=test

# 生成测试报告
mvn surefire-report:report
```

## 📊 查看测试报告

```bash
# 运行测试后，打开报告
open test-reports/acceptance-test-report.html
```

## 🎯 测试驱动开发流程

1. **运行验收测试** - 查看当前失败的测试
2. **实现功能** - 根据测试要求开发功能
3. **再次运行测试** - 验证功能是否满足验收标准
4. **重复直到全部通过** - 确保所有验收标准达成

## 📖 详细文档

查看 [SP1-自动化验收测试指南.md](./SP1-自动化验收测试指南.md) 获取完整配置和故障排除信息。

---

**💡 提示**: 这些测试脚本基于 SP1 任务说明书的验收标准设计，是开发团队的工作目标和验收依据。