package com.ktcloud.daangn.member.service;

import com.ktcloud.daangn.config.exception.InvalidInputException;
import com.ktcloud.daangn.member.entity.Member;
import com.ktcloud.daangn.member.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class MemberServiceUnitTest {

    @Mock
    MemberRepository memberRepository;

    @InjectMocks
    MemberServiceImpl memberService;

    @Test
    @DisplayName("Member가 존재할 경우 정상적으로 반환한다.")
    public void getByIdOrThrow_ValidMember_Success(){
        //given
        Member member = Member.builder()
                    .id(1L)
                    .email("test@test.com")
                    .build();

        given(memberRepository.findById(member.getId())).willReturn(Optional.of(member));
        //when
        Member findMember = memberService.getByIdOrThrow(member.getId());
        //then
        assertThat(findMember.getId()).isEqualTo(member.getId());
        assertThat(findMember.getEmail()).isEqualTo(member.getEmail());
    }

    @Test
    @DisplayName("Member가 존재하지 않을 경우 예외처리를 반환한다.")
    public void getByIdOrThrow_InvalidId_ThrowsException(){
        //given
        given(memberRepository.findById(99L)).willReturn(Optional.empty());
        //when & then
        assertThatThrownBy(() -> memberService.getByIdOrThrow(99L))
                .isInstanceOf(InvalidInputException.class)
                .hasMessage("존재하지 않는 ID입니다.");
    }
}