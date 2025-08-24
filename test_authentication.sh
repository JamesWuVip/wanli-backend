#!/bin/bash

# 万里后端认证功能测试脚本
echo "=== 万里后端认证功能测试 ==="
echo

# 服务器地址
BASE_URL="http://localhost:8080"

# 测试1: 访问公开接口（不需要认证）
echo "1. 测试公开接口访问"
echo "访问 /api/health:"
curl -s $BASE_URL/api/health | jq .
echo

echo "访问 /api/welcome:"
curl -s $BASE_URL/api/welcome | jq .
echo
echo

# 测试2: 访问受保护接口（无token）
echo "2. 测试受保护接口访问（无认证token）"
echo "访问 /api/courses（应该返回403）:"
curl -s -w "HTTP状态码: %{http_code}\n" $BASE_URL/api/courses
echo
echo

# 测试3: 用户登录获取token
echo "3. 用户登录获取JWT token"
echo "登录用户: test@example.com"
LOGIN_RESPONSE=$(curl -s -X POST $BASE_URL/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"password123"}')

echo "登录响应:"
echo $LOGIN_RESPONSE | jq .

# 提取token
TOKEN=$(echo $LOGIN_RESPONSE | jq -r '.token')
echo "提取的Token: $TOKEN"
echo
echo

# 测试4: 使用有效token访问受保护接口
echo "4. 使用有效token访问受保护接口"
echo "访问 /api/courses（带有效token）:"
curl -s -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  $BASE_URL/api/courses | jq .
echo
echo

# 测试5: 测试需要特殊权限的接口
echo "5. 测试需要特殊权限的接口"
echo "尝试创建课程（需要ROLE_HQ_TEACHER权限）:"
curl -s -X POST $BASE_URL/api/courses \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"title":"测试课程","description":"这是一个测试课程","status":"DRAFT"}' | jq .
echo
echo

# 测试6: 使用无效token
echo "6. 测试无效token"
echo "使用无效token访问受保护接口:"
curl -s -w "HTTP状态码: %{http_code}\n" \
  -H "Authorization: Bearer invalid_token_here" \
  -H "Content-Type: application/json" \
  $BASE_URL/api/courses
echo
echo

# 测试7: 测试token格式错误
echo "7. 测试token格式错误"
echo "使用错误格式的Authorization header:"
curl -s -w "HTTP状态码: %{http_code}\n" \
  -H "Authorization: invalid_format" \
  -H "Content-Type: application/json" \
  $BASE_URL/api/courses
echo
echo

echo "=== 认证功能测试完成 ==="