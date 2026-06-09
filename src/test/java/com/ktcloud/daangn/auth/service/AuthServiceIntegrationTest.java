package com.ktcloud.daangn.auth.service;

import com.ktcloud.daangn.auth.dto.AuthSignupRequestDto;
import com.ktcloud.daangn.common.exception.InvalidInputException;
import com.ktcloud.daangn.common.valueObject.Address;
import com.ktcloud.daangn.config.TestContainerConfig;
import com.ktcloud.daangn.member.entity.Member;
import com.ktcloud.daangn.member.entity.MemberRole;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AuthServiceIntegrationTest extends TestContainerConfig {

    @Autowired
    private AuthService authService;

    @Autowired
    private EntityManager em;

    private final Address address = new Address("서울시", "동작구", "사당동");

    @Nested
    @DisplayName("회원가입")
    class Signup {


        @Test
        @DisplayName("[HAPPY] 회원가입이 정상적으로 작동한다.")
        void signup_ValidRequest_Success() {
            //given
            String email = "test@test.com";
            String password = "password";
            String nickname = "nickname";
            AuthSignupRequestDto dto = new AuthSignupRequestDto(email, nickname, password, address);
            //when
            String message = authService.signup(dto);
            em.flush();
            em.clear();
            //then
            assertThat(message).isEqualTo("회원가입 성공");

            Member findMember = em.createQuery("select m from Member m where m.email = :email", Member.class)
                    .setParameter("email", email)
                    .getSingleResult();

            assertThat(findMember)
                    .satisfies(member -> {
                        assertThat(member.getEmail()).isEqualTo(email);
                        assertThat(member.getNickName()).isEqualTo(nickname);
                        assertThat(member.getPassword()).isNotEqualTo(password);
                        assertThat(member.getBalance()).isEqualTo(0L);
                        assertThat(member.getAddress()).isEqualTo(address);
                        assertThat(member.getMemberRole()).isEqualTo(MemberRole.MEMBER);
                    });
        }

        @Test
        @DisplayName("[Exception] 중복된 이메일로 회원가입하면 예외처리가 정상작동한다.")
        public void signup_DuplicateEmail_ExceptionThrown() {
            //given
            AuthSignupRequestDto dto = new AuthSignupRequestDto("test@test.com", "이름", "password", address);
            //when
            authService.signup(dto);
            //then
            assertThatThrownBy(() -> authService.signup(dto))
                    .isInstanceOf(InvalidInputException.class)
                    .hasMessage("중복된 이메일입니다.");
        }
    }
}
