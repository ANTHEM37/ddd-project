package io.github.anthem37.example.user.application.orchestration;

import io.github.anthem37.ddd.common.cqrs.command.ICommandBus;
import io.github.anthem37.ddd.common.cqrs.query.IQueryBus;
import io.github.anthem37.ddd.common.orchestration.Orchestration;
import io.github.anthem37.example.user.application.command.CreateUserCommand;
import io.github.anthem37.example.user.application.query.GetUserQuery;
import io.github.anthem37.example.user.domain.valueobject.Email;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 用户注册业务编排
 * 展示DDD框架的业务编排特性
 */
@Component
@AllArgsConstructor
public class UserRegistrationOrchestration {

    private final ICommandBus commandBus;
    private final IQueryBus queryBus;

    /**
     * 创建用户注册编排流程
     */
    public Orchestration createUserRegistrationFlow() {
        return new Orchestration("user-registration", "用户注册流程", commandBus, queryBus)
                // 1. 验证邮箱格式
                .addCondition("validate-email", "验证邮箱格式", ctx -> {
                    String email = ctx.getVariable("email", String.class);
                    try {
                        Email.of(email);
                        return true;
                    } catch (Exception e) {
                        ctx.setVariable("error", "邮箱格式不正确");
                        return false;
                    }
                })

                // 2. 验证密码强度
                .addCondition("validate-password", "验证密码强度", ctx -> {
                    String password = ctx.getVariable("password", String.class);
                    if (password == null || password.length() < 8) {
                        ctx.setVariable("error", "密码长度至少8位");
                        return false;
                    }
                    boolean hasLetter = password.matches(".*[a-zA-Z].*");
                    boolean hasDigit = password.matches(".*\\d.*");
                    if (!hasLetter || !hasDigit) {
                        ctx.setVariable("error", "密码必须包含字母和数字");
                        return false;
                    }
                    return true;
                })

                // 3. 创建用户命令
                .addCommand("create-user", "创建用户", ctx -> {
                    String username = ctx.getVariable("username", String.class);
                    String email = ctx.getVariable("email", String.class);
                    String password = ctx.getVariable("password", String.class);
                    return new CreateUserCommand(username, email, password);
                })

                // 4. 查询创建的用户
                .addQuery("get-user", "获取用户信息", ctx -> {
                    String userId = ctx.getResult("create-user", String.class);
                    return new GetUserQuery(userId);
                })

                // 5. 设置成功标志
                .addGeneric("set-success", "设置成功标志", ctx -> {
                    ctx.setVariable("success", true);
                    return "注册成功";
                })

                // 6. 设置失败标志
                .addGeneric("set-failure", "设置失败标志", ctx -> {
                    ctx.setVariable("success", false);
                    return "注册失败";
                })

                // 连接流程
                .connectWhenTrue("validate-email", "validate-password").connectWhenFalse("validate-email", "set-failure").connectWhenTrue("validate-password", "create-user").connectWhenFalse("validate-password", "set-failure").connect("create-user", "get-user").connect("get-user", "set-success");
    }

    /**
     * 执行用户注册流程
     */
    public Orchestration.Result executeUserRegistration(String username, String email, String password) {
        Orchestration orchestration = createUserRegistrationFlow();
        Orchestration.Context context = new Orchestration.Context("user-registration");

        // 设置输入参数
        context.setVariable("username", username);
        context.setVariable("email", email);
        context.setVariable("password", password);

        return orchestration.execute(context);
    }
}