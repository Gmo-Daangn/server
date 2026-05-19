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
        if (templateRepository.findByTemplateType(NotificationTemplateTypes.CHAT).isPresent()) {
            return;
        }
        templateRepository.save(NotificationTemplate.builder()
                .templateType(NotificationTemplateTypes.CHAT)
                .templateTitle("채팅")
                .templateText("새 채팅: {templateText}")
                .identifier(0L)
                .build());
    }
}
