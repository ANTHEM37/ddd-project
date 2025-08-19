package io.github.anthem37.ddd.infrastructure.converter;

import io.github.anthem37.ddd.common.exception.BusinessException;
import io.github.anthem37.ddd.common.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 持久化转换器抽象基类
 * 提供批量转换和异常处理的默认实现
 *
 * @param <D> 领域模型类型
 * @param <P> 持久化对象类型
 * @author anthem37
 * @date 2025/8/19 11:00:00
 */
public abstract class AbstractPersistenceConverter<D, P> implements IPersistenceConverter<D, P> {

    @Override
    public P toPersistence(D domain) {
        if (domain == null) {
            return null;
        }

        if (!supports(domain)) {
            throw new BusinessException("不支持的领域模型类型：" + domain.getClass().getSimpleName());
        }

        try {
            return doToPersistence(domain);
        } catch (Exception e) {
            throw new BusinessException("领域模型转持久化对象失败：" + e.getMessage(), e);
        }
    }

    @Override
    public D toDomain(P persistence) {
        if (persistence == null) {
            return null;
        }

        try {
            return doToDomain(persistence);
        } catch (Exception e) {
            throw new BusinessException("持久化对象转领域模型失败：" + e.getMessage(), e);
        }
    }

    /**
     * 批量转换领域模型为持久化对象
     *
     * @param domains 领域模型列表
     * @return 持久化对象列表
     */
    public List<P> toPersistenceList(List<D> domains) {
        if (CollectionUtils.isEmpty(domains)) {
            return Collections.emptyList();
        }
        return domains.stream()
                .map(this::toPersistence)
                .collect(Collectors.toList());
    }

    /**
     * 批量转换持久化对象为领域模型
     *
     * @param persistences 持久化对象列表
     * @return 领域模型列表
     */
    public List<D> toDomainList(List<P> persistences) {
        if (CollectionUtils.isEmpty(persistences)) {
            return Collections.emptyList();
        }
        return persistences.stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    /**
     * 执行领域模型到持久化对象的转换
     * 子类必须实现此方法
     *
     * @param domain 领域模型
     * @return 持久化对象
     */
    protected abstract P doToPersistence(D domain);

    /**
     * 执行持久化对象到领域模型的转换
     * 子类必须实现此方法
     *
     * @param persistence 持久化对象
     * @return 领域模型
     */
    protected abstract D doToDomain(P persistence);
}