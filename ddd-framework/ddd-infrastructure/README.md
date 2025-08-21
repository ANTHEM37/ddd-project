# DDD Infrastructure Module

DDD 框架的基础设施层模块，提供技术实现、数据持久化、外部服务集成、消息发布和框架自动配置功能。

## 📦 模块结构

```
ddd-infrastructure/
├── config/            # 自动配置
├── converter/         # 基础设施层转换器
├── cqrs/              # CQRS实现
│   └── bus/           # 命令和查询总线实现
├── messaging/         # 消息处理
│   └── event/         # 领域事件处理
└── persistence/       # 数据持久化
    └── repository/    # 仓储实现
```

## 🏗️ 核心组件

### 1. 自动配置 (Auto Configuration)

#### DDDFrameworkAutoConfiguration

框架的核心自动配置类，提供所有必要组件的自动装配。

```java

@Slf4j
@Configuration
@EnableAsync
@EnableAspectJAutoProxy
@EnableTransactionManagement
@ComponentScan(basePackages = {"io.github.anthem37.ddd.application", "io.github.anthem37.ddd.domain", "io.github.anthem37.ddd.infrastructure"})
public class DDDFrameworkAutoConfiguration {

    /**
     * 命令总线
     */
    @Bean
    @ConditionalOnMissingBean(ICommandBus.class)
    public ICommandBus commandBus(@Qualifier("commandExecutor") Executor commandExecutor) {
        return new CommandBus(commandExecutor);
    }

    /**
     * 查询总线
     */
    @Bean
    @ConditionalOnMissingBean(IQueryBus.class)
    public IQueryBus queryBus(@Qualifier("queryExecutor") Executor queryExecutor) {
        return new QueryBus(queryExecutor);
    }

    /**
     * 领域事件发布器
     */
    @Bean
    @ConditionalOnMissingBean(DomainEventPublisher.EventPublisher.class)
    public DomainEventPublisher.EventPublisher domainEventPublisher(
            ApplicationEventPublisher applicationEventPublisher,
            @Qualifier("eventExecutor") Executor eventExecutor) {
        return new SpringDomainEventPublisher(applicationEventPublisher, eventExecutor);
    }

    /**
     * 转换器管理器
     */
    @Bean
    @ConditionalOnMissingBean(ConverterRegistry.ConverterManager.class)
    public ConverterRegistry.ConverterManager converterManager() {
        return new SpringConverterManager();
    }
}
```

#### AsyncExecutorConfig

异步执行器配置，为不同类型的操作提供专门的线程池。

```java

@Configuration
@EnableAsync
@Slf4j
public class AsyncExecutorConfig {

    /**
     * 命令执行器
     * 用于处理写操作，线程数相对较少但执行时间可能较长
     */
    @Bean("commandExecutor")
    @ConditionalOnMissingBean(name = "commandExecutor")
    public Executor commandExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("Command-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();

        log.info("初始化命令执行器: corePoolSize=5, maxPoolSize=10, queueCapacity=100");

        return executor;
    }

    /**
     * 查询执行器
     * 用于处理读操作，通常线程数较多但执行时间较短
     */
    @Bean("queryExecutor")
    @ConditionalOnMissingBean(name = "queryExecutor")
    public Executor queryExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("Query-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();

        log.info("初始化查询执行器: corePoolSize=10, maxPoolSize=20, queueCapacity=200");

        return executor;
    }

    /**
     * 事件执行器
     * 用于处理领域事件，通常为异步执行
     */
    @Bean("eventExecutor")
    @ConditionalOnMissingBean(name = "eventExecutor")
    public Executor eventExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("Event-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();

        log.info("初始化事件执行器: corePoolSize=5, maxPoolSize=10, queueCapacity=100");

        return executor;
    }
}
```

### 2. CQRS 实现

CQRS (命令查询职责分离) 模式的具体实现位于 `cqrs/bus/impl` 包下，包括：

- `CommandBus`: 命令总线实现，负责命令的路由和执行
- `QueryBus`: 查询总线实现，负责查询的路由和执行
- `AbstractMessageBus`: 消息总线抽象基类，提供公共功能

### 3. 转换器

转换器组件位于 `converter` 包下，提供不同层级之间的数据转换功能：

- `AbstractEventConverter`: 领域事件转换器抽象基类
- `AbstractPersistenceConverter`: 持久化转换器抽象基类
- `IEventConverter`: 事件转换器接口
- `IPersistenceConverter`: 持久化转换器接口
- `SpringConverterManager`: 转换器管理器实现

### 4. 消息处理

消息处理组件位于 `messaging/event` 包下，提供领域事件的发布和处理功能：

- `AbstractEventHandler`: 事件处理器抽象基类
- `SpringDomainEventPublisher`: 基于 Spring 的领域事件发布器实现

### 5. 数据持久化

数据持久化组件位于 `persistence/repository` 包下，提供仓储模式的实现：

- `AbstractBaseRepository`: 仓储抽象基类，提供通用的 CRUD 操作

## 使用示例

### 1. 创建自定义仓储实现

```java

@Repository
public class UserRepositoryImpl extends AbstractBaseRepository<User, UserId> implements IUserRepository {

    public UserRepositoryImpl(JpaRepository<User, UserId> jpaRepository) {
        super(jpaRepository);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return jpaRepository.findOne((root, query, cb) -> {
            return cb.equal(root.get("username"), username);
        });
    }

    @Override
    public List<User> findActiveUsers() {
        return jpaRepository.findAll((root, query, cb) -> {
            return cb.isTrue(root.get("active"));
        });
    }
}
```

### 2. 创建领域事件处理器

```java

@Component
public class OrderConfirmedEventHandler extends AbstractEventHandler<OrderConfirmedEvent> {

    @Autowired
    private NotificationService notificationService;

    @Override
    public void handle(OrderConfirmedEvent event) {
        // 处理订单确认事件，例如发送通知
        notificationService.sendOrderConfirmation(event.getOrderId(), event.getTotalAmount());
    }
}
```

        return executor;
    }

    /**
     * 查询执行器
     * 用于处理读操作，线程数较多，执行时间相对较短
     */
    @Bean("queryExecutor")
    @ConditionalOnMissingBean(name = "queryExecutor")
    public Executor queryExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("Query-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();

        log.info("初始化查询执行器: corePoolSize=10, maxPoolSize=20, queueCapacity=200");
        return executor;
    }

    /**
     * 事件执行器
     * 用于处理领域事件，需要较高的并发能力
     */
    @Bean("eventExecutor")
    @ConditionalOnMissingBean(name = "eventExecutor")
    public Executor eventExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(8);
        executor.setMaxPoolSize(16);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("Event-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();

        log.info("初始化事件执行器: corePoolSize=8, maxPoolSize=16, queueCapacity=500");
        return executor;
    }

}

```

### 2. 消息处理 (Messaging)

#### SpringDomainEventPublisher

Spring 集成的领域事件发布器，将领域事件转换为 Spring 应用事件。

```java

@Slf4j
@RequiredArgsConstructor
public class SpringDomainEventPublisher implements DomainEventPublisher.EventPublisher, InitializingBean {

    private final ApplicationEventPublisher applicationEventPublisher;
    private final Executor eventExecutor;

    @Override
    public void publish(IDomainEvent event) {
        Assert.notNull(event, "领域事件不能为空");

        try {
            // 异步发布事件
            CompletableFuture.runAsync(() -> {
                try {
                    applicationEventPublisher.publishEvent(event);
                    log.debug("领域事件发布成功: {}", event.getEventType());
                } catch (Exception e) {
                    log.error("领域事件发布失败: {}, 错误: {}", event.getEventType(), e.getMessage(), e);
                }
            }, eventExecutor);

        } catch (Exception e) {
            log.error("领域事件发布异常: {}, 错误: {}", event.getEventType(), e.getMessage(), e);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // 注册到领域事件发布器
        DomainEventPublisher.setEventPublisher(this);
        log.info("Spring领域事件发布器初始化完成");
    }
}
```

#### AbstractEventHandler

事件处理器基类，提供统一的事件处理模板。

```java
public abstract class AbstractEventHandler<T extends IDomainEvent> {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    /**
     * 处理事件
     */
    @EventListener
    @Async("eventExecutor")
    public void handleEvent(T event) {
        if (!canHandle(event)) {
            return;
        }

        String eventType = event.getEventType();
        log.debug("开始处理领域事件: {}", eventType);

        try {
            doHandle(event);
            log.debug("领域事件处理成功: {}", eventType);
        } catch (Exception e) {
            log.error("领域事件处理失败: {}, 错误: {}", eventType, e.getMessage(), e);
            handleError(event, e);
        }
    }

    /**
     * 检查是否可以处理该事件
     */
    protected boolean canHandle(IDomainEvent event) {
        return getSupportedEventType().isInstance(event);
    }

    /**
     * 具体的事件处理逻辑
     */
    protected abstract void doHandle(T event);

    /**
     * 获取支持的事件类型
     */
    protected abstract Class<T> getSupportedEventType();

    /**
     * 错误处理
     */
    protected void handleError(T event, Exception e) {
        // 默认实现：记录错误日志
        // 子类可以重写实现自定义错误处理逻辑，如重试、死信队列等
        log.error("事件处理错误，事件类型: {}, 聚合ID: {}, 错误信息: {}",
                event.getEventType(), event.getAggregateId(), e.getMessage());
    }
}
```

#### 具体事件处理器示例

```java

@Component
@Slf4j
public class OrderCreatedEventHandler extends AbstractEventHandler<OrderCreatedEvent> {

    @Autowired
    private EmailService emailService;

    @Autowired
    private InventoryService inventoryService;

    @Override
    protected void doHandle(OrderCreatedEvent event) {
        String orderId = event.getAggregateId();

        // 发送订单确认邮件
        sendOrderConfirmationEmail(orderId);

        // 更新库存
        updateInventory(event.getOrderItems());

        // 记录业务日志
        log.info("订单创建事件处理完成: orderId={}, amount={}",
                orderId, event.getTotalAmount());
    }

    @Override
    protected Class<OrderCreatedEvent> getSupportedEventType() {
        return OrderCreatedEvent.class;
    }

    private void sendOrderConfirmationEmail(String orderId) {
        try {
            emailService.sendOrderConfirmation(orderId);
            log.debug("订单确认邮件发送成功: {}", orderId);
        } catch (Exception e) {
            log.error("订单确认邮件发送失败: {}, 错误: {}", orderId, e.getMessage());
            // 可以考虑重试或记录到失败队列
        }
    }

    private void updateInventory(List<OrderItem> items) {
        try {
            inventoryService.reserveItems(items);
            log.debug("库存预留成功: {} 个商品", items.size());
        } catch (Exception e) {
            log.error("库存预留失败: 错误: {}", e.getMessage());
            // 库存预留失败可能需要取消订单
        }
    }

    @Override
    protected void handleError(OrderCreatedEvent event, Exception e) {
        super.handleError(event, e);

        // 订单创建事件处理失败的特殊处理
        // 可能需要回滚订单状态或发送告警
        String orderId = event.getAggregateId();
        log.error("订单创建事件处理失败，可能需要人工介入: orderId={}", orderId);

        // 发送告警通知
        // alertService.sendAlert("订单事件处理失败", orderId, e.getMessage());
    }
}
```

### 3. 数据持久化 (Persistence)

#### AbstractBaseRepository

仓储基类，提供通用的数据访问功能。

```java
public abstract class AbstractBaseRepository<T, ID> implements IRepository<T, ID> {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    @Override
    public void save(T aggregate) {
        Assert.notNull(aggregate, "聚合根不能为空");

        try {
            doSave(aggregate);

            // 发布领域事件
            if (aggregate instanceof AbstractAggregateRoot) {
                publishDomainEvents((AbstractAggregateRoot<?>) aggregate);
            }

            log.debug("聚合根保存成功: {}", getAggregateIdentifier(aggregate));
        } catch (Exception e) {
            log.error("聚合根保存失败: {}, 错误: {}", getAggregateIdentifier(aggregate), e.getMessage(), e);
            throw new BusinessException("数据保存失败", e);
        }
    }

    @Override
    public Optional<T> findById(ID id) {
        Assert.notNull(id, "ID不能为空");

        try {
            T aggregate = doFindById(id);
            log.debug("聚合根查询: id={}, found={}", id, aggregate != null);
            return Optional.ofNullable(aggregate);
        } catch (Exception e) {
            log.error("聚合根查询失败: id={}, 错误: {}", id, e.getMessage(), e);
            throw new BusinessException("数据查询失败", e);
        }
    }

    @Override
    public void delete(T aggregate) {
        Assert.notNull(aggregate, "聚合根不能为空");

        try {
            // 标记为删除
            if (aggregate instanceof AbstractAggregateRoot) {
                ((AbstractAggregateRoot<?>) aggregate).safeRemove();
            }

            doDelete(aggregate);

            // 发布领域事件
            if (aggregate instanceof AbstractAggregateRoot) {
                publishDomainEvents((AbstractAggregateRoot<?>) aggregate);
            }

            log.debug("聚合根删除成功: {}", getAggregateIdentifier(aggregate));
        } catch (Exception e) {
            log.error("聚合根删除失败: {}, 错误: {}", getAggregateIdentifier(aggregate), e.getMessage(), e);
            throw new BusinessException("数据删除失败", e);
        }
    }

    @Override
    public void deleteById(ID id) {
        Optional<T> aggregate = findById(id);
        if (aggregate.isPresent()) {
            delete(aggregate.get());
        }
    }

    @Override
    public boolean existsById(ID id) {
        Assert.notNull(id, "ID不能为空");

        try {
            boolean exists = doExistsById(id);
            log.debug("聚合根存在性检查: id={}, exists={}", id, exists);
            return exists;
        } catch (Exception e) {
            log.error("聚合根存在性检查失败: id={}, 错误: {}", id, e.getMessage(), e);
            throw new BusinessException("数据检查失败", e);
        }
    }

    /**
     * 发布领域事件
     */
    private void publishDomainEvents(AbstractAggregateRoot<?> aggregate) {
        List<IDomainEvent> events = aggregate.getDomainEvents();

        for (IDomainEvent event : events) {
            try {
                DomainEventPublisher.publish(event);
            } catch (Exception e) {
                log.error("领域事件发布失败: {}, 错误: {}", event.getEventType(), e.getMessage(), e);
            }
        }

        // 清除已发布的事件
        aggregate.clearDomainEvents();

        if (!events.isEmpty()) {
            log.debug("发布了 {} 个领域事件", events.size());
        }
    }

    /**
     * 获取聚合根标识（用于日志）
     */
    protected String getAggregateIdentifier(T aggregate) {
        if (aggregate instanceof AbstractAggregateRoot) {
            return ((AbstractAggregateRoot<?>) aggregate).getBusinessIdentifier();
        }
        return aggregate.toString();
    }

    // 抽象方法，由具体实现类提供
    protected abstract void doSave(T aggregate);

    protected abstract T doFindById(ID id);

    protected abstract void doDelete(T aggregate);

    protected abstract boolean doExistsById(ID id);
}
```

#### 具体仓储实现示例

```java

@Repository
@Transactional
public class OrderRepository extends AbstractBaseRepository<Order, OrderId> implements IOrderRepository {

    @Autowired
    private JpaOrderRepository jpaRepository;

    @Autowired
    private OrderToPOConverter orderToPOConverter;

    @Autowired
    private OrderPOToOrderConverter orderPOToOrderConverter;

    @Override
    protected void doSave(Order order) {
        OrderPO orderPO = orderToPOConverter.convert(order);
        jpaRepository.save(orderPO);
    }

    @Override
    protected Order doFindById(OrderId orderId) {
        Optional<OrderPO> orderPO = jpaRepository.findById(orderId.getValue());
        return orderPO.map(orderPOToOrderConverter::convert).orElse(null);
    }

    @Override
    protected void doDelete(Order order) {
        jpaRepository.deleteById(order.getId().getValue());
    }

    @Override
    protected boolean doExistsById(OrderId orderId) {
        return jpaRepository.existsById(orderId.getValue());
    }

    @Override
    public List<Order> findByCustomerId(CustomerId customerId) {
        List<OrderPO> orderPOs = jpaRepository.findByCustomerId(customerId.getValue());
        return orderPOs.stream()
                .map(orderPOToOrderConverter::convert)
                .collect(Collectors.toList());
    }

    @Override
    public List<Order> findByStatus(OrderStatus status) {
        List<OrderPO> orderPOs = jpaRepository.findByStatus(status.name());
        return orderPOs.stream()
                .map(orderPOToOrderConverter::convert)
                .collect(Collectors.toList());
    }

    @Override
    public List<Order> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        List<OrderPO> orderPOs = jpaRepository.findByCreatedAtBetween(startDate, endDate);
        return orderPOs.stream()
                .map(orderPOToOrderConverter::convert)
                .collect(Collectors.toList());
    }

    @Override
    public long countByCustomerId(CustomerId customerId) {
        return jpaRepository.countByCustomerId(customerId.getValue());
    }
}

// JPA Repository 接口
@Repository
public interface JpaOrderRepository extends JpaRepository<OrderPO, String> {

    List<OrderPO> findByCustomerId(String customerId);

    List<OrderPO> findByStatus(String status);

    List<OrderPO> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    long countByCustomerId(String customerId);
}
```

### 4. 基础设施层转换器

#### SpringConverterManager

Spring 集成的转换器管理器，利用 Spring 容器管理转换器。

```java

@Component
@Slf4j
public class SpringConverterManager implements ConverterRegistry.ConverterManager, InitializingBean {

    @Autowired
    private ApplicationContext applicationContext;

    @Override
    public void register(String key, Object converter) {
        // Spring 容器管理，不需要手动注册
        log.debug("转换器注册请求: {}, 由Spring容器自动管理", key);
    }

    @Override
    public <T> T getConverter(String key, Class<T> type) {
        try {
            return applicationContext.getBean(key, type);
        } catch (NoSuchBeanDefinitionException e) {
            log.debug("未找到转换器: key={}, type={}", key, type.getSimpleName());
            return null;
        }
    }

    @Override
    public <T> T getConverter(Class<T> converterClass) {
        try {
            return applicationContext.getBean(converterClass);
        } catch (NoSuchBeanDefinitionException e) {
            log.debug("未找到转换器: type={}", converterClass.getSimpleName());
            return null;
        }
    }

    @Override
    public void remove(String key) {
        // Spring 容器管理，不支持运行时移除
        log.debug("转换器移除请求: {}, Spring容器不支持运行时移除", key);
    }

    @Override
    public boolean contains(String key) {
        return applicationContext.containsBean(key);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // 注册到转换器注册中心
        ConverterRegistry.setConverterManager(this);
        log.info("Spring转换器管理器初始化完成");
    }
}
```

#### 持久化转换器

```java
// 持久化转换器接口
public interface IPersistenceConverter<S, T> {

    /**
     * 转换对象
     */
    T convert(S source);

    /**
     * 批量转换
     */
    default List<T> convertList(List<S> sources) {
        return sources.stream()
                .map(this::convert)
                .collect(Collectors.toList());
    }

    /**
     * 检查是否支持转换
     */
    boolean supports(Class<?> sourceType, Class<?> targetType);
}

// 抽象持久化转换器
public abstract class AbstractPersistenceConverter<S, T> implements IPersistenceConverter<S, T> {

    @Override
    public T convert(S source) {
        Assert.notNull(source, "源对象不能为空");
        return doConvert(source);
    }

    /**
     * 具体的转换逻辑
     */
    protected abstract T doConvert(S source);
}

// 具体转换器实现
@Component
public class OrderToPOConverter extends AbstractPersistenceConverter<Order, OrderPO> {

    @Autowired
    private OrderItemToPOConverter itemConverter;

    @Override
    protected OrderPO doConvert(Order order) {
        OrderPO orderPO = new OrderPO();
        orderPO.setId(order.getId().getValue());
        orderPO.setCustomerId(order.getCustomerId().getValue());
        orderPO.setStatus(order.getStatus().name());
        orderPO.setTotalAmount(order.getTotalAmount().getAmount());
        orderPO.setCurrency(order.getTotalAmount().getCurrency().name());
        orderPO.setShippingAddress(order.getShippingAddress());
        orderPO.setRemark(order.getRemark());
        orderPO.setCreatedAt(order.getCreatedAt());
        orderPO.setUpdatedAt(order.getUpdatedAt());
        orderPO.setVersion(order.getVersion());

        // 转换订单项
        List<OrderItemPO> itemPOs = order.getItems().stream()
                .map(itemConverter::convert)
                .collect(Collectors.toList());
        orderPO.setItems(itemPOs);

        return orderPO;
    }

    @Override
    public boolean supports(Class<?> sourceType, Class<?> targetType) {
        return Order.class.isAssignableFrom(sourceType)
                && OrderPO.class.isAssignableFrom(targetType);
    }
}

@Component
public class OrderPOToOrderConverter extends AbstractPersistenceConverter<OrderPO, Order> {

    @Autowired
    private OrderItemPOToOrderItemConverter itemConverter;

    @Override
    protected Order doConvert(OrderPO orderPO) {
        // 重建聚合根
        Order order = Order.rebuild(
                OrderId.of(orderPO.getId()),
                CustomerId.of(orderPO.getCustomerId()),
                OrderStatus.valueOf(orderPO.getStatus()),
                new Money(orderPO.getTotalAmount(), Currency.valueOf(orderPO.getCurrency())),
                orderPO.getShippingAddress(),
                orderPO.getRemark(),
                orderPO.getCreatedAt(),
                orderPO.getUpdatedAt(),
                orderPO.getVersion()
        );

        // 重建订单项
        List<OrderItem> items = orderPO.getItems().stream()
                .map(itemConverter::convert)
                .collect(Collectors.toList());
        order.rebuildItems(items);

        return order;
    }

    @Override
    public boolean supports(Class<?> sourceType, Class<?> targetType) {
        return OrderPO.class.isAssignableFrom(sourceType)
                && Order.class.isAssignableFrom(targetType);
    }
}
```

#### 事件转换器

```java
// 事件转换器接口
public interface IEventConverter<S, T> {

    /**
     * 转换事件
     */
    T convert(S source);

    /**
     * 检查是否支持转换
     */
    boolean supports(Class<?> sourceType, Class<?> targetType);
}

// 抽象事件转换器
public abstract class AbstractEventConverter<S, T> implements IEventConverter<S, T> {

    @Override
    public T convert(S source) {
        Assert.notNull(source, "源事件不能为空");
        return doConvert(source);
    }

    /**
     * 具体的转换逻辑
     */
    protected abstract T doConvert(S source);
}

// 具体事件转换器实现
@Component
public class OrderCreatedEventToMessageConverter extends AbstractEventConverter<OrderCreatedEvent, OrderCreatedMessage> {

    @Override
    protected OrderCreatedMessage doConvert(OrderCreatedEvent event) {
        return OrderCreatedMessage.builder()
                .orderId(event.getAggregateId())
                .customerId(event.getCustomerId())
                .totalAmount(event.getTotalAmount())
                .occurredOn(event.getOccurredOn())
                .eventId(UUID.randomUUID().toString())
                .eventVersion(event.getVersion())
                .build();
    }

    @Override
    public boolean supports(Class<?> sourceType, Class<?> targetType) {
        return OrderCreatedEvent.class.isAssignableFrom(sourceType)
                && OrderCreatedMessage.class.isAssignableFrom(targetType);
    }
}
```

### 5. Spring Boot 自动配置

#### spring.factories

```properties
# Auto Configure
org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
io.github.anthem37.ddd.infrastructure.config.DDDFrameworkAutoConfiguration,\
io.github.anthem37.ddd.infrastructure.config.AsyncExecutorConfig
```

#### 条件化配置

```java

@Configuration
@ConditionalOnClass({ICommandBus.class, IQueryBus.class})
@ConditionalOnProperty(prefix = "ddd.framework", name = "enabled", havingValue = "true", matchIfMissing = true)
public class ConditionalDDDConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "ddd.cqrs", name = "enabled", havingValue = "true", matchIfMissing = true)
    public ICommandBus commandBus() {
        return new CommandBus();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "ddd.events", name = "enabled", havingValue = "true", matchIfMissing = true)
    public DomainEventPublisher.EventPublisher eventPublisher() {
        return new SpringDomainEventPublisher();
    }
}
```

## 🎯 设计原则

### 1. 技术隔离

- **框架无关**：领域层不依赖任何技术框架
- **适配器模式**：通过适配器连接领域层和技术实现
- **可替换性**：技术实现可以轻松替换而不影响业务逻辑

### 2. 配置驱动

- **自动配置**：提供开箱即用的默认配置
- **条件化配置**：根据环境和需求动态配置
- **可覆盖性**：用户可以覆盖默认配置

### 3. 异步优化

- **事件异步**：领域事件异步处理，提高性能
- **线程池隔离**：不同类型操作使用独立线程池
- **背压处理**：合理的队列容量和拒绝策略

### 4. 监控友好

- **日志记录**：关键操作都有详细日志
- **指标暴露**：支持监控指标收集
- **错误处理**：完善的异常处理和恢复机制

## 📝 使用示例

### 完整的基础设施配置示例

```java
// 1. 自定义配置
@Configuration
public class CustomInfrastructureConfig {

    /**
     * 自定义命令执行器
     */
    @Bean("commandExecutor")
    @Primary
    public Executor customCommandExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(8);
        executor.setMaxPoolSize(16);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("CustomCommand-");
        executor.initialize();
        return executor;
    }

    /**
     * 自定义事件发布器
     */
    @Bean
    @Primary
    public DomainEventPublisher.EventPublisher customEventPublisher(
            ApplicationEventPublisher applicationEventPublisher,
            @Qualifier("eventExecutor") Executor eventExecutor) {
        return new CustomDomainEventPublisher(applicationEventPublisher, eventExecutor);
    }
}

// 2. 自定义事件处理器
@Component
public class CustomOrderEventHandler extends AbstractEventHandler<OrderCreatedEvent> {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private AuditService auditService;

    @Override
    protected void doHandle(OrderCreatedEvent event) {
        String orderId = event.getAggregateId();

        // 发送通知
        notificationService.notifyOrderCreated(orderId);

        // 记录审计日志
        auditService.recordOrderCreation(orderId, event.getCustomerId());

        // 更新统计信息
        updateOrderStatistics(event);
    }

    @Override
    protected Class<OrderCreatedEvent> getSupportedEventType() {
        return OrderCreatedEvent.class;
    }

    private void updateOrderStatistics(OrderCreatedEvent event) {
        // 更新订单统计逻辑
        log.info("更新订单统计: customerId={}, amount={}",
                event.getCustomerId(), event.getTotalAmount());
    }

    @Override
    protected void handleError(OrderCreatedEvent event, Exception e) {
        super.handleError(event, e);

        // 发送告警
        String message = String.format("订单事件处理失败: orderId=%s, error=%s",
                event.getAggregateId(), e.getMessage());
        // alertService.sendAlert("订单事件处理失败", message);
    }
}

// 3. 自定义仓储实现
@Repository
@Transactional
public class CustomOrderRepository extends AbstractBaseRepository<Order, OrderId> {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private JpaOrderRepository jpaRepository;

    @Override
    protected void doSave(Order order) {
        // 保存到数据库
        OrderPO orderPO = convertToPO(order);
        jpaRepository.save(orderPO);

        // 缓存到Redis
        String cacheKey = "order:" + order.getId().getValue();
        redisTemplate.opsForValue().set(cacheKey, order, Duration.ofHours(1));
    }

    @Override
    protected Order doFindById(OrderId orderId) {
        String cacheKey = "order:" + orderId.getValue();

        // 先从缓存查找
        Order cachedOrder = (Order) redisTemplate.opsForValue().get(cacheKey);
        if (cachedOrder != null) {
            return cachedOrder;
        }

        // 从数据库查找
        Optional<OrderPO> orderPO = jpaRepository.findById(orderId.getValue());
        if (orderPO.isPresent()) {
            Order order = convertFromPO(orderPO.get());
            // 更新缓存
            redisTemplate.opsForValue().set(cacheKey, order, Duration.ofHours(1));
            return order;
        }

        return null;
    }

    private OrderPO convertToPO(Order order) {
        // 转换逻辑
        return new OrderPO();
    }

    private Order convertFromPO(OrderPO orderPO) {
        // 转换逻辑
        return new Order();
    }
}
```

### 配置属性示例

```yaml
# application.yml
ddd:
  framework:
    enabled: true
  cqrs:
    enabled: true
    command:
      async: true
      timeout: 30s
    query:
      async: true
      timeout: 10s
  events:
    enabled: true
    async: true
    retry:
      enabled: true
      max-attempts: 3
      delay: 1s
  persistence:
    cache:
      enabled: true
      ttl: 3600s
    transaction:
      timeout: 30s

# 线程池配置
spring:
  task:
    execution:
      pool:
        core-size: 8
        max-size: 16
        queue-capacity: 100
        keep-alive: 60s
      thread-name-prefix: "ddd-task-"

# 日志配置
logging:
  level:
    io.github.anthem37.ddd: DEBUG
    org.springframework.transaction: DEBUG
```

### 监控和指标

```java
// 自定义指标收集
@Component
public class DDDMetricsCollector {

    private final MeterRegistry meterRegistry;
    private final Counter commandCounter;
    private final Counter queryCounter;
    private final Counter eventCounter;
    private final Timer commandTimer;
    private final Timer queryTimer;

    public DDDMetricsCollector(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.commandCounter = Counter.builder("ddd.command.total")
                .description("Total number of commands processed")
                .register(meterRegistry);
        this.queryCounter = Counter.builder("ddd.query.total")
                .description("Total number of queries processed")
                .register(meterRegistry);
        this.eventCounter = Counter.builder("ddd.event.total")
                .description("Total number of events processed")
                .register(meterRegistry);
        this.commandTimer = Timer.builder("ddd.command.duration")
                .description("Command processing duration")
                .register(meterRegistry);
        this.queryTimer = Timer.builder("ddd.query.duration")
                .description("Query processing duration")
                .register(meterRegistry);
    }

    public void recordCommand(String commandType, Duration duration) {
        commandCounter.increment(Tags.of("type", commandType));
        commandTimer.record(duration);
    }

    public void recordQuery(String queryType, Duration duration) {
        queryCounter.increment(Tags.of("type", queryType));
        queryTimer.record(duration);
    }

    public void recordEvent(String eventType) {
        eventCounter.increment(Tags.of("type", eventType));
    }
}
```

## 🔧 扩展点

### 1. 自定义事件发布器

```java

@Component
public class CustomEventPublisher implements DomainEventPublisher.EventPublisher {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public void publish(IDomainEvent event) {
        // 发布到Kafka
        kafkaTemplate.send("domain-events", event.getEventType(), event);

        // 同时发布到本地事件总线
        ApplicationContextHolder.getApplicationContext()
                .publishEvent(event);
    }
}
```

### 2. 自定义转换器管理器

```java

@Component
public class CustomConverterManager implements ConverterRegistry.ConverterManager {

    private final Map<String, Object> converters = new ConcurrentHashMap<>();

    @Override
    public void register(String key, Object converter) {
        converters.put(key, converter);
    }

    @Override
    public <T> T getConverter(String key, Class<T> type) {
        Object converter = converters.get(key);
        return type.isInstance(converter) ? type.cast(converter) : null;
    }

    // 其他方法实现...
}
```

### 3. 自定义仓储基类

```java
public abstract class CacheableRepository<T, ID> extends AbstractBaseRepository<T, ID> {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public Optional<T> findById(ID id) {
        // 先查缓存
        String cacheKey = getCacheKey(id);
        T cached = getCachedEntity(cacheKey);
        if (cached != null) {
            return Optional.of(cached);
        }

        // 查数据库
        Optional<T> entity = super.findById(id);
        entity.ifPresent(e -> cacheEntity(cacheKey, e));

        return entity;
    }

    @Override
    public void save(T aggregate) {
        super.save(aggregate);

        // 更新缓存
        String cacheKey = getCacheKey(getEntityId(aggregate));
        cacheEntity(cacheKey, aggregate);
    }

    protected abstract String getCacheKey(ID id);

    protected abstract ID getEntityId(T entity);

    private T getCachedEntity(String cacheKey) {
        return (T) redisTemplate.opsForValue().get(cacheKey);
    }

    private void cacheEntity(String cacheKey, T entity) {
        redisTemplate.opsForValue().set(cacheKey, entity, Duration.ofHours(1));
    }
}
```

## 🚀 最佳实践

### 1. 事件处理最佳实践

- **幂等性**：确保事件处理器是幂等的
- **错误处理**：实现完善的错误处理和重试机制
- **监控**：添加适当的监控和告警
- **性能**：避免在事件处理器中执行耗时操作

### 2. 仓储实现最佳实践

- **事务管理**：正确使用事务注解
- **缓存策略**：合理使用缓存提高性能
- **批量操作**：支持批量保存和查询
- **分页查询**：大数据量查询使用分页

### 3. 转换器最佳实践

- **单一职责**：每个转换器只负责一种转换
- **性能优化**：避免深度嵌套的对象转换
- **空值处理**：正确处理空值和默认值
- **类型安全**：使用泛型确保类型安全

### 4. 配置最佳实践

- **环境隔离**：不同环境使用不同配置
- **敏感信息**：使用配置中心管理敏感配置
- **动态配置**：支持运行时配置更新
- **配置验证**：启动时验证配置的正确性

## 📚 相关文档

- [DDD Common 模块](../ddd-common/README.md) - 通用组件和工具
- [DDD Domain 模块](../ddd-domain/README.md) - 领域层核心组件
- [DDD Application 模块](../ddd-application/README.md) - 应用层服务
- [DDD Interfaces 模块](../ddd-interfaces/README.md) - 接口层组件
- [框架总体介绍](../README.md) - 框架整体架构和使用指南

---

**注意**：基础设施层是整个DDD框架的技术基础，负责将领域模型与具体的技术实现连接起来。在使用时要注意保持领域层的纯净性，避免技术细节泄露到业务逻辑中。
