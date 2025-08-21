package io.github.anthem37.example.user.application.command;

import io.github.anthem37.ddd.common.cqrs.command.ICommand;
import io.github.anthem37.example.user.domain.valueobject.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 更新用户状态命令
 * 展示DDD框架的命令模式
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class UpdateUserStatusCommand implements ICommand<Void> {

    private String userId;
    private UserStatus status;
}