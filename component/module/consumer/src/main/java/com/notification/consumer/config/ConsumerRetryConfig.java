package com.notification.consumer.config;

import com.notification.consumer.retry.RetryPolicy;
import com.notification.consumer.retry.RetryProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(RetryProperties.class)
public class ConsumerRetryConfig {

    @Bean
    public RetryPolicy retryPolicy(RetryProperties props) {
        return RetryPolicy.of(props.getMaxAttempts(), props.getBackoffMs());
    }
}
