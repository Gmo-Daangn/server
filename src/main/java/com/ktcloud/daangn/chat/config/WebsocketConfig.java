package com.ktcloud.daangn.chat.config;

import com.ktcloud.daangn.auth.dto.CustomUser;
import com.ktcloud.daangn.auth.jwt.JwtTokenProvider;
import com.ktcloud.daangn.chat.repository.ChatParticipantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.security.Principal;

@Configuration
@RequiredArgsConstructor
@EnableWebSocketMessageBroker
public class WebsocketConfig implements WebSocketMessageBrokerConfigurer {

    private static final String CHAT_ROOM_SUBSCRIPTION_PREFIX = "/sub/chat/rooms/";

    private final JwtTokenProvider jwtTokenProvider;
    private final ChatParticipantRepository chatParticipantRepository;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        //클라이언트의 send 요청 처리
        //
        registry.setApplicationDestinationPrefixes("/pub");
        registry.enableSimpleBroker("/sub");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/api/ws-stomp")
                .setAllowedOriginPatterns("*");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                if (accessor == null) {
                    return message;
                }

                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                    Authentication authentication = authenticate(accessor);
                    accessor.setUser(authentication);
                    return message;
                }

                if (isAuthenticatedCommand(accessor.getCommand()) && accessor.getUser() == null) {
                    throw new AuthenticationCredentialsNotFoundException("인증 정보가 없습니다.");
                }

                if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
                    validateChatRoomSubscription(accessor);
                }

                return message;
            }
        });
    }

    private Authentication authenticate(StompHeaderAccessor accessor) {
        String token = resolveToken(accessor);
        if (!StringUtils.hasText(token) || !jwtTokenProvider.validateToken(token)) {
            throw new AuthenticationCredentialsNotFoundException("유효한 토큰이 필요합니다.");
        }

        return jwtTokenProvider.authentication(token);
    }

    private boolean isAuthenticatedCommand(StompCommand command) {
        return StompCommand.SEND.equals(command) || StompCommand.SUBSCRIBE.equals(command);
    }

    private void validateChatRoomSubscription(StompHeaderAccessor accessor) {
        String destination = accessor.getDestination();
        if (!StringUtils.hasText(destination) || !destination.startsWith(CHAT_ROOM_SUBSCRIPTION_PREFIX)) {
            return;
        }

        Long roomId = extractRoomId(destination);
        Long memberId = getMemberId(accessor.getUser());
        if (chatParticipantRepository.findByChatRoom_IdAndMember_Id(roomId, memberId).isEmpty()) {
            throw new AccessDeniedException("채팅방 참여자가 아닙니다.");
        }
    }

    private Long extractRoomId(String destination) {
        int startIndex = CHAT_ROOM_SUBSCRIPTION_PREFIX.length();
        int endIndex = destination.indexOf("/", startIndex);
        String roomId = endIndex == -1 ? destination.substring(startIndex) : destination.substring(startIndex, endIndex);

        try {
            return Long.parseLong(roomId);
        } catch (NumberFormatException e) {
            throw new AccessDeniedException("잘못된 채팅방 구독 경로입니다.");
        }
    }

    private Long getMemberId(Principal principal) {
        if (principal instanceof Authentication authentication
                && authentication.getPrincipal() instanceof CustomUser user) {
            return user.getMemberId();
        }

        throw new AuthenticationCredentialsNotFoundException("인증 정보가 없습니다.");
    }

    private String resolveToken(StompHeaderAccessor accessor) {
        String authorization = accessor.getFirstNativeHeader("Authorization");
        if (!StringUtils.hasText(authorization)) {
            authorization = accessor.getFirstNativeHeader("authorization");
        }

        if (StringUtils.hasText(authorization) && authorization.startsWith("Bearer ") && authorization.length() > 7) {
            return authorization.substring(7);
        }

        return null;
    }
}
