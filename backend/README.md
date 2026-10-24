# EduConnect — Backend Authentication & Security Module (Member 2)
> **College Learning Resource Sharing Portal — Community Service Project (CSP)**  
> **Package:** `com.educonnect`  
> **Role:** Member 2 (Login, Registration, Authentication, RBAC, and Security Architecture)

---

## 1. Overview & Responsibilities

This module provides the core identity, authentication, authorization, and profile management services for **EduConnect**. It ensures:
- Secure student self-registration with email validation and password policy enforcement.
- Password hashing with **BCrypt** (strength factor 12) preventing plaintext credential storage.
- Stateless **JWT (JSON Web Token)** authentication compatible with Member 1's decoupled Vanilla HTML/JS frontend.
- Strict **Role-Based Access Control (RBAC)** distinguishing between `ROLE_STUDENT` and `ROLE_ADMIN`.
- Protection of private student endpoints and administrative moderation queues.
- Standardized, sanitized error responses preventing account enumeration and data leakage.
- Automated tests using **JUnit 5**, **Mockito**, and **Spring Boot MockMvc**.

---

## 2. Technology Stack

- **Language:** Java 21 (LTS)
- **Framework:** Spring Boot 3.3.4
- **Security:** Spring Security 6.3+ (Stateless `SecurityFilterChain`)
- **Persistence:** Spring Data JPA, Hibernate ORM
- **Database:** MySQL 8.x (production/dev), In-Memory H2 (automated test suite)
- **Token Infrastructure:** JJWT (Java JWT) 0.12.6 with HMAC-SHA256
- **Validation:** Jakarta Bean Validation (Hibernate Validator)
- **Build Tool:** Apache Maven 3.9+
- **Testing:** JUnit 5, Mockito, Spring Security Test, MockMvc

---

## 3. Database Design (`users` Table)

```sql
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    department VARCHAR(100),
    semester VARCHAR(50),
    role VARCHAR(20) NOT NULL DEFAULT 'STUDENT',
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_users_email UNIQUE (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_users_email ON users(email);
```

### Security Highlights:
- **Unique Constraint:** Guarantees uniqueness at both the JPA layer and database engine level.
- **Normalization:** Emails are automatically converted to trimmed lowercase before saving and querying.
- **Password Obfuscation:** Only salted BCrypt hashes are stored; plaintext passwords are never saved or logged.
- **No Privilege Escalation:** Public registration requests cannot supply or override the `role` field; `Role.STUDENT` is unconditionally assigned by the server.

---

## 4. REST API Contract & Examples

### 1. Student Registration
- **Endpoint:** `POST /api/auth/register`
- **Access:** Public
- **Request Body:**
  ```json
  {
    "fullName": "Priya Sharma",
    "email": "priya.sharma@college.edu",
    "password": "StrongPassword123!",
    "department": "Computer Science & Engineering",
    "semester": "Semester 4"
  }
  ```
- **Response (HTTP 201 Created):**
  ```json
  {
    "message": "Registration successful",
    "user": {
      "id": 1,
      "fullName": "Priya Sharma",
      "email": "priya.sharma@college.edu",
      "department": "Computer Science & Engineering",
      "semester": "Semester 4",
      "role": "STUDENT",
      "enabled": true
    }
  }
  ```

---

### 2. User Login
- **Endpoint:** `POST /api/auth/login`
- **Access:** Public
- **Request Body:**
  ```json
  {
    "email": "priya.sharma@college.edu",
    "password": "StrongPassword123!"
  }
  ```
- **Response (HTTP 200 OK):**
  ```json
  {
    "message": "Login successful",
    "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJwcml5YS5zaGFybWFAY29sbGVnZS5lZHU...",
    "tokenType": "Bearer",
    "user": {
      "id": 1,
      "fullName": "Priya Sharma",
      "email": "priya.sharma@college.edu",
      "department": "Computer Science & Engineering",
      "semester": "Semester 4",
      "role": "STUDENT",
      "enabled": true
    }
  }
  ```

---

### 3. Current User Identity
- **Endpoint:** `GET /api/auth/me`
- **Access:** Authenticated (`Authorization: Bearer <token>`)
- **Response (HTTP 200 OK):**
  ```json
  {
    "id": 1,
    "fullName": "Priya Sharma",
    "email": "priya.sharma@college.edu",
    "department": "Computer Science & Engineering",
    "semester": "Semester 4",
    "role": "STUDENT",
    "enabled": true
  }
  ```
- **Unauthenticated (HTTP 401 Unauthorized):**
  ```json
  {
    "status": 401,
    "message": "Authentication required. Please provide a valid Bearer token or sign in.",
    "path": "/api/auth/me",
    "timestamp": "2026-10-24T18:10:00Z"
  }
  ```

---

### 4. Logout
- **Endpoint:** `POST /api/auth/logout`
- **Access:** Authenticated
- **Response (HTTP 200 OK):**
  ```json
  {
    "message": "Logged out successfully"
  }
  ```

---

### 5. Update Profile
- **Endpoint:** `PUT /api/users/me`
- **Access:** Authenticated
- **Request Body:**
  ```json
  {
    "fullName": "Priya S. Rao",
    "department": "Computer Science & Engineering",
    "semester": "Semester 5"
  }
  ```
- **Response (HTTP 200 OK):** Updated `UserResponse` DTO.

---

## 5. Environment Variables & Setup

Create the MySQL database in your local MySQL instance:

```sql
CREATE DATABASE educonnect_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### Supported Environment Variables:

| Variable | Default Value | Description |
| :--- | :--- | :--- |
| `DB_HOST` | `localhost` | MySQL Server Host |
| `DB_PORT` | `3306` | MySQL Port |
| `DB_NAME` | `educonnect_db` | Database Name |
| `DB_USERNAME` | `root` | Database Username |
| `DB_PASSWORD` | `root` | Database Password |
| `JWT_SECRET` | *(Pre-configured 256-bit key)* | HMAC-SHA256 Signing Secret |
| `JWT_EXPIRATION_MS` | `86400000` (24h) | Token lifespan in milliseconds |
| `CORS_ALLOWED_ORIGINS`| `http://localhost:3000,...` | Comma-separated frontend origins |
| `ADMIN_EMAIL` | `admin@college.edu` | Initial admin account email |
| `ADMIN_PASSWORD` | `AdminPassword123!` | Initial admin account password |

---

## 6. How to Build & Run

### A. Run with Maven
```powershell
cd backend
mvn clean spring-boot:run
```

### B. Run Automated Test Suite
All tests execute against an isolated in-memory H2 database:
```powershell
cd backend
mvn test
```

### C. Build Production JAR
```powershell
cd backend
mvn clean package -DskipTests
java -jar target/educonnect-backend-1.0.0.jar
```

---

## 7. Initial Administrator Provisioning

To prevent storing plaintext admin credentials in Git:
1. `DataInitializer` checks if the configured `ADMIN_EMAIL` exists in MySQL on startup.
2. If absent, it hashes `ADMIN_PASSWORD` using BCrypt and inserts the record with `Role.ADMIN`.
3. In production, pass custom environment variables:
   ```bash
   ADMIN_EMAIL=faculty.coordinator@college.edu ADMIN_PASSWORD=ProductionSuperSecretPassword! java -jar app.jar
   ```

---

## 8. Integration with Member 1's Frontend

Member 1's vanilla JS client in `js/api.js` connects out-of-the-box:
1. `POST /api/auth/login` returns `{ "token": "...", "user": {...} }`.
2. Frontend stores token in `localStorage.setItem('educonnect_auth_token', token)`.
3. Subsequent `fetch` requests include `Authorization: Bearer <token>`.
4. `JwtAuthenticationFilter` validates the signature, extracts user context, and secures private resources seamlessly.
