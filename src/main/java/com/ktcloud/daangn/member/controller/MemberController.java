package com.ktcloud.daangn.member.controller;

import com.ktcloud.daangn.config.dto.BaseResponse;
import com.ktcloud.daangn.member.dto.MemberLoginRequestDto;
import com.ktcloud.daangn.member.dto.MemberSignupRequestDto;
import com.ktcloud.daangn.member.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @PostMapping("/auth")
    public BaseResponse<String> signup(@RequestBody @Valid MemberSignupRequestDto dto) {
        return BaseResponse.success(memberService.signup(dto));
    }

    @PostMapping("/auth/login")
    public BaseResponse<String> login(@RequestBody @Valid MemberLoginRequestDto dto) {
        return BaseResponse.success(memberService.login(dto));
    }
}
