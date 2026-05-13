package com.ktcloud.daangn.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ResultCode {


    SUCCESS(HttpStatus.OK.value(), "성공"), // 200
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR.value(), "서버 에러가 발생했습니다"), // 500
    NOT_FOUND(HttpStatus.NOT_FOUND.value(), "요청하신 api를 찾을 수 없습니다."), // 404
    BAD_REQUEST(HttpStatus.BAD_REQUEST.value(), "항목이 올바르지 않습니다"),
    VALIDATION_FAILED(HttpStatus.BAD_REQUEST.value(), "입력 값의 유효성 검사에 실패했습니다."), // 400
    MAIL_ERROR(HttpStatus.BAD_REQUEST.value(), "발급 받은 인증 코드가 만료 되었거나 잘못 되었습니다."),
    INVALID_JSON(HttpStatus.INTERNAL_SERVER_ERROR.value(), "전달된 JSON 형식이 올바르지 않습니다"), // 400
    RUN_TIME_ERROR(HttpStatus.INTERNAL_SERVER_ERROR.value(), "런타임 에러"),
    //Token, Auth
    INVALID_DATA(HttpStatus.BAD_REQUEST.value(), "데이터 처리 오류 발생"), // 400
    TOKEN_EXPIRED(HttpStatus.FORBIDDEN.value(), "토큰이 만료 되었습니다"), // 403
    INVALID_ACCESS_TOKEN(HttpStatus.FORBIDDEN.value(), "토큰이 유효 하지 않습니다."), // 403
    REST_TYPE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR.value(), "REST API TYPE 오류"), // 500
    ;

    private final int statusCode;
    private final String message;
}