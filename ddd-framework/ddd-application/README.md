# DDD Application Module

DDD 框架的应用层模块，负责业务流程编排、命令查询处理、应用服务协调和跨聚合的业务逻辑实现。

## 📦 模块结构

```
ddd-application/
└── service/          # 应用服务接口
```

## 🏗️ 核心组件

### 1. 应用服务接口

```java
package io.github.anthem37.ddd.application.service;

import io.github.anthem37.ddd.common.cqrs.command.ICommand;
import io.github.anthem37.ddd.common.cqrs.command.ICommandBus;
import io.github.anthem37.ddd.common.cqrs.query.IQuery;
import io.github.anthem37.ddd.common.cqrs.query.IQueryBus;

/**
 * 应用服务标记接口
 * 应用服务负责：
 * 1. 业务用例编排
 * 2. 事务管理
 * 3. 权限控制
 * 4. DTO转换
 *
 * @author anthem37
 * @date 2025/8/13 16:45:32
 */
public interface IApplicationService {

    /**
     * 获取命令总线
     */
    ICommandBus getCommandBus();

    /**
     * 获取查询总线
     */
    IQueryBus getQueryBus();

    /**
     * 发送命令
     */
    default <R> R sendCommand(ICommand<R> command) {
        return getCommandBus().send(command);
    }

    /**
     * 发送查询
     */
    default <T extends IQuery<R>, R> R sendQuery(T query) {
        return getQueryBus().send(query);
    }

}
```

### 2. CQRS 支持

应用层使用位于 `ddd-common` 模块中的 CQRS 相关接口，包括：

- `ICommand`: 命令标记接口，用于改变系统状态
- `ICommandBus`: 命令总线接口，负责命令的路由和执行
- `IQuery`: 查询标记接口，用于获取数据，不改变系统状态
- `IQueryBus`: 查询总线接口，负责查询的路由和执行

这些接口的具体实现由框架提供，应用层通过 `IApplicationService` 接口中的方法来使用这些功能。

## 使用示例

```java

@Service
public class UserApplicationService implements IApplicationService {

    private final ICommandBus commandBus;
    private final IQueryBus queryBus;

    @Autowired
    public UserApplicationService(ICommandBus commandBus, IQueryBus queryBus) {
        this.commandBus = commandBus;
        this.queryBus = queryBus;
    }

    @Override
    public ICommandBus getCommandBus() {
        return commandBus;
    }

    @Override
    public IQueryBus getQueryBus() {
        return queryBus;
    }

    public UserDTO createUser(CreateUserRequest request) {
        // 转换请求为命令
        CreateUserCommand command = new CreateUserCommand(
                request.getUsername(),
                request.getEmail(),
                request.getPassword()
        );

        // 发送命令
        UserId userId = sendCommand(command);

        // 查询创建的用户
        UserQuery query = new UserQuery(userId);
        return sendQuery(query);
    }
### 3.

    命令处理器(CommandHandler)

    命令处理器负责执行命令并返回结果。

            ```java
    @Component

    public class CreateOrderCommandHandler implements ICommandHandler<CreateOrderCommand, OrderId> {

        private final CustomerRepository customerRepository;
        private final OrderRepository orderRepository;
        private final OrderDomainService orderDomainService;

        @Autowired
        public CreateOrderCommandHandler(CustomerRepository customerRepository,
                                         OrderRepository orderRepository,
                                         OrderDomainService orderDomainService) {
            this.customerRepository = customerRepository;
            this.orderRepository = orderRepository;
            this.orderDomainService = orderDomainService;
        }

        @Override
        @Transactional
        public OrderId handle(CreateOrderCommand command) {
            // 验证命令
            Assert.isTrue(command.isValid(), "无效的创建订单命令");

            // 获取客户信息
            CustomerId customerId = CustomerId.of(command.getCustomerId());
            Customer customer = customerRepository.findById(customerId)
                    .orElseThrow(() -> new BusinessException("客户不存在"));

            // 使用领域服务创建订单
            Order order = orderDomainService.createOrder(customerId, command.getItems());

            // 保存订单
            orderRepository.save(order);

            return order.getId();
        }

        @Override
        public Class<CreateOrderCommand> getSupportedCommandType() {
            return CreateOrderCommand.class;
        }

    }

```

#### 命令总线 (CommandBus)

```java
// 命令总线接口
public interface ICommandBus {

    /**
     * 同步发送命令
     */
    <R> R send(ICommand<R> command);

    /**
     * 异步发送命令
     */
    <R> CompletableFuture<R> sendAsync(ICommand<R> command);
}

// 使用示例
@Service
public class OrderApplicationService {

    @Autowired
    private ICommandBus commandBus;

    public OrderId createOrder(CreateOrderRequest request) {
        CreateOrderCommand command = new CreateOrderCommand(
                request.getCustomerId(),
                request.getItems(),
                request.getShippingAddress()
        );

        return commandBus.send(command);
    }

    public CompletableFuture<OrderId> createOrderAsync(CreateOrderRequest request) {
        CreateOrderCommand command = new CreateOrderCommand(
                request.getCustomerId(),
                request.getItems(),
                request.getShippingAddress()
        );

        return commandBus.sendAsync(command);
    }
}
```

#### 查询处理 (Query)

查询代表系统的读操作，不会改变系统状态。

```java
// 查询接口
public interface IQuery<R> {

    /**
     * 验证查询是否有效
     */
    boolean isValid();

    /**
     * 获取查询的业务标识
     */
    default String getBusinessIdentifier() {
        return this.getClass().getSimpleName();
    }
}

// 具体查询实现
public class GetOrderQuery implements IQuery<OrderDTO> {

    private final String orderId;

    public GetOrderQuery(String orderId) {
        this.orderId = orderId;
    }

    @Override
    public boolean isValid() {
        return StringUtils.hasText(orderId);
    }

    @Override
    public String getBusinessIdentifier() {
        return String.format("GetOrder[%s]", orderId);
    }

    public String getOrderId() {
        return orderId;
    }
}

// 查询处理器
@Component
public class GetOrderQueryHandler implements IQueryHandler<GetOrderQuery, OrderDTO> {

    @Autowired
    private IOrderRepository orderRepository;

    @Autowired
    private OrderToDTOConverter converter;

    @Override
    public OrderDTO handle(GetOrderQuery query) {
        Assert.isTrue(query.isValid(), "无效的获取订单查询");

        OrderId orderId = OrderId.of(query.getOrderId());
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("订单不存在"));

        return converter.convert(order);
    }

    @Override
    public Class<GetOrderQuery> getSupportedQueryType() {
        return GetOrderQuery.class;
    }
}
```

### 2. 业务编排框架 (Orchestration)

业务编排框架支持复杂业务流程的可视化编排，包含四种节点类型和灵活的连接机制。

#### 基本使用

```java

@Service
public class OrderProcessOrchestration {

    @Autowired
    private ICommandBus commandBus;

    @Autowired
    private IQueryBus queryBus;

    public void processOrder(String customerId, List<OrderItemRequest> items) {
        // 创建编排实例
        Orchestration orchestration = new Orchestration(
                "order-process-001",
                "订单处理流程",
                commandBus,
                queryBus
        );

        // 构建流程
        orchestration
                // 1. 验证客户
                .addQuery("validate-customer", "验证客户",
                        ctx -> new ValidateCustomerQuery(customerId))

                // 2. 检查库存
                .addQuery("check-inventory", "检查库存",
                        ctx -> new CheckInventoryQuery(items))

                // 3. 库存充足判断
                .addCondition("inventory-sufficient", "库存充足",
                        ctx -> {
                            InventoryCheckResult result = ctx.getResult("check-inventory", InventoryCheckResult.class);
                            return result.isSufficient();
                        })

                // 4. 创建订单
                .addCommand("create-order", "创建订单",
                        ctx -> new CreateOrderCommand(customerId, items))

                // 5. 发送确认邮件
                .addCommand("send-email", "发送确认邮件",
                        ctx -> {
                            OrderId orderId = ctx.getResult("create-order", OrderId.class);
                            return new SendOrderConfirmationEmailCommand(orderId);
                        })

                // 6. 库存不足处理
                .addCommand("notify-shortage", "通知库存不足",
                        ctx -> new NotifyInventoryShortageCommand(customerId, items))

                // 连接节点
                .connect("validate-customer", "check-inventory")
                .connect("check-inventory", "inventory-sufficient")
                .connectWhenTrue("inventory-sufficient", "create-order")
                .connectWhenFalse("inventory-sufficient", "notify-shortage")
                .connect("create-order", "send-email");

        // 执行编排
        Orchestration.Context context = new Orchestration.Context("order-process-001");
        context.setVariable("customerId", customerId);
        context.setVariable("items", items);

        Orchestration.Result result = orchestration.execute(context);

        if (result.isSuccess()) {
            log.info("订单处理成功，耗时: {}ms", result.getExecutionTimeMillis());
        } else {
            log.error("订单处理失败: {}", result.getErrorMessage());
            throw new BusinessException("订单处理失败: " + result.getErrorMessage());
        }
    }
}
```

#### 复杂编排示例

```java

@Service
public class UserRegistrationOrchestration {

    public void registerUser(UserRegistrationRequest request) {
        Orchestration orchestration = new Orchestration(
                "user-registration",
                "用户注册流程",
                commandBus,
                queryBus
        );

        orchestration
                // 邮箱验证
                .addQuery("check-email", "检查邮箱",
                        ctx -> new CheckEmailExistsQuery(request.getEmail()))
                .addCondition("email-available", "邮箱可用", "check-email", false)

                // 用户名验证
                .addQuery("check-username", "检查用户名",
                        ctx -> new CheckUsernameExistsQuery(request.getUsername()))
                .addCondition("username-available", "用户名可用", "check-username", false)

                // 创建用户
                .addCommand("create-user", "创建用户",
                        ctx -> new CreateUserCommand(request))

                // 发送验证邮件
                .addCommand("send-verification", "发送验证邮件",
                        ctx -> {
                            UserId userId = ctx.getResult("create-user", UserId.class);
                            return new SendVerificationEmailCommand(userId, request.getEmail());
                        })

                // 创建用户配置
                .addCommand("create-profile", "创建用户配置",
                        ctx -> {
                            UserId userId = ctx.getResult("create-user", UserId.class);
                            return new CreateUserProfileCommand(userId);
                        })

                // 记录注册事件
                .addGeneric("log-registration", "记录注册",
                        ctx -> {
                            UserId userId = ctx.getResult("create-user", UserId.class);
                            log.info("用户注册成功: {}", userId);
                            return "logged";
                        })

                // 连接流程
                .connect("check-email", "email-available")
                .connectWhenTrue("email-available", "check-username")
                .connect("check-username", "username-available")
                .connectWhenTrue("username-available", "create-user")
                .connect("create-user", "send-verification")
                .connect("create-user", "create-profile")
                .connect("create-user", "log-registration");

        // 执行并导出流程图
        Orchestration.Result result = orchestration.execute();
        String plantUML = orchestration.toPlantUML();

        // 保存流程图到文件或数据库
        saveProcessDiagram("user-registration", plantUML);
    }
}
```

#### PlantUML 导出

编排框架支持自动生成 PlantUML 流程图：

```java
public void exportProcessDiagram() {
    Orchestration orchestration = buildOrderProcessOrchestration();

    String plantUML = orchestration.toPlantUML();
    System.out.println(plantUML);

    // 输出示例:
    // @startuml
    // !theme plain
    // title 订单处理流程
    // 
    // state "验证客户" as validate-customer <<query>>
    // state "检查库存" as check-inventory <<query>>
    // state "库存充足" as inventory-sufficient <<choice>>
    // state "创建订单" as create-order <<command>>
    // state "发送确认邮件" as send-email <<command>>
    // state "通知库存不足" as notify-shortage <<command>>
    // 
    // validate-customer --> check-inventory
    // check-inventory --> inventory-sufficient
    // inventory-sufficient --> create-order : true
    // inventory-sufficient --> notify-shortage : false
    // create-order --> send-email
    // 
    // @enduml
}
```

### 3. 应用层转换器

#### 命令转换器 (CommandConverter)

```java
// 命令转换器接口
public interface ICommandConverter<S, T extends ICommand<?>> {

    /**
     * 转换为命令
     */
    T toCommand(S source);

    /**
     * 批量转换
     */
    default List<T> toCommands(List<S> sources) {
        return sources.stream()
                .map(this::toCommand)
                .collect(Collectors.toList());
    }
}

// 具体转换器实现
@Component
public class CreateOrderRequestToCommandConverter
        extends AbstractCommandConverter<CreateOrderRequest, CreateOrderCommand> {

    @Override
    public CreateOrderCommand toCommand(CreateOrderRequest request) {
        Assert.notNull(request, "请求不能为空");

        return new CreateOrderCommand(
                request.getCustomerId(),
                convertOrderItems(request.getItems()),
                request.getShippingAddress()
        );
    }

    private List<CreateOrderItemRequest> convertOrderItems(List<OrderItemRequest> items) {
        return items.stream()
                .map(item -> new CreateOrderItemRequest(
                        item.getProductId(),
                        item.getQuantity(),
                        item.getUnitPrice()
                ))
                .collect(Collectors.toList());
    }

    @Override
    public boolean supports(Class<?> sourceType, Class<?> targetType) {
        return CreateOrderRequest.class.isAssignableFrom(sourceType)
                && CreateOrderCommand.class.isAssignableFrom(targetType);
    }
}
```

#### 查询转换器 (QueryConverter)

```java
// 查询转换器接口
public interface IQueryConverter<S, T extends IQuery<?>> {

    /**
     * 转换为查询
     */
    T toQuery(S source);

    /**
     * 批量转换
     */
    default List<T> toQueries(List<S> sources) {
        return sources.stream()
                .map(this::toQuery)
                .collect(Collectors.toList());
    }
}

// 具体转换器实现
@Component
public class OrderQueryRequestToQueryConverter
        extends AbstractQueryConverter<OrderQueryRequest, GetOrderQuery> {

    @Override
    public GetOrderQuery toQuery(OrderQueryRequest request) {
        Assert.notNull(request, "查询请求不能为空");
        Assert.hasText(request.getOrderId(), "订单ID不能为空");

        return new GetOrderQuery(request.getOrderId());
    }

    @Override
    public boolean supports(Class<?> sourceType, Class<?> targetType) {
        return OrderQueryRequest.class.isAssignableFrom(sourceType)
                && GetOrderQuery.class.isAssignableFrom(targetType);
    }
}
```

### 4. 应用服务

#### 应用服务接口

```java
public interface IApplicationService {

    /**
     * 获取服务名称
     */
    default String getServiceName() {
        return this.getClass().getSimpleName();
    }
}
```

#### 具体应用服务实现

```java

@Service
@Transactional
public class OrderApplicationService implements IApplicationService {

    @Autowired
    private ICommandBus commandBus;

    @Autowired
    private IQueryBus queryBus;

    @Autowired
    private CreateOrderRequestToCommandConverter commandConverter;

    @Autowired
    private OrderQueryRequestToQueryConverter queryConverter;

    /**
     * 创建订单
     */
    public OrderDTO createOrder(CreateOrderRequest request) {
        // 转换为命令
        CreateOrderCommand command = commandConverter.toCommand(request);

        // 发送命令
        OrderId orderId = commandBus.send(command);

        // 查询创建的订单
        GetOrderQuery query = new GetOrderQuery(orderId.getValue());
        return queryBus.send(query);
    }

    /**
     * 批量创建订单
     */
    public List<OrderDTO> createOrders(List<CreateOrderRequest> requests) {
        // 批量转换为命令
        List<CreateOrderCommand> commands = commandConverter.toCommands(requests);

        // 批量执行命令
        List<OrderId> orderIds = commands.stream()
                .map(commandBus::send)
                .collect(Collectors.toList());

        // 批量查询结果
        return orderIds.stream()
                .map(orderId -> new GetOrderQuery(orderId.getValue()))
                .map(queryBus::send)
                .collect(Collectors.toList());
    }

    /**
     * 获取订单
     */
    public OrderDTO getOrder(OrderQueryRequest request) {
        GetOrderQuery query = queryConverter.toQuery(request);
        return queryBus.send(query);
    }

    /**
     * 取消订单
     */
    public void cancelOrder(String orderId, String reason) {
        CancelOrderCommand command = new CancelOrderCommand(orderId, reason);
        commandBus.send(command);
    }

    /**
     * 异步处理订单
     */
    public CompletableFuture<OrderDTO> processOrderAsync(CreateOrderRequest request) {
        CreateOrderCommand command = commandConverter.toCommand(request);

        return commandBus.sendAsync(command)
                .thenCompose(orderId -> {
                    GetOrderQuery query = new GetOrderQuery(orderId.getValue());
                    return CompletableFuture.completedFuture(queryBus.send(query));
                });
    }
}
```

### 5. 消息总线基础类

#### AbstractMessageBus

```java
public abstract class AbstractMessageBus<M, H> {

    @Autowired
    protected ApplicationContext applicationContext;

    protected final Map<Class<?>, H> handlerCache = new ConcurrentHashMap<>();

    /**
     * 发送消息
     */
    protected <R> R send(M message) {
        Assert.notNull(message, getMessageTypeName() + "不能为空");
        Assert.isTrue(isValid(message), "无效的" + getMessageTypeName());

        H handler = findHandler(message);
        Assert.notNull(handler, "未找到" + getMessageTypeName() + "处理器: " + message.getClass().getSimpleName());

        try {
            return handleMessage(handler, message);
        } catch (Exception e) {
            log.error("{}处理失败: {}", getMessageTypeName(), message.getClass().getSimpleName(), e);
            throw new BusinessException(getMessageTypeName() + "处理失败", e);
        }
    }

    /**
     * 异步发送消息
     */
    protected <R> CompletableFuture<R> sendAsync(M message) {
        return CompletableFuture.supplyAsync(() -> send(message), getExecutor());
    }

    // 抽象方法，由子类实现
    protected abstract String getMessageTypeName();

    protected abstract Executor getExecutor();

    protected abstract boolean isValid(M message);

    protected abstract <R> R handleMessage(H handler, M message);

    protected abstract H findHandler(M message);
}
```

## 🎯 设计原则

### 1. CQRS 分离

- **命令职责**：处理写操作，改变系统状态
- **查询职责**：处理读操作，不改变系统状态
- **独立优化**：命令和查询可以独立优化和扩展

### 2. 业务编排

- **可视化**：通过 PlantUML 实现流程可视化
- **灵活性**：支持条件分支和复杂流程控制
- **可测试**：每个节点都可以独立测试

### 3. 异步支持

- **性能优化**：支持异步命令处理
- **响应性**：提高系统响应性能
- **可扩展**：支持分布式处理

### 4. 类型安全

- **编译时检查**：通过泛型确保类型安全
- **运行时验证**：通过断言确保运行时安全
- **接口约束**：通过接口定义明确契约

## 📝 使用示例

### 完整的电商订单处理示例

```java
// 1. 定义命令和查询
public class CreateOrderCommand implements ICommand<OrderId> {
    private final String customerId;
    private final List<OrderItemRequest> items;
    private final String shippingAddress;

    // 构造函数和验证逻辑...
}

public class GetOrderQuery implements IQuery<OrderDTO> {
    private final String orderId;

    // 构造函数和验证逻辑...
}

// 2. 实现处理器
@Component
public class CreateOrderCommandHandler implements ICommandHandler<CreateOrderCommand, OrderId> {

    @Override
    @Transactional
    public OrderId handle(CreateOrderCommand command) {
        // 业务逻辑处理
        Order order = Order.create(/* 参数 */);
        orderRepository.save(order);
        return order.getId();
    }
}

@Component
public class GetOrderQueryHandler implements IQueryHandler<GetOrderQuery, OrderDTO> {

    @Override
    public OrderDTO handle(GetOrderQuery query) {
        // 查询逻辑处理
        Order order = orderRepository.findById(OrderId.of(query.getOrderId()))
                .orElseThrow(() -> new BusinessException("订单不存在"));
        return orderConverter.convert(order);
    }
}

// 3. 应用服务协调
@Service
public class OrderApplicationService {

    @Autowired
    private ICommandBus commandBus;

    @Autowired
    private IQueryBus queryBus;

    public OrderDTO processOrder(CreateOrderRequest request) {
        // 创建订单
        CreateOrderCommand command = new CreateOrderCommand(
                request.getCustomerId(),
                request.getItems(),
                request.getShippingAddress()
        );
        OrderId orderId = commandBus.send(command);

        // 查询订单详情
        GetOrderQuery query = new GetOrderQuery(orderId.getValue());
        return queryBus.send(query);
    }
}

// 4. 业务编排
@Service
public class ComplexOrderProcessOrchestration {

    public void processComplexOrder(CreateOrderRequest request) {
        Orchestration orchestration = new Orchestration(
                "complex-order-process",
                "复杂订单处理流程",
                commandBus,
                queryBus
        );

        orchestration
                .addQuery("validate-customer", "验证客户",
                        ctx -> new ValidateCustomerQuery(request.getCustomerId()))
                .addQuery("check-credit", "检查信用",
                        ctx -> new CheckCustomerCreditQuery(request.getCustomerId()))
                .addCondition("credit-sufficient", "信用充足",
                        ctx -> {
                            CreditCheckResult result = ctx.getResult("check-credit", CreditCheckResult.class);
                            return result.isApproved();
                        })
                .addQuery("check-inventory", "检查库存",
                        ctx -> new CheckInventoryQuery(request.getItems()))
                .addCondition("inventory-available", "库存可用",
                        ctx -> {
                            InventoryResult result = ctx.getResult("check-inventory", InventoryResult.class);
                            return result.isAvailable();
                        })
                .addCommand("reserve-inventory", "预留库存",
                        ctx -> new ReserveInventoryCommand(request.getItems()))
                .addCommand("create-order", "创建订单",
                        ctx -> new CreateOrderCommand(request))
                .addCommand("process-payment", "处理支付",
                        ctx -> {
                            OrderId orderId = ctx.getResult("create-order", OrderId.class);
                            return new ProcessPaymentCommand(orderId, request.getPaymentInfo());
                        })
                .addCondition("payment-success", "支付成功",
                        ctx -> {
                            PaymentResult result = ctx.getResult("process-payment", PaymentResult.class);
                            return result.isSuccess();
                        })
                .addCommand("confirm-order", "确认订单",
                        ctx -> {
                            OrderId orderId = ctx.getResult("create-order", OrderId.class);
                            return new ConfirmOrderCommand(orderId);
                        })
                .addCommand("release-inventory", "释放库存",
                        ctx -> new ReleaseInventoryCommand(request.getItems()))
                .addCommand("cancel-order", "取消订单",
                        ctx -> {
                            OrderId orderId = ctx.getResult("create-order", OrderId.class);
                            return new CancelOrderCommand(orderId, "支付失败");
                        })

                // 连接流程
                .connect("validate-customer", "check-credit")
                .connect("check-credit", "credit-sufficient")
                .connectWhenTrue("credit-sufficient", "check-inventory")
                .connect("check-inventory", "inventory-available")
                .connectWhenTrue("inventory-available", "reserve-inventory")
                .connect("reserve-inventory", "create-order")
                .connect("create-order", "process-payment")
                .connect("process-payment", "payment-success")
                .connectWhenTrue("payment-success", "confirm-order")
                .connectWhenFalse("payment-success", "release-inventory")
                .connect("release-inventory", "cancel-order");

        // 执行编排
        Orchestration.Context context = new Orchestration.Context("complex-order-001");
        context.setVariable("request", request);

        Orchestration.Result result = orchestration.execute(context);

        if (!result.isSuccess()) {
            throw new BusinessException("订单处理失败: " + result.getErrorMessage());
        }

        // 导出流程图用于文档
        String plantUML = orchestration.toPlantUML();
        saveProcessDiagram("complex-order-process", plantUML);
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
        <groupId>io.github.anthem37</groupId>
        <artifactId>ddd-domain</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework</groupId>
        <artifactId>spring-context</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework</groupId>
        <artifactId>spring-tx</artifactId>
    </dependency>
</dependencies>
```

### 模块依赖

- **依赖**：ddd-common、ddd-domain
- **被依赖**：ddd-interfaces、ddd-infrastructure
- **协调**：领域层和基础设施层

## 🧪 测试

应用层测试专注于业务流程和集成测试：

```java

@ExtendWith(SpringExtension.class)
@SpringBootTest
class OrderApplicationServiceTest {

    @Autowired
    private OrderApplicationService orderApplicationService;

    @MockBean
    private IOrderRepository orderRepository;

    @Test
    void should_create_order_successfully() {
        // Given
        CreateOrderRequest request = CreateOrderRequest.builder()
                .customerId("customer-001")
                .items(Arrays.asList(
                        OrderItemRequest.builder()
                                .productId("product-001")
                                .quantity(2)
                                .build()
                ))
                .shippingAddress("北京市朝阳区")
                .build();

        // When
        OrderDTO result = orderApplicationService.createOrder(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getCustomerId()).isEqualTo("customer-001");
        assertThat(result.getItems()).hasSize(1);
    }
}

@ExtendWith(MockitoExtension.class)
class OrchestrationTest {

    @Mock
    private ICommandBus commandBus;

    @Mock
    private IQueryBus queryBus;

    @Test
    void should_execute_orchestration_successfully() {
        // Given
        when(queryBus.send(any(ValidateCustomerQuery.class)))
                .thenReturn(ValidationResult.success());
        when(commandBus.send(any(CreateOrderCommand.class)))
                .thenReturn(OrderId.of("order-001"));

        Orchestration orchestration = new Orchestration(
                "test-process", "测试流程", commandBus, queryBus);

        orchestration
                .addQuery("validate", "验证", ctx -> new ValidateCustomerQuery("customer-001"))
                .addCommand("create", "创建", ctx -> new CreateOrderCommand("customer-001"))
                .connect("validate", "create");

        // When
        Orchestration.Result result = orchestration.execute();

        // Then
        assertThat(result.isSuccess()).isTrue();
        verify(queryBus).send(any(ValidateCustomerQuery.class));
        verify(commandBus).send(any(CreateOrderCommand.class));
    }
}
```

## 📚 最佳实践

1. **命令设计**：命令应该包含执行操作所需的所有信息
2. **查询优化**：查询应该针对特定的读取场景进行优化
3. **处理器职责**：每个处理器只处理一种类型的命令或查询
4. **异常处理**：在处理器中进行适当的异常处理和转换
5. **事务管理**：命令处理器应该包含适当的事务边界
6. **编排设计**：复杂业务流程应该通过编排框架进行可视化管理
7. **转换器使用**：使用专门的转换器进行对象转换，保持代码清晰

## 🔧 配置

### 自动配置

框架提供自动配置，无需手动配置：

```java

@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

### 自定义配置

如需自定义配置，可以覆盖默认配置：

```java

@Configuration
public class CustomDDDConfiguration {

    @Bean
    @Primary
    public ICommandBus customCommandBus(@Qualifier("commandExecutor") Executor executor) {
        return new CustomCommandBus(executor);
    }

    @Bean
    @Primary
    public IQueryBus customQueryBus(@Qualifier("queryExecutor") Executor executor) {
        return new CustomQueryBus(executor);
    }
}
```

### 执行器配置

可以自定义命令和查询的执行器：

```java

@Configuration
@EnableAsync
public class ExecutorConfiguration {

    @Bean("commandExecutor")
    public Executor commandExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("Command-");
        executor.initialize();
        return executor;
    }

    @Bean("queryExecutor")
    public Executor queryExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("Query-");
        executor.initialize();
        return executor;
    }
}
```

## 🚀 性能优化

### 异步处理

```java

@Service
public class OrderApplicationService {

    @Autowired
    private ICommandBus commandBus;

    // 异步处理大批量订单
    public CompletableFuture<List<OrderId>> createOrdersBatch(List<CreateOrderRequest> requests) {
        List<CompletableFuture<OrderId>> futures = requests.stream()
                .map(request -> {
                    CreateOrderCommand command = new CreateOrderCommand(request);
                    return commandBus.sendAsync(command);
                })
                .collect(Collectors.toList());

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> futures.stream()
                        .map(CompletableFuture::join)
                        .collect(Collectors.toList()));
    }
}
```

### 缓存优化

```java

@Component
public class GetOrderQueryHandler implements IQueryHandler<GetOrderQuery, OrderDTO> {

    @Autowired
    private IOrderRepository orderRepository;

    @Cacheable(value = "orders", key = "#query.orderId")
    @Override
    public OrderDTO handle(GetOrderQuery query) {
        // 查询逻辑
    }
}
```

### 批量处理

```java

@Component
public class BatchOrderQueryHandler implements IQueryHandler<BatchOrderQuery, List<OrderDTO>> {

    @Override
    public List<OrderDTO> handle(BatchOrderQuery query) {
        // 批量查询优化
        List<OrderId> orderIds = query.getOrderIds();
        Map<OrderId, Order> orderMap = orderRepository.findByIds(orderIds);

        return orderIds.stream()
                .map(orderMap::get)
                .filter(Objects::nonNull)
                .map(orderConverter::convert)
                .collect(Collectors.toList());
    }
}
```

## 🔍 监控和日志

### 处理器监控

```java

@Component
@Slf4j
public class MonitoringCommandHandler implements ICommandHandler<CreateOrderCommand, OrderId> {

    @Autowired
    private MeterRegistry meterRegistry;

    @Override
    public OrderId handle(CreateOrderCommand command) {
        Timer.Sample sample = Timer.start(meterRegistry);

        try {
            OrderId result = doHandle(command);

            // 记录成功指标
            meterRegistry.counter("command.success", "type", "CreateOrder").increment();

            return result;
        } catch (Exception e) {
            // 记录失败指标
            meterRegistry.counter("command.failure", "type", "CreateOrder").increment();
            throw e;
        } finally {
            sample.stop(Timer.builder("command.duration")
                    .tag("type", "CreateOrder")
                    .register(meterRegistry));
        }
    }

    private OrderId doHandle(CreateOrderCommand command) {
        // 实际处理逻辑
    }
}
```

### 编排监控

```java

@Service
public class MonitoredOrchestrationService {

    public void executeWithMonitoring(Orchestration orchestration) {
        long startTime = System.currentTimeMillis();

        try {
            Orchestration.Result result = orchestration.execute();

            long duration = System.currentTimeMillis() - startTime;
            log.info("编排执行完成: {}, 耗时: {}ms, 成功: {}",
                    orchestration.getName(), duration, result.isSuccess());

            if (!result.isSuccess()) {
                log.error("编排执行失败: {}, 错误: {}",
                        orchestration.getName(), result.getErrorMessage());
            }

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("编排执行异常: {}, 耗时: {}ms",
                    orchestration.getName(), duration, e);
            throw e;
        }
    }
}
```

## 🔧 故障排查

### 常见问题

1. **处理器未找到**
    - 检查处理器是否标注了 `@Component`
    - 确认 `getSupportedCommandType()` 返回正确的类型
    - 验证包扫描路径是否包含处理器类

2. **编排执行失败**
    - 检查节点连接是否正确
    - 验证条件判断逻辑
    - 确认所有必需的节点都已定义

3. **异步处理超时**
    - 调整线程池配置
    - 检查处理器执行时间
    - 考虑增加超时配置

### 调试技巧

```java

@Component
@Slf4j
public class DebuggingCommandHandler implements ICommandHandler<CreateOrderCommand, OrderId> {

    @Override
    public OrderId handle(CreateOrderCommand command) {
        log.debug("开始处理命令: {}", command.getBusinessIdentifier());

        try {
            // 处理逻辑
            OrderId result = processOrder(command);

            log.debug("命令处理成功: {}, 结果: {}",
                    command.getBusinessIdentifier(), result);

            return result;
        } catch (Exception e) {
            log.error("命令处理失败: {}, 错误: {}",
                    command.getBusinessIdentifier(), e.getMessage(), e);
            throw e;
        }
    }
}
```

## 📋 总结

DDD Application 模块提供了完整的应用层解决方案，包括：

- **CQRS 支持**：清晰分离读写操作
- **业务编排**：可视化的复杂流程管理
- **类型安全**：编译时和运行时的类型检查
- **异步处理**：高性能的异步执行能力
- **Spring 集成**：无缝集成 Spring 生态系统

通过这些功能，开发者可以构建出结构清晰、易于维护、高性能的应用层服务。
