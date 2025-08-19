# DDD Common Module

DDD 框架的通用模块，提供跨层使用的基础工具类、异常定义、业务规则接口和转换器注册中心。

## 📦 模块结构

```
ddd-common/
├── assertion/           # 断言工具
├── converter/          # 转换器注册中心
├── exception/          # 异常定义
├── model/             # 通用模型接口
└── util/              # 工具类
```

## 🔧 核心组件

### 1. 断言工具 (Assert)

提供统一的业务异常处理和参数验证。

#### 基本断言
```java
// 非空断言
Assert.notNull(user, "用户不能为空");
Assert.hasText(username, "用户名不能为空");
Assert.notEmpty(orderItems, "订单项不能为空");

// 条件断言
Assert.isTrue(age >= 18, "年龄必须大于等于18岁");
Assert.isFalse(user.isDeleted(), "用户已被删除");

// 数值断言
Assert.isNotNegative(amount, "金额不能为负数");
Assert.inRange(score, 0, 100, "分数必须在0-100之间");

// 字符串断言
Assert.hasLength(password, 6, 20, "密码长度必须在6-20位之间");
Assert.matches(email, "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$", "邮箱格式不正确");
```

#### 业务规则断言
```java
// 单个规则检查
IBusinessRule rule = new UserCanPlaceOrderRule(user);
Assert.isTrue(rule.isSatisfied(), rule.getMessage());

// 多个规则检查
IBusinessRule[] rules = {
    new UserCanPlaceOrderRule(user),
    new OrderAmountValidRule(amount),
    new InventoryAvailableRule(productId, quantity)
};
for (IBusinessRule businessRule : rules) {
    Assert.isTrue(businessRule.isSatisfied(), businessRule.getMessage());
}
```

#### 编排专用断言
```java
// 编排流程中的断言，抛出 OrchestrationException
Assert.orchestrationNotNull(command, "命令不能为空");
Assert.orchestrationIsTrue(condition, "条件不满足");
Assert.orchestrationFail("编排执行失败");
```

### 2. 异常体系

#### BusinessException - 业务异常
```java
public class OrderService {
    
    public void processOrder(Order order) {
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BusinessException("ORDER_001", "订单状态不允许处理");
        }
        
        try {
            // 业务处理
        } catch (Exception e) {
            throw new BusinessException("ORDER_002", "订单处理失败", e);
        }
    }
}
```

#### BusinessRuleViolationException - 业务规则违反异常
```java
public class Order extends AbstractAggregateRoot<OrderId> {
    
    public void cancel() {
        IBusinessRule rule = new OrderCanBeCancelledRule(this.status, this.createTime);
        if (!rule.isSatisfied()) {
            throw new BusinessRuleViolationException(rule);
        }
        // 取消逻辑
    }
}
```

#### OrchestrationException - 编排异常
```java
public class OrderProcessOrchestration {
    
    private void validateNode(String nodeId) {
        if (StringUtils.isEmpty(nodeId)) {
            throw new OrchestrationException("节点ID不能为空");
        }
    }
}
```

### 3. 业务规则接口 (IBusinessRule)

定义业务不变性和约束条件的标准接口。

```java
// 实现业务规则
public class UserCanPlaceOrderRule implements IBusinessRule {
    
    private final User user;
    
    public UserCanPlaceOrderRule(User user) {
        this.user = user;
    }
    
    @Override
    public boolean isSatisfied() {
        return user != null 
            && user.isActive() 
            && !user.isBlocked()
            && user.hasValidPaymentMethod();
    }
    
    @Override
    public String getMessage() {
        if (user == null) return "用户不存在";
        if (!user.isActive()) return "用户未激活";
        if (user.isBlocked()) return "用户已被冻结";
        if (!user.hasValidPaymentMethod()) return "用户没有有效的支付方式";
        return "用户可以下单";
    }
    
    @Override
    public String getRuleName() {
        return "用户下单资格规则";
    }
}

// 在聚合根中使用
public class Order extends AbstractAggregateRoot<OrderId> {
    
    public static Order create(User user, List<OrderItem> items) {
        // 检查业务规则
        IBusinessRule[] creationRules = {
            new UserCanPlaceOrderRule(user),
            new OrderItemsValidRule(items),
            new InventoryAvailableRule(items)
        };
        for (IBusinessRule rule : creationRules) {
            Assert.isTrue(rule.isSatisfied(), rule.getMessage());
        }
        
        Order order = new Order(OrderId.generate());
        order.addDomainEvent(new OrderCreatedEvent(order.getId()));
        return order;
    }
}
```

### 4. 转换器注册中心 (ConverterRegistry)

参考 DomainEventPublisher 的设计模式，提供转换器的统一注册和获取。

```java
// 在应用启动时设置管理器（由基础设施层实现）
@PostConstruct
public void initializeConverters() {
    ConverterRegistry.setConverterManager(springConverterManager);
}

// 注册转换器
ConverterRegistry.register("userToDTO", userToDTOConverter);

// 获取转换器
UserToDTOConverter converter = ConverterRegistry.getConverter("userToDTO", UserToDTOConverter.class);

// 根据类型获取转换器（利用Spring容器）
UserToDTOConverter converter = ConverterRegistry.getConverter(UserToDTOConverter.class);

// 检查转换器是否存在
if (ConverterRegistry.contains("userToDTO")) {
    // 使用转换器
}
```

### 5. 工具类

#### StringUtils - 字符串工具
```java
// 空值检查
boolean isEmpty = StringUtils.isEmpty(str);
boolean hasText = StringUtils.hasText(str);

// 字符串操作
String trimmed = StringUtils.trim(str);
String capitalized = StringUtils.capitalize(str);

// 格式化
String formatted = StringUtils.format("Hello {}, welcome to {}", name, system);
```

#### CollectionUtils - 集合工具
```java
// 空值检查
boolean isEmpty = CollectionUtils.isEmpty(list);
boolean isNotEmpty = CollectionUtils.isNotEmpty(list);

// 集合操作
List<String> filtered = CollectionUtils.filter(list, item -> item.startsWith("A"));
List<Integer> mapped = CollectionUtils.map(list, String::length);

// 安全获取
String first = CollectionUtils.getFirst(list);
String last = CollectionUtils.getLast(list);
```

#### ReflectionUtils - 反射工具
```java
// 获取字段值
Object value = ReflectionUtils.getFieldValue(object, "fieldName");

// 设置字段值
ReflectionUtils.setFieldValue(object, "fieldName", newValue);

// 调用方法
Object result = ReflectionUtils.invokeMethod(object, "methodName", args);

// 获取泛型类型
Class<?> genericType = ReflectionUtils.getGenericType(field);
```

## 🎯 设计原则

### 1. 纯净性
- 不依赖任何外部框架
- 可以在任何 Java 环境中使用
- 专注于通用功能的实现

### 2. 类型安全
- 大量使用泛型确保编译时类型检查
- 提供类型安全的工具方法
- 避免强制类型转换

### 3. 异常统一
- 统一的异常处理机制
- 明确的异常分类和层次
- 丰富的异常信息

### 4. 易用性
- 简洁的 API 设计
- 链式调用支持
- 丰富的重载方法

## 📝 使用示例

### 参数验证示例
```java
@Service
public class UserService {
    
    public User createUser(String username, String email, int age) {
        // 参数验证
        Assert.hasText(username, "用户名不能为空");
        Assert.matches(email, EMAIL_REGEX, "邮箱格式不正确");
        Assert.inRange(age, 0, 150, "年龄必须在0-150之间");
        
        // 业务规则验证
        IBusinessRule usernameRule = new UsernameUniqueRule(username);
        Assert.isTrue(usernameRule.isSatisfied(), usernameRule.getMessage());
        
        IBusinessRule emailRule = new EmailUniqueRule(email);
        Assert.isTrue(emailRule.isSatisfied(), emailRule.getMessage());
        
        return new User(username, email, age);
    }
}
```

### 业务规则组合示例
```java
public class OrderCreationRule implements IBusinessRule {
    
    private final List<IBusinessRule> rules;
    
    public OrderCreationRule(User user, List<OrderItem> items) {
        this.rules = Arrays.asList(
            new UserCanPlaceOrderRule(user),
            new OrderItemsValidRule(items),
            new InventoryAvailableRule(items),
            new PaymentMethodValidRule(user.getPaymentMethod())
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

### 工具类使用示例
```java
@Component
public class DataProcessor {
    
    public List<String> processNames(List<String> names) {
        if (CollectionUtils.isEmpty(names)) {
            return Collections.emptyList();
        }
        
        return names.stream()
            .filter(StringUtils::hasText)
            .map(StringUtils::trim)
            .map(StringUtils::capitalize)
            .collect(Collectors.toList());
    }
    
    public void processUser(Object userObj) {
        Assert.isInstanceOf(userObj, User.class, "对象必须是User类型");
        
        User user = (User) userObj;
        String email = (String) ReflectionUtils.getFieldValue(user, "email");
        
        Assert.matches(email, EMAIL_REGEX, "用户邮箱格式不正确");
    }
}
```

## 🔗 依赖关系

### Maven 依赖
```xml
<dependencies>
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>
    <dependency>
        <groupId>org.slf4j</groupId>
        <artifactId>slf4j-api</artifactId>
    </dependency>
</dependencies>
```

### 模块依赖
- 无依赖其他 DDD 模块
- 被所有其他模块依赖
- 提供框架的基础能力

## 🧪 测试

模块包含完整的单元测试，覆盖所有核心功能：

```bash
# 运行测试
mvn test

# 查看测试覆盖率
mvn jacoco:report
```

测试用例包括：
- Assert 工具类的各种断言场景
- 异常类的创建和信息获取
- 业务规则的组合和验证
- 工具类的边界条件测试

## 📚 最佳实践

1. **统一异常处理**：使用 Assert 工具类统一抛出业务异常
2. **业务规则封装**：将复杂的业务逻辑封装为 IBusinessRule 实现
3. **参数验证前置**：在方法入口处进行参数验证
4. **异常信息明确**：提供清晰、具体的异常信息
5. **工具类优先**：优先使用框架提供的工具类而不是自己实现