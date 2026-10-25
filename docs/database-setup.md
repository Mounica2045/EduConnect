# EduConnect — Database Setup & Migration Guide
> **MySQL Relational Schema, Indexes, Foreign Keys, and Flyway Migrations**

---

## 1. Prerequisites

- **MySQL Server:** Version 8.0 or higher.
- **Client Tools:** MySQL Workbench, DBeaver, or the `mysql` CLI client.
- **Default Database Name:** `educonnect_db`
- **Character Set:** `utf8mb4` with collation `utf8mb4_unicode_ci`.

---

## 2. Quick Database Creation

Open your MySQL command line client or Workbench and execute:

```sql
-- 1. Create the database
CREATE DATABASE IF NOT EXISTS educonnect_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

-- 2. Verify database creation
USE educonnect_db;
SELECT DATABASE();
```

---

## 3. Database Migration Versions (Flyway)

The application uses versioned Flyway migrations located in `backend/src/main/resources/db/migration`:

1. **`V1__init_auth_schema.sql` (Member 2):**
   - Creates the `users` table.
   - Enforces unique constraint on `email`.
   - Indexes `email` for rapid authentication lookups.
2. **`V2__init_resource_schema.sql` (Member 3):**
   - Creates the `resources` table.
   - Adds foreign key `fk_resources_users` referencing `users(id)` with `ON DELETE SET NULL`.
   - Creates performance indexes on `(status, created_at)`, `category`, `department`, `semester`, `subject`, and `uploaded_by_id`.
3. **`V3__init_bookmarks_and_admin_schema.sql` (Member 4):**
   - Creates the `bookmarks` table with composite uniqueness constraint `uq_bookmarks_user_resource (user_id, resource_id)`.
   - Adds cascading foreign keys `ON DELETE CASCADE` for both `user_id` and `resource_id`.
   - Creates the `admin_audit_logs` table with indexes on `created_at`, `admin_id`, and `(target_type, target_id)`.

---

## 4. Complete Relational DDL Script (`schema.sql`)

If initializing manually without Flyway or for automated test verification, execute `backend/src/main/resources/schema.sql`:

```sql
-- Table: users
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

-- Table: resources
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

-- Table: bookmarks
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

-- Table: admin_audit_logs
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

---

## 5. Seed Accounts & Verification

Upon initial startup with `app.init.default-accounts=true`, the system automatically provisions:

1. **Administrator Account:**
   - **Email:** `admin@college.edu`
   - **Password:** `AdminPassword123!`
   - **Role:** `ADMIN`
2. **Student Account:**
   - **Email:** `student@college.edu`
   - **Password:** `Password123!`
   - **Role:** `STUDENT`

Both passwords are encrypted using BCrypt cost factor 12 before persistence.
