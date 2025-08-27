package com.wanli.controller;

import com.wanli.common.ApiResponse;
import com.wanli.dto.LoginRequestDto;
import com.wanli.dto.LoginResponseDto;
import com.wanli.dto.RegisterRequestDto;
import com.wanli.dto.UserResponseDto;
import com.wanli.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponseDto>> login(@Valid @RequestBody LoginRequestDto loginRequest) {
        LoginResponseDto response = authService.login(loginRequest);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponseDto>> register(@Valid @RequestBody RegisterRequestDto registerRequest) {
        UserResponseDto response = authService.register(registerRequest);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout() {
        authService.logout();
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<LoginResponseDto>> refreshToken(@RequestHeader("Authorization") String token) {
        LoginResponseDto response = authService.refreshToken(token);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}