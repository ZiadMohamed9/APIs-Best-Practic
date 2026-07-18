# APIs Best Practices

Welcome to the **APIs Best Practices** repository! This project serves as a public open-source reference for developers to practice and understand the implementation of critical API best practices. 

Whether you are building your first API or looking to harden a production-ready system, this repository provides clear, step-by-step examples of essential concepts.

## 🚀 The Master Plan

This project covers six fundamental pillars of robust API design and implementation. 
Currently, the primary implementation is happening in the `spring-boot` directory.

### 1. Authentication (JWT & 3rd Party) - **[IMPLEMENTED]**
Understanding the lifecycle of short-lived access tokens and long-lived refresh tokens is crucial for secure APIs.
* **The Scratch Plan**: Create User and RefreshToken entities (using BCrypt for passwords). Endpoints for Register, Login (15-min Access JWT, 7-day Refresh token), Refresh, and Logout. Implement security filters to validate JWT signatures and set context.
* **The 3rd Party Integration**: Transition from an Authorization Server to a Resource Server by integrating Keycloak or Auth0, relying on JWKS for token validation.

### 2. Rate Limiting and Quotas - **[PLANNED]**
Rate limiting protects your infrastructure; quotas enforce business tiers.
* **The Scratch Plan**: Use Middleware/Interceptors to extract User IDs or API Keys, checking Redis counters. Implement `429 Too Many Requests` responses and standard Rate Limit headers (`X-RateLimit-Limit`, `X-RateLimit-Remaining`, `X-RateLimit-Reset`).
* **The 3rd Party Tool**: Adopt **Bucket4j** (the Token Bucket algorithm standard) synced with Redis for distributed environments.

### 3. Webhook Delivery System - **[PLANNED]**
Webhooks require resilient, asynchronous background processing to avoid blocking main threads.
* **The Plan**: Allow clients to register via `POST /webhooks`. Trigger internal events via `@TransactionalEventListener` after successful DB commits. Use `@Async` workers (Virtual Threads) to deliver payloads with **Resilience4j** retry mechanisms and exponential backoff. Log all delivery results.

### 4. Response Caching - **[PLANNED]**
Caching sits between your controllers and your database to serve repetitive reads in milliseconds.
* **The Plan**: Utilize Redis as the CacheManager. Implement read caching (e.g., `@Cacheable`) and establish clear cache invalidation strategies (`@CacheEvict`, `@CachePut`) to ensure data freshness.

### 5. Versioned API (v1 and v2) - **[PLANNED]**
API contracts are promises to your clients. Breaking them is a cardinal sin.
* **The Plan**: Implement URI versioning (`/api/v1/...` and `/api/v2/...`). Map underlying JPA entities to different response DTOs using tools like **MapStruct** to handle structural changes between versions cleanly.

### 6. Observation and Monitoring - **[PLANNED]**
If you can't see what your API is doing, it's effectively broken.
* **The Plan**: Expose `/actuator/prometheus` endpoints. Inject OpenTelemetry tracing to track requests across controllers, databases, caches, and webhooks. Spin up Prometheus and Grafana via Docker Compose for visualization.

---

## 🤝 How to Contribute

This project is designed to be language and framework agnostic in the long run. While the initial focus is on **Spring Boot**, contributions for other tech stacks are highly encouraged!

### Adding a New Framework Implementation
Want to show how to implement these practices in `.NET`, `Node.js`, `FastAPI`, `Go`, or `Django`? We would love your contribution!
1. Fork the repository.
2. Create a new folder in the root directory named after the framework (e.g., `/nodejs`, `/fastapi`, `/dotnet`).
3. Implement the features outlined in the plan above.
4. Submit a Pull Request (PR)!

### Suggesting Enhancements or Fixes
* **Pull Requests**: Feel free to submit PRs for code enhancements, bug fixes, or documentation improvements in any of the existing implementations.
* **Issues**: Found a bug, have a question, or want to suggest a new "Best Practice" module? Please open an issue to discuss it.

Let's build the ultimate API reference guide together!
