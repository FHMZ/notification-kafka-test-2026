# Publisher Module

## Overview

The **Publisher module** is responsible for receiving notification requests via REST APIs and publishing them asynchronously for further processing.

It represents the **entry point of the asynchronous workflow**, ensuring that requests are validated, accepted, and dispatched without blocking the client.

This module is designed to be **highly decoupled, testable, and infrastructure-agnostic**, allowing seamless transition from in-memory messaging to Kafka-based messaging.

---

## Responsibilities

- Expose REST API for notification requests
- Validate incoming requests
- Build notification messages
- Publish messages asynchronously
- Return immediate acknowledgment (HTTP 202)
- Maintain strict separation between API and message transport

---

## Functional Flow

1. Client sends a request to create a notification
2. Request is validated
3. A notification message is built
4. Message is published to the messaging port
5. API returns **202 Accepted** immediately
6. Processing continues asynchronously

---

## Exposed Endpoint

| Method | Endpoint                  | Description                    |
|----------|--------------------------|--------------------------------|
| POST     | `/api/notifications/email` | Publishes an email notification |

---

## Architectural Design

The Publisher follows **Hexagonal Architecture (Ports & Adapters)** principles:

- Controllers handle HTTP communication
- Services implement business orchestration
- Ports define contracts for message publishing
- Adapters implement message transport mechanisms

This ensures:
- Low coupling
- High testability
- Easy replacement of infrastructure layers (Kafka, RabbitMQ, etc.)

---

## Package Structure

```text
publisher
├── controller
│ └── NotificationPublisherController.java
│
├── dto
│ ├── EmailNotificationRequestDTO.java
│ └── PublishAcceptedResponseDTO.java
│
├── model
│ └── NotificationMessage.java
│
├── port
│ └── NotificationPublisherPort.java
│
├── adapter
│ └── InMemoryNotificationPublisherAdapter.java
│
└── service
  ├── NotificationPublisherService.java
  └── impl/NotificationPublisherServiceImpl.java
```

---

## Key Design Decisions

- Immediate response with HTTP 202 for asynchronous behavior
- Strong request validation
- Message-based architecture
- Port-based decoupling from messaging infrastructure
- Clear separation between API, business logic, and transport
