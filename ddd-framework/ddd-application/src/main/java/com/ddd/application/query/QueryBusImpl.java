package com.ddd.application.query;

import com.ddd.application.bus.AbstractMessageBus;
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
public class QueryBusImpl extends AbstractMessageBus<Query<?>, QueryHandler<?, ?>> implements QueryBus {

    @Autowired
    @Qualifier("queryExecutor")
    private Executor queryExecutor;

    @Override
    @SuppressWarnings("unchecked")
    public <R> R send(Query<R> query) {
        return (R) super.send(query);
    }

    @Override
    public <R> CompletableFuture<R> sendAsync(Query<R> query) {
        return super.sendAsync(query);
    }

    @Override
    protected Executor getExecutor() {
        return queryExecutor;
    }

    @Override
    protected Class<QueryHandler<?, ?>> getHandlerType() {
        return (Class) QueryHandler.class;
    }

    @Override
    protected String getMessageTypeName() {
        return "查询";
    }

    @Override
    protected boolean isValid(Query<?> message) {
        return message.isValid();
    }

    @Override
    @SuppressWarnings("unchecked")
    protected <R> R handleMessage(QueryHandler<?, ?> handler, Query<?> message) {
        return (R) ((QueryHandler<Query<?>, ?>) handler).handle(message);
    }

    protected boolean isHandlerForMessage(QueryHandler<?, ?> handler, Class<?> messageClass) {
        return GenericTypeResolver.findImplementation(Collections.singletonList(handler), QueryHandler.class, messageClass).isPresent();
    }
}
