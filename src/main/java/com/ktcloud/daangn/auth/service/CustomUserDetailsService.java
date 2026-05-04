package com.ktcloud.daangn.auth.service;

import com.ktcloud.daangn.auth.dto.CustomUser;
import com.ktcloud.daangn.member.entity.Member;
import com.ktcloud.daangn.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberService memberService;

    @Override
    public UserDetails loadUserByUsername(@NonNull String email) throws UsernameNotFoundException {

        Member findMember = memberService.getByEmail(email).orElseThrow(() -> new UsernameNotFoundException("이메일 혹은 비밀번호 오류 입니다."));

        return new CustomUser(
                findMember.getId(),
                findMember.getEmail(),
                findMember.getPassword(),
                List.of(new SimpleGrantedAuthority(findMember.getMemberRole().toString()))
        );
    }
}
