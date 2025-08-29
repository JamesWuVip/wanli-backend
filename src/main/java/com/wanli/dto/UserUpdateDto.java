package com.wanli.dto;

import com.wanli.entity.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 用户更新数据传输对象
 * 
 * @author wanli
 * @version 1.0.0
 */
@Data
public class UserUpdateDto {
    
    @Email(message = "邮箱格式不正确")
    private String email;
    
    @Size(max = 100, message = "姓名长度不能超过100个字符")
    private String fullName;
    
    private UserRole role;
    
    // 手动添加getter方法以解决Lombok注解处理器问题
    public String getEmail() {
        return email;
    }
    
    public String getFullName() {
        return fullName;
    }
    
    public UserRole getRole() {
        return role;
    }
    
    // 手动添加setter方法
    public void setEmail(String email) {
        this.email = email;
    }
    
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    
    public void setRole(UserRole role) {
        this.role = role;
    }
}