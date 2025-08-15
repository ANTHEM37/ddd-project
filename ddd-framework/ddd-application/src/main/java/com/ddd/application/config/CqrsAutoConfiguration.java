package com.ddd.application.config;

import com.ddd.application.command.ICommand;
import com.ddd.application.command.ICommandBus;
import com.ddd.application.command.ICommandHandler;
import com.ddd.application.query.IQuery;
import com.ddd.application.query.IQueryBus;
import com.ddd.application.query.IQueryHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.ResolvableType;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * CQRS自动配置
 * 应用层负责自动扫描并注册CommandHandler和QueryHandler
 *
 * @author anthem37
 * @date 2025/8/14 09:53:12
 */
@Slf4j
@Component
public class CqrsAutoConfiguration {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private ICommandBus commandBus;

    @Autowired
    private IQueryBus queryBus;

    /**
     * 在Spring容器启动完成后自动注册所有处理器
     */
    @EventListener
    public void registerHandlers(ContextRefreshedEvent event) {
        registerCommandHandlers();
        registerQueryHandlers();
    }

    /**
     * 自动注册命令处理器
     * 注意：当前CommandBus和QueryBus实现已经内置了处理器发现机制，无需手动注册
     */
    private void registerCommandHandlers() {
        Map<String, ICommandHandler> handlers = applicationContext.getBeansOfType(ICommandHandler.class);
        log.info("发现命令处理器数量: {}", handlers.size());

        for (Map.Entry<String, ICommandHandler> entry : handlers.entrySet()) {
            ICommandHandler<?, ?> handler = entry.getValue();
            Class<?> commandClass = getCommandType(handler);

            if (commandClass != null && ICommand.class.isAssignableFrom(commandClass)) {
                log.debug("发现命令处理器: {} -> {}", commandClass.getSimpleName(), entry.getKey());
            }
        }
    }

    /**
     * 自动注册查询处理器
     * 注意：当前CommandBus和QueryBus实现已经内置了处理器发现机制，无需手动注册
     */
    private void registerQueryHandlers() {
        Map<String, IQueryHandler> handlers = applicationContext.getBeansOfType(IQueryHandler.class);
        log.info("发现查询处理器数量: {}", handlers.size());

        for (Map.Entry<String, IQueryHandler> entry : handlers.entrySet()) {
            IQueryHandler<? extends IQuery<?>, ?> handler = entry.getValue();
            Class<?> queryClass = getQueryType(handler);

            if (queryClass != null && IQuery.class.isAssignableFrom(queryClass)) {
                log.debug("发现查询处理器: {} -> {}", queryClass.getSimpleName(), entry.getKey());
            }
        }
    }

    /**
     * 使用Spring的ResolvableType获取命令类型
     */
    private Class<?> getCommandType(ICommandHandler<?, ?> handler) {
        ResolvableType resolvableType = ResolvableType.forClass(handler.getClass())
                .as(ICommandHandler.class);

        if (resolvableType.hasGenerics()) {
            ResolvableType commandType = resolvableType.getGeneric(0);
            return commandType.resolve();
        }

        return null;
    }

    /**
     * 使用Spring的ResolvableType获取查询类型
     */
    private Class<?> getQueryType(IQueryHandler<? extends IQuery<?>, ?> handler) {
        ResolvableType resolvableType = ResolvableType.forClass(handler.getClass())
                .as(IQueryHandler.class);

        if (resolvableType.hasGenerics()) {
            ResolvableType queryType = resolvableType.getGeneric(0);
            return queryType.resolve();
        }

        return null;
    }
}