package com.wanli.dto;

import com.wanli.entity.UserRole;
import com.wanli.entity.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * 用户信息响应数据传输对象
 * 
 * @author wanli
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDto {
    
    private UUID userId;
    private String username;
    private String email;
    private String fullName;
    private UserRole role;
    private UserStatus status;
    private OffsetDateTime createdAt;
    private OffsetDateTime lastLoginAt;
    private boolean isActive;
    
    // 手动添加builder方法以解决Lombok注解处理器问题
    public static UserResponseDtoBuilder builder() {
        return new UserResponseDtoBuilder();
    }
    
    public static class UserResponseDtoBuilder {
        private UUID userId;
        private String username;
        private String email;
        private String fullName;
        private UserRole role;
        private UserStatus status;
        private OffsetDateTime createdAt;
        private OffsetDateTime lastLoginAt;
        private boolean isActive;
        
        public UserResponseDtoBuilder userId(UUID userId) {
            this.userId = userId;
            return this;
        }
        
        public UserResponseDtoBuilder username(String username) {
            this.username = username;
            return this;
        }
        
        public UserResponseDtoBuilder email(String email) {
            this.email = email;
            return this;
        }
        
        public UserResponseDtoBuilder fullName(String fullName) {
            this.fullName = fullName;
            return this;
        }
        
        public UserResponseDtoBuilder role(UserRole role) {
            this.role = role;
            return this;
        }
        
        public UserResponseDtoBuilder status(UserStatus status) {
            this.status = status;
            return this;
        }
        
        public UserResponseDtoBuilder createdAt(OffsetDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }
        
        public UserResponseDtoBuilder lastLoginAt(OffsetDateTime lastLoginAt) {
            this.lastLoginAt = lastLoginAt;
            return this;
        }
        
        public UserResponseDtoBuilder isActive(boolean isActive) {
            this.isActive = isActive;
            return this;
        }
        
        public UserResponseDto build() {
            return new UserResponseDto(userId, username, email, fullName, role, status, createdAt, lastLoginAt, isActive);
        }
    }
}