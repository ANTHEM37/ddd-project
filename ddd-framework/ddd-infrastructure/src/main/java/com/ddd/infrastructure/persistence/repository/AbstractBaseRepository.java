package com.ddd.infrastructure.persistence.repository;

import com.ddd.common.assertion.Assert;
import com.ddd.domain.event.DomainEventPublisher;
import com.ddd.domain.event.IDomainEvent;
import com.ddd.domain.model.AggregateRoot;
import com.ddd.domain.repository.IRepository;
import com.ddd.domain.specification.ISpecification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 仓储基础实现类
 * 提供通用的CRUD操作实现，支持规约模式和事件发布
 *
 * @param <T>  聚合根类型
 * @param <ID> 聚合根标识类型
 * @author anthem37
 * @date 2025/8/14 16:52:19
 */
@Slf4j
public abstract class AbstractBaseRepository<T extends AggregateRoot<ID>, ID> implements IRepository<T, ID> {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void save(T aggregate) {
        Assert.notNull(aggregate, "聚合不能为空");
        //删除
        if (aggregate.isRemoved()) {
            log.debug("删除聚合: {}", aggregate.getId());
            doDeleteById(aggregate.getId());
            // 发布领域事件
            publishDomainEvents(aggregate);
            return;
        }
        //更新
        if (existsById(aggregate.getId())) {
            log.debug("更新聚合: {}", aggregate.getId());
            doUpdate(aggregate);
            // 发布领域事件
            publishDomainEvents(aggregate);
            return;
        }
        //插入
        log.debug("插入聚合: {}", aggregate.getId());
        doInsert(aggregate);
        // 发布领域事件
        publishDomainEvents(aggregate);
    }

    @Override
    public Optional<T> findById(ID id) {
        Assert.notNull(id, "ID不能为空");
        return doFindById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(ID id) {
        Optional<T> aggregateOptional = findById(id);
        if (!aggregateOptional.isPresent()) {
            log.debug("删除聚合失败，聚合不存在: {}", id);
            return;
        }
        T aggregate = aggregateOptional.get();
        remove(aggregate);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void remove(T aggregate) {
        Assert.notNull(aggregate, "聚合不能为空");
        aggregate.markAsRemoved();
        save(aggregate);
    }

    @Override
    public List<T> findAll() {
        return doFindAll();
    }

    @Override
    public boolean existsById(ID id) {
        return doExistsById(id);
    }

    @Override
    public long count() {
        return doCount();
    }

    /**
     * 根据规约查找实体列表
     *
     * @param specification 查询规约
     * @return 满足条件的实体列表
     */
    public List<T> findBySpecification(ISpecification<T> specification) {
        List<T> allEntities = findAll();
        return allEntities.stream().filter(specification::isSatisfiedBy).collect(Collectors.toList());
    }

    /**
     * 检查是否存在满足规约的实体
     *
     * @param specification 查询规约
     * @return true if exists
     */
    public boolean exists(ISpecification<T> specification) {
        return !findBySpecification(specification).isEmpty();
    }

    /**
     * 统计满足规约的实体数量
     *
     * @param specification 查询规约
     * @return 实体数量
     */
    public long count(ISpecification<T> specification) {
        return findBySpecification(specification).size();
    }

    /**
     * 批量保存聚合根并发布事件
     *
     * @param aggregates 聚合根列表
     */
    @Transactional
    public void saveAll(List<T> aggregates) {
        for (T aggregate : aggregates) {
            save(aggregate);
        }
    }

    // 子类需要实现的抽象方法
    protected abstract Optional<T> doFindById(ID id);

    protected abstract void doInsert(T aggregate);

    protected abstract void doUpdate(T aggregate);

    protected abstract void doDeleteById(ID id);

    protected abstract List<T> doFindAll();

    protected abstract boolean doExistsById(ID id);

    protected abstract long doCount();

    /**
     * 发布领域事件
     */
    protected void publishDomainEvents(T aggregate) {
        List<IDomainEvent> events = aggregate.getDomainEvents();
        for (IDomainEvent event : events) {
            DomainEventPublisher.publish(event);
        }
        aggregate.clearDomainEvents();
    }
}
