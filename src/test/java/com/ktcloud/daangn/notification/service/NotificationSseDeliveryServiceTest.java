package com.ktcloud.daangn.notification.service;

import com.ktcloud.daangn.notification.repository.EmitterRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationSseDeliveryServiceTest {

    static final Long MEMBER_ID = 1L;

    @Mock
    EmitterRepository emitterRepository;

    @InjectMocks
    NotificationSseDeliveryService deliveryService;

    @Test
    @DisplayName("[HAPPY] 활성화된 SSE 연결이 존재하면 클라이언트로 이벤트를 정상 전송한다")
    void deliver_activeEmitter_sendsSseSuccessfully() throws IOException {
        SseEmitter mockEmitter = mock(SseEmitter.class);
        given(emitterRepository.get(MEMBER_ID)).willReturn(mockEmitter);

        deliveryService.deliver(MEMBER_ID, "알림 메시지");

        verify(mockEmitter).send(any(SseEmitter.SseEventBuilder.class));
    }

    @Test
    @DisplayName("[HAPPY] SSE 연결이 없으면 전송을 시도하지 않는다")
    void deliver_missingEmitter_doesNothing() {
        given(emitterRepository.get(MEMBER_ID)).willReturn(null);

        deliveryService.deliver(MEMBER_ID, "알림 메시지");

        verify(emitterRepository).get(MEMBER_ID);
    }

    @Test
    @DisplayName("[Exception] SSE 전송 중 IOException 발생 시 emitter를 저장소에서 삭제한다")
    void deliver_ioException_deletesEmitter() throws IOException {
        SseEmitter mockEmitter = mock(SseEmitter.class);
        given(emitterRepository.get(MEMBER_ID)).willReturn(mockEmitter);
        doThrow(new IOException("클라이언트 연결 끊김")).when(mockEmitter).send(any(SseEmitter.SseEventBuilder.class));

        deliveryService.deliver(MEMBER_ID, "알림 메시지");

        verify(emitterRepository).deleteById(MEMBER_ID);
    }
}
