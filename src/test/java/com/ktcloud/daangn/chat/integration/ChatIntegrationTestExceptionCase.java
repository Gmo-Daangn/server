package com.ktcloud.daangn.chat.integration;

import com.ktcloud.daangn.chat.dto.ChatMessageRequestDto;
import com.ktcloud.daangn.chat.dto.ChatMessageResponseDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.messaging.simp.stomp.StompSession;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@ExtendWith(RestDocumentationExtension.class)
class ChatIntegrationTestExceptionCase extends ChatIntegrationTestSupport {

    @Nested
    @DisplayName("STOMP")
    class Stomp {

        @Test
        @DisplayName("[Integration] 참여자가 아닌 회원은 채팅방 WebSocket 메시지를 구독할 수 없다.")
        void subscribe_deniesNonParticipant() throws Exception {
            TestMembers members = saveMembers();
            Long outsiderId = saveMember("outsider");
            MvcResult enterResult = enterRoom(members, 100L)
                    .andExpect(status().isOk())
                    .andReturn();
            Long roomId = readLong(enterResult, "$.data.roomId");

            WebSocketStompClient stompClient = createStompClient();
            StompSession outsiderSession = connectStompSession(stompClient, outsiderId);
            StompSession senderSession = connectStompSession(stompClient, members.senderId());
            BlockingQueue<ChatMessageResponseDto> outsiderEvents = new LinkedBlockingQueue<>();
            BlockingQueue<ChatMessageResponseDto> senderEvents = new LinkedBlockingQueue<>();

            try {
                outsiderSession.subscribe("/sub/chat/rooms/" + roomId + "/messages", new ChatMessageFrameHandler(outsiderEvents));
                senderSession.subscribe("/sub/chat/rooms/" + roomId + "/messages", new ChatMessageFrameHandler(senderEvents));
                TimeUnit.MILLISECONDS.sleep(500);

                senderSession.send(
                        "/pub/chat/rooms/" + roomId + "/messages",
                        new ChatMessageRequestDto("secret message")
                );

                ChatMessageResponseDto senderMessage = pollMessage(senderEvents);
                assertThat(senderMessage.message()).isEqualTo("secret message");
                assertThat(outsiderEvents.poll(1, TimeUnit.SECONDS)).isNull();
            } finally {
                disconnectIfConnected(outsiderSession);
                disconnectIfConnected(senderSession);
                stompClient.stop();
            }
        }
    }
}
