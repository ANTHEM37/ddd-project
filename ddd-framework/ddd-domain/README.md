# DDD Domain Module

DDD 框架的领域层模块，包含领域模型基础类、领域事件机制、领域服务、仓储接口和规约模式实现。

## 📦 模块结构

```
ddd-domain/
├── converter/          # 领域层转换器
├── event/             # 领域事件
├── model/             # 领域模型基础类
├── repository/        # 仓储接口
├── service/           # 领域服务
└── specification/     # 规约模式
```

## 🏗️ 核心组件

### 1. 领域模型基础类

#### AbstractAggregateRoot - 聚合根基类

聚合根是聚合的入口，负责维护业务不变性和管理领域事件。

```java
public class Order extends AbstractAggregateRoot<OrderId> {

    private OrderStatus status;
    private List<OrderItem> items;
    private Money totalAmount;

    protected Order(OrderId id) {
        super(id);
        this.status = OrderStatus.PENDING;
        this.items = new ArrayList<>();
        this.totalAmount = Money.ZERO;
    }

    // 业务方法
    public void addItem(Product product, int quantity) {
        // 检查业务规则
        checkRule(new OrderCanAddItemRule(this.status));
        checkRule(new ProductAvailableRule(product, quantity));

        OrderItem item = new OrderItem(product, quantity);
        this.items.add(item);
        this.totalAmount = this.totalAmount.add(item.getSubtotal());

        // 发布领域事件
        addDomainEvent(new OrderItemAddedEvent(getId(), item));

        // 统一后处理
        afterBusinessOperation();
    }

    public void confirm() {
        checkRule(new OrderCanBeConfirmedRule(this.status, this.items));

        this.status = OrderStatus.CONFIRMED;
        addDomainEvent(new OrderConfirmedEvent(getId(), this.totalAmount));
        afterBusinessOperation();
    }

    public void cancel(String reason) {
        checkRule(new OrderCanBeCancelledRule(this.status));

        this.status = OrderStatus.CANCELLED;
        addDomainEvent(new OrderCancelledEvent(getId(), reason));
        afterBusinessOperation();
    }

    // 必须实现的抽象方法
    @Override
    protected void addDeletedDomainEvent() {
        addDomainEvent(new OrderDeletedEvent(getId()));
    }

    // 重写不变性检查
    @Override
    protected void validateInvariants() {
        Assert.notEmpty(items, "订单必须包含至少一个商品");
        Assert.isTrue(totalAmount.isPositive(), "订单总金额必须大于0");
        Assert.notNull(status, "订单状态不能为空");
    }

    // 重写删除前检查
    @Override
    protected void checkCanBeRemoved() {
        checkRule(new OrderCanBeDeletedRule(this.status));
    }
}
```

**聚合根提供的功能：**

- **业务规则检查**：`checkRule()` 和 `checkRules()` 方法
- **领域事件管理**：自动管理事件的添加、获取和清除
- **版本控制**：支持乐观锁的版本管理
- **删除管理**：安全删除机制，包含删除前检查
- **不变性验证**：`validateInvariants()` 确保聚合状态一致性
- **统一后处理**：`afterBusinessOperation()` 统一处理不变性检查和版本递增

#### AbstractEntity - 实体基类

实体具有唯一标识，可变状态，有生命周期。

```java
public class OrderItem extends AbstractEntity<OrderItemId> {

    private Product product;
    private int quantity;
    private Money unitPrice;

    public OrderItem(OrderItemId id, Product product, int quantity) {
        super(id);
        Assert.notNull(product, "商品不能为空");
        Assert.isTrue(quantity > 0, "数量必须大于0");

        this.product = product;
        this.quantity = quantity;
        this.unitPrice = product.getPrice();
    }

    public Money getSubtotal() {
        return unitPrice.multiply(quantity);
    }

    public void changeQuantity(int newQuantity) {
        Assert.isTrue(newQuantity > 0, "数量必须大于0");
        this.quantity = newQuantity;
    }
}
```

**实体特性：**

- **唯一标识**：基于 ID 的相等性比较
- **可变状态**：可以修改属性值
- **生命周期**：从创建到删除的完整生命周期

#### AbstractValueObject - 值对象基类

值对象是不可变的领域概念，基于属性值进行相等性比较。

```java
public class Money extends AbstractValueObject {

    public static final Money ZERO = new Money(BigDecimal.ZERO, Currency.CNY);

    private final BigDecimal amount;
    private final Currency currency;

    public Money(BigDecimal amount, Currency currency) {
        Assert.notNull(amount, "金额不能为空");
        Assert.notNull(currency, "货币不能为空");
        Assert.isTrue(amount.compareTo(BigDecimal.ZERO) >= 0, "金额不能为负数");

        this.amount = amount;
        this.currency = currency;
        validate();
    }

    public Money add(Money other) {
        Assert.equals(this.currency, other.currency, "货币类型必须相同");
        return new Money(this.amount.add(other.amount), this.currency);
    }

    public Money multiply(int multiplier) {
        return new Money(this.amount.multiply(BigDecimal.valueOf(multiplier)), this.currency);
    }

    public boolean isPositive() {
        return amount.compareTo(BigDecimal.ZERO) > 0;
    }

    @Override
    protected Object[] getEqualityComponents() {
        return new Object[]{amount, currency};
    }

    @Override
    public AbstractValueObject copy() {
        return new Money(amount, currency);
    }

    @Override
    protected void validate() {
        Assert.isTrue(amount.scale() <= 2, "金额小数位不能超过2位");
    }
}
```

**值对象特性：**

- **不可变性**：创建后不能修改，所有操作返回新实例
- **值相等性**：基于属性值进行相等性比较
- **自验证**：在构造时进行业务规则验证
- **无标识**：没有唯一标识，完全基于属性值

### 2. 领域事件机制

#### DomainEventPublisher - 领域事件发布器

纯领域层实现，不依赖外部框架，通过接口与基础设施层解耦。

```java
// 定义领域事件
public class OrderCreatedEvent implements IDomainEvent {

    private final OrderId orderId;
    private final Money totalAmount;
    private final LocalDateTime occurredOn;

    public OrderCreatedEvent(OrderId orderId, Money totalAmount) {
        this.orderId = orderId;
        this.totalAmount = totalAmount;
        this.occurredOn = LocalDateTime.now();
    }

    @Override
    public String getEventType() {
        return "OrderCreated";
    }

    @Override
    public LocalDateTime getOccurredOn() {
        return occurredOn;
    }

    @Override
    public String getAggregateId() {
        return orderId.getValue();
    }
}

// 在聚合根中发布事件
public class Order extends AbstractAggregateRoot<OrderId> {

    public static Order create(CustomerId customerId, List<OrderItem> items) {
        Order order = new Order(OrderId.generate());
        order.customerId = customerId;
        order.items = new ArrayList<>(items);
        order.calculateTotal();

        // 发布领域事件
        order.addDomainEvent(new OrderCreatedEvent(order.getId(), order.totalAmount));

        return order;
    }
}

// 基础设施层设置事件发布器
@PostConstruct
public void initializeEventPublisher() {
    DomainEventPublisher.setEventPublisher(springDomainEventPublisher);
}
```

#### IDomainEvent - 领域事件接口

```java
public interface IDomainEvent {

    /**
     * 获取事件类型
     */
    String getEventType();

    /**
     * 获取事件发生时间
     */
    LocalDateTime getOccurredOn();

    /**
     * 获取聚合根ID
     */
    String getAggregateId();

    /**
     * 获取事件版本（用于事件演化）
     */
    default int getVersion() {
        return 1;
    }
}
```

### 3. 领域服务

#### AbstractDomainService - 领域服务基类

领域服务封装不属于任何特定聚合的业务逻辑。

```java

@Component
public class OrderPricingService extends AbstractDomainService {

    @Autowired
    private IProductRepository productRepository;

    @Autowired
    private IPromotionRepository promotionRepository;

    /**
     * 计算订单价格
     */
    public Money calculateOrderPrice(List<OrderItem> items, CustomerId customerId) {
        Assert.notEmpty(items, "订单项不能为空");
        Assert.notNull(customerId, "客户ID不能为空");

        Money subtotal = calculateSubtotal(items);
        Money discount = calculateDiscount(items, customerId);
        Money tax = calculateTax(subtotal.subtract(discount));

        return subtotal.subtract(discount).add(tax);
    }

    private Money calculateSubtotal(List<OrderItem> items) {
        return items.stream()
                .map(OrderItem::getSubtotal)
                .reduce(Money.ZERO, Money::add);
    }

    private Money calculateDiscount(List<OrderItem> items, CustomerId customerId) {
        List<Promotion> applicablePromotions = promotionRepository.findApplicablePromotions(customerId);

        return applicablePromotions.stream()
                .map(promotion -> promotion.calculateDiscount(items))
                .reduce(Money.ZERO, Money::add);
    }

    private Money calculateTax(Money amount) {
        // 税率计算逻辑
        BigDecimal taxRate = new BigDecimal("0.13");
        BigDecimal taxAmount = amount.getAmount().multiply(taxRate);
        return new Money(taxAmount, amount.getCurrency());
    }
}
```

### 4. 仓储接口

#### IRepository - 仓储基础接口

```java
public interface IRepository<T, ID> {
    
    /**
     * 保存聚合根
     */
    void save(T aggregate);
    
    /**
     * 根据ID查找聚合根
     */
    Optional<T> findById(ID id);
    
    /**
     * 删除聚合根
     */
    void delete(T aggregate);
    
    /**
     * 根据ID删除聚合根
     */
    void deleteById(ID id);
    
    /**
     * 检查聚合根是否存在
     */
    boolean existsById(ID id);
}

// 具体仓储接口
public interface IOrderRepository extends IRepository<Order, OrderId> {
    
    /**
     * 根据客户ID查找订单
     */
    List<Order> findByCustomerId(CustomerId customerId);
    
    /**
     * 根据状态查找订单
     */
    List<Order> findByStatus(OrderStatus status);
    
    /**
     * 查找指定时间范围内的订单
     */
    List<Order> findByDateRange(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * 统计客户的订单数量
     */
    long countByCustomerId(CustomerId customerId);
}
```

### 5. 规约模式

#### ISpecification - 规约接口

规约模式用于封装复杂的查询条件和业务规则。

```java
public interface ISpecification<T> {
    
    /**
     * 检查对象是否满足规约
     */
    boolean isSatisfiedBy(T candidate);
    
    /**
     * 与操作
     */
    default ISpecification<T> and(ISpecification<T> other) {
        return new AndSpecification<>(this, other);
    }
    
    /**
     * 或操作
     */
    default ISpecification<T> or(ISpecification<T> other) {
        return new OrSpecification<>(this, other);
    }
    
    /**
     * 非操作
     */
    default ISpecification<T> not() {
        return new NotSpecification<>(this);
    }
}

// 具体规约实现
public class HighValueOrderSpecification implements ISpecification<Order> {
    
    private final Money threshold;
    
    public HighValueOrderSpecification(Money threshold) {
        this.threshold = threshold;
    }
    
    @Override
    public boolean isSatisfiedBy(Order order) {
        return order.getTotalAmount().compareTo(threshold) >= 0;
    }
}

public class VipCustomerSpecification implements ISpecification<Order> {
    
    @Override
    public boolean isSatisfiedBy(Order order) {
        return order.getCustomer().isVip();
    }
}

// 组合使用规约
public class OrderService {
    
    public List<Order> findHighValueVipOrders(List<Order> orders) {
        ISpecification<Order> spec = new HighValueOrderSpecification(new Money(new BigDecimal("1000"), Currency.CNY))
            .and(new VipCustomerSpecification());
            
        return orders.stream()
            .filter(spec::isSatisfiedBy)
            .collect(Collectors.toList());
    }
}
```

### 6. 领域转换器

#### IDomainConverter - 领域转换器接口

```java
public interface IDomainConverter<S, T> {

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

// 具体转换器实现
@Component
public class OrderToOrderSummaryConverter extends AbstractDomainConverter<Order, OrderSummary> {

    @Override
    public OrderSummary convert(Order order) {
        Assert.notNull(order, "订单不能为空");

        return OrderSummary.builder()
                .orderId(order.getId().getValue())
                .customerId(order.getCustomerId().getValue())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .itemCount(order.getItems().size())
                .createdAt(order.getCreatedAt())
                .build();
    }

    @Override
    public boolean supports(Class<?> sourceType, Class<?> targetType) {
        return Order.class.isAssignableFrom(sourceType)
                && OrderSummary.class.isAssignableFrom(targetType);
    }
}
```

## 🎯 设计原则

### 1. 领域纯净性

- **无外部依赖**：领域层不依赖任何外部框架或技术
- **业务语言**：使用业务专家的语言定义模型
- **自包含**：领域逻辑完全封装在领域对象内部

### 2. 聚合设计

- **一致性边界**：聚合确保内部数据的一致性
- **事务边界**：一个事务只修改一个聚合实例
- **引用方式**：聚合间通过ID引用，不直接持有对象引用

### 3. 事件驱动

- **松耦合**：通过事件实现聚合间的解耦
- **最终一致性**：通过事件实现跨聚合的最终一致性
- **审计跟踪**：事件提供完整的业务操作历史

### 4. 不变性保护

- **业务规则**：通过业务规则保护聚合不变性
- **封装性**：隐藏内部状态，只暴露必要的行为
- **验证机制**：在状态变更时自动验证不变性

## 📝 使用示例

### 完整的订单聚合示例

```java
// 订单聚合根
public class Order extends AbstractAggregateRoot<OrderId> {

    private CustomerId customerId;
    private OrderStatus status;
    private List<OrderItem> items;
    private Money totalAmount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 构造函数
    protected Order(OrderId id) {
        super(id);
        this.status = OrderStatus.PENDING;
        this.items = new ArrayList<>();
        this.totalAmount = Money.ZERO;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // 工厂方法
    public static Order create(CustomerId customerId, List<OrderItem> items) {
        Assert.notNull(customerId, "客户ID不能为空");
        Assert.notEmpty(items, "订单项不能为空");

        Order order = new Order(OrderId.generate());
        order.customerId = customerId;
        order.items = new ArrayList<>(items);
        order.calculateTotal();

        // 发布创建事件
        order.addDomainEvent(new OrderCreatedEvent(order.getId(), order.totalAmount));
        order.afterBusinessOperation();

        return order;
    }

    // 业务方法
    public void addItem(Product product, int quantity) {
        checkRule(new OrderCanAddItemRule(this.status));
        checkRule(new ProductAvailableRule(product, quantity));

        OrderItem item = new OrderItem(OrderItemId.generate(), product, quantity);
        this.items.add(item);
        calculateTotal();

        addDomainEvent(new OrderItemAddedEvent(getId(), item));
        afterBusinessOperation();
    }

    public void removeItem(OrderItemId itemId) {
        checkRule(new OrderCanRemoveItemRule(this.status));

        OrderItem item = findItem(itemId);
        Assert.notNull(item, "订单项不存在");

        this.items.remove(item);
        calculateTotal();

        addDomainEvent(new OrderItemRemovedEvent(getId(), itemId));
        afterBusinessOperation();
    }

    public void confirm() {
        checkRule(new OrderCanBeConfirmedRule(this.status, this.items));

        this.status = OrderStatus.CONFIRMED;
        this.updatedAt = LocalDateTime.now();

        addDomainEvent(new OrderConfirmedEvent(getId(), this.totalAmount));
        afterBusinessOperation();
    }

    public void cancel(String reason) {
        checkRule(new OrderCanBeCancelledRule(this.status));

        this.status = OrderStatus.CANCELLED;
        this.updatedAt = LocalDateTime.now();

        addDomainEvent(new OrderCancelledEvent(getId(), reason));
        afterBusinessOperation();
    }

    public void ship(ShippingAddress address) {
        checkRule(new OrderCanBeShippedRule(this.status));
        Assert.notNull(address, "收货地址不能为空");

        this.status = OrderStatus.SHIPPED;
        this.updatedAt = LocalDateTime.now();

        addDomainEvent(new OrderShippedEvent(getId(), address));
        afterBusinessOperation();
    }

    // 私有辅助方法
    private void calculateTotal() {
        this.totalAmount = items.stream()
                .map(OrderItem::getSubtotal)
                .reduce(Money.ZERO, Money::add);
    }

    private OrderItem findItem(OrderItemId itemId) {
        return items.stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst()
                .orElse(null);
    }

    // 重写基类方法
    @Override
    protected void addDeletedDomainEvent() {
        addDomainEvent(new OrderDeletedEvent(getId()));
    }

    @Override
    protected void validateInvariants() {
        Assert.notNull(customerId, "客户ID不能为空");
        Assert.notNull(status, "订单状态不能为空");
        Assert.notEmpty(items, "订单必须包含至少一个商品");
        Assert.isTrue(totalAmount.isPositive(), "订单总金额必须大于0");
        Assert.notNull(createdAt, "创建时间不能为空");
        Assert.notNull(updatedAt, "更新时间不能为空");
    }

    @Override
    protected void checkCanBeRemoved() {
        checkRule(new OrderCanBeDeletedRule(this.status));
    }

    @Override
    public String getBusinessIdentifier() {
        return String.format("Order[%s] - Customer[%s] - Amount[%s]",
                getId().getValue(), customerId.getValue(), totalAmount);
    }
}
```

### 业务规则示例

```java
// 订单可以添加商品规则
public class OrderCanAddItemRule implements IBusinessRule {

    private final OrderStatus status;

    public OrderCanAddItemRule(OrderStatus status) {
        this.status = status;
    }

    @Override
    public boolean isSatisfied() {
        return status == OrderStatus.PENDING;
    }

    @Override
    public String getMessage() {
        return String.format("订单状态为[%s]时不能添加商品", status);
    }

    @Override
    public String getRuleName() {
        return "订单添加商品规则";
    }
}

// 复合业务规则
public class OrderCreationRule implements IBusinessRule {

    private final Customer customer;
    private final List<OrderItem> items;
    private final List<IBusinessRule> rules;

    public OrderCreationRule(Customer customer, List<OrderItem> items) {
        this.customer = customer;
        this.items = items;
        this.rules = Arrays.asList(
                new CustomerCanPlaceOrderRule(customer),
                new OrderItemsValidRule(items),
                new InventoryAvailableRule(items)
        );
    }

    @Override
    public boolean isSatisfied() {
        return rules.stream().allMatch(IBusinessRule::isSatisfied);
    }

    @Override
    public String getMessage() {
        return rules.stream()
                .filter(rule -> !rule.isSatisfied())
                .map(IBusinessRule::getMessage)
                .collect(Collectors.joining("; "));
    }
}
```

### 领域服务协作示例

```java

@Component
public class OrderDomainService extends AbstractDomainService {

    @Autowired
    private ICustomerRepository customerRepository;

    @Autowired
    private IProductRepository productRepository;

    @Autowired
    private OrderPricingService pricingService;

    /**
     * 创建订单（涉及多个聚合的复杂业务逻辑）
     */
    public Order createOrder(CustomerId customerId, List<CreateOrderItemRequest> itemRequests) {
        // 获取客户信息
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new BusinessException("客户不存在"));

        // 构建订单项
        List<OrderItem> items = itemRequests.stream()
                .map(this::buildOrderItem)
                .collect(Collectors.toList());

        // 检查创建规则
        OrderCreationRule creationRule = new OrderCreationRule(customer, items);
        Assert.isTrue(creationRule.isSatisfied(), creationRule.getMessage());

        // 创建订单
        Order order = Order.create(customerId, items);

        // 计算价格（使用定价服务）
        Money calculatedPrice = pricingService.calculateOrderPrice(items, customerId);
        // 注意：这里应该在Order类中提供相应的方法来更新总金额
        // order.recalculateTotal(calculatedPrice);

        return order;
    }

    private OrderItem buildOrderItem(CreateOrderItemRequest request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new BusinessException("商品不存在"));

        return new OrderItem(OrderItemId.generate(), product, request.getQuantity());
    }
}
```

## 🔗 依赖关系

### Maven 依赖

```xml
<dependencies>
    <dependency>
        <groupId>io.github.anthem37</groupId>
        <artifactId>ddd-common</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework</groupId>
        <artifactId>spring-context</artifactId>
    </dependency>
</dependencies>
```

### 模块依赖

- **依赖**：ddd-common（断言、异常、业务规则）
- **被依赖**：ddd-application、ddd-infrastructure
- **不依赖**：任何外部技术框架

## 🧪 测试

领域层测试专注于业务逻辑验证：

```java

@ExtendWith(MockitoExtension.class)
class OrderTest {

    @Test
    void should_create_order_successfully() {
        // Given
        CustomerId customerId = CustomerId.of("customer-001");
        List<OrderItem> items = Arrays.asList(
                new OrderItem(OrderItemId.generate(), product1, 2),
                new OrderItem(OrderItemId.generate(), product2, 1)
        );

        // When
        Order order = Order.create(customerId, items);

        // Then
        assertThat(order.getCustomerId()).isEqualTo(customerId);
        assertThat(order.getItems()).hasSize(2);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(order.getDomainEvents()).hasSize(1);
        assertThat(order.getDomainEvents().get(0)).isInstanceOf(OrderCreatedEvent.class);
    }

    @Test
    void should_throw_exception_when_add_item_to_confirmed_order() {
        // Given
        Order order = createConfirmedOrder();

        // When & Then
        assertThatThrownBy(() -> order.addItem(product, 1))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("订单状态为[CONFIRMED]时不能添加商品");
    }
}
```

## 📚 最佳实践

1. **聚合设计**：保持聚合小而聚焦，一个聚合解决一个业务问题
2. **事件使用**：通过领域事件实现聚合间的解耦和集成
3. **业务规则**：将复杂的业务规则封装为独立的规则对象
4. **不变性保护**：在每次状态变更后验证聚合不变性
5. **领域语言**：使用业务专家的语言命名类和方法
6. **纯净性维护**：领域层不依赖任何技术框架
7. **测试驱动**：通过单元测试验证业务逻辑的正确性
