package io.github.anthem37.example.user.application.dto;

import io.github.anthem37.example.user.domain.valueobject.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 用户数据传输对象
 * 展示DDD框架的DTO模式
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class UserDTO {
    
    private String id;
    private String username;
    private String email;
    private UserStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}