package com.wanli.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录响应数据传输对象
 * 用于返回登录成功后的用户信息
 * 
 * @author wanli-team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDTO {
    
    /**
     * 用户ID
     */
    private String userId;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 访问令牌（简化版本，实际项目中应使用JWT）
     */
    private String token;
    
    /**
     * 登录时间戳
     */
    private Long loginTime;
}