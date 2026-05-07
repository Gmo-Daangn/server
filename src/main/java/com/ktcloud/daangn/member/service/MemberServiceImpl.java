package com.ktcloud.daangn.member.service;

import com.ktcloud.daangn.config.exception.InvalidInputException;
import com.ktcloud.daangn.member.entity.Member;
import com.ktcloud.daangn.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService{

    private final MemberRepository memberRepository;

    @Override
    public Boolean isEmailDuplicated(String email) {
        return memberRepository.existsByEmail(email);
    }

    @Override
    public Member register(Member member) {
        return memberRepository.save(member);
    }

    @Override
    public Member getByIdOrThrow(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new InvalidInputException(HttpStatus.BAD_REQUEST.value(), "존재하지 않는 ID입니다."));
    }

    @Override
    public Optional<Member> getByEmail(String email) {
        return memberRepository.findByEmail(email);
    }
}
