package com.ktcloud.daangn.chat.service;

import com.ktcloud.daangn.chat.dto.ChatMessageResponseDto;
import com.ktcloud.daangn.chat.entity.ChatMessage;
import com.ktcloud.daangn.chat.entity.ChatParticipant;
import com.ktcloud.daangn.chat.entity.ChatRoom;
import com.ktcloud.daangn.chat.repository.ChatMessageRepository;
import com.ktcloud.daangn.chat.repository.ChatParticipantRepository;
import com.ktcloud.daangn.chat.repository.ChatRoomRepository;
import com.ktcloud.daangn.common.exception.InvalidInputException;
import com.ktcloud.daangn.chat.event.ChatMessageSentEvent;
import com.ktcloud.daangn.member.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatMessageServiceImpl implements ChatMessageService {

    private static final int MAX_SEARCH_SIZE = 100;

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final ApplicationEventPublisher eventPublisher;

    // 메시지 전송 처리
    @Override
    @Transactional
    public ChatMessageResponseDto create(Long roomId, Long memberId, String message) {
        ChatRoom chatRoom = findRoomByIdOrThrow(roomId);
        List<ChatParticipant> participants = findParticipantsByRoomIdForUpdate(roomId);
        ChatParticipant senderParticipant = findParticipantOrThrow(participants, memberId);
        Member member = senderParticipant.getMember();
        long unreadCount = countReceivers(participants, memberId);

        ChatMessage chatMessage = chatMessageRepository.save(ChatMessage.createMessage(chatRoom, member, message));
        chatRoom.updateLastMessage(chatMessage);
        senderParticipant.markRead(chatMessage.getId());
        participants.stream()
                .filter(participant -> participant.isNotMember(memberId))
                .forEach(ChatParticipant::increaseUnreadCount);

        publishMessageSent(roomId, memberId, chatMessage, message, participants);

        return ChatMessageResponseDto.from(chatMessage, unreadCount);
    }

    // 채팅 메시지 목록 조회
    @Override
    public List<ChatMessageResponseDto> list(Long roomId, Long memberId) {
        List<ChatParticipant> participants = findParticipantsByRoomId(roomId);
        findParticipantOrThrow(participants, memberId);

        return chatMessageRepository.findByChatRoomIdWithRoomAndMemberOrderByIdAsc(roomId).stream()
                .map(chatMessage -> ChatMessageResponseDto.from(
                        chatMessage,
                        calculateUnreadCount(chatMessage, participants)
                ))
                .toList();
    }

    // 채팅방 메시지 검색
    @Override
    public List<ChatMessageResponseDto> search(Long roomId, Long memberId, String keyword, Long beforeMessageId, int size) {
        String normalizedKeyword = validateKeyword(keyword);
        int normalizedSize = validateSearchSize(size);
        List<ChatParticipant> participants = findParticipantsByRoomId(roomId);
        findParticipantOrThrow(participants, memberId);

        return chatMessageRepository.findMessagesByKeyword(
                        roomId,
                        normalizedKeyword,
                        beforeMessageId,
                        PageRequest.of(0, normalizedSize)
                ).stream()
                .map(chatMessage -> ChatMessageResponseDto.from(
                        chatMessage,
                        calculateUnreadCount(chatMessage, participants)
                ))
                .toList();
    }

    // 채팅 메시지 수정
    @Override
    @Transactional
    public ChatMessageResponseDto edit(Long messageId, Long memberId, String message) {
        ChatMessage chatMessage = findMessageByIdOrThrow(messageId);
        validateEditable(chatMessage, memberId);
        chatMessage.edit(message);
        updateRoomLastMessageIfNeeded(chatMessage);

        return ChatMessageResponseDto.from(
                chatMessage,
                calculateUnreadCount(chatMessage, findParticipantsByRoomId(chatMessage.getChatRoom().getId()))
        );
    }

    // 채팅 메시지 삭제
    @Override
    @Transactional
    public ChatMessageResponseDto delete(Long messageId, Long memberId) {
        ChatMessage chatMessage = findMessageByIdOrThrow(messageId);
        validateEditable(chatMessage, memberId);
        chatMessage.delete();
        updateRoomLastMessageIfNeeded(chatMessage);

        return ChatMessageResponseDto.from(
                chatMessage,
                calculateUnreadCount(chatMessage, findParticipantsByRoomId(chatMessage.getChatRoom().getId()))
        );
    }

    private ChatRoom findRoomByIdOrThrow(Long roomId) {
        return chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new InvalidInputException(HttpStatus.BAD_REQUEST.value(), "채팅방이 존재하지 않습니다."));
    }

    private ChatMessage findMessageByIdOrThrow(Long messageId) {
        return chatMessageRepository.findByIdWithRoomAndMember(messageId)
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

    private List<ChatParticipant> findParticipantsByRoomId(Long roomId) {
        return chatParticipantRepository.findByChatRoomIdWithMember(roomId);
    }

    private List<ChatParticipant> findParticipantsByRoomIdForUpdate(Long roomId) {
        return chatParticipantRepository.findByChatRoomIdWithMemberForUpdate(roomId);
    }

    private ChatParticipant findParticipantOrThrow(List<ChatParticipant> participants, Long memberId) {
        return participants.stream()
                .filter(participant -> participant.isMember(memberId))
                .findFirst()
                .orElseThrow(() -> new InvalidInputException(HttpStatus.BAD_REQUEST.value(), "채팅방 참여자가 아닙니다."));
    }

    private long calculateUnreadCount(ChatMessage chatMessage, List<ChatParticipant> participants) {
        return participants.stream()
                .filter(participant -> participant.isNotMember(chatMessage.getMember().getId()))
                .filter(participant -> participant.getLastReadMessageId() == null
                        || participant.getLastReadMessageId() < chatMessage.getId())
                .count();
    }

    private long countReceivers(List<ChatParticipant> participants, Long senderId) {
        return participants.stream()
                .filter(participant -> participant.isNotMember(senderId))
                .count();
    }

    private String validateKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            throw new InvalidInputException(HttpStatus.BAD_REQUEST.value(), "검색어는 비어 있을 수 없습니다.");
        }

        return keyword.trim()
                .replace("!", "!!")
                .replace("%", "!%")
                .replace("_", "!_");
    }

    private int validateSearchSize(int size) {
        if (size < 1 || size > MAX_SEARCH_SIZE) {
            throw new InvalidInputException(HttpStatus.BAD_REQUEST.value(), "검색 개수는 1 이상 100 이하로 입력해주세요.");
        }

        return size;
    }

    private void updateRoomLastMessageIfNeeded(ChatMessage chatMessage) {
        ChatRoom chatRoom = chatMessage.getChatRoom();
        if (chatRoom.isLastMessage(chatMessage.getId())) {
            chatRoom.updateLastMessage(chatMessage);
        }
    }

    // 채팅 메시지 저장 후 발생하는 이벤트 발행
    private void publishMessageSent(
            Long chatRoomId,
            Long senderId,
            ChatMessage chatMessage,
            String message,
            List<ChatParticipant> participants
    ) {
        List<Long> receiverIds = participants.stream()
                .map(participant -> participant.getMember().getId())
                .filter(id -> !id.equals(senderId))
                .toList();

        if (receiverIds.isEmpty()) {
            return;
        }
        
        Long messageId = chatMessage.getId();
        String messagePreview = truncatePreview(message, 120);

        eventPublisher.publishEvent(new ChatMessageSentEvent(
                chatRoomId,
                senderId,
                messageId,
                messagePreview,
                receiverIds));
    }

    // 메시지 미리보기
    private static String truncatePreview(String message, int maxLen) {
        if (message == null) {
            return "";
        }
        if (message.length() <= maxLen) {
            return message;
        }
        return message.substring(0, maxLen) + "…";
    }
}
