package com.ktcloud.daangn.config.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.time.temporal.ChronoUnit;

class BaseResponseTest {

    @Test
    @DisplayName("success() 팩토리 메서드는 200 코드와 '정상' 메시지로 응답을 생성한다")
    void success_setsCodeAndMessage() {
        BaseResponse<String> response = BaseResponse.success("결과");

        assertThat(response.code()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.message()).isEqualTo("정상");
    }

    @Test
    @DisplayName("success()는 전달된 body를 Data 필드에 담는다 (버그 수정 검증)")
    void success_includesBodyInData() {
        String payload = "회원가입 성공 ID : 42";

        BaseResponse<String> response = BaseResponse.success(payload);

        assertThat(response.Data()).isEqualTo(payload);
    }

    @Test
    @DisplayName("success()에 null을 전달하면 Data가 null이다")
    void success_nullBody_dataIsNull() {
        BaseResponse<String> response = BaseResponse.success(null);

        assertThat(response.Data()).isNull();
    }

    @Test
    @DisplayName("success()는 현재 시각을 localDateTime에 설정한다")
    void success_setsCurrentTimestamp() {
        LocalDateTime before = LocalDateTime.now();
        BaseResponse<String> response = BaseResponse.success("ok");
        LocalDateTime after = LocalDateTime.now();

        assertThat(response.localDateTime())
                .isAfterOrEqualTo(before)
                .isBeforeOrEqualTo(after);
    }

    @Test
    @DisplayName("success()는 제네릭 타입을 올바르게 처리한다")
    void success_genericType_integerBody() {
        BaseResponse<Integer> response = BaseResponse.success(99);

        assertThat(response.Data()).isEqualTo(99);
        assertThat(response.code()).isEqualTo(200);
    }

    @Test
    @DisplayName("생성자를 통해 직접 생성 시 전달된 값이 그대로 보존된다")
    void constructor_allFieldsPreserved() {
        LocalDateTime now = LocalDateTime.now();
        BaseResponse<String> response = new BaseResponse<>(404, now, "오류", "data");

        assertThat(response.code()).isEqualTo(404);
        assertThat(response.localDateTime()).isEqualTo(now);
        assertThat(response.message()).isEqualTo("오류");
        assertThat(response.Data()).isEqualTo("data");
    }
}