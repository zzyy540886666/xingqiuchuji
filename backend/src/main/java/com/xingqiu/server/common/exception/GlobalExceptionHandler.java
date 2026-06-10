package com.xingqiu.server.common.exception;

import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.xingqiu.server.common.response.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BizException.class)
    public ResponseEntity<ApiResponse<Void>> handleBizException(BizException ex) {
        log.warn("BizException: code={}, message={}", ex.getErrorCode().getCode(), ex.getMessage());
        HttpStatus status = mapHttpStatus(ex.getErrorCode());
        return ResponseEntity.status(status)
                .body(ApiResponse.fail(ex.getErrorCode().getCode(), ex.getMessage(), ex.getDetails()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> details = new HashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            details.put(fe.getField(), fe.getDefaultMessage());
        }
        return ResponseEntity.badRequest()
                .body(ApiResponse.fail(ErrorCode.BAD_REQUEST.getCode(), "参数校验失败", details));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(ConstraintViolationException ex) {
        Map<String, String> details = new HashMap<>();
        ex.getConstraintViolations().forEach(violation ->
                details.put(violation.getPropertyPath().toString(), violation.getMessage()));
        return ResponseEntity.badRequest()
                .body(ApiResponse.fail(ErrorCode.BAD_REQUEST.getCode(), "参数校验失败", details));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnreadableMessage(HttpMessageNotReadableException ex) {
        Map<String, String> details = new HashMap<>();
        if (ex.getCause() instanceof UnrecognizedPropertyException unrecognized) {
            details.put(unrecognized.getPropertyName(), "不支持的请求字段");
            return ResponseEntity.badRequest()
                    .body(ApiResponse.fail(ErrorCode.BAD_REQUEST.getCode(), "请求包含未知字段", details));
        }
        return ResponseEntity.badRequest()
                .body(ApiResponse.fail(ErrorCode.BAD_REQUEST.getCode(), "请求体格式不合法"));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<Void>> handleMaxUploadSize(MaxUploadSizeExceededException ex) {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(ApiResponse.fail("FILE_TOO_LARGE", "上传文件大小不能超过 200MB"));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.fail(ErrorCode.FORBIDDEN.getCode(), ErrorCode.FORBIDDEN.getMessage()));
    }

    @ExceptionHandler({NoResourceFoundException.class, NoHandlerFoundException.class})
    public ResponseEntity<ApiResponse<Void>> handleNotFound(Exception ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.fail(ErrorCode.NOT_FOUND.getCode(), ErrorCode.NOT_FOUND.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnknown(Exception ex) {
        log.error("Unhandled exception", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.fail(ErrorCode.INTERNAL_ERROR.getCode(), ErrorCode.INTERNAL_ERROR.getMessage()));
    }

    private HttpStatus mapHttpStatus(ErrorCode code) {
        return switch (code) {
            case BAD_REQUEST, AUTH_INVALID_CODE, SKU_OFFLINE, WITHDRAW_AMOUNT_INVALID, POST_AUDIT_REJECTED,
                 DISTRIBUTION_SELF_BIND, WORK_ORDER_ACTION_DENIED -> HttpStatus.BAD_REQUEST;
            case UNAUTHORIZED -> HttpStatus.UNAUTHORIZED;
            case FORBIDDEN, AUTH_USER_DISABLED -> HttpStatus.FORBIDDEN;
            case NOT_FOUND, SKU_NOT_FOUND, ORDER_NOT_FOUND, CONTRACT_NOT_FOUND, MEMBERSHIP_NOT_FOUND,
                 WALLET_NOT_FOUND, ASSET_NOT_FOUND, POST_NOT_FOUND, COMMISSION_NOT_FOUND, CONFIG_NOT_FOUND,
                 DEVICE_NOT_FOUND, WORK_ORDER_NOT_FOUND, INSPECTION_TASK_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case CONFLICT, ORDER_PRICE_CHANGED, ORDER_NOT_PAYABLE, ORDER_STATUS_INVALID,
                 IDEMPOTENCY_CONFLICT, ASSET_SLOT_CONFLICT, PLANET_CARD_SOLD_OUT -> HttpStatus.CONFLICT;
            case PAY_SIGN_FAILED, PAY_CALLBACK_VERIFY_FAIL, CONTRACT_GENERATE_FAILED -> HttpStatus.BAD_GATEWAY;
            case RATE_LIMITED, POST_RATE_LIMITED -> HttpStatus.TOO_MANY_REQUESTS;
            case BALANCE_INSUFFICIENT -> HttpStatus.UNPROCESSABLE_ENTITY;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }
}
