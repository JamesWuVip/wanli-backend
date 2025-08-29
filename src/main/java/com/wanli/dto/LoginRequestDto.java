package com.wanli.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 登录请求数据传输对象
 * 
 * @author wanli
 * @version 1.0.0
 */
@Data
public class LoginRequestDto {
    
    public LoginRequestDto() {}
    
    public LoginRequestDto(String username, String password) {
        this.username = username;
        this.password = password;
    }
    
    @NotBlank(message = "用户名不能为空")
    private String username;
    
    @NotBlank(message = "密码不能为空")
    private String password;
    
    // 手动添加getter方法以解决Lombok注解处理器问题
    public String getUsername() {
        return username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
}