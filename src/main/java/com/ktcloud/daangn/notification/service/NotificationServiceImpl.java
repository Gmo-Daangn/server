package com.ktcloud.daangn.notification.service;

import com.ktcloud.daangn.member.repository.MemberRepository;
import com.ktcloud.daangn.notification.dto.NotificationResponseDto;
import com.ktcloud.daangn.notification.entity.Notification;
import com.ktcloud.daangn.notification.entity.NotificationTemplate;
import com.ktcloud.daangn.notification.event.NotificationEvent;
import com.ktcloud.daangn.notification.repository.EmitterRepository;
import com.ktcloud.daangn.notification.repository.NotificationRepository;
import com.ktcloud.daangn.notification.repository.NotificationTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private static final Long DEFAULT_TIMEOUT = 60L * 1000 * 60;
    private final EmitterRepository emitterRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationTemplateRepository templateRepository;
    private final MemberRepository memberRepository;

    // SSE 구독 요청 처리
    @Override
    public SseEmitter subscribe(Long memberId) {
        requireMember(memberId);
        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);
        emitterRepository.save(memberId, emitter);

        emitter.onCompletion(() -> emitterRepository.deleteById(memberId));
        emitter.onTimeout(() -> emitterRepository.deleteById(memberId));

        sendToClient(memberId, "SSE 연결 성공 [memberId=" + memberId + "]");

        return emitter;
    }

    // 알림 생성 및 전송
    @Override
    @Transactional
    public void createAndSendNotification(NotificationEvent event) {
        requireMember(event.memberId());
        NotificationTemplate template = templateRepository.findByTemplateType(event.templateType())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 알림 템플릿입니다: " + event.templateType()));

        String finalMessage = template.getTemplateText().replace("{dynamic}", event.templateText());

        Notification notification = Notification.builder()
                .memberId(event.memberId())
                .template(template)
                .message(finalMessage)
                .build();

        notificationRepository.save(notification);

        sendToClient(event.memberId(), finalMessage);
    }

    private void sendToClient(Long memberId, Object data) {
        SseEmitter emitter = emitterRepository.get(memberId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .id(String.valueOf(System.currentTimeMillis()))
                        .name("notification")
                        .data(data));
            } catch (IOException exception) {
                emitterRepository.deleteById(memberId);
                log.error("SSE 전송 실패로 인한 연결 삭제: {}", memberId);
            }
        }
    }

    // 알림 목록 조회
    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponseDto> getNotifications(Long memberId) {
        requireMember(memberId);
        return notificationRepository.findActiveByMemberId(memberId)
                .stream()
                .map(NotificationResponseDto::from)
                .collect(Collectors.toList());
    }

    // 알림 삭제
    @Override
    @Transactional
    public String deleteNotification(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 알림입니다."));
        notification.delete();
        return "알림 삭제 성공";
    }

    // 알림 읽음 처리
    @Override
    @Transactional
    public String readNotification(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 알림입니다."));
        notification.markAsRead();
        return "알림 읽음 처리 성공";
    }

    private void requireMember(Long memberId) {
        memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다: " + memberId));
    }
}
