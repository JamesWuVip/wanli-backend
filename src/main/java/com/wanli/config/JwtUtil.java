package com.wanli.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * JWT工具类
 * 负责JWT Token的生成、解析和验证
 * 
 * @author wanli
 * @version 1.0.0
 */
@Component
public class JwtUtil {
    
    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);
    
    @Value("${jwt.secret}")
    private String secretKey;
    
    @Value("${jwt.access-token-expiration}")
    private Long accessTokenExpiration;
    
    @Value("${jwt.refresh-token-expiration}")
    private Long refreshTokenExpiration;
    
    /**
     * 获取签名密钥
     * @return SecretKey
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = Base64.getDecoder().decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
    
    /**
     * 生成访问Token
     * @param userDetails 用户详情
     * @return JWT Token
     */
    public String generateAccessToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", userDetails.getUsername());
        claims.put("authorities", userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));
        return createToken(claims, userDetails.getUsername(), accessTokenExpiration);
    }
    
    /**
     * 生成Token（基于Authentication）
     * @param authentication 认证对象
     * @return JWT Token
     */
    public String generateToken(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return generateAccessToken(userDetails);
    }
    
    /**
     * 生成刷新Token
     * @param username 用户名
     * @return JWT Token
     */
    public String generateRefreshToken(String username) {
        return createToken(new HashMap<>(), username, refreshTokenExpiration);
    }
    
    /**
     * 创建Token
     * @param claims 声明
     * @param subject 主题（用户名）
     * @param expiration 过期时间（毫秒）
     * @return JWT Token
     */
    private String createToken(Map<String, Object> claims, String subject, Long expiration) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }
    
    /**
     * 从Token中提取用户名
     * @param token JWT Token
     * @return 用户名
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    
    /**
     * 从Token中提取过期时间
     * @param token JWT Token
     * @return 过期时间
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
    
    /**
     * 从Token中提取权限列表
     * @param token JWT Token
     * @return 权限列表
     */
    @SuppressWarnings("unchecked")
    public List<String> extractAuthorities(String token) {
        Claims claims = extractAllClaims(token);
        return (List<String>) claims.get("authorities");
    }
    
    /**
     * 从Token中提取指定声明
     * @param token JWT Token
     * @param claimsResolver 声明解析器
     * @param <T> 返回类型
     * @return 声明值
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    
    /**
     * 从Token中提取所有声明
     * @param token JWT Token
     * @return 所有声明
     */
    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            log.warn("JWT Token已过期: {}", e.getMessage());
            throw e;
        } catch (UnsupportedJwtException e) {
            log.error("不支持的JWT Token: {}", e.getMessage());
            throw e;
        } catch (MalformedJwtException e) {
            log.error("JWT Token格式错误: {}", e.getMessage());
            throw e;
        } catch (SecurityException e) {
            log.error("JWT Token签名验证失败: {}", e.getMessage());
            throw e;
        } catch (IllegalArgumentException e) {
            log.error("JWT Token参数错误: {}", e.getMessage());
            throw e;
        }
    }
    
    /**
     * 检查Token是否过期
     * @param token JWT Token
     * @return 是否过期
     */
    public Boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        }
    }
    
    /**
     * 验证Token有效性
     * @param token JWT Token
     * @param userDetails 用户详情
     * @return 是否有效
     */
    public Boolean validateToken(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
        } catch (Exception e) {
            log.warn("Token验证失败: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 验证Token格式和签名
     * @param token JWT Token
     * @return 是否有效
     * @throws IllegalArgumentException 当token为null时
     */
    public Boolean validateTokenFormat(String token) {
        if (token == null) {
            throw new IllegalArgumentException("Token不能为null");
        }
        try {
            extractAllClaims(token);
            return true;
        } catch (Exception e) {
            log.warn("Token格式验证失败: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 获取Token剩余有效时间（秒）
     * @param token JWT Token
     * @return 剩余有效时间
     */
    public Long getTokenRemainingTime(String token) {
        try {
            Date expiration = extractExpiration(token);
            long remainingTime = expiration.getTime() - System.currentTimeMillis();
            return Math.max(0, remainingTime / 1000);
        } catch (Exception e) {
            return 0L;
        }
    }
    
    /**
     * 获取Token过期时间
     * @param token JWT Token
     * @return 过期时间
     */
    public Date getExpirationTime(String token) {
        return extractExpiration(token);
    }
    
    /**
     * 获取访问Token过期时间（秒）
     * @return 过期时间
     */
    public Long getExpirationTime() {
        return accessTokenExpiration / 1000;
    }
}