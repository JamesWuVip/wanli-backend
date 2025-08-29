package com.wanli.dto;

import com.wanli.entity.Course;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 课程响应DTO
 */
@Data
public class CourseResponseDto {

    private UUID id;
    private String courseCode;
    private String courseName;
    private String courseDescription;
    private Course.GradeLevel gradeLevel;
    private Course.Subject subject;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isActive;
    private Integer lessonCount;

    /**
     * 从Course实体转换为DTO
     */
    public static CourseResponseDto fromEntity(Course course) {
        CourseResponseDto dto = new CourseResponseDto();
        dto.setId(course.getId());
        dto.setCourseCode(course.getCourseCode());
        dto.setCourseName(course.getCourseName());
        dto.setCourseDescription(course.getDescription());
        dto.setGradeLevel(course.getGradeLevel());
        dto.setSubject(course.getSubject());
        dto.setCreatedBy(course.getCreatedBy());
        dto.setCreatedAt(course.getCreatedAt());
        dto.setUpdatedAt(course.getUpdatedAt());
        dto.setIsActive(course.getIsActive());
        dto.setLessonCount(course.getLessonCount());
        return dto;
    }
}