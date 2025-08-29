package com.wanli.dto;

import com.wanli.entity.InstitutionStatus;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 机构响应DTO
 */
@Data
public class InstitutionResponseDTO {

    /**
     * 机构ID
     */
    private String id;

    /**
     * 机构名称
     */
    private String name;

    /**
     * 机构描述
     */
    private String description;

    /**
     * 联系邮箱
     */
    private String contactEmail;

    /**
     * 联系电话
     */
    private String contactPhone;

    /**
     * 地址
     */
    private String address;

    /**
     * 机构状态
     */
    private InstitutionStatus status;

    /**
     * 创建者ID
     */
    private String createdBy;

    /**
     * 更新者ID
     */
    private String updatedBy;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 删除时间
     */
    private LocalDateTime deletedAt;

    /**
     * 用户数量（关联的用户数）
     */
    private Long userCount;

    /**
     * 课程数量（关联的课程数）
     */
    private Long courseCount;

    /**
     * 学员数量（关联的学员数）
     */
    private Long studentCount;
}