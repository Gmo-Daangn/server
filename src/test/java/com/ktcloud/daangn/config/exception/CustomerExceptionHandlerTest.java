package com.ktcloud.daangn.config.exception;

import com.ktcloud.daangn.auth.controller.AuthController;
import com.ktcloud.daangn.auth.service.AuthService;
import com.ktcloud.daangn.config.ResultCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * CustomerExceptionHandler 동작 검증 테스트.
 * AuthController를 슬라이스로 활용하여 핸들러가 HTTP 응답에 올바르게 반영되는지 확인한다.
 */
@WebMvcTest(AuthController.class)
class CustomerExceptionHandlerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    AuthService authService;

    // ── MethodArgumentNotValidException ──────────────────────────────────────

    @Nested
    @DisplayName("MethodArgumentNotValidException - 필드 유효성 실패")
    class ValidationFailedHandler {

        @Test
        @DisplayName("단일 필드 오류 시 Map<field, message>가 Data에 포함된다")
        void singleFieldError_mapWithOneEntry() throws Exception {
            String body = """
                    {
                      "email": "",
                      "nickname": "닉네임",
                      "password": "password",
                      "address": { "city": "서울시", "district": "동작구", "town": "사당동" }
                    }
                    """;

            mockMvc.perform(post("/api/v1/auth")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(ResultCode.VALIDATION_FAILED.getStatusCode()))
                    .andExpect(jsonPath("$.message").value(ResultCode.VALIDATION_FAILED.getMessage()))
                    .andExpect(jsonPath("$.Data").isMap())
                    .andExpect(jsonPath("$.Data.email").exists());
        }

        @Test
        @DisplayName("다중 필드 오류 시 모든 필드가 Data Map에 포함된다")
        void multipleFieldErrors_mapWithAllEntries() throws Exception {
            String body = """
                    {
                      "email": "not-valid",
                      "nickname": "",
                      "password": "",
                      "address": { "city": "서울시", "district": "동작구", "town": "사당동" }
                    }
                    """;

            mockMvc.perform(post("/api/v1/auth")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.Data").isMap())
                    .andExpect(jsonPath("$.Data", aMapWithSize(greaterThanOrEqualTo(2))))
                    .andExpect(jsonPath("$.Data.nickname").exists())
                    .andExpect(jsonPath("$.Data.password").exists());
        }

        @Test
        @DisplayName("유효성 실패 응답의 message는 VALIDATION_FAILED 코드의 메시지이다")
        void validationFailed_messageIsFromResultCode() throws Exception {
            String body = """
                    {
                      "email": "",
                      "nickname": "닉네임",
                      "password": "password",
                      "address": { "city": "서울시", "district": "동작구", "town": "사당동" }
                    }
                    """;

            mockMvc.perform(post("/api/v1/auth")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(jsonPath("$.message").value("입력 값의 유효성 검사에 실패했습니다."));
        }
    }

    // ── InvalidInputException ─────────────────────────────────────────────────

    @Nested
    @DisplayName("InvalidInputException - 커스텀 예외 처리")
    class InvalidInputExceptionHandler {

        @Test
        @DisplayName("InvalidInputException이 발생하면 400과 예외 메시지를 반환한다")
        void invalidInput_returns400WithMessage() throws Exception {
            given(authService.signup(any()))
                    .willThrow(new InvalidInputException(400, "중복된 이메일입니다."));

            String body = """
                    {
                      "email": "test@test.com",
                      "nickname": "닉네임",
                      "password": "password",
                      "address": { "city": "서울시", "district": "동작구", "town": "사당동" }
                    }
                    """;

            mockMvc.perform(post("/api/v1/auth")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("중복된 이메일입니다."))
                    .andExpect(jsonPath("$.Data").doesNotExist());
        }

        @Test
        @DisplayName("InvalidInputException의 statusCode가 응답 code 필드에 포함된다")
        void invalidInput_statusCodeInResponseBody() throws Exception {
            given(authService.login(any()))
                    .willThrow(new InvalidInputException(400, "이메일 혹은 비밀번호 오류입니다."));

            String body = """
                    {
                      "email": "test@test.com",
                      "password": "pw"
                    }
                    """;

            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(400))
                    .andExpect(jsonPath("$.message").value("이메일 혹은 비밀번호 오류입니다."));
        }
    }

    // ── IllegalArgumentException ──────────────────────────────────────────────

    @Nested
    @DisplayName("IllegalArgumentException - 잘못된 인수 예외 처리")
    class IllegalArgumentExceptionHandler {

        @Test
        @DisplayName("IllegalArgumentException이 발생하면 400과 예외 메시지를 반환한다")
        void illegalArgument_returns400WithMessage() throws Exception {
            given(authService.signup(any()))
                    .willThrow(new IllegalArgumentException("잘못된 입력값입니다."));

            String body = """
                    {
                      "email": "test@test.com",
                      "nickname": "닉네임",
                      "password": "password",
                      "address": { "city": "서울시", "district": "동작구", "town": "사당동" }
                    }
                    """;

            mockMvc.perform(post("/api/v1/auth")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(ResultCode.BAD_REQUEST.getStatusCode()))
                    .andExpect(jsonPath("$.message").value("잘못된 입력값입니다."));
        }
    }
}