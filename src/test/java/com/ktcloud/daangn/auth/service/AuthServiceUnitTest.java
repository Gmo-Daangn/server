package com.ktcloud.daangn.auth.service;

import com.ktcloud.daangn.config.exception.InvalidInputException;
import com.ktcloud.daangn.config.valueObject.Address;
import com.ktcloud.daangn.auth.dto.AuthLoginRequestDto;
import com.ktcloud.daangn.auth.dto.AuthSignupRequestDto;
import com.ktcloud.daangn.member.entity.Member;
import com.ktcloud.daangn.member.service.MemberService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceUnitTest {

    @Mock
    MemberService memberService;

    @Mock
    PasswordEncoder encoder;

    @InjectMocks
    AuthServiceImpl authService;

    @Nested
    @DisplayName("회원가입")
    class Signup {

        private  final Address address = new Address("서울시","동작구","사당동");

        @Test
        @DisplayName("[HAPPY] 회원가입이 정상적으로 작동한다.")
        public void signup_validRequest_success(){
            //given
            AuthSignupRequestDto dto = new AuthSignupRequestDto("test@test.com", "이름", "password", address);

            Member savedMember = Member.builder()
                    .id(1L)
                    .email(dto.email())
                    .build();

            given(encoder.encode(any())).willReturn("encodePassword");
            //when
            when(memberService.register(any())).thenReturn(savedMember);
            String message = authService.signup(dto);

            //then
            assertThat(message).isEqualTo("회원가입 성공 ID : 1");
        }

        @Test
        @DisplayName("[Exception] 중복된 이메일로 회원가입하면 예외처리가 정상작동한다.")
        public void signup_DuplicateEmail_ExceptionThrown(){
            //given
            AuthSignupRequestDto dto = new AuthSignupRequestDto("test@test.com", "이름", "password", address);

            given(memberService.isEmailDuplicated(dto.email())).willReturn(true);
            //when & then
            assertThatThrownBy(() -> authService.signup(dto))
                    .isInstanceOf(InvalidInputException.class)
                    .hasMessage("중복된 이메일입니다.");
        }
    }
}