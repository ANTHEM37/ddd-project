package io.github.anthem37.ddd.common.cqrs.converter;

import io.github.anthem37.ddd.common.cqrs.query.IQuery;
import io.github.anthem37.ddd.common.exception.BusinessException;

/**
 * 查询转换器抽象基类
 * 提供通用的查询转换逻辑
 *
 * @param <Q> 查询类型
 * @param <C> 查询条件类型
 * @author anthem37
 * @date 2025/8/19 11:00:00
 */
public abstract class AbstractQueryConverter<Q extends IQuery<?>, C> implements IQueryConverter<Q, C> {

    @Override
    public C convert(Q query) {
        if (!canConvert(query)) {
            throw new BusinessException("查询转换失败：查询对象无效或为空");
        }

        try {
            preProcess(query);
            C condition = doConvert(query);
            postProcess(query, condition);
            return condition;
        } catch (Exception e) {
            throw new BusinessException("查询转换过程中发生错误：" + e.getMessage(), e);
        }
    }

    /**
     * 执行具体的转换逻辑
     * 子类必须实现此方法
     *
     * @param query 查询对象
     * @return 查询条件
     */
    protected abstract C doConvert(Q query);

    /**
     * 转换前的预处理
     * 子类可以重写此方法进行自定义预处理
     *
     * @param query 查询对象
     */
    protected void preProcess(Q query) {
        // 默认空实现
    }

    /**
     * 转换后的后处理
     * 子类可以重写此方法进行自定义后处理
     *
     * @param query     查询对象
     * @param condition 转换后的查询条件
     */
    protected void postProcess(Q query, C condition) {
        // 默认空实现
    }

    /**
     * 验证查询参数
     * 子类可以重写此方法进行自定义验证
     *
     * @param query 查询对象
     * @return 验证是否通过
     */
    protected boolean validateQuery(Q query) {
        return true;
    }

    @Override
    public boolean canConvert(Q query) {
        return query != null && validateQuery(query);
    }
}