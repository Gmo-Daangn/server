package com.ktcloud.daangn.chat.service;

import com.ktcloud.daangn.chat.dto.ChatRoomEnterRequestDto;
import com.ktcloud.daangn.chat.dto.ChatRoomEnterResponseDto;
import com.ktcloud.daangn.chat.entity.ChatParticipant;
import com.ktcloud.daangn.chat.entity.ChatRoom;
import com.ktcloud.daangn.chat.entity.ChatType;
import com.ktcloud.daangn.chat.repository.ChatParticipantRepository;
import com.ktcloud.daangn.chat.repository.ChatRoomRepository;
import com.ktcloud.daangn.config.TestContainerConfig;
import com.ktcloud.daangn.member.entity.Member;
import com.ktcloud.daangn.member.entity.MemberRole;
import com.ktcloud.daangn.member.entity.ProviderToken;
import com.ktcloud.daangn.member.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.UUID;

abstract class ChatServiceTestSupport extends TestContainerConfig {

    @Autowired
    protected ChatRoomService chatRoomService;

    @Autowired
    protected ChatMessageService chatMessageService;

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Autowired
    private ChatParticipantRepository chatParticipantRepository;

    @Autowired
    private MemberService memberService;

    protected TestChatRoom createRoom() {
        Long senderId = saveMember("a");
        Long receiverId = saveMember("b");

        ChatRoomEnterResponseDto room = chatRoomService.enterDirectRoom(
                senderId,
                new ChatRoomEnterRequestDto(receiverId, 400L)
        );

        return new TestChatRoom(room.roomId(), senderId, receiverId);
    }

    protected TestChatRoom createSingleParticipantRoom() {
        Member member = saveMemberEntity("single");
        ChatRoom room = chatRoomRepository.save(ChatRoom.createRoom(500L, ChatType.PRODUCT));
        chatParticipantRepository.save(ChatParticipant.createParticipant(room, member));

        return new TestChatRoom(room.getId(), member.getId(), null);
    }

    protected Long createMultiRoom(Long... memberIds) {
        ChatRoom room = chatRoomRepository.save(ChatRoom.createRoom(600L, ChatType.MULTI));
        for (Long memberId : memberIds) {
            Member member = memberService.getByIdOrThrow(memberId);
            chatParticipantRepository.save(ChatParticipant.createParticipant(room, member));
        }

        return room.getId();
    }

    protected Long saveMember(String nickname) {
        return saveMemberEntity(nickname).getId();
    }

    private Member saveMemberEntity(String nickname) {
        String suffix = UUID.randomUUID().toString().replace("-", "");
        Member member = Member.builder()
                .email(nickname + "-" + suffix + "@test.com")
                .password("password")
                .nickName(nickname)
                .memberRole(MemberRole.MEMBER)
                .providerToken(ProviderToken.LOCAL)
                .createAt(LocalDateTime.now())
                .build();

        return memberService.register(member);
    }

    protected record TestChatRoom(
            Long roomId,
            Long senderId,
            Long receiverId
    ) {
    }
}
