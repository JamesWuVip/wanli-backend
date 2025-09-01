package com.wanli.repository;

import com.wanli.entity.SmsVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 短信验证码数据访问接口.
 *
 * <p>提供短信验证码的数据库操作方法，包括查询、保存、删除等功能。</p>
 * <p>支持按手机号、验证码类型、过期时间等条件进行查询。</p>
 *
 * @author JamesWu
 * @since 1.0.0
 */
@Repository
public interface SmsVerificationRepository extends JpaRepository<SmsVerification, String> {

    /**
     * 根据手机号和验证码类型查找最新的验证码记录.
     *
     * <p>按创建时间倒序排列，返回最新的一条记录。</p>
     *
     * @param phone 手机号
     * @param codeType 验证码类型
     * @return 最新的验证码记录，如果不存在则返回空
     */
    Optional<SmsVerification> findFirstByPhoneAndCodeTypeOrderByCreatedAtDesc(
            String phone, SmsVerification.CodeType codeType);

    /**
     * 根据手机号、验证码和验证码类型查找可用的验证码记录.
     *
     * <p>查找条件：</p>
     * <ul>
     *   <li>手机号匹配</li>
     *   <li>验证码匹配</li>
     *   <li>验证码类型匹配</li>
     *   <li>未被使用</li>
     *   <li>未过期</li>
     * </ul>
     *
     * @param phone 手机号
     * @param code 验证码
     * @param codeType 验证码类型
     * @param currentTime 当前时间
     * @return 可用的验证码记录，如果不存在则返回空
     */
    Optional<SmsVerification> findByPhoneAndCodeAndCodeTypeAndIsUsedFalseAndExpiresAtAfter(
            String phone, String code, SmsVerification.CodeType codeType, LocalDateTime currentTime);

    /**
     * 根据手机号和验证码类型查找所有验证码记录.
     *
     * <p>按创建时间倒序排列。</p>
     *
     * @param phone 手机号
     * @param codeType 验证码类型
     * @return 验证码记录列表
     */
    List<SmsVerification> findByPhoneAndCodeTypeOrderByCreatedAtDesc(
            String phone, SmsVerification.CodeType codeType);

    /**
     * 根据手机号查找所有验证码记录.
     *
     * <p>按创建时间倒序排列。</p>
     *
     * @param phone 手机号
     * @return 验证码记录列表
     */
    List<SmsVerification> findByPhoneOrderByCreatedAtDesc(String phone);

    /**
     * 查找所有已过期的验证码记录.
     *
     * @param currentTime 当前时间
     * @return 已过期的验证码记录列表
     */
    List<SmsVerification> findByExpiresAtBefore(LocalDateTime currentTime);

    /**
     * 查找指定时间范围内的验证码记录数量.
     *
     * <p>用于限制短信发送频率。</p>
     *
     * @param phone 手机号
     * @param codeType 验证码类型
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 验证码记录数量
     */
    long countByPhoneAndCodeTypeAndCreatedAtBetween(
            String phone, SmsVerification.CodeType codeType, 
            LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 批量标记验证码为已使用.
     *
     * <p>将指定手机号和验证码类型的所有未使用验证码标记为已使用。</p>
     * <p>通常在验证成功后调用，防止重复使用。</p>
     *
     * @param phone 手机号
     * @param codeType 验证码类型
     * @return 更新的记录数量
     */
    @Modifying
    @Query("UPDATE SmsVerification s SET s.isUsed = true " +
           "WHERE s.phone = :phone AND s.codeType = :codeType AND s.isUsed = false")
    int markAllAsUsedByPhoneAndCodeType(@Param("phone") String phone, 
                                       @Param("codeType") SmsVerification.CodeType codeType);

    /**
     * 删除已过期的验证码记录.
     *
     * <p>定期清理过期的验证码记录，释放存储空间。</p>
     *
     * @param currentTime 当前时间
     * @return 删除的记录数量
     */
    @Modifying
    @Query("DELETE FROM SmsVerification s WHERE s.expiresAt < :currentTime")
    int deleteExpiredCodes(@Param("currentTime") LocalDateTime currentTime);

    /**
     * 检查指定手机号在指定时间内是否存在验证码记录.
     *
     * <p>用于防止短信轰炸，限制发送频率。</p>
     *
     * @param phone 手机号
     * @param codeType 验证码类型
     * @param sinceTime 起始时间
     * @return 如果存在则返回true，否则返回false
     */
    boolean existsByPhoneAndCodeTypeAndCreatedAtAfter(
            String phone, SmsVerification.CodeType codeType, LocalDateTime sinceTime);

    /**
     * 查找指定手机号最近发送的验证码记录.
     *
     * <p>不区分验证码类型，按创建时间倒序排列。</p>
     *
     * @param phone 手机号
     * @return 最近的验证码记录，如果不存在则返回空
     */
    Optional<SmsVerification> findFirstByPhoneOrderByCreatedAtDesc(String phone);

    /**
     * 统计指定时间后的验证码记录数量.
     *
     * <p>用于限制短信发送频率。</p>
     *
     * @param phone 手机号
     * @param codeType 验证码类型
     * @param sinceTime 起始时间
     * @return 验证码记录数量
     */
    long countByPhoneAndCodeTypeAndCreatedAtAfter(
            String phone, SmsVerification.CodeType codeType, LocalDateTime sinceTime);

    /**
     * 查找最新的可用验证码记录.
     *
     * <p>查找指定手机号和类型的最新验证码记录，且未过期。</p>
     *
     * @param phone 手机号
     * @param codeType 验证码类型
     * @param currentTime 当前时间
     * @return 最新的可用验证码记录，如果不存在则返回空
     */
    @Query("SELECT s FROM SmsVerification s WHERE s.phone = :phone AND s.codeType = :codeType " +
           "AND s.expiresAt > :currentTime ORDER BY s.createdAt DESC")
    Optional<SmsVerification> findLatestByPhoneAndType(
            @Param("phone") String phone, 
            @Param("codeType") SmsVerification.CodeType codeType, 
            @Param("currentTime") LocalDateTime currentTime);

    /**
     * 批量标记验证码为已使用.
     *
     * <p>将指定手机号和验证码类型的所有未使用验证码标记为已使用。</p>
     *
     * @param phone 手机号
     * @param codeType 验证码类型
     * @return 更新的记录数量
     */
    @Modifying
    @Query("UPDATE SmsVerification s SET s.isUsed = true " +
           "WHERE s.phone = :phone AND s.codeType = :codeType AND s.isUsed = false")
    int markCodesAsUsed(@Param("phone") String phone, 
                       @Param("codeType") SmsVerification.CodeType codeType);

    /**
     * 查找可用的验证码记录.
     *
     * <p>查找指定手机号、验证码和类型的可用验证码记录。</p>
     *
     * @param phone 手机号
     * @param code 验证码
     * @param codeType 验证码类型
     * @param currentTime 当前时间
     * @return 可用的验证码记录，如果不存在则返回空
     */
    @Query("SELECT s FROM SmsVerification s WHERE s.phone = :phone AND s.code = :code " +
           "AND s.codeType = :codeType AND s.isUsed = false AND s.expiresAt > :currentTime")
    Optional<SmsVerification> findAvailableCode(
            @Param("phone") String phone, 
            @Param("code") String code, 
            @Param("codeType") SmsVerification.CodeType codeType, 
            @Param("currentTime") LocalDateTime currentTime);
}