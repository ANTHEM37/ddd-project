package io.github.anthem37.ddd.domain.specification;

/**
 * 规约模式接口
 * 用于封装查询条件和业务规则
 *
 * @param <T> 规约适用的实体类型
 * @author anthem37
 * @date 2025/8/14 08:45:16
 */
public interface ISpecification<T> {

    /**
     * 检查实体是否满足规约条件
     *
     * @param candidate 待检查的实体
     * @return true if the entity satisfies the specification
     */
    boolean isSatisfiedBy(T candidate);

    /**
     * 与操作 - 组合两个规约
     *
     * @param other 另一个规约
     * @return 组合后的规约
     */
    default ISpecification<T> and(ISpecification<T> other) {
        return new AndSpecification<>(this, other);
    }

    /**
     * 或操作 - 组合两个规约
     *
     * @param other 另一个规约
     * @return 组合后的规约
     */
    default ISpecification<T> or(ISpecification<T> other) {
        return new OrSpecification<>(this, other);
    }

    /**
     * 非操作 - 取反规约
     *
     * @return 取反后的规约
     */
    default ISpecification<T> not() {
        return new NotSpecification<>(this);
    }

    /**
     * 与规约实现
     */
    class AndSpecification<T> implements ISpecification<T> {
        private final ISpecification<T> left;
        private final ISpecification<T> right;

        public AndSpecification(ISpecification<T> left, ISpecification<T> right) {
            this.left = left;
            this.right = right;
        }

        @Override
        public boolean isSatisfiedBy(T candidate) {
            return left.isSatisfiedBy(candidate) && right.isSatisfiedBy(candidate);
        }
    }

    /**
     * 或规约实现
     */
    class OrSpecification<T> implements ISpecification<T> {
        private final ISpecification<T> left;
        private final ISpecification<T> right;

        public OrSpecification(ISpecification<T> left, ISpecification<T> right) {
            this.left = left;
            this.right = right;
        }

        @Override
        public boolean isSatisfiedBy(T candidate) {
            return left.isSatisfiedBy(candidate) || right.isSatisfiedBy(candidate);
        }
    }

    /**
     * 非规约实现
     */
    class NotSpecification<T> implements ISpecification<T> {
        private final ISpecification<T> specification;

        public NotSpecification(ISpecification<T> specification) {
            this.specification = specification;
        }

        @Override
        public boolean isSatisfiedBy(T candidate) {
            return !specification.isSatisfiedBy(candidate);
        }
    }
}