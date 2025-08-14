package com.ddd.interfaces.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 数据响应DTO
 * 用于向客户端返回数据
 *
 * @param <T> 数据类型
 * @author anthem37
 * @date 2025/8/14 11:42:15
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class DataResponse<T> extends BaseResponse {

    private static final long serialVersionUID = 1L;

    /**
     * 响应数据
     */
    private T data;

    /**
     * 创建数据响应
     */
    public DataResponse(T data) {
        super();
        this.data = data;
    }

    /**
     * 创建成功数据响应
     */
    public static <T> DataResponse<T> success(T data) {
        return new DataResponse<>(data);
    }

    /**
     * 创建成功数据响应（带消息）
     */
    public static <T> DataResponse<T> success(T data, String message) {
        DataResponse<T> response = new DataResponse<>(data);
        response.setMessage(message);
        return response;
    }

    /**
     * 创建失败数据响应
     */
    public static <T> DataResponse<T> error(String code, String message) {
        DataResponse<T> response = new DataResponse<>();
        response.setSuccess(false);
        response.setCode(code);
        response.setMessage(message);
        return response;
    }
}