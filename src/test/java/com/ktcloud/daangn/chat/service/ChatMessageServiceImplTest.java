package com.ktcloud.daangn.chat.service;

import com.ktcloud.daangn.chat.dto.ChatMessageResponseDto;
import com.ktcloud.daangn.chat.dto.ChatRoomEnterRequestDto;
import com.ktcloud.daangn.chat.dto.ChatRoomEnterResponseDto;
import com.ktcloud.daangn.config.TestContainerConfig;
import com.ktcloud.daangn.config.exception.InvalidInputException;
import com.ktcloud.daangn.member.dto.MemberSignupRequestDto;
import com.ktcloud.daangn.member.service.MemberService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Import(TestContainerConfig.class)
@ActiveProfiles("test")
@Transactional
class ChatMessageServiceImplTest {

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private ChatRoomService chatRoomService;

    @Autowired
    private MemberService memberService;

    @Test
    @DisplayName("[HAPPY] 메시지를 수정하면 수정 상태가 반영된다.")
    void edit_updatesMessage() {
        Long roomId = createRoom();
        ChatMessageResponseDto createdMessage = chatMessageService.create(
                roomId,
                "a@test.com",
                "before"
        );

        ChatMessageResponseDto editedMessage = chatMessageService.edit(
                createdMessage.messageId(),
                "a@test.com",
                "after"
        );

        assertThat(editedMessage.message()).isEqualTo("after");
        assertThat(editedMessage.edited()).isTrue();
    }

    @Test
    @DisplayName("[HAPPY] 메시지를 삭제하면 삭제 상태와 메시지 문구가 반영된다.")
    void delete_marksMessageDeleted() {
        Long roomId = createRoom();
        ChatMessageResponseDto createdMessage = chatMessageService.create(
                roomId,
                "a@test.com",
                "to delete"
        );

        ChatMessageResponseDto deletedMessage = chatMessageService.delete(
                createdMessage.messageId(),
                "a@test.com"
        );

        assertThat(deletedMessage.deleted()).isTrue();
        assertThat(deletedMessage.message()).isEqualTo("삭제된 메시지입니다.");
    }

    @Test
    @DisplayName("[Exception] 본인이 작성하지 않은 메시지는 수정할 수 없다.")
    void edit_throwsExceptionWhenNotWriter() {
        Long roomId = createRoom();
        ChatMessageResponseDto createdMessage = chatMessageService.create(
                roomId,
                "a@test.com",
                "owner message"
        );

        assertThatThrownBy(() -> chatMessageService.edit(
                createdMessage.messageId(),
                "b@test.com",
                "hack"
        )).isInstanceOf(InvalidInputException.class)
                .hasMessage("본인이 작성한 메시지만 수정 또는 삭제할 수 있습니다.");
    }

    @Test
    @DisplayName("[Exception] 참여하지 않은 채팅방에는 메시지를 보낼 수 없다.")
    void create_throwsExceptionWhenMemberIsNotParticipant() {
        Long roomId = createRoom();
        signup("c@test.com", "c");

        assertThatThrownBy(() -> chatMessageService.create(
                roomId,
                "c@test.com",
                "hello"
        )).isInstanceOf(InvalidInputException.class)
                .hasMessage("채팅방 참여자가 아닙니다.");
    }

    @Test
    @DisplayName("[Exception] 삭제된 메시지는 수정할 수 없다.")
    void edit_throwsExceptionWhenMessageAlreadyDeleted() {
        Long roomId = createRoom();
        ChatMessageResponseDto createdMessage = chatMessageService.create(
                roomId,
                "a@test.com",
                "owner message"
        );
        chatMessageService.delete(createdMessage.messageId(), "a@test.com");

        assertThatThrownBy(() -> chatMessageService.edit(
                createdMessage.messageId(),
                "a@test.com",
                "after"
        )).isInstanceOf(InvalidInputException.class)
                .hasMessage("삭제된 메시지는 수정 또는 삭제할 수 없습니다.");
    }

    @Test
    @DisplayName("[Exception] 존재하지 않는 메시지는 삭제할 수 없다.")
    void delete_throwsExceptionWhenMessageDoesNotExist() {
        createRoom();

        assertThatThrownBy(() -> chatMessageService.delete(
                Long.MAX_VALUE,
                "a@test.com"
        )).isInstanceOf(InvalidInputException.class)
                .hasMessage("메시지가 존재하지 않습니다.");
    }

    private Long createRoom() {
        signup("a@test.com", "a");
        signup("b@test.com", "b");

        ChatRoomEnterResponseDto room = chatRoomService.enterDirectRoom(
                new ChatRoomEnterRequestDto("a@test.com", "b@test.com", 400L)
        );

        return room.roomId();
    }

    private void signup(String email, String nickname) {
        memberService.signup(new MemberSignupRequestDto(email, nickname, "password", "서울"));
    }
}
