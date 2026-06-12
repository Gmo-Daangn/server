package com.ktcloud.daangn.chat.service;

import com.ktcloud.daangn.chat.dto.ChatMessageResponseDto;
import com.ktcloud.daangn.common.exception.InvalidInputException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ChatMessageServiceImplTestExceptionCase extends ChatServiceTestSupport {

    @Nested
    @DisplayName("채팅메시지")
    class ChatMessage {

        @Test
        @DisplayName("[Exception] 본인이 작성하지 않은 메시지는 수정할 수 없다.")
        void edit_throwsExceptionWhenNotWriter() {
            TestChatRoom room = createRoom();
            ChatMessageResponseDto createdMessage = chatMessageService.create(
                    room.roomId(),
                    room.senderId(),
                    "owner message"
            );

            assertThatThrownBy(() -> chatMessageService.edit(
                    createdMessage.messageId(),
                    room.receiverId(),
                    "hack"
            )).isInstanceOf(InvalidInputException.class)
                    .hasMessage("본인이 작성한 메시지만 수정 또는 삭제할 수 있습니다.");
        }

        @Test
        @DisplayName("[Exception] 참여하지 않은 채팅방에는 메시지를 보낼 수 없다.")
        void create_throwsExceptionWhenMemberIsNotParticipant() {
            TestChatRoom room = createRoom();
            Long outsiderId = saveMember("c");

            assertThatThrownBy(() -> chatMessageService.create(
                    room.roomId(),
                    outsiderId,
                    "hello"
            )).isInstanceOf(InvalidInputException.class)
                    .hasMessage("채팅방 참여자가 아닙니다.");
        }

        @Test
        @DisplayName("[Exception] 존재하지 않는 채팅방에는 메시지를 보낼 수 없다.")
        void create_throwsExceptionWhenRoomDoesNotExist() {
            Long memberId = saveMember("a");

            assertThatThrownBy(() -> chatMessageService.create(
                    Long.MAX_VALUE,
                    memberId,
                    "hello"
            )).isInstanceOf(InvalidInputException.class)
                    .hasMessage("채팅방이 존재하지 않습니다.");
        }

        @Test
        @DisplayName("[Exception] 삭제된 메시지는 수정할 수 없다.")
        void edit_throwsExceptionWhenMessageAlreadyDeleted() {
            TestChatRoom room = createRoom();
            ChatMessageResponseDto createdMessage = chatMessageService.create(
                    room.roomId(),
                    room.senderId(),
                    "owner message"
            );
            chatMessageService.delete(createdMessage.messageId(), room.senderId());

            assertThatThrownBy(() -> chatMessageService.edit(
                    createdMessage.messageId(),
                    room.senderId(),
                    "after"
            )).isInstanceOf(InvalidInputException.class)
                    .hasMessage("삭제된 메시지는 수정 또는 삭제할 수 없습니다.");
        }

        @Test
        @DisplayName("[Exception] 존재하지 않는 메시지는 삭제할 수 없다.")
        void delete_throwsExceptionWhenMessageDoesNotExist() {
            TestChatRoom room = createRoom();

            assertThatThrownBy(() -> chatMessageService.delete(
                    Long.MAX_VALUE,
                    room.senderId()
            )).isInstanceOf(InvalidInputException.class)
                    .hasMessage("메시지가 존재하지 않습니다.");
        }

        @Test
        @DisplayName("[Exception] 검색어가 null이면 메시지를 검색할 수 없다.")
        void search_throwsExceptionWhenKeywordIsNull() {
            TestChatRoom room = createRoom();

            assertThatThrownBy(() -> chatMessageService.search(
                    room.roomId(),
                    room.senderId(),
                    null,
                    null,
                    30
            )).isInstanceOf(InvalidInputException.class)
                    .hasMessage("검색어는 비어 있을 수 없습니다.");
        }

        @Test
        @DisplayName("[Exception] 검색어가 공백이면 메시지를 검색할 수 없다.")
        void search_throwsExceptionWhenKeywordIsBlank() {
            TestChatRoom room = createRoom();

            assertThatThrownBy(() -> chatMessageService.search(
                    room.roomId(),
                    room.senderId(),
                    "   ",
                    null,
                    30
            )).isInstanceOf(InvalidInputException.class)
                    .hasMessage("검색어는 비어 있을 수 없습니다.");
        }

        @Test
        @DisplayName("[Exception] 검색 개수가 1보다 작으면 메시지를 검색할 수 없다.")
        void search_throwsExceptionWhenSizeIsLessThanOne() {
            TestChatRoom room = createRoom();

            assertThatThrownBy(() -> chatMessageService.search(
                    room.roomId(),
                    room.senderId(),
                    "hello",
                    null,
                    0
            )).isInstanceOf(InvalidInputException.class)
                    .hasMessage("검색 개수는 1 이상 100 이하로 입력해주세요.");
        }

        @Test
        @DisplayName("[Exception] 검색 개수가 100보다 크면 메시지를 검색할 수 없다.")
        void search_throwsExceptionWhenSizeIsGreaterThanMax() {
            TestChatRoom room = createRoom();

            assertThatThrownBy(() -> chatMessageService.search(
                    room.roomId(),
                    room.senderId(),
                    "hello",
                    null,
                    101
            )).isInstanceOf(InvalidInputException.class)
                    .hasMessage("검색 개수는 1 이상 100 이하로 입력해주세요.");
        }
    }
}
