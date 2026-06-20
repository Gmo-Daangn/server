package com.ktcloud.daangn.notification.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.notification.redis")
public record NotificationRedisProperties(String pubsubChannel) {
}
