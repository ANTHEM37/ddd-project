package com.ddd.domain.converter;

import com.ddd.common.exception.BusinessException;
import com.ddd.common.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 领域转换器抽象基类
 * 提供通用的转换逻辑和批量处理
 *
 * @param <S> 源类型
 * @param <T> 目标类型
 * @author anthem37
 * @date 2025/8/19 11:00:00
 */
public abstract class AbstractDomainConverter<S, T> implements IDomainConverter<S, T> {

    @Override
    public T convert(S source) {
        if (source == null) {
            return null;
        }
        
        if (!supports(source)) {
            throw new BusinessException("不支持的转换类型：" + source.getClass().getSimpleName());
        }
        
        try {
            return doConvert(source);
        } catch (Exception e) {
            throw new BusinessException("领域对象转换失败：" + e.getMessage(), e);
        }
    }

    /**
     * 批量转换
     *
     * @param sources 源对象列表
     * @return 目标对象列表
     */
    public List<T> convertList(List<S> sources) {
        if (CollectionUtils.isEmpty(sources)) {
            return Collections.emptyList();
        }
        return sources.stream()
                .map(this::convert)
                .collect(Collectors.toList());
    }

    /**
     * 安全转换，处理null值
     *
     * @param source 源对象
     * @return 目标对象，如果输入为null则返回null
     */
    public T safeConvert(S source) {
        return source == null ? null : convert(source);
    }

    /**
     * 执行具体的转换逻辑
     * 子类必须实现此方法
     *
     * @param source 源对象
     * @return 目标对象
     */
    protected abstract T doConvert(S source);

    /**
     * 转换前的预处理
     * 子类可以重写此方法
     *
     * @param source 源对象
     */
    protected void preConvert(S source) {
        // 默认空实现
    }

    /**
     * 转换后的后处理
     * 子类可以重写此方法
     *
     * @param source 源对象
     * @param target 目标对象
     */
    protected void postConvert(S source, T target) {
        // 默认空实现
    }
}