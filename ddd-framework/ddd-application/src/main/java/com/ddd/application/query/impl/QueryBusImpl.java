package com.ddd.application.query.impl;

import com.ddd.application.bus.AbstractMessageBus;
import com.ddd.application.query.IQuery;
import com.ddd.application.query.IQueryBus;
import com.ddd.application.query.IQueryHandler;
import com.ddd.common.util.GenericTypeResolver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * 查询总线实现
 * 负责查询的路由和执行
 *
 * @author anthem37
 * @date 2025/8/14 09:17:53
 */
@Slf4j
@Component
public class QueryBusImpl extends AbstractMessageBus<IQuery<?>, IQueryHandler<?, ?>> implements IQueryBus {

    @Autowired
    @Qualifier("queryExecutor")
    private Executor queryExecutor;

    @Override
    @SuppressWarnings("unchecked")
    public <R> R send(IQuery<R> query) {
        return (R) super.send(query);
    }

    @Override
    public <R> CompletableFuture<R> sendAsync(IQuery<R> query) {
        return super.sendAsync(query);
    }

    @Override
    protected Executor getExecutor() {
        return queryExecutor;
    }

    @Override
    protected Class<IQueryHandler<?, ?>> getHandlerType() {
        return (Class) IQueryHandler.class;
    }

    @Override
    protected String getMessageTypeName() {
        return "查询";
    }

    @Override
    protected boolean isValid(IQuery<?> message) {
        return message.isValid();
    }

    @Override
    @SuppressWarnings("unchecked")
    protected <R> R handleMessage(IQueryHandler<?, ?> handler, IQuery<?> message) {
        return (R) ((IQueryHandler<IQuery<?>, ?>) handler).handle(message);
    }

    protected boolean isHandlerForMessage(IQueryHandler<?, ?> handler, Class<?> messageClass) {
        return GenericTypeResolver.findImplementation(Collections.singletonList(handler), IQueryHandler.class, messageClass).isPresent();
    }
}
