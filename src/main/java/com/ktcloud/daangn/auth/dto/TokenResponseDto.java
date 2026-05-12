package com.ktcloud.daangn.auth.dto;

public record TokenResponseDto(
        String grantType,
        String accessToken
) {
}
