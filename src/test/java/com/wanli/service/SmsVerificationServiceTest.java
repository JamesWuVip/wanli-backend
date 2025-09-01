package com.wanli.service;

import com.wanli.entity.SmsVerification;
import com.wanli.repository.SmsVerificationRepository;
import com.wanli.repository.UserRepository;
import com.wanli.service.impl.SmsVerificationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 短信验证服务测试类.
 * 
 * <p>测试短信验证码的发送、验证和管理功能。</p>
 * 
 * @author JamesWu
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class SmsVerificationServiceTest {

    @Mock
    private SmsVerificationRepository smsVerificationRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private SmsVerificationServiceImpl smsVerificationService;

    private SmsVerification mockSmsVerification;
    private final String validPhone = "13800138000";
    private final String validCode = "123456";
    private final SmsVerification.CodeType codeType = SmsVerification.CodeType.REGISTER;

    @BeforeEach
    void setUp() {
        mockSmsVerification = new SmsVerification();
        mockSmsVerification.setId("test-id");
        mockSmsVerification.setPhone(validPhone);
        mockSmsVerification.setCode(validCode);
        mockSmsVerification.setCodeType(codeType);
        mockSmsVerification.setIsUsed(false);
        mockSmsVerification.setCreatedAt(LocalDateTime.now());
        mockSmsVerification.setExpiresAt(LocalDateTime.now().plusMinutes(5));
    }

    @Test
    void should_SendVerificationCode_when_ValidPhoneAndCodeType() {
        // Given
        when(userRepository.existsByPhone(validPhone)).thenReturn(false);
        when(smsVerificationRepository.findLatestByPhoneAndType(eq(validPhone), eq(codeType), any(LocalDateTime.class)))
                .thenReturn(Optional.empty());
        when(smsVerificationRepository.countByPhoneAndCodeTypeAndCreatedAtAfter(
                eq(validPhone), eq(codeType), any(LocalDateTime.class)))
                .thenReturn(0L);
        when(smsVerificationRepository.save(any(SmsVerification.class)))
                .thenReturn(mockSmsVerification);

        // When
        boolean result = smsVerificationService.sendVerificationCode(validPhone, codeType);

        // Then
        assertThat(result).isTrue();
        verify(smsVerificationRepository, times(1)).save(any(SmsVerification.class));
        verify(userRepository, times(1)).existsByPhone(validPhone);
    }

    @Test
    void should_ThrowException_when_InvalidPhoneFormat() {
        // Given
        String invalidPhone = "invalid-phone";

        // When & Then
        assertThatThrownBy(() -> smsVerificationService.sendVerificationCode(invalidPhone, codeType))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("手机号格式不正确");

        verify(smsVerificationRepository, never()).save(any(SmsVerification.class));
    }

    @Test
    void should_ThrowException_when_PhoneAlreadyRegisteredForRegister() {
        // Given
        when(userRepository.existsByPhone(validPhone)).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> smsVerificationService.sendVerificationCode(validPhone, SmsVerification.CodeType.REGISTER))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("该手机号已被注册");

        verify(smsVerificationRepository, never()).save(any(SmsVerification.class));
    }

    @Test
    void should_ThrowException_when_PhoneNotRegisteredForLogin() {
        // Given
        when(userRepository.existsByPhone(validPhone)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> smsVerificationService.sendVerificationCode(validPhone, SmsVerification.CodeType.LOGIN))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("该手机号尚未注册");

        verify(smsVerificationRepository, never()).save(any(SmsVerification.class));
    }

    @Test
    @DisplayName("发送过于频繁时应抛出异常")
    void should_ThrowException_when_SendTooFrequently() {
        // Given
        final String phone = "13800138000";
        final SmsVerification.CodeType codeType = SmsVerification.CodeType.REGISTER;
        
        // Mock 最近发送的验证码（1分钟内）
        final SmsVerification recentCode = new SmsVerification();
        recentCode.setCreatedAt(LocalDateTime.now().minusSeconds(30)); // 30秒前发送
        when(smsVerificationRepository.findLatestByPhoneAndType(eq(phone), eq(codeType), any(LocalDateTime.class)))
            .thenReturn(Optional.of(recentCode));
        
        // When & Then
        final RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            smsVerificationService.sendVerificationCode(phone, codeType);
        });
        
        assertThat(exception.getMessage()).contains("发送过于频繁");
        
        // Verify interactions
        verify(smsVerificationRepository, times(2)).findLatestByPhoneAndType(eq(phone), eq(codeType), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("超过每日发送限制时应抛出异常")
    void should_ThrowException_when_ExceedDailyLimit() {
        // Given
        final String phone = "13800138000";
        final SmsVerification.CodeType codeType = SmsVerification.CodeType.REGISTER;
        
        // Mock 没有最近发送的验证码（通过频率检查）
        when(smsVerificationRepository.findLatestByPhoneAndType(eq(phone), eq(codeType), any(LocalDateTime.class)))
            .thenReturn(Optional.empty());
        
        // Mock 今日发送次数已达上限
        when(smsVerificationRepository.countByPhoneAndCodeTypeAndCreatedAtAfter(
            eq(phone), eq(codeType), any(LocalDateTime.class)))
            .thenReturn(10L); // 假设每日限制为10次
        
        // When & Then
        final RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            smsVerificationService.sendVerificationCode(phone, codeType);
        });
        
        assertThat(exception.getMessage()).contains("超过每日发送限制");
        
        // Verify interactions
        verify(smsVerificationRepository).findLatestByPhoneAndType(eq(phone), eq(codeType), any(LocalDateTime.class));
        verify(smsVerificationRepository).countByPhoneAndCodeTypeAndCreatedAtAfter(eq(phone), eq(codeType), any(LocalDateTime.class));
    }

    @Test
    void should_ReturnTrue_when_VerifyValidCode() {
        // Given
        when(smsVerificationRepository.findAvailableCode(
                eq(validPhone), eq(validCode), eq(codeType), any(LocalDateTime.class)))
                .thenReturn(Optional.of(mockSmsVerification));
        when(smsVerificationRepository.save(any(SmsVerification.class)))
                .thenReturn(mockSmsVerification);

        // When
        boolean result = smsVerificationService.validateVerificationCode(validPhone, validCode, codeType);

        // Then
        assertThat(result).isTrue();
        verify(smsVerificationRepository, times(1)).save(any(SmsVerification.class));
    }

    @Test
    void should_ThrowException_when_VerifyInvalidCodeFormat() {
        // Given
        String invalidCode = "abc"; // 无效格式的验证码

        // When & Then
        assertThatThrownBy(() -> smsVerificationService.validateVerificationCode(validPhone, invalidCode, codeType))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("验证码格式不正确");

        verify(smsVerificationRepository, never()).findAvailableCode(anyString(), anyString(), any(), any(LocalDateTime.class));
        verify(smsVerificationRepository, never()).save(any(SmsVerification.class));
    }

    @Test
    void should_ReturnFalse_when_VerifyNonExistentCode() {
        // Given
        String nonExistentCode = "999999"; // 不存在的验证码
        when(smsVerificationRepository.findAvailableCode(
                eq(validPhone), eq(nonExistentCode), eq(codeType), any(LocalDateTime.class)))
                .thenReturn(Optional.empty());

        // When
        boolean result = smsVerificationService.validateVerificationCode(validPhone, nonExistentCode, codeType);

        // Then
        assertThat(result).isFalse();
        verify(smsVerificationRepository, never()).save(any(SmsVerification.class));
    }

    @Test
    void should_ReturnFalse_when_VerifyExpiredCode() {
        // Given
        when(smsVerificationRepository.findAvailableCode(
                eq(validPhone), eq(validCode), eq(codeType), any(LocalDateTime.class)))
                .thenReturn(Optional.empty()); // 过期的验证码不会被查询到

        // When
        boolean result = smsVerificationService.validateVerificationCode(validPhone, validCode, codeType);

        // Then
        assertThat(result).isFalse();
        verify(smsVerificationRepository, never()).save(any(SmsVerification.class));
    }

    @Test
    void should_ReturnFalse_when_NoVerificationCodeFound() {
        // Given
        when(smsVerificationRepository.findAvailableCode(
                eq(validPhone), eq(validCode), eq(codeType), any(LocalDateTime.class)))
                .thenReturn(Optional.empty());

        // When
        boolean result = smsVerificationService.validateVerificationCode(validPhone, validCode, codeType);

        // Then
        assertThat(result).isFalse();
        verify(smsVerificationRepository, never()).save(any(SmsVerification.class));
    }

    @Test
    void should_ReturnVerificationHistory_when_ValidPhone() {
        // Given
        List<SmsVerification> mockHistory = List.of(mockSmsVerification);
        when(smsVerificationRepository.findByPhoneAndCodeTypeOrderByCreatedAtDesc(validPhone, codeType))
                .thenReturn(mockHistory);

        // When
        List<SmsVerification> result = smsVerificationService.getVerificationHistory(validPhone, codeType);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPhone()).isEqualTo(validPhone);
        verify(smsVerificationRepository, times(1)).findByPhoneAndCodeTypeOrderByCreatedAtDesc(validPhone, codeType);
    }

    @Test
    void should_ReturnEmptyList_when_InvalidPhone() {
        // Given
        String invalidPhone = "invalid-phone";

        // When
        List<SmsVerification> result = smsVerificationService.getVerificationHistory(invalidPhone, codeType);

        // Then
        assertThat(result).isEmpty();
        verify(smsVerificationRepository, never()).findByPhoneAndCodeTypeOrderByCreatedAtDesc(anyString(), any());
    }

    @Test
    void should_CleanupExpiredCodes_when_Called() {
        // Given
        when(smsVerificationRepository.deleteExpiredCodes(any(LocalDateTime.class)))
                .thenReturn(5);

        // When
        smsVerificationService.cleanupExpiredCodes(codeType);

        // Then
        verify(smsVerificationRepository, times(1)).deleteExpiredCodes(any(LocalDateTime.class));
    }

    @Test
    void should_ReturnTrue_when_VerificationCodeIsAvailable() {
        // Given
        mockSmsVerification.setIsUsed(false);
        mockSmsVerification.setExpiresAt(LocalDateTime.now().plusMinutes(3));
        when(smsVerificationRepository.findLatestByPhoneAndType(eq(validPhone), eq(codeType), any(LocalDateTime.class)))
                .thenReturn(Optional.of(mockSmsVerification));

        // When
        boolean result = smsVerificationService.isVerificationCodeAvailable(validPhone, codeType);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void should_ReturnFalse_when_VerificationCodeIsNotAvailable() {
        // Given
        when(smsVerificationRepository.findLatestByPhoneAndType(eq(validPhone), eq(codeType), any(LocalDateTime.class)))
                .thenReturn(Optional.empty());

        // When
        boolean result = smsVerificationService.isVerificationCodeAvailable(validPhone, codeType);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void should_ReturnTrue_when_CanSendVerificationCode() {
        // Given
        when(smsVerificationRepository.findLatestByPhoneAndType(eq(validPhone), eq(codeType), any(LocalDateTime.class)))
                .thenReturn(Optional.empty());
        when(smsVerificationRepository.countByPhoneAndCodeTypeAndCreatedAtAfter(
                eq(validPhone), eq(codeType), any(LocalDateTime.class)))
                .thenReturn(5L); // 少于每日限制

        // When
        boolean result = smsVerificationService.canSendVerificationCode(validPhone, codeType);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void should_ReturnFalse_when_CannotSendVerificationCode() {
        // Given
        SmsVerification recentCode = new SmsVerification();
        recentCode.setCreatedAt(LocalDateTime.now().minusSeconds(30)); // 30秒前发送的
        when(smsVerificationRepository.findLatestByPhoneAndType(eq(validPhone), eq(codeType), any(LocalDateTime.class)))
                .thenReturn(Optional.of(recentCode));

        // When
        boolean result = smsVerificationService.canSendVerificationCode(validPhone, codeType);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void should_ReturnNextSendTime_when_RecentCodeExists() {
        // Given
        SmsVerification recentCode = new SmsVerification();
        recentCode.setCreatedAt(LocalDateTime.now().minusSeconds(30)); // 30秒前发送的
        when(smsVerificationRepository.findLatestByPhoneAndType(eq(validPhone), eq(codeType), any(LocalDateTime.class)))
                .thenReturn(Optional.of(recentCode));

        // When
        long nextSendTime = smsVerificationService.getNextSendTime(validPhone, codeType);

        // Then
        assertThat(nextSendTime).isGreaterThan(0L);
    }

    @Test
    void should_ReturnZero_when_NoRecentCode() {
        // Given
        when(smsVerificationRepository.findLatestByPhoneAndType(eq(validPhone), eq(codeType), any(LocalDateTime.class)))
                .thenReturn(Optional.empty());

        // When
        long nextSendTime = smsVerificationService.getNextSendTime(validPhone, codeType);

        // Then
        assertThat(nextSendTime).isEqualTo(0L);
    }

    @Test
    void should_ReturnTrue_when_MarkCodeAsUsed() {
        // Given
        when(smsVerificationRepository.markCodesAsUsed(validPhone, codeType))
                .thenReturn(1);

        // When
        boolean result = smsVerificationService.markCodeAsUsed(validPhone, validCode, codeType);

        // Then
        assertThat(result).isTrue();
        verify(smsVerificationRepository, times(1)).markCodesAsUsed(validPhone, codeType);
    }

    @Test
    void should_ReturnFalse_when_MarkCodeAsUsedFails() {
        // Given
        when(smsVerificationRepository.markCodesAsUsed(validPhone, codeType))
                .thenReturn(0);

        // When
        boolean result = smsVerificationService.markCodeAsUsed(validPhone, validCode, codeType);

        // Then
        assertThat(result).isFalse();
        verify(smsVerificationRepository, times(1)).markCodesAsUsed(validPhone, codeType);
    }
}