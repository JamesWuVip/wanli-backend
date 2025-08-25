package com.wanli.service;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * 用户详情服务实现
 * 提供用户认证信息
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 这里应该从数据库加载用户信息
        // 目前使用硬编码的测试用户
        if ("admin".equals(username)) {
            return User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin123")) // 密码: admin123
                    .authorities(Arrays.asList(
                            new SimpleGrantedAuthority("ROLE_ADMIN"),
                            new SimpleGrantedAuthority("ROLE_USER")
                    ))
                    .build();
        } else if ("user".equals(username)) {
            return User.builder()
                    .username("user")
                    .password(passwordEncoder.encode("user123")) // 密码: user123
                    .authorities(List.of(new SimpleGrantedAuthority("ROLE_USER")))
                    .build();
        } else {
            throw new UsernameNotFoundException("User not found: " + username);
        }
    }
}
