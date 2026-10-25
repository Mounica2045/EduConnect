-- ==============================================================================
-- EduConnect Database Schema
-- Table: users (Authentication, Roles, and Profiles)
-- ==============================================================================

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

-- Index on email for accelerated lookup
CREATE INDEX idx_users_email ON users(email);

-- ==============================================================================
-- Table: resources (Educational Resource Management & Search - Member 3)
-- ==============================================================================

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

-- ==============================================================================
-- Table: bookmarks (Student Saved Resources - Member 4)
-- ==============================================================================

CREATE TABLE IF NOT EXISTS bookmarks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    resource_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_bookmarks_users FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_bookmarks_resources FOREIGN KEY (resource_id) REFERENCES resources(id) ON DELETE CASCADE,
    CONSTRAINT uq_bookmarks_user_resource UNIQUE (user_id, resource_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_bookmarks_user_created ON bookmarks(user_id, created_at);
CREATE INDEX idx_bookmarks_resource ON bookmarks(resource_id);

-- ==============================================================================
-- Table: admin_audit_logs (Administrative Audit Trail - Member 4)
-- ==============================================================================

CREATE TABLE IF NOT EXISTS admin_audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    admin_id BIGINT NOT NULL,
    admin_email VARCHAR(150) NOT NULL,
    action VARCHAR(50) NOT NULL,
    target_type VARCHAR(50) NOT NULL,
    target_id BIGINT NOT NULL,
    details VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_audit_admin FOREIGN KEY (admin_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_audit_logs_created ON admin_audit_logs(created_at);
CREATE INDEX idx_audit_logs_admin ON admin_audit_logs(admin_id);
CREATE INDEX idx_audit_logs_target ON admin_audit_logs(target_type, target_id);


