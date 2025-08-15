package com.ddd.application.command.impl;

import com.ddd.application.bus.AbstractMessageBus;
import com.ddd.application.command.ICommand;
import com.ddd.application.command.ICommandBus;
import com.ddd.application.command.ICommandHandler;
import com.ddd.common.util.GenericTypeResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * 命令总线实现
 * 负责命令的路由和执行
 *
 * @author anthem37
 * @date 2025/8/13 21:05:46
 */
@Component
public class CommandBusImpl extends AbstractMessageBus<ICommand<?>, ICommandHandler<?, ?>> implements ICommandBus {

    @Autowired
    @Qualifier("taskExecutor")
    private Executor taskExecutor;

    @Override
    @SuppressWarnings("unchecked")
    public <R> R send(ICommand<R> command) {

        return super.send(command);
    }

    @Override
    public <R> CompletableFuture<R> sendAsync(ICommand<R> command) {
        return super.sendAsync(command);
    }

    @Override
    protected Executor getExecutor() {
        return taskExecutor;
    }

    @Override
    protected Class<ICommandHandler<?, ?>> getHandlerType() {
        return (Class) ICommandHandler.class;
    }

    @Override
    protected String getMessageTypeName() {
        return "命令";
    }

    @Override
    protected boolean isValid(ICommand<?> message) {
        return message.isValid();
    }

    @Override
    @SuppressWarnings("unchecked")
    protected <R> R handleMessage(ICommandHandler<?, ?> handler, ICommand<?> message) {
        return (R) ((ICommandHandler<ICommand<?>, ?>) handler).handle(message);
    }

    protected boolean isHandlerForMessage(ICommandHandler<?, ?> handler, Class<?> messageClass) {
        return GenericTypeResolver.findImplementation(Collections.singletonList(handler), ICommandHandler.class, messageClass).isPresent();
    }
}
