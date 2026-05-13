package com.ktcloud.daangn.auth.service;

import com.ktcloud.daangn.auth.dto.AuthSignupRequestDto;
import com.ktcloud.daangn.common.exception.InvalidInputException;
import com.ktcloud.daangn.member.entity.Member;
import com.ktcloud.daangn.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    private final MemberService memberService;
    private final PasswordEncoder encoder;

    @Override
    @Transactional
    public String signup(AuthSignupRequestDto dto) {
        if (memberService.isEmailDuplicated(dto.email())) {
            throw new InvalidInputException(HttpStatus.BAD_REQUEST.value(), "중복된 이메일입니다.");
        }
        String encodePassword = encoder.encode(dto.password());
        Member savedMember = memberService.register(dto.toMember(encodePassword));

        return "회원가입 성공 ID : "+savedMember.getId() ;
    }
}
