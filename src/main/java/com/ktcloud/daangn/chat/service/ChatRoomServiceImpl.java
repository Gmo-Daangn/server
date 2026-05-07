package com.ktcloud.daangn.chat.service;

import com.ktcloud.daangn.chat.dto.*;
import com.ktcloud.daangn.chat.entity.ChatMessage;
import com.ktcloud.daangn.chat.entity.ChatParticipant;
import com.ktcloud.daangn.chat.entity.ChatRoom;
import com.ktcloud.daangn.chat.entity.ChatType;
import com.ktcloud.daangn.chat.repository.ChatMessageRepository;
import com.ktcloud.daangn.chat.repository.ChatParticipantRepository;
import com.ktcloud.daangn.chat.repository.ChatRoomRepository;
import com.ktcloud.daangn.config.exception.InvalidInputException;
import com.ktcloud.daangn.member.entity.Member;
import com.ktcloud.daangn.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
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
    public ChatRoomEnterResponseDto enterDirectRoom(ChatRoomEnterRequestDto dto) {
        if (dto.memberId().equals(dto.targetMemberId())) {
            throw new InvalidInputException(HttpStatus.BAD_REQUEST.value(), "본인과의 채팅방은 만들 수 없습니다.");
        }

        Member member = memberService.getByIdOrThrow(dto.memberId());
        Member targetMember = memberService.getByIdOrThrow(dto.targetMemberId());

        List<ChatRoom> existingRooms = chatRoomRepository.findExistingDirectRoom(
                dto.memberId(),
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

    // 내가 참여한 채팅방 목록 조회
    @Override
    public List<ChatRoomListResponseDto> findDirectRooms(Long memberId) {
        memberService.getByIdOrThrow(memberId);

        return chatParticipantRepository.findByMember_Id(memberId).stream()
                .map(ChatParticipant::getChatRoom)
                .distinct()
                .sorted(Comparator.comparing(ChatRoom::getCreatedAt).reversed())
                .map(chatRoom -> toRoomListResponse(chatRoom, memberId))
                .toList();
    }

    // 채팅방 메시지 읽음 처리
    @Override
    @Transactional
    public ChatRoomReadResponseDto readDirectRoom(Long roomId, ChatRoomReadRequestDto dto) {
        findParticipantByRoomIdAndMemberIdOrThrow(roomId, dto.memberId());

        List<ChatMessage> unreadMessages = chatMessageRepository.findByChatRoom_IdOrderByIdAsc(roomId).stream()
                .filter(message -> !message.getMember().getId().equals(dto.memberId()))
                .filter(message -> message.getReadCount() > 0)
                .toList();

        unreadMessages.forEach(ChatMessage::markRead);

        return new ChatRoomReadResponseDto(roomId, dto.memberId(), unreadMessages.size());
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

    private ChatRoomListResponseDto toRoomListResponse(ChatRoom chatRoom, Long memberId) {
        List<ChatParticipant> participants = chatParticipantRepository.findByChatRoom_Id(chatRoom.getId());
        ChatParticipant participant = participants.stream()
                .filter(item -> item.getMember().getId().equals(memberId))
                .findFirst()
                .orElseThrow(() -> new InvalidInputException(HttpStatus.BAD_REQUEST.value(), "채팅방 참여자가 아닙니다."));

        ChatParticipant otherParticipant = participants.stream()
                .filter(item -> !item.getMember().getId().equals(memberId))
                .findFirst()
                .orElse(participant);

        ChatMessage lastMessage = chatMessageRepository.findTopByChatRoom_IdOrderByIdDesc(chatRoom.getId()).orElse(null);
        long unreadMessageCount = calculateUnreadMessageCount(chatRoom.getId(), memberId);

        return new ChatRoomListResponseDto(
                chatRoom.getId(),
                chatRoom.getProductId(),
                otherParticipant.getMember().getId(),
                otherParticipant.getMember().getNickName(),
                lastMessage == null ? null : lastMessage.getMessage(),
                lastMessage == null ? null : lastMessage.getCreatedAt(),
                unreadMessageCount
        );
    }

    private long calculateUnreadMessageCount(Long roomId, Long memberId) {
        return chatMessageRepository.countByChatRoom_IdAndMember_IdNotAndReadCountGreaterThan(roomId, memberId, 0);
    }
}
