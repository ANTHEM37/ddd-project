package com.ddd.interfaces.facade;

import com.ddd.application.command.ICommand;
import com.ddd.application.command.ICommandBus;
import com.ddd.application.query.IQuery;
import com.ddd.application.query.IQueryBus;

import javax.annotation.Resource;

/**
 * 门面基类
 * 提供与应用层交互的基础设施
 *
 * @author anthem37
 * @date 2025/8/13 18:32:47
 */
public abstract class AbstractBaseFacade {

    @Resource
    protected ICommandBus commandBus;

    @Resource
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