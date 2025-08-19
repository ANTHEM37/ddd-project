-- H2测试数据库初始化脚本
CREATE TABLE IF NOT EXISTS users (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 插入测试数据
INSERT INTO users (id, username, email, password, status) VALUES 
('test-user-1', 'testuser1', 'test1@example.com', 'password123', 'ACTIVE'),
('test-user-2', 'testuser2', 'test2@example.com', 'password123', 'INACTIVE');