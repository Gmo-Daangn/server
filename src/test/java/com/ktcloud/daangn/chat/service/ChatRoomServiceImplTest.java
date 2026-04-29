package com.ktcloud.daangn.chat.service;

import com.ktcloud.daangn.chat.dto.ChatMessageRequestDto;
import com.ktcloud.daangn.chat.dto.ChatRoomEnterRequestDto;
import com.ktcloud.daangn.chat.dto.ChatRoomEnterResponseDto;
import com.ktcloud.daangn.chat.dto.ChatRoomListResponseDto;
import com.ktcloud.daangn.chat.dto.ChatRoomReadRequestDto;
import com.ktcloud.daangn.chat.dto.ChatRoomReadResponseDto;
import com.ktcloud.daangn.config.TestContainerConfig;
import com.ktcloud.daangn.config.exception.InvalidInputException;
import com.ktcloud.daangn.member.service.MemberService;
import com.ktcloud.daangn.member.dto.MemberSignupRequestDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Import(TestContainerConfig.class)
@ActiveProfiles("test")
@Transactional
class ChatRoomServiceImplTest {

    @Autowired
    private ChatRoomService chatRoomService;

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private MemberService memberService;

    @Test
    @DisplayName("[HAPPY] 1대1 채팅방 재입장 시 기존 채팅방을 반환한다.")
    void enterDirectRoom_returnsExistingRoom() {
        signup("a@test.com", "a");
        signup("b@test.com", "b");

        ChatRoomEnterRequestDto dto = new ChatRoomEnterRequestDto("a@test.com", "b@test.com", 100L);

        ChatRoomEnterResponseDto firstResponse = chatRoomService.enterDirectRoom(dto);
        ChatRoomEnterResponseDto secondResponse = chatRoomService.enterDirectRoom(dto);

        assertThat(firstResponse.created()).isTrue();
        assertThat(secondResponse.created()).isFalse();
        assertThat(secondResponse.roomId()).isEqualTo(firstResponse.roomId());
    }

    @Test
    @DisplayName("[HAPPY] 내가 참여한 1대1 채팅방 목록을 조회할 수 있다.")
    void findDirectRooms_returnsMyRooms() {
        signup("a@test.com", "a");
        signup("b@test.com", "b");

        ChatRoomEnterResponseDto room = chatRoomService.enterDirectRoom(
                new ChatRoomEnterRequestDto("a@test.com", "b@test.com", 200L)
        );
        chatMessageService.create(room.roomId(), new ChatMessageRequestDto("a@test.com", "hello"));

        List<ChatRoomListResponseDto> rooms = chatRoomService.findDirectRooms("a@test.com");

        assertThat(rooms).hasSize(1);
        assertThat(rooms.getFirst().roomId()).isEqualTo(room.roomId());
        assertThat(rooms.getFirst().otherMemberEmail()).isEqualTo("b@test.com");
        assertThat(rooms.getFirst().lastMessage()).isEqualTo("hello");
    }

    @Test
    @DisplayName("[HAPPY] 상대방 메시지를 읽으면 읽음 처리된다.")
    void readDirectRoom_marksUnreadMessagesAsRead() {
        signup("a@test.com", "a");
        signup("b@test.com", "b");

        ChatRoomEnterResponseDto room = chatRoomService.enterDirectRoom(
                new ChatRoomEnterRequestDto("a@test.com", "b@test.com", 300L)
        );
        chatMessageService.create(room.roomId(), new ChatMessageRequestDto("a@test.com", "first"));
        chatMessageService.create(room.roomId(), new ChatMessageRequestDto("a@test.com", "second"));

        ChatRoomReadResponseDto response = chatRoomService.readDirectRoom(
                room.roomId(),
                new ChatRoomReadRequestDto("b@test.com")
        );

        assertThat(response.roomId()).isEqualTo(room.roomId());
        assertThat(response.readMessageCount()).isEqualTo(2);
        assertThat(chatMessageService.list(room.roomId(), "b@test.com"))
                .extracting(it -> it.unreadCount())
                .containsOnly(0L);
    }

    @Test
    @DisplayName("[Exception] 본인과의 1대1 채팅방은 생성할 수 없다.")
    void enterDirectRoom_throwsExceptionWhenTargetIsSelf() {
        signup("a@test.com", "a");

        assertThatThrownBy(() -> chatRoomService.enterDirectRoom(
                new ChatRoomEnterRequestDto("a@test.com", "a@test.com", 100L)
        )).isInstanceOf(InvalidInputException.class)
                .hasMessage("본인과의 채팅방은 만들 수 없습니다.");
    }

    @Test
    @DisplayName("[Exception] 존재하지 않는 회원으로 채팅방 목록을 조회할 수 없다.")
    void findDirectRooms_throwsExceptionWhenMemberDoesNotExist() {
        assertThatThrownBy(() -> chatRoomService.findDirectRooms("missing@test.com"))
                .isInstanceOf(InvalidInputException.class)
                .hasMessage("회원이 존재하지 않습니다.");
    }

    @Test
    @DisplayName("[Exception] 참여하지 않은 채팅방은 읽음 처리할 수 없다.")
    void readDirectRoom_throwsExceptionWhenMemberIsNotParticipant() {
        signup("a@test.com", "a");
        signup("b@test.com", "b");
        signup("c@test.com", "c");

        ChatRoomEnterResponseDto room = chatRoomService.enterDirectRoom(
                new ChatRoomEnterRequestDto("a@test.com", "b@test.com", 300L)
        );
        chatMessageService.create(room.roomId(), new ChatMessageRequestDto("a@test.com", "first"));

        assertThatThrownBy(() -> chatRoomService.readDirectRoom(
                room.roomId(),
                new ChatRoomReadRequestDto("c@test.com")
        )).isInstanceOf(InvalidInputException.class)
                .hasMessage("채팅방 참여자가 아닙니다.");
    }

    private void signup(String email, String nickname) {
        memberService.signup(new MemberSignupRequestDto(email, nickname, "password", "서울"));
    }
}
