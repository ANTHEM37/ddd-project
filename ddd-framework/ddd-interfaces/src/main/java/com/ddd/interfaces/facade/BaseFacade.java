package com.ddd.interfaces.facade;

import com.ddd.application.command.Command;
import com.ddd.application.command.CommandBus;
import com.ddd.application.query.Query;
import com.ddd.application.query.QueryBus;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 门面基类
 * 提供与应用层交互的基础设施
 * 
 * @author anthem37
 * @date 2025/8/13 18:32:47
 */
public abstract class BaseFacade {

    @Autowired
    protected CommandBus commandBus;

    @Autowired
    protected QueryBus queryBus;

    /**
     * 发送命令
     */
    protected <R> R sendCommand(Command<R> command) {
        return commandBus.send(command);
    }

    /**
     * 发送查询
     */
    protected <T extends Query<R>, R> R sendQuery(T query) {
        return queryBus.send(query);
    }
}