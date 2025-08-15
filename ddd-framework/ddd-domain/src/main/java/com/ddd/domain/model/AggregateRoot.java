package com.ddd.domain.model;

import com.ddd.common.assertion.Assert;
import com.ddd.common.model.IBusinessRule;
import com.ddd.domain.event.IDomainEvent;
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

    private final List<IDomainEvent> domainEvents = new ArrayList<>();

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
    protected void checkRule(IBusinessRule rule) {
        Assert.satisfies(rule);
    }

    /**
     * 批量检查业务规则
     */
    protected void checkRules(IBusinessRule... rules) {
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
    protected void addDomainEvent(IDomainEvent event) {
        domainEvents.add(event);
    }

    /**
     * 获取所有领域事件（只读）
     */
    public List<IDomainEvent> getDomainEvents() {
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
        addDeletedDomainEvent();
    }

    /**
     * 添加删除领域事件
     * 子类必须实现此方法来发布相应的删除事件
     */
    protected abstract void addDeletedDomainEvent();

    /**
     * 检查聚合是否可以被删除
     * 子类可以重写此方法来实现删除前的业务规则检查
     */
    protected void checkCanBeRemoved() {
        // 默认实现为空，子类可以重写
    }

    /**
     * 安全删除聚合（带业务规则检查）
     */
    public final void safeRemove() {
        checkCanBeRemoved();
        markAsRemoved();
    }

    /**
     * 获取聚合的业务标识
     * 用于日志和调试，子类可以重写提供更有意义的标识
     */
    public String getBusinessIdentifier() {
        return getClass().getSimpleName() + ":" + getId();
    }

    /**
     * 检查聚合状态是否有效
     * 子类应该重写此方法来验证聚合的业务不变性
     */
    protected void validateInvariants() {
        // 默认实现为空，子类应该重写
    }

    /**
     * 应用业务操作后的统一处理
     * 包括不变性检查和版本递增
     */
    protected final void afterBusinessOperation() {
        validateInvariants();
        incrementVersion();
    }
}
