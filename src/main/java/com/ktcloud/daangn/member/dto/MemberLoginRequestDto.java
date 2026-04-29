package com.ktcloud.daangn.member.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record MemberLoginRequestDto(

        @NotBlank
        @Email
        String email,

        @NotBlank
        String password
) {
}
