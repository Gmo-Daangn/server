package com.ktcloud.daangn.member.repository;

import com.ktcloud.daangn.member.entity.Member;
import org.springframework.stereotype.Repository;

import java.util.Optional;

public interface MemberRepository {

    Member save(Member member);

    Optional<Member> findById(Long id);

    Optional<Member> findByEmail(String email);
}
