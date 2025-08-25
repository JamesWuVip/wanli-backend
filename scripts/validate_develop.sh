#!/bin/bash

# 万里书院前端项目 - Develop分支完整验证脚本
# 用于合并到develop分支前的完整质量检查

set -e  # 遇到错误立即退出

echo "[INFO] 开始Develop分支验证流程..."
echo "[INFO] ======================================"

# 检查当前分支
CURRENT_BRANCH=$(git branch --show-current)
echo "[INFO] 当前分支: $CURRENT_BRANCH ✓"

# 检查工作区状态
if ! git diff-index --quiet HEAD --; then
    echo "[ERROR] 工作区有未提交的更改，请先提交或暂存更改"
    git status --porcelain
    exit 1
fi
echo "[SUCCESS] 工作区状态检查通过"

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

# 5. E2E测试
echo "[INFO] 运行E2E测试..."
# 启动开发服务器（后台运行）
npm run dev &
DEV_PID=$!

# 等待服务器启动
sleep 5

# 运行E2E测试
if npm run test:e2e; then
    echo "[SUCCESS] E2E测试通过"
else
    echo "[ERROR] E2E测试失败"
    kill $DEV_PID 2>/dev/null || true
    exit 1
fi

# 关闭开发服务器
kill $DEV_PID 2>/dev/null || true

# 6. 多环境构建测试
echo "[INFO] 运行多环境构建测试..."
echo "[INFO] 构建开发环境..."
npm run build:dev > /dev/null 2>&1
echo "[SUCCESS] 开发环境构建通过"

echo "[INFO] 构建测试环境..."
npm run build:test > /dev/null 2>&1
echo "[SUCCESS] 测试环境构建通过"

echo "[INFO] 构建生产环境..."
npm run build:prod > /dev/null 2>&1
echo "[SUCCESS] 生产环境构建通过"

echo "[SUCCESS] ======================================"
echo "[SUCCESS] Develop分支验证完成！"
echo "[SUCCESS] 所有检查项目均已通过，代码质量优秀。"
echo "[SUCCESS] 可以安全地合并到staging分支。"
echo "[SUCCESS] ======================================"