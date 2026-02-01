package com.notification.consumer.retry;

public record RetryPolicy(
        int maxAttempts,
        long backoffMillis
) {
    public static RetryPolicy of(int maxAttempts, long backoffMillis) {
        return new RetryPolicy(maxAttempts, backoffMillis);
    }
}
