package com.ktcloud.daangn.chat.service;

import com.ktcloud.daangn.chat.dto.*;
import com.ktcloud.daangn.chat.entity.ChatParticipant;
import com.ktcloud.daangn.chat.entity.ChatRoom;
import com.ktcloud.daangn.chat.entity.ChatType;
import com.ktcloud.daangn.chat.repository.ChatMessageRepository;
import com.ktcloud.daangn.chat.repository.ChatParticipantRepository;
import com.ktcloud.daangn.chat.repository.ChatRoomRepository;
import com.ktcloud.daangn.common.exception.InvalidInputException;
import com.ktcloud.daangn.member.entity.Member;
import com.ktcloud.daangn.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatRoomServiceImpl implements ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final MemberService memberService;

    // 채팅방 생성 또는 입장 처리
    @Override
    @Transactional
    public ChatRoomEnterResponseDto enterDirectRoom(Long memberId, ChatRoomEnterRequestDto dto) {
        if (memberId.equals(dto.targetMemberId())) {
            throw new InvalidInputException(HttpStatus.BAD_REQUEST.value(), "본인과의 채팅방은 만들 수 없습니다.");
        }

        Member member = memberService.getByIdOrThrow(memberId);
        Member targetMember = memberService.getByIdOrThrow(dto.targetMemberId());

        List<ChatRoom> existingRooms = chatRoomRepository.findExistingDirectRoom(
                memberId,
                dto.targetMemberId(),
                dto.productId(),
                ChatType.PRODUCT
        );

        if (!existingRooms.isEmpty()) {
            ChatRoom chatRoom = existingRooms.getFirst();
            return new ChatRoomEnterResponseDto(chatRoom.getId(), false, "입장 성공");
        }

        return createRoom(dto.productId(), member, targetMember);
    }

    @Override
    public List<ChatRoomListResponseDto> findDirectRooms(Long memberId) {
        memberService.getByIdOrThrow(memberId);

        return chatParticipantRepository.findDirectRoomListByMemberId(memberId);
    }

    // 채팅방 메시지 읽음 처리
    @Override
    @Transactional
    public ChatRoomReadResponseDto readDirectRoom(Long roomId, Long memberId) {
        ChatParticipant participant = findParticipantByRoomIdAndMemberIdOrThrow(roomId, memberId);
        Long latestMessageId = chatMessageRepository.findLatestMessageIdByRoomId(roomId).orElse(null);
        long readMessageCount = participant.getUnreadCount();

        participant.markRead(latestMessageId);

        return new ChatRoomReadResponseDto(roomId, memberId, readMessageCount);
    }

    private ChatRoomEnterResponseDto createRoom(Long productId, Member member, Member targetMember) {
        ChatRoom chatRoom = chatRoomRepository.save(ChatRoom.createRoom(productId, ChatType.PRODUCT));
        chatParticipantRepository.save(ChatParticipant.createParticipant(chatRoom, member));
        chatParticipantRepository.save(ChatParticipant.createParticipant(chatRoom, targetMember));

        return new ChatRoomEnterResponseDto(chatRoom.getId(), true, "채팅방 생성 성공");
    }

    private ChatParticipant findParticipantByRoomIdAndMemberIdOrThrow(Long roomId, Long memberId) {
        return chatParticipantRepository.findByChatRoom_IdAndMember_Id(roomId, memberId)
                .orElseThrow(() -> new InvalidInputException(HttpStatus.BAD_REQUEST.value(), "채팅방 참여자가 아닙니다."));
    }

}
