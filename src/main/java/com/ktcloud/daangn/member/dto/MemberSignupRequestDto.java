package com.ktcloud.daangn.member.dto;

import com.ktcloud.daangn.member.entity.Member;
import com.ktcloud.daangn.member.entity.MemberRole;
import com.ktcloud.daangn.member.entity.ProviderToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

public record MemberSignupRequestDto(
        String email,
        String nickname,
        String password,
        String location

) {
    public Member toMember(PasswordEncoder encoder){
        return Member.builder()
                .email(email)
                .nickName(nickname)
                .password(encoder.encode(password))
                .createAt(LocalDateTime.now())
                .providerToken(ProviderToken.LOCAL)
                .memberRole(MemberRole.MEMBER)
                .build();

    }
}
