package com.ktcloud.daangn.member.service;

import com.ktcloud.daangn.common.exception.InvalidInputException;
import com.ktcloud.daangn.common.valueObject.Address;
import com.ktcloud.daangn.member.dto.MemberInfoResponseDto;
import com.ktcloud.daangn.member.entity.Member;
import com.ktcloud.daangn.member.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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

    @Nested
    @DisplayName("getByIdOrThrow")
    class getByIdOrThrow{
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

    @Test
    @DisplayName("[HAPPY] 내정보 조회를 성공적으로 진행한다.")
    public void getMyInfo_ValidMember_Success() {
        //given
        Member findMember = Member.builder()
                .id(1L)
                .email("test@test.com")
                .nickName("nickname")
                .address(new Address("서울", "강남", "역삼"))
                .build();
        given(memberRepository.findById(1L)).willReturn(Optional.of(findMember));

        //when
        MemberInfoResponseDto result = memberService.getMyInfo(1L);
        //then
        assertThat(result.email()).isEqualTo("test@test.com");
        assertThat(result.nickname()).isEqualTo("nickname");
    }
}