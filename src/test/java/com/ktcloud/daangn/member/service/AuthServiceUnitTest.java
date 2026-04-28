package com.ktcloud.daangn.member.service;

import com.ktcloud.daangn.config.exception.InvalidInputException;
import com.ktcloud.daangn.config.valueObject.Address;
import com.ktcloud.daangn.member.dto.MemberLoginRequestDto;
import com.ktcloud.daangn.member.dto.MemberSignupRequestDto;
import com.ktcloud.daangn.member.entity.Member;
import com.ktcloud.daangn.member.repository.MemberDBRepository;
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
    MemberDBRepository memberRepository;

    @Mock
    PasswordEncoder encoder;

    @InjectMocks
    AuthServiceImpl memberService;

    @Nested
    @DisplayName("회원가입")
    class Signup {

        private  final Address address = new Address("서울시","동작구","사당동");

        @Test
        @DisplayName("[HAPPY] 회원가입이 정상적으로 작동한다.")
        public void signup_validRequest_success(){
            //given
            MemberSignupRequestDto dto = new MemberSignupRequestDto("test@test.com", "이름", "password", address);

            Member savedMember = Member.builder()
                    .id(1L)
                    .email(dto.email())
                    .build();

            given(encoder.encode(any())).willReturn("encodePassword");
            //when
            when(memberRepository.save(any())).thenReturn(savedMember);
            String message = memberService.signup(dto);

            //then
            assertThat(message).isEqualTo("회원가입 성공 ID : 1");
        }

        @Test
        @DisplayName("[Exception] 중복된 이메일로 회원가입하면 예외처리가 정상작동한다.")
        public void signup_DuplicateEmail_ExceptionThrown(){
            //given
            MemberSignupRequestDto dto = new MemberSignupRequestDto("test@test.com", "이름", "password", address);

            given(memberRepository.existsByEmail(dto.email())).willReturn(true);
            //when & then
            assertThatThrownBy(() -> memberService.signup(dto))
                    .isInstanceOf(InvalidInputException.class)
                    .hasMessage("중복된 이메일입니다.");
        }
    }

    @Nested
    @DisplayName("로그인")
    class Login {
        @Test
        @DisplayName("[HAPPY] 로그인이 정상적으로 진행된다.")
        public void login_validRequest_Success(){
            //given
            MemberLoginRequestDto dto = new MemberLoginRequestDto("test@test.com", "password");

            Member findMember = Member.builder()
                    .id(1L)
                    .email(dto.email())
                    .password("encodePassword")
                    .build();

            given(memberRepository.findByEmail(dto.email())).willReturn(Optional.of(findMember));
            given(encoder.matches(dto.password(), findMember.getPassword())).willReturn(true);
            //when
            String message = memberService.login(dto);
            //then
            assertThat(message).isEqualTo("회원 아이디 ID : 1");
        }
        
        @Test
        @DisplayName("[Exception] 로그인 시 없는 이메일일 경우 예외처리 반환한다.")
        public void login_nonExistentEmail_ExceptionThrown(){
            //given
            MemberLoginRequestDto dto = new MemberLoginRequestDto("test@test.com", "password");
            //when
            when(memberRepository.findByEmail(dto.email())).thenReturn(Optional.empty());
            //then
            assertThatThrownBy(() -> memberService.login(dto))
                    .isInstanceOf(InvalidInputException.class)
                    .hasMessage("이메일 혹은 비밀번호 오류입니다.");
        }

        @Test
        @DisplayName("[Exception] 로그인 시 비밀번호가 틀린 경우 예외처리 반환한다.")
        public void login_WrongPassword_ExceptionThrown(){
            //given
            MemberLoginRequestDto dto = new MemberLoginRequestDto("test@test.com", "password");
            Member findMember = Member.builder()
                    .email(dto.email())
                    .password("encodePassword")
                    .build();

            given(memberRepository.findByEmail(dto.email())).willReturn(Optional.of(findMember));
            //when
            when(encoder.matches(dto.password(), findMember.getPassword())).thenReturn(false);
            //then
            assertThatThrownBy(() -> memberService.login(dto))
                    .isInstanceOf(InvalidInputException.class)
                    .hasMessage("이메일 혹은 비밀번호 오류입니다.");
        }
    }
}