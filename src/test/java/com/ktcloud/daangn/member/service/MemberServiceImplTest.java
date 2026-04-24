package com.ktcloud.daangn.member.service;

import com.ktcloud.daangn.config.TestContainerConfig;
import com.ktcloud.daangn.member.dto.MemberSignupRequestDto;
import com.ktcloud.daangn.member.entity.Member;
import com.ktcloud.daangn.member.repository.MemberDBRepositoryImpl;
import com.ktcloud.daangn.member.repository.MemberRepository;
import com.oracle.svm.core.annotate.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;


@SpringBootTest
@Import(TestContainerConfig.class)
@ActiveProfiles("test")
class MemberServiceImplTest {

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    MemberServiceImpl memberService;

    @Test
    @DisplayName("회원가입이 정상적으로 작동한다.")
    void signup() {
        MemberSignupRequestDto dto = new MemberSignupRequestDto(
                "test@test.com", "nickname", "password", "서울시"
        );

        String message = memberService.signup(dto);

        assertThat(message).contains("회원가입 성공");
    }
}