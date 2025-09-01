package com.wanli.exception;

import com.wanli.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.http.converter.HttpMessageNotReadableException;

import java.util.HashMap;
import java.util.Map;

/**
 * 全局异常处理器.
 * 统一处理应用程序中的异常.
     *
 * @author JamesWu.
 * @since 1.0.0.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 参数验证失败错误码.
     */
    private static final String VALIDATION_ERROR_CODE = "2001";

    /**
     * 请求格式错误码.
     */
    private static final String REQUEST_FORMAT_ERROR_CODE = "2002";

    /**
     * 不支持的媒体类型错误码.
     */
    private static final String UNSUPPORTED_MEDIA_TYPE_ERROR_CODE = "2003";

    /**
     * 系统内部错误码.
     */
    private static final String INTERNAL_ERROR_CODE = "1001";

    /**
     * 处理参数验证异常.
     *
     * @param ex 参数验证异常.
     * @return 错误响应.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>>
            handleValidationExceptions(
                    final MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ApiResponse<Map<String, String>> response =
                ApiResponse.error("参数验证失败", VALIDATION_ERROR_CODE);
        response.setData(errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    /**
     * 处理JSON格式错误.
     *
     * @param ex JSON解析异常.
     * @return 错误响应.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Object>> handleHttpMessageNotReadable(
            final HttpMessageNotReadableException ex) {
        ApiResponse<Object> response = ApiResponse.error("请求格式错误",
                REQUEST_FORMAT_ERROR_CODE);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    /**
     * 处理不支持的媒体类型.
     *
     * @param ex 媒体类型不支持异常.
     * @return 错误响应.
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiResponse<Object>> handleHttpMediaTypeNotSupported(
            final HttpMediaTypeNotSupportedException ex) {
        ApiResponse<Object> response = ApiResponse.error("不支持的媒体类型",
                UNSUPPORTED_MEDIA_TYPE_ERROR_CODE);
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                .body(response);
    }

    /**
     * 处理通用异常.
     *
     * @param ex 通用异常.
     * @return 错误响应.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGenericException(
            final Exception ex) {
        ApiResponse<Object> response = ApiResponse.error("系统内部错误",
                INTERNAL_ERROR_CODE);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }
}