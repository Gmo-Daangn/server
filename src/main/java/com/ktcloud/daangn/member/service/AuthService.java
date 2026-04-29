package com.ktcloud.daangn.member.service;

import com.ktcloud.daangn.member.dto.MemberLoginRequestDto;
import com.ktcloud.daangn.member.dto.MemberSignupRequestDto;

public interface AuthService {
    String signup(MemberSignupRequestDto dto);

    String login(MemberLoginRequestDto dto);
}
