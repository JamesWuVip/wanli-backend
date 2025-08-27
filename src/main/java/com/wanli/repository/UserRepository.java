package com.wanli.repository;

import com.wanli.entity.User;
import com.wanli.entity.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 用户数据访问层
 * 
 * @author wanli
 * @version 1.0.0
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    
    /**
     * 根据用户名查找用户
     */
    Optional<User> findByUsername(String username);
    
    /**
     * 根据邮箱查找用户
     */
    Optional<User> findByEmail(String email);
    
    /**
     * 检查用户名是否存在
     */
    boolean existsByUsername(String username);
    
    /**
     * 检查邮箱是否存在
     */
    boolean existsByEmail(String email);
    
    /**
     * 根据状态查找用户
     */
    List<User> findByStatus(UserStatus status);
    
    /**
     * 更新用户最后登录时间
     */
    @Modifying
    @Query("UPDATE User u SET u.lastLoginAt = :lastLoginAt WHERE u.id = :userId")
    void updateLastLoginTime(@Param("userId") UUID userId, @Param("lastLoginAt") OffsetDateTime lastLoginAt);
    
    /**
     * 锁定用户账户
     */
    @Modifying
    @Query("UPDATE User u SET u.lockedUntil = :lockedUntil WHERE u.id = :userId")
    void lockUser(@Param("userId") UUID userId, @Param("lockedUntil") OffsetDateTime lockedUntil);
    
    /**
     * 解锁过期的用户账户
     */
    @Modifying
    @Query("UPDATE User u SET u.lockedUntil = null, u.loginAttempts = 0 WHERE u.lockedUntil < :now")
    int unlockExpiredUsers(@Param("now") OffsetDateTime now);
}