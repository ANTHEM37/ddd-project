package io.github.anthem37.example.user.application.command;

import io.github.anthem37.ddd.common.cqrs.command.ICommand;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 创建用户命令
 * 展示DDD框架的命令模式
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class CreateUserCommand implements ICommand<String> {

    private String username;
    private String email;
    private String password;
}