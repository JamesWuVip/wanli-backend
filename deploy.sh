#!/bin/bash

# 自动配置文件切换脚本
# 根据当前分支自动选择对应的nixpacks配置文件

# 获取当前分支名
CURRENT_BRANCH=$(git branch --show-current)

echo "当前分支: $CURRENT_BRANCH"

# 根据分支选择配置文件
case $CURRENT_BRANCH in
  "main")
    echo "使用生产环境配置"
    cp nixpacks-prod.toml nixpacks.toml
    ;;
  "staging")
    echo "使用测试环境配置"
    cp nixpacks-staging.toml nixpacks.toml
    ;;
  "dev")
    echo "使用开发环境配置"
    cp nixpacks-dev.toml nixpacks.toml
    ;;
  *)
    echo "未知分支，使用开发环境配置"
    cp nixpacks-dev.toml nixpacks.toml
    ;;
esac

echo "配置文件切换完成"