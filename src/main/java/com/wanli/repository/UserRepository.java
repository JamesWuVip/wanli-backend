package com.wanli.repository;

import com.wanli.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 用户数据访问层接口.
 * 
 * <p>提供用户实体的数据库操作方法，包括基本的CRUD操作和复杂的查询功能。
 * 支持用户认证、用户管理、数据统计等业务场景的数据访问需求。</p>
 */
@Repository
public interface UserRepository extends JpaRepository<User, String> {

    /**
     * 根据用户名查询用户.
     *
     * @param username 用户名.
     * @return 用户信息（如果存在）.
     */
    Optional<User> findByUsername(String username);

    /**
     * 根据邮箱查询用户.
     *
     * @param email 邮箱地址.
     * @return 用户信息（如果存在）.
     */
    Optional<User> findByEmail(String email);

    /**
     * 根据手机号查询用户.
     *
     * @param phone 手机号.
     * @return 用户信息（如果存在）.
     */
    Optional<User> findByPhone(String phone);

    /**
     * 根据用户名或邮箱查询用户（用于登录）.
     *
     * @param username 用户名.
     * @param email 邮箱地址.
     * @return 用户信息（如果存在）.
     */
    Optional<User> findByUsernameOrEmail(String username, String email);

    /**
     * 检查用户名是否已存在.
     *
     * @param username 用户名.
     * @return 如果存在返回true，否则返回false.
     */
    boolean existsByUsername(String username);

    /**
     * 检查邮箱是否已存在.
     *
     * @param email 邮箱地址.
     * @return 如果存在返回true，否则返回false.
     */
    boolean existsByEmail(String email);

    /**
     * 检查手机号是否已存在.
     *
     * @param phone 手机号.
     * @return 如果存在返回true，否则返回false.
     */
    boolean existsByPhone(String phone);

    /**
     * 根据用户状态分页查询用户.
     *
     * @param status 用户状态.
     * @param pageable 分页参数.
     * @return 用户分页列表.
     */
    Page<User> findByStatus(User.UserStatus status, Pageable pageable);

    /**
     * 统计指定状态的用户数量.
     *
     * @param status 用户状态.
     * @return 用户数量.
     */
    long countByStatus(User.UserStatus status);

    /**
     * 查询已验证邮箱的用户.
     *
     * @param pageable 分页参数.
     * @return 用户分页列表.
     */
    Page<User> findByEmailVerifiedTrue(Pageable pageable);

    /**
     * 查询已验证手机号的用户.
     *
     * @param pageable 分页参数.
     * @return 用户分页列表.
     */
    Page<User> findByPhoneVerifiedTrue(Pageable pageable);

    /**
     * 根据创建时间范围查询用户.
     *
     * @param startTime 开始时间.
     * @param endTime 结束时间.
     * @param pageable 分页参数.
     * @return 用户分页列表.
     */
    Page<User> findByCreatedAtBetween(LocalDateTime startTime,
                                      LocalDateTime endTime,
                                      Pageable pageable);

    /**
     * 根据最后登录时间范围查询用户.
     *
     * @param startTime 开始时间.
     * @param endTime 结束时间.
     * @param pageable 分页参数.
     * @return 用户分页列表.
     */
    Page<User> findByLastLoginAtBetween(LocalDateTime startTime,
                                        LocalDateTime endTime,
                                        Pageable pageable);

    /**
     * 查询长时间未登录的用户.
     *
     * @param lastLoginBefore 最后登录时间之前.
     * @param pageable 分页参数.
     * @return 用户分页列表.
     */
    Page<User> findByLastLoginAtBeforeOrLastLoginAtIsNull(
            LocalDateTime lastLoginBefore, Pageable pageable);

    /**
     * 根据全名模糊查询用户（忽略大小写）.
     *
     * @param fullName 全名关键字.
     * @param pageable 分页参数.
     * @return 用户分页列表.
     */
    Page<User> findByFullNameContainingIgnoreCase(String fullName,
                                                  Pageable pageable);

    /**
     * 根据用户名模糊查询用户（忽略大小写）.
     *
     * @param username 用户名关键字.
     * @param pageable 分页参数.
     * @return 用户分页列表.
     */
    Page<User> findByUsernameContainingIgnoreCase(String username,
                                                  Pageable pageable);

    /**
     * 根据邮箱模糊查询用户（忽略大小写）.
     *
     * @param email 邮箱关键字.
     * @param pageable 分页参数.
     * @return 用户分页列表.
     */
    Page<User> findByEmailContainingIgnoreCase(String email,
                                               Pageable pageable);

    /**
     * 多条件模糊查询用户（用户名、手机号、全名）.
     *
     * @param keyword 搜索关键字.
     * @param pageable 分页参数.
     * @return 用户分页列表.
     */
    @Query("SELECT u FROM User u WHERE "
            + "LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
            + "LOWER(u.phone) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
            + "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<User> findByKeyword(@Param("keyword") String keyword,
                              Pageable pageable);

    /**
     * 查询活跃用户（最近30天内登录过的用户）.
     *
     * @param thirtyDaysAgo 30天前的时间.
     * @param pageable 分页参数.
     * @return 用户分页列表.
     */
    @Query("SELECT u FROM User u WHERE u.lastLoginAt >= :thirtyDaysAgo "
            + "AND u.status = 'ACTIVE'")
    Page<User> findActiveUsers(
            @Param("thirtyDaysAgo") LocalDateTime thirtyDaysAgo,
            Pageable pageable);

    /**
     * 统计活跃用户数量.
     *
     * @param thirtyDaysAgo 30天前的时间.
     * @return 活跃用户数量.
     */
    @Query("SELECT COUNT(u) FROM User u WHERE "
            + "u.lastLoginAt >= :thirtyDaysAgo "
            + "AND u.status = 'ACTIVE'")
    long countActiveUsers(
            @Param("thirtyDaysAgo")
            LocalDateTime thirtyDaysAgo);

    /**
     * 查询新注册用户（最近7天内注册的用户）.
     *
     * @param sevenDaysAgo 7天前的时间.
     * @param pageable 分页参数.
     * @return 用户分页列表.
     */
    @Query("SELECT u FROM User u WHERE u.createdAt >= :sevenDaysAgo")
    Page<User> findNewUsers(
            @Param("sevenDaysAgo") LocalDateTime sevenDaysAgo,
            Pageable pageable);

    /**
     * 统计新注册用户数量.
     *
     * @param sevenDaysAgo 7天前的时间.
     * @return 新注册用户数量.
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt >= :sevenDaysAgo")
    long countNewUsers(
            @Param("sevenDaysAgo") LocalDateTime sevenDaysAgo);

    /**
     * 查询待验证邮箱的用户.
     *
     * @param status 用户状态.
     * @param pageable 分页参数.
     * @return 用户分页列表.
     */
    Page<User> findByEmailVerifiedFalseAndStatus(User.UserStatus status,
                                                  Pageable pageable);

    /**
     * 查询待验证手机号的用户.
     *
     * @param status 用户状态.
     * @param pageable 分页参数.
     * @return 用户分页列表.
     */
    Page<User> findByPhoneVerifiedFalseAndStatus(User.UserStatus status,
                                                  Pageable pageable);

    /**
     * 根据创建者查询用户.
     *
     * @param createdBy 创建者.
     * @param pageable 分页参数.
     * @return 用户分页列表.
     */
    Page<User> findByCreatedBy(String createdBy, Pageable pageable);

    /**
     * 批量查询用户.
     *
     * @param ids 用户ID列表.
     * @return 用户列表.
     */
    List<User> findByIdIn(List<String> ids);

    /**
     * 查询指定状态的用户列表（不分页）.
     *
     * @param status 用户状态.
     * @return 用户列表.
     */
    List<User> findByStatus(User.UserStatus status);

    /**
     * 删除指定时间之前创建的已删除用户.
     *
     * @param deletedBefore 删除时间之前.
     * @param status 用户状态（已删除）.
     */
    @Query("DELETE FROM User u WHERE u.updatedAt < :deletedBefore "
            + "AND u.status = :status")
    void deleteOldDeletedUsers(
            @Param("deletedBefore") LocalDateTime deletedBefore,
            @Param("status") User.UserStatus status);
}