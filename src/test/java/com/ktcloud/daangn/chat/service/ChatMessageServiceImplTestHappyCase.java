package com.ktcloud.daangn.chat.service;

import com.ktcloud.daangn.chat.dto.ChatMessageResponseDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ChatMessageServiceImplTestHappyCase extends ChatServiceTestSupport {

    @Nested
    @DisplayName("채팅")
    class ChatMessage {

        @Test
        @DisplayName("[HAPPY] 메시지를 수정하면 수정 상태가 반영된다.")
        void edit_updatesMessage() {
            TestChatRoom room = createRoom();
            ChatMessageResponseDto createdMessage = chatMessageService.create(
                    room.roomId(),
                    room.senderId(),
                    "before"
            );

            ChatMessageResponseDto editedMessage = chatMessageService.edit(
                    createdMessage.messageId(),
                    room.senderId(),
                    "after"
            );

            assertThat(editedMessage.message()).isEqualTo("after");
            assertThat(editedMessage.edited()).isTrue();
        }

        @Test
        @DisplayName("[HAPPY] 메시지를 삭제하면 삭제 상태와 메시지 문구가 반영된다.")
        void delete_marksMessageDeleted() {
            TestChatRoom room = createRoom();
            ChatMessageResponseDto createdMessage = chatMessageService.create(
                    room.roomId(),
                    room.senderId(),
                    "to delete"
            );

            ChatMessageResponseDto deletedMessage = chatMessageService.delete(
                    createdMessage.messageId(),
                    room.senderId()
            );

            assertThat(deletedMessage.deleted()).isTrue();
            assertThat(deletedMessage.message()).isEqualTo("삭제된 메시지입니다.");
        }

        @Test
        @DisplayName("[HAPPY] 마지막 메시지가 아닌 메시지를 수정해도 채팅방 마지막 메시지는 유지된다.")
        void edit_oldMessageDoesNotChangeRoomLastMessage() {
            TestChatRoom room = createRoom();
            ChatMessageResponseDto oldMessage = chatMessageService.create(
                    room.roomId(),
                    room.senderId(),
                    "old message"
            );
            chatMessageService.create(
                    room.roomId(),
                    room.senderId(),
                    "latest message"
            );

            chatMessageService.edit(oldMessage.messageId(), room.senderId(), "edited old message");

            assertThat(chatRoomService.findDirectRooms(room.senderId()).getFirst().lastMessage())
                    .isEqualTo("latest message");
        }

        @Test
        @DisplayName("[HAPPY] 수신자가 일부 메시지만 읽은 상태면 이후 메시지만 미읽음으로 계산된다.")
        void list_calculatesUnreadCountWhenReceiverReadPreviousMessage() {
            TestChatRoom room = createRoom();
            ChatMessageResponseDto firstMessage = chatMessageService.create(
                    room.roomId(),
                    room.senderId(),
                    "first"
            );
            chatRoomService.readDirectRoom(room.roomId(), room.receiverId());
            ChatMessageResponseDto secondMessage = chatMessageService.create(
                    room.roomId(),
                    room.senderId(),
                    "second"
            );

            List<ChatMessageResponseDto> messages = chatMessageService.list(room.roomId(), room.senderId());

            assertThat(messages)
                    .extracting(ChatMessageResponseDto::messageId, ChatMessageResponseDto::unreadCount)
                    .containsExactly(
                            org.assertj.core.groups.Tuple.tuple(firstMessage.messageId(), 0L),
                            org.assertj.core.groups.Tuple.tuple(secondMessage.messageId(), 1L)
                    );
        }

        @Test
        @DisplayName("[HAPPY] 수신자가 없는 채팅방에 메시지를 보내면 미읽음 수가 0이다.")
        void create_returnsZeroUnreadCountWhenRoomHasNoReceiver() {
            TestChatRoom room = createSingleParticipantRoom();

            ChatMessageResponseDto response = chatMessageService.create(
                    room.roomId(),
                    room.senderId(),
                    "single participant message"
            );

            assertThat(response.unreadCount()).isZero();
        }

        @Test
        @DisplayName("[HAPPY] 긴 메시지를 보내도 메시지 생성이 처리된다.")
        void create_handlesLongMessagePreview() {
            TestChatRoom room = createRoom();
            String longMessage = "a".repeat(121);

            ChatMessageResponseDto response = chatMessageService.create(
                    room.roomId(),
                    room.senderId(),
                    longMessage
            );

            assertThat(response.message()).isEqualTo(longMessage);
        }

        @Test
        @DisplayName("[HAPPY] 채팅방 메시지를 검색하면 삭제되지 않은 메시지만 최신순으로 조회된다.")
        void search_returnsActiveMessagesByKeywordOrderByNewest() {
            TestChatRoom room = createRoom();
            ChatMessageResponseDto firstMessage = chatMessageService.create(
                    room.roomId(),
                    room.senderId(),
                    "오늘 거래 가능해요"
            );
            ChatMessageResponseDto secondMessage = chatMessageService.create(
                    room.roomId(),
                    room.senderId(),
                    "내일 거래 가능해요"
            );
            ChatMessageResponseDto deletedMessage = chatMessageService.create(
                    room.roomId(),
                    room.senderId(),
                    "거래 취소 메시지"
            );
            chatMessageService.delete(deletedMessage.messageId(), room.senderId());

            List<ChatMessageResponseDto> result = chatMessageService.search(
                    room.roomId(),
                    room.receiverId(),
                    "거래",
                    null,
                    10
            );

            assertThat(result)
                    .extracting(ChatMessageResponseDto::messageId)
                    .containsExactly(secondMessage.messageId(), firstMessage.messageId());
        }

        @Test
        @DisplayName("[HAPPY] 검색어의 LIKE 와일드카드는 일반 문자로 처리된다.")
        void search_treatsLikeWildcardAsPlainText() {
            TestChatRoom room = createRoom();
            ChatMessageResponseDto percentMessage = chatMessageService.create(
                    room.roomId(),
                    room.senderId(),
                    "100% 가능해요"
            );
            chatMessageService.create(
                    room.roomId(),
                    room.senderId(),
                    "100점 가능해요"
            );

            List<ChatMessageResponseDto> result = chatMessageService.search(
                    room.roomId(),
                    room.receiverId(),
                    "100%",
                    null,
                    10
            );

            assertThat(result)
                    .extracting(ChatMessageResponseDto::messageId)
                    .containsExactly(percentMessage.messageId());
        }
    }
}
