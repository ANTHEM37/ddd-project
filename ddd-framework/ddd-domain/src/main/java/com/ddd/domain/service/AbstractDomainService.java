package com.ddd.domain.service;

import com.ddd.common.assertion.Assert;
import com.ddd.common.model.BusinessRule;
import com.ddd.domain.event.DomainEvent;
import com.ddd.domain.event.DomainEventPublisher;

/**
 * 领域服务抽象基类
 * 提供通用的业务规则检查和事件发布功能
 * 
 * @author anthem37
 * @date 2025/8/13 19:48:25
 */
public abstract class AbstractDomainService implements DomainService {

    /**
     * 检查业务规则
     */
    protected void checkRule(BusinessRule rule) {
        Assert.satisfies(rule);
    }

    /**
     * 批量检查业务规则
     */
    protected void checkRules(BusinessRule... rules) {
        Assert.satisfiesAll(rules);
    }

    /**
     * 发布领域事件
     */
    protected void publishEvent(DomainEvent event) {
        DomainEventPublisher.publish(event);
    }

    /**
     * 批量发布领域事件
     */
    protected void publishEvents(DomainEvent... events) {
        for (DomainEvent event : events) {
            publishEvent(event);
        }
    }

    /**
     * 执行业务操作并发布事件
     */
    protected <T> T executeAndPublish(BusinessOperation<T> operation, DomainEvent... events) {
        T result = operation.execute();
        publishEvents(events);
        return result;
    }

    /**
     * 业务操作函数式接口
     */
    @FunctionalInterface
    protected interface BusinessOperation<T> {
        T execute();
    }
}