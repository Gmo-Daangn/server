package com.ktcloud.daangn.chat.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ChatMessageDeleteRequestDtoTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    @DisplayName("[Validation] 삭제 요청 작성자 회원 ID는 비어 있을 수 없다.")
    void validate_failsWhenMemberIdIsNull() {
        ChatMessageDeleteRequestDto dto = new ChatMessageDeleteRequestDto(null);

        assertThat(validator.validate(dto))
                .anySatisfy(violation -> {
                    assertThat(violation.getPropertyPath().toString()).isEqualTo("memberId");
                    assertThat(violation.getMessage()).isEqualTo("회원 ID는 비어 있을 수 없습니다.");
                });
    }
}
