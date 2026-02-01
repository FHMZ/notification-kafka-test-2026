# Auth Module

## Overview

The **Auth module** is responsible for handling authentication and authorization for the Notification Service platform.
Its main goal is to provide a **secure, lightweight, and stateless authentication mechanism** using **JWT (JSON Web Tokens)**, protecting all business endpoints without introducing unnecessary complexity or infrastructure dependencies.
This module was designed to be **simple, decoupled, testable, and production-ready**, following a clean modular architecture.

---

## Responsibilities

- User authentication using static credentials (configured via application properties)
- JWT token generation and validation
- Stateless security enforcement
- Protection of business endpoints
- Centralized error handling with standardized error responses
- Validation of authentication requests

---

## Functional Flow

### Login Flow

1. Client sends credentials to `/auth/login`
2. Auth module validates credentials
3. A JWT token is generated and returned
4. The client uses this token to access protected endpoints

### Authenticated Request Flow

1. Client sends request with `Authorization: Bearer <token>`
2. Security filter validates the token
3. If valid, request is authenticated
4. Business endpoint is executed
5. If invalid, a standardized `401 Unauthorized` error is returned

---

## Exposed Endpoints

| Method | Endpoint      | Description                          |
|---------|--------------|--------------------------------------|
| POST    | `/auth/login` | Authenticates user and returns JWT |
| GET     | `/auth/me`    | Returns authenticated user context |

---

## Architectural Design

The module follows **clean architecture principles**, separating concerns across controller, service, security, and exception layers.

- Controllers handle HTTP input/output
- Services handle authentication business logic
- Security layer manages JWT validation and request filtering
- Exception handlers enforce consistent API error responses

---

## Package Structure

```text
auth
├── controller
│ └── AuthController.java
│
├── controller/advice
│ └── AuthControllerAdvice.java
│
├── dto
│ ├── LoginRequestDTO.java
│ ├── TokenResponseDTO.java
│ ├── UserMeDTO.java
│ └── ErrorDTO.java
│
├── exception
│ └── BadCredentialsException.java
│
├── security
│ ├── JwtAuthenticationFilter.java
│ ├── JwtProperties.java
│ ├── JwtService.java
│ └── SecurityConfig.java
│
└── service
  ├── AuthService.java
  └── impl/AuthServiceImpl.java
```

---

## Key Design Decisions

- Stateless authentication using JWT
- No database dependency for authentication
- Fully decoupled from infrastructure concerns
- Simple configuration-driven credentials
- Clean modular architecture
- Strong test coverage (Controller + Service)
