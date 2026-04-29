package com.ktcloud.daangn.member.service;

import com.ktcloud.daangn.config.TestContainerConfig;
import com.ktcloud.daangn.config.exception.InvalidInputException;
import com.ktcloud.daangn.config.valueObject.Address;
import com.ktcloud.daangn.member.dto.MemberLoginRequestDto;
import com.ktcloud.daangn.member.dto.MemberSignupRequestDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


@SpringBootTest
@Import(TestContainerConfig.class)
@ActiveProfiles("test")
@Transactional
class AuthServiceIntegrationTest {

    @Autowired
    private AuthService authService;

    private  final Address address = new Address("서울시","동작구","사당동");

    @Nested
    @DisplayName("회원가입")
    class Signup {

        @Test
        @DisplayName("[HAPPY] 회원가입이 정상적으로 작동한다.")
        void signup_validRequest_success() {
            MemberSignupRequestDto dto = new MemberSignupRequestDto(
                    "test@test.com", "nickname", "password", address
            );

            String message = authService.signup(dto);

            assertThat(message).contains("회원가입 성공");
        }

        @Test
        @DisplayName("[Exception] 중복된 이메일로 회원가입하면 예외처리가 정상작동한다.")
        public void signup_DuplicateEmail_ExceptionThrown(){
            //given
            MemberSignupRequestDto dto = new MemberSignupRequestDto("test@test.com", "이름", "password", address);
            //when
            authService.signup(dto);
            //then
            assertThatThrownBy(() -> authService.signup(dto))
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
            MemberSignupRequestDto signupDto = new MemberSignupRequestDto("test@test.com", "이름", "password", address);
            authService.signup(signupDto);

            MemberLoginRequestDto loginDto = new MemberLoginRequestDto(signupDto.email(), signupDto.password());

            //when
            String message = authService.login(loginDto);

            //then
            assertThat(message).contains("회원 아이디 ID");
        }

        @Test
        @DisplayName("[Exception] 로그인 시 없는 이메일일 경우 예외처리 반환한다.")
        public void login_nonExistentEmail_ExceptionThrown(){
            //given
            MemberLoginRequestDto dto = new MemberLoginRequestDto("test@test.com", "password");
            //when & then
            assertThatThrownBy( () -> authService.login(dto))
                    .isInstanceOf(InvalidInputException.class)
                    .hasMessage("이메일 혹은 비밀번호 오류입니다.");
        }

        @Test
        @DisplayName("[Exception] 로그인 시 비밀번호가 틀린 경우 예외처리 반환한다.")
        public void login_WrongPassword_ExceptionThrown(){
            //given
            MemberSignupRequestDto signupDto = new MemberSignupRequestDto("test@test.com", "이름", "password", address);
            authService.signup(signupDto);

            MemberLoginRequestDto loginDto = new MemberLoginRequestDto(signupDto.email(), "wrongPassword");

            //when & then
            assertThatThrownBy(() -> authService.login(loginDto))
                    .isInstanceOf(InvalidInputException.class)
                    .hasMessage("이메일 혹은 비밀번호 오류입니다.");
        }
    }
}