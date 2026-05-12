package com.ktcloud.daangn.member.service;

import com.ktcloud.daangn.member.dto.MemberInfoResponseDto;
import com.ktcloud.daangn.member.entity.Member;

import java.util.Optional;

public interface MemberService {

    Boolean isEmailDuplicated(String email);

    Member register(Member member);

    Member getByIdOrThrow(Long id);

    Optional<Member> getByEmail(String email);

    MemberInfoResponseDto getMyInfo(Long id);
}
