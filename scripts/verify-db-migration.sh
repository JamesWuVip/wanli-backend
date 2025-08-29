#!/bin/bash

echo "🔍 验证数据库迁移状态..."

# 检查环境变量
if [ -z "$DATABASE_URL" ]; then
    echo "❌ 错误：DATABASE_URL 环境变量未设置"
    echo "请设置 DATABASE_URL 环境变量，例如："
    echo "export DATABASE_URL=postgresql://postgres:password@host:port/database"
    exit 1
fi

# 连接数据库并检查关键表
echo "📊 检查数据库表结构..."
psql $DATABASE_URL -c "\dt" | grep -E "(institutions|users|courses)"

# 检查 franchises 表是否已删除
echo "🔍 检查 franchises 表状态..."
if psql $DATABASE_URL -c "\dt" | grep -q "franchises"; then
    echo "❌ 警告：franchises 表仍然存在"
else
    echo "✅ franchises 表已成功删除"
fi

# 验证数据完整性
echo "📊 验证数据完整性..."
echo "机构数量："
psql $DATABASE_URL -c "SELECT COUNT(*) as institution_count FROM institutions;"
echo "用户数量："
psql $DATABASE_URL -c "SELECT COUNT(*) as user_count FROM users;"
echo "课程数量："
psql $DATABASE_URL -c "SELECT COUNT(*) as course_count FROM courses;"

# 检查外键约束
echo "🔗 检查外键约束..."
psql $DATABASE_URL -c "SELECT tc.table_name, tc.constraint_name, tc.constraint_type, kcu.column_name, ccu.table_name AS foreign_table_name, ccu.column_name AS foreign_column_name FROM information_schema.table_constraints AS tc JOIN information_schema.key_column_usage AS kcu ON tc.constraint_name = kcu.constraint_name AND tc.table_schema = kcu.table_schema JOIN information_schema.constraint_column_usage AS ccu ON ccu.constraint_name = tc.constraint_name AND ccu.table_schema = tc.table_schema WHERE tc.constraint_type = 'FOREIGN KEY' AND tc.table_name IN ('users', 'courses', 'classes') ORDER BY tc.table_name;"

echo "✅ 数据库迁移验证完成"