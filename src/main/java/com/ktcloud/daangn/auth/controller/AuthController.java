package com.ktcloud.daangn.auth.controller;

import com.ktcloud.daangn.auth.dto.AuthSignupRequestDto;
import com.ktcloud.daangn.auth.service.AuthService;
import com.ktcloud.daangn.config.dto.BaseResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping
    public BaseResponse<String> signup(@RequestBody @Valid AuthSignupRequestDto dto) {
        return BaseResponse.success(authService.signup(dto));
    }
}
