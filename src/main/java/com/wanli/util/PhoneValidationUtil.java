package com.wanli.util;

import java.util.Random;
import java.util.regex.Pattern;

/**
 * 手机号验证工具类.
 *
 * <p>提供手机号格式验证、验证码生成等功能。</p>
 * <p>支持中国大陆手机号格式验证和6位数字验证码生成。</p>
 *
 * @author JamesWu
 * @since 1.0.0
 */
public final class PhoneValidationUtil {

    /** 中国大陆手机号正则表达式. */
    private static final String CHINA_PHONE_REGEX = "^1[3-9]\\d{9}$";

    /** 手机号验证模式. */
    private static final Pattern PHONE_PATTERN = Pattern.compile(CHINA_PHONE_REGEX);

    /** 验证码长度. */
    private static final int CODE_LENGTH = 6;

    /** 验证码最小值. */
    private static final int CODE_MIN_VALUE = 100000;

    /** 验证码最大值. */
    private static final int CODE_MAX_VALUE = 999999;

    /** 随机数生成器. */
    private static final Random RANDOM = new Random();

    /**
     * 私有构造函数，防止实例化.
     */
    private PhoneValidationUtil() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * 验证手机号格式是否正确.
     *
     * <p>支持中国大陆手机号格式：</p>
     * <ul>
     *   <li>以1开头</li>
     *   <li>第二位为3-9</li>
     *   <li>总长度为11位数字</li>
     * </ul>
     *
     * @param phone 手机号
     * @return 如果格式正确返回true，否则返回false
     */
    public static boolean isValidPhone(final String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }
        return PHONE_PATTERN.matcher(phone.trim()).matches();
    }

    /**
     * 生成6位数字验证码.
     *
     * <p>生成范围：100000-999999</p>
     * <p>确保验证码始终为6位数字，不会出现前导零的情况。</p>
     *
     * @return 6位数字验证码
     */
    public static String generateVerificationCode() {
        final int code = RANDOM.nextInt(CODE_MAX_VALUE - CODE_MIN_VALUE + 1) + CODE_MIN_VALUE;
        return String.valueOf(code);
    }

    /**
     * 验证验证码格式是否正确.
     *
     * <p>验证码必须为6位数字。</p>
     *
     * @param code 验证码
     * @return 如果格式正确返回true，否则返回false
     */
    public static boolean isValidVerificationCode(final String code) {
        if (code == null || code.trim().isEmpty()) {
            return false;
        }
        final String trimmedCode = code.trim();
        return trimmedCode.length() == CODE_LENGTH && trimmedCode.matches("^\\d{6}$");
    }

    /**
     * 格式化手机号显示.
     *
     * <p>将手机号格式化为：138****1234 的形式，用于日志记录和显示。</p>
     * <p>保护用户隐私，避免完整手机号泄露。</p>
     *
     * @param phone 手机号
     * @return 格式化后的手机号，如果输入无效则返回"***"
     */
    public static String maskPhone(final String phone) {
        if (!isValidPhone(phone)) {
            return "***";
        }
        final String trimmedPhone = phone.trim();
        return trimmedPhone.substring(0, 3) + "****" + trimmedPhone.substring(7);
    }

    /**
     * 获取手机号运营商类型.
     *
     * <p>根据手机号前三位判断运营商：</p>
     * <ul>
     *   <li>中国移动：134-139, 147, 150-152, 157-159, 178, 182-184, 187-188, 198</li>
     *   <li>中国联通：130-132, 145, 155-156, 166, 175-176, 185-186</li>
     *   <li>中国电信：133, 149, 153, 173-174, 177, 180-181, 189, 199</li>
     * </ul>
     *
     * @param phone 手机号
     * @return 运营商类型，如果无法识别则返回"UNKNOWN"
     */
    public static String getCarrierType(final String phone) {
        if (!isValidPhone(phone)) {
            return "UNKNOWN";
        }

        final String prefix = phone.substring(0, 3);
        final int prefixNum = Integer.parseInt(prefix);

        // 中国移动
        if ((prefixNum >= 134 && prefixNum <= 139) ||
            prefixNum == 147 ||
            (prefixNum >= 150 && prefixNum <= 152) ||
            (prefixNum >= 157 && prefixNum <= 159) ||
            prefixNum == 178 ||
            (prefixNum >= 182 && prefixNum <= 184) ||
            (prefixNum >= 187 && prefixNum <= 188) ||
            prefixNum == 198) {
            return "CHINA_MOBILE";
        }

        // 中国联通
        if ((prefixNum >= 130 && prefixNum <= 132) ||
            prefixNum == 145 ||
            (prefixNum >= 155 && prefixNum <= 156) ||
            prefixNum == 166 ||
            (prefixNum >= 175 && prefixNum <= 176) ||
            (prefixNum >= 185 && prefixNum <= 186)) {
            return "CHINA_UNICOM";
        }

        // 中国电信
        if (prefixNum == 133 ||
            prefixNum == 149 ||
            prefixNum == 153 ||
            (prefixNum >= 173 && prefixNum <= 174) ||
            prefixNum == 177 ||
            (prefixNum >= 180 && prefixNum <= 181) ||
            prefixNum == 189 ||
            prefixNum == 199) {
            return "CHINA_TELECOM";
        }

        return "UNKNOWN";
    }

    /**
     * 检查两个手机号是否相同.
     *
     * <p>忽略空格和格式差异，只比较数字内容。</p>
     *
     * @param phone1 第一个手机号
     * @param phone2 第二个手机号
     * @return 如果相同返回true，否则返回false
     */
    public static boolean isSamePhone(final String phone1, final String phone2) {
        if (phone1 == null || phone2 == null) {
            return false;
        }
        return phone1.trim().equals(phone2.trim());
    }
}