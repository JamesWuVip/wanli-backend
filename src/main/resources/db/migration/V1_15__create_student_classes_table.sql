-- V1.15: 创建student_classes表
-- 根据StudentClass实体类创建对应的数据库表

BEGIN;

-- 创建student_classes表
CREATE TABLE IF NOT EXISTS student_classes (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    student_id UUID NOT NULL,
    class_id UUID NOT NULL,
    joined_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    left_at TIMESTAMP(6) WITH TIME ZONE,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP(6) WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP(6) WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    deleted_at TIMESTAMP(6) WITH TIME ZONE,
    created_by VARCHAR(36),
    updated_by VARCHAR(36)
);

-- 添加表注释
COMMENT ON TABLE student_classes IS '学生班级关联表';
COMMENT ON COLUMN student_classes.id IS '主键ID';
COMMENT ON COLUMN student_classes.student_id IS '学生ID';
COMMENT ON COLUMN student_classes.class_id IS '班级ID';
COMMENT ON COLUMN student_classes.joined_at IS '加入时间';
COMMENT ON COLUMN student_classes.left_at IS '离开时间';
COMMENT ON COLUMN student_classes.is_active IS '是否激活';
COMMENT ON COLUMN student_classes.created_at IS '创建时间';
COMMENT ON COLUMN student_classes.updated_at IS '更新时间';
COMMENT ON COLUMN student_classes.deleted_at IS '删除时间';
COMMENT ON COLUMN student_classes.created_by IS '创建者ID';
COMMENT ON COLUMN student_classes.updated_by IS '更新者ID';

-- 添加唯一约束
ALTER TABLE student_classes ADD CONSTRAINT uk_student_classes_student_class 
    UNIQUE (student_id, class_id);

-- 添加索引
CREATE INDEX IF NOT EXISTS idx_student_classes_student_id ON student_classes(student_id);
CREATE INDEX IF NOT EXISTS idx_student_classes_class_id ON student_classes(class_id);
CREATE INDEX IF NOT EXISTS idx_student_classes_joined_at ON student_classes(joined_at);
CREATE INDEX IF NOT EXISTS idx_student_classes_is_active ON student_classes(is_active);
CREATE INDEX IF NOT EXISTS idx_student_classes_deleted_at ON student_classes(deleted_at);

-- 创建复合索引优化常用查询
CREATE INDEX IF NOT EXISTS idx_student_classes_student_active 
    ON student_classes(student_id, is_active) 
    WHERE deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_student_classes_class_active 
    ON student_classes(class_id, is_active) 
    WHERE deleted_at IS NULL;

-- 添加外键约束（如果相关表存在）
-- ALTER TABLE student_classes ADD CONSTRAINT fk_student_classes_student_id 
--     FOREIGN KEY (student_id) REFERENCES users(id);
-- ALTER TABLE student_classes ADD CONSTRAINT fk_student_classes_class_id 
--     FOREIGN KEY (class_id) REFERENCES classes(id);

-- 创建更新时间触发器
CREATE OR REPLACE FUNCTION update_student_classes_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_student_classes_updated_at
    BEFORE UPDATE ON student_classes
    FOR EACH ROW
    EXECUTE FUNCTION update_student_classes_updated_at();

COMMIT;