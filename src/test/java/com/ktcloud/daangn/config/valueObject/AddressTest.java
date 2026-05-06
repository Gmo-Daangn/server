package com.ktcloud.daangn.config.valueObject;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class AddressTest {

    static Validator validator;

    @BeforeAll
    static void setupValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("city, district, town이 모두 존재하면 유효성 검사를 통과한다")
    void address_allFieldsPresent_noViolations() {
        Address address = new Address("서울시", "동작구", "사당동");

        Set<ConstraintViolation<Address>> violations = validator.validate(address);

        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("city가 blank이면 유효성 검사에 실패한다")
    void address_blankCity_hasViolation() {
        Address address = new Address("", "동작구", "사당동");

        Set<ConstraintViolation<Address>> violations = validator.validate(address);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("city"));
    }

    @Test
    @DisplayName("city가 null이면 유효성 검사에 실패한다")
    void address_nullCity_hasViolation() {
        Address address = new Address(null, "동작구", "사당동");

        Set<ConstraintViolation<Address>> violations = validator.validate(address);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("city"));
    }

    @Test
    @DisplayName("district가 blank이면 유효성 검사에 실패한다")
    void address_blankDistrict_hasViolation() {
        Address address = new Address("서울시", "", "사당동");

        Set<ConstraintViolation<Address>> violations = validator.validate(address);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("district"));
    }

    @Test
    @DisplayName("district가 null이면 유효성 검사에 실패한다")
    void address_nullDistrict_hasViolation() {
        Address address = new Address("서울시", null, "사당동");

        Set<ConstraintViolation<Address>> violations = validator.validate(address);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("district"));
    }

    @Test
    @DisplayName("town이 blank이면 유효성 검사에 실패한다")
    void address_blankTown_hasViolation() {
        Address address = new Address("서울시", "동작구", "");

        Set<ConstraintViolation<Address>> violations = validator.validate(address);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("town"));
    }

    @Test
    @DisplayName("town이 null이면 유효성 검사에 실패한다")
    void address_nullTown_hasViolation() {
        Address address = new Address("서울시", "동작구", null);

        Set<ConstraintViolation<Address>> violations = validator.validate(address);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("town"));
    }

    @Test
    @DisplayName("세 필드 모두 blank이면 세 개의 위반이 발생한다")
    void address_allBlank_threeViolations() {
        Address address = new Address("", "", "");

        Set<ConstraintViolation<Address>> violations = validator.validate(address);

        assertThat(violations).hasSize(3);
    }

    @Test
    @DisplayName("같은 값으로 생성된 Address는 동등하다 (record equality)")
    void address_sameValues_equals() {
        Address a1 = new Address("서울시", "동작구", "사당동");
        Address a2 = new Address("서울시", "동작구", "사당동");

        assertThat(a1).isEqualTo(a2);
    }

    @Test
    @DisplayName("Address의 각 필드는 레코드 accessor로 올바르게 반환된다")
    void address_accessors_returnCorrectValues() {
        Address address = new Address("부산시", "해운대구", "우동");

        assertThat(address.city()).isEqualTo("부산시");
        assertThat(address.district()).isEqualTo("해운대구");
        assertThat(address.town()).isEqualTo("우동");
    }
}