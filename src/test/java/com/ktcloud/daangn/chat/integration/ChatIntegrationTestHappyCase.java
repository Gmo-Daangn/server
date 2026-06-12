package com.ktcloud.daangn.chat.integration;

import com.ktcloud.daangn.chat.dto.ChatMessageRequestDto;
import com.ktcloud.daangn.chat.dto.ChatMessageResponseDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.messaging.simp.stomp.StompSession;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@ExtendWith(RestDocumentationExtension.class)
class ChatIntegrationTestHappyCase extends ChatIntegrationTestSupport {

    @Nested
    @DisplayName("채팅방")
    class ChatRoom {

        @Test
        @DisplayName("[Integration] 1대1 채팅방에 입장한다.")
        void enterDirectRoom_createsRoom() throws Exception {
            TestMembers members = saveMembers();

            enterRoom(members, 100L)
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
                    ));
        }

        @Test
        @DisplayName("[Integration] 기존 1대1 채팅방에 재입장한다.")
        void enterDirectRoom_returnsExistingRoom() throws Exception {
            TestMembers members = saveMembers();
            MvcResult enterResult = enterRoom(members, 100L)
                    .andExpect(status().isOk())
                    .andReturn();
            Long roomId = readLong(enterResult, "$.data.roomId");

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
        }

        @Test
        @DisplayName("[Integration] 내가 참여한 채팅방 목록을 조회한다.")
        void findDirectRooms_returnsRooms() throws Exception {
            TestMembers members = saveMembers();
            MvcResult enterResult = enterRoom(members, 100L)
                    .andExpect(status().isOk())
                    .andReturn();
            Long roomId = readLong(enterResult, "$.data.roomId");
            chatMessageService.create(roomId, members.senderId(), "hello integration");

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
        }

        @Test
        @DisplayName("[Integration] 채팅방 메시지를 읽음 처리한다.")
        void readDirectRoom_marksMessagesAsRead() throws Exception {
            TestMembers members = saveMembers();
            MvcResult enterResult = enterRoom(members, 100L)
                    .andExpect(status().isOk())
                    .andReturn();
            Long roomId = readLong(enterResult, "$.data.roomId");
            chatMessageService.create(roomId, members.senderId(), "hello integration");

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
        }
    }

    @Nested
    @DisplayName("채팅")
    class ChatMessage {

        @Test
        @DisplayName("[Integration] 채팅방의 기존 메시지를 조회한다.")
        void findMessages_returnsMessages() throws Exception {
            TestMembers members = saveMembers();
            MvcResult enterResult = enterRoom(members, 100L)
                    .andExpect(status().isOk())
                    .andReturn();
            Long roomId = readLong(enterResult, "$.data.roomId");
            ChatMessageResponseDto message = chatMessageService.create(
                    roomId,
                    members.senderId(),
                    "hello integration"
            );

            mockMvc.perform(get("/api/v1/chat/messages/{roomId}", roomId)
                            .with(authenticatedMember(members.senderId())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data[0].messageId").value(message.messageId()))
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
        }

        @Test
        @DisplayName("[Integration] 채팅방 메시지를 검색한다.")
        void searchMessages_returnsMessages() throws Exception {
            TestMembers members = saveMembers();
            MvcResult enterResult = enterRoom(members, 100L)
                    .andExpect(status().isOk())
                    .andReturn();
            Long roomId = readLong(enterResult, "$.data.roomId");
            ChatMessageResponseDto message = chatMessageService.create(
                    roomId,
                    members.senderId(),
                    "hello integration"
            );

            mockMvc.perform(get("/api/v1/chat/messages/{roomId}/search", roomId)
                            .with(authenticatedMember(members.senderId()))
                            .param("keyword", "hello"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data[0].messageId").value(message.messageId()))
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
        }

        @Test
        @DisplayName("[Integration] 메시지를 수정한다.")
        void editMessage_updatesMessage() throws Exception {
            TestMembers members = saveMembers();
            MvcResult enterResult = enterRoom(members, 100L)
                    .andExpect(status().isOk())
                    .andReturn();
            Long roomId = readLong(enterResult, "$.data.roomId");
            ChatMessageResponseDto message = chatMessageService.create(
                    roomId,
                    members.senderId(),
                    "hello integration"
            );

            mockMvc.perform(patch("/api/v1/chat/messages/{messageId}", message.messageId())
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
        }

        @Test
        @DisplayName("[Integration] 메시지를 삭제한다.")
        void deleteMessage_marksMessageDeleted() throws Exception {
            TestMembers members = saveMembers();
            MvcResult enterResult = enterRoom(members, 100L)
                    .andExpect(status().isOk())
                    .andReturn();
            Long roomId = readLong(enterResult, "$.data.roomId");
            ChatMessageResponseDto message = chatMessageService.create(
                    roomId,
                    members.senderId(),
                    "hello integration"
            );

            mockMvc.perform(delete("/api/v1/chat/messages/{messageId}", message.messageId())
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
        }
    }

    @Nested
    @DisplayName("STOMP")
    class Stomp {

        @Test
        @DisplayName("[Integration] STOMP로 메시지를 전송하면 구독자에게 브로드캐스트된다.")
        void sendMessage_publishesMessage() throws Exception {
            TestMembers members = saveMembers();
            MvcResult enterResult = enterRoom(members, 100L)
                    .andExpect(status().isOk())
                    .andReturn();
            Long roomId = readLong(enterResult, "$.data.roomId");

            WebSocketStompClient stompClient = createStompClient();
            StompSession stompSession = connectStompSession(stompClient, members.senderId());
            BlockingQueue<ChatMessageResponseDto> messageEvents = new LinkedBlockingQueue<>();

            try {
                stompSession.subscribe("/sub/chat/rooms/" + roomId + "/messages", new ChatMessageFrameHandler(messageEvents));
                // 구독 프레임이 서버 브로커에 반영되기 전에 send가 먼저 도착하면 이벤트를 놓칠 수 있다.
                TimeUnit.MILLISECONDS.sleep(500);

                stompSession.send(
                        "/pub/chat/rooms/" + roomId + "/messages",
                        new ChatMessageRequestDto("hello integration")
                );

                ChatMessageResponseDto sentMessage = pollMessage(messageEvents);
                assertThat(sentMessage.roomId()).isEqualTo(roomId);
                assertThat(sentMessage.senderId()).isEqualTo(members.senderId());
                assertThat(sentMessage.message()).isEqualTo("hello integration");
                assertThat(sentMessage.unreadCount()).isEqualTo(1);
            } finally {
                disconnectIfConnected(stompSession);
                stompClient.stop();
            }
        }
    }
}
