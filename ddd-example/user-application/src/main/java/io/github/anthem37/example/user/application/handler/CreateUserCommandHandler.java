package io.github.anthem37.example.user.application.handler;

import io.github.anthem37.ddd.common.cqrs.command.ICommandHandler;
import io.github.anthem37.example.user.application.command.CreateUserCommand;
import io.github.anthem37.example.user.domain.model.User;
import io.github.anthem37.example.user.domain.repository.IUserRepository;
import io.github.anthem37.example.user.domain.service.UserDomainService;
import io.github.anthem37.example.user.domain.valueobject.Email;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 创建用户命令处理器
 * 展示DDD框架的命令处理器特性
 */
@Component
public class CreateUserCommandHandler implements ICommandHandler<CreateUserCommand, String> {

    private final IUserRepository userRepository;
    private final UserDomainService userDomainService;

    public CreateUserCommandHandler(IUserRepository userRepository, UserDomainService userDomainService) {
        this.userRepository = userRepository;
        this.userDomainService = userDomainService;
    }

    @Override
    public Class<CreateUserCommand> getSupportedCommandType() {
        return CreateUserCommand.class;
    }

    @Override
    @Transactional
    public String handle(CreateUserCommand command) {
        // 验证密码强度
        userDomainService.validatePasswordStrength(command.getPassword());

        // 创建邮箱值对象
        Email email = Email.of(command.getEmail());

        // 检查用户是否可以注册
        userDomainService.checkUserCanRegister(command.getUsername(), email);

        // 创建用户聚合
        User user = User.create(command.getUsername(), email, command.getPassword());

        // 保存用户
        userRepository.save(user);

        return user.getId().getValue();
    }
}