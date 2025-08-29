package com.wanli.dto;

import com.wanli.entity.InstitutionStatus;
import lombok.Data;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

/**
 * 机构查询请求DTO
 */
@Data
public class InstitutionQueryDTO {

    /**
     * 机构名称关键字（模糊查询）
     */
    @Size(max = 100, message = "机构名称关键字长度不能超过100个字符")
    private String name;

    /**
     * 机构状态
     */
    private InstitutionStatus status;

    /**
     * 创建者ID
     */
    private String createdBy;

    /**
     * 页码（从0开始）
     */
    @Min(value = 0, message = "页码不能小于0")
    private Integer page = 0;

    /**
     * 每页大小
     */
    @Min(value = 1, message = "每页大小不能小于1")
    private Integer size = 10;

    /**
     * 排序字段
     */
    private String sortBy = "createdAt";

    /**
     * 排序方向（asc/desc）
     */
    private String sortDirection = "desc";
}