# DDD Interfaces Module

DDD 框架的接口层模块，负责处理用户请求、数据传输对象（DTO）管理、门面模式实现和统一的异常处理。

## 📦 模块结构

```
ddd-interfaces/
├── assembler/         # DTO 组装器
├── dto/              # 数据传输对象
└── facade/           # 门面层实现
```

## 🏗️ 核心组件

### 1. DTO 体系

#### 基础请求对象 (AbstractBaseRequest)

所有请求 DTO 的基类，提供通用的验证和标识功能。

```java
public abstract class AbstractBaseRequest {

    /**
     * 请求ID，用于链路追踪
     */
    private String requestId;

    /**
     * 请求时间戳
     */
    private Long timestamp;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 验证请求是否有效
     */
    public abstract boolean isValid();

    /**
     * 获取业务标识
     */
    public String getBusinessIdentifier() {
        return this.getClass().getSimpleName() + ":" + requestId;
    }

    // 构造函数和 getter/setter
    protected AbstractBaseRequest() {
        this.requestId = UUID.randomUUID().toString();
        this.timestamp = System.currentTimeMillis();
    }

    // getters and setters
    public String getRequestId() {
        return requestId;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
```

#### 响应对象

提供统一的响应格式：

```java
// 基础响应对象
public class BaseResponse {
    private boolean success;
    private String message;
    private String code;
    private Long timestamp;

    // 构造函数、getters and setters
}

// 数据响应对象
public class DataResponse<T> extends BaseResponse {
    private T data;

    // 构造函数、getters and setters
}

// 错误响应对象
public class ErrorResponse extends BaseResponse {
    private String details;
    private String requestId;

    // 构造函数、getters and setters
}

// 分页结果响应对象
public class PagedResult<T> extends BaseResponse {
    private List<T> items;
    private int pageIndex;
    private int pageSize;
    private long totalCount;
    private int totalPages;

    // 构造函数、getters and setters
}
```

#### 具体请求对象示例

```java

@Data
@EqualsAndHashCode(callSuper = true)
public class CreateUserRequest extends AbstractBaseRequest {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, message = "密码长度不能少于6位")
    private String password;

    @Override
    public boolean isValid() {
        return StringUtils.hasText(username)
                && StringUtils.hasText(email)
                && StringUtils.hasText(password);
    }
}
```

### 2. DTO 组装器

负责在领域模型和 DTO 之间进行转换：

```java
// 组装器接口
public interface IDTOAssembler<D, M> {
    D toDTO(M model);

    M toModel(D dto);

    List<D> toDTOList(Collection<M> models);

    List<M> toModelList(Collection<D> dtos);
}

// 抽象组装器基类
public abstract class AbstractDTOAssembler<D, M> implements IDTOAssembler<D, M> {

    @Override
    public List<D> toDTOList(Collection<M> models) {
        if (CollectionUtils.isEmpty(models)) {
            return Collections.emptyList();
        }
        return models.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<M> toModelList(Collection<D> dtos) {
        if (CollectionUtils.isEmpty(dtos)) {
            return Collections.emptyList();
        }
        return dtos.stream()
                .map(this::toModel)
                .collect(Collectors.toList());
    }
}
```

### 3. 门面层

提供统一的接口访问入口和异常处理：

```java
// 抽象门面基类
public abstract class AbstractBaseFacade {

    /**
     * 执行命令并返回结果
     */
    protected <R> R executeCommand(ICommand<R> command) {
        // 命令执行逻辑
    }

    /**
     * 执行查询并返回结果
     */
    protected <T extends IQuery<R>, R> R executeQuery(T query) {
        // 查询执行逻辑
    }
}

// REST API 异常处理器
@RestControllerAdvice
public class RestApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(RestApiExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex) {
        ErrorResponse response = new ErrorResponse();
        response.setSuccess(false);
        response.setMessage(ex.getMessage());
        response.setCode(ex.getCode());
        response.setDetails(ex.getDetails());
        response.setRequestId(ex.getRequestId());
        response.setTimestamp(System.currentTimeMillis());

        log.error("Business exception: {}", ex.getMessage(), ex);

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        ErrorResponse response = new ErrorResponse();
        response.setSuccess(false);
        response.setMessage("系统错误，请联系管理员");
        response.setCode("SYSTEM_ERROR");
        response.setDetails(ex.getMessage());
        response.setTimestamp(System.currentTimeMillis());

        log.error("System exception: {}", ex.getMessage(), ex);

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
```

## 使用示例

### 1. 创建控制器

```java

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserFacade userFacade;

    @Autowired
    public UserController(UserFacade userFacade) {
        this.userFacade = userFacade;
    }

    @PostMapping
    public ResponseEntity<DataResponse<UserDTO>> createUser(@Valid @RequestBody CreateUserRequest request) {
        UserDTO user = userFacade.createUser(request);
        return ResponseEntity.ok(new DataResponse<>(user));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DataResponse<UserDTO>> getUserById(@PathVariable String id) {
        UserDTO user = userFacade.getUserById(id);
        return ResponseEntity.ok(new DataResponse<>(user));
    }

    @GetMapping
    public ResponseEntity<DataResponse<PagedResult<UserDTO>>> getUsers(
            @RequestParam(defaultValue = "1") int pageIndex,
            @RequestParam(defaultValue = "10") int pageSize) {
        PagedResult<UserDTO> users = userFacade.getUsers(pageIndex, pageSize);
        return ResponseEntity.ok(new DataResponse<>(users));
    }
}
```

### 2. 创建门面实现

```java

@Service
public class UserFacadeImpl extends AbstractBaseFacade implements UserFacade {

    private final IApplicationService applicationService;
    private final UserDTOAssembler userDTOAssembler;

    @Autowired
    public UserFacadeImpl(IApplicationService applicationService, UserDTOAssembler userDTOAssembler) {
        this.applicationService = applicationService;
        this.userDTOAssembler = userDTOAssembler;
    }

    @Override
    public UserDTO createUser(CreateUserRequest request) {
        CreateUserCommand command = new CreateUserCommand(
                request.getUsername(),
                request.getEmail(),
                request.getPassword()
        );

        UserId userId = applicationService.sendCommand(command);
        UserQuery query = new UserQuery(userId);
        User user = applicationService.sendQuery(query);

        return userDTOAssembler.toDTO(user);
    }

    @Override
    public UserDTO getUserById(String id) {
        UserQuery query = new UserQuery(new UserId(id));
        User user = applicationService.sendQuery(query);
        return userDTOAssembler.toDTO(user);
    }

    @Override
    public PagedResult<UserDTO> getUsers(int pageIndex, int pageSize) {
        UserPageQuery query = new UserPageQuery(pageIndex, pageSize);
        PagedResult<User> users = applicationService.sendQuery(query);
        return new PagedResult<>(
                userDTOAssembler.toDTOList(users.getItems()),
                users.getPageIndex(),
                users.getPageSize(),
                users.getTotalCount(),
                users.getTotalPages()
        );
    }
}
```

    public boolean isValid() {
        return StringUtils.hasText(productId)
                && quantity != null && quantity > 0
                && unitPrice != null && unitPrice.compareTo(BigDecimal.ZERO) > 0;
    }

}

```

#### 响应对象体系

```java
// 基础响应
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BaseResponse {

    /**
     * 响应码
     */
    private String code;

    /**
     * 响应消息
     */
    private String message;

    /**
     * 响应时间戳
     */
    private Long timestamp;

    /**
     * 请求ID（用于链路追踪）
     */
    private String requestId;

    /**
     * 是否成功
     */
    public boolean isSuccess() {
        return "200".equals(code);
    }

    public static BaseResponse success() {
        return new BaseResponse("200", "操作成功", System.currentTimeMillis(), null);
    }

    public static BaseResponse error(String message) {
        return new BaseResponse("500", message, System.currentTimeMillis(), null);
    }
}

// 数据响应
@Data
@EqualsAndHashCode(callSuper = true)
public class DataResponse<T> extends BaseResponse {

    /**
     * 响应数据
     */
    private T data;

    public DataResponse() {
        super();
    }

    public DataResponse(String code, String message, T data) {
        super(code, message, System.currentTimeMillis(), null);
        this.data = data;
    }

    public static <T> DataResponse<T> success(T data) {
        return new DataResponse<>("200", "操作成功", data);
    }

    public static <T> DataResponse<T> error(String message) {
        return new DataResponse<>("500", message, null);
    }

    public static <T> DataResponse<T> error(String code, String message) {
        return new DataResponse<>(code, message, null);
    }
}

// 错误响应
@Data
@EqualsAndHashCode(callSuper = true)
public class ErrorResponse extends BaseResponse {

    /**
     * 错误详情
     */
    private String detail;

    /**
     * 错误堆栈（开发环境）
     */
    private String stackTrace;

    /**
     * 字段验证错误
     */
    private Map<String, String> fieldErrors;

    public ErrorResponse(String code, String message, String detail) {
        super(code, message, System.currentTimeMillis(), null);
        this.detail = detail;
    }

    public static ErrorResponse businessError(String message) {
        return new ErrorResponse("BIZ_ERROR", message, null);
    }

    public static ErrorResponse validationError(String message, Map<String, String> fieldErrors) {
        ErrorResponse response = new ErrorResponse("VALIDATION_ERROR", message, null);
        response.setFieldErrors(fieldErrors);
        return response;
    }

    public static ErrorResponse systemError(String message, String detail) {
        return new ErrorResponse("SYSTEM_ERROR", message, detail);
    }
}

// 分页响应
@Data
@EqualsAndHashCode(callSuper = true)
public class PagedResult<T> extends DataResponse<List<T>> {

    /**
     * 当前页码
     */
    private Integer pageNumber;

    /**
     * 页面大小
     */
    private Integer pageSize;

    /**
     * 总记录数
     */
    private Long totalElements;

    /**
     * 总页数
     */
    private Integer totalPages;

    /**
     * 是否有下一页
     */
    private Boolean hasNext;

    /**
     * 是否有上一页
     */
    private Boolean hasPrevious;

    public PagedResult(List<T> data, Integer pageNumber, Integer pageSize, Long totalElements) {
        super("200", "查询成功", data);
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
        this.totalElements = totalElements;
        this.totalPages = (int) Math.ceil((double) totalElements / pageSize);
        this.hasNext = pageNumber < totalPages - 1;
        this.hasPrevious = pageNumber > 0;
    }

    public static <T> PagedResult<T> of(List<T> data, Integer pageNumber, Integer pageSize, Long totalElements) {
        return new PagedResult<>(data, pageNumber, pageSize, totalElements);
    }
}
```

### 2. DTO 组装器 (Assembler)

#### 组装器接口

```java
public interface IDTOAssembler<S, T> {

    /**
     * 转换单个对象
     */
    T assemble(S source);

    /**
     * 批量转换
     */
    default List<T> assembleList(List<S> sources) {
        if (CollectionUtils.isEmpty(sources)) {
            return Collections.emptyList();
        }
        return sources.stream()
                .map(this::assemble)
                .collect(Collectors.toList());
    }

    /**
     * 检查是否支持转换
     */
    boolean supports(Class<?> sourceType, Class<?> targetType);
}
```

#### 抽象组装器基类

```java
public abstract class AbstractDTOAssembler<S, T> implements IDTOAssembler<S, T> {

    @Override
    public T assemble(S source) {
        Assert.notNull(source, "源对象不能为空");
        return doAssemble(source);
    }

    /**
     * 具体的转换逻辑，由子类实现
     */
    protected abstract T doAssemble(S source);

    /**
     * 转换分页结果
     */
    public PagedResult<T> assemblePage(List<S> sources, Integer pageNumber, Integer pageSize, Long totalElements) {
        List<T> data = assembleList(sources);
        return PagedResult.of(data, pageNumber, pageSize, totalElements);
    }
}
```

#### 具体组装器实现

```java

@Component
public class OrderToOrderDTOAssembler extends AbstractDTOAssembler<Order, OrderDTO> {

    @Autowired
    private OrderItemToOrderItemDTOAssembler itemAssembler;

    @Override
    protected OrderDTO doAssemble(Order order) {
        return OrderDTO.builder()
                .orderId(order.getId().getValue())
                .customerId(order.getCustomerId().getValue())
                .status(order.getStatus().name())
                .totalAmount(order.getTotalAmount().getAmount())
                .currency(order.getTotalAmount().getCurrency().name())
                .items(itemAssembler.assembleList(order.getItems()))
                .shippingAddress(order.getShippingAddress())
                .remark(order.getRemark())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .version(order.getVersion())
                .build();
    }

    @Override
    public boolean supports(Class<?> sourceType, Class<?> targetType) {
        return Order.class.isAssignableFrom(sourceType)
                && OrderDTO.class.isAssignableFrom(targetType);
    }
}

@Component
public class CreateOrderRequestToCommandAssembler extends AbstractDTOAssembler<CreateOrderRequest, CreateOrderCommand> {

    @Override
    protected CreateOrderCommand doAssemble(CreateOrderRequest request) {
        List<CreateOrderItemCommand> itemCommands = request.getItems().stream()
                .map(item -> new CreateOrderItemCommand(
                        item.getProductId(),
                        item.getQuantity(),
                        item.getUnitPrice()
                ))
                .collect(Collectors.toList());

        return new CreateOrderCommand(
                request.getCustomerId(),
                itemCommands,
                request.getShippingAddress(),
                request.getRemark()
        );
    }

    @Override
    public boolean supports(Class<?> sourceType, Class<?> targetType) {
        return CreateOrderRequest.class.isAssignableFrom(sourceType)
                && CreateOrderCommand.class.isAssignableFrom(targetType);
    }
}
```

### 3. 门面层 (Facade)

#### 门面基类

```java
public abstract class AbstractBaseFacade {

    @Autowired
    protected ICommandBus commandBus;

    @Autowired
    protected IQueryBus queryBus;

    /**
     * 发送命令
     */
    protected <R> R sendCommand(ICommand<R> command) {
        return commandBus.send(command);
    }

    /**
     * 发送查询
     */
    protected <T extends IQuery<R>, R> R sendQuery(T query) {
        return queryBus.send(query);
    }

    /**
     * 异步发送命令
     */
    protected <R> CompletableFuture<R> sendCommandAsync(ICommand<R> command) {
        return commandBus.sendAsync(command);
    }

    /**
     * 构建成功响应
     */
    protected <T> DataResponse<T> success(T data) {
        return DataResponse.success(data);
    }

    /**
     * 构建成功响应（无数据）
     */
    protected BaseResponse success() {
        return BaseResponse.success();
    }

    /**
     * 构建错误响应
     */
    protected <T> DataResponse<T> error(String message) {
        return DataResponse.error(message);
    }

    /**
     * 构建分页响应
     */
    protected <T> PagedResult<T> pagedResult(List<T> data, Integer pageNumber, Integer pageSize, Long totalElements) {
        return PagedResult.of(data, pageNumber, pageSize, totalElements);
    }
}
```

#### 具体门面实现

```java

@RestController
@RequestMapping("/api/orders")
@Validated
public class OrderFacade extends AbstractBaseFacade {

    @Autowired
    private CreateOrderRequestToCommandAssembler commandAssembler;

    @Autowired
    private OrderToOrderDTOAssembler orderAssembler;

    /**
     * 创建订单
     */
    @PostMapping
    public DataResponse<OrderDTO> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        // 转换为命令
        CreateOrderCommand command = commandAssembler.assemble(request);

        // 发送命令
        OrderId orderId = sendCommand(command);

        // 查询创建的订单
        GetOrderQuery query = new GetOrderQuery(orderId.getValue());
        Order order = sendQuery(query);

        // 转换为DTO
        OrderDTO orderDTO = orderAssembler.assemble(order);

        return success(orderDTO);
    }

    /**
     * 获取订单详情
     */
    @GetMapping("/{orderId}")
    public DataResponse<OrderDTO> getOrder(@PathVariable String orderId) {
        GetOrderQuery query = new GetOrderQuery(orderId);
        Order order = sendQuery(query);

        OrderDTO orderDTO = orderAssembler.assemble(order);
        return success(orderDTO);
    }

    /**
     * 分页查询订单
     */
    @GetMapping
    public PagedResult<OrderDTO> getOrders(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String customerId,
            @RequestParam(required = false) String status) {

        PageOrderQuery query = new PageOrderQuery(page, size, customerId, status);
        PageResult<Order> pageResult = sendQuery(query);

        List<OrderDTO> orderDTOs = orderAssembler.assembleList(pageResult.getContent());

        return pagedResult(orderDTOs, page, size, pageResult.getTotalElements());
    }

    /**
     * 取消订单
     */
    @PutMapping("/{orderId}/cancel")
    public BaseResponse cancelOrder(@PathVariable String orderId, @RequestBody CancelOrderRequest request) {
        CancelOrderCommand command = new CancelOrderCommand(orderId, request.getReason());
        sendCommand(command);
        return success();
    }

    /**
     * 批量创建订单
     */
    @PostMapping("/batch")
    public DataResponse<List<OrderDTO>> createOrders(@Valid @RequestBody List<CreateOrderRequest> requests) {
        // 转换为命令列表
        List<CreateOrderCommand> commands = requests.stream()
                .map(commandAssembler::assemble)
                .collect(Collectors.toList());

        // 批量执行命令
        List<OrderId> orderIds = commands.stream()
                .map(this::sendCommand)
                .collect(Collectors.toList());

        // 批量查询订单
        List<Order> orders = orderIds.stream()
                .map(orderId -> new GetOrderQuery(orderId.getValue()))
                .map(this::sendQuery)
                .collect(Collectors.toList());

        // 转换为DTO列表
        List<OrderDTO> orderDTOs = orderAssembler.assembleList(orders);

        return success(orderDTOs);
    }

    /**
     * 异步创建订单
     */
    @PostMapping("/async")
    public DataResponse<String> createOrderAsync(@Valid @RequestBody CreateOrderRequest request) {
        CreateOrderCommand command = commandAssembler.assemble(request);

        CompletableFuture<OrderId> future = sendCommandAsync(command);

        // 返回任务ID，客户端可以通过任务ID查询结果
        String taskId = UUID.randomUUID().toString();

        // 异步处理完成后的回调（实际项目中可能需要更复杂的任务管理）
        future.thenAccept(orderId -> {
            log.info("异步订单创建完成: taskId={}, orderId={}", taskId, orderId);
            // 可以发送通知或更新任务状态
        });

        return success(taskId);
    }
}
```

### 4. 统一异常处理

#### 全局异常处理器

```java

@RestControllerAdvice
@Slf4j
public class RestApiExceptionHandler {

    /**
     * 业务异常处理
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e, HttpServletRequest request) {
        log.warn("业务异常: {}, 请求路径: {}", e.getMessage(), request.getRequestURI());

        ErrorResponse response = ErrorResponse.businessError(e.getMessage());
        response.setRequestId(getRequestId(request));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * 业务规则违反异常处理
     */
    @ExceptionHandler(BusinessRuleViolationException.class)
    public ResponseEntity<ErrorResponse> handleBusinessRuleViolationException(
            BusinessRuleViolationException e, HttpServletRequest request) {

        log.warn("业务规则违反: {}, 规则: {}, 请求路径: {}",
                e.getMessage(), e.getRule().getRuleName(), request.getRequestURI());

        ErrorResponse response = new ErrorResponse("BUSINESS_RULE_VIOLATION", e.getMessage(), e.getRule().getRuleName());
        response.setRequestId(getRequestId(request));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * 参数验证异常处理
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException e, HttpServletRequest request) {

        Map<String, String> fieldErrors = new HashMap<>();
        e.getBindingResult().getFieldErrors().forEach(error ->
                fieldErrors.put(error.getField(), error.getDefaultMessage())
        );

        log.warn("参数验证失败: {}, 请求路径: {}", fieldErrors, request.getRequestURI());

        ErrorResponse response = ErrorResponse.validationError("参数验证失败", fieldErrors);
        response.setRequestId(getRequestId(request));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * 约束违反异常处理
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(
            ConstraintViolationException e, HttpServletRequest request) {

        Map<String, String> fieldErrors = new HashMap<>();
        e.getConstraintViolations().forEach(violation -> {
            String fieldName = violation.getPropertyPath().toString();
            String message = violation.getMessage();
            fieldErrors.put(fieldName, message);
        });

        log.warn("约束验证失败: {}, 请求路径: {}", fieldErrors, request.getRequestURI());

        ErrorResponse response = ErrorResponse.validationError("约束验证失败", fieldErrors);
        response.setRequestId(getRequestId(request));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * 编排异常处理
     */
    @ExceptionHandler(OrchestrationException.class)
    public ResponseEntity<ErrorResponse> handleOrchestrationException(
            OrchestrationException e, HttpServletRequest request) {

        log.error("编排执行异常: {}, 请求路径: {}", e.getMessage(), request.getRequestURI(), e);

        ErrorResponse response = new ErrorResponse("ORCHESTRATION_ERROR", "业务流程执行失败", e.getMessage());
        response.setRequestId(getRequestId(request));

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    /**
     * 系统异常处理
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e, HttpServletRequest request) {
        log.error("系统异常: {}, 请求路径: {}", e.getMessage(), request.getRequestURI(), e);

        ErrorResponse response = ErrorResponse.systemError("系统内部错误", e.getMessage());
        response.setRequestId(getRequestId(request));

        // 生产环境不返回详细错误信息
        if (isProductionEnvironment()) {
            response.setDetail("系统繁忙，请稍后重试");
            response.setStackTrace(null);
        } else {
            response.setStackTrace(getStackTrace(e));
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    private String getRequestId(HttpServletRequest request) {
        return request.getHeader("X-Request-ID");
    }

    private boolean isProductionEnvironment() {
        // 判断是否为生产环境的逻辑
        return "prod".equals(System.getProperty("spring.profiles.active"));
    }

    private String getStackTrace(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }
}
```

## 🎯 设计原则

### 1. 数据传输优化

- **DTO 专用**：专门用于数据传输，不包含业务逻辑
- **扁平化设计**：避免深层嵌套，便于序列化和传输
- **版本兼容**：支持 API 版本演化和向后兼容

### 2. 组装器模式

- **单一职责**：每个组装器只负责一种类型的转换
- **可复用性**：组装器可以在不同场景中复用
- **组合使用**：复杂对象的组装可以组合多个简单组装器

### 3. 门面封装

- **统一入口**：为外部系统提供统一的访问入口
- **协议适配**：适配不同的通信协议（HTTP、RPC等）
- **异常转换**：将内部异常转换为适合外部的响应格式

### 4. 异常处理

- **统一处理**：全局异常处理器统一处理所有异常
- **分类处理**：不同类型的异常采用不同的处理策略
- **信息安全**：生产环境不暴露敏感的系统信息

## 📝 使用示例

### 完整的 REST API 示例

```java
// 1. 定义请求 DTO
@Data
@EqualsAndHashCode(callSuper = true)
public class CreateUserRequest extends AbstractBaseRequest {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 20, message = "用户名长度必须在3-20之间")
    private String username;

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在6-20之间")
    private String password;

    @Size(max = 50, message = "昵称长度不能超过50")
    private String nickname;

    @Override
    public boolean isValid() {
        return StringUtils.hasText(username)
                && StringUtils.hasText(email)
                && StringUtils.hasText(password);
    }
}

// 2. 定义响应 DTO
@Data
@Builder
public class UserDTO {
    private String userId;
    private String username;
    private String email;
    private String nickname;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer version;
}

// 3. 实现组装器
@Component
public class CreateUserRequestToCommandAssembler extends AbstractDTOAssembler<CreateUserRequest, CreateUserCommand> {

    @Override
    protected CreateUserCommand doAssemble(CreateUserRequest request) {
        return new CreateUserCommand(
                request.getUsername(),
                request.getEmail(),
                request.getPassword(),
                request.getNickname()
        );
    }

    @Override
    public boolean supports(Class<?> sourceType, Class<?> targetType) {
        return CreateUserRequest.class.isAssignableFrom(sourceType)
                && CreateUserCommand.class.isAssignableFrom(targetType);
    }
}

@Component
public class UserToUserDTOAssembler extends AbstractDTOAssembler<User, UserDTO> {

    @Override
    protected UserDTO doAssemble(User user) {
        return UserDTO.builder()
                .userId(user.getId().getValue())
                .username(user.getUsername())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .status(user.getStatus().name())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .version(user.getVersion())
                .build();
    }

    @Override
    public boolean supports(Class<?> sourceType, Class<?> targetType) {
        return User.class.isAssignableFrom(sourceType)
                && UserDTO.class.isAssignableFrom(targetType);
    }
}

// 4. 实现门面
@RestController
@RequestMapping("/api/users")
@Validated
public class UserFacade extends AbstractBaseFacade {

    @Autowired
    private CreateUserRequestToCommandAssembler commandAssembler;

    @Autowired
    private UserToUserDTOAssembler userAssembler;

    /**
     * 创建用户
     */
    @PostMapping
    public DataResponse<UserDTO> createUser(@Valid @RequestBody CreateUserRequest request) {
        // 转换为命令
        CreateUserCommand command = commandAssembler.assemble(request);

        // 发送命令
        UserId userId = sendCommand(command);

        // 查询创建的用户
        GetUserQuery query = new GetUserQuery(userId.getValue());
        User user = sendQuery(query);

        // 转换为DTO
        UserDTO userDTO = userAssembler.assemble(user);

        return success(userDTO);
    }

    /**
     * 获取用户详情
     */
    @GetMapping("/{userId}")
    public DataResponse<UserDTO> getUser(@PathVariable String userId) {
        GetUserQuery query = new GetUserQuery(userId);
        User user = sendQuery(query);

        UserDTO userDTO = userAssembler.assemble(user);
        return success(userDTO);
    }

    /**
     * 分页查询用户
     */
    @GetMapping
    public PagedResult<UserDTO> getUsers(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {

        PageUserQuery query = new PageUserQuery(page, size, keyword);
        PageResult<User> pageResult = sendQuery(query);

        List<UserDTO> userDTOs = userAssembler.assembleList(pageResult.getContent());

        return pagedResult(userDTOs, page, size, pageResult.getTotalElements());
    }

    /**
     * 更新用户
     */
    @PutMapping("/{userId}")
    public DataResponse<UserDTO> updateUser(
            @PathVariable String userId,
            @Valid @RequestBody UpdateUserRequest request) {

        UpdateUserCommand command = new UpdateUserCommand(
                userId,
                request.getNickname(),
                request.getEmail()
        );

        sendCommand(command);

        // 查询更新后的用户
        GetUserQuery query = new GetUserQuery(userId);
        User user = sendQuery(query);

        UserDTO userDTO = userAssembler.assemble(user);
        return success(userDTO);
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{userId}")
    public BaseResponse deleteUser(@PathVariable String userId) {
        DeleteUserCommand command = new DeleteUserCommand(userId);
        sendCommand(command);
        return success();
    }
}
```

### 复杂业务场景示例

```java

@RestController
@RequestMapping("/api/orders")
public class ComplexOrderFacade extends AbstractBaseFacade {

    /**
     * 复杂订单处理（包含多个步骤）
     */
    @PostMapping("/complex")
    public DataResponse<OrderProcessResult> processComplexOrder(@Valid @RequestBody ComplexOrderRequest request) {

        // 1. 验证客户资格
        ValidateCustomerCommand validateCommand = new ValidateCustomerCommand(request.getCustomerId());
        CustomerValidationResult validationResult = sendCommand(validateCommand);

        if (!validationResult.isValid()) {
            return error("客户资格验证失败: " + validationResult.getReason());
        }

        // 2. 检查库存
        CheckInventoryQuery inventoryQuery = new CheckInventoryQuery(request.getItems());
        InventoryCheckResult inventoryResult = sendQuery(inventoryQuery);

        if (!inventoryResult.isAvailable()) {
            return error("库存不足");
        }

        // 3. 创建订单
        CreateOrderCommand createCommand = new CreateOrderCommand(request);
        OrderId orderId = sendCommand(createCommand);

        // 4. 处理支付
        ProcessPaymentCommand paymentCommand = new ProcessPaymentCommand(orderId, request.getPaymentInfo());
        PaymentResult paymentResult = sendCommand(paymentCommand);

        if (!paymentResult.isSuccess()) {
            // 取消订单
            CancelOrderCommand cancelCommand = new CancelOrderCommand(orderId.getValue(), "支付失败");
            sendCommand(cancelCommand);
            return error("支付失败: " + paymentResult.getFailureReason());
        }

        // 5. 确认订单
        ConfirmOrderCommand confirmCommand = new ConfirmOrderCommand(orderId.getValue());
        sendCommand(confirmCommand);

        // 6. 查询最终结果
        GetOrderQuery orderQuery = new GetOrderQuery(orderId.getValue());
        Order order = sendQuery(orderQuery);

        OrderProcessResult result = OrderProcessResult.builder()
                .orderId(orderId.getValue())
                .status("SUCCESS")
                .paymentId(paymentResult.getPaymentId())
                .totalAmount(order.getTotalAmount().getAmount())
                .processedAt(LocalDateTime.now())
                .build();

        return success(result);
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
        <artifactId>ddd-application</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>
</dependencies>
```

### 模块依赖

- **依赖**：ddd-common、ddd-application
- **被依赖**：无（最外层）
- **协调**：应用层和外部系统

## 🧪 测试

接口层测试专注于 API 契约和集成测试：

```java

@SpringBootTest
@AutoConfigureTestDatabase
@Transactional
class OrderFacadeIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private OrderFacade orderFacade;

    @Test
    void should_create_order_successfully() {
        // Given
        CreateOrderRequest request = CreateOrderRequest.builder()
                .customerId("customer-001")
                .items(Arrays.asList(
                        OrderItemRequest.builder()
                                .productId("product-001")
                                .quantity(2)
                                .unitPrice(new BigDecimal("99.99"))
                                .build()
                ))
                .shippingAddress("北京市朝阳区")
                .build();

        // When
        ResponseEntity<DataResponse<OrderDTO>> response = restTemplate.postForEntity(
                "/api/orders",
                request,
                new ParameterizedTypeReference<DataResponse<OrderDTO>>() {
                }
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getData().getCustomerId()).isEqualTo("customer-001");
    }

    @Test
    void should_return_validation_error_when_request_invalid() {
        // Given
        CreateOrderRequest request = CreateOrderRequest.builder()
                .customerId("") // 无效的客户ID
                .items(Collections.emptyList()) // 空的订单项
                .build();

        // When
        ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(
                "/api/orders",
                request,
                ErrorResponse.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getCode()).isEqualTo("VALIDATION_ERROR");
        assertThat(response.getBody().getFieldErrors()).isNotEmpty();
    }
}

@ExtendWith(MockitoExtension.class)
class OrderFacadeUnitTest {

    @Mock
    private ICommandBus commandBus;

    @Mock
    private IQueryBus queryBus;

    @Mock
    private CreateOrderRequestToCommandAssembler commandAssembler;

    @Mock
    private OrderToOrderDTOAssembler orderAssembler;

    @InjectMocks
    private OrderFacade orderFacade;

    @Test
    void should_create_order_successfully() {
        // Given
        CreateOrderRequest request = createValidRequest();
        CreateOrderCommand command = new CreateOrderCommand();
        OrderId orderId = OrderId.of("order-001");
        Order order = mock(Order.class);
        OrderDTO orderDTO = new OrderDTO();

        when(commandAssembler.assemble(request)).thenReturn(command);
        when(commandBus.send(command)).thenReturn(orderId);
        when(queryBus.send(any(GetOrderQuery.class))).thenReturn(order);
        when(orderAssembler.assemble(order)).thenReturn(orderDTO);

        // When
        DataResponse<OrderDTO> response = orderFacade.createOrder(request);

        // Then
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).isEqualTo(orderDTO);

        verify(commandAssembler).assemble(request);
        verify(commandBus).send(command);
        verify(queryBus).send(any(GetOrderQuery.class));
        verify(orderAssembler).assemble(order);
    }
}
```

## 📚 最佳实践

### 1. DTO 设计

- **数据传输专用**：DTO 只用于数据传输，不包含业务逻辑
- **验证注解**：使用 Bean Validation 注解进行参数验证
- **版本兼容**：考虑 API 版本演化，保持向后兼容
- **文档化**：为 DTO 字段添加清晰的注释

### 2. 组装器使用

- **单一职责**：每个组装器只负责一种转换
- **可测试性**：组装器应该易于单元测试
- **性能考虑**：避免在组装过程中进行复杂的业务逻辑处理
- **异常处理**：在组装过程中进行适当的异常处理

### 3. 门面设计

- **统一接口**：为外部系统提供统一、简洁的接口
- **协议无关**：门面层应该与具体的通信协议解耦
- **异常转换**：将内部异常转换为适合外部的响应格式
- **日志记录**：记录关键的业务操作和异常信息

### 4. 异常处理

- **分层处理**：不同类型的异常采用不同的处理策略
- **信息安全**：生产环境不暴露敏感的系统信息
- **用户友好**：提供用户友好的错误信息
- **可追踪性**：包含请求ID等信息便于问题追踪

### 5. API 设计

- **RESTful 风格**：遵循 REST 设计原则
- **幂等性**：确保相同请求的幂等性
- **状态码**：使用合适的 HTTP 状态码
- **分页支持**：为列表查询提供分页支持

## 🔧 配置和扩展

### 自定义异常处理

```java

@RestControllerAdvice
public class CustomExceptionHandler extends RestApiExceptionHandler {

    /**
     * 自定义业务异常处理
     */
    @ExceptionHandler(CustomBusinessException.class)
    public ResponseEntity<ErrorResponse> handleCustomBusinessException(
            CustomBusinessException e, HttpServletRequest request) {

        log.warn("自定义业务异常: {}", e.getMessage());

        ErrorResponse response = new ErrorResponse("CUSTOM_ERROR", e.getMessage(), e.getDetail());
        response.setRequestId(getRequestId(request));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
}
```

### 自定义组装器

```java

@Component
public class CustomOrderAssembler extends AbstractDTOAssembler<Order, CustomOrderDTO> {

    @Override
    protected CustomOrderDTO doAssemble(Order order) {
        // 自定义转换逻辑
        return CustomOrderDTO.builder()
                .id(order.getId().getValue())
                .customerName(getCustomerName(order.getCustomerId()))
                .itemCount(order.getItems().size())
                .statusDescription(getStatusDescription(order.getStatus()))
                .build();
    }

    private String getCustomerName(CustomerId customerId) {
        // 获取客户名称的逻辑
        return "Customer Name";
    }

    private String getStatusDescription(OrderStatus status) {
        // 状态描述转换逻辑
        return status.getDescription();
    }
}
```

### API 版本控制

```java

@RestController
@RequestMapping("/api/v1/orders")
public class OrderV1Facade extends AbstractBaseFacade {
    // V1 版本的 API 实现
}

@RestController
@RequestMapping("/api/v2/orders")
public class OrderV2Facade extends AbstractBaseFacade {
    // V2 版本的 API 实现，保持向后兼容
}
```

## 🚀 性能优化

### 响应缓存

```java

@RestController
@RequestMapping("/api/orders")
public class CachedOrderFacade extends AbstractBaseFacade {

    @GetMapping("/{orderId}")
    @Cacheable(value = "orders", key = "#orderId")
    public DataResponse<OrderDTO> getOrder(@PathVariable String orderId) {
        // 查询逻辑
    }

    @PutMapping("/{orderId}")
    @CacheEvict(value = "orders", key = "#orderId")
    public DataResponse<OrderDTO> updateOrder(@PathVariable String orderId, @RequestBody UpdateOrderRequest request) {
        // 更新逻辑
    }
}
```

### 异步处理

```java

@RestController
@RequestMapping("/api/orders")
public class AsyncOrderFacade extends AbstractBaseFacade {

    @PostMapping("/async")
    public DataResponse<String> createOrderAsync(@RequestBody CreateOrderRequest request) {
        String taskId = UUID.randomUUID().toString();

        CompletableFuture.supplyAsync(() -> {
            CreateOrderCommand command = commandAssembler.assemble(request);
            return sendCommand(command);
        }).thenAccept(orderId -> {
            // 异步处理完成后的回调
            notifyOrderCreated(taskId, orderId);
        });

        return success(taskId);
    }
}
```

### 批量处理优化

```java

@RestController
@RequestMapping("/api/orders")
public class BatchOrderFacade extends AbstractBaseFacade {

    @PostMapping("/batch")
    public DataResponse<List<OrderDTO>> createOrdersBatch(@RequestBody List<CreateOrderRequest> requests) {
        // 参数验证
        requests.forEach(request -> Assert.isTrue(request.isValid(), "请求参数无效"));

        // 批量转换
        List<CreateOrderCommand> commands = requests.stream()
                .map(commandAssembler::assemble)
                .collect(Collectors.toList());

        // 并行执行
        List<OrderId> orderIds = commands.parallelStream()
                .map(this::sendCommand)
                .collect(Collectors.toList());

        // 批量查询
        List<Order> orders = orderIds.stream()
                .map(orderId -> new GetOrderQuery(orderId.getValue()))
                .map(this::sendQuery)
                .collect(Collectors.toList());

        List<OrderDTO> orderDTOs = orderAssembler.assembleList(orders);
        return success(orderDTOs);
    }
}
```

## 📋 总结

DDD Interfaces 模块提供了完整的接口层解决方案，包括：

- **DTO 体系**：完整的请求响应对象定义
- **组装器模式**：灵活的对象转换机制
- **门面封装**：统一的外部接口
- **异常处理**：全局统一的异常处理机制
- **验证支持**：完整的参数验证体系

通过这些功能，开发者可以构建出结构清晰、易于维护、用户友好的接口层服务，为外部系统提供稳定可靠的 API 接口。
