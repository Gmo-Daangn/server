package com.ktcloud.daangn.chat.service;

import com.ktcloud.daangn.chat.dto.ChatRoomEnterRequestDto;
import com.ktcloud.daangn.chat.dto.ChatRoomEnterResponseDto;
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
class ChatRoomServiceImplTestExceptionCase extends ChatServiceTestSupport {

    @Nested
    @DisplayName("채팅방")
    class ChatRoom {

        @Test
        @DisplayName("[Exception] 본인과의 1대1 채팅방은 생성할 수 없다.")
        void enterDirectRoom_throwsExceptionWhenTargetIsSelf() {
            Long memberId = saveMember("a");

            assertThatThrownBy(() -> chatRoomService.enterDirectRoom(
                    memberId,
                    new ChatRoomEnterRequestDto(memberId, 100L)
            )).isInstanceOf(InvalidInputException.class)
                    .hasMessage("본인과의 채팅방은 만들 수 없습니다.");
        }

        @Test
        @DisplayName("[Exception] 존재하지 않는 회원으로 채팅방 목록을 조회할 수 없다.")
        void findDirectRooms_throwsExceptionWhenMemberDoesNotExist() {
            assertThatThrownBy(() -> chatRoomService.findDirectRooms(Long.MAX_VALUE))
                    .isInstanceOf(InvalidInputException.class)
                    .hasMessage("존재하지 않는 ID입니다.");
        }

        @Test
        @DisplayName("[Exception] 참여하지 않은 채팅방은 읽음 처리할 수 없다.")
        void readDirectRoom_throwsExceptionWhenMemberIsNotParticipant() {
            Long senderId = saveMember("a");
            Long receiverId = saveMember("b");
            Long outsiderId = saveMember("c");

            ChatRoomEnterResponseDto room = chatRoomService.enterDirectRoom(
                    senderId,
                    new ChatRoomEnterRequestDto(receiverId, 300L)
            );
            chatMessageService.create(room.roomId(), senderId, "first");

            assertThatThrownBy(() -> chatRoomService.readDirectRoom(
                    room.roomId(),
                    outsiderId
            )).isInstanceOf(InvalidInputException.class)
                    .hasMessage("채팅방 참여자가 아닙니다.");
        }
    }
}
