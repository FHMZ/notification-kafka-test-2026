package com.notification.publisher.adapter;

import com.notification.publisher.model.NotificationMessage;

public interface NotificationPublisherAdapter {

    void publish(NotificationMessage message);

}
