package com.ktcloud.daangn.auth.dto;

import com.ktcloud.daangn.config.valueObject.Address;
import com.ktcloud.daangn.member.entity.Member;
import com.ktcloud.daangn.member.entity.MemberRole;
import com.ktcloud.daangn.member.entity.ProviderToken;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class AuthSignupRequestDtoTest {

    static Validator validator;

    @BeforeAll
    static void setupValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private static final Address VALID_ADDRESS = new Address("서울시", "동작구", "사당동");

    // ── toMember() conversion ─────────────────────────────────────────────────

    @Nested
    @DisplayName("toMember() 변환 검증")
    class ToMember {

        @Test
        @DisplayName("encodePassword가 Member의 password로 설정된다")
        void toMember_encodedPasswordSet() {
            AuthSignupRequestDto dto = new AuthSignupRequestDto(
                    "test@test.com", "닉네임", "rawPassword", VALID_ADDRESS
            );

            Member member = dto.toMember("encodedPassword");

            assertThat(member.getPassword()).isEqualTo("encodedPassword");
        }

        @Test
        @DisplayName("dto의 email이 Member의 email로 설정된다")
        void toMember_emailSet() {
            AuthSignupRequestDto dto = new AuthSignupRequestDto(
                    "user@example.com", "닉네임", "password", VALID_ADDRESS
            );

            Member member = dto.toMember("encoded");

            assertThat(member.getEmail()).isEqualTo("user@example.com");
        }

        @Test
        @DisplayName("dto의 nickname이 Member의 nickName으로 설정된다")
        void toMember_nicknameSet() {
            AuthSignupRequestDto dto = new AuthSignupRequestDto(
                    "test@test.com", "홍길동", "password", VALID_ADDRESS
            );

            Member member = dto.toMember("encoded");

            assertThat(member.getNickName()).isEqualTo("홍길동");
        }

        @Test
        @DisplayName("dto의 address가 Member의 address로 설정된다")
        void toMember_addressSet() {
            AuthSignupRequestDto dto = new AuthSignupRequestDto(
                    "test@test.com", "닉네임", "password", VALID_ADDRESS
            );

            Member member = dto.toMember("encoded");

            assertThat(member.getAddress()).isEqualTo(VALID_ADDRESS);
        }

        @Test
        @DisplayName("providerToken은 LOCAL로 설정된다")
        void toMember_providerTokenIsLocal() {
            AuthSignupRequestDto dto = new AuthSignupRequestDto(
                    "test@test.com", "닉네임", "password", VALID_ADDRESS
            );

            Member member = dto.toMember("encoded");

            assertThat(member.getProviderToken()).isEqualTo(ProviderToken.LOCAL);
        }

        @Test
        @DisplayName("memberRole은 MEMBER로 설정된다")
        void toMember_memberRoleIsMember() {
            AuthSignupRequestDto dto = new AuthSignupRequestDto(
                    "test@test.com", "닉네임", "password", VALID_ADDRESS
            );

            Member member = dto.toMember("encoded");

            assertThat(member.getMemberRole()).isEqualTo(MemberRole.MEMBER);
        }

        @Test
        @DisplayName("createAt이 null이 아니다")
        void toMember_createAtIsSet() {
            AuthSignupRequestDto dto = new AuthSignupRequestDto(
                    "test@test.com", "닉네임", "password", VALID_ADDRESS
            );

            Member member = dto.toMember("encoded");

            assertThat(member.getCreateAt()).isNotNull();
        }

        @Test
        @DisplayName("원본 rawPassword는 Member에 저장되지 않는다")
        void toMember_rawPasswordNotStored() {
            AuthSignupRequestDto dto = new AuthSignupRequestDto(
                    "test@test.com", "닉네임", "rawPassword", VALID_ADDRESS
            );

            Member member = dto.toMember("hashed");

            assertThat(member.getPassword()).isNotEqualTo("rawPassword");
        }
    }

    // ── Validation constraints ────────────────────────────────────────────────

    @Nested
    @DisplayName("유효성 검사")
    class Validation {

        @Test
        @DisplayName("모든 필드가 유효하면 위반이 없다")
        void allValid_noViolations() {
            AuthSignupRequestDto dto = new AuthSignupRequestDto(
                    "test@test.com", "닉네임", "password", VALID_ADDRESS
            );

            Set<ConstraintViolation<AuthSignupRequestDto>> violations = validator.validate(dto);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("email이 형식에 맞지 않으면 위반이 발생한다")
        void invalidEmail_violation() {
            AuthSignupRequestDto dto = new AuthSignupRequestDto(
                    "not-an-email", "닉네임", "password", VALID_ADDRESS
            );

            Set<ConstraintViolation<AuthSignupRequestDto>> violations = validator.validate(dto);

            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("email"));
        }

        @Test
        @DisplayName("nickname이 blank이면 위반이 발생한다")
        void blankNickname_violation() {
            AuthSignupRequestDto dto = new AuthSignupRequestDto(
                    "test@test.com", "", "password", VALID_ADDRESS
            );

            Set<ConstraintViolation<AuthSignupRequestDto>> violations = validator.validate(dto);

            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("nickname"));
        }

        @Test
        @DisplayName("password가 blank이면 위반이 발생한다")
        void blankPassword_violation() {
            AuthSignupRequestDto dto = new AuthSignupRequestDto(
                    "test@test.com", "닉네임", "", VALID_ADDRESS
            );

            Set<ConstraintViolation<AuthSignupRequestDto>> violations = validator.validate(dto);

            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("password"));
        }

        @Test
        @DisplayName("address가 null이면 위반이 발생한다")
        void nullAddress_violation() {
            AuthSignupRequestDto dto = new AuthSignupRequestDto(
                    "test@test.com", "닉네임", "password", null
            );

            Set<ConstraintViolation<AuthSignupRequestDto>> violations = validator.validate(dto);

            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("address"));
        }
    }
}