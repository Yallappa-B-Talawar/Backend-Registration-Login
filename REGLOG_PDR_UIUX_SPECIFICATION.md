# RegLog: Product Design Requirements (PDR) & UI/UX Design Specification
**Full-Stack Authentication System (`RegLogFrontApp` & `RegLogBackApp`)**

---

## Document Control
- **Project Name:** RegLog
- **Frontend Application:** `RegLogFrontApp` (React.js, Vanilla CSS / Tailwind CSS)
- **Backend Application:** `RegLogBackApp` (Java, Spring Boot, Spring Security, MySQL)
- **Document Version:** 1.0.0
- **Status:** Approved / Base Architecture Specification
- **Security Standard:** OWASP Top 10 Aligned, HttpOnly Cookie JWT Session Pattern

---

# Table of Contents
1. [Product Overview](#1-product-overview)
2. [Problem Statement](#2-problem-statement)
3. [Product Objectives & Goals](#3-product-objectives--goals)
4. [Target Users & Personas](#4-target-users--personas)
5. [User Stories & Acceptance Criteria](#5-user-stories--acceptance-criteria)
6. [Functional Requirements](#6-functional-requirements)
7. [Non-Functional Requirements](#7-non-functional-requirements)
8. [Information Architecture](#8-information-architecture)
9. [End-to-End User Journey & Flowcharts](#9-end-to-end-user-journey--flowcharts)
10. [Frontend Architecture (`RegLogFrontApp`)](#10-frontend-architecture-reglogfrontapp)
11. [Backend Architecture (`RegLogBackApp`)](#11-backend-architecture-reglogbackapp)
12. [Database Schema & Entity Relationship](#12-database-schema--entity-relationship)
13. [API Specification](#13-api-specification)
14. [Authentication & JWT Lifecycle Architecture](#14-authentication--jwt-lifecycle-architecture)
15. [Security Engineering & Hardening](#15-security-engineering--hardening)
16. [Visual Design System](#16-visual-design-system)
17. [UI/UX Wireframes & Screen Specifications](#17-uiux-wireframes--screen-specifications)
18. [Responsive Behavior & Viewport Breakpoints](#18-responsive-behavior--viewport-breakpoints)
19. [Component Specifications & Micro-Interactions](#19-component-specifications--micro-interactions)
20. [Form Validation & UI State Matrix](#20-form-validation--ui-state-matrix)
21. [Accessibility (a11y) Specifications](#21-accessibility-a11y-specifications)
22. [Testing Scenarios & Quality Assurance Matrix](#22-testing-scenarios--quality-assurance-matrix)
23. [Future Scope & Roadmap](#23-future-scope--roadmap)

---

# 1. Product Overview

**RegLog** is a full-stack, enterprise-grade authentication system designed to provide secure, resilient, and frictionless user registration, login, session validation, and logout.

The system is architected as two decoupled applications:
1. **`RegLogFrontApp`**: A modern single-page application (SPA) built using React.js, JSX, React Router, and modern CSS/Tailwind CSS. It provides a centered, card-based authentication workflow with instant client-side validation and real-time visual feedback.
2. **`RegLogBackApp`**: A robust RESTful backend built with Java, Spring Boot, Spring Security, Spring Data JPA, and MySQL. It enforces BCrypt password hashing, stateful JWT verification stored in MySQL, and cookie-based authentication via secure `HttpOnly` transport.

```
+-------------------------------------------------------------------------------+
|                                REGLOG SYSTEM                                  |
|                                                                               |
|  +--------------------------------+       +--------------------------------+  |
|  |       RegLogFrontApp           |       |         RegLogBackApp          |  |
|  |     (React SPA / Client)       |       |  (Spring Boot / Spring Web)    |  |
|  |                                | HTTP  |                                |  |
|  |  • Signup.jsx                  |<=====>|  • UserController              |  |
|  |  • Login.jsx                   | Cookie|  • AuthController              |  |
|  |  • Home.jsx (Protected)        | (JWT) |  • JwtAuthenticationFilter     |  |
|  |  • ProtectedRoute.jsx          |       |  • Spring Security Context     |  |
|  +--------------------------------+       +---------------+----------------+  |
|                                                           |                   |
|                                                           | JPA / JDBC        |
|                                                           v                   |
|                                           +--------------------------------+  |
|                                           |     reglog_db (MySQL 3306)     |  |
|                                           |  • users table                 |  |
|                                           |  • jwt_tokens table            |  |
|                                           +--------------------------------+  |
+-------------------------------------------------------------------------------+
```

---

# 2. Problem Statement

Authentication implementations in web applications frequently suffer from common vulnerabilities and poor user experience:
1. **Insecure Token Storage**: Storing JSON Web Tokens (JWT) in browser `localStorage` or `sessionStorage` exposes tokens to Cross-Site Scripting (XSS) attacks.
2. **Stateless JWT Revocation Failure**: Pure stateless JWTs cannot be immediately revoked when a user logs out, leaving tokens valid until their natural expiration.
3. **Cluttered UI / Weak Validation**: Beginners and standard web users encounter uninformative error messages, lack of responsive design, missing loading states, and jarring page reloads.
4. **Tight Coupling**: Monolithic architectures blur the boundary between presentation, authentication protocols, and persistence layers.

**RegLog addresses these issues** by persisting token records in MySQL (`jwt_tokens`), transmitting tokens exclusively through `HttpOnly`, `SameSite` cookies (completely inaccessible to JavaScript), maintaining a 1-hour expiration cycle, revoking tokens server-side upon logout, and delivering an aesthetic, accessible, and responsive user interface.

---

# 3. Product Objectives & Goals

### 3.1 Objectives
* Deliver an end-to-end authentication workflow: Registration -> Login -> Protected Dashboard (`Home`) -> Logout.
* Shield client-side sessions against XSS through strict `HttpOnly` cookie delivery.
* Prevent Cross-Site Request Forgery (CSRF) via `SameSite` policy and Spring Security CORS/CSRF safeguards.
* Maintain an active token registry in MySQL (`jwt_tokens`) to ensure explicit revocation capabilities.
* Provide an intuitive, modern, responsive UI with zero reliance on browser alerts or raw page redirects.

### 3.2 Key Performance Indicators (KPIs)
* **Authentication Latency**: End-to-end API response time under 250ms for login and registration.
* **Security Rating**: Zero sensitive tokens exposed to `window.localStorage`, `window.sessionStorage`, or JavaScript scope.
* **Form Usability**: Client-side validation prevents 100% of malformed payloads from hitting the network.
* **Responsive Fidelity**: 100% visual and functional compliance across mobile (320px), tablet (768px), and desktop (1024px+).

---

# 4. Target Users & Personas

### 4.1 Target Users
* **General Web Users**: Individuals creating an account, logging in, viewing their authenticated profile, and securely logging out across mobile or desktop devices.
* **Security Auditors & Developers**: Engineers evaluating a reference implementation of HttpOnly JWT storage coupled with database-backed token revocation in Spring Boot.

### 4.2 User Persona: Alex Morgan
* **Role**: End User
* **Goal**: Quickly sign up with email and phone, receive immediate validation, log in seamlessly, and see a personalized dashboard without technical friction.
* **Pain Points**: Cryptic error messages, forgetting passwords because of hidden inputs, apps staying logged in on public terminals after clicking logout.

---

# 5. User Stories & Acceptance Criteria

| ID | User Story | Acceptance Criteria |
|---|---|---|
| **US-01** | As a new user, I want to create an account with username, email, phone, and password so that I can access protected areas of the application. | 1. All fields are mandatory.<br>2. Email and phone format are validated before submission.<br>3. Passwords must match and satisfy complexity.<br>4. Duplicates receive clear server error messages.<br>5. Successful signup navigates automatically to `/login`. |
| **US-02** | As a registered user, I want to log in using my username and password so that I can establish an authenticated session. | 1. Validation for non-empty fields.<br>2. Submit triggers `POST /api/login/`.<br>3. Backend verifies credentials and issues HttpOnly cookie containing JWT.<br>4. Redirects to `/home` upon success. |
| **US-03** | As an authenticated user, I want to view my personalized `/home` page displaying my username fetched from the server. | 1. Unauthenticated users visiting `/home` are redirected to `/login`.<br>2. Display renders `Welcome, <username>!`.<br>3. User data is verified via `GET /api/user/me`. |
| **US-04** | As an authenticated user, I want to log out securely so that my session cannot be reused. | 1. Clicking Logout issues `POST /api/logout`.<br>2. Token record is deleted/revoked in `jwt_tokens`.<br>3. Cookie is cleared (`max-age=0`).<br>4. User is redirected to `/login`. |
| **US-05** | As an authenticated user whose session has exceeded 1 hour, I want the application to handle token expiry gracefully. | 1. Expired tokens trigger HTTP 401 on protected requests.<br>2. ProtectedRoute intercepts 401 and routes user to `/login` with an expiration prompt. |

---

# 6. Functional Requirements

### 6.1 User Registration (`RegLogFrontApp` & `RegLogBackApp`)
* **FR-REG-01**: The system shall collect `username`, `email`, `phone`, `password`, and `confirmPassword`.
* **FR-REG-02**: The frontend shall enforce instant inline regex validation for email and phone numbers.
* **FR-REG-03**: The frontend shall verify that `password` equals `confirmPassword` before dispatching `POST /api/reg`.
* **FR-REG-04**: The backend shall reject duplicate usernames or emails with HTTP 409 Conflict or HTTP 400 Bad Request.
* **FR-REG-05**: The backend shall hash passwords using BCrypt (work factor >= 10) before persisting to MySQL.
* **FR-REG-06**: Upon successful registration, the client shall redirect to `/login` with a success banner.

### 6.2 User Authentication (`Login`)
* **FR-AUTH-01**: The login screen shall accept `username` and `password`.
* **FR-AUTH-02**: The frontend shall disable the login button and display a spinner during in-flight requests.
* **FR-AUTH-03**: The backend shall verify credentials against the `users` table using BCrypt.
* **FR-AUTH-04**: The backend shall generate a signed JWT with a 1-hour expiration timestamp.
* **FR-AUTH-05**: The backend shall persist the generated token in `jwt_tokens` linked to the user's `id`.
* **FR-AUTH-06**: The backend `AuthController` shall write the JWT into a `Set-Cookie` header (`HttpOnly`, `Path=/`, `Max-Age=3600`, `SameSite=Lax`).
* **FR-AUTH-07**: The frontend shall never access or save the JWT in `localStorage` or `sessionStorage`.

### 6.3 Dashboard & Protected Routes (`Home`)
* **FR-HOME-01**: Access to `/home` shall require an active authenticated session.
* **FR-HOME-02**: The frontend `ProtectedRoute` component shall query `GET /api/user/me` with browser credentials included (`credentials: 'include'`).
* **FR-HOME-03**: The page shall render `Welcome, <username>!` using authenticated data returned by the backend.
* **FR-HOME-04**: An unauthenticated or expired session shall redirect to `/login`.

### 6.4 Logout & Session Revocation
* **FR-OUT-01**: The Home page shall feature a visible, accessible Logout button.
* **FR-OUT-02**: Clicking Logout shall invoke `POST /api/logout`.
* **FR-OUT-03**: The backend shall delete the corresponding token record from `jwt_tokens`.
* **FR-OUT-04**: The backend shall overwrite the cookie with `Max-Age=0` and empty value.
* **FR-OUT-05**: The frontend shall reset its in-memory auth state and redirect to `/login`.

---

# 7. Non-Functional Requirements

### 7.1 Security
* **NFR-SEC-01**: Passwords must never be stored, logged, or serialized in plaintext.
* **NFR-SEC-02**: JWTs must use HMAC-SHA256 (HS256) with a 256-bit cryptographically random secret key.
* **NFR-SEC-03**: Cookies must carry the `HttpOnly` flag to prevent XSS script access and `SameSite=Lax` (or `Strict`) to mitigate CSRF.
* **NFR-SEC-04**: CORS configuration on Spring Boot must explicitly whitelist `http://localhost:5173` (or frontend origin) with `allowCredentials=true`. Wildcard `*` origins are strictly prohibited with credentials.

### 7.2 Performance & Scalability
* **NFR-PERF-01**: API latency for authentication endpoints must not exceed 300ms under standard local load.
* **NFR-PERF-02**: Database queries on `users.name` and `users.email` must be indexed with unique constraints.
* **NFR-PERF-03**: `jwt_tokens.token` must be indexed to allow sub-millisecond token lookup during filter execution.

### 7.3 Reliability & Maintainability
* **NFR-REL-01**: Clean separation of concerns: Controllers handle HTTP requests/cookies, Services execute business logic, Repositories handle data access.
* **NFR-REL-02**: Standardized error response payload across all backend endpoints:
  ```json
  {
    "timestamp": "2026-09-16T10:30:00Z",
    "status": 401,
    "error": "Unauthorized",
    "message": "Invalid username or password",
    "path": "/api/login/"
  }
  ```

---

# 8. Information Architecture

```
                                  [ User Entry ]
                                         |
                                         v
                         +-------------------------------+
                         |          Path Router          |
                         +---------------+---------------+
                                         |
                +------------------------+------------------------+
                |                        |                        |
                v                        v                        v
        +---------------+        +---------------+        +---------------+
        |  / or /login  |        |    /signup    |        |     /home     |
        |  (Login.jsx)  |        |  (Signup.jsx) |        |   (Home.jsx)  |
        +-------+-------+        +-------+-------+        +-------+-------+
                |                        |                        |
                | Link to /signup        | Link to /login         | [ ProtectedRoute ]
                +------------------------+                        | Validates Cookie Session
                                                                  v
                                                        +-------------------+
                                                        | Authenticated User|
                                                        |  - Welcome banner |
                                                        |  - Logout button  |
                                                        +---------+---------+
                                                                  |
                                                                  v POST /api/logout
                                                        +-------------------+
                                                        | Redirect to /login|
                                                        +-------------------+
```

---

# 9. End-to-End User Journey & Flowcharts

### 9.1 Registration Flowchart
```
[ User on /signup ]
       |
       v
Fills: Username, Email, Phone, Password, Confirm Password
       |
       v
Frontend Validation (Required, Regex, Match)
       |
       +---> [ Validation Fails ] ---> Display Inline Field Errors
       |
       v [ Validation Passes ]
Button enters Loading State (Disabled + Spinner)
       |
       v HTTP POST /api/reg
[ Spring Boot: UserController ]
       |
       v
[ UserService: Check Duplicate Username/Email in UserRepository ]
       |
       +---> [ Exists ] ---> Return 409 Conflict / 400 Bad Request
       |
       v [ Unique ]
BCrypt Password Hashing -> Save to MySQL `users` table
       |
       v Return 201 Created / 200 OK
[ Frontend Receives Success ]
       |
       v
Toast/Notification: "Account created successfully! Redirecting to login..."
       |
       v Redirect to /login
```

### 9.2 Login Flowchart
```
[ User on /login ]
       |
       v
Fills: Username & Password
       |
       v
Frontend Validation (Non-empty checks)
       |
       v HTTP POST /api/login/
[ Spring Boot: AuthController ]
       |
       v
[ AuthService: Authenticate via UserRepository & PasswordEncoder ]
       |
       +---> [ Invalid Credentials ] ---> Return 401 Unauthorized
       |
       v [ Valid Credentials ]
Generate JWT (Claims: sub=username, userId, iat, exp=now + 1hr)
       |
       v
Save Record to MySQL `jwt_tokens` (user_id, token, created_at, expires_at)
       |
       v Return Auth Result to AuthController
[ AuthController Sets HttpOnly Cookie ]
       | Set-Cookie: token=<jwt>; HttpOnly; SameSite=Lax; Max-Age=3600; Path=/
       v
Return 200 OK Body: { "message": "Login successful", "username": "alex" }
       |
       v
[ Frontend updates in-memory user state -> Navigates to /home ]
```

### 9.3 Protected Route & Home Access Flowchart
```
[ User navigates to /home ]
       |
       v
[ ProtectedRoute Component Mounts ]
       |
       v Dispatch GET /api/user/me (credentials: 'include')
[ Spring Boot: JwtAuthenticationFilter intercepts request ]
       |
       +---> [ Cookie missing or empty ] ---> 401 Unauthorized
       |
       v [ Cookie contains JWT ]
Validate Signature & Expiration
       |
       +---> [ Expired / Tampered ] ---> 401 Unauthorized
       |
       v
Query MySQL `jwt_tokens` table for active token existence
       |
       +---> [ Token revoked/not found ] ---> 401 Unauthorized
       |
       v [ Token Active ]
Extract Username & load UserDetails -> Populate SecurityContextHolder
       |
       v
UserController returns 200 OK: { "id": 1, "username": "alex", "email": "alex@example.com", "phone": "1234567890" }
       |
       v
[ Home.jsx renders: "Welcome, alex!" ]
```

### 9.4 Logout Flowchart
```
[ User clicks "Logout" button on /home ]
       |
       v
Trigger POST /api/logout (credentials: 'include')
       |
       v
[ Spring Boot: AuthController & AuthService ]
       |
Extract JWT from Request Cookie
       |
Delete / Invalidate token record in MySQL `jwt_tokens` table
       |
Generate Clear Cookie: Set-Cookie: token=; HttpOnly; Max-Age=0; Path=/
       |
Return 200 OK: { "message": "Logged out successfully" }
       |
       v
[ Frontend clears user session state -> Redirects to /login ]
```

---

# 10. Frontend Architecture (`RegLogFrontApp`)

### 10.1 Technology Stack
* **Language**: JavaScript (ES6+)
* **Syntax**: JSX
* **Framework**: React.js (v18+)
* **Routing**: React Router DOM (v6+)
* **Styling**: Modern CSS3 / Tailwind CSS (custom utility-first styling with tokens)
* **HTTP Client**: Standard `Fetch API` (with custom wrapper `services/api.js`) or `Axios` configured with `withCredentials: true`
* **TypeScript**: **Explicitly Excluded** as per requirements.

### 10.2 Directory Structure
```text
RegLogFrontApp/
│
├── public/
│   └── favicon.ico
│
├── src/
│   ├── components/
│   │   └── ProtectedRoute.jsx     # Route guard validating session via /api/user/me
│   │
│   ├── pages/
│   │   ├── Signup.jsx             # User registration form with validation
│   │   ├── Login.jsx              # User credentials form with cookie reception
│   │   └── Home.jsx               # Protected user landing dashboard
│   │
│   ├── services/
│   │   └── api.js                 # Centralized Fetch/Axios API client with credentials
│   │
│   ├── App.jsx                    # Route provider and global layout
│   ├── main.jsx                   # React root mount
│   └── index.css                  # Design system tokens, utilities, and resets
│
├── index.html
├── package.json
└── vite.config.js
```

### 10.3 Route Configuration (`App.jsx`)
```jsx
// Routes mapping
// /        -> Login.jsx
// /login   -> Login.jsx
// /signup  -> Signup.jsx
// /home    -> ProtectedRoute -> Home.jsx
```

### 10.4 Protected Route Guard Mechanism (`ProtectedRoute.jsx`)
The route guard mounts, triggers a verification check to `GET /api/user/me` with `credentials: 'include'`, and handles three distinct states:
1. **Verifying (`loading`)**: Renders a sleek centered pulsing skeleton loader.
2. **Authenticated (`true`)**: Renders the `<Outlet />` or `<Home />` component, passing authenticated user details.
3. **Unauthenticated (`false` or 401)**: Redirects immediately to `<Navigate to="/login" replace />`.

---

# 11. Backend Architecture (`RegLogBackApp`)

### 11.1 Technology Stack
* **Language**: Java 17 / 21 LTS
* **Framework**: Spring Boot 3.x
* **Security**: Spring Security 6.x
* **Persistence**: Spring Data JPA / Hibernate
* **Database**: MySQL 8.x running on `localhost:3306` (`reglog_db`)
* **Token Utility**: JJWT (Java JWT by io.jsonwebtoken) or Spring Security OAuth2 Resource Server JWT
* **Build Tool**: Apache Maven

### 11.2 Package & Class Architecture
```text
RegLogBackApp/
│
├── src/main/java/com/reglog/
│   ├── controller/
│   │   ├── UserController.java       # POST /api/reg, GET /api/user/me
│   │   └── AuthController.java       # POST /api/login/, POST /api/logout
│   │
│   ├── service/
│   │   ├── UserService.java          # User creation, duplicate checks, profile fetch
│   │   └── AuthService.java          # Password verification, JWT generation & revocation
│   │
│   ├── repository/
│   │   ├── UserRepository.java       # Spring Data JPA queries on 'users'
│   │   └── JwtTokenRepository.java   # Spring Data JPA queries on 'jwt_tokens'
│   │
│   ├── entity/
│   │   ├── User.java                 # Entity mapping for 'users'
│   │   └── JwtToken.java             # Entity mapping for 'jwt_tokens'
│   │
│   ├── dto/
│   │   ├── RegisterRequest.java      # name, email, phone, password
│   │   ├── LoginRequest.java         # username, password
│   │   ├── LoginResponse.java        # message, username
│   │   └── UserProfileResponse.java  # id, username, email, phone
│   │
│   ├── security/
│   │   ├── SecurityConfig.java       # FilterChain, CORS, CSRF, PasswordEncoder
│   │   ├── JwtAuthenticationFilter.java # Cookie extractor, JWT signature & DB validator
│   │   └── JwtUtils.java             # Signing, parsing, claim extraction, expiry check
│   │
│   └── RegLogApplication.java        # Main Spring Boot Application Entrypoint
│
├── src/main/resources/
│   └── application.properties        # DB connections, JWT secrets, port configs
└── pom.xml
```

### 11.3 Architectural Separation of Concerns
```
+-------------------------------------------------------------------------------+
|                       SEPARATION OF RESPONSIBILITIES                          |
+-------------------------------------------------------------------------------+
| 1. User Service Layer:                                                        |
|    UserController    --> Accepts registration DTO, delegates to UserService   |
|    UserService       --> Validates uniqueness, hashes password with BCrypt,    |
|                          persists via UserRepository                          |
|    UserRepository    --> Executes SQL against `users` table                   |
+-------------------------------------------------------------------------------+
| 2. Auth Service Layer:                                                        |
|    AuthController    --> Receives credentials, invokes AuthService, builds    |
|                          HttpOnly ResponseCookie, returns client response     |
|    AuthService       --> Authenticates user, generates JWT with claims,       |
|                          persists record via JwtTokenRepository               |
|    JwtTokenRepository--> Executes SQL against `jwt_tokens` table              |
+-------------------------------------------------------------------------------+
| 3. Security Filter Layer:                                                     |
|    JwtAuthFilter     --> Extracts cookie, validates token against DB, sets    |
|                          SecurityContextHolder authentication token           |
+-------------------------------------------------------------------------------+
```

---

# 12. Database Schema & Entity Relationship

### 12.1 Database Specification
* **Database Name**: `reglog_db`
* **Host & Port**: `localhost:3306`
* **Character Set**: `utf8mb4`
* **Collation**: `utf8mb4_unicode_ci`

### 12.2 Entity Relationship Diagram (ERD)
```
+-----------------------------------+       +-----------------------------------+
|               users               |       |            jwt_tokens             |
+-----------------------------------+       +-----------------------------------+
| PK  id         BIGINT AUTO_INCR   | 1     | PK  id         BIGINT AUTO_INCR   |
|     name       VARCHAR(50) UNIQUE |<-----\| FK  user_id    BIGINT             |
|     email      VARCHAR(100) UNIQUE|      *|     token      VARCHAR(512) UNIQUE|
|     phone      VARCHAR(20)        |       |     created_at TIMESTAMP          |
|     password   VARCHAR(255)       |       |     expires_at TIMESTAMP          |
+-----------------------------------+       +-----------------------------------+
```

### 12.3 Table Definitions

#### Table: `users`
| Column Name | Data Type | Modifiers | Description |
|---|---|---|---|
| `id` | `BIGINT` | `PRIMARY KEY AUTO_INCREMENT` | Unique identifier for user |
| `name` | `VARCHAR(50)` | `NOT NULL UNIQUE` | Unique username chosen during registration |
| `email` | `VARCHAR(100)`| `NOT NULL UNIQUE` | Unique email address |
| `phone` | `VARCHAR(20)` | `NOT NULL` | Contact telephone number |
| `password` | `VARCHAR(255)`| `NOT NULL` | BCrypt salted hash (starts with `$2a$` or `$2b$`) |

#### Table: `jwt_tokens`
| Column Name | Data Type | Modifiers | Description |
|---|---|---|---|
| `id` | `BIGINT` | `PRIMARY KEY AUTO_INCREMENT` | Unique token record ID |
| `user_id` | `BIGINT` | `NOT NULL, FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE` | Associated user |
| `token` | `VARCHAR(512)`| `NOT NULL UNIQUE` | Serialized signed JWT string |
| `created_at`| `TIMESTAMP` | `NOT NULL DEFAULT CURRENT_TIMESTAMP` | Issuance timestamp |
| `expires_at`| `TIMESTAMP` | `NOT NULL` | Expiration timestamp (`created_at + 1 HOUR`) |

---

# 13. API Specification

All backend endpoints are prefixed with `/api`.

### 13.1 `POST /api/reg` — User Registration
* **Description**: Creates a new user record after verifying that username and email are unique.
* **Authentication**: None (Public endpoint).
* **Request Headers**: `Content-Type: application/json`
* **Request Body**:
  ```json
  {
    "name": "john_doe",
    "email": "john@example.com",
    "phone": "+1234567890",
    "password": "SecurePassword123"
  }
  ```
* **Success Response (`201 Created`)**:
  ```json
  {
    "status": "success",
    "message": "User registered successfully",
    "username": "john_doe"
  }
  ```
* **Error Responses**:
  * `400 Bad Request`: Validation failure (e.g., missing fields, invalid email format).
    ```json
    { "error": "Validation Failed", "details": ["Email must be a valid email address"] }
    ```
  * `409 Conflict`: Duplicate username or email.
    ```json
    { "error": "Conflict", "message": "Username or Email already registered" }
    ```

---

### 13.2 `POST /api/login/` — User Authentication
* **Description**: Validates credentials, issues a signed JWT, stores the token record in MySQL, and sets an `HttpOnly` cookie.
* **Authentication**: None (Public endpoint).
* **Request Headers**: `Content-Type: application/json`
* **Request Body**:
  ```json
  {
    "username": "john_doe",
    "password": "SecurePassword123"
  }
  ```
* **Response Headers**:
  ```http
  Set-Cookie: token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...; Max-Age=3600; Path=/; HttpOnly; SameSite=Lax
  ```
* **Success Response (`200 OK`)**:
  ```json
  {
    "status": "success",
    "message": "Login successful",
    "username": "john_doe"
  }
  ```
* **Error Responses**:
  * `400 Bad Request`: Empty username or password.
  * `401 Unauthorized`: Invalid username or password.
    ```json
    { "error": "Unauthorized", "message": "Invalid username or password" }
    ```

---

### 13.3 `POST /api/logout` — Session Termination
* **Description**: Revokes the active session by deleting the token from `jwt_tokens` and invalidating the cookie.
* **Authentication**: Required (Valid JWT cookie must be present).
* **Request Headers**: `Cookie: token=eyJhbGciOiJI...`
* **Response Headers**:
  ```http
  Set-Cookie: token=; Max-Age=0; Path=/; HttpOnly; SameSite=Lax
  ```
* **Success Response (`200 OK`)**:
  ```json
  {
    "status": "success",
    "message": "Logout successful"
  }
  ```
* **Error Response**:
  * `401 Unauthorized`: No active session to terminate.

---

### 13.4 `GET /api/user/me` — Authenticated User Profile
* **Description**: Resolves current authenticated user identity using the JWT provided in the cookie.
* **Authentication**: Required (Valid JWT cookie).
* **Request Headers**: `Cookie: token=eyJhbGciOiJI...`
* **Success Response (`200 OK`)**:
  ```json
  {
    "id": 1,
    "username": "john_doe",
    "email": "john@example.com",
    "phone": "+1234567890"
  }
  ```
* **Error Responses**:
  * `401 Unauthorized`: Cookie missing, token expired, or token not found in `jwt_tokens`.
    ```json
    { "error": "Unauthorized", "message": "Session has expired or is invalid. Please log in again." }
    ```

---

# 14. Authentication & JWT Lifecycle Architecture

### 14.1 Token Structure
```json
// Header
{
  "alg": "HS256",
  "typ": "JWT"
}

// Payload (Claims)
{
  "sub": "john_doe",
  "userId": 1,
  "iat": 1789540000,
  "exp": 1789543600
}
```

### 14.2 1-Hour Time Window
* **`iat` (Issued At)**: e.g., `10:00:00 AM`
* **`exp` (Expiration)**: Exactly `11:00:00 AM` (`iat + 3600 seconds`)
* **MySQL `jwt_tokens`**:
  * `created_at`: `2026-09-16 10:00:00`
  * `expires_at`: `2026-09-16 11:00:00`
* **Cookie Max-Age**: `3600` (browser discards cookie after 3600 seconds)

### 14.3 State Tracking in Database
Unlike purely stateless JWT setups, `RegLogBackApp` couples cryptographic validity with database verification:
1. **Creation**: Upon successful login, `AuthService` saves `{ user_id, token, created_at, expires_at }` into `jwt_tokens`.
2. **Verification**: When `JwtAuthenticationFilter` validates an incoming request, it checks:
   - Is cryptographic signature valid?
   - Is current time `< expires_at`?
   - Does this token exist in `jwt_tokens`?
3. **Revocation**: Upon `/api/logout`, the record is removed from `jwt_tokens`, instantly preventing replay even if the cookie was copied before deletion.

---

# 15. Security Engineering & Hardening

### 15.1 OWASP Hardening Matrix
| Vulnerability | Threat Vector | RegLog Mitigation Mechanism |
|---|---|---|
| **XSS Token Theft** | Malicious script extracts token from `localStorage` | Token is stored exclusively in `HttpOnly` cookie. JavaScript cannot read `document.cookie`. |
| **CSRF** | Malicious site triggers authenticated POST requests | Cookie is configured with `SameSite=Lax` (or `Strict`); CORS explicitly restricts authorized origin. |
| **Brute Force / Credential Stuffing** | Rapid login attempts | BCrypt work factor introduces CPU cost (~80ms per attempt); input length limits enforced. |
| **Token Replay After Logout** | Stolen token used before expiry | Server-side record in `jwt_tokens` is deleted at logout. Filter queries database to ensure token is active. |
| **SQL Injection** | Malicious SQL in username/password | Spring Data JPA uses parameterized PreparedStatements across all repository methods. |

### 15.2 Cookie Security Directive
```http
Set-Cookie: token=<jwt_payload>; 
  Max-Age=3600; 
  Expires=Wed, 16 Sep 2026 11:00:00 GMT; 
  Path=/; 
  HttpOnly; 
  SameSite=Lax; 
  Secure
```
*(Note: `Secure` flag is set to `true` in production HTTPS environments and conditionally relaxed for `localhost` development).*

---

# 16. Visual Design System

The RegLog interface is styled using a modern, minimal, corporate-tech aesthetic. It relies on clean contrast, subtle card elevation, crisp typography, and unambiguous state indicators.

### 16.1 Color Palette
```
+-------------------------------------------------------------------------------+
|                               COLOR PALETTE                                   |
+-------------------------------------------------------------------------------+
| PRIMARY BLUE      | #2563EB | rgb(37, 99, 235)   | Main brand, CTA buttons    |
| PRIMARY HOVER     | #1D4ED8 | rgb(29, 78, 216)   | Button hover/active states |
| PRIMARY ACCENT    | #EFF6FF | rgb(239, 246, 255) | Input focus backgrounds    |
| BACKGROUND (PAGE) | #F8FAFC | rgb(248, 250, 252) | Light modern slate         |
| CARD BACKGROUND   | #FFFFFF | rgb(255, 255, 255) | Surface card elevation     |
| TEXT PRIMARY      | #0F172A | rgb(15, 23, 42)    | Headings and labels        |
| TEXT SECONDARY    | #64748B | rgb(100, 116, 139) | Subheadings and helpers    |
| BORDER DEFAULT    | #E2E8F0 | rgb(226, 232, 240) | Field borders, dividers    |
| BORDER FOCUS      | #3B82F6 | rgb(59, 130, 246)  | Active focus rings         |
| ERROR RED         | #EF4444 | rgb(239, 68, 68)   | Validation errors, alert   |
| ERROR BACKGROUND  | #FEF2F2 | rgb(254, 242, 242) | Error banner background    |
| SUCCESS GREEN     | #10B981 | rgb(16, 185, 129)  | Success toast, valid checks|
| SUCCESS BACKGROUND| #ECFDF5 | rgb(236, 253, 245) | Success banner background  |
+-------------------------------------------------------------------------------+
```

### 16.2 Typography Hierarchy
* **Font Family**: `'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif`
* **Hierarchy**:
  * **Brand Mark**: `24px` (`1.5rem`), Weight 800 (ExtraBold), Tracking `+0.05em`
  * **H1 (Screen Title)**: `28px` (`1.75rem`), Weight 700 (Bold), Line Height `1.2`
  * **Subtitle**: `14px` (`0.875rem`), Weight 400 (Regular), Line Height `1.4`
  * **Form Labels**: `13px` (`0.8125rem`), Weight 600 (SemiBold), Uppercase / Capitalized
  * **Input Text**: `15px` (`0.9375rem`), Weight 400 (Regular)
  * **Button Text**: `15px` (`0.9375rem`), Weight 600 (SemiBold), Letter Spacing `+0.025em`
  * **Validation Errors**: `12px` (`0.75rem`), Weight 500 (Medium)

### 16.3 Elevation & Shadows
* **Card Elevation**: `box-shadow: 0 10px 25px -5px rgba(15, 23, 42, 0.08), 0 8px 10px -6px rgba(15, 23, 42, 0.04);`
* **Card Border**: `1px solid #E2E8F0;`
* **Border Radius**:
  * Input Fields: `8px` (`0.5rem`)
  * Buttons: `8px` (`0.5rem`)
  * Cards: `16px` (`1.0rem`)
  * Badges / Pills: `9999px` (Full rounded)

### 16.4 Spacing System
* Standard 4px scale:
  * `spacing-1`: `4px`
  * `spacing-2`: `8px`
  * `spacing-3`: `12px`
  * `spacing-4`: `16px`
  * `spacing-6`: `24px`
  * `spacing-8`: `32px`
  * `spacing-10`: `40px`

---

# 17. UI/UX Wireframes & Screen Specifications

### 17.1 Wireframe: Login Page (`Login.jsx`)
```
+-----------------------------------------------------------------------+
|  Viewport: 100vw, 100vh (Centered Content) Background: #F8FAFC        |
|                                                                       |
|                     +-------------------------------+                 |
|                     |        [CARD CONTAINER]       |                 |
|                     |        Max-Width: 420px       |                 |
|                     |                               |                 |
|                     |            REGLOG             |  <-- Brand Logo |
|                     |                               |                 |
|                     |         Welcome Back          |  <-- H1 Title   |
|                     |    Login to your account      |  <-- Subtitle   |
|                     |                               |                 |
|                     |  [!] Invalid username/pwd     |  <-- Alert Area |
|                     |                               |                 |
|                     |  Username                     |                 |
|                     |  +-------------------------+  |                 |
|                     |  | Enter username          |  |                 |
|                     |  +-------------------------+  |                 |
|                     |                               |                 |
|                     |  Password                     |                 |
|                     |  +----------------------+--+  |                 |
|                     |  | Enter password       |👁 |  |  <-- Toggle Vis|
|                     |  +----------------------+--+  |                 |
|                     |                               |                 |
|                     |  +-------------------------+  |                 |
|                     |  |         LOGIN           |  |  <-- CTA Button |
|                     |  +-------------------------+  |                 |
|                     |                               |                 |
|                     |    New user? Sign up          |  <-- Text Link  |
|                     +-------------------------------+                 |
|                                                                       |
+-----------------------------------------------------------------------+
```

### 17.2 Wireframe: Signup Page (`Signup.jsx`)
```
+-----------------------------------------------------------------------+
|  Viewport: 100vw, 100vh (Centered Content) Background: #F8FAFC        |
|                                                                       |
|                     +-------------------------------+                 |
|                     |        [CARD CONTAINER]       |                 |
|                     |        Max-Width: 460px       |                 |
|                     |                               |                 |
|                     |            REGLOG             |  <-- Brand Logo |
|                     |                               |                 |
|                     |        Create Account         |  <-- H1 Title   |
|                     |  Create your account to       |                 |
|                     |          continue             |  <-- Subtitle   |
|                     |                               |                 |
|                     |  Username                     |                 |
|                     |  +-------------------------+  |                 |
|                     |  | Enter username          |  |                 |
|                     |  +-------------------------+  |                 |
|                     |  * Username required          |                 |
|                     |                               |                 |
|                     |  Email                        |                 |
|                     |  +-------------------------+  |                 |
|                     |  | Enter email             |  |                 |
|                     |  +-------------------------+  |                 |
|                     |                               |                 |
|                     |  Phone                        |                 |
|                     |  +-------------------------+  |                 |
|                     |  | Enter phone number      |  |                 |
|                     |  +-------------------------+  |                 |
|                     |                               |                 |
|                     |  Password                     |                 |
|                     |  +----------------------+--+  |                 |
|                     |  | Create password      |👁 |  |                 |
|                     |  +----------------------+--+  |                 |
|                     |                               |                 |
|                     |  Confirm Password             |                 |
|                     |  +----------------------+--+  |                 |
|                     |  | Confirm password     |👁 |  |                 |
|                     |  +----------------------+--+  |                 |
|                     |                               |                 |
|                     |  +-------------------------+  |                 |
|                     |  |        SIGN UP          |  |  <-- CTA Button |
|                     |  +-------------------------+  |                 |
|                     |                               |                 |
|                     |    Already a user? Login      |  <-- Text Link  |
|                     +-------------------------------+                 |
|                                                                       |
+-----------------------------------------------------------------------+
```

### 17.3 Wireframe: Protected Home Page (`Home.jsx`)
```
+-----------------------------------------------------------------------+
|  +-----------------------------------------------------------------+  |
|  | [LOGO] REGLOG                                  [LOGOUT BUTTON]  |  |
|  +-----------------------------------------------------------------+  |
|                                                                       |
|                     +-------------------------------+                 |
|                     |        [WELCOME CARD]         |                 |
|                     |        Max-Width: 600px       |                 |
|                     |                               |                 |
|                     |          🎉 Active            |                 |
|                     |                               |                 |
|                     |      Welcome, alex_morgan!    |                 |
|                     |                               |                 |
|                     | You are successfully logged   |                 |
|                     |              in.              |                 |
|                     |                               |                 |
|                     | Session Security:             |                 |
|                     | • Mode: HttpOnly Cookie       |                 |
|                     | • Lifetime: 60 Minutes        |                 |
|                     |                               |                 |
|                     |         [ LOGOUT ]            |                 |
|                     +-------------------------------+                 |
|                                                                       |
+-----------------------------------------------------------------------+
```

---

# 18. Responsive Behavior & Viewport Breakpoints

| Breakpoint | Target Devices | Layout Adjustments | Form Card Styling |
|---|---|---|---|
| **Mobile (`< 768px`)** | iPhone 13/14/15, Pixel, Galaxy | Single-column, `padding: 16px`. Card spans `width: 100%`, `margin: 0 auto`. Touch-target heights >= 44px. | Minimal padding (`20px`), border simplified or hidden on small screens (<380px). |
| **Tablet (`768px - 1023px`)** | iPad, Tablets | Centered card (`max-width: 440px`), generous spacing (`32px` padding). | Elevation active (`shadow-md`), rounded corners (`16px`). |
| **Desktop (`1024px+`)** | Laptops, Desktop Monitors | Centered card (`max-width: 440px` for Login, `480px` for Signup). Navigation header on Home extends `max-width: 1200px`. | Full elevation (`shadow-xl`), clear focus outlines, micro-hover interactions. |

---

# 19. Component Specifications & Micro-Interactions

### 19.1 Button Component
* **Normal**: Background `#2563EB`, Text `#FFFFFF`, font-weight `600`, height `46px`, border-radius `8px`.
* **Hover**: Background shifts to `#1D4ED8` via `transition: all 0.2s ease-in-out`.
* **Active**: Scale down `transform: scale(0.99)`.
* **Focus**: Accessible ring `outline: 2px solid #3B82F6; outline-offset: 2px;`.
* **Disabled / Loading**: Background `#93C5FD`, cursor `not-allowed`. Loading displays an animated SVG spinner with text: `"Signing up..."` or `"Logging in..."`.

### 19.2 Input Field Component
* **Normal**: Height `44px`, background `#FFFFFF`, border `1px solid #E2E8F0`, padding `0 14px`.
* **Focus**: Border `#3B82F6`, shadow ring `box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.15);`.
* **Error**: Border `#EF4444`, shadow ring `box-shadow: 0 0 0 3px rgba(239, 68, 68, 0.15);`.
* **Password Visibility Toggle**: Inline icon positioned absolute right (`12px`). Interactive button with accessible label (`aria-label="Toggle password visibility"`).

---

# 20. Form Validation & UI State Matrix

### 20.1 Client-Side Validation Rules
| Field | Rule Specification | Client Error Message |
|---|---|---|
| **Username** | Required, min 3 characters, alphanumeric & underscores | *"Username must be at least 3 characters long"* |
| **Email** | Required, matches standard regex `/^[^\s@]+@[^\s@]+\.[^\s@]+$/` | *"Please enter a valid email address"* |
| **Phone** | Required, numeric/international format `/^\+?[0-9\s\-]{7,15}$/` | *"Please enter a valid phone number"* |
| **Password** | Required, min 6 characters | *"Password must be at least 6 characters long"* |
| **Confirm Password** | Required, must strictly equal `Password` value | *"Passwords do not match"* |

### 20.2 State Machine Matrix
```
+---------------------------------------------------------------------------------------+
| Trigger            | Current State  | Next State     | UI Representation             |
+--------------------+----------------+----------------+-------------------------------+
| User types input   | Idle / Error   | Validating     | Real-time validation clears   |
| Click Submit       | Idle           | Loading        | Button disabled + Spinner     |
| Server returns 400 | Loading        | Field Error    | Red border + Error message    |
| Server returns 401 | Loading        | Auth Error     | Top alert banner appears      |
| Server returns 201 | Loading (Reg)  | Success/Route  | Success toast -> Route /login |
| Server returns 200 | Loading (Auth) | Authenticated  | Route /home                   |
| 401 on /home       | Authenticated  | Session Expired| Clear state -> Route /login   |
+---------------------------------------------------------------------------------------+
```

---

# 21. Accessibility (a11y) Specifications

* **WCAG 2.1 Level AA Compliance**:
  * All text-to-background contrast ratios exceed `4.5:1` (Primary text `#0F172A` on `#FFFFFF` gives `16.0:1`).
  * Interactive blue `#2563EB` on `#FFFFFF` gives `4.6:1`.
* **Screen Reader Support**:
  * Semantic HTML5 elements (`<main>`, `<header>`, `<nav>`, `<form>`, `<label>`, `<button>`).
  * Every input explicitly linked with `<label htmlFor="field-id">`.
  * Inline validation errors carry `role="alert"` and `aria-live="polite"`.
* **Keyboard Navigability**:
  * Logical tab indexing: `Username -> Email -> Phone -> Password -> Confirm Password -> Submit Button -> Navigation Link`.
  * Form submittable via `Enter` key on any field.

---

# 22. Testing Scenarios & Quality Assurance Matrix

### 22.1 End-to-End Test Matrix
| Test Case ID | Scope | Steps | Expected Result |
|---|---|---|---|
| **TC-E2E-01** | Signup Validation | Submit empty signup form | 5 validation errors rendered below inputs; network request blocked. |
| **TC-E2E-02** | Password Mismatch | Enter differing passwords | "Passwords do not match" rendered; submission blocked. |
| **TC-E2E-03** | Successful Signup | Enter valid unique details | `POST /api/reg` returns 201; user redirected to `/login`. |
| **TC-E2E-04** | Duplicate User | Attempt registration with existing username | Backend returns 409; UI displays "Username or Email already taken". |
| **TC-E2E-05** | Invalid Login | Enter incorrect password | Backend returns 401; UI shows "Invalid username or password". |
| **TC-E2E-06** | Successful Login | Enter valid credentials | `POST /api/login/` returns 200; `token` cookie set as `HttpOnly`; UI redirects to `/home`. |
| **TC-E2E-07** | Protected Route Guard | Directly visit `http://localhost:5173/home` without cookie | Request to `/api/user/me` returns 401; app redirects to `/login`. |
| **TC-E2E-08** | Token Expiration | Force token timestamp to 1 hour past; navigate to `/home` | API returns 401; ProtectedRoute kicks user out to `/login`. |
| **TC-E2E-09** | Logout Revocation | Click Logout on Home page | `POST /api/logout` deletes row from `jwt_tokens`, sets cookie `Max-Age=0`; redirects to `/login`. |

---

# 23. Future Scope & Roadmap

*(Note: In accordance with core requirements, the initial release intentionally excludes these features to maintain a clean, lightweight core.)*

1. **Email Verification**: Integration with SMTP / SendGrid for confirmation email links prior to account activation.
2. **Forgot Password & Recovery Flow**: One-time secure password reset tokens sent via email.
3. **Multi-Factor Authentication (MFA / 2FA)**: Time-based One-Time Password (TOTP) through Google Authenticator.
4. **OAuth2 / Social Login**: Single sign-on with Google, GitHub, and Apple.
5. **Administrative Dashboard**: Role-based access control (RBAC) with `ROLE_ADMIN` to manage user accounts and monitor active token sessions.
6. **Refresh Token Rotation**: Sliding-window session extension with separate refresh tokens.
