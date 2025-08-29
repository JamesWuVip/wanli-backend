-- 创建student_classes表
-- 作者: wanli
-- 创建时间: 2025-01-20
-- 描述: 创建学生班级关联表，用于管理学生与班级的关系

CREATE TABLE student_classes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    student_id UUID NOT NULL,
    class_id UUID NOT NULL,
    joined_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    left_at TIMESTAMP WITH TIME ZONE,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP WITH TIME ZONE
);

-- 添加唯一约束（一个学生在同一个班级中只能有一个活跃记录）
ALTER TABLE student_classes ADD CONSTRAINT uk_student_classes_student_class_active 
    UNIQUE (student_id, class_id, is_active) 
    WHERE is_active = true;

-- 添加检查约束
ALTER TABLE student_classes ADD CONSTRAINT chk_student_classes_left_after_joined 
    CHECK (left_at IS NULL OR left_at >= joined_at);

-- 添加索引
CREATE INDEX idx_student_classes_student_id ON student_classes(student_id);
CREATE INDEX idx_student_classes_class_id ON student_classes(class_id);
CREATE INDEX idx_student_classes_joined_at ON student_classes(joined_at);
CREATE INDEX idx_student_classes_is_active ON student_classes(is_active);
CREATE INDEX idx_student_classes_deleted_at ON student_classes(deleted_at);

-- 添加外键约束
ALTER TABLE student_classes ADD CONSTRAINT fk_student_classes_student_id 
    FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE;

ALTER TABLE student_classes ADD CONSTRAINT fk_student_classes_class_id 
    FOREIGN KEY (class_id) REFERENCES classes(id) ON DELETE CASCADE;

-- 添加表和列注释
COMMENT ON TABLE student_classes IS '学生班级关联表';
COMMENT ON COLUMN student_classes.id IS '主键ID';
COMMENT ON COLUMN student_classes.student_id IS '学生ID';
COMMENT ON COLUMN student_classes.class_id IS '班级ID';
COMMENT ON COLUMN student_classes.joined_at IS '加入时间';
COMMENT ON COLUMN student_classes.left_at IS '离开时间';
COMMENT ON COLUMN student_classes.is_active IS '是否激活';
COMMENT ON COLUMN student_classes.created_at IS '创建时间';
COMMENT ON COLUMN student_classes.updated_at IS '更新时间';
COMMENT ON COLUMN student_classes.deleted_at IS '删除时间（软删除）';

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
