好的，遵照指示。

基于我们刚刚确立的，在单一Railway账户下构建“本地-测试-生产”三套环境的整合架构方案，现对Sprint 0的任务说明书进行重新规划。此版指令将更详尽，确保开发工作从一开始就完全匹配这套新的架构蓝图。

---

### 任务说明书：Sprint 0 **(后端 \- 修订版)**

**致：** 后端开发Agent

**发件人：** 首席系统架构师

**主题：** **Sprint 0 (修订版) \- 构建支持多环境的后端基础设施**

**版本：** 2.0

**概述：**

本次Sprint的核心目标已升级。我们不仅要搭建后端服务，更要使其具备

**原生多环境支持能力**，为本地、测试 (Staging) 和生产 (Production) 环境奠定坚实基础。所有配置项（如数据库、CORS源）必须实现外部化，由环境变量驱动，以适应我们资源整合的部署模型 1111。

**详细需求与任务分解 (Requirements & Tasks)：**

1. **代码库与基础API：**  
   * 初始化  
     wanli-backend (Spring Boot) Git仓库 2。

   * 开发一个  
     /api/health 端点，返回固定JSON {"message": "Hello from Backend\!"} 3。

2. **多环境配置：**  
   * 在 src/main/resources 下创建环境特定的配置文件：application-staging.yml 和 application-prod.yml。  
   * 将数据库连接、CORS策略等所有环境可变配置，全部使用占位符，以便从环境变量中读取。  
   * **数据库Schema配置**：数据源配置必须参数化，以接受 DB\_SCHEMA 环境变量，并配置JPA/Hibernate使用此Schema。  
   * **CORS配置**：跨域许可源 (allowed origins) 必须由 CORS\_ALLOWED\_ORIGINS 环境变量动态指定。  
3. **本地开发环境支持 (Docker)：**  
   * 在项目根目录创建 Dockerfile，用于将Spring Boot应用容器化。  
   * 创建 docker-compose.yml 文件，定义一个 postgres 服务，为本地开发提供数据库支持。  
4. **云端部署配置 (Railway)：**  
   * 在Railway项目中创建 wanli-backend 服务。  
   * 配置该服务，使其能够基于Git分支进行部署：  
     * staging 分支 \-\> 部署到 **测试环境**。  
     * main 分支 \-\> 部署到 **生产环境**。  
   * 在Railway的环境变量设置中，为测试和生产环境分别配置不同的变量集 (例如 DB\_SCHEMA 和 CORS\_ALLOWED\_ORIGINS)。

**交付物 (Deliverables)：**

1. 一个包含多环境配置和 Dockerfile 的 wanli-backend Spring Boot项目。  
2. 一个 docker-compose.yml 文件，能够一键启动本地PostgreSQL数据库。  
3. 在Railway上配置完成的、支持分支部署的后端服务。  
4. 一个成功部署并可通过 https://api-staging.wanli.ai/api/health 访问的测试环境API。

**测试与验收标准 (Acceptance Criteria)：**

| ID | 测试场景 | 预期结果 |  |
| :---- | :---- | :---- | :---- |
| **AC-BE-0.1** | **本地运行** | 开发者能够通过docker-compose up启动本地数据库，并在IDE中成功运行后端应用，访问http://localhost:8080/api/health得到正确响应。 |  |
| **AC-BE-0.2** | **测试环境部署** | 向staging分支推送代码后，CI/CD自动触发，后端应用成功部署。 |  |
| **AC-BE-0.3** | **测试环境API访问** | 访问 | https://api-staging.wanli.ai/api/health 必须返回 200 OK 状态和正确的JSON内容 4。  |
| **AC-BE-0.4** | **环境隔离验证** | 检查Railway上测试环境的配置，确认其环境变量（如DB\_SCHEMA）指向的是staging，并且生产环境的配置占位符也已设置妥当。 |  |

---

### **任务说明书：Sprint 0 (前端 \- 修订版)**

**致：** 前端开发Agent

**发件人：** 首席系统架构师

**主题：** **Sprint 0 (修订版) \- 构建支持多环境的前端基础设施**

**版本：** 2.0

**概述：**

根据新的整合方案，前端应用将统一部署在Railway平台。本次Sprint的核心目标是构建一个能适应多环境API端点的前端应用，并建立本地代理解决跨域问题，同时完成应用的容器化以便在Railway上部署 5555。

**详细需求与任务分解 (Requirements & Tasks)：**

1. **代码库与基础页面：**  
   * 初始化  
     wanli-frontend (Vue.js \+ TypeScript) Git仓库 6。

   * 开发一个简单欢迎页面，该页面在加载时会调用后端的  
     /api/health 接口，并将其返回的消息显示在屏幕上 7。

2. **多环境配置：**  
   * 利用 .env 文件机制 (.env.development, .env.staging, .env.production) 来管理不同环境下的API基础路径 (VITE\_API\_BASE\_URL)。  
   * 应用内的API请求服务层，必须从环境变量中读取API基地址，而不是硬编码。  
3. **本地开发环境支持 (Proxy)：**  
   * 配置Vite开发服务器 (vite.config.ts)，添加一个API代理规则，将所有对 /api 的请求转发到本地后端服务 http://localhost:8080。  
4. **容器化 (Docker)：**  
   * 在项目根目录创建 Dockerfile。该文件需包含多阶段构建：  
     * 第一阶段：使用Node.js环境执行 npm run build，生成静态文件。  
     * 第二阶段：使用一个轻量级的Web服务器（如Nginx），将构建好的静态文件复制进去并提供服务。  
5. **云端部署配置 (Railway)：**  
   * 在同一个Railway项目中创建 wanli-frontend 服务。  
   * 配置该服务，使其能够基于Git分支进行部署：  
     * staging 分支 \-\> 部署到 **测试环境**。  
     * main 分支 \-\> 部署到 **生产环境**。  
   * 在Railway的环境变量设置中，为测试和生产环境的 VITE\_API\_BASE\_URL 变量分别配置为 https://api-staging.wanli.ai 和 https://api.wanli.ai。

**交付物 (Deliverables)：**

1. 一个包含多环境配置、本地代理设置和 Dockerfile 的 wanli-frontend Vue.js项目。  
2. 在Railway上配置完成的、支持分支部署的前端服务。  
3. 一个成功部署并可通过 https://app-staging.wanli.ai 访问的测试环境网页，该网页能成功调用测试环境的后端API并展示返回信息。

**测试与验收标准 (Acceptance Criteria)：**

| ID | 测试场景 | 预期结果 |  |  |
| :---- | :---- | :---- | :---- | :---- |
| **AC-FE-0.1** | **本地运行与代理** | 开发者在本地同时运行前后端时，前端页面 (http://localhost:5173) 必须能通过代理成功调用后端API并显示 "Hello from Backend\!"，浏览器控制台无CORS错误。 |  |  |
| **AC-FE-0.2** | **测试环境部署** | 向staging分支推送代码后，CI/CD自动触发，前端应用成功部署。 |  |  |
| **AC-FE-0.3** | **测试环境端到端通信** | 访问 | https://app-staging.wanli.ai，页面必须成功显示 "Hello from Backend\!" 8。开发者工具网络面板显示API请求发向  | api-staging.wanli.ai 并成功返回。 |
| **AC-FE-0.4** | **环境隔离验证** | 检查Railway上测试环境的配置，确认VITE\_API\_BASE\_URL指向的是测试API域名，并且生产环境的配置也已设置妥当。 |  |  |

