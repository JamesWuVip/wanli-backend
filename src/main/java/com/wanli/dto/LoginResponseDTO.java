package com.wanli.dto;

import java.time.LocalDateTime;

/**
 * 登录响应DTO.
     *
 * <p>用于返回用户登录成功后的响应信息，包含JWT令牌、用户信息和过期时间等。</p>.
     *
 * @author JamesWu.
 * @since 1.0.0.
 */
public class LoginResponseDTO {

    /** JWT令牌. */
    private String token;

    /** 用户信息. */
    private UserInfoDto user;

    /** 过期时间（秒）. */
    private long expiresIn;

    /** 过期时间. */
    private LocalDateTime expirationTime;

    /**
     * 默认构造函数.
     */
    public LoginResponseDTO() {
        // 默认构造函数
    }

    /**
     * 带参数的构造函数.
     *
     * @param jwtToken JWT令牌.
     * @param userInfo 用户信息.
     * @param tokenExpiresIn 过期时间（秒）.
     */
    public LoginResponseDTO(final String jwtToken, final UserInfoDto userInfo,
                            final long tokenExpiresIn) {
        this.token = jwtToken;
        this.user = userInfo;
        this.expiresIn = tokenExpiresIn;
    }

    /**
     * 获取JWT令牌.
     *
     * @return JWT令牌.
     */
    public String getToken() {
        return token;
    }

    /**
     * 设置JWT令牌.
     *
     * @param jwtToken JWT令牌.
     */
    public void setToken(final String jwtToken) {
        this.token = jwtToken;
    }

    /**
     * 获取用户信息.
     *
     * @return 用户信息.
     */
    public UserInfoDto getUser() {
        return user;
    }

    /**
     * 设置用户信息.
     *
     * @param userInfo 用户信息.
     */
    public void setUser(final UserInfoDto userInfo) {
        this.user = userInfo;
    }

    /**
     * 获取过期时间（秒）.
     *
     * @return 过期时间（秒）.
     */
    public long getExpiresIn() {
        return expiresIn;
    }

    /**
     * 设置过期时间（秒）.
     *
     * @param tokenExpiresIn 过期时间（秒）.
     */
    public void setExpiresIn(final long tokenExpiresIn) {
        this.expiresIn = tokenExpiresIn;
    }

    /**
     * 获取过期时间.
     *
     * @return 过期时间.
     */
    public LocalDateTime getExpirationTime() {
        return expirationTime;
    }

    /**
     * 设置过期时间.
     *
     * @param tokenExpirationTime 过期时间.
     */
    public void setExpirationTime(final LocalDateTime tokenExpirationTime) {
        this.expirationTime = tokenExpirationTime;
    }

    /**
     * 用户信息DTO.
     *
     * <p>包含用户的基本信息，用于登录响应中返回用户详情。</p>
     */
    public static class UserInfoDto {
        /** 用户ID. */
        private String id;

        /** 用户名. */
        private String username;

        /** 邮箱. */
        private String email;

        /** 全名. */
        private String fullName;

        /** 手机号. */
        private String phone;

        /** 状态. */
        private String status;

        /** 创建时间. */
        private LocalDateTime createdAt;

        /** 更新时间. */
        private LocalDateTime updatedAt;

        /**
         * 默认构造函数.
         */
        public UserInfoDto() {
            // 默认构造函数
        }

        /**
         * 带基本参数的构造函数.
         *
         * @param userId 用户ID.
         * @param userName 用户名.
         * @param userEmail 邮箱.
         * @param userFullName 全名.
         */
        public UserInfoDto(final String userId, final String userName,
                          final String userEmail, final String userFullName) {
            this.id = userId;
            this.username = userName;
            this.email = userEmail;
            this.fullName = userFullName;
        }

        /**
         * 带完整参数的构造函数.
         *
         * @param userId 用户ID.
         * @param userName 用户名.
         * @param userEmail 邮箱.
         * @param userFullName 全名.
         * @param userPhone 手机号.
         * @param userStatus 状态.
         * @param userCreatedAt 创建时间.
         */
        public UserInfoDto(final String userId, final String userName,
                          final String userEmail, final String userFullName,
                          final String userPhone, final String userStatus,
                          final LocalDateTime userCreatedAt) {
            this.id = userId;
            this.username = userName;
            this.email = userEmail;
            this.fullName = userFullName;
            this.phone = userPhone;
            this.status = userStatus;
            this.createdAt = userCreatedAt;
        }

        /**
         * 获取用户ID.
     *
         * @return 用户ID.
         */
        public String getId() {
            return id;
        }

        /**
         * 设置用户ID.
         *
         * @param userId 用户ID.
         */
        public void setId(final String userId) {
            this.id = userId;
        }

        /**
         * 获取用户名.
     *
         * @return 用户名.
         */
        public String getUsername() {
            return username;
        }

        /**
         * 设置用户名.
         *
         * @param userName 用户名.
         */
        public void setUsername(final String userName) {
            this.username = userName;
        }

        /**
         * 获取邮箱.
     *
         * @return 邮箱.
         */
        public String getEmail() {
            return email;
        }

        /**
         * 设置邮箱.
         *
         * @param userEmail 邮箱.
         */
        public void setEmail(final String userEmail) {
            this.email = userEmail;
        }

        /**
         * 获取全名.
     *
         * @return 全名.
         */
        public String getFullName() {
            return fullName;
        }

        /**
         * 设置全名.
         *
         * @param userFullName 全名.
         */
        public void setFullName(final String userFullName) {
            this.fullName = userFullName;
        }

        /**
         * 获取手机号.
     *
         * @return 手机号.
         */
        public String getPhone() {
            return phone;
        }

        /**
         * 设置手机号.
         *
         * @param userPhone 手机号.
         */
        public void setPhone(final String userPhone) {
            this.phone = userPhone;
        }

        /**
         * 获取状态.
     *
         * @return 状态.
         */
        public String getStatus() {
            return status;
        }

        /**
         * 设置状态.
         *
         * @param userStatus 状态.
         */
        public void setStatus(final String userStatus) {
            this.status = userStatus;
        }

        /**
         * 获取创建时间.
     *
         * @return 创建时间.
         */
        public LocalDateTime getCreatedAt() {
            return createdAt;
        }

        /**
         * 设置创建时间.
         *
         * @param userCreatedAt 创建时间.
         */
        public void setCreatedAt(final LocalDateTime userCreatedAt) {
            this.createdAt = userCreatedAt;
        }

        /**
         * 获取更新时间.
     *
         * @return 更新时间.
         */
        public LocalDateTime getUpdatedAt() {
            return updatedAt;
        }

        /**
         * 设置更新时间.
         *
         * @param userUpdatedAt 更新时间.
         */
        public void setUpdatedAt(final LocalDateTime userUpdatedAt) {
            this.updatedAt = userUpdatedAt;
        }
    }
}