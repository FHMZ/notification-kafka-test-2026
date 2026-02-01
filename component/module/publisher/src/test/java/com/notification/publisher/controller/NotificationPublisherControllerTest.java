package com.notification.publisher.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.notification.publisher.dto.EmailNotificationRequestDTO;
import com.notification.publisher.service.NotificationPublisherService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class NotificationPublisherControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private NotificationPublisherService publisherService;

    @BeforeEach
    void setup() {
        NotificationPublisherController controller = new NotificationPublisherController(publisherService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                // Se você tiver um Advice padrão de validation/errors, plugue aqui:
                // .setControllerAdvice(new PublisherControllerAdvice())
                .build();

        objectMapper = new ObjectMapper();
    }

    @Test
    void publishEmail_shouldReturn202_whenServiceSucceeds() throws Exception {
        when(publisherService.publishEmail(any(EmailNotificationRequestDTO.class)))
                .thenReturn("notification-id-123");

        var request = new EmailNotificationRequestDTO(
                "test@mail.com",
                "Hello",
                "Body content"
        );

        mockMvc.perform(post("/api/notifications/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.notificationId").value("notification-id-123"))
                .andExpect(jsonPath("$.status").value("ACCEPTED"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void publishEmail_shouldReturn400_whenInvalidRequest() throws Exception {
        // inválido: to não é email, subject/body vazios
        var request = new EmailNotificationRequestDTO(
                "not-an-email",
                "",
                ""
        );

        mockMvc.perform(post("/api/notifications/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

}
