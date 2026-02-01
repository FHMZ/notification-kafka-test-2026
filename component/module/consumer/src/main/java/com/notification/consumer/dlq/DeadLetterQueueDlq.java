package com.notification.consumer.dlq;

import com.notification.consumer.model.NotificationMessage;

public interface DeadLetterQueueDlq {

    void send(NotificationMessage message, String reason);

}
