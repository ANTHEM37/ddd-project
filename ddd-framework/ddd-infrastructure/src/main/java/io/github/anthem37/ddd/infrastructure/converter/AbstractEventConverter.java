package io.github.anthem37.ddd.infrastructure.converter;

import io.github.anthem37.ddd.common.exception.BusinessException;
import io.github.anthem37.ddd.domain.event.IDomainEvent;

/**
 * 事件转换器抽象基类
 * 提供通用的事件转换逻辑和异常处理
 *
 * @param <E> 领域事件类型
 * @param <M> 消息类型
 * @author anthem37
 * @date 2025/8/19 11:00:00
 */
public abstract class AbstractEventConverter<E extends IDomainEvent, M> implements IEventConverter<E, M> {

    @Override
    public M toMessage(E event) {
        if (event == null) {
            return null;
        }

        if (!supports(event)) {
            throw new BusinessException("不支持的事件类型：" + event.getClass().getSimpleName());
        }

        try {
            return doToMessage(event);
        } catch (Exception e) {
            throw new BusinessException("事件转消息失败：" + e.getMessage(), e);
        }
    }

    @Override
    public E toEvent(M message) {
        if (message == null) {
            return null;
        }

        try {
            return doToEvent(message);
        } catch (Exception e) {
            throw new BusinessException("消息转事件失败：" + e.getMessage(), e);
        }
    }

    /**
     * 执行事件到消息的转换
     * 子类必须实现此方法
     *
     * @param event 领域事件
     * @return 消息对象
     */
    protected abstract M doToMessage(E event);

    /**
     * 执行消息到事件的转换
     * 子类必须实现此方法
     *
     * @param message 消息对象
     * @return 领域事件
     */
    protected abstract E doToEvent(M message);

    /**
     * 转换前的预处理
     * 子类可以重写此方法
     *
     * @param event 领域事件
     */
    protected void preProcess(E event) {
        // 默认空实现
    }

    /**
     * 转换后的后处理
     * 子类可以重写此方法
     *
     * @param event   领域事件
     * @param message 消息对象
     */
    protected void postProcess(E event, M message) {
        // 默认空实现
    }
}