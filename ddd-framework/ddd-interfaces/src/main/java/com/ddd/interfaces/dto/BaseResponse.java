package com.ddd.interfaces.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 响应DTO基类
 * 所有响应DTO应继承此类
 *
 * @author anthem37
 * @date 2025/8/14 10:58:32
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BaseResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 是否成功
     */
    private boolean success = true;

    /**
     * 响应代码
     */
    private String code = "200";

    /**
     * 响应消息
     */
    private String message = "操作成功";

    /**
     * 响应时间戳
     */
    private Long timestamp = System.currentTimeMillis();

    /**
     * 创建成功响应
     */
    public static BaseResponse success() {
        return new BaseResponse();
    }

    /**
     * 创建成功响应（带消息）
     */
    public static BaseResponse success(String message) {
        BaseResponse response = new BaseResponse();
        response.setMessage(message);
        return response;
    }

    /**
     * 创建失败响应
     */
    public static BaseResponse failure(String code, String message) {
        BaseResponse response = new BaseResponse();
        response.setSuccess(false);
        response.setCode(code);
        response.setMessage(message);
        return response;
    }
}