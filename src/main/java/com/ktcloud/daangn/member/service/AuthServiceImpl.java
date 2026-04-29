package com.ktcloud.daangn.member.service;

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

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder encoder;

    @Override
    @Transactional
    public String signup(MemberSignupRequestDto dto) {
        if (memberRepository.existsByEmail(dto.email())) {
            throw new InvalidInputException(HttpStatus.BAD_REQUEST.value(), "중복된 이메일입니다.");
        }
        String encodePassword = encoder.encode(dto.password());
        Member savedMember = memberRepository.save(dto.toMember(encodePassword));

        return "회원가입 성공 ID : "+savedMember.getId() ;
    }

    @Override
    public String login(MemberLoginRequestDto dto) {
        Optional<Member> findMember = memberRepository.findByEmail(dto.email());
        if (findMember.isEmpty()) throw new InvalidInputException(HttpStatus.BAD_REQUEST.value(), "이메일 혹은 비밀번호 오류입니다.");
        else if (!encoder.matches(dto.password(), findMember.get().getPassword())) throw new InvalidInputException(HttpStatus.BAD_REQUEST.value(), "이메일 혹은 비밀번호 오류입니다.");
        return "회원 아이디 ID : "+ findMember.get().getId();
    }
}
