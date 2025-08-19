package io.github.anthem37.ddd.domain.model;

import java.io.Serializable;
import java.util.Arrays;

/**
 * 值对象抽象基类
 * 提供通用的相等性比较和哈希码计算
 * <p>
 * 子类应该显式实现 getEqualityComponents() 方法，明确指定哪些属性参与相等性比较
 *
 * @author anthem37
 * @date 2025/8/13 15:12:36
 */
public abstract class AbstractValueObject implements IValueObject, Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 相等性比较
     * 基于 getEqualityComponents() 返回的属性进行比较
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        AbstractValueObject other = (AbstractValueObject) obj;
        return Arrays.deepEquals(getEqualityComponents(), other.getEqualityComponents());
    }

    /**
     * 哈希码计算
     * 基于 getEqualityComponents() 返回的属性计算哈希码
     */
    @Override
    public int hashCode() {
        return Arrays.deepHashCode(getEqualityComponents());
    }

    /**
     * 字符串表示
     * 包含类名和相等性组件
     */
    @Override
    public String toString() {
        return getClass().getSimpleName() + Arrays.toString(getEqualityComponents());
    }

    /**
     * 获取用于相等性比较的组件
     * 子类必须重写此方法，明确指定哪些属性参与相等性比较
     * <p>
     * 示例实现:
     * <pre>
     * {@code
     * @Override
     * protected Object[] getEqualityComponents() {
     *     return new Object[] { firstName, lastName, email };
     * }
     * }
     * </pre>
     *
     * @return 用于相等性比较的属性数组
     */
    protected abstract Object[] getEqualityComponents();

    /**
     * 验证值对象的业务规则
     * 子类应该重写此方法来实现自验证
     */
    protected void validate() {
        // 默认实现为空，子类可以重写
    }

    /**
     * 创建值对象的副本
     * 子类必须实现此方法，提供深拷贝功能
     *
     * @return 值对象的副本
     */
    public abstract AbstractValueObject copy();
}
