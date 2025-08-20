-- 万里后端数据库初始化脚本
-- 创建不同环境的schema以实现数据隔离

-- 创建staging环境schema
CREATE SCHEMA IF NOT EXISTS staging;

-- 创建production环境schema
CREATE SCHEMA IF NOT EXISTS production;

-- 为staging schema设置权限
GRANT ALL PRIVILEGES ON SCHEMA staging TO postgres;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA staging TO postgres;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA staging TO postgres;

-- 为production schema设置权限
GRANT ALL PRIVILEGES ON SCHEMA production TO postgres;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA production TO postgres;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA production TO postgres;

-- 输出确认信息
SELECT 'Database schemas created successfully' AS status;