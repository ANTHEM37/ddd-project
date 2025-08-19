package io.github.anthem37.ddd.domain.model;

import io.github.anthem37.ddd.common.exception.BusinessRuleViolationException;
import io.github.anthem37.ddd.common.model.IBusinessRule;
import io.github.anthem37.ddd.domain.event.IDomainEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 聚合根单元测试
 *
 * @author anthem37
 * @date 2025/8/14 10:30:00
 */
class AggregateRootTest {

    private TestAggregateRoot aggregateRoot;

    @BeforeEach
    void setUp() {
        aggregateRoot = new TestAggregateRoot("test-id");
    }

    @Test
    void testConstructor() {
        assertEquals("test-id", aggregateRoot.getId());
        assertEquals(0, aggregateRoot.getVersion());
        assertFalse(aggregateRoot.isRemoved());
        assertFalse(aggregateRoot.hasUnpublishedEvents());
    }

    @Test
    void testIncrementVersion() {
        assertEquals(0, aggregateRoot.getVersion());

        aggregateRoot.incrementVersion();
        assertEquals(1, aggregateRoot.getVersion());

        aggregateRoot.incrementVersion();
        assertEquals(2, aggregateRoot.getVersion());
    }

    @Test
    void testDomainEventOperations() {
        // 初始状态
        assertFalse(aggregateRoot.hasUnpublishedEvents());
        assertTrue(aggregateRoot.getDomainEvents().isEmpty());

        // 添加事件
        aggregateRoot.addTestEvent("event1");
        assertTrue(aggregateRoot.hasUnpublishedEvents());
        assertEquals(1, aggregateRoot.getDomainEvents().size());

        // 添加更多事件
        aggregateRoot.addTestEvent("event2");
        assertEquals(2, aggregateRoot.getDomainEvents().size());

        // 清除事件
        aggregateRoot.clearDomainEvents();
        assertFalse(aggregateRoot.hasUnpublishedEvents());
        assertTrue(aggregateRoot.getDomainEvents().isEmpty());
    }

    @Test
    void testGetDomainEvents_Immutable() {
        aggregateRoot.addTestEvent("event1");
        List<IDomainEvent> events = aggregateRoot.getDomainEvents();

        // 尝试修改返回的列表应该抛出异常
        assertThrows(UnsupportedOperationException.class, () -> {
            events.add(new TestDomainEvent("should-fail"));
        });
    }

    @Test
    void testCheckRule_ValidRule() {
        IBusinessRule validRule = new SatisfiedRule();

        // 有效规则不应该抛出异常
        assertDoesNotThrow(() -> aggregateRoot.checkTestRule(validRule));
    }

    @Test
    void testCheckRule_InvalidRule() {
        IBusinessRule invalidRule = new UnsatisfiedRule();

        // 无效规则应该抛出异常
        assertThrows(BusinessRuleViolationException.class, () -> {
            aggregateRoot.checkTestRule(invalidRule);
        });
    }

    @Test
    void testCheckRules_AllValid() {
        IBusinessRule rule1 = new SatisfiedRule();
        IBusinessRule rule2 = new SatisfiedRule();

        // 所有规则都有效不应该抛出异常
        assertDoesNotThrow(() -> aggregateRoot.checkTestRules(rule1, rule2));
    }

    @Test
    void testCheckRules_OneInvalid() {
        IBusinessRule validRule = new SatisfiedRule();
        IBusinessRule invalidRule = new UnsatisfiedRule();

        // 有一个规则无效应该抛出异常
        assertThrows(BusinessRuleViolationException.class, () -> {
            aggregateRoot.checkTestRules(validRule, invalidRule);
        });
    }

    @Test
    void testMarkAsRemoved() {
        assertFalse(aggregateRoot.isRemoved());
        assertFalse(aggregateRoot.hasUnpublishedEvents());

        aggregateRoot.markAsRemoved();

        assertTrue(aggregateRoot.isRemoved());
        assertTrue(aggregateRoot.hasUnpublishedEvents());

        // 验证删除事件被添加
        List<IDomainEvent> events = aggregateRoot.getDomainEvents();
        assertEquals(1, events.size());
        assertEquals("TestAggregateDeleted", events.get(0).getEventType());
    }

    @Test
    void testSafeRemove() {
        assertFalse(aggregateRoot.isRemoved());

        aggregateRoot.safeRemove();

        assertTrue(aggregateRoot.isRemoved());
        assertTrue(aggregateRoot.hasUnpublishedEvents());
    }

    @Test
    void testSafeRemove_WithCannotBeRemovedRule() {
        aggregateRoot.setCanBeRemoved(false);

        // 当不能删除时应该抛出异常
        assertThrows(BusinessRuleViolationException.class, () -> {
            aggregateRoot.safeRemove();
        });

        // 聚合应该仍然未被删除
        assertFalse(aggregateRoot.isRemoved());
    }

    @Test
    void testGetBusinessIdentifier() {
        String identifier = aggregateRoot.getBusinessIdentifier();
        assertEquals("TestAggregateRoot:test-id", identifier);
    }

    @Test
    void testAfterBusinessOperation() {
        int initialVersion = aggregateRoot.getVersion();

        aggregateRoot.performBusinessOperation();

        // 版本应该递增
        assertEquals(initialVersion + 1, aggregateRoot.getVersion());
        // 不变性检查应该被调用
        assertTrue(aggregateRoot.isInvariantsValidated());
    }

    @Test
    void testAfterBusinessOperation_InvalidInvariants() {
        aggregateRoot.setInvariantsValid(false);

        // 不变性无效时应该抛出异常
        assertThrows(BusinessRuleViolationException.class, () -> {
            aggregateRoot.performBusinessOperation();
        });
    }

    // 测试用的业务规则实现
    private static class SatisfiedRule implements IBusinessRule {
        @Override
        public boolean isSatisfied() {
            return true;
        }

        @Override
        public String getMessage() {
            return "规则满足";
        }
    }

    private static class UnsatisfiedRule implements IBusinessRule {
        @Override
        public boolean isSatisfied() {
            return false;
        }

        @Override
        public String getMessage() {
            return "规则不满足";
        }
    }

    // 测试用的聚合根实现
    private static class TestAggregateRoot extends AbstractAggregateRoot<String> {
        private boolean canBeRemoved = true;
        private boolean invariantsValid = true;
        private boolean invariantsValidated = false;

        public TestAggregateRoot(String id) {
            super(id);
        }

        public void addTestEvent(String data) {
            addDomainEvent(new TestDomainEvent(data));
        }

        public void checkTestRule(IBusinessRule rule) {
            checkRule(rule);
        }

        public void checkTestRules(IBusinessRule... rules) {
            checkRules(rules);
        }

        public void setCanBeRemoved(boolean canBeRemoved) {
            this.canBeRemoved = canBeRemoved;
        }

        public void setInvariantsValid(boolean invariantsValid) {
            this.invariantsValid = invariantsValid;
        }

        public boolean isInvariantsValidated() {
            return invariantsValidated;
        }

        public void performBusinessOperation() {
            afterBusinessOperation();
        }

        @Override
        protected void addDeletedDomainEvent() {
            addDomainEvent(new TestDomainEvent("deleted") {
                @Override
                public String getEventType() {
                    return "TestAggregateDeleted";
                }
            });
        }

        @Override
        protected void checkCanBeRemoved() {
            if (!canBeRemoved) {
                throw new BusinessRuleViolationException("聚合不能被删除");
            }
        }

        @Override
        protected void validateInvariants() {
            invariantsValidated = true;
            if (!invariantsValid) {
                throw new BusinessRuleViolationException("聚合不变性验证失败");
            }
        }
    }

    // 测试用的领域事件实现
    private static class TestDomainEvent implements IDomainEvent {
        private final String data;
        private final LocalDateTime occurredOn;

        public TestDomainEvent(String data) {
            this.data = data;
            this.occurredOn = LocalDateTime.now();
        }

        @Override
        public String getEventType() {
            return "TestDomainEvent";
        }

        @Override
        public LocalDateTime getOccurredOn() {
            return occurredOn;
        }

        public String getData() {
            return data;
        }
    }
}