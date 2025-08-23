package com.wanli.backend.exception;

import org.springframework.http.HttpStatus;

/** 业务异常基类 用于封装业务逻辑中的异常情况，提供统一的异常处理机制 */
public class BusinessException extends RuntimeException {

  private final String errorCode;
  private final HttpStatus httpStatus;

  /**
   * 构造函数
   *
   * @param message 异常消息
   */
  public BusinessException(String message) {
    this(message, "BUSINESS_ERROR", HttpStatus.BAD_REQUEST);
  }

  /**
   * 构造函数
   *
   * @param message 异常消息
   * @param errorCode 错误码
   */
  public BusinessException(String message, String errorCode) {
    this(message, errorCode, HttpStatus.BAD_REQUEST);
  }

  /**
   * 构造函数
   *
   * @param message 异常消息
   * @param httpStatus HTTP状态码
   */
  public BusinessException(String message, HttpStatus httpStatus) {
    this(message, "BUSINESS_ERROR", httpStatus);
  }

  /**
   * 构造函数
   *
   * @param message 异常消息
   * @param errorCode 错误码
   * @param httpStatus HTTP状态码
   */
  public BusinessException(String message, String errorCode, HttpStatus httpStatus) {
    super(message);
    this.errorCode = errorCode;
    this.httpStatus = httpStatus;
  }

  /**
   * 构造函数
   *
   * @param message 异常消息
   * @param cause 原因异常
   */
  public BusinessException(String message, Throwable cause) {
    this(message, "BUSINESS_ERROR", HttpStatus.BAD_REQUEST, cause);
  }

  /**
   * 构造函数
   *
   * @param message 异常消息
   * @param errorCode 错误码
   * @param httpStatus HTTP状态码
   * @param cause 原因异常
   */
  public BusinessException(
      String message, String errorCode, HttpStatus httpStatus, Throwable cause) {
    super(message, cause);
    this.errorCode = errorCode;
    this.httpStatus = httpStatus;
  }

  /**
   * 获取错误码
   *
   * @return 错误码
   */
  public String getErrorCode() {
    return errorCode;
  }

  /**
   * 获取HTTP状态码
   *
   * @return HTTP状态码
   */
  public HttpStatus getHttpStatus() {
    return httpStatus;
  }

  /**
   * 创建权限不足异常
   *
   * @param message 异常消息
   * @return BusinessException实例
   */
  public static BusinessException forbidden(String message) {
    return new BusinessException(message, "PERMISSION_DENIED", HttpStatus.FORBIDDEN);
  }

  /**
   * 创建资源未找到异常
   *
   * @param message 异常消息
   * @return BusinessException实例
   */
  public static BusinessException notFound(String message) {
    return new BusinessException(message, "RESOURCE_NOT_FOUND", HttpStatus.NOT_FOUND);
  }

  /**
   * 创建未授权异常
   *
   * @param message 异常消息
   * @return BusinessException实例
   */
  public static BusinessException unauthorized(String message) {
    return new BusinessException(message, "UNAUTHORIZED", HttpStatus.UNAUTHORIZED);
  }

  /**
   * 创建参数错误异常
   *
   * @param message 异常消息
   * @return BusinessException实例
   */
  public static BusinessException badRequest(String message) {
    return new BusinessException(message, "BAD_REQUEST", HttpStatus.BAD_REQUEST);
  }

  /**
   * 创建内部服务器错误异常
   *
   * @param message 异常消息
   * @return BusinessException实例
   */
  public static BusinessException internalServerError(String message) {
    return new BusinessException(
        message, "INTERNAL_SERVER_ERROR", HttpStatus.INTERNAL_SERVER_ERROR);
  }

  /**
   * 创建冲突异常
   *
   * @param message 异常消息
   * @return BusinessException实例
   */
  public static BusinessException conflict(String message) {
    return new BusinessException(message, "CONFLICT", HttpStatus.CONFLICT);
  }
}
