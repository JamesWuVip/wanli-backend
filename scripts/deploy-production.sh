#!/bin/bash
set -e

echo "🚀 开始部署到生产环境..."
echo "⚠️  警告：即将部署到生产环境，请确认所有测试已通过！"
read -p "确认继续部署到生产环境？(y/N): " confirm

if [[ $confirm != [yY] ]]; then
    echo "❌ 部署已取消"
    exit 1
fi

# 切换到 main 分支
echo "🔄 切换到 main 分支..."
git checkout main
git pull origin main

# 合并 staging 分支
echo "🔀 合并 staging 分支到 main..."
git merge staging --no-edit -m "release: 发布staging测试通过的版本到生产环境"

# 推送到远程仓库
echo "📤 推送到远程 main 分支..."
git push origin main

# 创建版本标签
VERSION=$(date +"v%Y.%m.%d-%H%M")
echo "🏷️  创建版本标签: $VERSION"
git tag -a $VERSION -m "Production release $VERSION"
git push origin $VERSION

echo "🎉 生产环境部署完成！版本: $VERSION"