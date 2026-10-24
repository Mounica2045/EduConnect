-- Flyway Migration V2: Initialize resources table (Member 3)
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
