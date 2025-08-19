package io.github.anthem37.example.user.domain.event;

import io.github.anthem37.ddd.domain.event.IDomainEvent;
import io.github.anthem37.example.user.domain.valueobject.UserId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 用户创建事件
 * 展示DDD框架的领域事件特性
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class UserCreatedEvent implements IDomainEvent {

    private final String eventType = "UserCreated";
    private UserId userId;
    private String username;
    private String email;
    private final LocalDateTime occurredOn = LocalDateTime.now();

}