package com.wanli.service;

import com.wanli.entity.User;
import com.wanli.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

/**
 * 自定义用户详情服务.
     *
 * <p>实现Spring Security的UserDetailsService接口，用于从数据库加载用户信息。</p>.
 * <p>支持用户状态检查和权限管理。</p>.
     *
 * @author JamesWu.
 * @since 1.0.0.
 */
@Service
@Transactional(readOnly = true)
public class CustomUserDetailsService implements UserDetailsService {

    /** 用户数据访问层，用于查询用户信息. */
    @Autowired
    private UserRepository userRepository;

    /**
     * 根据用户名加载用户信息.
     *
     * <p>从数据库中查找用户，并检查用户状态是否为活跃状态。</p>.
     *
     * @param username 用户名.
     * @return 用户详情对象.
     * @throws UsernameNotFoundException 当用户不存在或已被禁用时抛出.
     */
    @Override
    public UserDetails loadUserByUsername(final String username)
            throws UsernameNotFoundException {
        Optional<User> userOpt = userRepository.findByUsername(username);

        if (!userOpt.isPresent()) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }

        User user = userOpt.get();

        // 检查用户状态
        if (user.getStatus() != User.UserStatus.ACTIVE) {
            throw new UsernameNotFoundException("用户已被禁用: " + username);
        }

        return new CustomUserPrincipal(user);
    }

    /**
     * 自定义用户主体类.
     *
     * <p>实现Spring Security的UserDetails接口，封装用户信息和权限。</p>.
     * <p>提供用户账户状态检查和权限管理功能。</p>.
     */
    public static class CustomUserPrincipal implements UserDetails {

        /** 用户实体对象，包含用户的基本信息. */
        private final User user;

        /**
         * 构造函数.
     *
         * @param userEntity 用户实体.
         */
        public CustomUserPrincipal(final User userEntity) {
            this.user = userEntity;
        }

        /**
         * 获取用户实体.
     *
         * @return 用户实体.
         */
        public User getUser() {
            return user;
        }

        /**
         * 获取用户权限集合.
     *
         * @return 权限集合.
         */
        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            Collection<GrantedAuthority> authorities = new ArrayList<>();
            // 默认给所有用户ROLE_USER权限
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
            return authorities;
        }

        /**
         * 获取用户密码.
     *
         * @return 密码哈希.
         */
        @Override
        public String getPassword() {
            return user.getPasswordHash();
        }

        /**
         * 获取用户名.
     *
         * @return 用户名.
         */
        @Override
        public String getUsername() {
            return user.getUsername();
        }

        /**
         * 检查账户是否未过期.
     *
         * @return 总是返回true，表示账户未过期.
         */
        @Override
        public boolean isAccountNonExpired() {
            return true;
        }

        /**
         * 检查账户是否未锁定.
     *
         * @return 当用户状态为活跃时返回true.
         */
        @Override
        public boolean isAccountNonLocked() {
            return user.getStatus() == User.UserStatus.ACTIVE;
        }

        /**
         * 检查凭据是否未过期.
     *
         * @return 总是返回true，表示凭据未过期.
         */
        @Override
        public boolean isCredentialsNonExpired() {
            return true;
        }

        /**
         * 检查用户是否启用.
     *
         * @return 当用户状态为活跃时返回true.
         */
        @Override
        public boolean isEnabled() {
            return user.getStatus() == User.UserStatus.ACTIVE;
        }
    }
}