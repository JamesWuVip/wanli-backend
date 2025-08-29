package com.wanli.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * 登录响应数据传输对象
 * 
 * @author wanli
 * @version 1.0.0
 */
@Data
public class LoginResponseDto {
    
    public LoginResponseDto() {}
    
    public LoginResponseDto(String accessToken, String tokenType, Long expiresIn, UserInfoDto user) {
        this.accessToken = accessToken;
        this.tokenType = tokenType;
        this.expiresIn = expiresIn;
        this.user = user;
    }
    
    private String accessToken;
    private String tokenType;
    private Long expiresIn;
    private UserInfoDto user;
    
    // 手动添加builder方法以解决Lombok注解处理器问题
    public static LoginResponseDtoBuilder builder() {
        return new LoginResponseDtoBuilder();
    }
    
    public static class LoginResponseDtoBuilder {
        private String accessToken;
        private String tokenType;
        private Long expiresIn;
        private UserInfoDto user;
        
        public LoginResponseDtoBuilder accessToken(String accessToken) {
            this.accessToken = accessToken;
            return this;
        }
        
        public LoginResponseDtoBuilder tokenType(String tokenType) {
            this.tokenType = tokenType;
            return this;
        }
        
        public LoginResponseDtoBuilder expiresIn(Long expiresIn) {
            this.expiresIn = expiresIn;
            return this;
        }
        
        public LoginResponseDtoBuilder user(UserInfoDto user) {
            this.user = user;
            return this;
        }
        
        public LoginResponseDto build() {
            return new LoginResponseDto(accessToken, tokenType, expiresIn, user);
        }
    }
    
    @Data
    public static class UserInfoDto {
        
        public UserInfoDto() {}
        
        public UserInfoDto(UUID userId, String username, String email, String fullName, String role, OffsetDateTime lastLoginAt) {
            this.userId = userId;
            this.username = username;
            this.email = email;
            this.fullName = fullName;
            this.role = role;
            this.lastLoginAt = lastLoginAt;
        }
        private UUID userId;
        private String username;
        private String email;
        private String fullName;
        private String role;
        private OffsetDateTime lastLoginAt;
        
        // 手动添加builder方法以解决Lombok注解处理器问题
        public static UserInfoDtoBuilder builder() {
            return new UserInfoDtoBuilder();
        }
        
        public static class UserInfoDtoBuilder {
            private UUID userId;
            private String username;
            private String email;
            private String fullName;
            private String role;
            private OffsetDateTime lastLoginAt;
            
            public UserInfoDtoBuilder userId(UUID userId) {
                this.userId = userId;
                return this;
            }
            
            public UserInfoDtoBuilder username(String username) {
                this.username = username;
                return this;
            }
            
            public UserInfoDtoBuilder email(String email) {
                this.email = email;
                return this;
            }
            
            public UserInfoDtoBuilder fullName(String fullName) {
                this.fullName = fullName;
                return this;
            }
            
            public UserInfoDtoBuilder role(String role) {
                this.role = role;
                return this;
            }
            
            public UserInfoDtoBuilder lastLoginAt(OffsetDateTime lastLoginAt) {
                this.lastLoginAt = lastLoginAt;
                return this;
            }
            
            public UserInfoDto build() {
                return new UserInfoDto(userId, username, email, fullName, role, lastLoginAt);
            }
        }
    }
}