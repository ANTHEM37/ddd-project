-- 用户管理数据库初始化脚本
-- 展示DDD框架的数据库设计

CREATE DATABASE IF NOT EXISTS ddd_example 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE ddd_example;

-- 用户表
CREATE TABLE IF NOT EXISTS users (
    id VARCHAR(36) NOT NULL COMMENT '用户ID',
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    email VARCHAR(100) NOT NULL COMMENT '邮箱',
    password VARCHAR(255) NOT NULL COMMENT '密码',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE-激活，INACTIVE-停用，LOCKED-锁定，DELETED-已删除',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username),
    UNIQUE KEY uk_email (email),
    KEY idx_status (status),
    KEY idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 插入测试数据
INSERT INTO users (id, username, email, password, status) VALUES 
('test-user-1', 'testuser1', 'test1@example.com', 'password123', 'ACTIVE'),
('test-user-2', 'testuser2', 'test2@example.com', 'password123', 'INACTIVE'),
('test-user-3', 'testuser3', 'test3@example.com', 'password123', 'ACTIVE');