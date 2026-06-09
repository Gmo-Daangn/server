package com.ktcloud.daangn.auth.service;

import com.ktcloud.daangn.member.service.MemberService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceUnitTest {

    @Mock private MemberService memberService;

    @InjectMocks private CustomUserDetailsService customUserDetailsService;

    @Test
    void loadUserByUsername_NotFound_ThrowsException() {
        //given
        String email = "test@test.com";
        given(memberService.getByEmail(email)).willReturn(Optional.empty());

        //when, then
        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername(email))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("이메일 혹은 비밀번호 오류 입니다.");
    }
}