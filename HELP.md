# HELP.md — Notification Service

This document provides **operational notes, troubleshooting tips, and evaluation hints**
to help reviewers and developers understand and validate the system behavior quickly.

---

## 🔐 Authentication Help

### Login
```
POST /auth/login
```

Example payload:
```json
{
  "username": "admin@company.com",
  "password": "admin123"
}
```

Response:
```json
{
  "token": "<JWT>"
}
```

Use this token as:
```
Authorization: Bearer <JWT>
```

Only `/auth/login` is public.  
All notification endpoints require JWT.

---

## 📤 Publisher Behavior

- The API **never sends emails directly**
- Requests are validated before publishing
- On success, the API returns **202 Accepted**
- Publishing is **asynchronous**

If validation fails:
- HTTP `400 Bad Request`
- Nothing is published to Kafka

This behavior is intentional and required by the test.

---

## 📥 Consumer Behavior

### Happy Path
- Message is consumed
- Payload is deserialized
- Notification is processed
- Success is logged

### Processing Failure
- Retry is applied
- Each retry attempt is logged
- After max retries → message sent to DLQ

Retry configuration:
```yaml
app:
  consumer:
    retry:
      max-attempts: 3
      backoff-ms: 1000
```

---

## ☠️ Dead Letter Queue (DLQ)

### DLQ Topic
```
notifications.email.dlq
```

Messages are sent to DLQ when:
- JSON payload is invalid
- Processing fails after max retries

DLQ messages contain:
- Failure reason
- Timestamp
- Original message payload

This allows safe inspection, replay, or auditing.

---

## 🧪 How to Trigger DLQ Scenarios

### 1️⃣ Invalid JSON Payload
Use Kafka UI → Produce Message:
- Topic: `notifications.email`
- Value:
```
1
```

Result:
- Consumer fails to deserialize
- Message routed to `notifications.email.dlq`

---

### 2️⃣ Processing Failure (Retry + DLQ)
Send a valid request with:
```json
{
  "to": "user@example.com",
  "subject": "FAIL",
  "body": "test"
}
```

Result:
- Retry attempts logged
- Permanent failure logged
- Message sent to DLQ

---

## ⚠️ Common Warnings Explained

### `LEADER_NOT_AVAILABLE`
This warning may appear briefly when:
- A topic is auto-created
- Kafka metadata is still propagating

It is expected in local Docker environments and **not an error**.

---

## 📊 Kafka UI Tips

Kafka UI:
```
http://localhost:8085
```

Useful checks:
- Topics → `notifications.email`
- Topics → `notifications.email.dlq`
- Consumers → `notification-service` (state: STABLE)

---

## 🧠 Design Notes (For Reviewers)

- Modular Monolith chosen intentionally for simplicity
- Kafka used with idempotent producer and `acks=all`
- Retry logic isolated from business logic
- DLQ implemented in Kafka (not in-memory)
- Code optimized for clarity and testability

---

## ✅ Final Notes

This system is intentionally scoped to:
- Demonstrate architectural maturity
- Show correct async messaging patterns
- Avoid unnecessary infrastructure complexity

For production, this design can be extended with:
- Schema Registry
- Message replay tooling
- Metrics & tracing
- Horizontal consumer scaling
