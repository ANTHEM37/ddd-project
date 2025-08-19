package com.ddd.interfaces.assembler;

/**
 * DTO组装器接口
 * 负责接口层的复杂对象组装和拆解
 *
 * @param <D> DTO类型
 * @param <M> 领域模型类型
 * @author anthem37
 * @date 2025/8/19 11:00:00
 */
public interface IDTOAssembler<D, M> {

    /**
     * 将领域模型组装为DTO
     *
     * @param model 领域模型
     * @return DTO对象
     */
    D assemble(M model);

    /**
     * 将DTO拆解为领域模型
     *
     * @param dto DTO对象
     * @return 领域模型
     */
    M disassemble(D dto);
}