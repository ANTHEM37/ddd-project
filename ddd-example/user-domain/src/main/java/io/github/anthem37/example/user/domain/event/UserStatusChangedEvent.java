package io.github.anthem37.example.user.domain.event;

import io.github.anthem37.ddd.domain.event.IDomainEvent;
import io.github.anthem37.example.user.domain.valueobject.UserId;
import io.github.anthem37.example.user.domain.valueobject.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 用户状态变更事件
 * 展示DDD框架的领域事件特性
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class UserStatusChangedEvent implements IDomainEvent {

    private final String eventType = "UserStatusChanged";
    private UserId userId;
    private UserStatus oldStatus;
    private UserStatus newStatus;
    private final LocalDateTime occurredOn = LocalDateTime.now();
}