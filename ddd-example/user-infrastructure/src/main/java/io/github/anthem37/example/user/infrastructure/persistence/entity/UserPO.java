package io.github.anthem37.example.user.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.github.anthem37.example.user.domain.valueobject.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 用户持久化对象
 * 展示DDD框架的持久化对象模式
 */
@TableName("users")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class UserPO {

    @TableId(type = IdType.INPUT)
    private String id;

    @TableField("username")
    private String username;

    @TableField("email")
    private String email;

    @TableField("password")
    private String password;

    @TableField("status")
    private UserStatus status;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}