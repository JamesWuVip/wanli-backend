package com.wanli.dto;

import com.wanli.entity.InstitutionStatus;
import lombok.Data;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 机构创建请求DTO
 */
@Data
public class InstitutionCreateDTO {

    /**
     * 机构名称
     */
    @NotBlank(message = "机构名称不能为空")
    @Size(max = 100, message = "机构名称长度不能超过100个字符")
    private String name;

    /**
     * 机构描述
     */
    @Size(max = 500, message = "机构描述长度不能超过500个字符")
    private String description;

    /**
     * 联系邮箱
     */
    @Email(message = "联系邮箱格式不正确")
    @Size(max = 100, message = "联系邮箱长度不能超过100个字符")
    private String contactEmail;

    /**
     * 联系电话
     */
    @Pattern(regexp = "^[0-9-+()\\s]*$", message = "联系电话格式不正确")
    @Size(max = 20, message = "联系电话长度不能超过20个字符")
    private String contactPhone;

    /**
     * 地址
     */
    @Size(max = 200, message = "地址长度不能超过200个字符")
    private String address;

    /**
     * 机构状态
     */
    private InstitutionStatus status = InstitutionStatus.ACTIVE;

    /**
     * 创建者ID
     */
    private String createdBy;
}