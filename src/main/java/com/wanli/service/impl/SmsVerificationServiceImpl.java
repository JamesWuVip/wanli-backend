package com.wanli.service.impl;

import com.wanli.service.SmsService;
import com.wanli.service.SmsVerificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * 短信验证服务实现类.
 *
 * <p>提供短信验证码的发送、验证和管理功能，支持多种使用场景。
 * 包含频率限制、业务逻辑验证和过期清理等功能。</p>
 *
 * @author JamesWu
 * @since 1.0.0
 */
@Service
public class SmsVerificationServiceImpl implements SmsVerificationService {

    /** 日志记录器. */
    private static final Logger log = LoggerFactory.getLogger(SmsVerificationServiceImpl.class);

    /** 验证码有效期（分钟）. */
    @Value("${app.sms.verification.expiry-minutes:5}")
    private int verificationExpiryMinutes;

    /** 发送频率限制（秒）. */
    @Value("${app.sms.verification.rate-limit-seconds:60}")
    private int rateLimitSeconds;

    /** 每日发送限制. */
    @Value("${app.sms.verification.daily-limit:10}")
    private int dailyLimit;

    /** 验证码重试次数限制. */
    @Value("${app.sms.verification.max-retry-count:5}")
    private int maxRetryCount;

    /** Redis缓存前缀. */
    private static final String REDIS_PREFIX = "sms:verification:";
    private static final String RATE_LIMIT_PREFIX = "sms:rate_limit:";
    private static final String DAILY_COUNT_PREFIX = "sms:daily_count:";
    private static final String RETRY_COUNT_PREFIX = "sms:retry_count:";

    /** 短信服务. */
    private final SmsService smsService;

    /** Redis模板. */
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 构造函数.
     *
     * @param smsService 短信服务
     * @param redisTemplate Redis模板
     */
    @Autowired
    public SmsVerificationServiceImpl(final SmsService smsService,
                                     final RedisTemplate<String, Object> redisTemplate) {
        this.smsService = smsService;
        this.redisTemplate = redisTemplate;
    }

    /**
     * 发送短信验证码.
     *
     * @param phoneNumber 手机号码
     * @param scene 使用场景
     * @return 发送结果
     * @throws IllegalArgumentException 当参数不正确时抛出
     * @throws IllegalStateException 当发送频率超限时抛出
     */
    @Override
    public boolean sendVerificationCode(final String phoneNumber, final String scene) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("手机号码不能为空");
        }

        if (scene == null || scene.trim().isEmpty()) {
            throw new IllegalArgumentException("使用场景不能为空");
        }

        if (!smsService.isValidPhoneNumber(phoneNumber)) {
            throw new IllegalArgumentException("手机号格式不正确: " + phoneNumber);
        }

        log.info("开始发送短信验证码: phoneNumber={}, scene={}", phoneNumber, scene);

        try {
            // 检查发送频率限制
            checkRateLimit(phoneNumber, scene);

            // 检查每日发送限制
            checkDailyLimit(phoneNumber);

            // 生成验证码
            String verificationCode = smsService.generateVerificationCode();

            // 发送短信
            boolean sendResult = smsService.sendVerificationCode(phoneNumber, verificationCode, scene);

            if (sendResult) {
                // 保存验证码到缓存
                saveVerificationCode(phoneNumber, scene, verificationCode);

                // 更新发送频率限制
                updateRateLimit(phoneNumber, scene);

                // 更新每日发送计数
                updateDailyCount(phoneNumber);

                log.info("短信验证码发送成功: phoneNumber={}, scene={}", phoneNumber, scene);
                return true;
            } else {
                log.warn("短信验证码发送失败: phoneNumber={}, scene={}", phoneNumber, scene);
                return false;
            }
        } catch (Exception e) {
            log.error("发送短信验证码异常: phoneNumber={}, scene={}, error={}", 
                     phoneNumber, scene, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 验证短信验证码.
     *
     * @param phoneNumber 手机号码
     * @param verificationCode 验证码
     * @param scene 使用场景
     * @return 验证结果
     * @throws IllegalArgumentException 当参数不正确时抛出
     * @throws IllegalStateException 当验证次数超限时抛出
     */
    @Override
    public boolean verifyCode(final String phoneNumber, final String verificationCode,
                             final String scene) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("手机号码不能为空");
        }

        if (verificationCode == null || verificationCode.trim().isEmpty()) {
            throw new IllegalArgumentException("验证码不能为空");
        }

        if (scene == null || scene.trim().isEmpty()) {
            throw new IllegalArgumentException("使用场景不能为空");
        }

        if (!smsService.isValidPhoneNumber(phoneNumber)) {
            throw new IllegalArgumentException("手机号格式不正确: " + phoneNumber);
        }

        log.info("开始验证短信验证码: phoneNumber={}, scene={}", phoneNumber, scene);

        try {
            // 检查验证重试次数
            checkRetryLimit(phoneNumber, scene);

            // 从缓存获取验证码
            String cachedCode = getCachedVerificationCode(phoneNumber, scene);

            if (cachedCode == null) {
                log.warn("验证码不存在或已过期: phoneNumber={}, scene={}", phoneNumber, scene);
                incrementRetryCount(phoneNumber, scene);
                return false;
            }

            // 验证码匹配检查
            boolean isValid = cachedCode.equals(verificationCode.trim());

            if (isValid) {
                // 验证成功，清除缓存的验证码和重试计数
                clearVerificationCode(phoneNumber, scene);
                clearRetryCount(phoneNumber, scene);
                log.info("短信验证码验证成功: phoneNumber={}, scene={}", phoneNumber, scene);
                return true;
            } else {
                // 验证失败，增加重试计数
                incrementRetryCount(phoneNumber, scene);
                log.warn("短信验证码验证失败: phoneNumber={}, scene={}", phoneNumber, scene);
                return false;
            }
        } catch (Exception e) {
            log.error("验证短信验证码异常: phoneNumber={}, scene={}, error={}", 
                     phoneNumber, scene, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 检查验证码是否存在且未过期.
     *
     * @param phoneNumber 手机号码
     * @param scene 使用场景
     * @return 验证码存在且未过期返回true，否则返回false
     */
    @Override
    public boolean isCodeValid(final String phoneNumber, final String scene) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return false;
        }

        if (scene == null || scene.trim().isEmpty()) {
            return false;
        }

        try {
            String cachedCode = getCachedVerificationCode(phoneNumber, scene);
            return cachedCode != null;
        } catch (Exception e) {
            log.error("检查验证码有效性异常: phoneNumber={}, scene={}, error={}", 
                     phoneNumber, scene, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 清除指定手机号和场景的验证码.
     *
     * @param phoneNumber 手机号码
     * @param scene 使用场景
     */
    @Override
    public void clearVerificationCode(final String phoneNumber, final String scene) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return;
        }

        if (scene == null || scene.trim().isEmpty()) {
            return;
        }

        try {
            String key = buildVerificationKey(phoneNumber, scene);
            redisTemplate.delete(key);
            log.debug("清除验证码缓存: phoneNumber={}, scene={}", phoneNumber, scene);
        } catch (Exception e) {
            log.error("清除验证码缓存异常: phoneNumber={}, scene={}, error={}", 
                     phoneNumber, scene, e.getMessage(), e);
        }
    }

    /**
     * 获取剩余发送次数.
     *
     * @param phoneNumber 手机号码
     * @return 剩余发送次数
     */
    @Override
    public int getRemainingDailyCount(final String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return 0;
        }

        try {
            String key = buildDailyCountKey(phoneNumber);
            Object count = redisTemplate.opsForValue().get(key);
            int usedCount = count != null ? (Integer) count : 0;
            return Math.max(0, dailyLimit - usedCount);
        } catch (Exception e) {
            log.error("获取剩余发送次数异常: phoneNumber={}, error={}", phoneNumber, e.getMessage(), e);
            return 0;
        }
    }

    /**
     * 检查发送频率限制.
     *
     * @param phoneNumber 手机号码
     * @param scene 使用场景
     * @throws IllegalStateException 当发送频率超限时抛出
     */
    private void checkRateLimit(final String phoneNumber, final String scene) {
        String key = buildRateLimitKey(phoneNumber, scene);
        Boolean exists = redisTemplate.hasKey(key);
        
        if (Boolean.TRUE.equals(exists)) {
            Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
            throw new IllegalStateException(
                String.format("发送频率过快，请等待 %d 秒后重试", ttl != null ? ttl : rateLimitSeconds)
            );
        }
    }

    /**
     * 检查每日发送限制.
     *
     * @param phoneNumber 手机号码
     * @throws IllegalStateException 当每日发送次数超限时抛出
     */
    private void checkDailyLimit(final String phoneNumber) {
        String key = buildDailyCountKey(phoneNumber);
        Object count = redisTemplate.opsForValue().get(key);
        int usedCount = count != null ? (Integer) count : 0;
        
        if (usedCount >= dailyLimit) {
            throw new IllegalStateException(
                String.format("今日发送次数已达上限 %d 次，请明日再试", dailyLimit)
            );
        }
    }

    /**
     * 检查验证重试次数限制.
     *
     * @param phoneNumber 手机号码
     * @param scene 使用场景
     * @throws IllegalStateException 当验证次数超限时抛出
     */
    private void checkRetryLimit(final String phoneNumber, final String scene) {
        String key = buildRetryCountKey(phoneNumber, scene);
        Object count = redisTemplate.opsForValue().get(key);
        int retryCount = count != null ? (Integer) count : 0;
        
        if (retryCount >= maxRetryCount) {
            throw new IllegalStateException(
                String.format("验证失败次数过多，请重新获取验证码")
            );
        }
    }

    /**
     * 保存验证码到缓存.
     *
     * @param phoneNumber 手机号码
     * @param scene 使用场景
     * @param verificationCode 验证码
     */
    private void saveVerificationCode(final String phoneNumber, final String scene,
                                     final String verificationCode) {
        String key = buildVerificationKey(phoneNumber, scene);
        redisTemplate.opsForValue().set(key, verificationCode, 
                                       Duration.ofMinutes(verificationExpiryMinutes));
        log.debug("保存验证码到缓存: phoneNumber={}, scene={}, expiry={}分钟", 
                 phoneNumber, scene, verificationExpiryMinutes);
    }

    /**
     * 从缓存获取验证码.
     *
     * @param phoneNumber 手机号码
     * @param scene 使用场景
     * @return 验证码，不存在返回null
     */
    private String getCachedVerificationCode(final String phoneNumber, final String scene) {
        String key = buildVerificationKey(phoneNumber, scene);
        Object code = redisTemplate.opsForValue().get(key);
        return code != null ? (String) code : null;
    }

    /**
     * 更新发送频率限制.
     *
     * @param phoneNumber 手机号码
     * @param scene 使用场景
     */
    private void updateRateLimit(final String phoneNumber, final String scene) {
        String key = buildRateLimitKey(phoneNumber, scene);
        redisTemplate.opsForValue().set(key, LocalDateTime.now().toString(), 
                                       Duration.ofSeconds(rateLimitSeconds));
    }

    /**
     * 更新每日发送计数.
     *
     * @param phoneNumber 手机号码
     */
    private void updateDailyCount(final String phoneNumber) {
        String key = buildDailyCountKey(phoneNumber);
        redisTemplate.opsForValue().increment(key);
        // 设置过期时间为第二天凌晨
        redisTemplate.expireAt(key, 
            LocalDateTime.now().plusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0));
    }

    /**
     * 增加验证重试计数.
     *
     * @param phoneNumber 手机号码
     * @param scene 使用场景
     */
    private void incrementRetryCount(final String phoneNumber, final String scene) {
        String key = buildRetryCountKey(phoneNumber, scene);
        redisTemplate.opsForValue().increment(key);
        redisTemplate.expire(key, Duration.ofMinutes(verificationExpiryMinutes));
    }

    /**
     * 清除验证重试计数.
     *
     * @param phoneNumber 手机号码
     * @param scene 使用场景
     */
    private void clearRetryCount(final String phoneNumber, final String scene) {
        String key = buildRetryCountKey(phoneNumber, scene);
        redisTemplate.delete(key);
    }

    /**
     * 构建验证码缓存键.
     *
     * @param phoneNumber 手机号码
     * @param scene 使用场景
     * @return 缓存键
     */
    private String buildVerificationKey(final String phoneNumber, final String scene) {
        return REDIS_PREFIX + phoneNumber + ":" + scene;
    }

    /**
     * 构建频率限制缓存键.
     *
     * @param phoneNumber 手机号码
     * @param scene 使用场景
     * @return 缓存键
     */
    private String buildRateLimitKey(final String phoneNumber, final String scene) {
        return RATE_LIMIT_PREFIX + phoneNumber + ":" + scene;
    }

    /**
     * 构建每日计数缓存键.
     *
     * @param phoneNumber 手机号码
     * @return 缓存键
     */
    private String buildDailyCountKey(final String phoneNumber) {
        return DAILY_COUNT_PREFIX + phoneNumber;
    }

    /**
     * 构建重试计数缓存键.
     *
     * @param phoneNumber 手机号码
     * @param scene 使用场景
     * @return 缓存键
     */
    private String buildRetryCountKey(final String phoneNumber, final String scene) {
        return RETRY_COUNT_PREFIX + phoneNumber + ":" + scene;
    }
}