# 万里在线教育平台 - 后端API文档

## 概述

万里在线教育平台后端API提供用户认证、课程管理和课时管理功能。本文档详细描述了所有可用的API端点、请求格式和响应格式。

## 基础信息

- **基础URL**: `http://localhost:8080`
- **认证方式**: JWT Bearer Token
- **内容类型**: `application/json`

## 认证

### 用户注册

**端点**: `POST /api/auth/register`

**描述**: 注册新用户账户

**请求体**:
```json
{
  "username": "string",
  "password": "string",
  "email": "string",
  "role": "student|teacher|admin"
}
```

**响应**:
- **201 Created**: 注册成功
```json
{
  "success": true,
  "message": "用户注册成功",
  "user": {
    "id": "uuid",
    "username": "string",
    "email": "string",
    "role": "string",
    "created_at": "datetime",
    "updated_at": "datetime"
  }
}
```

- **400 Bad Request**: 请求参数错误或用户已存在
```json
{
  "success": false,
  "message": "错误信息"
}
```

### 用户登录

**端点**: `POST /api/auth/login`

**描述**: 用户登录获取JWT令牌

**请求体**:
```json
{
  "username": "string",
  "password": "string"
}
```

**响应**:
- **200 OK**: 登录成功
```json
{
  "success": true,
  "message": "登录成功",
  "token": "jwt_token_string",
  "user": {
    "id": "uuid",
    "username": "string",
    "email": "string",
    "role": "string"
  }
}
```

- **401 Unauthorized**: 用户名或密码错误
```json
{
  "success": false,
  "message": "用户名或密码错误"
}
```

## 课程管理

### 创建课程

**端点**: `POST /api/courses`

**描述**: 创建新课程（需要教师或管理员权限）

**请求头**:
```
Authorization: Bearer <jwt_token>
Content-Type: application/json
```

**请求体**:
```json
{
  "title": "string",
  "description": "string",
  "status": "DRAFT|PUBLISHED|ARCHIVED"
}
```

**响应**:
- **201 Created**: 课程创建成功
```json
{
  "success": true,
  "message": "课程创建成功",
  "course": {
    "id": "uuid",
    "title": "string",
    "description": "string",
    "status": "string",
    "creator_id": "uuid",
    "created_at": "datetime",
    "updated_at": "datetime"
  }
}
```

- **403 Forbidden**: 权限不足
```json
{
  "success": false,
  "message": "权限不足，只有教师和管理员可以创建课程"
}
```

### 获取课程列表

**端点**: `GET /api/courses`

**描述**: 获取所有课程列表

**响应**:
- **200 OK**: 获取成功
```json
{
  "success": true,
  "courses": [
    {
      "id": "uuid",
      "title": "string",
      "description": "string",
      "status": "string",
      "creator_id": "uuid",
      "created_at": "datetime",
      "updated_at": "datetime"
    }
  ]
}
```

### 更新课程

**端点**: `PUT /api/courses/{courseId}`

**描述**: 更新课程信息（需要课程创建者或管理员权限）

**请求头**:
```
Authorization: Bearer <jwt_token>
Content-Type: application/json
```

**请求体**:
```json
{
  "title": "string",
  "description": "string",
  "status": "DRAFT|PUBLISHED|ARCHIVED"
}
```

**响应**:
- **200 OK**: 更新成功
```json
{
  "success": true,
  "message": "课程更新成功",
  "course": {
    "id": "uuid",
    "title": "string",
    "description": "string",
    "status": "string",
    "creator_id": "uuid",
    "created_at": "datetime",
    "updated_at": "datetime"
  }
}
```

- **404 Not Found**: 课程不存在
```json
{
  "success": false,
  "message": "课程不存在"
}
```

## 课时管理

### 创建课时

**端点**: `POST /api/courses/{courseId}/lessons`

**描述**: 为指定课程创建课时（需要课程创建者或管理员权限）

**请求头**:
```
Authorization: Bearer <jwt_token>
Content-Type: application/json
```

**请求体**:
```json
{
  "title": "string",
  "order_index": "integer"
}
```

**响应**:
- **201 Created**: 课时创建成功
```json
{
  "id": "uuid",
  "course_id": "uuid",
  "title": "string",
  "order_index": "integer",
  "created_at": "datetime",
  "updated_at": "datetime"
}
```

- **400 Bad Request**: 请求参数错误
```json
{
  "success": false,
  "message": "该课程中已存在相同标题的课时"
}
```

### 获取课时列表

**端点**: `GET /api/courses/{courseId}/lessons`

**描述**: 获取指定课程的所有课时

**响应**:
- **200 OK**: 获取成功
```json
[
  {
    "id": "uuid",
    "course_id": "uuid",
    "title": "string",
    "order_index": "integer",
    "created_at": "datetime",
    "updated_at": "datetime"
  }
]
```

- **404 Not Found**: 课程不存在
```json
{
  "success": false,
  "message": "课程不存在"
}
```

## 错误处理

### 通用错误响应

- **400 Bad Request**: 请求参数错误
- **401 Unauthorized**: 未认证或令牌无效
- **403 Forbidden**: 权限不足
- **404 Not Found**: 资源不存在
- **500 Internal Server Error**: 服务器内部错误

### 错误响应格式
```json
{
  "success": false,
  "message": "错误描述信息"
}
```

## 数据模型

### User（用户）
- `id`: UUID - 用户唯一标识
- `username`: String - 用户名（唯一）
- `password`: String - 密码（加密存储）
- `email`: String - 邮箱地址（唯一）
- `role`: String - 用户角色（student/teacher/admin）
- `created_at`: DateTime - 创建时间
- `updated_at`: DateTime - 更新时间
- `deleted_at`: DateTime - 删除时间（软删除）

### Course（课程）
- `id`: UUID - 课程唯一标识
- `title`: String - 课程标题
- `description`: String - 课程描述
- `status`: String - 课程状态（DRAFT/PUBLISHED/ARCHIVED）
- `creator_id`: UUID - 创建者ID
- `created_at`: DateTime - 创建时间
- `updated_at`: DateTime - 更新时间
- `deleted_at`: DateTime - 删除时间（软删除）

### Lesson（课时）
- `id`: UUID - 课时唯一标识
- `course_id`: UUID - 所属课程ID
- `title`: String - 课时标题
- `order_index`: Integer - 排序索引
- `created_at`: DateTime - 创建时间
- `updated_at`: DateTime - 更新时间
- `deleted_at`: DateTime - 删除时间（软删除）

## 认证说明

1. 用户注册后需要登录获取JWT令牌
2. 需要认证的API请求必须在请求头中包含：`Authorization: Bearer <jwt_token>`
3. JWT令牌包含用户信息，用于权限验证
4. 令牌过期后需要重新登录获取新令牌

## 权限说明

- **学生（student）**: 只能查看课程和课时信息
- **教师（teacher）**: 可以创建、更新自己的课程和课时
- **管理员（admin）**: 拥有所有权限，可以管理所有用户、课程和课时

## 开发环境

- **Java**: 17+
- **Spring Boot**: 3.x
- **数据库**: H2（开发）/ PostgreSQL（生产）
- **认证**: JWT
- **构建工具**: Maven