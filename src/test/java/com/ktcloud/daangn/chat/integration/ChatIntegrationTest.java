package com.ktcloud.daangn.chat.integration;

import com.jayway.jsonpath.JsonPath;
import com.ktcloud.daangn.chat.dto.ChatMessageResponseDto;
import com.ktcloud.daangn.chat.dto.ChatMessageWriteRequestDto;
import com.ktcloud.daangn.config.TestContainerConfig;
import com.ktcloud.daangn.member.entity.Member;
import com.ktcloud.daangn.member.entity.MemberRole;
import com.ktcloud.daangn.member.entity.ProviderToken;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.messaging.converter.ByteArrayMessageConverter;
import org.springframework.messaging.converter.CompositeMessageConverter;
import org.springframework.messaging.converter.JacksonJsonMessageConverter;
import org.springframework.messaging.converter.StringMessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Import(TestContainerConfig.class)
@ActiveProfiles("test")
class ChatIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @LocalServerPort
    private int port;

    private static Long readLong(String json, String expression) {
        Number number = JsonPath.read(json, expression);
        return number.longValue();
    }

    private static ChatMessageResponseDto parseMessage(byte[] payload) {
        String json = new String(payload, StandardCharsets.UTF_8);

        // STOMP 클라이언트 컨버터 차이에 테스트가 흔들리지 않도록 body를 직접 파싱
        return new ChatMessageResponseDto(
                readLong(json, "$.messageId"),
                readLong(json, "$.roomId"),
                readLong(json, "$.senderId"),
                JsonPath.read(json, "$.message"),
                JsonPath.read(json, "$.edited"),
                JsonPath.read(json, "$.deleted"),
                readLong(json, "$.unreadCount"),
                LocalDateTime.parse(JsonPath.read(json, "$.createdAt"))
        );
    }

    @Test
    @DisplayName("[Integration] 채팅방 입장, 메시지 전송, 기존 메시지 조회, 읽음, 수정, 삭제를 처리한다.")
    void chatFlow_entersRoomAndManagesMessages() throws Exception {
        TestMembers members = saveMembers();

        // 회원 가입 API를 통하지 않고 채팅에 필요한 회원만 준비
        MvcResult enterResult = enterRoom(members, 100L)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.Data.created").value(true))
                .andReturn();
        Long roomId = readLong(enterResult, "$.Data.roomId");

        // HTTP API는 MockMvc로 빠르게 검증하고 메시지 전송 및 브로드캐스트는 실제 STOMP 연결로 확인
        WebSocketStompClient stompClient = new WebSocketStompClient(new StandardWebSocketClient());
        stompClient.setMessageConverter(new CompositeMessageConverter(List.of(
                new ByteArrayMessageConverter(),
                new StringMessageConverter(),
                new JacksonJsonMessageConverter()
        )));

        StompSession stompSession = stompClient
                .connectAsync("ws://localhost:" + port + "/api/ws-stomp", new StompSessionHandlerAdapter() {
                })
                .get(3, TimeUnit.SECONDS);

        BlockingQueue<ChatMessageResponseDto> messageEvents = new LinkedBlockingQueue<>();
        stompSession.subscribe("/sub/chat/rooms/" + roomId + "/messages", new ChatMessageFrameHandler(messageEvents));
        // 구독 프레임이 서버 브로커에 반영되기 전에 send가 먼저 도착하면 이벤트를 놓칠 수 있어 짧게 대기
        TimeUnit.MILLISECONDS.sleep(500);

        try {
            stompSession.send(
                    "/pub/chat/rooms/" + roomId + "/messages",
                    new ChatMessageWriteRequestDto(members.senderId(), "hello integration")
            );

            ChatMessageResponseDto sentMessage = pollMessage(messageEvents);
            assertThat(sentMessage.roomId()).isEqualTo(roomId);
            assertThat(sentMessage.senderId()).isEqualTo(members.senderId());
            assertThat(sentMessage.message()).isEqualTo("hello integration");
            assertThat(sentMessage.unreadCount()).isEqualTo(1);

            mockMvc.perform(get("/api/v1/chat/messages/{roomId}", roomId)
                            .param("memberId", members.senderId().toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.Data[0].messageId").value(sentMessage.messageId()))
                    .andExpect(jsonPath("$.Data[0].message").value("hello integration"));

            enterRoom(members, 100L)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.Data.roomId").value(roomId))
                    .andExpect(jsonPath("$.Data.created").value(false));

            mockMvc.perform(post("/api/v1/chat/rooms/read/{roomId}", roomId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "memberId": %d
                                    }
                                    """.formatted(members.receiverId())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.Data.readMessageCount").value(1));

            mockMvc.perform(get("/api/v1/chat/messages/{roomId}", roomId)
                            .param("memberId", members.receiverId().toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.Data[0].unreadCount").value(0));

            mockMvc.perform(patch("/api/v1/chat/messages/{messageId}", sentMessage.messageId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "memberId": %d,
                                      "message": "edited integration"
                                    }
                                    """.formatted(members.senderId())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.Data.message").value("edited integration"))
                    .andExpect(jsonPath("$.Data.edited").value(true));

            ChatMessageResponseDto editedMessage = pollMessage(messageEvents);
            assertThat(editedMessage.messageId()).isEqualTo(sentMessage.messageId());
            assertThat(editedMessage.message()).isEqualTo("edited integration");
            assertThat(editedMessage.edited()).isTrue();

            mockMvc.perform(delete("/api/v1/chat/messages/{messageId}", sentMessage.messageId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "memberId": %d
                                    }
                                    """.formatted(members.senderId())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.Data.deleted").value(true))
                    .andExpect(jsonPath("$.Data.message").value("삭제된 메시지입니다."));

            ChatMessageResponseDto deletedMessage = pollMessage(messageEvents);
            assertThat(deletedMessage.messageId()).isEqualTo(sentMessage.messageId());
            assertThat(deletedMessage.deleted()).isTrue();
            assertThat(deletedMessage.message()).isEqualTo("삭제된 메시지입니다.");
        } finally {
            stompSession.disconnect();
            stompClient.stop();
        }
    }

    private TestMembers saveMembers() {
        String suffix = UUID.randomUUID().toString().replace("-", "");
        return transactionTemplate.execute(_ -> {
            Member sender = member("chat-it-sender-" + suffix + "@test.com", "sender");
            Member receiver = member("chat-it-receiver-" + suffix + "@test.com", "receiver");
            entityManager.persist(sender);
            entityManager.persist(receiver);
            return new TestMembers(sender.getId(), receiver.getId());
        });
    }

    private Member member(String email, String nickname) {
        return Member.builder()
                .email(email)
                .password("password")
                .nickName(nickname)
                .memberRole(MemberRole.MEMBER)
                .providerToken(ProviderToken.LOCAL)
                .createAt(LocalDateTime.now())
                .build();
    }

    private org.springframework.test.web.servlet.ResultActions enterRoom(TestMembers members, Long productId) throws Exception {
        return mockMvc.perform(post("/api/v1/chat/rooms/enter")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "memberId": %d,
                          "targetMemberId": %d,
                          "productId": %d
                        }
                        """.formatted(members.senderId(), members.receiverId(), productId)));
    }

    private Long readLong(MvcResult result, String expression) throws Exception {
        Number number = JsonPath.read(result.getResponse().getContentAsString(), expression);
        return number.longValue();
    }

    private ChatMessageResponseDto pollMessage(BlockingQueue<ChatMessageResponseDto> messageEvents) throws InterruptedException {
        // WebSocket 이벤트는 비동기로 도착, 큐에서 기다린 뒤 검증
        ChatMessageResponseDto message = messageEvents.poll(10, TimeUnit.SECONDS);
        assertThat(message).isNotNull();
        return message;
    }

    private record TestMembers(
            Long senderId,
            Long receiverId
    ) {
    }

    private static class ChatMessageFrameHandler implements StompFrameHandler {

        private final BlockingQueue<ChatMessageResponseDto> messageEvents;

        private ChatMessageFrameHandler(BlockingQueue<ChatMessageResponseDto> messageEvents) {
            this.messageEvents = messageEvents;
        }

        @Override
        public Type getPayloadType(StompHeaders headers) {
            return byte[].class;
        }

        @Override
        public void handleFrame(StompHeaders headers, Object payload) {
            messageEvents.offer(parseMessage((byte[]) payload));
        }
    }
}
