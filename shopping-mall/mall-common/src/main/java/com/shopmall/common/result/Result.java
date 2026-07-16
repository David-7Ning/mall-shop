package com.shopmall.common.result;

import lombok.Data;

import java.io.Serializable;

/**
 * 统一响应结果封装类
 * 所有接口返回值都使用此格式，保证前端接收到的数据结构一致
 *
 * 格式示例：
 * {
 *   "code": 200,
 *   "message": "操作成功",
 *   "data": { ... }
 * }
 */
@Data
public class Result<T> implements Serializable {

    /** 状态码 */
    private int code;

    /** 提示信息 */
    private String message;

    /** 返回数据 */
    private T data;

    // ==================== 成功响应 ====================

    /** 成功（无数据） */
    public static <T> Result<T> success() {
        return build(ResultCode.SUCCESS, null);
    }

    /** 成功（带数据） */
    public static <T> Result<T> success(T data) {
        return build(ResultCode.SUCCESS, data);
    }

    /** 成功（自定义提示信息） */
    public static <T> Result<T> success(String message, T data) {
        Result<T> result = new Result<>();
        result.setCode(ResultCode.SUCCESS.getCode());
        result.setMessage(message);
        result.setData(data);
        return result;
    }

    // ==================== 失败响应 ====================

    /** 失败（使用枚举中的状态码） */
    public static <T> Result<T> fail(ResultCode resultCode) {
        return build(resultCode, null);
    }

    /** 失败（自定义提示信息） */
    public static <T> Result<T> fail(String message) {
        Result<T> result = new Result<>();
        result.setCode(ResultCode.BUSINESS_ERROR.getCode());
        result.setMessage(message);
        result.setData(null);
        return result;
    }

    /** 失败（自定义状态码和提示信息） */
    public static <T> Result<T> fail(int code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        result.setData(null);
        return result;
    }

    // ==================== 私有构建方法 ====================

    private static <T> Result<T> build(ResultCode resultCode, T data) {
        Result<T> result = new Result<>();
        result.setCode(resultCode.getCode());
        result.setMessage(resultCode.getMessage());
        result.setData(data);
        return result;
    }
}