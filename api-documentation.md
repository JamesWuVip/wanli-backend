# 万里教育后台管理系统 API 文档

## 概述

万里教育后台管理系统提供了完整的用户管理、认证授权等功能的RESTful API接口。本文档详细描述了所有可用的API端点、请求参数、响应格式以及错误处理机制。

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

**成功响应：**

```json
{
  "success": true,
  "code": "200",
  "message": "操作成功",
  "data": {
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "username": "john_doe",
    "email": "john@example.com",
    "fullName": "张三",
    "role": "STUDENT",
    "status": "ACTIVE",
    "createdAt": "2024-01-15T10:30:00+08:00",
    "lastLoginAt": "2024-01-20T14:25:00+08:00",
    "isActive": true
  },
  "timestamp": 1640995200000
}
```

### 1.4 用户登出

**接口地址：** `POST /api/auth/logout`

**接口描述：** 用户登出接口，使JWT令牌失效

**请求头：**
```
Authorization: Bearer <your-jwt-token>
```

**成功响应：**

```json
{
  "success": true,
  "code": "200",
  "message": "登出成功",
  "data": null,
  "timestamp": 1640995200000
}
```

## 2. 用户管理接口

### 2.1 创建用户

**接口地址：** `POST /users`

**接口描述：** 管理员创建新用户（需要管理员权限）

**权限要求：** ADMIN角色

**请求参数：**

```json
{
  "username": "jane_doe",
  "password": "password123",
  "email": "jane@example.com",
  "fullName": "李四",
  "role": "BRANCH_TEACHER"
}
```

**参数说明：**

| 参数名 | 类型 | 必填 | 验证规则 | 描述 |
|---|---|---|---|---|
| username | String | 是 | 3-20字符，只能包含字母、数字和下划线 | 用户名 |
| password | String | 是 | 6-20字符 | 密码 |
| email | String | 是 | 有效的邮箱格式 | 邮箱地址 |
| fullName | String | 是 | 最大50字符 | 用户姓名 |
| role | String | 否 | 枚举值：HQ_TEACHER, BRANCH_TEACHER, STUDENT, ADMIN | 用户角色，默认为STUDENT |

**成功响应：**

```json
{
  "success": true,
  "code": "200",
  "message": "用户创建成功",
  "data": {
    "userId": "550e8400-e29b-41d4-a716-446655440001",
    "username": "jane_doe",
    "email": "jane@example.com",
    "fullName": "李四",
    "role": "BRANCH_TEACHER",
    "status": "ACTIVE",
    "createdAt": "2024-01-15T10:30:00+08:00",
    "lastLoginAt": null,
    "isActive": true
  },
  "timestamp": 1640995200000
}
```

### 2.2 根据ID获取用户

**接口地址：** `GET /users/{id}`

**接口描述：** 根据用户ID获取用户详细信息

**路径参数：**

| 参数名 | 类型 | 必填 | 描述 |
|---|---|---|---|
| id | UUID | 是 | 用户ID |

**成功响应：**

```json
{
  "success": true,
  "code": "200",
  "message": "操作成功",
  "data": {
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "username": "john_doe",
    "email": "john@example.com",
    "fullName": "张三",
    "role": "STUDENT",
    "status": "ACTIVE",
    "createdAt": "2024-01-15T10:30:00+08:00",
    "lastLoginAt": "2024-01-20T14:25:00+08:00",
    "isActive": true
  },
  "timestamp": 1640995200000
}
```

### 2.3 获取所有用户

**接口地址：** `GET /users`

**接口描述：** 获取所有用户列表（需要管理员权限）

**权限要求：** ADMIN角色

**成功响应：**

```json
{
  "success": true,
  "code": "200",
  "message": "操作成功",
  "data": [
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
    },
    {
      "userId": "550e8400-e29b-41d4-a716-446655440001",
      "username": "jane_doe",
      "email": "jane@example.com",
      "fullName": "李四",
      "role": "BRANCH_TEACHER",
      "status": "ACTIVE",
      "createdAt": "2024-01-15T11:30:00+08:00",
      "lastLoginAt": null,
      "isActive": true
    }
  ],
  "timestamp": 1640995200000
}
```

### 2.4 更新用户信息

**接口地址：** `PUT /users/{id}`

**接口描述：** 更新指定用户的信息（需要管理员权限）

**权限要求：** ADMIN角色

**路径参数：**

| 参数名 | 类型 | 必填 | 描述 |
|---|---|---|---|
| id | UUID | 是 | 用户ID |

**请求参数：**

```json
{
  "email": "newemail@example.com",
  "fullName": "新姓名",
  "role": "HQ_TEACHER"
}
```

**参数说明：**

| 参数名 | 类型 | 必填 | 验证规则 | 描述 |
|---|---|---|---|---|
| email | String | 否 | 有效的邮箱格式 | 邮箱地址 |
| fullName | String | 否 | 最大100字符 | 用户姓名 |
| role | String | 否 | 枚举值：HQ_TEACHER, BRANCH_TEACHER, STUDENT, ADMIN | 用户角色 |

**成功响应：**

```json
{
  "success": true,
  "code": "200",
  "message": "用户信息更新成功",
  "data": {
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "username": "john_doe",
    "email": "newemail@example.com",
    "fullName": "新姓名",
    "role": "HQ_TEACHER",
    "status": "ACTIVE",
    "createdAt": "2024-01-15T10:30:00+08:00",
    "lastLoginAt": "2024-01-20T14:25:00+08:00",
    "isActive": true
  },
  "timestamp": 1640995200000
}
```

### 2.5 删除用户

**接口地址：** `DELETE /users/{id}`

**接口描述：** 删除指定用户（需要管理员权限）

**权限要求：** ADMIN角色

**路径参数：**

| 参数名 | 类型 | 必填 | 描述 |
|---|---|---|---|
| id | UUID | 是 | 用户ID |

**成功响应：**

```json
{
  "success": true,
  "code": "200",
  "message": "用户删除成功",
  "data": null,
  "timestamp": 1640995200000
}
```

### 2.6 根据用户名获取用户

**接口地址：** `GET /users/username/{username}`

**接口描述：** 根据用户名获取用户详细信息

**路径参数：**

| 参数名 | 类型 | 必填 | 描述 |
|---|---|---|---|
| username | String | 是 | 用户名 |

**成功响应：**

```json
{
  "success": true,
  "code": "200",
  "message": "操作成功",
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "username": "john_doe",
    "passwordHash": "$2a$10$...",
    "email": "john@example.com",
    "fullName": "张三",
    "role": "STUDENT",
    "status": "ACTIVE",
    "lastLoginAt": "2024-01-20T14:25:00+08:00",
    "loginAttempts": 0,
    "lockedUntil": null,
    "createdAt": "2024-01-15T10:30:00+08:00",
    "updatedAt": "2024-01-20T14:25:00+08:00",
    "createdBy": "system",
    "updatedBy": "john_doe",
    "active": true,
    "locked": false
  },
  "timestamp": 1640995200000
}
```

### 2.7 检查用户名是否存在

**接口地址：** `GET /users/check/username/{username}`

**接口描述：** 检查指定用户名是否已存在

**路径参数：**

| 参数名 | 类型 | 必填 | 描述 |
|---|---|---|---|
| username | String | 是 | 要检查的用户名 |

**成功响应：**

```json
{
  "success": true,
  "code": "200",
  "message": "操作成功",
  "data": true,
  "timestamp": 1640995200000
}
```

**响应数据说明：**
- `true`：用户名已存在
- `false`：用户名不存在

### 2.8 检查邮箱是否存在

**接口地址：** `GET /users/check/email/{email}`

**接口描述：** 检查指定邮箱是否已存在

**路径参数：**

| 参数名 | 类型 | 必填 | 描述 |
|---|---|---|---|
| email | String | 是 | 要检查的邮箱地址 |

**成功响应：**

```json
{
  "success": true,
  "code": "200",
  "message": "操作成功",
  "data": false,
  "timestamp": 1640995200000
}
```

**响应数据说明：**
- `true`：邮箱已存在
- `false`：邮箱不存在

## 3. 系统健康检查接口

### 3.1 健康检查

**接口地址：** `GET /api/health`

**接口描述：** 系统健康状态检查

**成功响应：**

```json
{
  "success": true,
  "code": "200",
  "message": "操作成功",
  "data": {
    "status": "UP",
    "timestamp": "2024-01-20T14:25:00+08:00"
  },
  "timestamp": 1640995200000
}
```

### 3.2 系统信息

**接口地址：** `GET /api/info`

**接口描述：** 获取系统基本信息

**成功响应：**

```json
{
  "success": true,
  "code": "200",
  "message": "操作成功",
  "data": {
    "application": "wanli-backend",
    "profile": "default",
    "version": "1.0.0",
    "javaVersion": "17.0.2",
    "osName": "Mac OS X",
    "osVersion": "10.15.7",
    "timestamp": "2024-01-20T14:25:00"
  },
  "timestamp": 1640995200000
}
```

## 错误码说明

### 通用错误码

| 错误码 | HTTP状态码 | 描述 |
|---|---|---|
| 200 | 200 | 操作成功 |
| 400 | 400 | 请求参数错误 |
| 401 | 401 | 未授权访问 |
| 403 | 403 | 权限不足 |
| 404 | 404 | 资源不存在 |
| 500 | 500 | 系统内部错误 |

### 业务错误码

| 错误码 | HTTP状态码 | 描述 |
|---|---|---|
| USER_001 | 400 | 用户名已存在 |
| USER_002 | 400 | 邮箱已存在 |
| USER_003 | 404 | 用户不存在 |
| USER_004 | 400 | 密码无效 |
| USER_005 | 400 | 用户账户已锁定 |
| AUTH_001 | 401 | 认证失败 |
| AUTH_002 | 401 | JWT令牌无效 |
| AUTH_003 | 401 | JWT令牌已过期 |
| VALIDATION_001 | 400 | 参数验证失败 |
| DATABASE_001 | 500 | 数据库操作失败 |
| SYSTEM_001 | 500 | 系统内部错误 |

## 常见错误响应示例

### 参数验证失败

```json
{
  "success": false,
  "code": "VALIDATION_001",
  "message": "参数验证失败: 用户名不能为空",
  "data": null,
  "timestamp": 1640995200000
}
```

### 用户不存在

```json
{
  "success": false,
  "code": "USER_003",
  "message": "用户不存在",
  "data": null,
  "timestamp": 1640995200000
}
```

### 权限不足

```json
{
  "success": false,
  "code": "403",
  "message": "权限不足",
  "data": null,
  "timestamp": 1640995200000
}
```

### JWT令牌无效

```json
{
  "success": false,
  "code": "AUTH_002",
  "message": "JWT令牌无效",
  "data": null,
  "timestamp": 1640995200000
}
```

## 使用示例

### 用户注册和登录流程

1. **用户注册**

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "password": "password123",
    "email": "john@example.com",
    "fullName": "张三",
    "role": "STUDENT"
  }'
```

2. **用户登录**

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "password": "password123"
  }'
```

3. **获取当前用户信息**

```bash
curl -X GET http://localhost:8080/api/auth/me \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

4. **用户登出**

```bash
curl -X POST http://localhost:8080/api/auth/logout \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

### 管理员操作示例

1. **创建用户（管理员）**

```bash
curl -X POST http://localhost:8080/users \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <admin-jwt-token>" \
  -d '{
    "username": "teacher01",
    "password": "teacher123",
    "email": "teacher@example.com",
    "fullName": "王老师",
    "role": "HQ_TEACHER"
  }'
```

2. **获取所有用户（管理员）**

```bash
curl -X GET http://localhost:8080/users \
  -H "Authorization: Bearer <admin-jwt-token>"
```

3. **更新用户信息（管理员）**

```bash
curl -X PUT http://localhost:8080/users/550e8400-e29b-41d4-a716-446655440000 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <admin-jwt-token>" \
  -d '{
    "email": "newemail@example.com",
    "fullName": "新姓名",
    "role": "HQ_TEACHER"
  }'
```

## 课程管理接口

### 创建课程
- **接口地址**: `POST /api/courses`
- **接口描述**: 创建新课程
- **权限要求**: 需要认证，ADMIN或TEACHER角色
- **请求参数**:
```json
{
  "courseCode": "MATH001",
  "title": "小学数学基础",
  "description": "适合一年级学生的数学基础课程",
  "gradeLevel": "GRADE_1",
  "subject": "MATH",
  "institutionId": "550e8400-e29b-41d4-a716-446655440000"
}
```
- **参数说明**:
  - `courseCode`: 课程编码，必填，唯一，最大20字符
  - `title`: 课程标题，必填，最大100字符
  - `description`: 课程描述，选填，最大500字符
  - `gradeLevel`: 年级等级，必填，枚举值：GRADE_1~GRADE_6
  - `subject`: 学科，必填，枚举值：CHINESE、MATH、ENGLISH
  - `institutionId`: 所属机构ID，选填，UUID格式
- **成功响应**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "courseCode": "MATH001",
    "title": "小学数学基础",
    "description": "适合一年级学生的数学基础课程",
    "gradeLevel": "GRADE_1",
    "subject": "MATH",
    "isActive": true,
    "lessonCount": 0,
    "createdAt": "2024-01-15T10:30:00.000Z",
    "updatedAt": "2024-01-15T10:30:00.000Z",
    "createdBy": "admin"
  }
}
```

### 获取课程列表
- **接口地址**: `GET /api/courses`
- **接口描述**: 分页获取课程列表
- **权限要求**: 需要认证
- **请求参数**:
  - `page`: 页码，默认0
  - `size`: 页大小，默认10，最大100
  - `gradeLevel`: 年级过滤，选填
  - `subject`: 学科过滤，选填
  - `isActive`: 状态过滤，选填
- **成功响应**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "content": [
      {
        "id": "550e8400-e29b-41d4-a716-446655440000",
        "courseCode": "MATH001",
        "title": "小学数学基础",
        "description": "适合一年级学生的数学基础课程",
        "gradeLevel": "GRADE_1",
        "subject": "MATH",
        "isActive": true,
        "lessonCount": 0,
        "createdAt": "2024-01-15T10:30:00.000Z",
        "updatedAt": "2024-01-15T10:30:00.000Z",
        "createdBy": "admin"
      }
    ],
    "pageable": {
      "pageNumber": 0,
      "pageSize": 10
    },
    "totalElements": 1,
    "totalPages": 1,
    "first": true,
    "last": true
  }
}
```

### 获取课程详情
- **接口地址**: `GET /api/courses/{courseId}`
- **接口描述**: 根据ID获取课程详情
- **权限要求**: 需要认证
- **路径参数**:
  - `courseId`: 课程ID，UUID格式
- **成功响应**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "courseCode": "MATH001",
    "title": "小学数学基础",
    "description": "适合一年级学生的数学基础课程",
    "gradeLevel": "GRADE_1",
    "subject": "MATH",
    "isActive": true,
    "lessonCount": 0,
    "createdAt": "2024-01-15T10:30:00.000Z",
    "updatedAt": "2024-01-15T10:30:00.000Z",
    "createdBy": "admin"
  }
}
```

### 更新课程信息
- **接口地址**: `PUT /api/courses/{courseId}`
- **接口描述**: 更新课程信息
- **权限要求**: 需要认证，ADMIN或TEACHER角色
- **路径参数**:
  - `courseId`: 课程ID，UUID格式
- **请求参数**:
```json
{
  "title": "小学数学进阶",
  "description": "适合一年级学生的数学进阶课程",
  "gradeLevel": "GRADE_2",
  "subject": "MATH"
}
```
- **成功响应**: 同获取课程详情

### 删除课程
- **接口地址**: `DELETE /api/courses/{courseId}`
- **接口描述**: 删除课程（软删除）
- **权限要求**: 需要认证，ADMIN角色
- **路径参数**:
  - `courseId`: 课程ID，UUID格式
- **成功响应**:
```json
{
  "code": 200,
  "message": "课程删除成功",
  "data": null
}
```

### 切换课程状态
- **接口地址**: `PATCH /api/courses/{courseId}/status`
- **接口描述**: 切换课程激活状态
- **权限要求**: 需要认证，ADMIN或TEACHER角色
- **路径参数**:
  - `courseId`: 课程ID，UUID格式
- **成功响应**: 同获取课程详情

## 机构管理接口

### 创建机构
- **接口地址**: `POST /api/institutions`
- **接口描述**: 创建新机构
- **权限要求**: 需要认证，ADMIN角色
- **请求参数**:
```json
{
  "name": "万里教育总部",
  "code": "HQ001",
  "type": "HEADQUARTERS",
  "address": "北京市朝阳区教育大厦",
  "contactPhone": "010-12345678",
  "contactEmail": "hq@wanli.edu.cn",
  "description": "万里教育集团总部机构"
}
```
- **参数说明**:
  - `name`: 机构名称，必填，最大100字符
  - `code`: 机构编码，必填，唯一，最大20字符
  - `type`: 机构类型，必填，枚举值：HEADQUARTERS、BRANCH
  - `address`: 机构地址，选填，最大200字符
  - `contactPhone`: 联系电话，选填，最大20字符
  - `contactEmail`: 联系邮箱，选填，有效邮箱格式
  - `description`: 机构描述，选填，最大500字符
- **成功响应**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "name": "万里教育总部",
    "code": "HQ001",
    "type": "HEADQUARTERS",
    "address": "北京市朝阳区教育大厦",
    "contactPhone": "010-12345678",
    "contactEmail": "hq@wanli.edu.cn",
    "description": "万里教育集团总部机构",
    "isActive": true,
    "createdAt": "2024-01-15T10:30:00.000Z",
    "updatedAt": "2024-01-15T10:30:00.000Z",
    "createdBy": "admin"
  }
}
```

### 获取机构详情
- **接口地址**: `GET /api/institutions/{id}`
- **接口描述**: 根据ID获取机构详情
- **权限要求**: 需要认证
- **路径参数**:
  - `id`: 机构ID，UUID格式
- **成功响应**: 同创建机构响应

### 获取机构列表
- **接口地址**: `GET /api/institutions`
- **接口描述**: 分页获取机构列表
- **权限要求**: 需要认证
- **请求参数**:
  - `page`: 页码，默认0
  - `size`: 页大小，默认10，最大100
  - `type`: 机构类型过滤，选填
  - `isActive`: 状态过滤，选填
- **成功响应**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "content": [
      {
        "id": "550e8400-e29b-41d4-a716-446655440000",
        "name": "万里教育总部",
        "code": "HQ001",
        "type": "HEADQUARTERS",
        "address": "北京市朝阳区教育大厦",
        "contactPhone": "010-12345678",
        "contactEmail": "hq@wanli.edu.cn",
        "description": "万里教育集团总部机构",
        "isActive": true,
        "createdAt": "2024-01-15T10:30:00.000Z",
        "updatedAt": "2024-01-15T10:30:00.000Z",
        "createdBy": "admin"
      }
    ],
    "pageable": {
      "pageNumber": 0,
      "pageSize": 10
    },
    "totalElements": 1,
    "totalPages": 1,
    "first": true,
    "last": true
  }
}
```

### 更新机构信息
- **接口地址**: `PUT /api/institutions/{id}`
- **接口描述**: 更新机构信息
- **权限要求**: 需要认证，ADMIN角色
- **路径参数**:
  - `id`: 机构ID，UUID格式
- **请求参数**: 同创建机构（除code字段外）
- **成功响应**: 同创建机构响应

### 切换机构状态
- **接口地址**: `PATCH /api/institutions/{id}/status`
- **接口描述**: 切换机构激活状态
- **权限要求**: 需要认证，ADMIN角色
- **路径参数**:
  - `id`: 机构ID，UUID格式
- **成功响应**: 同创建机构响应

### 删除机构
- **接口地址**: `DELETE /api/institutions/{id}`
- **接口描述**: 删除机构（软删除）
- **权限要求**: 需要认证，ADMIN角色
- **路径参数**:
  - `id`: 机构ID，UUID格式
- **成功响应**:
```json
{
  "code": 200,
  "message": "机构删除成功",
  "data": null
}
```

### 获取活跃机构列表
- **接口地址**: `GET /api/institutions/active`
- **接口描述**: 获取所有活跃状态的机构列表
- **权限要求**: 需要认证
- **成功响应**: 返回机构数组，格式同获取机构列表中的content字段

### 获取机构统计信息
- **接口地址**: `GET /api/institutions/statistics`
- **接口描述**: 获取机构统计信息
- **权限要求**: 需要认证，ADMIN角色
- **成功响应**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "totalInstitutions": 10,
    "activeInstitutions": 8,
    "inactiveInstitutions": 2,
    "headquartersCount": 1,
    "branchCount": 9
  }
}
```

## 版本历史

### v1.0.0 (2024-01-15)
- 初始版本发布
- 实现用户管理、认证授权基础功能
- 添加系统健康检查接口
- 完善API文档和错误处理机制
- 实现课程管理功能
- 实现机构管理功能
- 修复系统错误和优化性能

---

**文档更新时间：** 2024-01-20 14:30:00  
**文档版本：** v1.0.0  
**维护人员：** 万里教育技术团队