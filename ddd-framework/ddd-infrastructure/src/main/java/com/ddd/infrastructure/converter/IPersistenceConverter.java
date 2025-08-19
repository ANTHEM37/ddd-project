package com.ddd.infrastructure.converter;

/**
 * 持久化转换器接口
 * 负责领域模型与持久化对象之间的转换
 *
 * @param <D> 领域模型类型
 * @param <P> 持久化对象类型
 * @author anthem37
 * @date 2025/8/19 11:00:00
 */
public interface IPersistenceConverter<D, P> {

    /**
     * 将领域模型转换为持久化对象
     *
     * @param domain 领域模型
     * @return 持久化对象
     */
    P toPersistence(D domain);

    /**
     * 将持久化对象转换为领域模型
     *
     * @param persistence 持久化对象
     * @return 领域模型
     */
    D toDomain(P persistence);

    /**
     * 检查是否支持转换
     *
     * @param domain 领域模型
     * @return 是否支持转换
     */
    default boolean supports(D domain) {
        return domain != null;
    }
}