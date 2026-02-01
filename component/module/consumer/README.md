# Consumer Module

## Overview

The **Consumer module** is responsible for asynchronously processing notification messages published by the Publisher module.

It represents the **core of the asynchronous workflow**, handling message consumption, business processing, retry logic, and dead-letter handling (DLQ).

This module was designed to be **resilient, fault-tolerant, decoupled, and fully testable**, following event-driven and clean architecture principles.

---

## Responsibilities

- Consume notification messages asynchronously
- Process business logic for each notification
- Apply retry strategy with controlled backoff
- Route failed messages to a Dead Letter Queue (DLQ)
- Ensure reliability and fault tolerance
- Decouple business logic from messaging infrastructure

---

## Functional Flow

1. A notification message is consumed from the messaging channel
2. The message is forwarded to the processing service
3. If processing succeeds:
    - The message is acknowledged
4. If processing fails:
    - Retry logic is applied
    - The message is retried according to configured policy
5. If all retries are exhausted:
    - The message is routed to the Dead Letter Queue (DLQ)
    - Processing continues for the next messages

---

## Architectural Design

### Core Components

- **Listener**
    - Consumes messages from the message channel
    - Forwards messages to the processing pipeline

- **Processor**
    - Implements the business logic of notification handling

- **Retry Handler**
    - Controls retry attempts and backoff strategy

- **DLQ Handler**
    - Persists or stores messages that permanently fail processing

---

## Package Structure

```text
consumer
├── listener
│ └── InMemoryNotificationConsumer.java
│
├── service
│ ├── NotificationProcessorService.java
│ └── impl/NotificationProcessorServiceImpl.java
│
├── retry
│ └── RetryExecutor.java
│
├── dlq
│ └── DeadLetterQueueStore.java
│
└── model
  └── NotificationMessage.java
```

---

## Key Design Decisions

- Asynchronous message-driven processing
- Explicit retry handling
- Dead Letter Queue (DLQ) for fault isolation
- Port-based abstraction for infrastructure independence
- Clear separation of concerns
- Deterministic processing pipeline

---


