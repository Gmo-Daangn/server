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
import com.ktcloud.daangn.member.repository.MemberDBRepositoryImpl;
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
    private final MemberDBRepositoryImpl memberRepository;

    // 채팅방 생성 또는 입장 처리
    @Override
    @Transactional
    public ChatRoomEnterResponseDto enterDirectRoom(ChatRoomEnterRequestDto dto) {
        if (dto.memberEmail().equals(dto.targetMemberEmail())) {
            throw new InvalidInputException(HttpStatus.BAD_REQUEST.value(), "본인과의 채팅방은 만들 수 없습니다.");
        }

        Member member = findMemberByEmailOrThrow(dto.memberEmail());
        Member targetMember = findMemberByEmailOrThrow(dto.targetMemberEmail());

        List<ChatRoom> existingRooms = chatRoomRepository.findExistingDirectRoom(
                dto.memberEmail(),
                dto.targetMemberEmail(),
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
    public List<ChatRoomListResponseDto> findDirectRooms(String memberEmail) {
        findMemberByEmailOrThrow(memberEmail);

        return chatParticipantRepository.findByMember_Email(memberEmail).stream()
                .map(ChatParticipant::getChatRoom)
                .distinct()
                .sorted(Comparator.comparing(ChatRoom::getCreatedAt).reversed())
                .map(chatRoom -> toRoomListResponse(chatRoom, memberEmail))
                .toList();
    }

    // 채팅방 메시지 읽음 처리
    @Override
    @Transactional
    public ChatRoomReadResponseDto readDirectRoom(Long roomId, ChatRoomReadRequestDto dto) {
        findParticipantByRoomIdAndEmailOrThrow(roomId, dto.memberEmail());

        List<ChatMessage> unreadMessages = chatMessageRepository.findByChatRoom_IdOrderByIdAsc(roomId).stream()
                .filter(message -> !message.getMember().getEmail().equals(dto.memberEmail()))
                .filter(message -> message.getReadCount() > 0)
                .toList();

        unreadMessages.forEach(ChatMessage::markRead);

        return new ChatRoomReadResponseDto(roomId, dto.memberEmail(), unreadMessages.size());
    }

    private ChatRoomEnterResponseDto createRoom(Long productId, Member member, Member targetMember) {
        ChatRoom chatRoom = chatRoomRepository.save(ChatRoom.createRoom(productId, ChatType.PRODUCT));
        chatParticipantRepository.save(ChatParticipant.createParticipant(chatRoom, member));
        chatParticipantRepository.save(ChatParticipant.createParticipant(chatRoom, targetMember));

        return new ChatRoomEnterResponseDto(chatRoom.getId(), true, "채팅방 생성 성공");
    }

    private Member findMemberByEmailOrThrow(String memberEmail) {
        return memberRepository.findByEmail(memberEmail)
                .orElseThrow(() -> new InvalidInputException(HttpStatus.BAD_REQUEST.value(), "회원이 존재하지 않습니다."));
    }

    private ChatParticipant findParticipantByRoomIdAndEmailOrThrow(Long roomId, String memberEmail) {
        return chatParticipantRepository.findByChatRoom_IdAndMember_Email(roomId, memberEmail)
                .orElseThrow(() -> new InvalidInputException(HttpStatus.BAD_REQUEST.value(), "채팅방 참여자가 아닙니다."));
    }

    private ChatRoomListResponseDto toRoomListResponse(ChatRoom chatRoom, String memberEmail) {
        List<ChatParticipant> participants = chatParticipantRepository.findByChatRoom_Id(chatRoom.getId());
        ChatParticipant participant = participants.stream()
                .filter(item -> item.getMember().getEmail().equals(memberEmail))
                .findFirst()
                .orElseThrow(() -> new InvalidInputException(HttpStatus.BAD_REQUEST.value(), "채팅방 참여자가 아닙니다."));

        ChatParticipant otherParticipant = participants.stream()
                .filter(item -> !item.getMember().getEmail().equals(memberEmail))
                .findFirst()
                .orElse(participant);

        ChatMessage lastMessage = chatMessageRepository.findTopByChatRoom_IdOrderByIdDesc(chatRoom.getId()).orElse(null);
        long unreadMessageCount = calculateUnreadMessageCount(chatRoom.getId(), memberEmail);

        return new ChatRoomListResponseDto(
                chatRoom.getId(),
                chatRoom.getProductId(),
                otherParticipant.getMember().getEmail(),
                otherParticipant.getMember().getNickName(),
                lastMessage == null ? null : lastMessage.getMessage(),
                lastMessage == null ? null : lastMessage.getCreatedAt(),
                unreadMessageCount
        );
    }

    private long calculateUnreadMessageCount(Long roomId, String memberEmail) {
        return chatMessageRepository.countByChatRoom_IdAndMember_EmailNotAndReadCountGreaterThan(roomId, memberEmail, 0);
    }
}
