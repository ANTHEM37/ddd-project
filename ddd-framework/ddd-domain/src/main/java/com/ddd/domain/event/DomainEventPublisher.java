package com.ddd.domain.event;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * 领域事件发布器
 * 纯领域层实现，不依赖外部框架
 *
 * @author anthem37
 * @date 2025/8/13 18:05:37
 */
@Slf4j
public class DomainEventPublisher {

    private static final ThreadLocal<List<DomainEvent>> EVENTS = new ThreadLocal<>();
    private static EventPublisher eventPublisher;

    /**
     * 设置事件发布器实现
     */
    public static void setEventPublisher(EventPublisher publisher) {
        DomainEventPublisher.eventPublisher = publisher;
    }

    /**
     * 发布单个事件
     */
    public static void publish(DomainEvent event) {
        if (event == null) {
            return;
        }

        if (eventPublisher != null) {
            eventPublisher.publish(event);
            return;
        }
        log.debug("未设置事件发布器，事件将被忽略: {}", event.getEventType());
    }

    /**
     * 延迟发布事件（在事务提交后）
     */
    public static void publishAfterCommit(DomainEvent event) {
        List<DomainEvent> events = EVENTS.get();
        if (events == null) {
            events = new ArrayList<>();
            EVENTS.set(events);
        }
        events.add(event);
    }

    /**
     * 提交延迟的事件
     */
    public static void commitEvents() {
        List<DomainEvent> events = EVENTS.get();
        if (events != null && !events.isEmpty()) {
            // 逐个发布事件，而不是使用批量发布
            // 这样可以确保每个事件都被正确处理
            events.forEach(DomainEventPublisher::publish);
            EVENTS.remove();
        }
    }

    /**
     * 清除延迟的事件
     */
    public static void clearEvents() {
        EVENTS.remove();
    }

    /**
     * 事件发布器接口
     * 由基础设施层实现
     */
    public interface EventPublisher {

        /**
         * 发布领域事件
         */
        void publish(DomainEvent event);
    }
}
