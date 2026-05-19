package com.ktcloud.daangn.notification.controller;

import com.ktcloud.daangn.auth.authority.SecurityConfig;
import com.ktcloud.daangn.auth.jwt.JwtAccessDeniedHandler;
import com.ktcloud.daangn.auth.jwt.JwtAuthenticationEntryPoint;
import com.ktcloud.daangn.auth.jwt.JwtTokenProvider;
import com.ktcloud.daangn.notification.dto.NotificationResponseDto;
import com.ktcloud.daangn.notification.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@WebMvcTest(NotificationController.class)
@Import({
        SecurityConfig.class,
        JwtAuthenticationEntryPoint.class,
        JwtAccessDeniedHandler.class
})
@ExtendWith(RestDocumentationExtension.class)
class NotificationControllerTest {

    private MockMvc mockMvc;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private NotificationService notificationService;

    @BeforeEach
    void setUp(WebApplicationContext context, RestDocumentationContextProvider restDocumentation) {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(documentationConfiguration(restDocumentation))
                .apply(springSecurity())
                .build();
    }

    @Nested
    @DisplayName("알림 API")
    class NotificationApi {

        @Test
        @DisplayName("[HAPPY] SSE 알림 구독을 성공한다.")
        public void subscribe_Success() throws Exception {
            Long memberId = 1L;
            SseEmitter emitter = new SseEmitter();
            given(notificationService.subscribe(memberId)).willReturn(emitter);

            mockMvc.perform(get("/api/v1/notification/sse")
                            .param("memberId", String.valueOf(memberId))
                            .accept(MediaType.TEXT_EVENT_STREAM_VALUE))
                    .andExpect(status().isOk())
                    .andDo(document("notification-subscribe-success",
                            queryParameters(
                                    parameterWithName("memberId").description("구독할 멤버 ID")
                            )
                    ));
        }

        @Test
        @DisplayName("[HAPPY] 알림 목록 조회를 성공한다.")
        public void getNotifications_Success() throws Exception {
            Long memberId = 1L;
            List<NotificationResponseDto> responseList = List.of(
                    new NotificationResponseDto(
                            1L, memberId, "CHAT", "채팅", 1L, "새 채팅: 안녕하세요", false, LocalDateTime.now()
                    )
            );
            given(notificationService.getNotifications(memberId)).willReturn(responseList);

            mockMvc.perform(get("/api/v1/notification")
                            .param("memberId", String.valueOf(memberId))
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andDo(document("notification-list-success",
                            queryParameters(
                                    parameterWithName("memberId").description("수신자(회원) ID")
                            ),
                            responseFields(
                                    fieldWithPath("code").description("HTTP 상태 코드"),
                                    fieldWithPath("localDateTime").description("응답 시간"),
                                    fieldWithPath("message").description("응답 메시지"),
                                    fieldWithPath("data[].id").description("알림 ID"),
                                    fieldWithPath("data[].receiverId").description("수신자 회원 ID"),
                                    fieldWithPath("data[].templateType").description("알림 템플릿 타입"),
                                    fieldWithPath("data[].templateTitle").description("알림 제목"),
                                    fieldWithPath("data[].identifier").description("도메인 식별자 (예: 채팅방 ID)"),
                                    fieldWithPath("data[].message").description("알림 본문 내용"),
                                    fieldWithPath("data[].isRead").description("읽음 여부"),
                                    fieldWithPath("data[].createdAt").description("알림 생성 시간")
                            )
                    ));
        }

        @Test
        @DisplayName("[HAPPY] 알림을 읽음 처리를 성공한다.")
        public void readNotification_Success() throws Exception {
            Long notificationId = 1L;
            given(notificationService.readNotification(notificationId)).willReturn("알림 읽음 처리 성공");

            // when & then
            mockMvc.perform(patch("/api/v1/notification/{id}", notificationId)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andDo(document("notification-read-success",
                            pathParameters(
                                    parameterWithName("id").description("읽음 처리할 알림 ID")
                            ),
                            responseFields(
                                    fieldWithPath("code").description("HTTP 상태 코드"),
                                    fieldWithPath("localDateTime").description("응답 시간"),
                                    fieldWithPath("message").description("응답 메시지"),
                                    fieldWithPath("data").description("결과 텍스트 데이터")
                            )
                    ));
        }

        @Test
        @DisplayName("[HAPPY] 알림 삭제를 성공한다.")
        public void deleteNotification_Success() throws Exception {
            Long notificationId = 1L;
            given(notificationService.deleteNotification(notificationId)).willReturn("알림 삭제 성공");

            mockMvc.perform(delete("/api/v1/notification/{id}", notificationId)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andDo(document("notification-delete-success",
                            pathParameters(
                                    parameterWithName("id").description("삭제할 알림 ID")
                            ),
                            responseFields(
                                    fieldWithPath("code").description("HTTP 상태 코드"),
                                    fieldWithPath("localDateTime").description("응답 시간"),
                                    fieldWithPath("message").description("응답 메시지"),
                                    fieldWithPath("data").description("결과 텍스트 데이터")
                            )
                    ));
        }
    }
}