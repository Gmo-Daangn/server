package com.ktcloud.daangn.notification.event;

import com.ktcloud.daangn.payment.dto.PaymentInitRequestDto;
import com.ktcloud.daangn.payment.dto.PaymentRequestDto;
import com.ktcloud.daangn.payment.dto.PaymentTokenDto;
import com.ktcloud.daangn.payment.event.DepositCompleteEvent;
import com.ktcloud.daangn.payment.event.PaymentCompleteEvent;
import com.ktcloud.daangn.payment.event.PaymentRequestEvent;
import com.ktcloud.daangn.post.entity.Post;
import com.ktcloud.daangn.post.service.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class PaymentEventPublishingAspect {

    private final ApplicationEventPublisher eventPublisher;
    private final PaymentNotificationContextResolver contextResolver;
    private final PostService postService;

    @AfterReturning("execution(* com.ktcloud.daangn.payment.service.PaymentService.deposit(..)) && args(dto)")
    public void publishDepositCompleteEvent(PaymentRequestDto dto) {
        eventPublisher.publishEvent(new DepositCompleteEvent(dto.memberId(), dto.tranAmt()));
    }

    @AfterReturning("execution(* com.ktcloud.daangn.payment.service.PaymentService.requestPayment(..)) && args(dto)")
    public void publishPaymentRequestEvents(PaymentInitRequestDto dto) {
        for (PaymentNotificationContextResolver.PaymentRequestTarget target
                : contextResolver.resolvePaymentRequestTargets(dto.postId())) {
            eventPublisher.publishEvent(new PaymentRequestEvent(
                    target.buyerMemberId(),
                    dto.amount(),
                    target.roomId()
            ));
        }
    }

    @AfterReturning("execution(* com.ktcloud.daangn.payment.service.PaymentService.confirmPayment(..)) && args(buyerMemberId, dto)")
    public void publishPaymentCompleteEvent(Long buyerMemberId, PaymentTokenDto dto) {
        Post post = postService.getPostOrThrow(dto.postId());
        Long sellerId = post.getMemberId();

        contextResolver.resolveChatRoomId(sellerId, buyerMemberId, dto.postId())
                .ifPresentOrElse(
                        roomId -> eventPublisher.publishEvent(new PaymentCompleteEvent(
                                sellerId,
                                dto.amount(),
                                roomId
                        )),
                        () -> log.warn(
                                "결제 완료 알림을 보낼 채팅방을 찾지 못했습니다. sellerId={}, buyerId={}, postId={}",
                                sellerId,
                                buyerMemberId,
                                dto.postId()
                        )
                );
    }
}
