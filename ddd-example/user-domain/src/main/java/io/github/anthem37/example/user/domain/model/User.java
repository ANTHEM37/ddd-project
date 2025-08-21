package io.github.anthem37.example.user.domain.model;

import io.github.anthem37.ddd.domain.model.AbstractAggregateRoot;
import io.github.anthem37.example.user.domain.event.UserCreatedEvent;
import io.github.anthem37.example.user.domain.event.UserStatusChangedEvent;
import io.github.anthem37.example.user.domain.valueobject.Email;
import io.github.anthem37.example.user.domain.valueobject.UserId;
import io.github.anthem37.example.user.domain.valueobject.UserStatus;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 用户聚合根
 * 展示DDD框架的聚合根特性
 */
@EqualsAndHashCode(callSuper = true)
public class User extends AbstractAggregateRoot<UserId> {

    @Getter
    @Setter
    private String username;
    @Getter
    @Setter
    private Email email;
    @Getter
    @Setter
    private String password;
    @Getter
    @Setter
    private UserStatus status;
    @Getter
    @Setter
    private LocalDateTime createdAt;
    @Getter
    @Setter
    private LocalDateTime updatedAt;

    // 用于持久化的构造函数
    public User(UserId id, String username, Email email, String password, UserStatus status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        super(id);
        this.username = username;
        this.email = email;
        this.password = password;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // 工厂方法 - 创建新用户
    public static User create(String username, Email email, String password) {
        UserId userId = UserId.generate();
        User user = new User(userId, username, email, password, UserStatus.ACTIVE, LocalDateTime.now(), LocalDateTime.now());

        // 发布领域事件
        user.addDomainEvent(new UserCreatedEvent(userId, username, email.getValue()));

        return user;
    }

    // 业务方法 - 激活用户
    public void activate() {
        if (this.status == UserStatus.ACTIVE) {
            throw new IllegalStateException("用户已经是激活状态");
        }

        UserStatus oldStatus = this.status;
        this.status = UserStatus.ACTIVE;
        this.updatedAt = LocalDateTime.now();

        // 发布状态变更事件
        this.addDomainEvent(new UserStatusChangedEvent(this.getId(), oldStatus, this.status));
        afterBusinessOperation();
    }

    // 业务方法 - 停用用户
    public void deactivate() {
        if (this.status == UserStatus.INACTIVE) {
            throw new IllegalStateException("用户已经是停用状态");
        }

        UserStatus oldStatus = this.status;
        this.status = UserStatus.INACTIVE;
        this.updatedAt = LocalDateTime.now();

        // 发布状态变更事件
        this.addDomainEvent(new UserStatusChangedEvent(this.getId(), oldStatus, this.status));
        afterBusinessOperation();
    }

    // 业务方法 - 更新邮箱
    public void updateEmail(Email newEmail) {
        if (this.email.equals(newEmail)) {
            return;
        }

        this.email = newEmail;
        this.updatedAt = LocalDateTime.now();
        afterBusinessOperation();
    }

    @Override
    protected void addDeletedDomainEvent() {
        // 添加用户删除事件
        // 这里可以添加UserDeletedEvent，暂时留空
    }

    @Override
    protected void validateInvariants() {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalStateException("用户名不能为空");
        }
        if (email == null) {
            throw new IllegalStateException("邮箱不能为空");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalStateException("密码不能为空");
        }
        if (status == null) {
            throw new IllegalStateException("用户状态不能为空");
        }
    }
}