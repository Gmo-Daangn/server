package com.ktcloud.daangn.member.controller;

import com.ktcloud.daangn.auth.authority.SecurityConfig;
import com.ktcloud.daangn.auth.dto.CustomUser;
import com.ktcloud.daangn.auth.jwt.JwtTokenProvider;
import com.ktcloud.daangn.config.valueObject.Address;
import com.ktcloud.daangn.member.dto.MemberInfoResponseDto;
import com.ktcloud.daangn.member.service.MemberService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MemberController.class)
@Import(SecurityConfig.class)
class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private SecurityConfig securityConfig;

    @MockitoBean
    private MemberService memberService;

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
            mockMvc.perform(get("/api/v1/members").with(SecurityMockMvcRequestPostProcessors.user(customUser)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.email").value("test@test.com"))
                    .andExpect(jsonPath("$.data.nickname").value("테스트"));
        }
        
        @Test
        @DisplayName("[Exception] 토큰 정보가 없을 시 조회가 불가능하다.")
        public void getMyInfo_NotValidMember_Failed() throws Exception{
            mockMvc.perform(get("/api/v1/members"))
                    .andExpect(status().isUnauthorized());
        }
    }
}