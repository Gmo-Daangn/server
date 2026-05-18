package com.ktcloud.daangn.member.controller;

import com.ktcloud.daangn.auth.authority.SecurityConfig;
import com.ktcloud.daangn.auth.dto.CustomUser;
import com.ktcloud.daangn.auth.jwt.JwtAccessDeniedHandler;
import com.ktcloud.daangn.auth.jwt.JwtAuthenticationEntryPoint;
import com.ktcloud.daangn.auth.jwt.JwtTokenProvider;
import com.ktcloud.daangn.common.valueObject.Address;
import com.ktcloud.daangn.member.dto.MemberInfoResponseDto;
import com.ktcloud.daangn.member.service.MemberService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MemberController.class)
@Import({
        SecurityConfig.class,
        JwtAuthenticationEntryPoint.class,
        JwtAccessDeniedHandler.class
})
@ExtendWith(RestDocumentationExtension.class)
class MemberControllerTest {

    private MockMvc mockMvc;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private MemberService memberService;

    @BeforeEach
    void setUp(WebApplicationContext context, RestDocumentationContextProvider restDocumentation) {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(documentationConfiguration(restDocumentation))
                .apply(springSecurity())
                .build();
    }

    @Nested
    @DisplayName("내정보 조회")
    class MyInfo{
        
        @Test
        @DisplayName("[HAPPY] 내 정보 조회시 정상적으로 반환된다.")
        public void getMyInfo_ValidMember_Success() throws Exception {
            //given
            MemberInfoResponseDto responseDto = new MemberInfoResponseDto(
                    "test@test.com",
                    "테스트",
                    new Address("서울", "강남", "역삼")
            );
            given(memberService.getMyInfo(1L)).willReturn(responseDto);

            CustomUser customUser = new CustomUser(
                    1L,
                    "test@test.com",
                    "password",
                    List.of(new SimpleGrantedAuthority("ROLE_MEMBER"))
            );
            //when & then
            mockMvc.perform(get("/api/v1/members")
                            .with(SecurityMockMvcRequestPostProcessors.user(customUser))
                            .header("Authorization", "Bearer {accessToken}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.email").value("test@test.com"))
                    .andExpect(jsonPath("$.data.nickname").value("테스트"))
                    .andDo(document("member-info-success",
                            requestHeaders(
                                headerWithName("Authorization").description("Bearer 액세스 토큰")
                            ),
                            responseFields(
                                fieldWithPath("code").description("HTTP 상태 코드"),
                                fieldWithPath("localDateTime").description("응답시간"),
                                fieldWithPath("message").description("응답 메시지"),
                                fieldWithPath("data.email").description("이메일"),
                                fieldWithPath("data.nickname").description("닉네임"),
                                fieldWithPath("data.address.city").description("시/도"),
                                fieldWithPath("data.address.district").description("구/군"),
                                fieldWithPath("data.address.town").description("동/읍/면")
                            )
                    ));
        }
        
        @Test
        @DisplayName("[Exception] 토큰 정보가 없을 시 조회가 불가능하다.")
        public void getMyInfo_NotValidMember_Failed() throws Exception{
            mockMvc.perform(get("/api/v1/members"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.code").value(401))
                    .andExpect(jsonPath("$.message").value("인증 오류"))
                    .andExpect(jsonPath("$.data").value("인증이 필요합니다."));
        }
    }
}