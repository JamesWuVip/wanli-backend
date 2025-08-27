package com.wanli.dto;

import com.wanli.entity.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 用户注册数据传输对象
 * 
 * @author wanli
 * @version 1.0.0
 */
@Data
public class UserRegistrationDto {
    
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度必须在3-50个字符之间")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字和下划线")
    private String username;
    
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 100, message = "密码长度必须在6-100个字符之间")
    private String password;
    
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;
    
    @NotBlank(message = "姓名不能为空")
    @Size(max = 100, message = "姓名长度不能超过100个字符")
    private String fullName;
    
    private UserRole role = UserRole.STUDENT; // 默认为学生角色
    
    // 手动添加getter方法以解决Lombok注解处理器问题
    public String getUsername() {
        return username;
    }
    
    public String getPassword() {
        return password;
    }
    
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
    public void setUsername(String username) {
        this.username = username;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
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