package com.ddd.application.command;

import com.ddd.application.bus.AbstractMessageBus;
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
public class CommandBusImpl extends AbstractMessageBus<Command<?>, CommandHandler<?, ?>> implements CommandBus {

    @Autowired
    @Qualifier("taskExecutor")
    private Executor taskExecutor;

    @Override
    @SuppressWarnings("unchecked")
    public <R> R send(Command<R> command) {

        return super.send(command);
    }

    @Override
    public <R> CompletableFuture<R> sendAsync(Command<R> command) {
        return super.sendAsync(command);
    }

    @Override
    protected Executor getExecutor() {
        return taskExecutor;
    }

    @Override
    protected Class<CommandHandler<?, ?>> getHandlerType() {
        return (Class) CommandHandler.class;
    }

    @Override
    protected String getMessageTypeName() {
        return "命令";
    }

    @Override
    protected boolean isValid(Command<?> message) {
        return message.isValid();
    }

    @Override
    @SuppressWarnings("unchecked")
    protected <R> R handleMessage(CommandHandler<?, ?> handler, Command<?> message) {
        return (R) ((CommandHandler<Command<?>, ?>) handler).handle(message);
    }

    protected boolean isHandlerForMessage(CommandHandler<?, ?> handler, Class<?> messageClass) {
        return GenericTypeResolver.findImplementation(Collections.singletonList(handler), CommandHandler.class, messageClass).isPresent();
    }
}
