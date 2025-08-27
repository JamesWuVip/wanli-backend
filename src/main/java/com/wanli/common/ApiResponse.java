package com.wanli.common;

/**
 * 统一API响应格式
 * 用于封装所有API接口的响应数据
 * 
 * @author wanli
 * @version 1.0.0
 * @param <T> 响应数据类型
 */
public class ApiResponse<T> {
    
    private boolean success;
    private String code;
    private String message;
    private T data;
    private Long timestamp;
    
    private ApiResponse(boolean success, String code, String message, T data) {
        this.success = success;
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * 成功响应
     * @param data 响应数据
     * @param <T> 数据类型
     * @return ApiResponse
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, ErrorCode.SUCCESS.getCode(), "操作成功", data);
    }
    
    /**
     * 成功响应（仅消息）
     * @param message 响应消息
     * @return ApiResponse
     */
    public static ApiResponse<Void> success(String message) {
        return new ApiResponse<>(true, ErrorCode.SUCCESS.getCode(), message, null);
    }
    
    /**
     * 成功响应（自定义消息）
     * @param message 响应消息
     * @param data 响应数据
     * @param <T> 数据类型
     * @return ApiResponse
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, ErrorCode.SUCCESS.getCode(), message, data);
    }
    
    /**
     * 失败响应
     * @param code 错误码
     * @param message 错误消息
     * @param <T> 数据类型
     * @return ApiResponse
     */
    public static <T> ApiResponse<T> error(String code, String message) {
        return new ApiResponse<>(false, code, message, null);
    }
    
    /**
     * 失败响应（带数据）
     * @param code 错误码
     * @param message 错误消息
     * @param data 响应数据
     * @param <T> 数据类型
     * @return ApiResponse
     */
    public static <T> ApiResponse<T> error(String code, String message, T data) {
        return new ApiResponse<>(false, code, message, data);
    }
    
    /**
     * 失败响应（使用错误码枚举）
     * @param errorCode 错误码枚举
     * @param <T> 数据类型
     * @return ApiResponse
     */
    public static <T> ApiResponse<T> error(ErrorCode errorCode) {
        return new ApiResponse<>(false, errorCode.getCode(), errorCode.getMessage(), null);
    }
    
    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getCode() {
        return code;
    }
    
    public void setCode(String code) {
        this.code = code;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public T getData() {
        return data;
    }
    
    public void setData(T data) {
        this.data = data;
    }
    
    public Long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}