package com.wanli.service.impl;

import com.wanli.service.TokenBlacklistService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Token黑名单服务实现类
 * 使用内存存储已登出的JWT Token
 * 
 * @author wanli
 * @version 1.0.0
 */
@Slf4j
@Service
public class TokenBlacklistServiceImpl implements TokenBlacklistService {
    
    /**
     * 使用ConcurrentHashMap存储黑名单Token
     * Key: Token字符串
     * Value: 过期时间戳
     */
    private final ConcurrentMap<String, Long> blacklistedTokens = new ConcurrentHashMap<>();
    
    /**
     * 定时清理过期Token的调度器
     */
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    
    /**
     * Token默认过期时间（24小时，单位：毫秒）
     */
    private static final long DEFAULT_EXPIRATION_TIME = 24 * 60 * 60 * 1000L;
    
    public TokenBlacklistServiceImpl() {
        // 启动定时清理任务，每小时执行一次
        scheduler.scheduleAtFixedRate(this::cleanupExpiredTokens, 1, 1, TimeUnit.HOURS);
        log.info("TokenBlacklistService initialized with automatic cleanup");
    }
    
    @Override
    public void blacklistToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            log.warn("Attempted to add null or empty token to blacklist");
            return;
        }
        
        long expirationTime = System.currentTimeMillis() + DEFAULT_EXPIRATION_TIME;
        blacklistedTokens.put(token, expirationTime);
        log.info("Token added to blacklist, total blacklisted tokens: {}", blacklistedTokens.size());
    }
    
    /**
     * 将Token加入黑名单（带自定义过期时间）
     * @param token JWT Token
     * @param expirationTimeMillis 过期时间戳（毫秒）
     */
    public void blacklistToken(String token, long expirationTimeMillis) {
        if (token == null || token.trim().isEmpty()) {
            log.warn("Attempted to add null or empty token to blacklist");
            return;
        }
        
        if (expirationTimeMillis <= System.currentTimeMillis()) {
            log.warn("Attempted to add token with past expiration time to blacklist");
            return;
        }
        
        blacklistedTokens.put(token, expirationTimeMillis);
        log.info("Token added to blacklist with custom expiration, total blacklisted tokens: {}", blacklistedTokens.size());
    }
    
    @Override
    public boolean isTokenBlacklisted(String token) {
        if (token == null || token.trim().isEmpty()) {
            return false;
        }
        
        Long expirationTime = blacklistedTokens.get(token);
        if (expirationTime == null) {
            return false;
        }
        
        // 检查Token是否已过期
        if (expirationTime <= System.currentTimeMillis()) {
            // Token已过期，从黑名单中移除
            blacklistedTokens.remove(token);
            log.debug("Expired token removed from blacklist during check");
            return false;
        }
        
        return true;
    }
    
    /**
     * 从黑名单中移除Token
     * @param token JWT Token
     */
    public void removeFromBlacklist(String token) {
        if (token == null || token.trim().isEmpty()) {
            log.warn("Attempted to remove null or empty token from blacklist");
            return;
        }
        
        Long removed = blacklistedTokens.remove(token);
        if (removed != null) {
            log.info("Token removed from blacklist, remaining tokens: {}", blacklistedTokens.size());
        } else {
            log.debug("Attempted to remove non-existent token from blacklist");
        }
    }
    
    /**
     * 清理过期的Token
     */
    public void cleanupExpiredTokens() {
        long currentTime = System.currentTimeMillis();
        int initialSize = blacklistedTokens.size();
        
        // 移除所有过期的Token
        blacklistedTokens.entrySet().removeIf(entry -> {
            boolean expired = entry.getValue() <= currentTime;
            if (expired) {
                log.debug("Removing expired token from blacklist");
            }
            return expired;
        });
        
        int finalSize = blacklistedTokens.size();
        int removedCount = initialSize - finalSize;
        
        if (removedCount > 0) {
            log.info("Cleanup completed: removed {} expired tokens, remaining: {}", removedCount, finalSize);
        } else {
            log.debug("Cleanup completed: no expired tokens found, total: {}", finalSize);
        }
    }
    
    @Override
    public int getBlacklistSize() {
        return blacklistedTokens.size();
    }
    
    @Override
    public void clearBlacklist() {
        int size = blacklistedTokens.size();
        blacklistedTokens.clear();
        log.info("Blacklist cleared: removed {} tokens", size);
    }
}