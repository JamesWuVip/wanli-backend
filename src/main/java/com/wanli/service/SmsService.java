package com.wanli.service;

/**
 * 短信服务接口
 * 提供短信发送功能
 * 
 * @author wanli-backend
 * @version 1.0.0
 */
public interface SmsService {
    
    /**
     * 发送短信验证码
     * 
     * @param phone 手机号码
     * @param code 验证码
     * @param type 短信类型
     * @return 发送结果
     */
    boolean sendVerificationCode(final String phone, final String code, final String type);
    
    /**
     * 发送通知短信.
     *
     * <p>用于发送各种通知类短信，如订单状态变更、系统通知等。</p>
     *
     * @param phone 手机号
     * @param message 短信内容
     * @return 发送结果，true表示发送成功，false表示发送失败
     * @throws IllegalArgumentException 当手机号格式不正确时抛出
     */
    boolean sendNotification(String phone, String message);

    /**
     * 发送验证短信.
     *
     * <p>发送包含验证码的短信到指定手机号。</p>
     *
     * @param phone 手机号
     * @param code 验证码
     * @return 发送结果，true表示发送成功，false表示发送失败
     * @throws IllegalArgumentException 当手机号格式不正确时抛出
     */
    boolean sendVerificationSms(String phone, String code);

    /**
     * 验证手机号格式是否正确.
     *
     * <p>检查手机号是否符合中国大陆手机号格式。</p>
     *
     * @param phone 手机号
     * @return 格式正确返回true，否则返回false
     */
    boolean isValidPhoneFormat(String phone);
}