package com.ddd.application.config;

import com.ddd.application.command.Command;
import com.ddd.application.command.CommandBus;
import com.ddd.application.command.CommandHandler;
import com.ddd.application.query.Query;
import com.ddd.application.query.QueryBus;
import com.ddd.application.query.QueryHandler;
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
    private CommandBus commandBus;

    @Autowired
    private QueryBus queryBus;

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
        Map<String, CommandHandler> handlers = applicationContext.getBeansOfType(CommandHandler.class);
        log.info("发现命令处理器数量: {}", handlers.size());

        for (Map.Entry<String, CommandHandler> entry : handlers.entrySet()) {
            CommandHandler<?, ?> handler = entry.getValue();
            Class<?> commandClass = getCommandType(handler);

            if (commandClass != null && Command.class.isAssignableFrom(commandClass)) {
                log.debug("发现命令处理器: {} -> {}", commandClass.getSimpleName(), entry.getKey());
            }
        }
    }

    /**
     * 自动注册查询处理器
     * 注意：当前CommandBus和QueryBus实现已经内置了处理器发现机制，无需手动注册
     */
    private void registerQueryHandlers() {
        Map<String, QueryHandler> handlers = applicationContext.getBeansOfType(QueryHandler.class);
        log.info("发现查询处理器数量: {}", handlers.size());

        for (Map.Entry<String, QueryHandler> entry : handlers.entrySet()) {
            QueryHandler<? extends Query<?>, ?> handler = entry.getValue();
            Class<?> queryClass = getQueryType(handler);

            if (queryClass != null && Query.class.isAssignableFrom(queryClass)) {
                log.debug("发现查询处理器: {} -> {}", queryClass.getSimpleName(), entry.getKey());
            }
        }
    }

    /**
     * 使用Spring的ResolvableType获取命令类型
     */
    private Class<?> getCommandType(CommandHandler<?, ?> handler) {
        ResolvableType resolvableType = ResolvableType.forClass(handler.getClass())
                .as(CommandHandler.class);

        if (resolvableType.hasGenerics()) {
            ResolvableType commandType = resolvableType.getGeneric(0);
            return commandType.resolve();
        }

        return null;
    }

    /**
     * 使用Spring的ResolvableType获取查询类型
     */
    private Class<?> getQueryType(QueryHandler<? extends Query<?>, ?> handler) {
        ResolvableType resolvableType = ResolvableType.forClass(handler.getClass())
                .as(QueryHandler.class);

        if (resolvableType.hasGenerics()) {
            ResolvableType queryType = resolvableType.getGeneric(0);
            return queryType.resolve();
        }

        return null;
    }
}