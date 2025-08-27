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
 * 用户数据访问层接口
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
     * 根据用户状态查找用户
     */
    List<User> findByStatus(UserStatus status);
    
    /**
     * 查找锁定的用户（锁定时间未过期）
     */
    @Query("SELECT u FROM User u WHERE u.lockedUntil IS NOT NULL AND u.lockedUntil > :now")
    List<User> findLockedUsers(@Param("now") OffsetDateTime now);
    
    /**
     * 查找需要解锁的用户（锁定时间已过期）
     */
    @Query("SELECT u FROM User u WHERE u.lockedUntil IS NOT NULL AND u.lockedUntil <= :now")
    List<User> findUsersToUnlock(@Param("now") OffsetDateTime now);
    
    /**
     * 批量解锁用户
     */
    @Modifying
    @Query("UPDATE User u SET u.lockedUntil = NULL, u.loginAttempts = 0 WHERE u.lockedUntil IS NOT NULL AND u.lockedUntil <= :now")
    int unlockExpiredUsers(@Param("now") OffsetDateTime now);
    
    /**
     * 更新用户最后登录时间
     */
    @Modifying
    @Query("UPDATE User u SET u.lastLoginAt = :loginTime WHERE u.id = :userId")
    int updateLastLoginTime(@Param("userId") UUID userId, @Param("loginTime") OffsetDateTime loginTime);
    
    /**
     * 重置用户登录尝试次数
     */
    @Modifying
    @Query("UPDATE User u SET u.loginAttempts = 0 WHERE u.id = :userId")
    int resetLoginAttempts(@Param("userId") UUID userId);
    
    /**
     * 增加用户登录尝试次数
     */
    @Modifying
    @Query("UPDATE User u SET u.loginAttempts = u.loginAttempts + 1 WHERE u.id = :userId")
    int incrementLoginAttempts(@Param("userId") UUID userId);
    
    /**
     * 锁定用户账户
     */
    @Modifying
    @Query("UPDATE User u SET u.lockedUntil = :lockUntil WHERE u.id = :userId")
    int lockUser(@Param("userId") UUID userId, @Param("lockUntil") OffsetDateTime lockUntil);
}