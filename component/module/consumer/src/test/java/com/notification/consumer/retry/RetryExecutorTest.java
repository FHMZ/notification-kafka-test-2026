package com.notification.consumer.retry;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class RetryExecutorTest {

    private RetryExecutor retryExecutor;

    @BeforeEach
    void setup() {
        retryExecutor = new RetryExecutor();
    }

    @Test
    void execute_shouldRetryUntilSuccess() throws Exception {
        AtomicInteger attempts = new AtomicInteger(0);

        retryExecutor.execute(
                "test-operation",
                RetryPolicy.of(3, 0),
                () -> {
                    if (attempts.incrementAndGet() < 3) {
                        throw new RuntimeException("fail");
                    }
                }
        );

        // 2 falhas + 1 sucesso
        assertEquals(3, attempts.get());
    }

    @Test
    void execute_shouldThrowAfterMaxAttempts() {
        AtomicInteger attempts = new AtomicInteger(0);

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> retryExecutor.execute(
                        "test-operation",
                        RetryPolicy.of(3, 0),
                        () -> {
                            attempts.incrementAndGet();
                            throw new RuntimeException("always fails");
                        }
                )
        );

        assertEquals(3, attempts.get());
        assertEquals("always fails", ex.getMessage());
    }

}
