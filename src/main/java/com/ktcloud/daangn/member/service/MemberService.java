package com.ktcloud.daangn.member.service;

import com.ktcloud.daangn.config.dto.BaseResponse;
import com.ktcloud.daangn.member.dto.MemberLoginRequestDto;
import com.ktcloud.daangn.member.dto.MemberSignupRequestDto;
import com.ktcloud.daangn.member.entity.Member;
import org.springframework.stereotype.Service;

public interface MemberService {
    String signup(MemberSignupRequestDto dto);

    String login(MemberLoginRequestDto dto);
}
