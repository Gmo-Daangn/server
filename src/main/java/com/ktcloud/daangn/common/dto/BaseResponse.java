package com.ktcloud.daangn.common.dto;

import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

public record BaseResponse<T>(
        int code, //http status code
        LocalDateTime localDateTime,
        String message,
        T data
) {
    public static <T> BaseResponse<T> success(T body) {
        return new BaseResponse<>(HttpStatus.OK.value(), LocalDateTime.now(), "정상", body);
    }

    public static <T> BaseResponse<T> fail(int errorCode, String message, T body){
        return new BaseResponse<>(errorCode, LocalDateTime.now(), message, body);
    }
}