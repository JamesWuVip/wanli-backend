# 万里教育后端系统 API 文档

## 概述

万里教育后端系统提供了完整的教育管理功能，包括用户管理、认证授权、课程管理、机构管理等核心功能。本文档详细描述了所有可用的API接口。

**基础URL**: `http://localhost:8080/api`

## 认证机制

系统使用JWT（JSON Web Token）进行身份认证。除了注册和登录接口外，所有接口都需要在请求头中携带有效的JWT令牌。

**请求头格式**:
```
Authorization: Bearer <your-jwt-token>
Content-Type: application/json
```

## 统一响应格式

所有API接口都遵循统一的响应格式：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {}
}
```

- `code`: 响应状态码（200表示成功，其他表示错误）
- `message`: 响应消息
- `data`: 响应数据（成功时包含具体数据，失败时可能为null）

## 数据模型

### 用户角色
- `ADMIN`: 管理员，拥有所有权限
- `TEACHER`: 教师，可以管理课程和学生
- `STUDENT`: 学生，只能查看自己的信息

### 用户状态
- `ACTIVE`: 活跃用户
- `INACTIVE`: 非活跃用户
- `SUSPENDED`: 暂停用户

### 用户信息响应
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "username": "admin",
  "email": "admin@example.com",
  "fullName": "管理员",
  "phone": "13800138000",
  "role": "ADMIN",
  "status": "ACTIVE",
  "createdAt": "2024-01-15T10:30:00.000Z",
  "updatedAt": "2024-01-15T10:30:00.000Z"
}
```

## 认证相关接口

### 用户注册
- **接口地址**: `POST /auth/register`
- **接口描述**: 用户注册
- **权限要求**: 无需认证
- **请求参数**:
```json
{
  "username": "testuser",
  "password": "password123",
  "email": "test@example.com",
  "fullName": "测试用户",
  "phone": "13800138000"
}
```
- **参数说明**:
  - `username`: 用户名，必填，3-50字符，唯一
  - `password`: 密码，必填，6-100字符
  - `email`: 邮箱，必填，标准邮箱格式，唯一
  - `fullName`: 全名，必填，1-100字符
  - `phone`: 手机号，选填，11位数字
- **成功响应**:
```json
{
  "code": 200,
  "message": "用户注册成功",
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "username": "testuser",
    "email": "test@example.com",
    "fullName": "测试用户",
    "phone": "13800138000",
    "role": "STUDENT",
    "status": "ACTIVE",
    "createdAt": "2024-01-15T10:30:00.000Z",
    "updatedAt": "2024-01-15T10:30:00.000Z"
  }
}
```

### 用户登录
- **接口地址**: `POST /auth/login`
- **接口描述**: 用户登录
- **权限要求**: 无需认证
- **请求参数**:
```json
{
  "username": "admin",
  "password": "admin123"
}
```
- **参数说明**:
  - `username`: 用户名，必填
  - `password`: 密码，必填
- **成功响应**:
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "username": "admin",
      "email": "admin@example.com",
      "fullName": "管理员",
      "phone": "13800138000",
      "role": "ADMIN",
      "status": "ACTIVE",
      "createdAt": "2024-01-15T10:30:00.000Z",
      "updatedAt": "2024-01-15T10:30:00.000Z"
    }
  }
}
```

### 获取当前用户信息
- **接口地址**: `GET /auth/me`
- **接口描述**: 获取当前登录用户的信息
- **权限要求**: 需要认证
- **成功响应**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "username": "admin",
    "email": "admin@example.com",
    "fullName": "管理员",
    "phone": "13800138000",
    "role": "ADMIN",
    "status": "ACTIVE",
    "createdAt": "2024-01-15T10:30:00.000Z",
    "updatedAt": "2024-01-15T10:30:00.000Z"
  }
}
```

### 用户登出
- **接口地址**: `POST /auth/logout`
- **接口描述**: 用户登出
- **权限要求**: 需要认证
- **成功响应**:
```json
{
  "code": 200,
  "message": "登出成功",
  "data": null
}
```

## 用户管理接口

### 创建用户
- **接口地址**: `POST /users`
- **接口描述**: 创建新用户（管理员功能）
- **权限要求**: 需要认证，ADMIN角色
- **请求参数**: 同用户注册接口
- **成功响应**: 同用户注册接口

### 根据ID获取用户
- **接口地址**: `GET /users/{id}`
- **接口描述**: 根据用户ID获取用户信息
- **权限要求**: 需要认证，ADMIN角色或查询自己的信息
- **路径参数**:
  - `id`: 用户ID，UUID格式
- **成功响应**: 同获取当前用户信息

### 获取所有用户
- **接口地址**: `GET /users`
- **接口描述**: 分页获取用户列表
- **权限要求**: 需要认证，ADMIN角色
- **请求参数**:
  - `page`: 页码，默认0
  - `size`: 页大小，默认10，最大100
  - `role`: 角色过滤，选填
  - `status`: 状态过滤，选填
- **成功响应**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "content": [
      {
        "id": "550e8400-e29b-41d4-a716-446655440000",
        "username": "admin",
        "email": "admin@example.com",
        "fullName": "管理员",
        "phone": "13800138000",
        "role": "ADMIN",
        "status": "ACTIVE",
        "createdAt": "2024-01-15T10:30:00.000Z",
        "updatedAt": "2024-01-15T10:30:00.000Z"
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

### 更新用户信息
- **接口地址**: `PUT /users/{id}`
- **接口描述**: 更新用户信息
- **权限要求**: 需要认证，ADMIN角色或更新自己的信息
- **路径参数**:
  - `id`: 用户ID，UUID格式
- **请求参数**:
```json
{
  "email": "newemail@example.com",
  "fullName": "新的全名",
  "phone": "13900139000"
}
```
- **参数说明**:
  - `email`: 邮箱，选填，标准邮箱格式
  - `fullName`: 全名，选填，1-100字符
  - `phone`: 手机号，选填，11位数字
- **成功响应**: 同获取用户信息

### 删除用户
- **接口地址**: `DELETE /users/{id}`
- **接口描述**: 删除用户（软删除）
- **权限要求**: 需要认证，ADMIN角色
- **路径参数**:
  - `id`: 用户ID，UUID格式
- **成功响应**:
```json
{
  "code": 200,
  "message": "用户删除成功",
  "data": null
}
```

### 根据用户名获取用户
- **接口地址**: `GET /users/username/{username}`
- **接口描述**: 根据用户名获取用户信息
- **权限要求**: 需要认证，ADMIN角色
- **路径参数**:
  - `username`: 用户名
- **成功响应**: 同获取用户信息

### 检查用户名是否存在
- **接口地址**: `GET /users/check/username/{username}`
- **接口描述**: 检查用户名是否已存在
- **权限要求**: 无需认证
- **路径参数**:
  - `username`: 用户名
- **成功响应**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "exists": true
  }
}
```

### 检查邮箱是否存在
- **接口地址**: `GET /users/check/email/{email}`
- **接口描述**: 检查邮箱是否已存在
- **权限要求**: 无需认证
- **路径参数**:
  - `email`: 邮箱地址
- **成功响应**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "exists": false
  }
}
```

## 系统接口

### 健康检查
- **接口地址**: `GET /health`
- **接口描述**: 系统健康检查
- **权限要求**: 无需认证
- **成功响应**:
```json
{
  "code": 200,
  "message": "系统运行正常",
  "data": {
    "status": "UP",
    "timestamp": "2024-01-15T10:30:00.000Z"
  }
}
```

### 系统信息
- **接口地址**: `GET /info`
- **接口描述**: 获取系统信息
- **权限要求**: 无需认证
- **成功响应**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "appName": "万里教育后端系统",
    "version": "1.0.0",
    "environment": "development",
    "timestamp": "2024-01-15T10:30:00.000Z"
  }
}
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
- **成功响应**:
```json
{
  "code": 200,
  "message": "课程状态切换成功",
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "isActive": false
  }
}
```

## 机构管理接口

### 创建机构
- **接口地址**: `POST /api/institutions`
- **接口描述**: 创建新的教育机构
- **权限要求**: 需要认证，ADMIN角色
- **请求参数**:
```json
{
  "name": "万里教育机构",
  "description": "专注于小学教育的优质机构",
  "contactEmail": "contact@wanli.edu",
  "contactPhone": "13800138000",
  "address": "北京市朝阳区教育大街123号"
}
```
- **参数说明**:
  - `name`: 机构名称，必填，最大100字符
  - `description`: 机构描述，选填
  - `contactEmail`: 联系邮箱，选填，最大100字符
  - `contactPhone`: 联系电话，选填，最大20字符
  - `address`: 地址，选填
- **成功响应**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "name": "万里教育机构",
    "description": "专注于小学教育的优质机构",
    "contactEmail": "contact@wanli.edu",
    "contactPhone": "13800138000",
    "address": "北京市朝阳区教育大街123号",
    "status": "ACTIVE",
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
  - `status`: 状态过滤，选填，枚举值：ACTIVE、INACTIVE、SUSPENDED
- **成功响应**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "content": [
      {
        "id": "550e8400-e29b-41d4-a716-446655440000",
        "name": "万里教育机构",
        "description": "专注于小学教育的优质机构",
        "contactEmail": "contact@wanli.edu",
        "contactPhone": "13800138000",
        "address": "北京市朝阳区教育大街123号",
        "status": "ACTIVE",
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
- **请求参数**: 同创建机构
- **成功响应**: 同创建机构响应

### 更新机构状态
- **接口地址**: `PATCH /api/institutions/{id}/status`
- **接口描述**: 更新机构状态
- **权限要求**: 需要认证，ADMIN角色
- **路径参数**:
  - `id`: 机构ID，UUID格式
- **请求参数**:
```json
{
  "status": "INACTIVE"
}
```
- **参数说明**:
  - `status`: 机构状态，必填，枚举值：ACTIVE、INACTIVE、SUSPENDED
- **成功响应**:
```json
{
  "code": 200,
  "message": "机构状态更新成功",
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "status": "INACTIVE"
  }
}
```

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
- **成功响应**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "name": "万里教育机构",
      "description": "专注于小学教育的优质机构",
      "contactEmail": "contact@wanli.edu",
      "contactPhone": "13800138000",
      "address": "北京市朝阳区教育大街123号",
      "status": "ACTIVE",
      "createdAt": "2024-01-15T10:30:00.000Z",
      "updatedAt": "2024-01-15T10:30:00.000Z",
      "createdBy": "admin"
    }
  ]
}
```

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
    "inactiveInstitutions": 1,
    "suspendedInstitutions": 1
  }
}
```

## 错误码说明

### 通用错误码
- `400`: 请求参数错误
- `401`: 未认证或认证失败
- `403`: 权限不足
- `404`: 资源不存在
- `409`: 资源冲突（如用户名已存在）
- `500`: 服务器内部错误

### 业务错误码
- `2001`: 用户名已存在
- `2002`: 邮箱已存在
- `2003`: 用户不存在
- `2004`: 密码错误
- `2005`: 用户状态异常
- `3001`: 课程不存在
- `3002`: 课程编码已存在
- `4001`: 机构不存在
- `4002`: 机构状态异常
- `6001`: 数据库操作失败
- `6002`: 数据验证失败

## 常见错误响应示例

### 参数验证失败
```json
{
  "code": 400,
  "message": "参数验证失败",
  "data": {
    "errors": [
      {
        "field": "username",
        "message": "用户名长度必须在3-50个字符之间"
      },
      {
        "field": "email",
        "message": "邮箱格式不正确"
      }
    ]
  }
}
```

### 用户不存在
```json
{
  "code": 404,
  "message": "用户不存在",
  "data": null
}
```

### 权限不足
```json
{
  "code": 403,
  "message": "权限不足，无法访问该资源",
  "data": null
}
```

### JWT令牌无效
```json
{
  "code": 401,
  "message": "JWT令牌无效或已过期",
  "data": null
}
```

## 使用示例

### 用户注册和登录流程

1. **注册新用户**
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password123",
    "email": "test@example.com",
    "fullName": "测试用户",
    "phone": "13800138000"
  }'
```

2. **用户登录**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password123"
  }'
```

3. **使用JWT令牌访问受保护的接口**
```bash
curl -X GET http://localhost:8080/api/auth/me \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

### 管理员操作示例

1. **创建课程**
```bash
curl -X POST http://localhost:8080/api/courses \
  -H "Authorization: Bearer <admin-token>" \
  -H "Content-Type: application/json" \
  -d '{
    "courseCode": "MATH001",
    "title": "小学数学基础",
    "description": "适合一年级学生的数学基础课程",
    "gradeLevel": "GRADE_1",
    "subject": "MATH"
  }'
```

2. **创建机构**
```bash
curl -X POST http://localhost:8080/api/institutions \
  -H "Authorization: Bearer <admin-token>" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "万里教育机构",
    "description": "专注于小学教育的优质机构",
    "contactEmail": "contact@wanli.edu",
    "contactPhone": "13800138000",
    "address": "北京市朝阳区教育大街123号"
  }'
```

## 注意事项

### 安全性
- 所有需要认证的接口都需要在请求头中携带有效的JWT令牌
- JWT令牌格式：`Authorization: Bearer <token>`
- 令牌过期时间为24小时，过期后需要重新登录

### 数据格式
- 所有时间字段使用ISO 8601格式：`YYYY-MM-DDTHH:mm:ss.SSSZ`
- 所有ID字段使用UUID格式
- 分页查询默认页大小为10，最大页大小为100

### 枚举值说明
- **年级等级**: GRADE_1(一年级), GRADE_2(二年级), GRADE_3(三年级), GRADE_4(四年级), GRADE_5(五年级), GRADE_6(六年级)
- **学科**: CHINESE(语文), MATH(数学), ENGLISH(英语)
- **机构状态**: ACTIVE(活跃), INACTIVE(非活跃), SUSPENDED(暂停)

### 限制说明
- 用户名长度：3-50个字符
- 密码长度：6-100个字符
- 邮箱格式必须符合标准邮箱格式
- 手机号格式：11位数字
- 课程编码长度：最大20字符，必须唯一
- 课程标题长度：最大100字符
- 机构名称长度：最大100字符

### 最佳实践
- 建议在生产环境中使用HTTPS协议
- 建议实现客户端令牌自动刷新机制
- 建议对敏感操作进行二次确认
- 建议实现适当的错误重试机制
- 建议在创建课程时指定所属机构
- 建议定期检查机构状态，及时处理异常机构

---

**文档版本**: v1.2.0  
**最后更新**: 2024-01-15  
**维护者**: 万里教育开发团队