package com.ktcloud.daangn.notification.service;

import com.ktcloud.daangn.member.entity.Member;
import com.ktcloud.daangn.member.repository.MemberRepository;
import com.ktcloud.daangn.notification.dto.NotificationResponseDto;
import com.ktcloud.daangn.notification.entity.Notification;
import com.ktcloud.daangn.notification.entity.NotificationTemplate;
import com.ktcloud.daangn.notification.event.NotificationEvent;
import com.ktcloud.daangn.notification.repository.EmitterRepository;
import com.ktcloud.daangn.notification.repository.NotificationRepository;
import com.ktcloud.daangn.notification.repository.NotificationTemplateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    static final Long MEMBER_ID = 1L;

    @Mock
    EmitterRepository emitterRepository;
    @Mock
    NotificationRepository notificationRepository;
    @Mock
    NotificationTemplateRepository templateRepository;
    @Mock
    MemberRepository memberRepository;

    @InjectMocks
    NotificationServiceImpl notificationService;

    Member existingMember;

    @BeforeEach
    void setUp() {
        existingMember = Member.builder()
                .id(MEMBER_ID)
                .email("a@b.com")
                .build();
    }

    private void givenExistingMember(Long receiverId) {
        given(memberRepository.findById(receiverId)).willReturn(Optional.of(existingMember));
    }

    private NotificationTemplate buildTemplate(String type, String text) {
        return NotificationTemplate.builder()
                .templateType(type)
                .templateTitle("제목")
                .templateText(text)
                .identifier(10L)
                .build();
    }

    private NotificationTemplate stubTemplateRepository(String type, String text) {
        NotificationTemplate template = buildTemplate(type, text);
        given(templateRepository.findByTemplateType(type)).willReturn(Optional.of(template));
        return template;
    }

    @Nested
    @DisplayName("subscribe (SSE 구독)")
    class Subscribe {

        @Test
        @DisplayName("[HAPPY] 회원이 존재하면 SseEmitter를 저장하고 반환한다")
        void subscribe_memberExists_returnsEmitterAndSaves() {
            givenExistingMember(MEMBER_ID);

            SseEmitter result = notificationService.subscribe(MEMBER_ID);

            assertThat(result).isNotNull();
            verify(emitterRepository).save(eq(MEMBER_ID), any(SseEmitter.class));
        }

        @Test
        @DisplayName("[Exception] 존재하지 않는 회원이면 예외가 발생한다")
        void subscribe_memberNotFound_throws() {
            given(memberRepository.findById(MEMBER_ID)).willReturn(Optional.empty());

            assertThatThrownBy(() -> notificationService.subscribe(MEMBER_ID))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("존재하지 않는 회원입니다");

            verifyNoInteractions(emitterRepository);
        }
    }

    @Nested
    @DisplayName("createAndSendNotification")
    class CreateAndSendNotification {

        @Test
        @DisplayName("[HAPPY] 템플릿 치환 후 저장하고 클라이언트로 전송을 시도한다")
        void create_validEvent_savesWithFinalMessage() {
            givenExistingMember(MEMBER_ID);
            NotificationTemplate template = stubTemplateRepository("ORDER", "안녕 {templateText}");
            Notification saved = Notification.builder()
                    .receiver(existingMember)
                    .template(template)
                    .message("안녕 주문완료")
                    .build();
            given(notificationRepository.save(any(Notification.class))).willReturn(saved);

            NotificationEvent event = new NotificationEvent(MEMBER_ID, "ORDER", 99L, "주문완료");
            notificationService.createAndSendNotification(event);

            ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
            verify(notificationRepository).save(captor.capture());
            Notification captured = captor.getValue();
            assertThat(captured.getReceiver().getId()).isEqualTo(MEMBER_ID);
            assertThat(captured.getTemplate()).isEqualTo(template);
            assertThat(captured.getMessage()).isEqualTo("안녕 주문완료");
            verify(emitterRepository).get(MEMBER_ID);
        }

        @Test
        @DisplayName("[Exception] 회원이 없으면 저장하지 않는다")
        void create_memberMissing_throwsAndDoesNotSave() {
            given(memberRepository.findById(MEMBER_ID)).willReturn(Optional.empty());

            NotificationEvent event = new NotificationEvent(MEMBER_ID, "ORDER", 1L, "x");
            assertThatThrownBy(() -> notificationService.createAndSendNotification(event))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("존재하지 않는 회원");

            verifyNoInteractions(notificationRepository, templateRepository, emitterRepository);
        }

        @Test
        @DisplayName("[Exception] 등록된 템플릿 타입이 없으면 예외가 발생한다")
        void create_unknownTemplate_throws() {
            givenExistingMember(MEMBER_ID);
            given(templateRepository.findByTemplateType("UNKNOWN")).willReturn(Optional.empty());

            NotificationEvent event = new NotificationEvent(MEMBER_ID, "UNKNOWN", 1L, "텍스트");
            assertThatThrownBy(() -> notificationService.createAndSendNotification(event))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("존재하지 않는 알림 템플릿");
        }
    }

    @Nested
    @DisplayName("getNotifications")
    class GetNotifications {

        @Test
        @DisplayName("[HAPPY] 활성 알림 목록을 DTO로 반환한다")
        void get_listMappedToDto() {
            givenExistingMember(MEMBER_ID);
            NotificationTemplate template = buildTemplate("TYPE_A", "{templateText}");
            LocalDateTime created = LocalDateTime.of(2026, 1, 1, 12, 0);
            Notification notification = Notification.builder()
                    .receiver(existingMember)
                    .template(template)
                    .message("msg")
                    .build();
            ReflectionTestUtils.setField(notification, "id", 100L);
            ReflectionTestUtils.setField(notification, "createdAt", created);

            given(notificationRepository.findActiveByReceiverId(MEMBER_ID)).willReturn(List.of(notification));

            List<NotificationResponseDto> result = notificationService.getNotifications(MEMBER_ID);

            assertThat(result).hasSize(1);
            NotificationResponseDto dto = result.getFirst();
            assertThat(dto.id()).isEqualTo(100L);
            assertThat(dto.receiverId()).isEqualTo(MEMBER_ID);
            assertThat(dto.templateType()).isEqualTo("TYPE_A");
            assertThat(dto.message()).isEqualTo("msg");
            assertThat(dto.isRead()).isFalse();
            assertThat(dto.createdAt()).isEqualTo(created);
        }

        @Test
        @DisplayName("[Exception] 회원이 없으면 예외가 발생한다")
        void get_memberMissing_throws() {
            given(memberRepository.findById(MEMBER_ID)).willReturn(Optional.empty());

            assertThatThrownBy(() -> notificationService.getNotifications(MEMBER_ID))
                    .isInstanceOf(IllegalArgumentException.class);

            verifyNoInteractions(notificationRepository);
        }
    }

    private Notification storedNotification(Long id, NotificationTemplate template) {
        Notification n = Notification.builder()
                .receiver(existingMember)
                .template(template)
                .message("m")
                .build();
        ReflectionTestUtils.setField(n, "id", id);
        return n;
    }

    @Nested
    @DisplayName("deleteNotification")
    class DeleteNotification {

        @Test
        @DisplayName("[HAPPY] 존재하는 알림이면 논리 삭제 처리한다")
        void delete_existing_softDeletes() {
            NotificationTemplate t = buildTemplate("X", "{templateText}");
            Notification notification = storedNotification(5L, t);
            given(notificationRepository.findById(5L)).willReturn(Optional.of(notification));

            String message = notificationService.deleteNotification(5L);

            assertThat(message).isEqualTo("알림 삭제 성공");
            assertThat(notification.isDeleted()).isTrue();
        }

        @Test
        @DisplayName("[Exception] 알림이 없으면 예외가 발생한다")
        void delete_missing_throws() {
            given(notificationRepository.findById(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> notificationService.deleteNotification(99L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("존재하지 않는 알림입니다.");
        }
    }

    @Nested
    @DisplayName("readNotification")
    class ReadNotification {

        @Test
        @DisplayName("[HAPPY] 존재하는 알림이면 읽음 처리한다")
        void read_existing_marksRead() {
            NotificationTemplate t = buildTemplate("Y", "{templateText}");
            Notification notification = storedNotification(7L, t);
            given(notificationRepository.findById(7L)).willReturn(Optional.of(notification));

            String message = notificationService.readNotification(7L);

            assertThat(message).isEqualTo("알림 읽음 처리 성공");
            assertThat(notification.isRead()).isTrue();
        }

        @Test
        @DisplayName("[Exception] 알림이 없으면 예외가 발생한다")
        void read_missing_throws() {
            given(notificationRepository.findById(88L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> notificationService.readNotification(88L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("존재하지 않는 알림입니다.");
        }
    }
}
