package io.github.anthem37.ddd.application.converter;

import io.github.anthem37.ddd.application.query.IQuery;

/**
 * 查询转换器接口
 * 负责Query与查询条件之间的转换
 *
 * @param <Q> 查询类型
 * @param <C> 查询条件类型
 * @author anthem37
 * @date 2025/8/19 11:00:00
 */
public interface IQueryConverter<Q extends IQuery<?>, C> {

    /**
     * 将查询转换为查询条件
     *
     * @param query 查询对象
     * @return 查询条件
     */
    C convert(Q query);

    /**
     * 验证查询是否可以转换
     *
     * @param query 查询对象
     * @return 是否可以转换
     */
    default boolean canConvert(Q query) {
        return query != null;
    }
}