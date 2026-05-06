package com.ktcloud.daangn.notification.entity;

import com.ktcloud.daangn.member.entity.Member;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationEntityTest {

    private Member buildMember() {
        return Member.builder().id(1L).email("user@test.com").build();
    }

    private NotificationTemplate buildTemplate() {
        return NotificationTemplate.builder()
                .templateType("ORDER")
                .templateTitle("주문 알림")
                .templateText("{templateText}")
                .identifier(10L)
                .build();
    }

    @Test
    @DisplayName("빌더로 생성한 Notification은 기본적으로 읽지 않은 상태이다")
    void builder_defaultIsReadFalse() {
        Notification notification = Notification.builder()
                .receiver(buildMember())
                .template(buildTemplate())
                .message("주문이 완료됐습니다.")
                .build();

        assertThat(notification.isRead()).isFalse();
    }

    @Test
    @DisplayName("빌더로 생성한 Notification은 기본적으로 삭제되지 않은 상태이다")
    void builder_defaultIsDeletedFalse() {
        Notification notification = Notification.builder()
                .receiver(buildMember())
                .template(buildTemplate())
                .message("알림 내용")
                .build();

        assertThat(notification.isDeleted()).isFalse();
    }

    @Test
    @DisplayName("markAsRead() 호출 후 isRead()는 true를 반환한다")
    void markAsRead_setsIsReadTrue() {
        Notification notification = Notification.builder()
                .receiver(buildMember())
                .template(buildTemplate())
                .message("읽음 처리 대상")
                .build();

        notification.markAsRead();

        assertThat(notification.isRead()).isTrue();
    }

    @Test
    @DisplayName("delete() 호출 후 isDeleted()는 true를 반환한다")
    void delete_setsIsDeletedTrue() {
        Notification notification = Notification.builder()
                .receiver(buildMember())
                .template(buildTemplate())
                .message("삭제 처리 대상")
                .build();

        notification.delete();

        assertThat(notification.isDeleted()).isTrue();
    }

    @Test
    @DisplayName("delete()를 호출해도 isRead()는 변경되지 않는다")
    void delete_doesNotAffectIsRead() {
        Notification notification = Notification.builder()
                .receiver(buildMember())
                .template(buildTemplate())
                .message("내용")
                .build();

        notification.delete();

        assertThat(notification.isRead()).isFalse();
    }

    @Test
    @DisplayName("markAsRead()를 호출해도 isDeleted()는 변경되지 않는다")
    void markAsRead_doesNotAffectIsDeleted() {
        Notification notification = Notification.builder()
                .receiver(buildMember())
                .template(buildTemplate())
                .message("내용")
                .build();

        notification.markAsRead();

        assertThat(notification.isDeleted()).isFalse();
    }

    @Test
    @DisplayName("빌더로 생성 시 receiver, template, message가 올바르게 설정된다")
    void builder_fieldsSetCorrectly() {
        Member member = buildMember();
        NotificationTemplate template = buildTemplate();
        String message = "테스트 메시지";

        Notification notification = Notification.builder()
                .receiver(member)
                .template(template)
                .message(message)
                .build();

        assertThat(notification.getReceiver()).isSameAs(member);
        assertThat(notification.getTemplate()).isSameAs(template);
        assertThat(notification.getMessage()).isEqualTo(message);
    }

    @Test
    @DisplayName("markAsRead()와 delete()를 모두 호출하면 두 상태 모두 true이다")
    void markAsReadAndDelete_bothTrue() {
        Notification notification = Notification.builder()
                .receiver(buildMember())
                .template(buildTemplate())
                .message("내용")
                .build();

        notification.markAsRead();
        notification.delete();

        assertThat(notification.isRead()).isTrue();
        assertThat(notification.isDeleted()).isTrue();
    }
}