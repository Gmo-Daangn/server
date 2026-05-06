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
import static org.mockito.ArgumentMatchers.any;
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

    @Test
    @DisplayName("이메일이 존재하면 isEmailDuplicated()는 true를 반환한다.")
    public void isEmailDuplicated_existingEmail_returnsTrue() {
        //given
        given(memberRepository.existsByEmail("test@test.com")).willReturn(true);
        //when
        Boolean result = memberService.isEmailDuplicated("test@test.com");
        //then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("이메일이 존재하지 않으면 isEmailDuplicated()는 false를 반환한다.")
    public void isEmailDuplicated_nonExistentEmail_returnsFalse() {
        //given
        given(memberRepository.existsByEmail("new@test.com")).willReturn(false);
        //when
        Boolean result = memberService.isEmailDuplicated("new@test.com");
        //then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("register()는 repository.save()가 반환한 Member를 그대로 반환한다.")
    public void register_savesAndReturnsMember() {
        //given
        Member member = Member.builder().id(1L).email("test@test.com").build();
        given(memberRepository.save(any(Member.class))).willReturn(member);
        //when
        Member saved = memberService.register(member);
        //then
        assertThat(saved.getId()).isEqualTo(1L);
        assertThat(saved.getEmail()).isEqualTo("test@test.com");
    }

    @Test
    @DisplayName("getByEmail()은 이메일로 Member를 찾으면 Optional.of(member)를 반환한다.")
    public void getByEmail_existingEmail_returnsMember() {
        //given
        Member member = Member.builder().id(2L).email("find@test.com").build();
        given(memberRepository.findByEmail("find@test.com")).willReturn(Optional.of(member));
        //when
        Optional<Member> result = memberService.getByEmail("find@test.com");
        //then
        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("find@test.com");
    }

    @Test
    @DisplayName("getByEmail()은 이메일이 없으면 Optional.empty()를 반환한다.")
    public void getByEmail_nonExistentEmail_returnsEmpty() {
        //given
        given(memberRepository.findByEmail("none@test.com")).willReturn(Optional.empty());
        //when
        Optional<Member> result = memberService.getByEmail("none@test.com");
        //then
        assertThat(result).isEmpty();
    }
}