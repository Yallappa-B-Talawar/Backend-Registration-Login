# RegLogBackApp — Full-Stack Authentication Backend

A robust RESTful authentication backend built with **Java 21**, **Spring Boot 3.3.3**, **Spring Security 6**, **Spring Data JPA**, **JJWT (v0.12.6)**, and **MySQL**.

---

## Features
- **User Registration (`POST /api/reg`)**: Duplicate check on username & email, BCrypt password hashing, and MySQL persistence.
- **User Login (`POST /api/login/`)**: Authenticates credentials, generates 1-hour signed JWT, persists token record in `jwt_tokens` table, and sets an `HttpOnly` browser cookie (`SameSite=Lax`, `Max-Age=3600`).
- **Protected User Profile (`GET /api/user/me`)**: Validates session cookie, queries token status, and loads authenticated profile.
- **Logout (`POST /api/logout`)**: Deletes active token from `jwt_tokens` in MySQL and wipes the `HttpOnly` cookie.
- **JWT Authentication Filter**: Intercepts protected requests, verifies signature & expiration, and populates Spring Security Context.
- **No Client Storage Exposure**: Tokens are never stored in `localStorage` or `sessionStorage` to prevent XSS attacks.

---

## Database Architecture (`defaultdb` / MySQL)

### `users`
- `id`: BIGINT AUTO_INCREMENT (Primary Key)
- `name`: VARCHAR(50) UNIQUE NOT NULL
- `email`: VARCHAR(100) UNIQUE NOT NULL
- `phone`: VARCHAR(20) NOT NULL
- `password`: VARCHAR(255) NOT NULL (BCrypt salted hash)

### `jwt_tokens`
- `id`: BIGINT AUTO_INCREMENT (Primary Key)
- `user_id`: BIGINT (Foreign Key to `users.id`)
- `token`: VARCHAR(512) UNIQUE NOT NULL
- `created_at`: TIMESTAMP NOT NULL
- `expires_at`: TIMESTAMP NOT NULL

---

## Configuration & Security (`application.properties`)
```properties
server.port=${PORT:8080}

# MySQL Database Configuration (Environment Variable Placeholders)
spring.datasource.url=${DB_URL:jdbc:mysql://localhost:3306/defaultdb?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC}
spring.datasource.username=${DB_USERNAME:root}
spring.datasource.password=${DB_PASSWORD:password}
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA & Hibernate Configuration
spring.jpa.hibernate.ddl-auto=update

# JWT Configuration (1 hour = 3600000 ms)
jwt.secret=${JWT_SECRET:404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970}
jwt.expiration-ms=3600000
jwt.cookie-name=token
```

---

## How to Run

1. Supply environment variables or local properties for `DB_PASSWORD`.
2. Run the application:
```bash
mvn clean spring-boot:run
```
Server will start on port `8080`.
