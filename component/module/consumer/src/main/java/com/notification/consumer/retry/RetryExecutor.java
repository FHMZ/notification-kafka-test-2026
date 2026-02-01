package com.notification.consumer.retry;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RetryExecutor {

    @FunctionalInterface
    public interface ThrowingRunnable {
        void run() throws Exception;
    }

    public void execute(String operationName, RetryPolicy policy, ThrowingRunnable action) throws Exception {
        Exception last = null;

        for (int attempt = 1; attempt <= policy.maxAttempts(); attempt++) {
            try {
                if (attempt > 1) {
                    log.warn("Retry attempt {}/{} for {}", attempt, policy.maxAttempts(), operationName);
                }
                action.run();
                return;

            } catch (Exception ex) {
                last = ex;

                log.warn(
                        "Failed attempt {}/{} for {}: {}",
                        attempt,
                        policy.maxAttempts(),
                        operationName,
                        ex.getClass().getSimpleName() + ": " + (ex.getMessage() == null ? "" : ex.getMessage())
                );

                if (attempt < policy.maxAttempts()) {
                    sleep(policy.backoffMillis());
                }
            }
        }

        throw last;
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }
}
