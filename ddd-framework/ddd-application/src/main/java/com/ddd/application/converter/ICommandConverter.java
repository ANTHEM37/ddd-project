package com.ddd.application.converter;

import com.ddd.application.command.ICommand;

/**
 * 命令转换器接口
 * 负责Command与领域模型之间的转换
 *
 * @param <C> 命令类型
 * @param <M> 领域模型类型
 * @author anthem37
 * @date 2025/8/19 11:00:00
 */
public interface ICommandConverter<C extends ICommand<?>, M> {

    /**
     * 将命令转换为领域模型
     *
     * @param command 命令对象
     * @return 领域模型
     */
    M convert(C command);

    /**
     * 验证命令是否可以转换
     *
     * @param command 命令对象
     * @return 是否可以转换
     */
    default boolean canConvert(C command) {
        return command != null && command.isValid();
    }
}