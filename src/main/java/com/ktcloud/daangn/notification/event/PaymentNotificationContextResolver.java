package com.ktcloud.daangn.notification.event;

import com.ktcloud.daangn.chat.entity.ChatParticipant;
import com.ktcloud.daangn.chat.entity.ChatRoom;
import com.ktcloud.daangn.chat.entity.ChatType;
import com.ktcloud.daangn.chat.repository.ChatParticipantRepository;
import com.ktcloud.daangn.chat.repository.ChatRoomRepository;
import com.ktcloud.daangn.post.entity.Post;
import com.ktcloud.daangn.post.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PaymentNotificationContextResolver {

    private final PostService postService;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatParticipantRepository chatParticipantRepository;

    public Optional<Long> resolveChatRoomId(Long sellerMemberId, Long buyerMemberId, Long postId) {
        List<ChatRoom> rooms = chatRoomRepository.findExistingDirectRoom(
                sellerMemberId,
                buyerMemberId,
                postId,
                ChatType.PRODUCT
        );
        if (rooms.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(rooms.getFirst().getId());
    }

    public List<PaymentRequestTarget> resolvePaymentRequestTargets(Long postId) {
        Post post = postService.getPostOrThrow(postId);
        Long sellerId = post.getMemberId();

        return chatParticipantRepository.findByMember_Id(sellerId).stream()
                .map(ChatParticipant::getChatRoom)
                .filter(room -> ChatType.PRODUCT == room.getType())
                .filter(room -> postId.equals(room.getProductId()))
                .filter(room -> chatParticipantRepository.countByChatRoom_Id(room.getId()) == 2)
                .map(room -> toPaymentRequestTarget(room, sellerId))
                .flatMap(Optional::stream)
                .toList();
    }

    private Optional<PaymentRequestTarget> toPaymentRequestTarget(ChatRoom room, Long sellerId) {
        return chatParticipantRepository.findByChatRoom_Id(room.getId()).stream()
                .map(participant -> participant.getMember().getId())
                .filter(memberId -> !memberId.equals(sellerId))
                .findFirst()
                .map(buyerId -> new PaymentRequestTarget(buyerId, room.getId()));
    }

    public record PaymentRequestTarget(Long buyerMemberId, Long roomId) {
    }
}
