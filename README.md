# Notification Service

This project implements a **Notification Service** using a **Modular Monolith architecture**, designed to demonstrate clean architecture principles, message-driven communication, and production-ready patterns in a controlled technical test scenario.

---

## 🧱 Architecture Overview

The system is structured as a **Modular Monolith**, composed of clearly isolated modules that communicate via well-defined contracts.

### Why Modular Monolith?

For this test, a Modular Monolith was chosen over microservices to:

- Reduce operational complexity (single deployable unit)
- Keep strong modular boundaries (clear ownership and responsibilities)
- Enable easier testing and debugging
- Allow future extraction to microservices with minimal refactoring

This approach balances **simplicity** and **scalability**, which is ideal for technical evaluations.

---

## 🧩 Modules

### 1. Auth Module
Responsible for authentication and authorization.

- Login with email/password
- JWT generation and validation
- Stateless security
- Clear separation between controller, service, and security layers

---

### 2. Publisher Module
Responsible for accepting notification requests and publishing them asynchronously.

**Responsibilities:**
- Expose HTTP API for notification requests
- Validate input
- Publish messages to Kafka
- Return `202 Accepted` for async processing

**Key characteristics:**
- Port & Adapter pattern
- Kafka producer abstraction
- No business logic tied to Kafka directly

---

### 3. Consumer Module
Responsible for consuming and processing notifications.

**Responsibilities:**
- Consume messages from Kafka
- Deserialize payloads
- Apply retry policy
- Send messages to Dead Letter Queue (DLQ) on failure

**Implemented patterns:**
- Retry Executor
- Configurable Retry Policy
- Dead Letter Queue (DLQ) backed by Kafka

---

## 🔁 Message Flow

```
HTTP Request
  ↓
Publisher API
  ↓
Kafka Topic (notifications.email)
  ↓
Consumer Listener
  ↓
Processing Service
  ↓
Success OR Retry → DLQ
```

---

## 🔐 Security

- JWT-based authentication
- Stateless security
- Only `/auth/login` is publicly accessible
- Notification endpoint requires a valid JWT token

---

## 🧪 Testing Strategy

The project includes **unit tests for all critical layers**:

### Publisher
- Controller tests (HTTP + validation)
- Service tests (business logic)

### Consumer
- Listener tests (JSON parsing, retry, DLQ routing)
- Service tests (processing logic)
- Retry behavior fully covered

Kafka infrastructure is intentionally abstracted and not mocked at the business layer.

---

## 🚀 Running the Application

### Requirements
- Docker
- Docker Compose
- Java 17+

---

## Clone the Repository (Use Main Branch)

```bash
git clone https://github.com/FHMZ/notification-kafka-test-2026.git
```

---

### 1️⃣ Start Kafka Infrastructure

From the project root:

```bash
docker compose up -d
```

This starts:
- Zookeeper
- Kafka Broker (with persistent volumes)
- Kafka UI

Kafka UI:
```
http://localhost:8085
```

Kafka data is stored in Docker volumes to ensure **messages are not lost on broker restarts**.

---

### 2️⃣ Run the Application

#### Option A – IDE
Run the Spring Boot application via your IDE.

#### Option B – Command Line
```bash
./gradlew :component:app:api-notification:bootRun
```

---

### 3️⃣ Verify Kafka Consumption

In Kafka UI:
- Navigate to **Consumers**
- Check consumer group `notification-service`
- Expected state:
    - STABLE
    - Members = 1
    - Lag = 0

---

## ♻️ Durability, Retry & DLQ

### Durability
- Kafka persists messages to disk
- Broker data is stored in Docker volumes
- Messages survive broker restarts

### Retry
- Retry logic is implemented at the consumer level
- Maximum attempts and backoff are configurable via `application.yml`

### Dead Letter Queue (DLQ)
- Implemented as a dedicated Kafka topic: `notifications.email.dlq`
- Messages are routed to DLQ after exhausting retries
- DLQ messages include:
    - Failure reason
    - Timestamp
    - Original message payload

---

## 📡 API Usage

### Authentication
```http
POST /auth/login
```

### Send Email Notification
```http
POST /api/notifications/email
Authorization: Bearer <JWT>
```

Returns:
```
202 Accepted
```

---

## 📦 Repository & Delivery

This project is delivered via **GitHub**.

The repository includes:
- Full source code
- Docker Compose configuration
- Unit tests
- `README.md` and `HELP.md` documentation

To evaluate the solution:
1. Clone the repository
2. Start Kafka using Docker Compose
3. Run the application
4. Use the documented endpoints to publish notifications
5. Observe retries and DLQ behavior via logs and Kafka UI

---

## 📌 Key Design Decisions

- Modular Monolith instead of Microservices
- Kafka abstraction via ports/adapters
- Explicit retry and DLQ handling
- No framework leakage into business logic
- Focus on testability and clarity

---

## ✅ Result

The system demonstrates:
- Clean architecture
- Asynchronous messaging
- Fault tolerance with retry + DLQ
- Clear durability guarantees
- Production-ready structure

**Designed specifically to meet senior-level backend technical test requirements.**
