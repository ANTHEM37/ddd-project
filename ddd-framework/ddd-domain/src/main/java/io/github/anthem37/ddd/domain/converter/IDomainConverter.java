package io.github.anthem37.ddd.domain.converter;

/**
 * 领域转换器接口
 * 负责领域层内部对象之间的转换
 *
 * @param <S> 源类型
 * @param <T> 目标类型
 * @author anthem37
 * @date 2025/8/19 11:00:00
 */
public interface IDomainConverter<S, T> {

    /**
     * 将源对象转换为目标对象
     *
     * @param source 源对象
     * @return 目标对象
     */
    T convert(S source);

    /**
     * 反向转换（如果支持）
     *
     * @param target 目标对象
     * @return 源对象
     */
    default S reverse(T target) {
        throw new UnsupportedOperationException("反向转换未实现");
    }

    /**
     * 检查是否支持转换
     *
     * @param source 源对象
     * @return 是否支持转换
     */
    default boolean supports(S source) {
        return source != null;
    }
}