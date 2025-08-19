package io.github.anthem37.ddd.common.assertion;

import io.github.anthem37.ddd.common.exception.BusinessException;
import io.github.anthem37.ddd.common.exception.BusinessRuleViolationException;
import io.github.anthem37.ddd.common.exception.OrchestrationException;
import io.github.anthem37.ddd.common.model.IBusinessRule;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Assert工具类单元测试
 *
 * @author anthem37
 * @date 2025/8/15 19:00:00
 */
class AssertTest {

    @Test
    void testIsTrue_Success() {
        // 正常情况不应抛出异常
        assertDoesNotThrow(() -> Assert.isTrue(true, "测试消息"));
    }

    @Test
    void testIsTrue_ThrowsException() {
        BusinessException exception = assertThrows(BusinessException.class,
                () -> Assert.isTrue(false, "测试失败消息"));
        assertEquals("测试失败消息", exception.getMessage());
    }

    @Test
    void testIsFalse_Success() {
        assertDoesNotThrow(() -> Assert.isFalse(false, "测试消息"));
    }

    @Test
    void testIsFalse_ThrowsException() {
        BusinessException exception = assertThrows(BusinessException.class,
                () -> Assert.isFalse(true, "测试失败消息"));
        assertEquals("测试失败消息", exception.getMessage());
    }

    @Test
    void testNotNull_Success() {
        assertDoesNotThrow(() -> Assert.notNull("not null", "测试消息"));
    }

    @Test
    void testNotNull_ThrowsException() {
        BusinessException exception = assertThrows(BusinessException.class,
                () -> Assert.notNull(null, "对象不能为空"));
        assertEquals("对象不能为空", exception.getMessage());
    }

    @Test
    void testIsNull_Success() {
        assertDoesNotThrow(() -> Assert.isNull(null, "测试消息"));
    }

    @Test
    void testIsNull_ThrowsException() {
        BusinessException exception = assertThrows(BusinessException.class,
                () -> Assert.isNull("not null", "对象必须为空"));
        assertEquals("对象必须为空", exception.getMessage());
    }

    @Test
    void testHasText_Success() {
        assertDoesNotThrow(() -> Assert.hasText("有内容", "测试消息"));
        assertDoesNotThrow(() -> Assert.hasText("  有内容  ", "测试消息"));
    }

    @Test
    void testHasText_ThrowsException() {
        BusinessException exception1 = assertThrows(BusinessException.class,
                () -> Assert.hasText(null, "文本不能为空"));
        assertEquals("文本不能为空", exception1.getMessage());

        BusinessException exception2 = assertThrows(BusinessException.class,
                () -> Assert.hasText("", "文本不能为空"));
        assertEquals("文本不能为空", exception2.getMessage());

        BusinessException exception3 = assertThrows(BusinessException.class,
                () -> Assert.hasText("   ", "文本不能为空"));
        assertEquals("文本不能为空", exception3.getMessage());
    }

    @Test
    void testHasLength_Success() {
        assertDoesNotThrow(() -> Assert.hasLength("测试", 1, 5, "长度测试"));
        assertDoesNotThrow(() -> Assert.hasLength("测试内容", 2, 10, "长度测试"));
    }

    @Test
    void testHasLength_ThrowsException() {
        BusinessException exception1 = assertThrows(BusinessException.class,
                () -> Assert.hasLength("测", 2, 5, "长度不符合要求"));
        assertEquals("长度不符合要求", exception1.getMessage());

        BusinessException exception2 = assertThrows(BusinessException.class,
                () -> Assert.hasLength("测试内容过长了", 1, 5, "长度不符合要求"));
        assertEquals("长度不符合要求", exception2.getMessage());
    }

    @Test
    void testNotEmpty_Collection_Success() {
        assertDoesNotThrow(() -> Assert.notEmpty(Arrays.asList("item"), "集合不能为空"));
    }

    @Test
    void testNotEmpty_Collection_ThrowsException() {
        BusinessException exception1 = assertThrows(BusinessException.class,
                () -> Assert.notEmpty(Collections.emptyList(), "集合不能为空"));
        assertEquals("集合不能为空", exception1.getMessage());

        BusinessException exception2 = assertThrows(BusinessException.class,
                () -> Assert.notEmpty((java.util.Collection<?>) null, "集合不能为空"));
        assertEquals("集合不能为空", exception2.getMessage());
    }

    @Test
    void testNotEmpty_Array_Success() {
        assertDoesNotThrow(() -> Assert.notEmpty(new String[]{"item"}, "数组不能为空"));
    }

    @Test
    void testNotEmpty_Array_ThrowsException() {
        BusinessException exception1 = assertThrows(BusinessException.class,
                () -> Assert.notEmpty(new String[0], "数组不能为空"));
        assertEquals("数组不能为空", exception1.getMessage());

        BusinessException exception2 = assertThrows(BusinessException.class,
                () -> Assert.notEmpty((Object[]) null, "数组不能为空"));
        assertEquals("数组不能为空", exception2.getMessage());
    }

    @Test
    void testIsNotNegative_Success() {
        assertDoesNotThrow(() -> Assert.isNotNegative(0, "数值不能为负"));
        assertDoesNotThrow(() -> Assert.isNotNegative(10, "数值不能为负"));
        assertDoesNotThrow(() -> Assert.isNotNegative(10.5, "数值不能为负"));
    }

    @Test
    void testIsNotNegative_ThrowsException() {
        BusinessException exception = assertThrows(BusinessException.class,
                () -> Assert.isNotNegative(-1, "数值不能为负"));
        assertEquals("数值不能为负", exception.getMessage());
    }

    @Test
    void testInRange_Success() {
        assertDoesNotThrow(() -> Assert.inRange(5, 1, 10, "数值超出范围"));
        assertDoesNotThrow(() -> Assert.inRange(1, 1, 10, "数值超出范围"));
        assertDoesNotThrow(() -> Assert.inRange(10, 1, 10, "数值超出范围"));
    }

    @Test
    void testInRange_ThrowsException() {
        BusinessException exception1 = assertThrows(BusinessException.class,
                () -> Assert.inRange(0, 1, 10, "数值超出范围"));
        assertEquals("数值超出范围", exception1.getMessage());

        BusinessException exception2 = assertThrows(BusinessException.class,
                () -> Assert.inRange(11, 1, 10, "数值超出范围"));
        assertEquals("数值超出范围", exception2.getMessage());
    }

    @Test
    void testSatisfies_Success() {
        IBusinessRule rule = new IBusinessRule() {
            @Override
            public boolean isSatisfied() {
                return true;
            }

            @Override
            public String getMessage() {
                return "规则满足";
            }
        };

        assertDoesNotThrow(() -> Assert.satisfies(rule));
    }

    @Test
    void testSatisfies_ThrowsException() {
        IBusinessRule rule = new IBusinessRule() {
            @Override
            public boolean isSatisfied() {
                return false;
            }

            @Override
            public String getMessage() {
                return "规则不满足";
            }

            @Override
            public String getRuleName() {
                return "TestRule";
            }
        };

        BusinessRuleViolationException exception = assertThrows(BusinessRuleViolationException.class,
                () -> Assert.satisfies(rule));
        assertEquals("规则不满足", exception.getMessage());
        assertEquals("TestRule", exception.getRuleName());
    }

    @Test
    void testSatisfiesAll_Success() {
        IBusinessRule rule1 = createSatisfiedRule("规则1");
        IBusinessRule rule2 = createSatisfiedRule("规则2");

        assertDoesNotThrow(() -> Assert.satisfiesAll(rule1, rule2));
    }

    @Test
    void testSatisfiesAll_ThrowsException() {
        IBusinessRule rule1 = createSatisfiedRule("规则1");
        IBusinessRule rule2 = createUnsatisfiedRule("规则2不满足", "Rule2");

        BusinessRuleViolationException exception = assertThrows(BusinessRuleViolationException.class,
                () -> Assert.satisfiesAll(rule1, rule2));
        assertEquals("规则2不满足", exception.getMessage());
        assertEquals("Rule2", exception.getRuleName());
    }

    @Test
    void testOrchestrationAssertions() {
        // 测试编排专用断言方法
        OrchestrationException exception1 = assertThrows(OrchestrationException.class,
                () -> Assert.orchestrationHasText("", "编排文本不能为空"));
        assertEquals("编排文本不能为空", exception1.getMessage());

        OrchestrationException exception2 = assertThrows(OrchestrationException.class,
                () -> Assert.orchestrationNotNull(null, "编排对象不能为空"));
        assertEquals("编排对象不能为空", exception2.getMessage());

        OrchestrationException exception3 = assertThrows(OrchestrationException.class,
                () -> Assert.orchestrationIsTrue(false, "编排条件不满足"));
        assertEquals("编排条件不满足", exception3.getMessage());

        OrchestrationException exception4 = assertThrows(OrchestrationException.class,
                () -> Assert.orchestrationFail("编排直接失败"));
        assertEquals("编排直接失败", exception4.getMessage());
    }

    @Test
    void testMatches_Success() {
        assertDoesNotThrow(() -> Assert.matches("test@example.com", ".*@.*\\..*", "邮箱格式错误"));
    }

    @Test
    void testMatches_ThrowsException() {
        BusinessException exception = assertThrows(BusinessException.class,
                () -> Assert.matches("invalid-email", ".*@.*\\..*", "邮箱格式错误"));
        assertEquals("邮箱格式错误", exception.getMessage());
    }

    @Test
    void testEquals_Success() {
        assertDoesNotThrow(() -> Assert.equals("test", "test", "值不相等"));
        assertDoesNotThrow(() -> Assert.equals(null, null, "值不相等"));
    }

    @Test
    void testEquals_ThrowsException() {
        BusinessException exception = assertThrows(BusinessException.class,
                () -> Assert.equals("test1", "test2", "值不相等"));
        assertEquals("值不相等", exception.getMessage());
    }

    @Test
    void testFail() {
        BusinessException exception1 = assertThrows(BusinessException.class,
                () -> Assert.fail("直接失败"));
        assertEquals("直接失败", exception1.getMessage());

        BusinessException exception2 = assertThrows(BusinessException.class,
                () -> Assert.fail(() -> "延迟失败消息"));
        assertEquals("延迟失败消息", exception2.getMessage());
    }

    private IBusinessRule createSatisfiedRule(String message) {
        return new IBusinessRule() {
            @Override
            public boolean isSatisfied() {
                return true;
            }

            @Override
            public String getMessage() {
                return message;
            }
        };
    }

    private IBusinessRule createUnsatisfiedRule(String message, String ruleName) {
        return new IBusinessRule() {
            @Override
            public boolean isSatisfied() {
                return false;
            }

            @Override
            public String getMessage() {
                return message;
            }

            @Override
            public String getRuleName() {
                return ruleName;
            }
        };
    }
}