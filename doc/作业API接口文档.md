### **万里书院 \- API接口文档 (V0.2)**

文档ID: WANLI-API-V0.2  
版本: 0.2  
面向Sprint: Sprint 1  
最后更新: 2025年8月22日  
更新内容: 补充缺失API端点、修正响应格式、添加技术细节说明

### **1\. 认证 (Authentication)**

所有需要认证的API请求，都必须在HTTP请求头中包含 Authorization 字段，其值为 Bearer \<YOUR\_JWT\>。

#### **JWT令牌说明**

* **格式**: Bearer Token  
* **有效期**: 8小时  
* **包含信息**: 用户ID、用户名、角色、签发时间、过期时间  
* **使用方式**: 在请求头中添加 `Authorization: Bearer <token>`

#### **标准化错误响应格式**

**业务逻辑错误**:
{  
  "success": false,  
  "message": "具体的错误信息"  
}

**HTTP标准错误**:
{  
  "timestamp": "2025-08-22T10:00:00.000+00:00",  
  "status": 400,  
  "error": "Bad Request",  
  "message": "具体的错误信息",  
  "path": "/api/auth/register"  
}

#### **数据验证规则**

**用户注册**:
- username: 必填，长度3-50字符  
- password: 必填，长度6-100字符  
- email: 必填，有效邮箱格式  
- role: 可选，默认为学生角色

**课程管理**:
- title: 必填，长度1-200字符  
- description: 可选，最大1000字符  
- status: 可选，枚举值(DRAFT, PUBLISHED, ARCHIVED)

**课时管理**:
- title: 必填，长度1-200字符  
- order_index: 必填，正整数

### **2\. 认证模块 (/api/auth)**

#### **2.1 用户注册**

* **Endpoint:** POST /api/auth/register  
* **描述:** 创建一个新用户账号。  
* **认证:** 公开访问。  
* **请求体 (Request Body):** application/json  
  {  
    "username": "new\_teacher",  
    "password": "securePassword123",  
    "email": "teacher@example.com",  
    "role": "ROLE\_HQ\_TEACHER"  
  }

* **响应 (Success 201 Created):**  
  {  
    "success": true,  
    "message": "用户注册成功",  
    "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJuZXdfdGVhY2hlciIsInJvbGVzIjpbIlJPTEVfSFFfVEVBQ0hFUiJdLCJpYXQiOjE2NjExNjE2MDAsImV4cCI6MTY2MTE5MDQwMH0.abcdefg...",  
    "user": {  
      "id": "a1b2c3d4-e5f6-7890-1234-567890abcdef",  
      "username": "new_teacher",  
      "email": "teacher@example.com",  
      "role": "ROLE_HQ_TEACHER",  
      "created_at": "2025-08-22T10:00:00.000Z",  
      "updated_at": "2025-08-22T10:00:00.000Z"  
    }  
  }

* **错误响应:**  
  * 400 Bad Request: 请求体验证失败（如用户名为空、邮箱格式错误）。  
  * 409 Conflict: 用户名或邮箱已存在。

#### **2.2 用户登录**

* **Endpoint:** POST /api/auth/login  
* **描述:** 使用用户名和密码登录，获取JWT。  
* **认证:** 公开访问。  
* **请求体 (Request Body):** application/json  
  {  
    "username": "new\_teacher",  
    "password": "securePassword123"  
  }

* **响应 (Success 200 OK):**  
  {  
    "success": true,  
    "message": "登录成功",  
    "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJuZXdfdGVhY2hlciIsInJvbGVzIjpbIlJPTEVfSFFfVEVBQ0hFUiJdLCJpYXQiOjE2NjExNjE2MDAsImV4cCI6MTY2MTE5MDQwMH0.abcdefg...",  
    "user": {  
      "id": "a1b2c3d4-e5f6-7890-1234-567890abcdef",  
      "username": "new_teacher",  
      "email": "teacher@example.com",  
      "role": "ROLE_HQ_TEACHER"  
    }  
  }

* **错误响应:**  
  * 401 Unauthorized: 用户名或密码错误。

### **3\. 课程模块 (/api/courses)**

#### **3.1 创建课程**

* **Endpoint:** POST /api/courses  
* **描述:** 创建一个新的课程。  
* **认证:** 需要JWT。  
* **权限:** ROLE\_HQ\_TEACHER  
* **请求体 (Request Body):** application/json  
  {  
    "title": "初中物理第一册",  
    "description": "本课程涵盖力学基础知识。"  
  }

* **响应 (Success 201 Created):**  
  {  
    "id": "c1d2e3f4-a5b6-c7d8-e9f0-a1b2c3d4e5f6",  
    "creator\_id": "a1b2c3d4-e5f6-7890-1234-567890abcdef",  
    "title": "初中物理第一册",  
    "description": "本课程涵盖力学基础知识。",  
    "status": "DRAFT",  
    "created\_at": "2025-08-22T10:05:00.000Z",  
    "updated\_at": "2025-08-22T10:05:00.000Z"  
  }

* **错误响应:**  
  * 400 Bad Request: 标题为空。  
  * 401 Unauthorized: 未提供或JWT无效。  
  * 403 Forbidden: 用户角色非 ROLE\_HQ\_TEACHER。

#### **3.2 获取课程列表**

* **Endpoint:** GET /api/courses  
* **描述:** 获取所有课程的列表。  
* **认证:** 需要JWT。  
* **权限:** 任何已认证用户。  
* **响应 (Success 200 OK):** application/json  
  \[  
    {  
      "id": "c1d2e3f4-a5b6-c7d8-e9f0-a1b2c3d4e5f6",  
      "title": "初中物理第一册",  
      "status": "DRAFT"  
    },  
    {  
      "id": "f6e5d4c3-b2a1-d8c7-b6a5-f4e3d2c1b0a9",  
      "title": "高中化学选修四",  
      "status": "PUBLISHED"  
    }  
  \]

* **错误响应:**  
  * 401 Unauthorized: 未提供或JWT无效。

#### **3.3 更新课程**

* **Endpoint:** PUT /api/courses/{id}  
* **描述:** 更新指定ID的课程信息。  
* **认证:** 需要JWT。  
* **权限:** ROLE\_HQ\_TEACHER  
* **路径参数 (Path Parameters):**  
  * id (UUID): 课程的唯一标识符。  
* **请求体 (Request Body):** application/json  
  {  
    "title": "初中物理（上册）",  
    "description": "本课程涵盖力学与声学基础知识。",  
    "status": "PUBLISHED"  
  }

* **响应 (Success 200 OK):** 返回更新后的完整课程对象。  
* **错误响应:**  
  * 400 Bad Request: 请求体验证失败。  
  * 401 Unauthorized: 未提供或JWT无效。  
  * 403 Forbidden: 用户角色非 ROLE\_HQ\_TEACHER。  
  * 404 Not Found: 课程ID不存在。

### **4\. 课时模块 (/api/courses/{courseId}/lessons)**

#### **4.1 创建课时**

* **Endpoint:** POST /api/courses/{courseId}/lessons  
* **描述:** 在指定课程下创建一个新课时。  
* **认证:** 需要JWT。  
* **权限:** ROLE\_HQ\_TEACHER  
* **路径参数 (Path Parameters):**  
  * courseId (UUID): 课时所属课程的ID。  
* **请求体 (Request Body):** application/json  
  {  
    "title": "第一章：力的初步认识",  
    "order\_index": 1  
  }

* **响应 (Success 201 Created):**  
  {  
    "id": "l1m2n3o4-p5q6-r7s8-t9u0-v1w2x3y4z5a6",  
    "course\_id": "c1d2e3f4-a5b6-c7d8-e9f0-a1b2c3d4e5f6",  
    "title": "第一章：力的初步认识",  
    "order\_index": 1,  
    "created\_at": "2025-08-22T10:10:00.000Z",  
    "updated\_at": "2025-08-22T10:10:00.000Z"  
  }

* **错误响应:**  
  * 400 Bad Request: 标题为空。  
  * 401 Unauthorized: 未提供或JWT无效。  
  * 403 Forbidden: 用户角色非 ROLE\_HQ\_TEACHER。  
  * 404 Not Found: courseId 不存在。

#### **4.2 获取指定课程的课时列表**

* **Endpoint:** GET /api/courses/{courseId}/lessons  
* **描述:** 获取指定课程下的所有课时列表。  
* **认证:** 需要JWT。  
* **权限:** 任何已认证用户。  
* **路径参数 (Path Parameters):**  
  * courseId (UUID): 课程的唯一标识符。  
* **响应 (Success 200 OK):** application/json  
  \[  
    {  
      "id": "l1m2n3o4-p5q6-r7s8-t9u0-v1w2x3y4z5a6",  
      "title": "第一章：力的初步认识",  
      "order\_index": 1  
    },  
    {  
      "id": "a6z5y4x3-w2v1-u0t9-s8r7-q6p5o4n3m2l1",  
      "title": "第二章：牛顿第一定律",  
      "order\_index": 2  
    }  
  \]

* **错误响应:**  
  * 401 Unauthorized: 未提供或JWT无效。  
  * 404 Not Found: courseId 不存在。

#### **4.3 获取课时详情**

* **Endpoint:** GET /api/courses/{courseId}/lessons/{lessonId}  
* **描述:** 获取指定课程下的特定课时详情。  
* **认证:** 需要JWT。  
* **权限:** 任何已认证用户。  
* **路径参数 (Path Parameters):**  
  * courseId (UUID): 课程的唯一标识符。  
  * lessonId (UUID): 课时的唯一标识符。  

* **响应 (Success 200 OK):** application/json  
  {  
    "id": "l1m2n3o4-p5q6-r7s8-t9u0-v1w2x3y4z5a6",  
    "course_id": "c1d2e3f4-a5b6-c7d8-e9f0-a1b2c3d4e5f6",  
    "title": "第一章：力的初步认识",  
    "order_index": 1,  
    "created_at": "2025-08-22T10:10:00.000Z",  
    "updated_at": "2025-08-22T10:10:00.000Z"  
  }

* **错误响应:**  
  * 401 Unauthorized: 未提供或JWT无效。  
  * 404 Not Found: courseId 或 lessonId 不存在。

#### **4.4 更新课时**

* **Endpoint:** PUT /api/courses/{courseId}/lessons/{lessonId}  
* **描述:** 更新指定课程下的特定课时信息。  
* **认证:** 需要JWT。  
* **权限:** ROLE_HQ_TEACHER（且必须是课程创建者）  
* **路径参数 (Path Parameters):**  
  * courseId (UUID): 课程的唯一标识符。  
  * lessonId (UUID): 课时的唯一标识符。  
* **请求体 (Request Body):** application/json  
  {  
    "title": "第一章：力的深入理解",  
    "order_index": 1  
  }

* **响应 (Success 200 OK):**  
  {  
    "success": true,  
    "message": "课时更新成功",  
    "lesson": {  
      "id": "l1m2n3o4-p5q6-r7s8-t9u0-v1w2x3y4z5a6",  
      "course_id": "c1d2e3f4-a5b6-c7d8-e9f0-a1b2c3d4e5f6",  
      "title": "第一章：力的深入理解",  
      "order_index": 1,  
      "created_at": "2025-08-22T10:10:00.000Z",  
      "updated_at": "2025-08-22T10:15:00.000Z"  
    }  
  }

* **错误响应:**  
  * 400 Bad Request: 请求体验证失败。  
  * 401 Unauthorized: 未提供或JWT无效。  
  * 403 Forbidden: 用户角色非 ROLE_HQ_TEACHER 或非课程创建者。  
  * 404 Not Found: courseId 或 lessonId 不存在。