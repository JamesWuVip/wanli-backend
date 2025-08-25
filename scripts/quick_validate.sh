#!/bin/bash

# 万里书院前端项目 - 快速验证脚本
# 用于开发过程中的快速代码质量检查

set -e  # 遇到错误立即退出

echo "[INFO] 开始快速验证流程..."
echo "[INFO] ============================"

# 1. 代码格式检查
echo "[INFO] 运行代码格式检查..."
npm run format:check
echo "[SUCCESS] 代码格式检查通过"

# 2. ESLint检查
echo "[INFO] 运行ESLint检查..."
npm run lint
echo "[SUCCESS] ESLint检查通过"

# 3. TypeScript类型检查
echo "[INFO] 运行TypeScript类型检查..."
npm run type-check
echo "[SUCCESS] TypeScript类型检查通过"

# 4. 单元测试
echo "[INFO] 运行单元测试..."
npm run test:unit
echo "[SUCCESS] 单元测试通过"

# 5. 快速构建检查
echo "[INFO] 运行快速构建检查..."
npm run build:dev > /dev/null 2>&1
echo "[SUCCESS] 快速构建检查通过"

echo "[SUCCESS] ============================"
echo "[SUCCESS] 快速验证完成！代码质量良好，可以提交。"
echo "[SUCCESS] ============================"