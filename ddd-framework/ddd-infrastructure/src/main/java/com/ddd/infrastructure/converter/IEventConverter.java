package com.ddd.infrastructure.converter;

import com.ddd.domain.event.IDomainEvent;

/**
 * 事件转换器接口
 * 负责领域事件与外部消息格式之间的转换
 *
 * @param <E> 领域事件类型
 * @param <M> 消息类型
 * @author anthem37
 * @date 2025/8/19 11:00:00
 */
public interface IEventConverter<E extends IDomainEvent, M> {

    /**
     * 将领域事件转换为消息
     *
     * @param event 领域事件
     * @return 消息对象
     */
    M toMessage(E event);

    /**
     * 将消息转换为领域事件
     *
     * @param message 消息对象
     * @return 领域事件
     */
    E toEvent(M message);

    /**
     * 检查是否支持该事件类型
     *
     * @param event 领域事件
     * @return 是否支持
     */
    default boolean supports(E event) {
        return event != null;
    }

    /**
     * 获取支持的事件类型
     *
     * @return 事件类型
     */
    Class<E> getSupportedEventType();
}