package com.wanli.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一API响应格式
 * 用于封装所有API接口的响应数据
 * 
 * @author wanli-team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    
    /**
     * 响应状态码
     */
    private Integer code;
    
    /**
     * 响应消息
     */
    private String message;
    
    /**
     * 响应数据
     */
    private T data;
    
    /**
     * 响应时间戳
     */
    private Long timestamp;
    
    /**
     * 成功响应
     * 
     * @param data 响应数据
     * @param <T> 数据类型
     * @return 成功响应对象
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
            .code(200)
            .message("操作成功")
            .data(data)
            .timestamp(System.currentTimeMillis())
            .build();
    }
    
    /**
     * 成功响应（无数据）
     * 
     * @return 成功响应对象
     */
    public static ApiResponse<Void> success() {
        return ApiResponse.<Void>builder()
            .code(200)
            .message("操作成功")
            .timestamp(System.currentTimeMillis())
            .build();
    }
    
    /**
     * 错误响应
     * 
     * @param code 错误码
     * @param message 错误消息
     * @param <T> 数据类型
     * @return 错误响应对象
     */
    public static <T> ApiResponse<T> error(Integer code, String message) {
        return ApiResponse.<T>builder()
            .code(code)
            .message(message)
            .timestamp(System.currentTimeMillis())
            .build();
    }
    
    /**
     * 错误响应（默认错误码）
     * 
     * @param message 错误消息
     * @param <T> 数据类型
     * @return 错误响应对象
     */
    public static <T> ApiResponse<T> error(String message) {
        return error(1000, message);
    }
}