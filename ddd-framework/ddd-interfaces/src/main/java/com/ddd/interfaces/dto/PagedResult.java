package com.ddd.interfaces.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 分页结果DTO
 * 用于向客户端返回分页数据
 *
 * @param <T> 数据项类型
 * @author anthem37
 * @date 2025/8/14 15:27:43
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PagedResult<T> {

    /**
     * 当前页码
     */
    private int pageNumber;

    /**
     * 每页大小
     */
    private int pageSize;

    /**
     * 总记录数
     */
    private long totalElements;

    /**
     * 总页数
     */
    private int totalPages;

    /**
     * 是否为第一页
     */
    private boolean first;

    /**
     * 是否为最后一页
     */
    private boolean last;

    /**
     * 当前页数据
     */
    private List<T> content;

    /**
     * 创建分页结果
     */
    public static <T> PagedResult<T> of(List<T> content, int pageNumber, int pageSize, long totalElements) {
        int totalPages = pageSize > 0 ? (int) Math.ceil((double) totalElements / pageSize) : 0;
        boolean first = pageNumber == 1;
        boolean last = pageNumber >= totalPages;

        return new PagedResult<>(
                pageNumber,
                pageSize,
                totalElements,
                totalPages,
                first,
                last,
                content
        );
    }
}