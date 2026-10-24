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
