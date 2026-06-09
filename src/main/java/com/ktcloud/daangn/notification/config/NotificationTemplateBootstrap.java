package com.ktcloud.daangn.notification.config;

import com.ktcloud.daangn.notification.entity.NotificationTemplate;
import com.ktcloud.daangn.notification.repository.NotificationTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationTemplateBootstrap implements ApplicationRunner {

    private final NotificationTemplateRepository templateRepository;

    @Override
    public void run(ApplicationArguments args) {
        createTemplateIfNotFound(NotificationTemplateTypes.CHAT, "채팅", "새 채팅: {templateText}");
        createTemplateIfNotFound(NotificationTemplateTypes.DEPOSIT_COMPLETE, "당근페이 충전", "당근페이에 {templateText}원이 충전되었습니다.");
        createTemplateIfNotFound(NotificationTemplateTypes.PAYMENT_REQUEST, "결제 요청", "판매자님이 {templateText}원 송금을 요청했어요.");
        createTemplateIfNotFound(NotificationTemplateTypes.PAYMENT_COMPLETE, "결제 완료", "구매자님이 {templateText}원을 송금했어요. 확인 후 물품을 전달해 주세요.");
    }

    private void createTemplateIfNotFound(String type, String title, String text) {
        if (templateRepository.findByTemplateType(type).isEmpty()) {
            templateRepository.save(NotificationTemplate.builder()
                    .templateType(type)
                    .templateTitle(title)
                    .templateText(text)
                    .identifier(0L)
                    .build());
        }
    }
}