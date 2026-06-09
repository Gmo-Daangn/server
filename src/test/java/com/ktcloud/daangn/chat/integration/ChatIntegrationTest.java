package com.ktcloud.daangn.chat.integration;

import com.jayway.jsonpath.JsonPath;
import com.ktcloud.daangn.auth.dto.CustomUser;
import com.ktcloud.daangn.auth.jwt.JwtTokenProvider;
import com.ktcloud.daangn.chat.dto.ChatMessageRequestDto;
import com.ktcloud.daangn.chat.dto.ChatMessageResponseDto;
import com.ktcloud.daangn.config.TestContainerConfig;
import com.ktcloud.daangn.member.entity.Member;
import com.ktcloud.daangn.member.entity.MemberRole;
import com.ktcloud.daangn.member.entity.ProviderToken;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.messaging.converter.ByteArrayMessageConverter;
import org.springframework.messaging.converter.CompositeMessageConverter;
import org.springframework.messaging.converter.JacksonJsonMessageConverter;
import org.springframework.messaging.converter.StringMessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.socket.WebSocketHttpHeaders;
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
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@ExtendWith(RestDocumentationExtension.class)
class ChatIntegrationTest extends TestContainerConfig {

    private MockMvc mockMvc;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

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

    @BeforeEach
    void setUp(WebApplicationContext context, RestDocumentationContextProvider restDocumentation) {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(documentationConfiguration(restDocumentation))
                .apply(springSecurity())
                .build();
    }

    @Test
    @DisplayName("[Integration] 채팅방 입장, 메시지 전송, 기존 메시지 조회, 읽음, 수정, 삭제를 처리한다.")
    void chatFlow_entersRoomAndManagesMessages() throws Exception {
        TestMembers members = saveMembers();

        // 회원 가입 API를 통하지 않고 채팅에 필요한 회원만 준비
        MvcResult enterResult = enterRoom(members, 100L)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.created").value(true))
                .andDo(document("chat-room-enter-success",
                        requestFields(
                                fieldWithPath("targetMemberId").description("1대1 채팅 상대 회원 ID"),
                                fieldWithPath("productId").description("상품 기반 채팅일 경우 상품 ID")
                        ),
                        responseFields(
                                fieldWithPath("code").description("HTTP 상태 코드"),
                                fieldWithPath("localDateTime").description("응답 시간"),
                                fieldWithPath("message").description("응답 메시지"),
                                fieldWithPath("data.roomId").description("채팅방 ID"),
                                fieldWithPath("data.created").description("새 채팅방 생성 여부"),
                                fieldWithPath("data.message").description("채팅방 입장 결과 메시지")
                        )
                ))
                .andReturn();
        Long roomId = readLong(enterResult, "$.data.roomId");

        // HTTP API는 MockMvc로 빠르게 검증하고 메시지 전송 및 브로드캐스트는 실제 STOMP 연결로 확인
        WebSocketStompClient stompClient = new WebSocketStompClient(new StandardWebSocketClient());
        stompClient.setMessageConverter(new CompositeMessageConverter(List.of(
                new ByteArrayMessageConverter(),
                new StringMessageConverter(),
                new JacksonJsonMessageConverter()
        )));

        StompHeaders connectHeaders = new StompHeaders();
        connectHeaders.add("Authorization", bearerToken(members.senderId()));
        StompSession stompSession = stompClient
                .connectAsync(
                        "ws://localhost:" + port + "/api/ws-stomp",
                        new WebSocketHttpHeaders(),
                        connectHeaders,
                        new StompSessionHandlerAdapter() {
                        }
                )
                .get(3, TimeUnit.SECONDS);

        BlockingQueue<ChatMessageResponseDto> messageEvents = new LinkedBlockingQueue<>();
        stompSession.subscribe("/sub/chat/rooms/" + roomId + "/messages", new ChatMessageFrameHandler(messageEvents));
        // 구독 프레임이 서버 브로커에 반영되기 전에 send가 먼저 도착하면 이벤트를 놓칠 수 있어 짧게 대기
        TimeUnit.MILLISECONDS.sleep(500);

        try {
            stompSession.send(
                    "/pub/chat/rooms/" + roomId + "/messages",
                    new ChatMessageRequestDto("hello integration")
            );

            ChatMessageResponseDto sentMessage = pollMessage(messageEvents);
            assertThat(sentMessage.roomId()).isEqualTo(roomId);
            assertThat(sentMessage.senderId()).isEqualTo(members.senderId());
            assertThat(sentMessage.message()).isEqualTo("hello integration");
            assertThat(sentMessage.unreadCount()).isEqualTo(1);

            mockMvc.perform(get("/api/v1/chat/messages/{roomId}", roomId)
                            .with(authenticatedMember(members.senderId())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data[0].messageId").value(sentMessage.messageId()))
                    .andExpect(jsonPath("$.data[0].message").value("hello integration"))
                    .andDo(document("chat-message-list-success",
                            pathParameters(
                                    parameterWithName("roomId").description("메시지를 조회할 채팅방 ID")
                            ),
                            responseFields(
                                    fieldWithPath("code").description("HTTP 상태 코드"),
                                    fieldWithPath("localDateTime").description("응답 시간"),
                                    fieldWithPath("message").description("응답 메시지"),
                                    fieldWithPath("data[].messageId").description("메시지 ID"),
                                    fieldWithPath("data[].roomId").description("채팅방 ID"),
                                    fieldWithPath("data[].senderId").description("메시지 작성자 회원 ID"),
                                    fieldWithPath("data[].message").description("메시지 내용"),
                                    fieldWithPath("data[].edited").description("메시지 수정 여부"),
                                    fieldWithPath("data[].deleted").description("메시지 삭제 여부"),
                                    fieldWithPath("data[].unreadCount").description("메시지를 아직 읽지 않은 참여자 수"),
                                    fieldWithPath("data[].createdAt").description("메시지 생성 시간")
                            )
                    ));

            mockMvc.perform(get("/api/v1/chat/messages/{roomId}/search", roomId)
                            .with(authenticatedMember(members.senderId()))
                            .param("keyword", "hello"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data[0].messageId").value(sentMessage.messageId()))
                    .andExpect(jsonPath("$.data[0].message").value("hello integration"))
                    .andDo(document("chat-message-search-success",
                            pathParameters(
                                    parameterWithName("roomId").description("메시지를 검색할 채팅방 ID")
                            ),
                            queryParameters(
                                    parameterWithName("keyword").description("검색어"),
                                    parameterWithName("beforeMessageId").optional().description("이 메시지 ID보다 이전 메시지만 조회하는 커서"),
                                    parameterWithName("size").optional().description("검색 결과 개수. 기본값 30, 최대 100")
                            ),
                            responseFields(
                                    fieldWithPath("code").description("HTTP 상태 코드"),
                                    fieldWithPath("localDateTime").description("응답 시간"),
                                    fieldWithPath("message").description("응답 메시지"),
                                    fieldWithPath("data[].messageId").description("메시지 ID"),
                                    fieldWithPath("data[].roomId").description("채팅방 ID"),
                                    fieldWithPath("data[].senderId").description("메시지 작성자 회원 ID"),
                                    fieldWithPath("data[].message").description("검색된 메시지 내용"),
                                    fieldWithPath("data[].edited").description("메시지 수정 여부"),
                                    fieldWithPath("data[].deleted").description("메시지 삭제 여부"),
                                    fieldWithPath("data[].unreadCount").description("메시지를 아직 읽지 않은 참여자 수"),
                                    fieldWithPath("data[].createdAt").description("메시지 생성 시간")
                            )
                    ));

            mockMvc.perform(get("/api/v1/chat/rooms")
                            .with(authenticatedMember(members.senderId())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data[0].roomId").value(roomId))
                    .andDo(document("chat-room-list-success",
                            responseFields(
                                    fieldWithPath("code").description("HTTP 상태 코드"),
                                    fieldWithPath("localDateTime").description("응답 시간"),
                                    fieldWithPath("message").description("응답 메시지"),
                                    fieldWithPath("data[].roomId").description("채팅방 ID"),
                                    fieldWithPath("data[].productId").description("채팅방과 연결된 상품 ID"),
                                    fieldWithPath("data[].otherMemberId").description("상대 회원 ID"),
                                    fieldWithPath("data[].otherMemberNickname").description("상대 회원 닉네임"),
                                    fieldWithPath("data[].lastMessage").description("마지막 메시지 내용"),
                                    fieldWithPath("data[].lastMessageCreatedAt").description("마지막 메시지 생성 시간"),
                                    fieldWithPath("data[].unreadMessageCount").description("채팅방의 미읽음 메시지 수")
                            )
                    ));

            enterRoom(members, 100L)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.roomId").value(roomId))
                    .andExpect(jsonPath("$.data.created").value(false))
                    .andDo(document("chat-room-reenter-success",
                            requestFields(
                                    fieldWithPath("targetMemberId").description("1대1 채팅 상대 회원 ID"),
                                    fieldWithPath("productId").description("상품 기반 채팅일 경우 상품 ID")
                            ),
                            responseFields(
                                    fieldWithPath("code").description("HTTP 상태 코드"),
                                    fieldWithPath("localDateTime").description("응답 시간"),
                                    fieldWithPath("message").description("응답 메시지"),
                                    fieldWithPath("data.roomId").description("기존 채팅방 ID"),
                                    fieldWithPath("data.created").description("새 채팅방 생성 여부"),
                                    fieldWithPath("data.message").description("채팅방 입장 결과 메시지")
                            )
                    ));

            mockMvc.perform(post("/api/v1/chat/rooms/read/{roomId}", roomId)
                            .with(authenticatedMember(members.receiverId())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.readMessageCount").value(1))
                    .andDo(document("chat-room-read-success",
                            pathParameters(
                                    parameterWithName("roomId").description("읽음 처리할 채팅방 ID")
                            ),
                            responseFields(
                                    fieldWithPath("code").description("HTTP 상태 코드"),
                                    fieldWithPath("localDateTime").description("응답 시간"),
                                    fieldWithPath("message").description("응답 메시지"),
                                    fieldWithPath("data.roomId").description("읽음 처리된 채팅방 ID"),
                                    fieldWithPath("data.memberId").description("읽음 처리 요청 회원 ID"),
                                    fieldWithPath("data.readMessageCount").description("읽음 처리된 메시지 수")
                            )
                    ));

            mockMvc.perform(get("/api/v1/chat/messages/{roomId}", roomId)
                            .with(authenticatedMember(members.receiverId())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data[0].unreadCount").value(0));

            mockMvc.perform(patch("/api/v1/chat/messages/{messageId}", sentMessage.messageId())
                            .with(authenticatedMember(members.senderId()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "message": "edited integration"
                                    }
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.message").value("edited integration"))
                    .andExpect(jsonPath("$.data.edited").value(true))
                    .andDo(document("chat-message-edit-success",
                            pathParameters(
                                    parameterWithName("messageId").description("수정할 메시지 ID")
                            ),
                            requestFields(
                                    fieldWithPath("message").description("수정할 메시지 내용")
                            ),
                            responseFields(
                                    fieldWithPath("code").description("HTTP 상태 코드"),
                                    fieldWithPath("localDateTime").description("응답 시간"),
                                    fieldWithPath("message").description("응답 메시지"),
                                    fieldWithPath("data.messageId").description("메시지 ID"),
                                    fieldWithPath("data.roomId").description("채팅방 ID"),
                                    fieldWithPath("data.senderId").description("메시지 작성자 회원 ID"),
                                    fieldWithPath("data.message").description("수정된 메시지 내용"),
                                    fieldWithPath("data.edited").description("메시지 수정 여부"),
                                    fieldWithPath("data.deleted").description("메시지 삭제 여부"),
                                    fieldWithPath("data.unreadCount").description("메시지를 아직 읽지 않은 참여자 수"),
                                    fieldWithPath("data.createdAt").description("메시지 생성 시간")
                            )
                    ));

            ChatMessageResponseDto editedMessage = pollMessage(messageEvents);
            assertThat(editedMessage.messageId()).isEqualTo(sentMessage.messageId());
            assertThat(editedMessage.message()).isEqualTo("edited integration");
            assertThat(editedMessage.edited()).isTrue();

            mockMvc.perform(delete("/api/v1/chat/messages/{messageId}", sentMessage.messageId())
                            .with(authenticatedMember(members.senderId())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.deleted").value(true))
                    .andExpect(jsonPath("$.data.message").value("삭제된 메시지입니다."))
                    .andDo(document("chat-message-delete-success",
                            pathParameters(
                                    parameterWithName("messageId").description("삭제할 메시지 ID")
                            ),
                            responseFields(
                                    fieldWithPath("code").description("HTTP 상태 코드"),
                                    fieldWithPath("localDateTime").description("응답 시간"),
                                    fieldWithPath("message").description("응답 메시지"),
                                    fieldWithPath("data.messageId").description("메시지 ID"),
                                    fieldWithPath("data.roomId").description("채팅방 ID"),
                                    fieldWithPath("data.senderId").description("메시지 작성자 회원 ID"),
                                    fieldWithPath("data.message").description("삭제 처리 후 메시지 내용"),
                                    fieldWithPath("data.edited").description("메시지 수정 여부"),
                                    fieldWithPath("data.deleted").description("메시지 삭제 여부"),
                                    fieldWithPath("data.unreadCount").description("메시지를 아직 읽지 않은 참여자 수"),
                                    fieldWithPath("data.createdAt").description("메시지 생성 시간")
                            )
                    ));

            ChatMessageResponseDto deletedMessage = pollMessage(messageEvents);
            assertThat(deletedMessage.messageId()).isEqualTo(sentMessage.messageId());
            assertThat(deletedMessage.deleted()).isTrue();
            assertThat(deletedMessage.message()).isEqualTo("삭제된 메시지입니다.");
        } finally {
            stompSession.disconnect();
            stompClient.stop();
        }
    }

    @Test
    @DisplayName("[Integration] 참여자가 아닌 회원은 채팅방 WebSocket 메시지를 구독할 수 없다.")
    void websocketSubscribe_deniesNonParticipant() throws Exception {
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

    private Long saveMember(String nickname) {
        String suffix = UUID.randomUUID().toString().replace("-", "");
        return transactionTemplate.execute(_ -> {
            Member member = member("chat-it-" + nickname + "-" + suffix + "@test.com", nickname);
            entityManager.persist(member);
            return member.getId();
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
                .with(authenticatedMember(members.senderId()))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "targetMemberId": %d,
                          "productId": %d
                        }
                        """.formatted(members.receiverId(), productId)));
    }

    private WebSocketStompClient createStompClient() {
        WebSocketStompClient stompClient = new WebSocketStompClient(new StandardWebSocketClient());
        stompClient.setMessageConverter(new CompositeMessageConverter(List.of(
                new ByteArrayMessageConverter(),
                new StringMessageConverter(),
                new JacksonJsonMessageConverter()
        )));
        return stompClient;
    }

    private StompSession connectStompSession(WebSocketStompClient stompClient, Long memberId) throws Exception {
        StompHeaders connectHeaders = new StompHeaders();
        connectHeaders.add("Authorization", bearerToken(memberId));
        return stompClient
                .connectAsync(
                        "ws://localhost:" + port + "/api/ws-stomp",
                        new WebSocketHttpHeaders(),
                        connectHeaders,
                        new StompSessionHandlerAdapter() {
                        }
                )
                .get(3, TimeUnit.SECONDS);
    }

    private void disconnectIfConnected(StompSession stompSession) {
        try {
            if (stompSession.isConnected()) {
                stompSession.disconnect();
            }
        } catch (IllegalStateException ignored) {
        }
    }

    private static RequestPostProcessor authenticatedMember(Long memberId) {
        return user(customUser(memberId));
    }

    private String bearerToken(Long memberId) {
        CustomUser user = customUser(memberId);
        Authentication authentication = new UsernamePasswordAuthenticationToken(user, "", user.getAuthorities());
        return "Bearer " + jwtTokenProvider.createToken(authentication).accessToken();
    }

    private static CustomUser customUser(Long memberId) {
        return new CustomUser(
                memberId,
                "chat-user-" + memberId + "@test.com",
                "",
                List.of(new SimpleGrantedAuthority(MemberRole.MEMBER.toString()))
        );
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
