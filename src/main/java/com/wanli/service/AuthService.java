package com.wanli.service;

import com.wanli.dto.LoginRequestDto;
import com.wanli.dto.LoginResponseDto;
import com.wanli.dto.RegisterRequestDto;
import com.wanli.dto.UserResponseDto;
import com.wanli.entity.User;
import com.wanli.exception.BadRequestException;
import com.wanli.exception.ResourceNotFoundException;
import com.wanli.repository.UserRepository;
import com.wanli.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
public class AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    public LoginResponseDto login(LoginRequestDto loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = jwtUtil.generateToken(authentication.getName());

        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));

        LoginResponseDto.UserInfoDto userInfo = new LoginResponseDto.UserInfoDto();
        userInfo.setId(user.getId());
        userInfo.setUsername(user.getUsername());
        userInfo.setEmail(user.getEmail());
        userInfo.setRole(user.getRole());

        LoginResponseDto response = new LoginResponseDto();
        response.setToken(token);
        response.setUser(userInfo);
        response.setExpiresIn(jwtUtil.getExpirationTime());

        return response;
    }

    public UserResponseDto register(RegisterRequestDto registerRequest) {
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new BadRequestException("用户名已存在");
        }

        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new BadRequestException("邮箱已存在");
        }

        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setRole("USER");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);

        UserResponseDto response = new UserResponseDto();
        response.setId(savedUser.getId());
        response.setUsername(savedUser.getUsername());
        response.setEmail(savedUser.getEmail());
        response.setRole(savedUser.getRole());
        response.setCreatedAt(savedUser.getCreatedAt());
        response.setUpdatedAt(savedUser.getUpdatedAt());

        return response;
    }

    public void logout() {
        SecurityContextHolder.clearContext();
    }

    public LoginResponseDto refreshToken(String token) {
        String bearerToken = token.replace("Bearer ", "");
        String username = jwtUtil.getUsernameFromToken(bearerToken);
        
        if (jwtUtil.isTokenValid(bearerToken, username)) {
            String newToken = jwtUtil.generateToken(username);
            
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));

            LoginResponseDto.UserInfoDto userInfo = new LoginResponseDto.UserInfoDto();
            userInfo.setId(user.getId());
            userInfo.setUsername(user.getUsername());
            userInfo.setEmail(user.getEmail());
            userInfo.setRole(user.getRole());

            LoginResponseDto response = new LoginResponseDto();
            response.setToken(newToken);
            response.setUser(userInfo);
            response.setExpiresIn(jwtUtil.getExpirationTime());

            return response;
        } else {
            throw new BadRequestException("Token无效");
        }
    }
}