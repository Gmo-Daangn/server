package com.ktcloud.daangn.member.service;

import com.ktcloud.daangn.config.dto.BaseResponse;
import com.ktcloud.daangn.config.exception.InvalidInputException;
import com.ktcloud.daangn.member.dto.MemberLoginRequestDto;
import com.ktcloud.daangn.member.dto.MemberSignupRequestDto;
import com.ktcloud.daangn.member.entity.Member;
import com.ktcloud.daangn.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberServiceImpl implements MemberService{

    private final MemberRepository memberRepository;
    private final PasswordEncoder encoder;

    @Override
    @Transactional
    public String signup(MemberSignupRequestDto dto) {
        memberRepository.findByEmail(dto.email())
                .ifPresent( m -> {
                    throw new InvalidInputException(HttpStatus.BAD_REQUEST.value(), "중복된 이메일입니다.");
                });
        Member savedMember = memberRepository.save(dto.toMember(encoder));

        return "회원가입 성공 ID : "+savedMember.getId() ;
    }

    @Override
    public String login(MemberLoginRequestDto dto) {

        return null;
    }
}
