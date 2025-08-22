package com.wanli.backend.config;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.wanli.backend.entity.User;
import com.wanli.backend.repository.UserRepository;
import com.wanli.backend.util.JwtUtil;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/** JWT认证过滤器 用于验证请求中的JWT令牌并设置安全上下文 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  @Autowired private JwtUtil jwtUtil;

  @Autowired private UserRepository userRepository;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    String authHeader = request.getHeader("Authorization");
    String token = null;
    String username = null;

    // 从Authorization头中提取JWT令牌
    if (authHeader != null && authHeader.startsWith("Bearer ")) {
      token = authHeader.substring(7);
      try {
        username = jwtUtil.getUsernameFromToken(token);
      } catch (Exception e) {
        logger.error("无法从JWT令牌中获取用户名: " + e.getMessage());
      }
    }

    // 如果令牌有效且当前没有认证信息，则设置认证
    if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

      // 验证令牌
      if (jwtUtil.validateToken(token)) {
        try {
          // 从令牌中获取用户信息
          UUID userId = jwtUtil.getUserIdFromToken(token);
          String role = jwtUtil.getRoleFromToken(token);

          // 验证用户是否存在且未被删除
          Optional<User> userOptional = userRepository.findByIdAndDeletedAtIsNull(userId);
          if (userOptional.isPresent()) {
            User user = userOptional.get();

            // 创建认证令牌
            UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(
                    user.getUsername(),
                    null,
                    Collections.singletonList(
                        new SimpleGrantedAuthority("ROLE_" + role.toUpperCase())));

            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            // 设置安全上下文
            SecurityContextHolder.getContext().setAuthentication(authToken);

            // 将用户信息添加到请求属性中，供控制器使用
            request.setAttribute("currentUserId", userId);
            request.setAttribute("currentUsername", user.getUsername());
            request.setAttribute("currentUserRole", role);
          }
        } catch (Exception e) {
          logger.error("JWT令牌验证失败: " + e.getMessage());
        }
      }
    }

    filterChain.doFilter(request, response);
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
    String path = request.getRequestURI();
    // 对于公开的端点，跳过JWT验证
    return path.equals("/api/health")
        || path.equals("/api/welcome")
        || path.equals("/api/auth/register")
        || path.equals("/api/auth/login");
  }
}
