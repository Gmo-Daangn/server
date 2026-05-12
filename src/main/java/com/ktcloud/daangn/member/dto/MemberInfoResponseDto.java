package com.ktcloud.daangn.member.dto;

import com.ktcloud.daangn.config.valueObject.Address;
import com.ktcloud.daangn.member.entity.Member;

public record MemberInfoResponseDto(
        String email,
        String nickname,
        Address address
) {
    public static MemberInfoResponseDto from(Member member) {
        return new MemberInfoResponseDto(
                member.getEmail(),
                member.getNickName(),
                member.getAddress()
        );
    }
}
