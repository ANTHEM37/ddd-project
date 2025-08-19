package io.github.anthem37.example.user.interfaces.dto;

import io.github.anthem37.example.user.domain.valueobject.UserStatus;

import java.time.LocalDateTime;

/**
 * 用户响应DTO
 * 展示DDD框架的接口层DTO特性
 */
public class UserResponse {
    
    private String id;
    private String username;
    private String email;
    private UserStatus status;
    private String statusDescription;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public UserResponse() {
    }
    
    public UserResponse(String id, String username, String email, UserStatus status, 
                       LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.status = status;
        this.statusDescription = status.getDescription();
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public UserStatus getStatus() {
        return status;
    }
    
    public void setStatus(UserStatus status) {
        this.status = status;
        this.statusDescription = status != null ? status.getDescription() : null;
    }
    
    public String getStatusDescription() {
        return statusDescription;
    }
    
    public void setStatusDescription(String statusDescription) {
        this.statusDescription = statusDescription;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    @Override
    public String toString() {
        return "UserResponse{" +
                "id='" + id + '\'' +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", status=" + status +
                ", statusDescription='" + statusDescription + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}