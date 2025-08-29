-- V1.14: 为courses表添加缺失的字段
-- 添加实体类中定义但数据库表中缺失的字段

BEGIN;

-- 添加course_code字段
ALTER TABLE courses ADD COLUMN IF NOT EXISTS course_code VARCHAR(20);

-- 添加grade_level字段
ALTER TABLE courses ADD COLUMN IF NOT EXISTS grade_level VARCHAR(20);

-- 添加subject字段
ALTER TABLE courses ADD COLUMN IF NOT EXISTS subject VARCHAR(20);

-- 添加is_active字段
ALTER TABLE courses ADD COLUMN IF NOT EXISTS is_active BOOLEAN DEFAULT true;

-- 添加institution_id字段
ALTER TABLE courses ADD COLUMN IF NOT EXISTS institution_id VARCHAR(36);

-- 为现有数据设置默认值
UPDATE courses SET 
    course_code = CONCAT('COURSE_', SUBSTRING(id::text, 1, 8)),
    grade_level = 'GRADE_1',
    subject = 'CHINESE',
    is_active = true
WHERE course_code IS NULL OR grade_level IS NULL OR subject IS NULL OR is_active IS NULL;

-- 添加NOT NULL约束
ALTER TABLE courses ALTER COLUMN course_code SET NOT NULL;
ALTER TABLE courses ALTER COLUMN grade_level SET NOT NULL;
ALTER TABLE courses ALTER COLUMN subject SET NOT NULL;
ALTER TABLE courses ALTER COLUMN is_active SET NOT NULL;

-- 添加唯一约束
ALTER TABLE courses ADD CONSTRAINT uk_courses_course_code UNIQUE (course_code);

-- 添加检查约束
ALTER TABLE courses ADD CONSTRAINT chk_courses_grade_level 
    CHECK (grade_level IN ('GRADE_1', 'GRADE_2', 'GRADE_3', 'GRADE_4', 'GRADE_5', 'GRADE_6'));

ALTER TABLE courses ADD CONSTRAINT chk_courses_subject 
    CHECK (subject IN ('CHINESE', 'MATH', 'ENGLISH'));

ALTER TABLE courses ADD CONSTRAINT chk_courses_status 
    CHECK (status IN ('DRAFT', 'PUBLISHED'));

-- 添加索引
CREATE INDEX IF NOT EXISTS idx_courses_course_code ON courses(course_code);
CREATE INDEX IF NOT EXISTS idx_courses_grade_level ON courses(grade_level);
CREATE INDEX IF NOT EXISTS idx_courses_subject ON courses(subject);
CREATE INDEX IF NOT EXISTS idx_courses_is_active ON courses(is_active);
CREATE INDEX IF NOT EXISTS idx_courses_institution_id ON courses(institution_id);

-- 添加外键约束（如果institutions表存在）
-- ALTER TABLE courses ADD CONSTRAINT fk_courses_institution_id 
--     FOREIGN KEY (institution_id) REFERENCES institutions(id);

COMMIT;