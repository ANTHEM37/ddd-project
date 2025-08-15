package com.ddd.common.model;

/**
 * 业务规则接口
 * 用于封装业务不变性和约束条件
 *
 * @author anthem37
 * @date 2025/8/14 12:37:19
 */
public interface IBusinessRule {

    /**
     * 检查业务规则是否满足
     *
     * @return true if rule is satisfied, false otherwise
     */
    boolean isSatisfied();

    /**
     * 获取规则违反时的错误消息
     *
     * @return error message when rule is violated
     */
    String getMessage();

    /**
     * 获取规则名称
     *
     * @return rule name for identification
     */
    default String getRuleName() {
        return this.getClass().getSimpleName();
    }
}