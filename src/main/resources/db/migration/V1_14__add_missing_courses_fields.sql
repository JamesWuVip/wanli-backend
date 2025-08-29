-- 添加courses表缺失的字段
-- 作者: wanli
-- 创建时间: 2025-01-20
-- 描述: 为courses表添加course_code, grade_level, subject, is_active, institution_id字段

-- 添加course_code字段（课程代码）
ALTER TABLE courses ADD COLUMN course_code VARCHAR(20) NOT NULL DEFAULT 'DEFAULT_CODE';

-- 添加grade_level字段（年级）
ALTER TABLE courses ADD COLUMN grade_level INTEGER NOT NULL DEFAULT 1;

-- 添加subject字段（学科）
ALTER TABLE courses ADD COLUMN subject VARCHAR(100) NOT NULL DEFAULT 'General';

-- 添加is_active字段（是否激活）
ALTER TABLE courses ADD COLUMN is_active BOOLEAN NOT NULL DEFAULT true;

-- 添加institution_id字段（机构ID）
ALTER TABLE courses ADD COLUMN institution_id UUID;

-- 添加唯一约束
ALTER TABLE courses ADD CONSTRAINT uk_courses_code_institution UNIQUE (course_code, institution_id);

-- 添加检查约束
ALTER TABLE courses ADD CONSTRAINT chk_courses_grade_level CHECK (grade_level >= 1 AND grade_level <= 12);

-- 添加索引
CREATE INDEX idx_courses_course_code ON courses(course_code);
CREATE INDEX idx_courses_grade_level ON courses(grade_level);
CREATE INDEX idx_courses_subject ON courses(subject);
CREATE INDEX idx_courses_is_active ON courses(is_active);
CREATE INDEX idx_courses_institution_id ON courses(institution_id);

-- 添加外键约束
ALTER TABLE courses ADD CONSTRAINT fk_courses_institution_id 
    FOREIGN KEY (institution_id) REFERENCES institutions(id) ON DELETE SET NULL;

-- 添加表注释
COMMENT ON COLUMN courses.course_code IS '课程代码';
COMMENT ON COLUMN courses.grade_level IS '年级(1-12)';
COMMENT ON COLUMN courses.subject IS '学科';
COMMENT ON COLUMN courses.is_active IS '是否激活';
COMMENT ON COLUMN courses.institution_id IS '所属机构ID';
