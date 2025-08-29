# 万里书院后台管理系统 API 文档

## 概述

万里书院后台管理系统提供了完整的用户管理、认证授权等功能的RESTful API接口。万里书院专注于5年级到初中的中小学语文课程，提供短期班和常规课两种课程类型，部分课程适合多个年级学习。本文档详细描述了所有可用的API端点、请求参数、响应格式以及错误处理机制。

**基础信息：**
- API版本：1.0.0
- 基础URL：`http://localhost:8080`
- 内容类型：`application/json`
- 字符编码：`UTF-8`

## 认证机制

系统采用JWT（JSON Web Token）进行身份认证。除了公开接口外，所有API请求都需要在请求头中包含有效的JWT令牌。

**请求头格式：**
```
Authorization: Bearer <your-jwt-token>
```

## 统一响应格式

所有API接口都采用统一的响应格式：

```json
{
  "success": true,
  "code": "200",
  "message": "操作成功",
  "data": {},
  "timestamp": 1640995200000
}
```

**响应字段说明：**
- `success`：布尔值，表示请求是否成功
- `code`：字符串，响应状态码
- `message`：字符串，响应消息
- `data`：对象，响应数据（成功时包含具体数据，失败时为null）
- `timestamp`：长整型，响应时间戳

## 数据模型

### 用户角色枚举 (UserRole)

| 值 | 显示名称 | 权限标识 | 描述 |
|---|---|---|---|
| `HQ_TEACHER` | 总部教师 | ROLE_HQ_TEACHER | 总部教师角色 |
| `BRANCH_TEACHER` | 分校教师 | ROLE_BRANCH_TEACHER | 分校教师角色 |
| `STUDENT` | 学生 | ROLE_STUDENT | 学生角色 |
| `ADMIN` | 管理员 | ROLE_ADMIN | 系统管理员角色 |

### 用户状态枚举 (UserStatus)

| 值 | 显示名称 | 描述 |
|---|---|---|
| `ACTIVE` | 激活 | 用户账户正常激活状态 |
| `INACTIVE` | 未激活 | 用户账户未激活状态 |
| `LOCKED` | 锁定 | 用户账户被锁定状态 |
| `DELETED` | 已删除 | 用户账户已删除状态 |

### 用户信息响应 (UserResponseDto)

```json
{
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "username": "john_doe",
  "email": "john@example.com",
  "fullName": "张三",
  "role": "STUDENT",
  "status": "ACTIVE",
  "createdAt": "2024-01-15T10:30:00+08:00",
  "lastLoginAt": "2024-01-20T14:25:00+08:00",
  "isActive": true
}
```

## API 接口详情

## 1. 认证相关接口

### 1.1 用户注册

**接口地址：** `POST /api/auth/register`

**接口描述：** 用户注册接口，创建新的用户账户

**请求参数：**

```json
{
  "username": "john_doe",
  "password": "password123",
  "email": "john@example.com",
  "fullName": "张三",
  "role": "STUDENT"
}
```

**参数说明：**

| 参数名 | 类型 | 必填 | 验证规则 | 描述 |
|---|---|---|---|---|
| username | String | 是 | 3-50字符，只能包含字母、数字和下划线 | 用户名 |
| password | String | 是 | 6-100字符 | 密码 |
| email | String | 是 | 有效的邮箱格式 | 邮箱地址 |
| fullName | String | 是 | 最大100字符 | 用户姓名 |
| role | String | 否 | 枚举值：HQ_TEACHER, BRANCH_TEACHER, STUDENT, ADMIN | 用户角色，默认为STUDENT |

**成功响应：**

```json
{
  "success": true,
  "code": "200",
  "message": "用户注册成功",
  "data": {
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "username": "john_doe",
    "email": "john@example.com",
    "fullName": "张三",
    "role": "STUDENT",
    "status": "ACTIVE",
    "createdAt": "2024-01-15T10:30:00+08:00",
    "lastLoginAt": null,
    "isActive": true
  },
  "timestamp": 1640995200000
}
```

**错误响应示例：**

```json
{
  "success": false,
  "code": "USER_001",
  "message": "用户名已存在",
  "data": null,
  "timestamp": 1640995200000
}
```

### 1.2 用户登录

**接口地址：** `POST /api/auth/login`

**接口描述：** 用户登录接口，验证用户凭据并返回JWT令牌

**请求参数：**

```json
{
  "username": "john_doe",
  "password": "password123"
}
```

**参数说明：**

| 参数名 | 类型 | 必填 | 描述 |
|---|---|---|---|
| username | String | 是 | 用户名 |
| password | String | 是 | 密码 |

**成功响应：**

```json
{
  "success": true,
  "code": "200",
  "message": "登录成功",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 3600,
    "user": {
      "userId": "550e8400-e29b-41d4-a716-446655440000",
      "username": "john_doe",
      "email": "john@example.com",
      "fullName": "张三",
      "role": "STUDENT",
      "lastLoginAt": "2024-01-20T14:25:00+08:00"
    }
  },
  "timestamp": 1640995200000
}
```

### 1.3 获取当前用户信息

**接口地址：** `GET /api/auth/me`

**接口描述：** 获取当前登录用户的详细信息

**请求头：**
```
Authorization: Bearer <your-jwt-token>
```