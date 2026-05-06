package com.ktcloud.daangn.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

class ResultCodeTest {

    @Test
    @DisplayName("VALIDATION_FAILED는 400 상태 코드를 가진다")
    void validationFailed_hasStatusCode400() {
        assertThat(ResultCode.VALIDATION_FAILED.getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    @DisplayName("VALIDATION_FAILED 메시지는 '입력 값의 유효성 검사에 실패했습니다.'이다")
    void validationFailed_hasCorrectMessage() {
        assertThat(ResultCode.VALIDATION_FAILED.getMessage())
                .isEqualTo("입력 값의 유효성 검사에 실패했습니다.");
    }

    @Test
    @DisplayName("BAD_REQUEST는 400 상태 코드를 가진다")
    void badRequest_hasStatusCode400() {
        assertThat(ResultCode.BAD_REQUEST.getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    @DisplayName("SUCCESS는 200 상태 코드와 '성공' 메시지를 가진다")
    void success_hasStatusCode200AndSuccessMessage() {
        assertThat(ResultCode.SUCCESS.getStatusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(ResultCode.SUCCESS.getMessage()).isEqualTo("성공");
    }

    @Test
    @DisplayName("NOT_FOUND는 404 상태 코드를 가진다")
    void notFound_hasStatusCode404() {
        assertThat(ResultCode.NOT_FOUND.getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    @Test
    @DisplayName("INTERNAL_SERVER_ERROR는 500 상태 코드를 가진다")
    void internalServerError_hasStatusCode500() {
        assertThat(ResultCode.INTERNAL_SERVER_ERROR.getStatusCode())
                .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    @Test
    @DisplayName("VALIDATION_FAILED와 BAD_REQUEST는 별도의 열거형 상수이다")
    void validationFailed_isDistinctFromBadRequest() {
        assertThat(ResultCode.VALIDATION_FAILED).isNotEqualTo(ResultCode.BAD_REQUEST);
        assertThat(ResultCode.VALIDATION_FAILED.name()).isEqualTo("VALIDATION_FAILED");
    }

    @Test
    @DisplayName("모든 ResultCode 상수는 statusCode와 message를 가진다")
    void allResultCodes_haveStatusCodeAndMessage() {
        for (ResultCode code : ResultCode.values()) {
            assertThat(code.getStatusCode()).isPositive();
            assertThat(code.getMessage()).isNotBlank();
        }
    }
}