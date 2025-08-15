package com.ddd.common.exception;

import com.ddd.common.model.IBusinessRule;
import lombok.Getter;

/**
 * 业务规则违反异常
 * 当业务不变性被违反时抛出
 *
 * @author anthem37
 * @date 2025/8/13 17:52:41
 */
@Getter
public class BusinessRuleViolationException extends BusinessException {

    private static final long serialVersionUID = 1L;

    /**
     * 违反的规则名称
     */
    private final String ruleName;

    /**
     * 构造函数
     *
     * @param message 异常消息
     */
    public BusinessRuleViolationException(String message) {
        super(message);
        this.ruleName = "Unknown";
    }

    /**
     * 构造函数
     *
     * @param rule 违反的业务规则
     */
    public BusinessRuleViolationException(IBusinessRule rule) {
        super(rule.getMessage());
        this.ruleName = rule.getRuleName();
        setErrorCode("RULE_VIOLATION");
    }

    /**
     * 构造函数
     *
     * @param message  异常消息
     * @param ruleName 规则名称
     */
    public BusinessRuleViolationException(String message, String ruleName) {
        super(message);
        this.ruleName = ruleName;
        setErrorCode("RULE_VIOLATION");
    }

    /**
     * 构造函数
     *
     * @param errorCode 错误码
     * @param message   异常消息
     * @param ruleName  规则名称
     */
    public BusinessRuleViolationException(String errorCode, String message, String ruleName) {
        super(errorCode, message);
        this.ruleName = ruleName;
    }

    /**
     * 构造函数
     *
     * @param message  异常消息
     * @param cause    原因异常
     * @param ruleName 规则名称
     */
    public BusinessRuleViolationException(String message, Throwable cause, String ruleName) {
        super(message, cause);
        this.ruleName = ruleName;
        setErrorCode("RULE_VIOLATION");
    }
}
