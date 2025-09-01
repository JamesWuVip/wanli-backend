package com.wanli.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * JWT Token 提供者.
 * 负责JWT token的生成、验证和解析.
 *
 * @author JamesWu
 * @since 1.0.0
 */
@Component
public class JwtTokenProvider {

    /** JWT密钥. */
    @Value("${jwt.secret:default-secret-key}")
    private String jwtSecret;

    /** JWT过期时间（毫秒）. */
    @Value("${jwt.expiration:3600000}") // 默认1小时
    private long jwtExpirationInMs;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    /**
     * 生成JWT token.
     *
     * @param authentication 认证信息.
     * @return JWT token.
     */
    public String generateToken(final Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Date expiryDate = new Date(System.currentTimeMillis()
                + jwtExpirationInMs);

        return Jwts.builder()
                .setSubject(userPrincipal.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 从token中获取用户名.
     *
     * @param token JWT token.
     * @return 用户名.
     */
    public String getUsernameFromToken(final String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }

    /**
     * 验证token是否有效.
     *
     * @param token JWT token.
     * @return 验证结果.
     */
    public boolean validateToken(final String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (final JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * 获取token过期时间.
     *
     * @return 过期时间（毫秒）.
     */
    public long getExpirationTime() {
        return jwtExpirationInMs;
    }

    /**
     * 从token中获取过期时间.
     *
     * @param token JWT token.
     * @return 过期时间.
     */
    public Date getExpirationDateFromToken(final String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getExpiration();
    }

    /**
     * 检查token是否过期.
     *
     * @param token JWT token.
     * @return 是否过期.
     */
    public boolean isTokenExpired(final String token) {
        Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }
}