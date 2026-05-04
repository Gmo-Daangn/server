package com.ktcloud.daangn.chat.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ChatMessageWriteRequestDtoTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    @DisplayName("[Validation] 메시지는 비어 있을 수 없다.")
    void validate_failsWhenMessageIsBlank() {
        ChatMessageWriteRequestDto dto = new ChatMessageWriteRequestDto("a@test.com", "   ");

        assertThat(validator.validate(dto))
                .anySatisfy(violation -> {
                    assertThat(violation.getPropertyPath().toString()).isEqualTo("message");
                    assertThat(violation.getMessage()).isEqualTo("메시지는 비어 있을 수 없습니다.");
                });
    }

    @Test
    @DisplayName("[Validation] 작성자 이메일은 비어 있을 수 없다.")
    void validate_failsWhenMemberEmailIsBlank() {
        ChatMessageWriteRequestDto dto = new ChatMessageWriteRequestDto("   ", "hello");

        assertThat(validator.validate(dto))
                .anySatisfy(violation -> {
                    assertThat(violation.getPropertyPath().toString()).isEqualTo("memberEmail");
                    assertThat(violation.getMessage()).isEqualTo("이메일은 비어 있을 수 없습니다.");
                });
    }
}
