package io.github.anthem37.ddd.common.cqrs.converter;

import io.github.anthem37.ddd.common.cqrs.command.ICommand;
import io.github.anthem37.ddd.common.exception.BusinessException;

/**
 * 命令转换器抽象基类
 * 提供通用的转换逻辑和异常处理
 *
 * @param <C> 命令类型
 * @param <M> 领域模型类型
 * @author anthem37
 * @date 2025/8/19 11:00:00
 */
public abstract class AbstractCommandConverter<C extends ICommand<?>, M> implements ICommandConverter<C, M> {

    @Override
    public M convert(C command) {
        if (!canConvert(command)) {
            throw new BusinessException("命令转换失败：命令无效或为空");
        }

        try {
            return doConvert(command);
        } catch (Exception e) {
            throw new BusinessException("命令转换过程中发生错误：" + e.getMessage(), e);
        }
    }

    /**
     * 执行具体的转换逻辑
     * 子类必须实现此方法
     *
     * @param command 命令对象
     * @return 领域模型
     */
    protected abstract M doConvert(C command);

    /**
     * 转换前的预处理
     * 子类可以重写此方法进行自定义预处理
     *
     * @param command 命令对象
     */
    protected void preProcess(C command) {
        // 默认空实现
    }

    /**
     * 转换后的后处理
     * 子类可以重写此方法进行自定义后处理
     *
     * @param command 命令对象
     * @param model   转换后的领域模型
     */
    protected void postProcess(C command, M model) {
        // 默认空实现
    }
}