仅使用GITHUB MCP 部署代码
数据库使用 PostgreSQL
在github上建立dev、staging、production三个分支
dev分支用于开发，staging分支用于测试，production分支用于生产
每个分支都有一个对应的railway服务，服务名称为`wanli-backend-<分支名>`
每个服务都有一个对应的数据库，运行在railway上，数据库名称为`wanli_backend_<分支名>`
每个数据库都有一个对应的环境变量，环境变量名称为`DATABASE_URL_<分支名>`
当运行或部署代码时，应检查在哪个环境下运行，根据环境切换到对应的分支上，并使用相应的环境变量和配置文件
部署完成后，使用railway cli 检查部署日志，确保服务正常运行
railway上的service配置已经包含了github地址，每次github的代码更新都会触发service的重新部署
