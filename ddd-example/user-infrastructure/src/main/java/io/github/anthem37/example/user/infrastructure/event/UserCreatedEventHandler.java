package io.github.anthem37.example.user.infrastructure.event;

import io.github.anthem37.ddd.domain.event.IEventHandler;
import io.github.anthem37.example.user.domain.event.UserCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 用户创建事件处理器
 * 展示DDD框架的事件处理特性
 */
@Slf4j
@Component
public class UserCreatedEventHandler implements IEventHandler<UserCreatedEvent> {

    @Override
    public void handle(UserCreatedEvent event) {
        handleUserCreated(event);
    }

    @Override
    public Class<UserCreatedEvent> getSupportedEventType() {
        return UserCreatedEvent.class;
    }

    /**
     * 处理用户创建事件
     */
    private void handleUserCreated(UserCreatedEvent event) {
        log.info("处理用户创建事件: userId={}, username={}, email={}", event.getUserId().getValue(), event.getUsername(), event.getEmail());

        // 这里可以添加具体的业务逻辑，比如：
        // 1. 发送欢迎邮件
        // 2. 创建用户配置文件
        // 3. 记录审计日志
        // 4. 发送通知给管理员
    }
}