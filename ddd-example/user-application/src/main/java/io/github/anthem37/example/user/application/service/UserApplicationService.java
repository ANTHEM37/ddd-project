package io.github.anthem37.example.user.application.service;

import io.github.anthem37.ddd.application.command.ICommandBus;
import io.github.anthem37.ddd.application.orchestration.Orchestration;
import io.github.anthem37.ddd.application.query.IQueryBus;
import io.github.anthem37.ddd.application.service.IApplicationService;
import io.github.anthem37.example.user.application.command.CreateUserCommand;
import io.github.anthem37.example.user.application.command.UpdateUserStatusCommand;
import io.github.anthem37.example.user.application.dto.UserDTO;
import io.github.anthem37.example.user.application.orchestration.UserRegistrationOrchestration;
import io.github.anthem37.example.user.application.query.GetUserQuery;
import io.github.anthem37.example.user.domain.valueobject.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.stereotype.Service;

/**
 * 用户应用服务
 * 展示DDD框架的应用服务特性
 */
@Service
@AllArgsConstructor
public class UserApplicationService implements IApplicationService {

    @Getter
    private final ICommandBus commandBus;
    @Getter
    private final IQueryBus queryBus;
    private final UserRegistrationOrchestration userRegistrationOrchestration;

    /**
     * 创建用户
     */
    public String createUser(String username, String email, String password) {
        CreateUserCommand command = new CreateUserCommand(username, email, password);
        return commandBus.send(command);
    }

    /**
     * 激活用户
     */
    public void activateUser(String userId) {
        UpdateUserStatusCommand command = new UpdateUserStatusCommand(userId, UserStatus.ACTIVE);
        commandBus.send(command);
    }

    /**
     * 停用用户
     */
    public void deactivateUser(String userId) {
        UpdateUserStatusCommand command = new UpdateUserStatusCommand(userId, UserStatus.INACTIVE);
        commandBus.send(command);
    }

    /**
     * 获取用户信息
     */
    public UserDTO getUser(String userId) {
        GetUserQuery query = new GetUserQuery(userId);
        return queryBus.send(query);
    }

    /**
     * 使用业务编排创建用户
     * 展示DDD框架的业务编排特性
     */
    public Orchestration.Result createUserWithOrchestration(String username, String email, String password) {
        return userRegistrationOrchestration.executeUserRegistration(username, email, password);
    }

    /**
     * 获取用户注册流程的PlantUML图
     */
    public String getUserRegistrationFlowDiagram() {
        return userRegistrationOrchestration.createUserRegistrationFlow().toPlantUML();
    }

}