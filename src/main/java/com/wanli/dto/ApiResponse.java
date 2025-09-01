package com.wanli.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 统一API响应格式.
     *
 * <p>提供标准化的API响应结构，包含成功状态、消息、数据和错误码等信息。</p>.
     *
 * @param <T> 响应数据类型.
 * @author JamesWu.
 * @since 1.0.0.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class ApiResponse<T> {

    /** 成功状态. */
    private boolean success;

    /** 响应消息. */
    private String message;

    /** 响应数据. */
    private T data;

    /** 错误码. */
    private String errorCode;

    /** 时间戳. */
    private long timestamp;

    /**
     * 私有构造函数.
     *
     * <p>初始化时间戳为当前系统时间。</p>.
     */
    private ApiResponse() {
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * 创建成功响应（无数据）.
     *
     * @param <T> 响应数据类型.
     * @return 成功响应对象.
     */
    public static <T> ApiResponse<T> success() {
        ApiResponse<T> response = new ApiResponse<>();
        response.success = true;
        response.message = "操作成功";
        return response;
    }

    /**
     * 创建成功响应（带数据）.
     *
     * @param <T> 响应数据类型.
     * @param data 响应数据.
     * @return 成功响应对象.
     */
    public static <T> ApiResponse<T> success(final T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.success = true;
        response.message = "操作成功";
        response.data = data;
        return response;
    }

    /**
     * 创建成功响应（自定义消息和数据）.
     *
     * @param <T> 响应数据类型.
     * @param message 响应消息.
     * @param data 响应数据.
     * @return 成功响应对象.
     */
    public static <T> ApiResponse<T> success(final String message,
                                            final T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.success = true;
        response.message = message;
        response.data = data;
        return response;
    }

    /**
     * 创建失败响应（仅消息）.
     *
     * @param <T> 响应数据类型.
     * @param message 错误消息.
     * @return 失败响应对象.
     */
    public static <T> ApiResponse<T> error(final String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.success = false;
        response.message = message;
        return response;
    }

    /**
     * 创建失败响应（消息和错误码）.
     *
     * @param <T> 响应数据类型.
     * @param message 错误消息.
     * @param errorCode 错误码.
     * @return 失败响应对象.
     */
    public static <T> ApiResponse<T> error(final String message,
                                          final String errorCode) {
        ApiResponse<T> response = new ApiResponse<>();
        response.success = false;
        response.message = message;
        response.errorCode = errorCode;
        return response;
    }

    /**
     * 获取成功状态.
     *
     * @return 成功状态.
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * 设置成功状态.
     *
     * @param successStatus 成功状态.
     */
    public void setSuccess(final boolean successStatus) {
        this.success = successStatus;
    }

    /**
     * 获取响应消息.
     *
     * @return 响应消息.
     */
    public String getMessage() {
        return message;
    }

    /**
     * 设置响应消息.
     *
     * @param responseMessage 响应消息.
     */
    public void setMessage(final String responseMessage) {
        this.message = responseMessage;
    }

    /**
     * 获取响应数据.
     *
     * @return 响应数据.
     */
    public T getData() {
        return data;
    }

    /**
     * 设置响应数据.
     *
     * @param responseData 响应数据.
     */
    public void setData(final T responseData) {
        this.data = responseData;
    }

    /**
     * 获取错误码.
     *
     * @return 错误码.
     */
    public String getErrorCode() {
        return errorCode;
    }

    /**
     * 设置错误码.
     *
     * @param code 错误码.
     */
    public void setErrorCode(final String code) {
        this.errorCode = code;
    }

    /**
     * 获取时间戳.
     *
     * @return 时间戳.
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * 设置时间戳.
     *
     * @param time 时间戳.
     */
    public void setTimestamp(final long time) {
        this.timestamp = time;
    }
}