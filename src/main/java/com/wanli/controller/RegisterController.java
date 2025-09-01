// ========== AI代码生成执行规范 ==========
// 生成前检查: 
//   ✓ 验证包结构符合项目约定
//   ✓ 确认类名业务语义清晰
//   ✓ 检查依赖注入策略
//   ✓ 验证异常处理完备性
// 质量门禁:
//   ✓ 所有public方法必须有完整Javadoc
//   ✓ 所有参数使用final修饰符
//   ✓ 异常处理分层明确
//   ✓ 日志记录规范完整
// 验证命令: mvn clean compile && mvn checkstyle:check && mvn test
// ==========================================

package com.wanli.controller;

import com.wanli.dto.ApiResponse;
import com.wanli.dto.RegisterDTO;
import com.wanli.dto.RegisterResponseDTO;
import com.wanli.entity.User;
import com.wanli.service.SmsService;
import com.wanli.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
// import io.swagger.v3.oas.annotations.responses.ApiResponse; // 注释掉避免与自定义ApiResponse冲突
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.Objects;

/**
 * 用户注册控制器.
 * 
 * <p>核心职责：</p>
 * <ul>
 *   <li>处理用户注册请求和参数验证</li>
 *   <li>协调用户创建和手机号验证流程</li>
 *   <li>提供标准化的注册响应格式</li>
 * </ul>
 * 
 * <p>API端点：</p>
 * <ul>
 *   <li>POST /api/register - 用户注册</li>
 * </ul>
 * 
 * <p>业务流程：</p>
 * <ol>
 *   <li>接收并验证注册请求参数</li>
 *   <li>检查用户名和手机号唯一性</li>
 *   <li>创建新用户账户</li>
 *   <li>发送手机号验证短信</li>
 *   <li>返回注册成功响应</li>
 * </ol>
 * 
 * <p>设计原则：</p>
 * <ul>
 *   <li>单一职责：专注于用户注册业务流程</li>
 *   <li>依赖注入：通过构造函数注入所有服务依赖</li>
 *   <li>异常安全：完整的参数验证和业务异常处理</li>
 *   <li>日志完备：关键操作点的详细日志记录</li>
 * </ul>
 * 
 * @author AI Generated
 * @version 1.0
 * @since 2025-01-22
 * @see RegisterDTO
 * @see RegisterResponseDTO
 * @see UserService
 * @see SmsService
 */
@RestController
@RequestMapping("/api")
@Validated
@Tag(name = "用户注册", description = "用户注册相关API")
public class RegisterController {
    
    private static final Logger logger = LoggerFactory.getLogger(RegisterController.class);
    
    // 业务常量 - 使用有意义的业务名称
    private static final String REGISTRATION_SUCCESS_MESSAGE = "用户注册成功，请查收手机验证短信";
    private static final String REGISTRATION_FAILED_MESSAGE = "用户注册失败";
    private static final String SMS_SEND_SUCCESS_MESSAGE = "验证短信已发送至您的手机";
    
    // 依赖字段 - 全部final确保不可变性
    private final UserService userService;
    private final SmsService smsService;
    
    /**
     * 构造函数 - 依赖注入模式.
     * 
     * <p>通过构造函数注入所有必需依赖，确保对象创建时的完整性和一致性。
     * 所有依赖都进行非空验证，避免运行时空指针异常。</p>
     * 
     * @param userService 用户服务，负责用户账户的创建、查询和管理操作
     * @param smsService 短信服务，负责发送注册验证短信和其他短信通知
     * @throws IllegalArgumentException 当任何依赖参数为null时
     */
    public RegisterController(@NotNull final UserService userService,
                            @NotNull final SmsService smsService) {
        this.userService = Objects.requireNonNull(userService, 
            "userService is required and cannot be null");
        this.smsService = Objects.requireNonNull(smsService, 
            "smsService is required and cannot be null");
        
        logger.info("Successfully initialized {} with dependencies: [UserService: {}, SmsService: {}]", 
                   this.getClass().getSimpleName(), 
                   userService.getClass().getSimpleName(),
                   smsService.getClass().getSimpleName());
    }
    
    /**
     * 用户注册API端点.
     * 
     * <p>业务流程：</p>
     * <ol>
     *   <li>接收并验证注册请求参数（用户名、手机号、密码等）</li>
     *   <li>检查用户名和手机号的唯一性约束</li>
     *   <li>调用用户服务创建新用户账户</li>
     *   <li>发送手机号验证短信到用户手机</li>
     *   <li>构建并返回注册成功响应</li>
     * </ol>
     * 
     * <p>业务规则：</p>
     * <ul>
     *   <li>用户名必须在系统中唯一</li>
     *   <li>手机号必须在系统中唯一且格式有效</li>
     *   <li>密码必须符合安全策略要求</li>
     *   <li>确认密码必须与密码一致</li>
     *   <li>注册成功后自动发送手机号验证短信</li>
     * </ul>
     * 
     * <p>响应格式：</p>
     * <ul>
     *   <li>成功：HTTP 201 Created + 用户基本信息</li>
     *   <li>失败：HTTP 400 Bad Request + 错误详情</li>
     * </ul>
     * 
     * @param registerDTO 用户注册请求数据，包含用户名、手机号、密码、确认密码、全名等信息
     * @return ResponseEntity包装的ApiResponse，成功时包含RegisterResponseDTO数据
     * @throws IllegalArgumentException 当输入参数违反基础验证规则时
     * @throws RuntimeException 当用户名或手机号已存在时
     * @throws RuntimeException 当用户创建或短信发送失败时
     */
    @PostMapping("/register")
    @Operation(
        summary = "用户注册",
        description = "创建新用户账户并发送手机号验证短信。支持用户名、手机号、密码等基本信息注册。",
        tags = {"用户注册"}
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "注册成功",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = com.wanli.dto.ApiResponse.class)
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "请求参数无效或用户名/手机号已存在",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = com.wanli.dto.ApiResponse.class)
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500",
            description = "服务器内部错误",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = com.wanli.dto.ApiResponse.class)
            )
        )
    })
    public ResponseEntity<ApiResponse<RegisterResponseDTO>> register(
            @Parameter(
                description = "用户注册信息",
                required = true,
                schema = @Schema(implementation = RegisterDTO.class)
            )
            @Valid @RequestBody final RegisterDTO registerDTO) {
        
        logger.info("Starting user registration process for username: [{}], phone: [{}]", 
                   registerDTO.getUsername(), registerDTO.getPhone());
        
        try {
            // 第一层：参数基础验证（由@Valid注解自动处理）
            Objects.requireNonNull(registerDTO, "registerDTO cannot be null");
            
            // 第二层：业务规则验证
            validateRegistrationBusinessRules(registerDTO);
            
            // 第三层：创建用户账户
            logger.debug("Creating user account for: [{}]", registerDTO.getUsername());
            
            // 将RegisterDTO转换为User对象
            final User newUser = new User();
            newUser.setUsername(registerDTO.getUsername());
            newUser.setPhone(registerDTO.getPhone());
            newUser.setPasswordHash(registerDTO.getPassword()); // 将在UserService中加密
            newUser.setFullName(registerDTO.getFullName());
            
            final User createdUser = userService.createUser(newUser);
            
            // 第四层：发送手机号验证短信
            logger.debug("Sending SMS verification to: [{}]", registerDTO.getPhone());
            smsService.sendVerificationSms(createdUser.getPhone(), createdUser.getId());
            
            // 第五层：构建成功响应
            final RegisterResponseDTO responseDTO = buildRegisterResponse(createdUser);
            
            logger.info("User registration completed successfully for username: [{}], userId: [{}]", 
                       createdUser.getUsername(), createdUser.getId());
            
            return ResponseEntity.status(HttpStatus.CREATED)
                     .body(ApiResponse.success(REGISTRATION_SUCCESS_MESSAGE, responseDTO));
            
        } catch (final IllegalArgumentException e) {
            logger.warn("Registration failed due to invalid parameters for username: [{}], error: [{}]", 
                       registerDTO.getUsername(), e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("INVALID_PARAMETER", e.getMessage()));
            
        } catch (final RuntimeException e) {
            logger.error("Registration failed for username: [{}], error: [{}]", 
                        registerDTO.getUsername(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("REGISTRATION_FAILED", 
                           REGISTRATION_FAILED_MESSAGE + ": " + e.getMessage()));
            
        } catch (final Exception e) {
            logger.error("Unexpected error during registration for username: [{}]", 
                        registerDTO.getUsername(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("INTERNAL_ERROR", "系统内部错误，请稍后重试"));
        }
    }
    
    /**
     * 验证注册业务规则.
     * 
     * <p>执行注册相关的业务规则验证，包括：</p>
     * <ul>
     *   <li>密码与确认密码一致性检查</li>
     *   <li>用户名唯一性检查</li>
     *   <li>手机号唯一性检查</li>
     *   <li>手机号格式有效性检查</li>
     * </ul>
     * 
     * @param registerDTO 待验证的注册请求数据
     * @throws IllegalArgumentException 当业务规则验证失败时
     */
    private void validateRegistrationBusinessRules(@NotNull final RegisterDTO registerDTO) {
        logger.debug("Validating business rules for registration: [{}]", registerDTO.getUsername());
        
        // 验证密码一致性
        if (!registerDTO.getPassword().equals(registerDTO.getConfirmPassword())) {
            throw new IllegalArgumentException("密码与确认密码不一致");
        }
        
        // 验证用户名唯一性
        if (userService.existsByUsername(registerDTO.getUsername())) {
            throw new RuntimeException("用户名已存在: " + registerDTO.getUsername());
        }
        
        // 验证手机号唯一性
        if (userService.existsByPhone(registerDTO.getPhone())) {
            throw new RuntimeException("手机号已存在: " + registerDTO.getPhone());
        }
        
        // 验证手机号格式
        if (!smsService.isValidPhoneFormat(registerDTO.getPhone())) {
            throw new IllegalArgumentException("手机号格式无效: " + registerDTO.getPhone());
        }
        
        logger.debug("Business rules validation passed for: [{}]", registerDTO.getUsername());
    }
    
    /**
     * 构建注册成功响应数据.
     * 
     * <p>将创建的用户实体转换为注册响应DTO，包含：</p>
     * <ul>
     *   <li>用户基本信息（ID、用户名、手机号、全名）</li>
     *   <li>手机号验证状态</li>
     *   <li>注册时间戳</li>
     *   <li>手机号验证提示信息</li>
     * </ul>
     * 
     * @param user 已创建的用户实体
     * @return 注册响应DTO对象
     * @throws IllegalArgumentException 当用户实体为null时
     */
    private RegisterResponseDTO buildRegisterResponse(@NotNull final User user) {
        Objects.requireNonNull(user, "user cannot be null");
        
        logger.debug("Building registration response for user: [{}]", user.getUsername());
        
        final RegisterResponseDTO response = new RegisterResponseDTO(
            user.getId(),
            user.getUsername(),
            user.getPhone(),
            user.getFullName()
        );
        response.setPhoneVerified(user.getPhoneVerified());
        response.setRegisteredAt(user.getCreatedAt().toEpochSecond(java.time.ZoneOffset.UTC) * 1000);
        response.setVerificationMessage(SMS_SEND_SUCCESS_MESSAGE);
        
        return response;
    }
}