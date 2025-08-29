package com.wanli.dto;

import com.wanli.entity.Course;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 创建课程请求DTO
 */
@Data
public class CourseCreateDto {

    @NotBlank(message = "课程名称不能为空")
    @Size(min = 2, max = 100, message = "课程名称长度必须在2-100字符之间")
    private String courseName;

    @Size(max = 500, message = "课程描述不能超过500字符")
    private String courseDescription;

    @NotNull(message = "年级不能为空")
    private Course.GradeLevel gradeLevel;

    @NotNull(message = "学科不能为空")
    private Course.Subject subject;
}