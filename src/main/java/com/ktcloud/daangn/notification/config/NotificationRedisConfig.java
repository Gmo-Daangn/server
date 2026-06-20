package com.ktcloud.daangn.notification.config;

import com.ktcloud.daangn.notification.redis.NotificationSseRedisSubscriber;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

@Configuration
@Profile({"dev", "prod"})
@RequiredArgsConstructor
@EnableConfigurationProperties(NotificationRedisProperties.class)
public class NotificationRedisConfig {

    private final NotificationRedisProperties properties;
    private final NotificationSseRedisSubscriber subscriber;

    @Bean
    RedisMessageListenerContainer notificationRedisMessageListenerContainer(
            RedisConnectionFactory connectionFactory) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(subscriber, new ChannelTopic(properties.pubsubChannel()));
        return container;
    }
}
