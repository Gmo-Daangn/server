package com.ktcloud.daangn.notification.event;

import com.ktcloud.daangn.common.valueObject.Address;
import com.ktcloud.daangn.chat.entity.ChatParticipant;
import com.ktcloud.daangn.chat.entity.ChatRoom;
import com.ktcloud.daangn.chat.entity.ChatType;
import com.ktcloud.daangn.chat.repository.ChatParticipantRepository;
import com.ktcloud.daangn.chat.repository.ChatRoomRepository;
import com.ktcloud.daangn.member.entity.Member;
import com.ktcloud.daangn.post.entity.Post;
import com.ktcloud.daangn.post.service.PostService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class PaymentNotificationContextResolverTest {

    private static final Long SELLER_ID = 1L;
    private static final Long BUYER_ID = 2L;
    private static final Long POST_ID = 1L;
    private static final Long ROOM_ID = 1L;

    @Mock
    PostService postService;

    @Mock
    ChatRoomRepository chatRoomRepository;

    @Mock
    ChatParticipantRepository chatParticipantRepository;

    @InjectMocks
    PaymentNotificationContextResolver resolver;

    Member seller;
    Member buyer;
    Post post;
    ChatRoom chatRoom;

    @BeforeEach
    void setUp() {
        seller = Member.builder().id(SELLER_ID).build();
        buyer = Member.builder().id(BUYER_ID).build();
        post = Post.builder()
                .member(seller)
                .title("title")
                .content("content")
                .price(5_000L)
                .location(new Address("서울시", "강남구", "청담동"))
                .build();
        ReflectionTestUtils.setField(post, "id", POST_ID);

        chatRoom = ChatRoom.builder()
                .productId(POST_ID)
                .type(ChatType.PRODUCT)
                .build();
        ReflectionTestUtils.setField(chatRoom, "id", ROOM_ID);
    }

    @Test
    @DisplayName("[HAPPY] 판매자와 구매자의 채팅방 ID를 조회한다")
    void resolveChatRoomId_returnsRoomId() {
        given(chatRoomRepository.findExistingDirectRoom(SELLER_ID, BUYER_ID, POST_ID, ChatType.PRODUCT))
                .willReturn(List.of(chatRoom));

        Optional<Long> roomId = resolver.resolveChatRoomId(SELLER_ID, BUYER_ID, POST_ID);

        assertThat(roomId).contains(ROOM_ID);
    }

    @Test
    @DisplayName("[HAPPY] 게시글과 연결된 1:1 채팅방에서 결제 요청 대상을 조회한다")
    void resolvePaymentRequestTargets_returnsBuyerAndRoomId() {
        ChatParticipant sellerParticipant = ChatParticipant.createParticipant(chatRoom, seller);
        ChatParticipant buyerParticipant = ChatParticipant.createParticipant(chatRoom, buyer);

        given(postService.getPostOrThrow(POST_ID)).willReturn(post);
        given(chatParticipantRepository.findByMember_Id(SELLER_ID)).willReturn(List.of(sellerParticipant));
        given(chatParticipantRepository.countByChatRoom_Id(ROOM_ID)).willReturn(2L);
        given(chatParticipantRepository.findByChatRoom_Id(ROOM_ID))
                .willReturn(List.of(sellerParticipant, buyerParticipant));

        List<PaymentNotificationContextResolver.PaymentRequestTarget> targets =
                resolver.resolvePaymentRequestTargets(POST_ID);

        assertThat(targets).hasSize(1);
        assertThat(targets.getFirst().buyerMemberId()).isEqualTo(BUYER_ID);
        assertThat(targets.getFirst().roomId()).isEqualTo(ROOM_ID);
    }
}
