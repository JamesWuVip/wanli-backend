好的，遵照指示。

我已经详细研究了产品待办清单 (Product Backlog) 和 Sprint 0 的任务书结构。现在，我为您和Coding Agent准备一份清晰、可执行的 Sprint 1 任务说明书。

这份任务书将严格遵循我们既定的格式，确保每一项任务都明确、可衡量，为构建系统的核心功能——**身份认证与内容基础**——提供精确的指引。

---

### **任务说明书：Sprint 1 (后端)**

**致：** 后端开发Agent

**发件人：** 首席系统架构师

**主题：** **Sprint 1 \- 核心认证与内容基础API**

**版本：** 1.0

**概述：**

在Sprint 0成功搭建基础设施之后，本Sprint的目标是构建应用的“身份标识”和“内容骨架”。我们将开发一套完整、安全的认证API，并为最高优先级的核心内容（课程、课时）提供受角色保护的增删改查(CRUD)接口。本次交付的成果将是整个系统业务逻辑的基石。

**详细需求与任务分解 (Requirements & Tasks)：**

1. **用户认证体系 (Authentication System):**  
   * **数据库与实体:**  
     * 根据《数据库设计文档》，创建 users 表对应的JPA实体 (Entity) 和数据访问层 (Repository)。  
   * **注册功能 (Registration):**  
     * 开发API端点: POST /api/auth/register。  
     * 请求体 (Request Body) 需包含 username, password, email, role 等字段。  
     * 必须使用BCrypt算法对用户密码进行单向加密存储。  
   * **登录功能 (Login):**  
     * 开发API端点: POST /api/auth/login。  
     * 验证用户凭据，成功后生成并返回一个包含用户角色信息的JWT (JSON Web Token)。  
   * **安全集成 (Security Integration):**  
     * 全面集成Spring Security，配置JWT过滤器，用于验证后续所有受保护API请求的Token。  
2. **内容管理基础 (Content Management Foundation):**  
   * **课程管理 (US-101):**  
     * 创建 courses 表对应的JPA实体和数据访问层。  
     * 开发以下受保护的API端点：  
       * POST /api/courses: 创建新课程。  
       * GET /api/courses: 获取所有课程的列表。  
       * PUT /api/courses/{id}: 更新指定课程的信息。  
     * **访问控制:** 以上所有端点必须受到保护，仅允许拥有 ROLE\_HQ\_TEACHER 角色的用户访问。  
   * **课时管理 (US-102):**  
     * 创建 lessons 表对应的JPA实体和数据访问层，并正确配置与 Course 实体的关联关系。  
     * 开发以下受保护的API端点：  
       * POST /api/courses/{courseId}/lessons: 为指定课程添加新课时。  
       * GET /api/courses/{courseId}/lessons: 获取指定课程下的所有课时列表。  
     * **访问控制:** 以上端点同样仅对 ROLE\_HQ\_TEACHER 角色开放。

**交付物 (Deliverables)：**

1. 一套功能完备、经过严格测试的用户注册和登录API。  
2. 一套受JWT和角色权限保护的、用于管理课程和课时的API。  
3. 相应的数据库表结构已通过JPA/Hibernate自动在 staging Schema中创建。

**测试与验收标准 (Acceptance Criteria)：**

| ID | 测试场景 | 预期结果 |
| :---- | :---- | :---- |
| **AC-BE-1.1** | **用户注册** | 调用 POST /api/auth/register 成功创建一个新用户，数据库中密码为加密字符串。 |
| **AC-BE-1.2** | **用户登录** | 使用正确的凭据调用 POST /api/auth/login，返回 200 OK 状态码和一个有效的JWT。 |
| **AC-BE-1.3** | **无权限访问** | 未携带JWT或使用非 ROLE\_HQ\_TEACHER 角色的JWT访问 POST /api/courses，必须返回 403 Forbidden 错误。 |
| **AC-BE-1.4** | **有权限访问** | 使用 ROLE\_HQ\_TEACHER 角色的JWT，可以成功调用课程和课时的所有API端点。 |
| **AC-BE-1.5** | **数据关联** | 创建一个课时后，该课时在数据库中必须正确地通过外键关联到其所属的课程。 |

---

### **任务说明书：Sprint 1 (前端)**

**致：** 前端开发Agent

**发件人：** 首席系统架构师

**主题：** **Sprint 1 \- 应用骨架与认证流程实现**

**版本：** 1.0

**概述：**

本Sprint的目标是将Sprint 0的静态欢迎页，升级为一个具备基础导航、页面路由和完整用户认证流程的单页应用 (SPA)。我们将构建应用的“骨架”，并与后端认证API进行全面对接，实现一个用户可以真实登录、系统可以识别其身份的动态应用。

**详细需求与任务分解 (Requirements & Tasks)：**

1. **应用框架与路由 (Application Shell & Routing):**  
   * **基础布局:** 创建一个包含顶部导航栏、侧边栏和主内容区域的应用基础布局组件。  
   * **路由配置:** 使用Vue Router配置应用的路由规则，至少包含 /login, /register, /dashboard (登录后的主页) 等路径。  
   * **API服务层:** 创建一个集中的API客户端 (例如使用Axios)，并配置拦截器 (interceptor)，用于在每次请求时自动附加本地存储的JWT。  
2. **认证流程实现 (Authentication Flow):**  
   * **UI开发:** 开发登录页面和注册页面的UI组件，包含所有必要的表单输入框和提交按钮。  
   * **API对接:** 将注册和登录表单与后端对应的 POST /api/auth/register 和 POST /api/auth/login 接口进行对接。  
   * **Token处理:** 登录成功后，必须将后端返回的JWT安全地存储在浏览器的localStorage中。  
   * **路由守卫 (Route Guard):** 实现全局路由守卫。对于需要认证的路径（如 /dashboard），如果用户未登录（即localStorage中没有有效的JWT），则必须自动重定向到 /login 页面。  
3. **核心内容展示 (Initial Content Display):**  
   * **课程列表页面 (US-101/US-201部分实现):**  
     * 创建 /dashboard 页面，作为登录后的默认着陆页。  
     * 在该页面加载时，调用后端的 GET /api/courses 接口来获取课程数据。  
     * 将获取到的课程列表数据渲染到页面上，至少展示每个课程的标题。

**交付物 (Deliverables)：**

1. 一个用户可以真实完成注册和登录流程的前端应用。  
2. 一个受路由守卫保护的、登录后才能访问的仪表盘页面。  
3. 仪表盘页面能够成功从后端获取并展示课程列表。

**测试与验收标准 (Acceptance Criteria)：**

| ID | 测试场景 | 预期结果 |
| :---- | :---- | :---- |
| **AC-FE-1.1** | **路由保护** | 在未登录状态下，直接在浏览器地址栏输入 app.staging.wanli.ai/dashboard，页面应自动跳转到登录页。 |
| **AC-FE-1.2** | **注册与登录** | 用户可以在注册页面成功创建账号，然后使用该账号在登录页面成功登录，并被自动导航至 /dashboard 页面。 |
| **AC-FE-1.3** | **Token存储与使用** | 登录成功后，浏览器的localStorage中应存在JWT。刷新页面或跳转到其他受保护页面时，用户应保持登录状态。 |
| **AC-FE-1.4** | **数据展示** | 成功登录并进入/dashboard页面后，页面应向后端发起对/api/courses的请求，并成功将返回的课程列表展示出来。 |
| **AC-FE-1.5** | **退出登录** | （可选，建议实现）导航栏有“退出”按钮，点击后清除localStorage中的JWT并跳转到登录页。 |

