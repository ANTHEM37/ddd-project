package com.ddd.interfaces.assembler;

import java.util.List;
import java.util.stream.Collectors;

/**
 * DTO装配器接口
 * 负责在领域对象和DTO之间进行转换
 *
 * @param <D> DTO类型
 * @param <E> 领域对象类型
 * @author anthem37
 * @date 2025/8/14 11:15:27
 */
public interface IDTOAssembler<D, E> {

    /**
     * 将领域对象转换为DTO
     */
    D toDto(E entity);

    /**
     * 将DTO转换为领域对象
     */
    E toEntity(D dto);

    /**
     * 将领域对象列表转换为DTO列表
     */
    default List<D> toDtoList(List<E> entities) {
        return entities.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * 将DTO列表转换为领域对象列表
     */
    default List<E> toEntityList(List<D> dtos) {
        return dtos.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }

    /**
     * 更新领域对象
     */
    default void updateEntity(D dto, E entity) {
        // 默认实现为空，子类可以重写
    }
}