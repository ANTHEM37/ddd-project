package io.github.anthem37.ddd.interfaces.facade;

import io.github.anthem37.ddd.application.command.ICommand;
import io.github.anthem37.ddd.application.command.ICommandBus;
import io.github.anthem37.ddd.application.query.IQuery;
import io.github.anthem37.ddd.application.query.IQueryBus;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 门面基类
 * 提供与应用层交互的基础设施
 *
 * @author anthem37
 * @date 2025/8/13 18:32:47
 */
public abstract class AbstractBaseFacade {

    @Autowired
    protected ICommandBus commandBus;

    @Autowired
    protected IQueryBus queryBus;

    /**
     * 发送命令
     */
    protected <R> R sendCommand(ICommand<R> command) {
        return commandBus.send(command);
    }

    /**
     * 发送查询
     */
    protected <T extends IQuery<R>, R> R sendQuery(T query) {
        return queryBus.send(query);
    }
}