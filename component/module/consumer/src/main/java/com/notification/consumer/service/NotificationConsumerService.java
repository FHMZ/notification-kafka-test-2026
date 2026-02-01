package com.notification.consumer.service;

import com.notification.consumer.model.NotificationMessage;

public interface NotificationConsumerService {

    void process(NotificationMessage message);

}
