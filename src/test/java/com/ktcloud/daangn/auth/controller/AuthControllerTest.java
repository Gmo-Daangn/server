package com.ktcloud.daangn.auth.controller;

import com.ktcloud.daangn.auth.dto.AuthSignupRequestDto;
import com.ktcloud.daangn.auth.service.AuthService;
import com.ktcloud.daangn.config.TestContainerConfig;
import com.ktcloud.daangn.config.valueObject.Address;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import tools.jackson.databind.json.JsonMapper;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@WebMvcTest(AuthController.class)
@Import(TestContainerConfig.class)
@ExtendWith(RestDocumentationExtension.class)
public class AuthControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private JsonMapper jsonMapper;

    @MockitoBean
    private AuthService authService;

    @BeforeEach
    void setUp(WebApplicationContext webApplicationContext, RestDocumentationContextProvider restDocumentation) {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(documentationConfiguration(restDocumentation))
                .build();
    }

    @Nested
    @DisplayName("회원가입")
    class SignUp {
        private  final Address address = new Address("서울시","동작구","사당동");

        @Test
        @DisplayName("[Happy] 회원가입시 정상적으로 작동한다.")
        public void signup_validRequest_success() throws Exception{
            //given
            AuthSignupRequestDto dto = new AuthSignupRequestDto("test@test.com", "이름", "password", address);

            //expected
            mockMvc.perform(post("/api/v1/auth")
                            .accept(MediaType.APPLICATION_JSON_VALUE)
                            .contentType(APPLICATION_JSON_VALUE)
                            .content(jsonMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk())
                    .andDo(document("auth-signup-success", requestFields(
                            fieldWithPath("email").description("이메일"),
                            fieldWithPath("password").description("비밀번호"),
                            fieldWithPath("nickname").description("닉네임"),
                            subsectionWithPath("address").description("주소 정보")
                    ),responseFields(
                            fieldWithPath("code").description("HTTP Status Code"),
                            fieldWithPath("localDateTime").description("시간"),
                            fieldWithPath("message").description("설명 메시지"),
                            fieldWithPath("data").description("데이터")
                    )));
        }
    }
}


