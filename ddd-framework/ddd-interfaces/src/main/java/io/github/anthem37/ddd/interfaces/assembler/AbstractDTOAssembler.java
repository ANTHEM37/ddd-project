package io.github.anthem37.ddd.interfaces.assembler;

import io.github.anthem37.ddd.common.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DTO组装器抽象基类
 * 提供批量组装和拆解的默认实现
 *
 * @param <D> DTO类型
 * @param <M> 领域模型类型
 * @author anthem37
 * @date 2025/8/19 11:00:00
 */
public abstract class AbstractDTOAssembler<D, M> implements IDTOAssembler<D, M> {

    /**
     * 批量组装领域模型为DTO列表
     *
     * @param models 领域模型列表
     * @return DTO列表
     */
    public List<D> assembleList(List<M> models) {
        if (CollectionUtils.isEmpty(models)) {
            return Collections.emptyList();
        }
        return models.stream()
                .map(this::assemble)
                .collect(Collectors.toList());
    }

    /**
     * 批量拆解DTO列表为领域模型
     *
     * @param dtos DTO列表
     * @return 领域模型列表
     */
    public List<M> disassembleList(List<D> dtos) {
        if (CollectionUtils.isEmpty(dtos)) {
            return Collections.emptyList();
        }
        return dtos.stream()
                .map(this::disassemble)
                .collect(Collectors.toList());
    }

    /**
     * 安全组装，处理null值
     *
     * @param model 领域模型
     * @return DTO对象，如果输入为null则返回null
     */
    public D safeAssemble(M model) {
        return model == null ? null : assemble(model);
    }

    /**
     * 安全拆解，处理null值
     *
     * @param dto DTO对象
     * @return 领域模型，如果输入为null则返回null
     */
    public M safeDisassemble(D dto) {
        return dto == null ? null : disassemble(dto);
    }
}