package com.ktcloud.daangn.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
class AuthControllerValidTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    AuthService authService;

    @Nested
    @DisplayName("POST /api/v1/auth - 회원가입")
    class Signup {

        @Test
        @DisplayName("[HAPPY] 유효한 요청이면 200 및 서비스 반환값을 포함한다")
        void signup_validRequest_returns200() throws Exception {
            given(authService.signup(any())).willReturn("회원가입 성공 ID : 1");

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
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.Data").value("회원가입 성공 ID : 1"));
        }

        @Test
        @DisplayName("[Exception] 이메일이 비어 있으면 400 유효성 실패를 반환한다")
        void signup_blankEmail_returns400() throws Exception {
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
                    .andExpect(jsonPath("$.Data.email").exists());
        }

        @Test
        @DisplayName("[Exception] 이메일 형식이 올바르지 않으면 400 유효성 실패를 반환한다")
        void signup_invalidEmailFormat_returns400() throws Exception {
            String body = """
                    {
                      "email": "not-an-email",
                      "nickname": "닉네임",
                      "password": "password",
                      "address": { "city": "서울시", "district": "동작구", "town": "사당동" }
                    }
                    """;

            mockMvc.perform(post("/api/v1/auth")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.Data.email").exists());
        }

        @Test
        @DisplayName("[Exception] 닉네임이 비어 있으면 400 유효성 실패를 반환한다")
        void signup_blankNickname_returns400() throws Exception {
            String body = """
                    {
                      "email": "test@test.com",
                      "nickname": "",
                      "password": "password",
                      "address": { "city": "서울시", "district": "동작구", "town": "사당동" }
                    }
                    """;

            mockMvc.perform(post("/api/v1/auth")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.Data.nickname").exists());
        }

        @Test
        @DisplayName("[Exception] 비밀번호가 비어 있으면 400 유효성 실패를 반환한다")
        void signup_blankPassword_returns400() throws Exception {
            String body = """
                    {
                      "email": "test@test.com",
                      "nickname": "닉네임",
                      "password": "",
                      "address": { "city": "서울시", "district": "동작구", "town": "사당동" }
                    }
                    """;

            mockMvc.perform(post("/api/v1/auth")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.Data.password").exists());
        }

        @Test
        @DisplayName("[Exception] address가 null이면 400 유효성 실패를 반환한다")
        void signup_nullAddress_returns400() throws Exception {
            String body = """
                    {
                      "email": "test@test.com",
                      "nickname": "닉네임",
                      "password": "password",
                      "address": null
                    }
                    """;

            mockMvc.perform(post("/api/v1/auth")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.Data.address").exists());
        }

        @Test
        @DisplayName("[Exception] address.city가 비어 있으면 400 유효성 실패를 반환한다")
        void signup_blankAddressCity_returns400() throws Exception {
            String body = """
                    {
                      "email": "test@test.com",
                      "nickname": "닉네임",
                      "password": "password",
                      "address": { "city": "", "district": "동작구", "town": "사당동" }
                    }
                    """;

            mockMvc.perform(post("/api/v1/auth")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.Data['address.city']").exists());
        }

        @Test
        @DisplayName("[Exception] 여러 필드가 동시에 잘못되면 모든 오류가 Map으로 반환된다")
        void signup_multipleInvalidFields_returnsAllErrors() throws Exception {
            String body = """
                    {
                      "email": "bad-email",
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
                    .andExpect(jsonPath("$.Data.nickname").exists())
                    .andExpect(jsonPath("$.Data.password").exists());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/auth/login - 로그인")
    class Login {

        @Test
        @DisplayName("[HAPPY] 유효한 요청이면 200 및 서비스 반환값을 포함한다")
        void login_validRequest_returns200() throws Exception {
            given(authService.login(any())).willReturn("회원 아이디 ID : 1");

            String body = """
                    {
                      "email": "test@test.com",
                      "password": "password"
                    }
                    """;

            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.Data").value("회원 아이디 ID : 1"));
        }

        @Test
        @DisplayName("[Exception] 이메일이 비어 있으면 400 유효성 실패를 반환한다")
        void login_blankEmail_returns400() throws Exception {
            String body = """
                    {
                      "email": "",
                      "password": "password"
                    }
                    """;

            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.Data.email").exists());
        }

        @Test
        @DisplayName("[Exception] 이메일 형식이 올바르지 않으면 400 유효성 실패를 반환한다")
        void login_invalidEmailFormat_returns400() throws Exception {
            String body = """
                    {
                      "email": "not-an-email",
                      "password": "password"
                    }
                    """;

            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.Data.email").exists());
        }

        @Test
        @DisplayName("[Exception] 비밀번호가 비어 있으면 400 유효성 실패를 반환한다")
        void login_blankPassword_returns400() throws Exception {
            String body = """
                    {
                      "email": "test@test.com",
                      "password": ""
                    }
                    """;

            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.Data.password").exists());
        }

        @Test
        @DisplayName("[Exception] 요청 바디 필드가 모두 없으면 400을 반환한다")
        void login_emptyBody_returns400() throws Exception {
            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest());
        }
    }
}