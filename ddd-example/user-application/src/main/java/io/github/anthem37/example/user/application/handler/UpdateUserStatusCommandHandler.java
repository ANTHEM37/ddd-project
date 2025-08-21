package io.github.anthem37.example.user.application.handler;

import io.github.anthem37.ddd.common.cqrs.command.ICommandHandler;
import io.github.anthem37.example.user.application.command.UpdateUserStatusCommand;
import io.github.anthem37.example.user.domain.model.User;
import io.github.anthem37.example.user.domain.repository.IUserRepository;
import io.github.anthem37.example.user.domain.valueobject.UserId;
import io.github.anthem37.example.user.domain.valueobject.UserStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 更新用户状态命令处理器
 * 展示DDD框架的命令处理器特性
 */
@Component
public class UpdateUserStatusCommandHandler implements ICommandHandler<UpdateUserStatusCommand, Void> {
    
    private final IUserRepository userRepository;
    
    public UpdateUserStatusCommandHandler(IUserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    @Override
    public Class<UpdateUserStatusCommand> getSupportedCommandType() {
        return UpdateUserStatusCommand.class;
    }
    
    @Override
    @Transactional
    public Void handle(UpdateUserStatusCommand command) {
        UserId userId = UserId.of(command.getUserId());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在: " + command.getUserId()));
        
        UserStatus targetStatus = command.getStatus();
        
        // 根据目标状态执行相应的业务操作
        switch (targetStatus) {
            case ACTIVE:
                user.activate();
                break;
            case INACTIVE:
                user.deactivate();
                break;
            default:
                throw new IllegalArgumentException("不支持的状态变更: " + targetStatus);
        }
        
        // 保存用户
        userRepository.save(user);
        
        return null;
    }
}