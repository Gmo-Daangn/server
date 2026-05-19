package com.ktcloud.daangn.notification.service;

import com.ktcloud.daangn.config.TestContainerConfig;
import com.ktcloud.daangn.common.exception.InvalidInputException;
import com.ktcloud.daangn.common.valueObject.Address;
import com.ktcloud.daangn.member.entity.Member;
import com.ktcloud.daangn.member.entity.MemberRole;
import com.ktcloud.daangn.member.entity.ProviderToken;
import com.ktcloud.daangn.member.service.MemberService;
import com.ktcloud.daangn.notification.dto.NotificationResponseDto;
import com.ktcloud.daangn.notification.entity.NotificationTemplate;
import com.ktcloud.daangn.notification.event.NotificationEvent;
import com.ktcloud.daangn.notification.repository.NotificationTemplateRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Import(TestContainerConfig.class)
@ActiveProfiles("test")
@Transactional
class NotificationServiceIntegrationTest {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private NotificationTemplateRepository templateRepository;

    private Member createMember(String email) {
        return memberService.register(Member.builder()
                .email(email)
                .password("password")
                .nickName("test")
                .address(new Address("서울", "강남구", "청담동"))
                .memberRole(MemberRole.MEMBER)
                .providerToken(ProviderToken.LOCAL)
                .build());
    }

    private NotificationTemplate createTemplate(String type, String text) {
        return templateRepository.save(NotificationTemplate.builder()
                .templateType(type)
                .templateTitle("알림 제목")
                .templateText(text)
                .identifier(101L)
                .build());
    }

    @Nested
    @DisplayName("createAndSendNotification + getNotifications")
    class CreateAndGet {

        @Test
        @DisplayName("[HAPPY] 알림 생성 후 조회 시 DTO로 반환된다")
        void createThenGet_success() {
            Member member = createMember("test@test.com");
            createTemplate("ORDER", "맥북 {templateText}");

            notificationService.createAndSendNotification(
                    new NotificationEvent(member.getId(), "ORDER", 99L, "주문완료")
            );

            List<NotificationResponseDto> result = notificationService.getNotifications(member.getId());

            assertThat(result).hasSize(1);
            assertThat(result.getFirst())
                    .extracting("receiverId", "templateType", "message", "isRead")
                    .containsExactly(member.getId(), "ORDER", "맥북 주문완료", false);
        }

        @Test
        @DisplayName("[Exception] 존재하지 않는 회원이면 예외가 발생한다")
        void create_memberMissing_throws() {
            createTemplate("ORDER", "맥북 {templateText}");

            assertThatThrownBy(() -> notificationService.createAndSendNotification(
                    new NotificationEvent(999999L, "ORDER", 1L, "x")
            ))
                    .isInstanceOf(InvalidInputException.class)
                    .hasMessageContaining("존재하지 않는 ID");
        }

        @Test
        @DisplayName("[Exception] 템플릿 타입이 없으면 예외가 발생한다")
        void create_unknownTemplate_throws() {
            Member member = createMember("noti-template@test.com");

            assertThatThrownBy(() -> notificationService.createAndSendNotification(
                    new NotificationEvent(member.getId(), "UNKNOWN", 1L, "x")
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("존재하지 않는 알림 템플릿");
        }
    }

    @Nested
    @DisplayName("readNotification / deleteNotification")
    class ReadAndDelete {

        @Test
        @DisplayName("[HAPPY] 읽음 처리 후 조회하면 isRead가 true다")
        void read_success() {
            Member member = createMember("noti-read@test.com");
            createTemplate("CHAT", "메시지 {templateText}");
            notificationService.createAndSendNotification(
                    new NotificationEvent(member.getId(), "CHAT", 10L, "도착")
            );

            Long notificationId = notificationService.getNotifications(member.getId()).getFirst().id();

            String message = notificationService.readNotification(notificationId);
            List<NotificationResponseDto> result = notificationService.getNotifications(member.getId());

            assertThat(message).isEqualTo("알림 읽음 처리 성공");
            assertThat(result).hasSize(1);
            assertThat(result.getFirst().isRead()).isTrue();
        }

        @Test
        @DisplayName("[HAPPY] 삭제 처리 후 활성 목록에서 제외된다")
        void delete_success() {
            Member member = createMember("noti-delete@test.com");
            createTemplate("ORDER", "거래 {templateText}");
            notificationService.createAndSendNotification(
                    new NotificationEvent(member.getId(), "ORDER", 33L, "완료")
            );

            Long notificationId = notificationService.getNotifications(member.getId()).getFirst().id();

            String message = notificationService.deleteNotification(notificationId);
            List<NotificationResponseDto> result = notificationService.getNotifications(member.getId());

            assertThat(message).isEqualTo("알림 삭제 성공");
            assertThat(result).isEmpty();
        }
    }
}
