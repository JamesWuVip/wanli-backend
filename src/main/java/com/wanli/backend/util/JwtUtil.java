package com.wanli.backend.util;

import java.util.Date;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

/** JWT工具类 用于生成、解析和验证JWT令牌 */
@Component
public class JwtUtil {

  @Value("${jwt.secret:wanli-backend-secret-key-for-jwt-token-generation-and-validation}")
  private String jwtSecret;

  @Value("${jwt.expiration:86400000}") // 默认24小时
  private long jwtExpiration;

  /**
   * 生成JWT令牌
   *
   * @param userId 用户ID
   * @param username 用户名
   * @param role 用户角色
   * @return JWT令牌
   */
  public String generateToken(UUID userId, String username, String role) {
    Date now = new Date();
    Date expiryDate = new Date(now.getTime() + jwtExpiration);

    SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());

    return Jwts.builder()
        .setSubject(userId.toString())
        .claim("username", username)
        .claim("role", role)
        .setIssuedAt(now)
        .setExpiration(expiryDate)
        .signWith(key, SignatureAlgorithm.HS256)
        .compact();
  }

  /**
   * 从JWT令牌中获取用户ID
   *
   * @param token JWT令牌
   * @return 用户ID
   */
  public UUID getUserIdFromToken(String token) {
    Claims claims = getClaimsFromToken(token);
    return UUID.fromString(claims.getSubject());
  }

  /**
   * 从JWT令牌中获取用户名
   *
   * @param token JWT令牌
   * @return 用户名
   */
  public String getUsernameFromToken(String token) {
    Claims claims = getClaimsFromToken(token);
    return claims.get("username", String.class);
  }

  /**
   * 从JWT令牌中获取用户角色
   *
   * @param token JWT令牌
   * @return 用户角色
   */
  public String getRoleFromToken(String token) {
    Claims claims = getClaimsFromToken(token);
    return claims.get("role", String.class);
  }

  /**
   * 从JWT令牌中获取过期时间
   *
   * @param token JWT令牌
   * @return 过期时间
   */
  public Date getExpirationDateFromToken(String token) {
    Claims claims = getClaimsFromToken(token);
    return claims.getExpiration();
  }

  /**
   * 验证JWT令牌是否有效
   *
   * @param token JWT令牌
   * @return 是否有效
   */
  public boolean validateToken(String token) {
    try {
      getClaimsFromToken(token);
      return true;
    } catch (JwtException | IllegalArgumentException e) {
      return false;
    }
  }

  /**
   * 检查JWT令牌是否过期
   *
   * @param token JWT令牌
   * @return 是否过期
   */
  public boolean isTokenExpired(String token) {
    try {
      Date expiration = getExpirationDateFromToken(token);
      return expiration.before(new Date());
    } catch (JwtException | IllegalArgumentException e) {
      return true;
    }
  }

  /**
   * 从JWT令牌中解析Claims
   *
   * @param token JWT令牌
   * @return Claims对象
   */
  private Claims getClaimsFromToken(String token) {
    SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());

    return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
  }
}
