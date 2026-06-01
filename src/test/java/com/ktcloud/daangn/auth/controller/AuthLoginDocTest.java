package com.ktcloud.daangn.auth.controller;

import com.ktcloud.daangn.auth.dto.AuthLoginRequestDto;
import com.ktcloud.daangn.auth.dto.AuthSignupRequestDto;
import com.ktcloud.daangn.common.valueObject.Address;
import com.ktcloud.daangn.config.TestContainerConfig;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import tools.jackson.databind.json.JsonMapper;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@ExtendWith(RestDocumentationExtension.class)
public class AuthLoginDocTest extends TestContainerConfig {
    /**
     * 로그인 로직의 경우 스프링 시큐리티관리로 필터에서 로그인이 진행되므로
     * SpringBootTest로 작성
     */
    @Autowired
    private EntityManager em;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JsonMapper jsonMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp(WebApplicationContext context, RestDocumentationContextProvider restDocumentation) {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(documentationConfiguration(restDocumentation))
                .apply(springSecurity())
                .build();
    }

    @Nested
    @DisplayName("로그인")
    class Login {

        private final Address address = new Address("서울시", "동작구", "사당동");

        @Test
        @Transactional
        @DisplayName("[HAPPY] 로그인시 정상적으로 작동한다.")
        public void login_ValidRequest_Success() throws Exception {
            //given
            AuthSignupRequestDto dto = new AuthSignupRequestDto("test@test.com", "이름", "password", address);
            em.persist(dto.toMember(passwordEncoder.encode(dto.password())));

            AuthLoginRequestDto loginDto = new AuthLoginRequestDto("test@test.com", "password");
            //expected
            mockMvc.perform(post("/api/v1/auth/login")
                            .accept(MediaType.APPLICATION_JSON_VALUE)
                            .contentType(APPLICATION_JSON_VALUE)
                            .content(jsonMapper.writeValueAsString(loginDto)))
                    .andExpect(status().isOk())
                    .andDo(document("auth-login-success", requestFields(
                            fieldWithPath("email").description("이메일"),
                            fieldWithPath("password").description("비밀번호")
                    ), responseFields(
                            fieldWithPath("code").description("HTTP Status Code"),
                            fieldWithPath("localDateTime").description("시간"),
                            fieldWithPath("message").description("설명 메시지"),
                            fieldWithPath("data.grantType").description("토큰 타입"),
                            fieldWithPath("data.accessToken").description("접근 토큰")
                    )));
        }
    }
}
