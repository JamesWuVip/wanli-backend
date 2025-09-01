package com.wanli.service;

import com.wanli.dto.SmsVerificationRequestDTO;
import com.wanli.dto.SmsVerificationValidateDTO;
import com.wanli.entity.SmsVerification;
import java.util.List;


/**
 * 短信验证服务接口.
 *
 * <p>提供短信验证码的发送、验证和管理功能。</p>
 * <p>支持注册、登录、重置密码等多种场景的短信验证。</p>
 *
 * @author JamesWu
 * @since 1.0.0
 */
public interface SmsVerificationService {

    /**
     * 发送短信验证码.
     *
     * <p>根据请求类型发送相应的验证码短信：</p>
     * <ul>
     *   <li>注册验证码：验证手机号未被注册</li>
     *   <li>登录验证码：验证手机号已注册</li>
     *   <li>重置密码验证码：验证手机号已注册</li>
     * </ul>
     *
     * <p>发送前会进行以下检查：</p>
     * <ul>
     *   <li>手机号格式验证</li>
     *   <li>发送频率限制（同一手机号1分钟内只能发送1次）</li>
     *   <li>每日发送次数限制（同一手机号每天最多10次）</li>
     *   <li>业务逻辑验证（如注册时检查手机号是否已存在）</li>
     * </ul>
     *
     * @param requestDTO 发送验证码请求
     * @return 发送结果消息
     * @throws IllegalArgumentException 当手机号格式不正确时
     * @throws RuntimeException 当发送频率超限或业务验证失败时
     */
    boolean sendVerificationCode(String phone, SmsVerification.CodeType codeType);

    /**
     * 验证短信验证码.
     *
     * <p>验证用户输入的验证码是否正确和有效：</p>
     * <ul>
     *   <li>验证码格式检查</li>
     *   <li>验证码是否存在</li>
     *   <li>验证码是否已过期</li>
     *   <li>验证码是否已被使用</li>
     *   <li>验证码类型是否匹配</li>
     * </ul>
     *
     * <p>验证成功后会自动标记验证码为已使用状态。</p>
     *
     * @param validateDTO 验证码验证请求
     * @return 验证是否成功
     * @throws IllegalArgumentException 当参数格式不正确时
     */
    boolean validateVerificationCode(String phone, String code, SmsVerification.CodeType codeType);

    /**
     * 检查验证码是否有效.
     *
     * <p>检查指定手机号和类型的验证码是否存在且有效，但不标记为已使用。</p>
     * <p>用于在业务流程中预检查验证码状态。</p>
     *
     * @param phone 手机号
     * @param codeType 验证码类型
     * @return 如果存在有效验证码返回true，否则返回false
     */
    List<SmsVerification> getVerificationHistory(String phone, SmsVerification.CodeType codeType);

    /**
     * 获取最新的验证码记录.
     *
     * <p>获取指定手机号和类型的最新验证码记录，用于业务逻辑判断。</p>
     *
     * @param phone 手机号
     * @param codeType 验证码类型
     * @return 最新的验证码记录，如果不存在返回null
     */
    void cleanupExpiredCodes(SmsVerification.CodeType codeType);

    /**
     * 清理过期的验证码记录.
     *
     * <p>定期清理数据库中的过期验证码记录，释放存储空间。</p>
     * <p>建议通过定时任务调用此方法。</p>
     *
     * @return 清理的记录数量
     */
    boolean isVerificationCodeAvailable(String phone, SmsVerification.CodeType codeType);

    /**
     * 检查发送频率限制.
     *
     * <p>检查指定手机号是否可以发送验证码：</p>
     * <ul>
     *   <li>1分钟内只能发送1次</li>
     *   <li>每天最多发送10次</li>
     * </ul>
     *
     * @param phone 手机号
     * @param codeType 验证码类型
     * @return 如果可以发送返回true，否则返回false
     */
    boolean canSendVerificationCode(String phone, SmsVerification.CodeType codeType);

    /**
     * 获取下次可发送时间.
     *
     * <p>获取指定手机号下次可以发送验证码的时间戳。</p>
     * <p>用于前端显示倒计时。</p>
     *
     * @param phone 手机号
     * @param codeType 验证码类型
     * @return 下次可发送的时间戳（毫秒），如果可以立即发送返回0
     */
    long getNextSendTime(String phone, SmsVerification.CodeType codeType);

    /**
     * 标记验证码为已使用.
     *
     * <p>手动标记指定的验证码为已使用状态。</p>
     * <p>用于某些特殊业务场景。</p>
     *
     * @param phone 手机号
     * @param code 验证码
     * @param codeType 验证码类型
     * @return 标记是否成功
     */
    boolean markCodeAsUsed(String phone, String code, SmsVerification.CodeType codeType);
}