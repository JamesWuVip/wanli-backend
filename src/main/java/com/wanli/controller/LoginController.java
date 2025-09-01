package com.wanli.controller;

import com.wanli.dto.ApiResponse;
import com.wanli.dto.LoginDTO;
import com.wanli.dto.LoginResponseDTO;
import com.wanli.service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

/**
 * 登录控制器.
     *
 * <p>提供用户登录相关的REST API接口，包括用户登录、状态检查和凭据验证。</p>.
     *
 * @author JamesWu.
 * @since 1.0.0.
 */
@RestController
@RequestMapping("/api/auth")
@Validated
@CrossOrigin(origins = "*", maxAge = 3600)
public class LoginController {

    /**
     * CORS最大缓存时间（秒）.
     */
    private static final int CORS_MAX_AGE_SECONDS = 3600;

    /**
     * 登录服务.
     */
    @Autowired
    private LoginService loginService;

    /**
     * 用户登录.
     *
     * @param loginDTO 登录请求数据.
     * @return 登录响应.
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponseDTO>> login(
            @Valid @RequestBody final LoginDTO loginDTO) {
        try {
            LoginResponseDTO responseDTO = loginService.login(loginDTO);
            ApiResponse<LoginResponseDTO> response = ApiResponse.success(
                responseDTO);
            return ResponseEntity.ok(response);
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("手机号或密码错误", "AUTH_001"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("登录服务异常", "SYS_001"));
        }
    }

    /**
     * 获取登录状态.
     *
     * @return 登录状态.
     */
    @GetMapping("/status")
    public ResponseEntity<ApiResponse<String>> getLoginStatus() {
        ApiResponse<String> response = ApiResponse.success("登录API服务正常运行");
        return ResponseEntity.ok(response);
    }

    /**
     * 验证用户凭据.
     *
     * @param loginDTO 登录凭据.
     * @return 验证结果.
     */
    @PostMapping("/validate")
    public ResponseEntity<ApiResponse<Boolean>> validateCredentials(
            @Valid @RequestBody final LoginDTO loginDTO) {
        try {
            boolean isValid = loginService.validateCredentials(
                loginDTO.getPhone(), loginDTO.getPassword());
            ApiResponse<Boolean> response = ApiResponse.success(isValid);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("验证服务异常", "SYS_002"));
        }
    }
}