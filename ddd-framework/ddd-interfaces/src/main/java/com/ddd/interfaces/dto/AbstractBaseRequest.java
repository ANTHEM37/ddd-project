package com.ddd.interfaces.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 请求DTO基类
 * 所有请求DTO应继承此类
 *
 * @author anthem37
 * @date 2025/8/14 14:28:36
 */
@Data
public abstract class AbstractBaseRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 请求ID，用于跟踪
     */
    private String requestId;

    /**
     * 请求时间戳
     */
    private Long timestamp = System.currentTimeMillis();
}