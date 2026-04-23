package com.ktcloud.daangn.config.dto;

import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

public record BaseResponse<T>(
        int code, //http status code
        LocalDateTime localDateTime,
        String message,
        T Data
) {
    public static <T> BaseResponse<T> success(T body) {
        return new BaseResponse<>(HttpStatus.OK.value(), LocalDateTime.now(), "정상", body);
    }
}