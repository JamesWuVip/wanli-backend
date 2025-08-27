package com.wanli.dto;

import java.time.LocalDateTime;

public class LoginResponseDto {

    private String token;
    private UserInfoDto user;
    private long expiresIn;

    // Constructors
    public LoginResponseDto() {}

    public LoginResponseDto(String token, UserInfoDto user, long expiresIn) {
        this.token = token;
        this.user = user;
        this.expiresIn = expiresIn;
    }

    // Getters and Setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public UserInfoDto getUser() {
        return user;
    }

    public void setUser(UserInfoDto user) {
        this.user = user;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(long expiresIn) {
        this.expiresIn = expiresIn;
    }

    // Inner class for user info
    public static class UserInfoDto {
        private Long id;
        private String username;
        private String email;
        private String role;

        // Constructors
        public UserInfoDto() {}

        public UserInfoDto(Long id, String username, String email, String role) {
            this.id = id;
            this.username = username;
            this.email = email;
            this.role = role;
        }

        // Getters and Setters
        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }
    }
}