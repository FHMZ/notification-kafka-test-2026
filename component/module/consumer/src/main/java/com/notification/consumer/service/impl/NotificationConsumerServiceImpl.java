package com.notification.consumer.service.impl;

import com.notification.consumer.model.NotificationMessage;
import com.notification.consumer.service.NotificationConsumerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class NotificationConsumerServiceImpl implements NotificationConsumerService {

    @Override
    public void process(NotificationMessage message) {
        // Aqui é o "business processing". No coding test basta demonstrar o pipeline.
        // Regra simples para simular falha e exercitar retry/dlq:
        if (message.subject() != null && message.subject().toLowerCase().contains("fail")) {
            throw new IllegalStateException("Simulated processing failure");
        }

        log.info("Notification processed: id={}, type={}, to={}", message.id(), message.type(), message.to());
    }
}
