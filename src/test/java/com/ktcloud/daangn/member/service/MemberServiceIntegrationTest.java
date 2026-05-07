package com.ktcloud.daangn.member.service;

import com.ktcloud.daangn.config.TestContainerConfig;
import com.ktcloud.daangn.config.valueObject.Address;
import com.ktcloud.daangn.member.dto.MemberInfoResponseDto;
import com.ktcloud.daangn.member.entity.Member;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(TestContainerConfig.class)
@ActiveProfiles("test")
@Transactional
public class MemberServiceIntegrationTest {

    @Autowired
    private MemberService memberService;

    @Autowired
    private EntityManager em;

    private final Address address = new Address("서울시", "동작구", "사당동");

    @Test
    @DisplayName("[HAPPY] 내 정보 조회시 성공적으로 반환한다.")
    public void getMyInfo_ValidMember_Success(){
        //given
        Member member = Member.builder()
                .email("test@test.com")
                .nickName("이름")
                .address(address)
                .build();

        em.persist(member);
        Long memberId = member.getId();
        em.flush();
        em.clear();
        //when
        MemberInfoResponseDto result = memberService.getMyInfo(memberId);
        //then
        assertThat(result.email()).isEqualTo("test@test.com");
        assertThat(result.nickname()).isEqualTo("이름");
        assertThat(result.address()).isEqualTo(new Address("서울시", "동작구", "사당동"));
    }
}
