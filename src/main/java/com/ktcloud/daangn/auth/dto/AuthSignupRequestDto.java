package com.ktcloud.daangn.auth.dto;

import com.ktcloud.daangn.common.valueObject.Address;
import com.ktcloud.daangn.member.entity.Member;
import com.ktcloud.daangn.member.entity.MemberRole;
import com.ktcloud.daangn.member.entity.ProviderToken;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record AuthSignupRequestDto(

        @NotBlank
        @Email
        String email,
        @NotBlank
        String nickname,
        @NotBlank
        String password,
        @NotNull
        @Valid
        Address address

) {
    public Member toMember(String encodePassword){
        return Member.builder()
                .email(email)
                .nickName(nickname)
                .password(encodePassword)
                .address(address)
                .createAt(LocalDateTime.now())
                .providerToken(ProviderToken.LOCAL)
                .memberRole(MemberRole.MEMBER)
                .build();

    }
}
