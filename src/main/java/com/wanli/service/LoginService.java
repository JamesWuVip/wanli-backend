package com.wanli.service;

import com.wanli.dto.LoginDTO;
import com.wanli.dto.LoginResponseDTO;
import com.wanli.entity.User;
import com.wanli.repository.UserRepository;
import com.wanli.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.
        UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 登录服务类.
     *
 * <p>处理用户登录相关的业务逻辑，包括用户认证、JWT令牌生成和用户状态检查。</p>.
 * <p>集成Spring Security进行安全认证。</p>.
     *
 * @author JamesWu.
 * @since 1.0.0.
 */
@Service
@Transactional
public class LoginService {

    /**
     * 毫秒转秒的转换常量.
     */
    private static final long MILLISECONDS_TO_SECONDS = 1000L;

    /**
     * Spring Security认证管理器.
     */
    @Autowired
    private AuthenticationManager authenticationManager;

    /**
     * 用户数据访问对象.
     */
    @Autowired
    private UserRepository userRepository;

    /**
     * JWT令牌提供者.
     */
    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    /**
     * 密码编码器.
     */
    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 用户服务.
     */
    @Autowired
    private UserService userService;

    /**
     * 用户登录.
     *
     * <p>验证用户凭据，生成JWT令牌，更新最后登录时间。</p>.
     *
     * @param loginDTO 登录请求数据.
     * @return 登录响应数据，包含JWT令牌和用户信息.
     * @throws BadCredentialsException 当用户名或密码错误，或用户账户被禁用时抛出.
     */
    public LoginResponseDTO login(final LoginDTO loginDTO) {
        try {
            // 验证手机号和密码
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginDTO.getPhone(),
                            loginDTO.getPassword()
                    )
            );

            // 查找用户
            Optional<User> userOpt = userRepository.findByPhone(
                    loginDTO.getPhone());
            if (userOpt.isEmpty()) {
                throw new BadCredentialsException("用户不存在");
            }

            User user = userOpt.get();

            // 检查用户状态
            if (!"ACTIVE".equals(user.getStatus().toString())) {
                throw new BadCredentialsException("用户账户已被禁用");
            }

            // 生成JWT token
            String token = jwtTokenProvider.generateToken(authentication);
            long expirationTime = jwtTokenProvider.getExpirationTime();
            LocalDateTime expirationDateTime = LocalDateTime.now()
                    .plusSeconds(expirationTime / MILLISECONDS_TO_SECONDS);

            // 更新最后登录时间
            userService.updateLastLoginTime(user.getId());

            // 构建用户信息DTO
            LoginResponseDTO.UserInfoDto userInfo =
                    new LoginResponseDTO.UserInfoDto(
                            user.getId(),
                            user.getUsername(),
                            user.getEmail(),
                            user.getFullName(),
                            user.getPhone(),
                            user.getStatus().toString(),
                            user.getCreatedAt()
                    );
            // 单独设置updatedAt字段
            userInfo.setUpdatedAt(user.getUpdatedAt());

            // 返回登录响应
            LoginResponseDTO response = new LoginResponseDTO(token, userInfo,
                    expirationTime);
            response.setExpirationTime(expirationDateTime);
            return response;

        } catch (AuthenticationException e) {
            throw new BadCredentialsException("手机号或密码错误");
        }
    }

    /**
     * 验证用户凭据.
     *
     * <p>检查手机号和密码是否匹配。</p>.
     *
     * @param phone 手机号.
     * @param password 明文密码.
     * @return 如果凭据有效返回true，否则返回false.
     */
    public boolean validateCredentials(final String phone,
            final String password) {
        Optional<User> userOpt = userRepository.findByPhone(phone);
        if (userOpt.isEmpty()) {
            return false;
        }

        User user = userOpt.get();
        return passwordEncoder.matches(password, user.getPasswordHash());
    }

    /**
     * 检查用户是否存在且状态为活跃.
     *
     * <p>验证用户是否存在并且状态为ACTIVE。</p>.
     *
     * @param phone 手机号.
     * @return 如果用户存在且状态为活跃返回true，否则返回false.
     */
    public boolean isUserActiveByPhone(final String phone) {
        Optional<User> userOpt = userRepository.findByPhone(phone);
        if (userOpt.isEmpty()) {
            return false;
        }

        User user = userOpt.get();
        return "ACTIVE".equals(user.getStatus().toString());
    }
}