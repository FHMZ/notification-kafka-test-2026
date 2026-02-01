package com.notification.publisher.service;

import com.notification.publisher.dto.EmailNotificationRequestDTO;

public interface NotificationPublisherService {

    String publishEmail(EmailNotificationRequestDTO request);

}
