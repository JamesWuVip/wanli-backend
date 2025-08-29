#!/bin/bash
set -e

echo "🚀 开始部署到 staging 环境..."

# 检查当前分支状态
echo "📋 检查 git 状态..."
git status

# 切换到 staging 分支
echo "🔄 切换到 staging 分支..."
git checkout staging
git pull origin staging

# 合并 dev 分支
echo "🔀 合并 dev 分支到 staging..."
git merge dev --no-edit -m "deploy: 自动部署dev分支到staging环境"

# 推送到远程仓库
echo "📤 推送到远程 staging 分支..."
git push origin staging

# 等待部署完成
echo "⏳ 等待 Railway 部署完成..."
sleep 30

# 验证部署状态
echo "✅ 部署完成！请检查 Railway 控制台确认部署状态"
echo "🌐 Staging 环境地址: https://your-staging-url.railway.app"

echo "🎉 staging 环境部署完成！"