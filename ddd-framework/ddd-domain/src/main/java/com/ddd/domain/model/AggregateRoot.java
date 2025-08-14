package com.ddd.domain.model;

import com.ddd.common.assertion.Assert;
import com.ddd.common.model.BusinessRule;
import com.ddd.domain.event.DomainEvent;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 聚合根基类
 * 聚合根是聚合的入口，负责维护业务不变性
 *
 * @param <ID> 聚合根标识类型
 * @author anthem37
 * @date 2025/8/14 09:27:38
 */
public abstract class AggregateRoot<ID> extends Entity<ID> {

    private final List<DomainEvent> domainEvents = new ArrayList<>();

    @Getter
    private int version = 0;

    @Getter
    private boolean removed = false;

    protected AggregateRoot(ID id) {
        super(id);
    }

    /**
     * 检查业务规则
     * 如果规则不满足则抛出异常
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
     * 增加版本号（用于并发控制）
     */
    public void incrementVersion() {
        this.version++;
    }

    /**
     * 添加领域事件
     */
    protected void addDomainEvent(DomainEvent event) {
        domainEvents.add(event);
    }

    /**
     * 获取所有领域事件（只读）
     */
    public List<DomainEvent> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    /**
     * 清除所有领域事件
     */
    public void clearDomainEvents() {
        domainEvents.clear();
    }

    /**
     * 检查是否有未发布的事件
     */
    public boolean hasUnpublishedEvents() {
        return !domainEvents.isEmpty();
    }

    /**
     * 标记聚合为已删除
     */
    public void markAsRemoved() {
        this.removed = true;
    }

    /**
     * 聚合删除事件
     * 使用Lombok简化代码
     */
    @Getter
    public static class AggregateRemovedEvent implements DomainEvent {
        private final Object aggregateId;

        public AggregateRemovedEvent(Object aggregateId) {
            this.aggregateId = aggregateId;
        }

        @Override
        public String getEventType() {
            return "AggregateRemoved";
        }

        // Lombok @Getter 已自动生成 getAggregateId() 方法
    }
}
