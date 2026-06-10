package com.ktcloud.daangn.chat.integration;

import com.jayway.jsonpath.JsonPath;
import com.ktcloud.daangn.auth.dto.CustomUser;
import com.ktcloud.daangn.auth.jwt.JwtTokenProvider;
import com.ktcloud.daangn.chat.dto.ChatMessageResponseDto;
import com.ktcloud.daangn.chat.service.ChatMessageService;
import com.ktcloud.daangn.config.TestContainerConfig;
import com.ktcloud.daangn.member.entity.Member;
import com.ktcloud.daangn.member.entity.MemberRole;
import com.ktcloud.daangn.member.entity.ProviderToken;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
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
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

abstract class ChatIntegrationTestSupport extends TestContainerConfig {

    protected MockMvc mockMvc;

    @Autowired
    protected ChatMessageService chatMessageService;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @LocalServerPort
    private int port;

    protected static RequestPostProcessor authenticatedMember(Long memberId) {
        return user(customUser(memberId));
    }

    private static CustomUser customUser(Long memberId) {
        return new CustomUser(
                memberId,
                "chat-user-" + memberId + "@test.com",
                "",
                List.of(new SimpleGrantedAuthority(MemberRole.MEMBER.toString()))
        );
    }

    private static Long readLong(String json, String expression) {
        Number number = JsonPath.read(json, expression);
        return number.longValue();
    }

    private static ChatMessageResponseDto parseMessage(byte[] payload) {
        String json = new String(payload, StandardCharsets.UTF_8);

        // STOMP 클라이언트 컨버터 차이에 테스트가 흔들리지 않도록 body를 직접 파싱한다.
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

    // junit5 기준 문제없는 코드, 혹시 빨간 에러가 떠도 컴파일 및 동작에 문제가 없음
    @BeforeEach
    void setUp(WebApplicationContext context, RestDocumentationContextProvider restDocumentation) {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(documentationConfiguration(restDocumentation))
                .apply(springSecurity())
                .build();
    }

    protected TestMembers saveMembers() {
        String suffix = UUID.randomUUID().toString().replace("-", "");
        return transactionTemplate.execute(_ -> {
            Member sender = member("chat-it-sender-" + suffix + "@test.com", "sender");
            Member receiver = member("chat-it-receiver-" + suffix + "@test.com", "receiver");
            entityManager.persist(sender);
            entityManager.persist(receiver);
            return new TestMembers(sender.getId(), receiver.getId());
        });
    }

    protected Long saveMember(String nickname) {
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

    protected ResultActions enterRoom(TestMembers members, Long productId) throws Exception {
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

    protected WebSocketStompClient createStompClient() {
        WebSocketStompClient stompClient = new WebSocketStompClient(new StandardWebSocketClient());
        stompClient.setMessageConverter(new CompositeMessageConverter(List.of(
                new ByteArrayMessageConverter(),
                new StringMessageConverter(),
                new JacksonJsonMessageConverter()
        )));
        return stompClient;
    }

    protected StompSession connectStompSession(WebSocketStompClient stompClient, Long memberId) throws Exception {
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

    protected void disconnectIfConnected(StompSession stompSession) {
        try {
            if (stompSession.isConnected()) {
                stompSession.disconnect();
            }
        } catch (IllegalStateException ignored) {
        }
    }

    private String bearerToken(Long memberId) {
        CustomUser user = customUser(memberId);
        Authentication authentication = new UsernamePasswordAuthenticationToken(user, "", user.getAuthorities());
        return "Bearer " + jwtTokenProvider.createToken(authentication).accessToken();
    }

    protected Long readLong(MvcResult result, String expression) throws Exception {
        Number number = JsonPath.read(result.getResponse().getContentAsString(), expression);
        return number.longValue();
    }

    protected ChatMessageResponseDto pollMessage(BlockingQueue<ChatMessageResponseDto> messageEvents) throws InterruptedException {
        // WebSocket 이벤트는 비동기로 도착하므로 큐에서 일정 시간 기다린 뒤 검증한다.
        ChatMessageResponseDto message = messageEvents.poll(10, TimeUnit.SECONDS);
        assertThat(message).isNotNull();
        return message;
    }

    protected record TestMembers(
            Long senderId,
            Long receiverId
    ) {
    }

    protected static class ChatMessageFrameHandler implements StompFrameHandler {

        private final BlockingQueue<ChatMessageResponseDto> messageEvents;

        protected ChatMessageFrameHandler(BlockingQueue<ChatMessageResponseDto> messageEvents) {
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
