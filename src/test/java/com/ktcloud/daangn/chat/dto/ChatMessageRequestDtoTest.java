package com.ktcloud.daangn.chat.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ChatMessageRequestDtoTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    @DisplayName("[Validation] 메시지는 비어 있을 수 없다.")
    void validate_failsWhenMessageIsBlank() {
        ChatMessageRequestDto dto = new ChatMessageRequestDto("   ");

        assertThat(validator.validate(dto))
                .anySatisfy(violation -> {
                    assertThat(violation.getPropertyPath().toString()).isEqualTo("message");
                    assertThat(violation.getMessage()).isEqualTo("메시지는 비어 있을 수 없습니다.");
                });
    }
}
