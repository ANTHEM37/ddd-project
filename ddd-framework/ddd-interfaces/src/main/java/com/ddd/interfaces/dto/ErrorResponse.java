package com.ddd.interfaces.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 错误响应DTO
 * 用于向客户端返回统一格式的错误信息
 *
 * @author anthem37
 * @date 2025/8/13 16:24:29
 */
@Data
@NoArgsConstructor
public class ErrorResponse {

    /**
     * 错误代码
     */
    private String code;

    /**
     * 错误消息
     */
    private String message;

    /**
     * 错误详情
     */
    private Object details;

    /**
     * 时间戳
     */
    private long timestamp = System.currentTimeMillis();

    /**
     * 构造函数 - 带错误代码和消息
     */
    public ErrorResponse(String code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * 构造函数 - 带错误代码、消息和详情
     */
    public ErrorResponse(String code, String message, Object details) {
        this.code = code;
        this.message = message;
        this.details = details;
    }

    /**
     * 构造函数 - 带所有参数
     */
    public ErrorResponse(String code, String message, Object details, long timestamp) {
        this.code = code;
        this.message = message;
        this.details = details;
        this.timestamp = timestamp;
    }
}
