package com.ktcloud.daangn.chat.service;

import com.ktcloud.daangn.chat.dto.ChatMessageRequestDto;
import com.ktcloud.daangn.chat.dto.ChatMessageResponseDto;
import com.ktcloud.daangn.chat.dto.ChatRoomEnterRequestDto;
import com.ktcloud.daangn.chat.dto.ChatRoomEnterResponseDto;
import com.ktcloud.daangn.chat.entity.ChatMessage;
import com.ktcloud.daangn.chat.entity.ChatParticipant;
import com.ktcloud.daangn.chat.entity.ChatRoom;
import com.ktcloud.daangn.chat.entity.ChatType;
import com.ktcloud.daangn.chat.repository.ChatMessageRepository;
import com.ktcloud.daangn.chat.repository.ChatParticipantRepository;
import com.ktcloud.daangn.chat.repository.ChatRoomRepository;
import com.ktcloud.daangn.member.entity.Member;
import com.ktcloud.daangn.member.repository.MemberDBRepositoryImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatServiceImpl implements ChatService {
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final MemberDBRepositoryImpl memberDBRepository;
    private final ChatParticipantRepository chatParticipantRepository;

    @Transactional
    @Override
    public ChatRoomEnterResponseDto CreateRoom(ChatRoomEnterRequestDto dto) {
        Member member = memberDBRepository.findByEmail(dto.memberEmail()).orElse(null);
        Member targetMember = memberDBRepository.findByEmail(dto.targetMemberEmail()).orElse(null);
        ChatRoom chatRoom = ChatRoom.createRoom(dto.productId(), ChatType.PRODUCT);
        chatRoomRepository.save(chatRoom);
        chatParticipantRepository.save(ChatParticipant.createParticipant(chatRoom, targetMember));
        chatParticipantRepository.save(ChatParticipant.createParticipant(chatRoom, member));
        return new ChatRoomEnterResponseDto(chatRoom.getId(), "입장 성공");
    }

    @Override
    public ChatRoom EnterRoom(ChatRoomEnterRequestDto dto) {

        return null;
    }

    @Override
    public ChatMessageResponseDto SendChat(ChatMessageRequestDto dto) {

        ChatRoom chatRoom = chatRoomRepository.findById(dto.roomId()).orElse(null);
        Member member = memberDBRepository.findByEmail(dto.memberEmail()).orElse(null);
        chatMessageRepository.save(ChatMessage.createMessage(chatRoom, member, dto.message(), 3L));
        return new ChatMessageResponseDto("메시지 전송 성공");
    }

    @Override
    public List<ChatRoom> GetRoomList(String memberEmail) {
        Member member = memberDBRepository.findByEmail(memberEmail).orElse(null);
        return List.of();
    }
}
