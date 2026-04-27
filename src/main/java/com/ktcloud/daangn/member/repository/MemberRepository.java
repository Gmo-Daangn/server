package com.ktcloud.daangn.member.repository;

import com.ktcloud.daangn.member.entity.Member;

import java.util.Optional;

public interface MemberRepository {

    Boolean existsByEmail(String email);

    Member save(Member member);

    Member findById(Long id);

    Optional<Member> findByEmail(String email);
}
