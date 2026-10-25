# EduConnect — System Architecture & Design
> **College Learning Resource Sharing Portal — Community Service Project (CSP)**

---

## 1. High-Level Architectural Overview

EduConnect utilizes a decoupled **Multi-Page Application (MPA) + RESTful Backend** architecture. The frontend is built purely with standards-compliant HTML5, CSS3 design tokens, and modular Vanilla ES6 JavaScript, communicating via asynchronous JSON HTTP calls with a **Java 21 / Spring Boot 3.3.4** backend microservice stack backed by a **MySQL 8.x** relational database.

```mermaid
graph TD
    subgraph Client ["Client Tier (Browser)"]
        UI["Vanilla HTML5 / CSS3 / ES6 MPA"]
        API_CLIENT["apiClient (Fetch API + Bearer JWT)"]
        UI --> API_CLIENT
    end

    subgraph Security ["Security & Gateway Tier"]
        CORS["CORS Filter"]
        JWT_FILTER["JwtAuthenticationFilter"]
        AUTH_MANAGER["AuthenticationManager (BCrypt)"]
        API_CLIENT --> CORS
        CORS --> JWT_FILTER
        JWT_FILTER --> AUTH_MANAGER
    end

    subgraph Controllers ["Spring Boot REST Controller Tier"]
        AUTH_CTRL["AuthController (/api/auth)"]
        USER_CTRL["UserController (/api/users)"]
        RES_CTRL["ResourceController (/api/resources)"]
        BM_CTRL["BookmarkController (/api/bookmarks)"]
        ADM_CTRL["AdminController (/api/admin)"]
        CNT_CTRL["ContactController (/api/contact)"]
        JWT_FILTER --> AUTH_CTRL
        JWT_FILTER --> USER_CTRL
        JWT_FILTER --> RES_CTRL
        JWT_FILTER --> BM_CTRL
        JWT_FILTER --> ADM_CTRL
        JWT_FILTER --> CNT_CTRL
    end

    subgraph Services ["Service & Business Logic Tier"]
        AUTH_SVC["AuthService"]
        USER_SVC["UserService"]
        RES_SVC["ResourceService"]
        FILE_SVC["FileStorageService"]
        BM_SVC["BookmarkService"]
        ADM_SVC["AdminService"]
        RES_CTRL --> FILE_SVC
        RES_CTRL --> RES_SVC
        BM_CTRL --> BM_SVC
        ADM_CTRL --> ADM_SVC
    end

    subgraph Persistence ["Data & Storage Tier"]
        REPO["Spring Data JPA Repositories"]
        DISK["Local Persistent Volume (/app/uploads)"]
        MYSQL[("MySQL 8.x Relational Database")]
        FILE_SVC --> DISK
        SERVICES --> REPO
        REPO --> MYSQL
    end
```

---

## 2. Component Tier Responsibilities

### 1. Presentation Tier (Member 1)
- **Zero Frontend Frameworks:** Implemented without React, Angular, Bootstrap, or Tailwind to satisfy academic constraints and ensure lightweight cross-browser compatibility.
- **Design Tokens:** Strict CSS custom properties for palette (Deep Navy `#1e3a8a`, Academic Blue `#2563eb`, Accent Teal `#0d9488`, Warm Gold `#d97706`), typography, spacing, shadows, and radii.
- **Accessibility:** WCAG 2.2 AA compliant with high-contrast ratios, visible focus outlines, ARIA roles, and screen-reader status indicators.

### 2. Identity, Authentication & Security Tier (Member 2)
- **Stateless Authentication:** Spring Security 6.x `SecurityFilterChain` utilizing signed JWT tokens (HMAC-SHA256).
- **Password Protection:** Passwords are hashed using BCrypt (cost factor 12) before persistence; plaintext credentials never touch logs or responses.
- **Role Separation:** Enforces `ROLE_STUDENT` and `ROLE_ADMIN` permissions via method-level `@PreAuthorize` guards.

### 3. Resource Management & Search Tier (Member 3)
- **Dynamic Multi-Criteria Search:** JPA Criteria queries encapsulated within `ResourceSpecification` combining text keyword, category, department, semester, and file format.
- **File Management:** Physical document storage with sanitized filenames (`UUID_originalName`), path traversal blocking, MIME type validation, and streamed downloads.

### 4. Database, Bookmarking & Admin Tier (Member 4)
- **Relational Integrity:** Foreign keys with `ON DELETE CASCADE` between bookmarks, resources, and users. Composite uniqueness on `(user_id, resource_id)`.
- **Administrative Moderation:** Workflow enforcing `PENDING` status on submissions until approved by an administrator. Comprehensive audit logging via `admin_audit_logs`.

### 5. Integration, QA & Deployment Tier (Member 5)
- **End-to-End Orchestration:** Seamless API configuration via `window.EDUCONNECT_API_BASE_URL`.
- **Test Automation:** 80 automated unit, MockMvc, and end-to-end integration tests with 100% pass rate.
- **Continuous Integration & Delivery:** GitHub Actions workflow and Docker multi-stage containerization.

---

## 3. Relational Database Schema Model

```mermaid
erDiagram
    USERS ||--o{ RESOURCES : "uploads"
    USERS ||--o{ BOOKMARKS : "saves"
    RESOURCES ||--o{ BOOKMARKS : "is saved in"
    USERS ||--o{ ADMIN_AUDIT_LOGS : "performs"

    USERS {
        bigint id PK
        varchar full_name
        varchar email UK
        varchar password_hash
        varchar department
        varchar semester
        varchar role
        boolean enabled
        timestamp created_at
        timestamp updated_at
    }

    RESOURCES {
        bigint id PK
        varchar title
        text description
        varchar subject
        varchar department
        varchar semester
        varchar category
        varchar file_name
        varchar file_location
        varchar file_type
        bigint file_size
        varchar external_url
        bigint uploaded_by_id FK
        varchar status
        int download_count
        varchar tags
        timestamp created_at
        timestamp updated_at
    }

    BOOKMARKS {
        bigint id PK
        bigint user_id FK
        bigint resource_id FK
        timestamp created_at
    }

    ADMIN_AUDIT_LOGS {
        bigint id PK
        bigint admin_id
        varchar admin_email
        varchar action
        varchar target_type
        bigint target_id
        varchar details
        timestamp created_at
    }
```
