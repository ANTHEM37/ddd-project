package io.github.anthem37.example.user.infrastructure.event;

import io.github.anthem37.ddd.domain.event.IEventHandler;
import io.github.anthem37.example.user.domain.event.UserStatusChangedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 用户状态变更事件处理器
 * 展示DDD框架的事件处理特性
 *
 * @author hb28301
 * @date 2025/8/19 15:34:25
 */
@Slf4j
@Component
public class UserStatusChangedEventHandler implements IEventHandler<UserStatusChangedEvent> {

    @Override
    public void handle(UserStatusChangedEvent event) {
        handleUserStatusChanged(event);
    }

    @Override
    public Class<UserStatusChangedEvent> getSupportedEventType() {

        return UserStatusChangedEvent.class;
    }

    /**
     * 处理用户状态变更事件
     */
    private void handleUserStatusChanged(UserStatusChangedEvent event) {
        log.info("处理用户状态变更事件: userId={}, oldStatus={}, newStatus={}", event.getUserId().getValue(), event.getOldStatus(), event.getNewStatus());

        // 这里可以添加具体的业务逻辑，比如：
        // 1. 发送状态变更通知
        // 2. 更新相关系统状态
        // 3. 记录状态变更历史
        // 4. 触发其他业务流程
    }
}
