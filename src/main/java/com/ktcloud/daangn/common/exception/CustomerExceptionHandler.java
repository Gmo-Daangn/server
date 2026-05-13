package com.ktcloud.daangn.common.exception;

import com.ktcloud.daangn.common.ResultCode;
import com.ktcloud.daangn.common.dto.BaseResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.net.BindException;
import java.security.SignatureException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestControllerAdvice
public class CustomerExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<BaseResponse<Map<String, String>>> methodArgumentNotValidException(MethodArgumentNotValidException ex) {
        Map<String, String> validationErrors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        error -> Optional.ofNullable(error.getDefaultMessage()).orElse("유효하지 않은 값입니다."),
                        (existing, _) -> existing
                ));
        return ResponseEntity
                .status(ResultCode.VALIDATION_FAILED.getStatusCode())
                .body(new BaseResponse<>(ResultCode.VALIDATION_FAILED.getStatusCode(), LocalDateTime.now(), ResultCode.VALIDATION_FAILED.getMessage(), validationErrors));
    }

    /// 매개변수 값이 올바르게 처리 되지 않았을때 에러처리
    @ExceptionHandler(IllegalArgumentException.class)
    protected ResponseEntity<BaseResponse<String>> illegalArgumentException(IllegalArgumentException ex) {
        return ResponseEntity
                .status(ResultCode.BAD_REQUEST.getStatusCode())
                .body(new BaseResponse<>(ResultCode.BAD_REQUEST.getStatusCode(), LocalDateTime.now(), ex.getMessage(), null));
    }
    //TODO NOT_FIND 변경
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    protected ResponseEntity<BaseResponse<String>> httpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException ex, HttpServletRequest  req) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new BaseResponse<>(ResultCode.BAD_REQUEST.getStatusCode(), LocalDateTime.now(), ex.getMessage(), null));
    }

    
    @ExceptionHandler(InvalidInputException.class)
    protected ResponseEntity<BaseResponse<String>> apiCustomException(InvalidInputException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new BaseResponse<>(ex.getStatusCode(), LocalDateTime.now(), ex.getMessage(), null));
    }

    //TODO 추후 정리 필요 (중복 로직)
    // 404 NOT_FOUND 에러 처리
    @ExceptionHandler(NoHandlerFoundException.class)
    protected ResponseEntity<BaseResponse<String>> handleNotFoundException(NoHandlerFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new BaseResponse<>(
                        ResultCode.NOT_FOUND.getStatusCode(),
                        LocalDateTime.now(),
                        ex.getMessage(),
                        null
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse<String>> handleException(Exception ex) {

        ResultCode resultCode = switch (ex) {
            case NoHandlerFoundException _ -> ResultCode.NOT_FOUND;
            case BindException _ -> ResultCode.INVALID_DATA;
            case HttpMessageNotReadableException _ -> ResultCode.INVALID_JSON;
            case SignatureException _ -> ResultCode.INVALID_ACCESS_TOKEN;
            case SecurityException _ /*MalformedJwtException t*/ -> ResultCode.INVALID_ACCESS_TOKEN;
            case RuntimeException _ -> ResultCode.RUN_TIME_ERROR;
            default -> ResultCode.INTERNAL_SERVER_ERROR;
        };

        BaseResponse<String> response = new BaseResponse<>(
                resultCode.getStatusCode(),
                LocalDateTime.now(),
                resultCode.getMessage(),
                null
        );

        return ResponseEntity
                .status(resultCode.getStatusCode())
                .body(response);
    }

}