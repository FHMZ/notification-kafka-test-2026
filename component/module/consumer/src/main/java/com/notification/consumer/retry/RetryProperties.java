package com.notification.consumer.retry;

import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "app.consumer.retry")
public class RetryProperties {

    @Min(1)
    private int maxAttempts = 3;

    @Min(0)
    private long backoffMs = 300;
}
