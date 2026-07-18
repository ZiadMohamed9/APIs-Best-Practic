# 🔐 JWT Authentication API

A hands-on, production-grade implementation of **JWT (JSON Web Token) authentication** using **Spring Boot 4**, **Spring Security**, and **JJWT 0.12**. Built as a best-practices reference for stateless RESTful API security.

---

## 📋 Table of Contents

- [Overview](#overview)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Architecture](#architecture)
- [API Endpoints](#api-endpoints)
- [Security Design](#security-design)
- [Getting Started](#getting-started)
- [Testing the API](#testing-the-api)
- [Best Practices Applied](#best-practices-applied)

---

## Overview

This project demonstrates a complete JWT authentication lifecycle:

1. **Register** — Create a new user account (password hashed with BCrypt)
2. **Login** — Authenticate credentials and receive an access + refresh token pair
3. **Access protected resources** — Pass the access token as a `Bearer` header
4. **Refresh** — Exchange a refresh token for a new access token before expiry
5. **Role-based access** — Protect specific endpoints by role (`USER` / `ADMIN`)

---

## Tech Stack

| Technology | Version | Role |
|---|---|---|
| Java | 25 | Language |
| Spring Boot | 4.1.0 | Application framework |
| Spring Security | (managed) | Security filter chain |
| Spring Data JPA | (managed) | Database access layer |
| JJWT | 0.12.6 | JWT creation & validation |
| H2 (in-memory) | (managed) | Development database |
| Lombok | (managed) | Boilerplate reduction |
| springdoc-openapi | 2.8.8 | Swagger UI |
| Maven | 3.9.x | Build tool |

---

## Project Structure

```
com.auth.jwt
├── config/
│   ├── ApplicationConfig.java      # UserDetailsService bean
│   └── SecurityConfig.java         # Filter chain, AuthProvider, PasswordEncoder
├── controller/
│   ├── AuthController.java         # /api/auth/** — register, login, refresh
│   └── DemoController.java         # /api/demo/** — public, secured, admin
├── dto/
│   ├── RegisterRequest.java        # Record with validation annotations
│   ├── LoginRequest.java           # Record with validation annotations
│   ├── AuthResponse.java           # Access + refresh token pair
│   ├── RefreshRequest.java         # Refresh token input
│   └── ApiError.java               # Consistent error response shape
├── entity/
│   ├── User.java                   # JPA entity + implements UserDetails
│   └── Role.java                   # Enum: USER, ADMIN
├── exception/
│   └── GlobalExceptionHandler.java # @RestControllerAdvice — consistent error responses
├── repository/
│   └── UserRepository.java         # JpaRepository<User, UUID>
├── security/
│   └── JwtAuthFilter.java          # OncePerRequestFilter — extracts and validates JWT
└── service/
    ├── AuthService.java            # Registration, login, refresh logic
    └── JwtService.java             # Token generation and validation (JJWT)
```

---

## Architecture

### Request Flow

```
HTTP Request
    │
    ▼
┌─────────────────────────────────────────┐
│           JwtAuthFilter                 │  ← OncePerRequestFilter
│  1. Extract "Authorization: Bearer xxx" │
│  2. Validate token signature + expiry   │
│  3. Set auth in SecurityContextHolder   │
└─────────────────────────────────────────┘
    │
    ▼
┌─────────────────────────────────────────┐
│         SecurityFilterChain             │  ← Checks rules
│  - /api/auth/** → permitAll            │
│  - /api/demo/public → permitAll        │
│  - everything else → authenticated     │
└─────────────────────────────────────────┘
    │
    ▼
┌─────────────────────────────────────────┐
│            Controller                   │
│  @PreAuthorize("hasRole('ADMIN')")      │  ← Method-level security
└─────────────────────────────────────────┘
```

### Token Strategy

```
Login / Register
       │
       ├──► Access Token  (15 min)  ─► Use in Authorization header
       └──► Refresh Token (7 days)  ─► Use to get new access token
```

Both tokens are signed **HMAC-SHA256** JWTs. The access token is short-lived to limit the damage window if stolen. The refresh token is long-lived and rotated on each use.

---

## API Endpoints

### Authentication — `/api/auth`

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `POST` | `/api/auth/register` | ❌ Public | Create a new user account |
| `POST` | `/api/auth/login` | ❌ Public | Authenticate and receive tokens |
| `POST` | `/api/auth/refresh` | ❌ Public | Exchange refresh token for new tokens |

#### Register — `POST /api/auth/register`

**Request Body:**
```json
{
  "firstName": "Ziad",
  "lastName": "Mohamed",
  "email": "ziad@example.com",
  "password": "SecurePass123"
}
```

**Response — `201 Created`:**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
}
```

#### Login — `POST /api/auth/login`

**Request Body:**
```json
{
  "email": "ziad@example.com",
  "password": "SecurePass123"
}
```

**Response — `200 OK`:**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
}
```

#### Refresh — `POST /api/auth/refresh`

**Request Body:**
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
}
```

**Response — `200 OK`:**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
}
```

---

### Demo Endpoints — `/api/demo`

| Method | Endpoint | Auth Required | Role | Description |
|---|---|---|---|---|
| `GET` | `/api/demo/public` | ❌ | — | Publicly accessible |
| `GET` | `/api/demo/secured` | ✅ | Any | Requires a valid JWT |
| `GET` | `/api/demo/admin` | ✅ | `ADMIN` | Requires ADMIN role |

---

### Error Responses

All errors return a consistent `ApiError` shape:

```json
{
  "status": 401,
  "message": "Invalid email or password.",
  "timestamp": "2026-07-18T18:45:00"
}
```

| Scenario | Status |
|---|---|
| Wrong credentials | `401 Unauthorized` |
| Expired JWT | `401 Unauthorized` |
| Tampered JWT signature | `401 Unauthorized` |
| Missing / invalid role | `403 Forbidden` |
| Validation failure (`@Valid`) | `400 Bad Request` |
| User not found | `404 Not Found` |

---

## Security Design

### Stateless Sessions
The server **never stores session state**. Every request is independently authenticated via its JWT. This makes the API horizontally scalable — any server instance can handle any request.

```java
.sessionManagement(session ->
    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
)
```

### Password Hashing
Passwords are hashed with **BCrypt** before storage. BCrypt is adaptive (slow by design), making brute-force attacks computationally expensive. Plain-text passwords never touch the database.

### Token Validation
Every request through `JwtAuthFilter` is validated for:
1. **Signature** — was the token signed by this server's secret key?
2. **Expiry** — has the token expired?
3. **Subject match** — does the token's `sub` claim match the user in the database?

### Role-Based Access Control
The `User` entity implements Spring Security's `UserDetails`, returning authorities in the format `ROLE_USER` / `ROLE_ADMIN`. Method-level security with `@PreAuthorize("hasRole('ADMIN')")` enforces role checks at the controller level.

### User IDs
Users are identified by **UUID** (not auto-increment integers). This prevents enumeration attacks where an attacker could guess valid user IDs by incrementing a number.

---

## Getting Started

### Prerequisites

- Java 25 (JDK 25)
- Maven (or use the included `mvnw` wrapper)

> **Note:** Ensure `JAVA_HOME` points to your JDK 25 installation:
> ```powershell
> $env:JAVA_HOME = "C:\Program Files\Java\jdk-25.0.3"
> ```

### Configuration

The app uses **H2 in-memory database** by default — no external database setup required.

Key settings in `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:jwt_db
  jpa:
    hibernate:
      ddl-auto: update    # Auto-creates the 'users' table on startup
    show-sql: true        # Prints SQL queries to console

jwt:
  secret-key: "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970"
  access-token-expiration: 900000       # 15 minutes
  refresh-token-expiration: 604800000   # 7 days
```

> ⚠️ **Production Note:** Never hardcode the `secret-key`. Use environment variables:
> ```yaml
> jwt:
>   secret-key: ${JWT_SECRET_KEY}
> ```

### Running the Application

```bash
# Clone and navigate to the project
cd "APIs Best Practices/jwt"

# Run with the Maven wrapper (Windows)
.\mvnw.cmd spring-boot:run

# Or on Unix/Mac
./mvnw spring-boot:run
```

The application starts at **`http://localhost:8080`**

---

## Testing the API

### Swagger UI

Open your browser and navigate to:

```
http://localhost:8080/swagger-ui/index.html
```

All endpoints are documented and executable directly from the browser.

### H2 Console

Inspect the in-memory database at:

```
http://localhost:8080/h2-console/
```

| Field | Value |
|---|---|
| JDBC URL | `jdbc:h2:mem:jwt_db` |
| Username | `sa` |
| Password | *(leave blank)* |

### Adding an Admin User

By default, the registration API creates users with the `USER` role. Since the H2 database is in-memory and resets on startup, the best way to consistently have an `ADMIN` user is to seed the database using a `CommandLineRunner` component. 

Create a file `DataInitializer.java` in your config package:
```java
@Configuration
public class DataInitializer {
    @Bean
    public CommandLineRunner initAdminUser(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (!userRepository.existsByEmail("admin@example.com")) {
                User admin = User.builder()
                        .firstName("Super").lastName("Admin")
                        .email("admin@example.com")
                        .password(passwordEncoder.encode("admin123"))
                        .role(Role.ADMIN)
                        .build();
                userRepository.save(admin);
            }
        };
    }
}
```

### cURL Examples

```bash
# 1. Register a new user
curl -s -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"firstName":"Ziad","lastName":"Mohamed","email":"ziad@test.com","password":"SecurePass123"}' | jq

# 2. Login (save tokens from response)
curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"ziad@test.com","password":"SecurePass123"}' | jq

# 3. Access public endpoint (no token needed)
curl -s http://localhost:8080/api/demo/public

# 4. Access secured endpoint WITH token
curl -s http://localhost:8080/api/demo/secured \
  -H "Authorization: Bearer <YOUR_ACCESS_TOKEN>"

# 5. Access secured endpoint WITHOUT token — expect 403
curl -s http://localhost:8080/api/demo/secured

# 6. Access admin endpoint (expect 403 for USER role)
curl -s http://localhost:8080/api/demo/admin \
  -H "Authorization: Bearer <YOUR_ACCESS_TOKEN>"

# 7. Refresh tokens
curl -s -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"refreshToken":"<YOUR_REFRESH_TOKEN>"}' | jq
```

---

## Best Practices Applied

| Practice | Implementation |
|---|---|
| **UUID entity IDs** | Prevents user count enumeration |
| **BCrypt password hashing** | Adaptive, brute-force resistant |
| **Stateless sessions** | `SessionCreationPolicy.STATELESS` — truly RESTful |
| **Short-lived access tokens** | 15-minute expiry limits theft damage window |
| **Refresh token rotation** | New refresh token issued on every refresh |
| **Externalized JWT config** | Secret key and expiry in `application.yml`, injectable via env vars |
| **`UserDetails` on entity** | Clean Spring Security integration without adapter classes |
| **Java Records for DTOs** | Immutable, zero-boilerplate request/response objects |
| **`@Valid` on all inputs** | Validates DTOs before hitting business logic |
| **`OncePerRequestFilter`** | Guarantees JWT filter runs exactly once per request |
| **`@RestControllerAdvice`** | Consistent JSON error responses — no HTML error pages |
| **Circular dependency resolved** | `ApplicationConfig` (`UserDetailsService`) separated from `SecurityConfig` (filter chain) |
| **`@EnableMethodSecurity`** | `@PreAuthorize` for fine-grained role control per endpoint |
| **No `WebSecurityConfigurerAdapter`** | Modern component-based Spring Security 6+ configuration |
