-- 性能优化：为courses和lessons表添加索引
-- 创建时间：2024年
-- 目的：优化查询性能，支持高频查询场景

-- ========== Courses表索引 ==========

-- 创建者ID索引 - 用于按创建者查询课程
CREATE INDEX IF NOT EXISTS idx_courses_creator_id ON courses(creator_id);

-- 状态索引 - 用于按状态查询课程
CREATE INDEX IF NOT EXISTS idx_courses_status ON courses(status);

-- 创建时间索引 - 用于按时间排序
CREATE INDEX IF NOT EXISTS idx_courses_created_at ON courses(created_at);

-- 软删除索引 - 用于过滤已删除记录
CREATE INDEX IF NOT EXISTS idx_courses_deleted_at ON courses(deleted_at);

-- 复合索引：创建者+状态+删除状态 - 用于高频查询组合
CREATE INDEX IF NOT EXISTS idx_courses_creator_status_deleted ON courses(creator_id, status, deleted_at);

-- 复合索引：状态+创建时间+删除状态 - 用于分页查询
CREATE INDEX IF NOT EXISTS idx_courses_status_created_deleted ON courses(status, created_at, deleted_at);

-- 标题索引 - 用于模糊查询（部分匹配）
CREATE INDEX IF NOT EXISTS idx_courses_title ON courses(title);

-- ========== Lessons表索引 ==========

-- 课程ID索引 - 用于按课程查询课时
CREATE INDEX IF NOT EXISTS idx_lessons_course_id ON lessons(course_id);

-- 创建者ID索引 - 用于按创建者查询课时
CREATE INDEX IF NOT EXISTS idx_lessons_creator_id ON lessons(creator_id);

-- 排序索引 - 用于课时排序
CREATE INDEX IF NOT EXISTS idx_lessons_order_index ON lessons(order_index);

-- 创建时间索引 - 用于按时间排序
CREATE INDEX IF NOT EXISTS idx_lessons_created_at ON lessons(created_at);

-- 软删除索引 - 用于过滤已删除记录
CREATE INDEX IF NOT EXISTS idx_lessons_deleted_at ON lessons(deleted_at);

-- 复合索引：课程+排序+删除状态 - 用于获取课程的课时列表
CREATE INDEX IF NOT EXISTS idx_lessons_course_order_deleted ON lessons(course_id, order_index, deleted_at);

-- 复合索引：课程+创建时间+删除状态 - 用于分页查询
CREATE INDEX IF NOT EXISTS idx_lessons_course_created_deleted ON lessons(course_id, created_at, deleted_at);

-- 复合索引：创建者+创建时间+删除状态 - 用于创建者的课时查询
CREATE INDEX IF NOT EXISTS idx_lessons_creator_created_deleted ON lessons(creator_id, created_at, deleted_at);

-- 标题索引 - 用于模糊查询（部分匹配）
CREATE INDEX IF NOT EXISTS idx_lessons_title ON lessons(title);

-- ========== 索引使用说明 ==========
-- 1. 单列索引适用于单一条件查询
-- 2. 复合索引适用于多条件查询，注意列的顺序很重要
-- 3. 前缀匹配（LIKE 'prefix%'）可以有效利用索引
-- 4. 软删除字段（deleted_at）包含在复合索引中，提高过滤效率
-- 5. 创建时间字段用于排序，包含在复合索引中避免额外排序开销