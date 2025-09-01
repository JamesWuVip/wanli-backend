package com.wanli.service;

/**
 * 邮箱服务接口.
 *
 * <p>提供邮箱相关的服务功能，包括发送验证邮件、密码重置邮件等。</p>
 *
 * @author JamesWu
 * @since 1.0.0
 */
public interface EmailService {

    /**
     * 发送邮箱验证邮件.
     *
     * <p>向指定邮箱地址发送验证邮件，包含验证链接和验证码。</p>
     *
     * @param email 目标邮箱地址
     * @param username 用户名
     * @param verificationToken 验证令牌
     * @return 发送结果，成功返回true，失败返回false
     * @throws IllegalArgumentException 当邮箱地址格式不正确时抛出
     */
    boolean sendEmailVerification(String email, String username, String verificationToken);

    /**
     * 发送密码重置邮件.
     *
     * <p>向指定邮箱地址发送密码重置邮件，包含重置链接。</p>
     *
     * @param email 目标邮箱地址
     * @param username 用户名
     * @param resetToken 重置令牌
     * @return 发送结果，成功返回true，失败返回false
     * @throws IllegalArgumentException 当邮箱地址格式不正确时抛出
     */
    boolean sendPasswordResetEmail(String email, String username, String resetToken);

    /**
     * 发送欢迎邮件.
     *
     * <p>向新注册用户发送欢迎邮件。</p>
     *
     * @param email 目标邮箱地址
     * @param username 用户名
     * @return 发送结果，成功返回true，失败返回false
     * @throws IllegalArgumentException 当邮箱地址格式不正确时抛出
     */
    boolean sendWelcomeEmail(String email, String username);

    /**
     * 验证邮箱地址格式.
     *
     * <p>检查邮箱地址是否符合标准格式。</p>
     *
     * @param email 邮箱地址
     * @return 格式正确返回true，否则返回false
     */
    boolean isValidEmailFormat(String email);

    /**
     * 生成邮箱验证令牌.
     *
     * <p>生成用于邮箱验证的安全令牌。</p>
     *
     * @param email 邮箱地址
     * @param userId 用户ID
     * @return 验证令牌
     */
    String generateVerificationToken(String email, String userId);
}