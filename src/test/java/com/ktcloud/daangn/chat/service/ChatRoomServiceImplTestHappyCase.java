package com.ktcloud.daangn.chat.service;

import com.ktcloud.daangn.chat.dto.ChatRoomEnterRequestDto;
import com.ktcloud.daangn.chat.dto.ChatRoomEnterResponseDto;
import com.ktcloud.daangn.chat.dto.ChatRoomListResponseDto;
import com.ktcloud.daangn.chat.dto.ChatRoomReadResponseDto;
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
class ChatRoomServiceImplTestHappyCase extends ChatServiceTestSupport {

    @Nested
    @DisplayName("채팅방")
    class ChatRoom {

        @Test
        @DisplayName("[HAPPY] 1대1 채팅방 재입장 시 기존 채팅방을 반환한다.")
        void enterDirectRoom_returnsExistingRoom() {
            Long senderId = saveMember("a");
            Long receiverId = saveMember("b");

            ChatRoomEnterRequestDto dto = new ChatRoomEnterRequestDto(receiverId, 100L);

            ChatRoomEnterResponseDto firstResponse = chatRoomService.enterDirectRoom(senderId, dto);
            ChatRoomEnterResponseDto secondResponse = chatRoomService.enterDirectRoom(senderId, dto);

            assertThat(firstResponse.created()).isTrue();
            assertThat(secondResponse.created()).isFalse();
            assertThat(secondResponse.roomId()).isEqualTo(firstResponse.roomId());
        }

        @Test
        @DisplayName("[HAPPY] 내가 참여한 1대1 채팅방 목록을 조회할 수 있다.")
        void findDirectRooms_returnsMyRooms() {
            Long senderId = saveMember("a");
            Long receiverId = saveMember("b");

            ChatRoomEnterResponseDto room = chatRoomService.enterDirectRoom(
                    senderId,
                    new ChatRoomEnterRequestDto(receiverId, 200L)
            );
            chatMessageService.create(room.roomId(), senderId, "hello");

            List<ChatRoomListResponseDto> rooms = chatRoomService.findDirectRooms(senderId);

            assertThat(rooms).hasSize(1);
            assertThat(rooms.getFirst().roomId()).isEqualTo(room.roomId());
            assertThat(rooms.getFirst().otherMemberId()).isEqualTo(receiverId);
            assertThat(rooms.getFirst().lastMessage()).isEqualTo("hello");
        }

        @Test
        @DisplayName("[HAPPY] 1대1 채팅방 목록에는 다중 채팅방이 포함되지 않는다.")
        void findDirectRooms_excludesMultiRoom() {
            Long senderId = saveMember("a");
            Long receiverId = saveMember("b");
            Long anotherMemberId = saveMember("c");

            ChatRoomEnterResponseDto directRoom = chatRoomService.enterDirectRoom(
                    senderId,
                    new ChatRoomEnterRequestDto(receiverId, 200L)
            );
            Long multiRoomId = createMultiRoom(senderId, receiverId, anotherMemberId);

            List<ChatRoomListResponseDto> rooms = chatRoomService.findDirectRooms(senderId);

            assertThat(rooms)
                    .extracting(ChatRoomListResponseDto::roomId)
                    .containsExactly(directRoom.roomId())
                    .doesNotContain(multiRoomId);
        }

        @Test
        @DisplayName("[HAPPY] 상대방 메시지를 읽으면 읽음 처리된다.")
        void readDirectRoom_marksUnreadMessagesAsRead() {
            Long senderId = saveMember("a");
            Long receiverId = saveMember("b");

            ChatRoomEnterResponseDto room = chatRoomService.enterDirectRoom(
                    senderId,
                    new ChatRoomEnterRequestDto(receiverId, 300L)
            );
            chatMessageService.create(room.roomId(), senderId, "first");
            chatMessageService.create(room.roomId(), senderId, "second");

            ChatRoomReadResponseDto response = chatRoomService.readDirectRoom(
                    room.roomId(),
                    receiverId
            );

            assertThat(response.roomId()).isEqualTo(room.roomId());
            assertThat(response.readMessageCount()).isEqualTo(2);
            assertThat(chatMessageService.list(room.roomId(), receiverId))
                    .extracting(it -> it.unreadCount())
                    .containsOnly(0L);
        }
    }
}
