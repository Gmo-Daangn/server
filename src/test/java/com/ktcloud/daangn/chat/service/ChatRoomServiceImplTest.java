package com.ktcloud.daangn.chat.service;

import com.ktcloud.daangn.chat.dto.*;
import com.ktcloud.daangn.common.exception.InvalidInputException;
import com.ktcloud.daangn.config.TestContainerConfig;
import com.ktcloud.daangn.member.entity.Member;
import com.ktcloud.daangn.member.entity.MemberRole;
import com.ktcloud.daangn.member.entity.ProviderToken;
import com.ktcloud.daangn.member.service.MemberService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

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
        Long senderId = saveMember("a");
        Long receiverId = saveMember("b");

        ChatRoomEnterRequestDto dto = new ChatRoomEnterRequestDto(senderId, receiverId, 100L);

        ChatRoomEnterResponseDto firstResponse = chatRoomService.enterDirectRoom(dto);
        ChatRoomEnterResponseDto secondResponse = chatRoomService.enterDirectRoom(dto);

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
                new ChatRoomEnterRequestDto(senderId, receiverId, 200L)
        );
        chatMessageService.create(room.roomId(), senderId, "hello");

        List<ChatRoomListResponseDto> rooms = chatRoomService.findDirectRooms(senderId);

        assertThat(rooms).hasSize(1);
        assertThat(rooms.getFirst().roomId()).isEqualTo(room.roomId());
        assertThat(rooms.getFirst().otherMemberId()).isEqualTo(receiverId);
        assertThat(rooms.getFirst().lastMessage()).isEqualTo("hello");
    }

    @Test
    @DisplayName("[HAPPY] 상대방 메시지를 읽으면 읽음 처리된다.")
    void readDirectRoom_marksUnreadMessagesAsRead() {
        Long senderId = saveMember("a");
        Long receiverId = saveMember("b");

        ChatRoomEnterResponseDto room = chatRoomService.enterDirectRoom(
                new ChatRoomEnterRequestDto(senderId, receiverId, 300L)
        );
        chatMessageService.create(room.roomId(), senderId, "first");
        chatMessageService.create(room.roomId(), senderId, "second");

        ChatRoomReadResponseDto response = chatRoomService.readDirectRoom(
                room.roomId(),
                new ChatRoomReadRequestDto(receiverId)
        );

        assertThat(response.roomId()).isEqualTo(room.roomId());
        assertThat(response.readMessageCount()).isEqualTo(2);
        assertThat(chatMessageService.list(room.roomId(), receiverId))
                .extracting(it -> it.unreadCount())
                .containsOnly(0L);
    }

    @Test
    @DisplayName("[Exception] 본인과의 1대1 채팅방은 생성할 수 없다.")
    void enterDirectRoom_throwsExceptionWhenTargetIsSelf() {
        Long memberId = saveMember("a");

        assertThatThrownBy(() -> chatRoomService.enterDirectRoom(
                new ChatRoomEnterRequestDto(memberId, memberId, 100L)
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
                new ChatRoomEnterRequestDto(senderId, receiverId, 300L)
        );
        chatMessageService.create(room.roomId(), senderId, "first");

        assertThatThrownBy(() -> chatRoomService.readDirectRoom(
                room.roomId(),
                new ChatRoomReadRequestDto(outsiderId)
        )).isInstanceOf(InvalidInputException.class)
                .hasMessage("채팅방 참여자가 아닙니다.");
    }

    private Long saveMember(String nickname) {
        String suffix = UUID.randomUUID().toString().replace("-", "");
        Member member = Member.builder()
                .email(nickname + "-" + suffix + "@test.com")
                .password("password")
                .nickName(nickname)
                .memberRole(MemberRole.MEMBER)
                .providerToken(ProviderToken.LOCAL)
                .createAt(LocalDateTime.now())
                .build();

        return memberService.register(member).getId();
    }
}
