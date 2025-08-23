-- 为lessons表添加新字段：description, duration, status
-- 以及相应的索引优化

-- 添加新字段
ALTER TABLE lessons 
ADD COLUMN description TEXT,
ADD COLUMN duration INTEGER,
ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'DRAFT';

-- 添加字段注释
COMMENT ON COLUMN lessons.description IS '课时描述';
COMMENT ON COLUMN lessons.duration IS '课时时长（秒）';
COMMENT ON COLUMN lessons.status IS '课时状态：DRAFT, PUBLISHED, ARCHIVED';

-- 创建新的索引
CREATE INDEX idx_lessons_status ON lessons(status);
CREATE INDEX idx_lessons_duration ON lessons(duration);
CREATE INDEX idx_lessons_course_status_deleted ON lessons(course_id, status, deleted_at);
CREATE INDEX idx_lessons_status_created_deleted ON lessons(status, created_at, deleted_at);

-- 添加状态约束
ALTER TABLE lessons 
ADD CONSTRAINT chk_lessons_status 
CHECK (status IN ('DRAFT', 'PUBLISHED', 'ARCHIVED'));

-- 添加时长约束（时长必须为正数）
ALTER TABLE lessons 
ADD CONSTRAINT chk_lessons_duration 
CHECK (duration IS NULL OR duration > 0);