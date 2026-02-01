package com.notification.publisher.controller;

import com.notification.publisher.dto.EmailNotificationRequestDTO;
import com.notification.publisher.dto.PublishAcceptedResponseDTO;
import com.notification.publisher.service.NotificationPublisherService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
public class NotificationPublisherController {

    private final NotificationPublisherService publishService;

    public NotificationPublisherController(NotificationPublisherService publishService) {
        this.publishService = publishService;
    }

    @PostMapping("/email")
    public ResponseEntity<PublishAcceptedResponseDTO> publishEmail(
            @RequestBody @Valid EmailNotificationRequestDTO request
    ) {
        String id = publishService.publishEmail(request);
        return ResponseEntity.accepted().body(PublishAcceptedResponseDTO.accepted(id));
    }
}
