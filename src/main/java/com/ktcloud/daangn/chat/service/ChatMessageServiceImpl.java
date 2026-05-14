package com.ktcloud.daangn.chat.service;

import com.ktcloud.daangn.chat.dto.ChatMessageResponseDto;
import com.ktcloud.daangn.chat.entity.ChatMessage;
import com.ktcloud.daangn.chat.entity.ChatParticipant;
import com.ktcloud.daangn.chat.entity.ChatRoom;
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
public class ChatMessageServiceImpl implements ChatMessageService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final MemberService memberService;

    // 메시지 전송 처리
    @Override
    @Transactional
    public ChatMessageResponseDto create(Long roomId, Long memberId, String message) {
        ChatRoom chatRoom = findRoomByIdOrThrow(roomId);
        Member member = memberService.getByIdOrThrow(memberId);
        findParticipantByRoomIdAndMemberIdOrThrow(roomId, memberId);
        long readCount = Math.max(chatParticipantRepository.countByChatRoom_Id(roomId) - 1, 0);

        ChatMessage chatMessage = chatMessageRepository.save(ChatMessage.createMessage(chatRoom, member, message, readCount));

        return ChatMessageResponseDto.from(chatMessage);
    }

    // 채팅 메시지 목록 조회
    @Override
    public List<ChatMessageResponseDto> list(Long roomId, Long memberId) {
        findParticipantByRoomIdAndMemberIdOrThrow(roomId, memberId);

        return chatMessageRepository.findByChatRoom_IdOrderByIdAsc(roomId).stream()
                .map(ChatMessageResponseDto::from)
                .toList();
    }

    // 채팅 메시지 수정
    @Override
    @Transactional
    public ChatMessageResponseDto edit(Long messageId, Long memberId, String message) {
        ChatMessage chatMessage = findMessageByIdOrThrow(messageId);
        validateEditable(chatMessage, memberId);
        chatMessage.edit(message);

        return ChatMessageResponseDto.from(chatMessage);
    }

    // 채팅 메시지 삭제
    @Override
    @Transactional
    public ChatMessageResponseDto delete(Long messageId, Long memberId) {
        ChatMessage chatMessage = findMessageByIdOrThrow(messageId);
        validateEditable(chatMessage, memberId);
        chatMessage.delete();

        return ChatMessageResponseDto.from(chatMessage);
    }

    private ChatRoom findRoomByIdOrThrow(Long roomId) {
        return chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new InvalidInputException(HttpStatus.BAD_REQUEST.value(), "채팅방이 존재하지 않습니다."));
    }

    private ChatParticipant findParticipantByRoomIdAndMemberIdOrThrow(Long roomId, Long memberId) {
        return chatParticipantRepository.findByChatRoom_IdAndMember_Id(roomId, memberId)
                .orElseThrow(() -> new InvalidInputException(HttpStatus.BAD_REQUEST.value(), "채팅방 참여자가 아닙니다."));
    }

    private ChatMessage findMessageByIdOrThrow(Long messageId) {
        return chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new InvalidInputException(HttpStatus.BAD_REQUEST.value(), "메시지가 존재하지 않습니다."));
    }

    private void validateEditable(ChatMessage chatMessage, Long memberId) {
        if (!chatMessage.isWrittenBy(memberId)) {
            throw new InvalidInputException(HttpStatus.BAD_REQUEST.value(), "본인이 작성한 메시지만 수정 또는 삭제할 수 있습니다.");
        }
        if (chatMessage.isDeletedMessage()) {
            throw new InvalidInputException(HttpStatus.BAD_REQUEST.value(), "삭제된 메시지는 수정 또는 삭제할 수 없습니다.");
        }
    }
}
