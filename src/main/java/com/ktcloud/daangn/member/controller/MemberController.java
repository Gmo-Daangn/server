package com.ktcloud.daangn.member.controller;

import com.ktcloud.daangn.auth.dto.CustomUser;
import com.ktcloud.daangn.common.dto.BaseResponse;
import com.ktcloud.daangn.member.dto.MemberInfoResponseDto;
import com.ktcloud.daangn.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @GetMapping
    public BaseResponse<MemberInfoResponseDto> getMyInfo(@AuthenticationPrincipal CustomUser user) {
        Long memberId = user.getMemberId();
        return BaseResponse.success(memberService.getMyInfo(memberId));
    }


}
