package com.wanli.util;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 响应工具类
 * 用于统一API响应格式
 * 
 * @author JamesWu
 * @since 1.0.0
 */
public class ResponseUtil {

    /**
     * 成功响应
     * 
     * @param data 响应数据
     * @return 响应Map
     */
    public static Map<String, Object> success(Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", 0);
        response.put("message", "操作成功");
        response.put("data", data);
        response.put("timestamp", LocalDateTime.now());
        return response;
    }

    /**
     * 成功响应（无数据）
     * 
     * @return 响应Map
     */
    public static Map<String, Object> success() {
        return success(null);
    }

    /**
     * 成功响应（自定义消息）
     * 
     * @param message 响应消息
     * @param data 响应数据
     * @return 响应Map
     */
    public static Map<String, Object> success(String message, Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", 0);
        response.put("message", message);
        response.put("data", data);
        response.put("timestamp", LocalDateTime.now());
        return response;
    }

    /**
     * 失败响应
     * 
     * @param code 错误码
     * @param message 错误消息
     * @return 响应Map
     */
    public static Map<String, Object> error(int code, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", code);
        response.put("message", message);
        response.put("data", null);
        response.put("timestamp", LocalDateTime.now());
        return response;
    }

    /**
     * 失败响应（带数据）
     * 
     * @param code 错误码
     * @param message 错误消息
     * @param data 错误数据
     * @return 响应Map
     */
    public static Map<String, Object> error(int code, String message, Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", code);
        response.put("message", message);
        response.put("data", data);
        response.put("timestamp", LocalDateTime.now());
        return response;
    }

    /**
     * 分页响应
     * 
     * @param data 数据列表
     * @param total 总数
     * @param page 当前页
     * @param size 每页大小
     * @return 响应Map
     */
    public static Map<String, Object> page(Object data, long total, int page, int size) {
        Map<String, Object> pageInfo = new HashMap<>();
        pageInfo.put("list", data);
        pageInfo.put("total", total);
        pageInfo.put("page", page);
        pageInfo.put("size", size);
        pageInfo.put("pages", (total + size - 1) / size);
        
        return success("查询成功", pageInfo);
    }

}