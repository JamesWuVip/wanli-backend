package com.wanli.service;

/**
 * Token黑名单服务接口
 * 用于管理已登出的JWT Token
 * 
 * @author wanli
 * @version 1.0.0
 */
public interface TokenBlacklistService {
    
    /**
     * 将Token加入黑名单
     * @param token JWT Token
     */
    void blacklistToken(String token);
    
    /**
     * 检查Token是否在黑名单中
     * @param token JWT Token
     * @return 是否在黑名单中
     */
    boolean isTokenBlacklisted(String token);
    
    /**
     * 清理黑名单（可用于定期清理过期Token）
     */
    void clearBlacklist();
    
    /**
     * 获取黑名单大小
     * @return 黑名单Token数量
     */
    int getBlacklistSize();
}