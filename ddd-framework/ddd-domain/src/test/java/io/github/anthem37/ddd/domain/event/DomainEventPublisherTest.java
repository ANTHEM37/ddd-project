package io.github.anthem37.ddd.domain.event;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 领域事件发布器单元测试
 *
 * @author anthem37
 * @date 2025/8/15 19:50:00
 */
class DomainEventPublisherTest {

    private TestEventPublisher testPublisher;
    private List<IDomainEvent> publishedEvents;

    @BeforeEach
    void setUp() {
        publishedEvents = new ArrayList<>();
        testPublisher = new TestEventPublisher(publishedEvents);
        DomainEventPublisher.setEventPublisher(testPublisher);
    }

    @AfterEach
    void tearDown() {
        DomainEventPublisher.setEventPublisher(null);
    }

    @Test
    void testPublish_WithValidEvent() {
        // 创建测试事件
        TestDomainEvent event = new TestDomainEvent("test-data");

        // 发布事件
        DomainEventPublisher.publish(event);

        // 验证事件被发布
        assertEquals(1, publishedEvents.size());
        assertEquals(event, publishedEvents.get(0));
    }

    @Test
    void testPublish_WithNullEvent() {
        // 发布空事件
        DomainEventPublisher.publish(null);

        // 验证没有事件被发布
        assertEquals(0, publishedEvents.size());
    }

    @Test
    void testPublish_WithoutEventPublisher() {
        // 清除事件发布器
        DomainEventPublisher.setEventPublisher(null);

        // 创建测试事件
        TestDomainEvent event = new TestDomainEvent("test-data");

        // 发布事件（不应该抛出异常）
        assertDoesNotThrow(() -> DomainEventPublisher.publish(event));

        // 验证没有事件被发布
        assertEquals(0, publishedEvents.size());
    }

    @Test
    void testSetEventPublisher() {
        TestEventPublisher newPublisher = new TestEventPublisher(new ArrayList<>());

        // 设置新的发布器
        DomainEventPublisher.setEventPublisher(newPublisher);

        // 发布事件
        TestDomainEvent event = new TestDomainEvent("test-data");
        DomainEventPublisher.publish(event);

        // 验证原发布器没有收到事件
        assertEquals(0, publishedEvents.size());
        // 验证新发布器收到了事件
        assertEquals(1, newPublisher.getPublishedEvents().size());
    }

    // 测试用的事件发布器实现
    private static class TestEventPublisher implements DomainEventPublisher.EventPublisher {
        private final List<IDomainEvent> publishedEvents;

        public TestEventPublisher(List<IDomainEvent> publishedEvents) {
            this.publishedEvents = publishedEvents;
        }

        @Override
        public void publish(IDomainEvent event) {
            publishedEvents.add(event);
        }

        public List<IDomainEvent> getPublishedEvents() {
            return publishedEvents;
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

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            TestDomainEvent that = (TestDomainEvent) obj;
            return data.equals(that.data);
        }

        @Override
        public int hashCode() {
            return data.hashCode();
        }
    }
}