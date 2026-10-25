# EduConnect — Backend Services (Members 2, 3, 4 & 5)
> **College Learning Resource Sharing Portal — Community Service Project (CSP)**  
> **Package:** `com.educonnect`  
> **Modules:** Member 2 (Authentication & Security), Member 3 (Resource Management & Search APIs), Member 4 (Database, Bookmarks & Admin Module), Member 5 (Integration, QA, CI/CD & Deployment)

---

## 1. Overview & Responsibilities

This backend provides the core microservices for **EduConnect**:
- **Member 2 (Auth & Security):** Identity management, BCrypt hashing, stateless JWT authentication, and RBAC (`ROLE_STUDENT`, `ROLE_ADMIN`).
- **Member 3 (Resources & Search):** Educational resource management, multi-criteria faceted filtering, file upload/streaming, and category taxonomy.
- **Member 4 (Database, Bookmarks & Admin):** MySQL relational schema design, Flyway migrations, student bookmarking, administrator dashboard metrics, moderation queue, user account management, and administrative audit logging.
- **Member 5 (Integration, Testing & Deployment):** End-to-end integration testing (`UserJourneysIntegrationTest`), Contact microservice (`POST /api/contact`), Docker multi-stage containerization, GitHub Actions CI pipeline, and full-stack documentation.
- **80 Automated Tests:** 100% passing test coverage using JUnit 5, Mockito, and Spring Boot MockMvc.

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

## 5. Educational Resource Management & Search APIs (Member 3)

### 1. Database Design (`resources` Table)

```sql
CREATE TABLE IF NOT EXISTS resources (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    subject VARCHAR(100) NOT NULL,
    department VARCHAR(100) NOT NULL,
    semester VARCHAR(50),
    category VARCHAR(50) NOT NULL DEFAULT 'NOTES',
    file_name VARCHAR(255),
    file_location VARCHAR(500),
    file_type VARCHAR(50) DEFAULT 'PDF',
    file_size BIGINT,
    external_url VARCHAR(500),
    uploaded_by_id BIGINT,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    download_count INT NOT NULL DEFAULT 0,
    tags VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_resources_users FOREIGN KEY (uploaded_by_id) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_resources_status_created ON resources(status, created_at);
CREATE INDEX idx_resources_category ON resources(category);
CREATE INDEX idx_resources_department ON resources(department);
CREATE INDEX idx_resources_semester ON resources(semester);
CREATE INDEX idx_resources_subject ON resources(subject);
CREATE INDEX idx_resources_uploader ON resources(uploaded_by_id);
```

### 2. Resource Management Endpoints

| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/resources` | Public | Multi-criteria faceted search, filter, paginate, and sort approved materials |
| `GET` | `/api/resources/recent` | Public | Retrieves latest approved resources (e.g. for homepage cards) |
| `GET` | `/api/resources/categories` | Public | Returns complete taxonomy of resource categories with UI badges |
| `GET` | `/api/resources/category/{slug}` | Public | Browse resources filtered by category slug (`notes`, `qp`, `lab`, `prog`, etc.) |
| `GET` | `/api/resources/subject/{subject}` | Public | Browse resources by subject course name |
| `GET` | `/api/resources/{id}` | Public* | Single resource details (*approved only; owner/admin can view pending/rejected) |
| `GET` | `/api/resources/{id}/download` | Public* | Streams document file download with `Content-Disposition` attachment |
| `GET` | `/api/resources/mine` | Authenticated | Retrieves current authenticated student's uploaded materials |
| `POST` | `/api/resources` | Authenticated | Creates a new resource with multipart file upload (`25MB` max) or JSON external URL |
| `PUT` | `/api/resources/{id}` | Authenticated | Updates resource metadata (owner or admin only) |
| `DELETE` | `/api/resources/{id}` | Authenticated | Deletes resource and removes stored file from disk (owner or admin only) |
| `PATCH` | `/api/resources/{id}/approve` | Admin Only | Approves pending resource for public library publication |
| `PATCH` | `/api/resources/{id}/reject` | Admin Only | Rejects resource from publication |
| `GET` | `/api/admin/resources` | Admin Only | Administrative moderation queue and management feed |

### 3. Multi-Criteria Search & Filtering (`GET /api/resources`)

Supports dynamic combination of:
- `q`: Search keyword across `title`, `description`, `subject`, `department`, and `tags`.
- `category`: Category slug (`notes`, `qp`, `lab`, `prog`, `tutorials`, `other`).
- `department`: Department name (e.g. `Computer Science & Engineering`).
- `semester`: Semester level (e.g. `Semester 4`).
- `subject`: Course name (e.g. `Data Structures`).
- `fileType`: Document format (e.g. `PDF`, `DOCX`, `ZIP`).
- `sort`: `recent` (default), `popular` (by download count), `title_asc`, `title_desc`.
- `page`: 0-indexed page number (default 0).
- `size`: Items per page (default 6).

**Example Response:**
```json
{
  "content": [
    {
      "id": 1,
      "title": "Data Structures & Algorithms Comprehensive Notes",
      "description": "Complete handwritten and digital notes covering Trees, Graphs, and DP.",
      "subject": "Data Structures",
      "department": "Computer Science & Engineering",
      "semester": "Semester 3",
      "category": "notes",
      "categoryLabel": "Subject Notes",
      "badgeClass": "badge-notes",
      "fileName": "DSA_Notes.pdf",
      "fileType": "PDF",
      "fileSize": "4.5 MB",
      "fileSizeBytes": 4718592,
      "downloadCount": 142,
      "status": "APPROVED",
      "approved": true,
      "contributorName": "Aarav Sharma",
      "uploadDate": "2026-10-24T18:22:45.000Z"
    }
  ],
  "page": 0,
  "size": 6,
  "totalElements": 5,
  "totalPages": 1,
  "first": true,
  "last": true
}
```

### 4. File Upload & Validation Rules

- **Allowed Formats:** `.pdf`, `.docx`, `.doc`, `.pptx`, `.ppt`, `.zip`, `.txt`
- **Max File Size:** `25 MB` (enforced at both Spring Boot servlet level and service layer)
- **Path Traversal Protection:** Filenames are sanitized via `StringUtils.cleanPath()` and reject any directory traversal patterns (`..`).
- **Collision-Resistant Storage:** Stored with generated UUID prefix (`UUID_sanitized_name`).
- **Moderation Workflow:** Student uploads enter `PENDING` status. Faculty/Administrator uploads are automatically marked `APPROVED`.

---

## 6. MySQL Database, Bookmarks & Admin Module (Member 4)

### 1. Database Design (`bookmarks` & `admin_audit_logs` Tables)

```sql
-- Bookmarks table with composite uniqueness and cascade deletes
CREATE TABLE IF NOT EXISTS bookmarks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    resource_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_bookmarks_user_resource UNIQUE (user_id, resource_id),
    CONSTRAINT fk_bookmarks_users FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_bookmarks_resources FOREIGN KEY (resource_id) REFERENCES resources(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_bookmarks_user_created ON bookmarks(user_id, created_at);
CREATE INDEX idx_bookmarks_resource ON bookmarks(resource_id);

-- Admin audit log table
CREATE TABLE IF NOT EXISTS admin_audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    admin_id BIGINT NOT NULL,
    admin_email VARCHAR(150) NOT NULL,
    action VARCHAR(50) NOT NULL,
    target_type VARCHAR(50) NOT NULL,
    target_id BIGINT NOT NULL,
    details VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_audit_logs_created ON admin_audit_logs(created_at);
CREATE INDEX idx_audit_logs_admin ON admin_audit_logs(admin_id);
CREATE INDEX idx_audit_logs_target ON admin_audit_logs(target_type, target_id);
```

### 2. Bookmarks Endpoints (`/api/bookmarks`)

| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/bookmarks/{resourceId}` | Authenticated | Saves an approved resource to user's bookmarks (returns `201 Created`, or `409 Conflict` if already saved) |
| `DELETE` | `/api/bookmarks/{resourceId}` | Authenticated | Removes resource from user's bookmarks |
| `GET` | `/api/bookmarks/{resourceId}/status` | Authenticated | Checks if resource is bookmarked by current user (`{ "bookmarked": true }`) |
| `GET` | `/api/bookmarks` | Authenticated | Returns JSON array of saved resources (Member 1 frontend) or paginated when `?page=0&size=10` |

### 3. Administrator Moderation & Management Endpoints (`/api/admin`)

| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/admin/dashboard` | Admin Only | Summary metric cards (`totalUsers`, `totalResources`, `approvedResources`, `pendingResources`, `rejectedResources`, `totalBookmarks`) |
| `GET` | `/api/admin/resources/pending` | Admin Only | Paginated moderation review queue for pending resources |
| `GET` | `/api/admin/resources` | Admin Only | Full resource catalog list with moderation flags |
| `PATCH` | `/api/admin/resources/{id}/approve` | Admin Only | Approves resource publication and logs audit record |
| `PATCH` | `/api/admin/resources/{id}/reject` | Admin Only | Rejects resource with optional review reason and logs audit record |
| `DELETE` | `/api/admin/resources/{id}` | Admin Only | Permanently deletes resource and purges stored physical file |
| `GET` | `/api/admin/users` | Admin Only | Paginated user accounts list (safe DTO with zero password hash exposure) |
| `PATCH` | `/api/admin/users/{id}/status` | Admin Only | Enables or disables student accounts (self-disable prevention enforced) |
| `GET` | `/api/admin/audit-logs` | Admin Only | Chronological audit trail of all moderation actions |

### 4. Postman Collection
A complete Postman v2.1.0 collection with pre-configured requests, environment variables, Bearer tokens, and automated test scripts is available at:  
`backend/EduConnect_Member4_Bookmarks_and_Admin.postman_collection.json`

---

## 7. Environment Variables & Setup

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
| `UPLOAD_DIR` | `uploads/resources` | Document storage directory on server |
| `ADMIN_EMAIL` | `admin@college.edu` | Initial admin account email |
| `ADMIN_PASSWORD` | `AdminPassword123!` | Initial admin account password |

---

## 8. How to Build & Run

### A. Run with Docker Compose (Recommended)
From the repository root directory:
```bash
docker compose up -d --build
```

### B. Run with Maven Wrapper
```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

### C. Run Automated Test Suite (All 80 Tests)
All unit, controller, security, and end-to-end integration tests execute against an isolated in-memory H2 database:
```powershell
cd backend
.\mvnw.cmd test
```

### D. Build Production JAR
```powershell
cd backend
.\mvnw.cmd clean package -DskipTests
java -jar target/educonnect-backend-1.0.0.jar
```

---

## 9. Frontend-to-Backend Integration Contract

1. **Authentication:** Frontend stores JWT in `localStorage` (`educonnect_auth_token`), attaching `Authorization: Bearer <token>` on all mutation requests.
2. **Library Explorer (`resources.html`):** Synchronizes URL query params (`q`, `category`, `department`, `semester`, `fileType`, `sort`, `page`) directly with `GET /api/resources`.
3. **Upload Workflow (`upload.html`):** Submits `multipart/form-data` to `POST /api/resources` containing document file and metadata.
4. **Moderator Portal (`admin.html`):** Connects to `GET /api/admin/resources` and triggers `PATCH /api/admin/resources/{id}/approve` or `reject`.
5. **Contact Inquiries (`contact.html`):** Submits student feedback and questions to `POST /api/contact`.

