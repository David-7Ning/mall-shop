package com.shopmall.common.result;

import lombok.Getter;

/**
 * 统一响应状态码枚举
 * 每个状态码由 code（数字编码）和 message（描述信息）组成
 */
@Getter
public enum ResultCode {

    // 成功
    SUCCESS(200, "操作成功"),

    // 客户端错误 4xx
    PARAM_ERROR(400, "参数错误"),
    UNAUTHORIZED(401, "未登录或token已过期"),
    FORBIDDEN(403, "没有相关权限"),
    NOT_FOUND(404, "资源不存在"),

    // 业务错误 5xx
    BUSINESS_ERROR(500, "业务处理失败"),
    USER_EXISTS(5001, "用户名已存在"),
    USER_NOT_FOUND(5002, "用户不存在"),
    PASSWORD_ERROR(5003, "密码错误"),
    PRODUCT_NOT_FOUND(5101, "商品不存在"),
    STOCK_NOT_ENOUGH(5102, "库存不足"),
    CART_EMPTY(5201, "购物车为空"),
    ORDER_NOT_FOUND(5301, "订单不存在");

    /** 数字编码 */
    private final int code;

    /** 描述信息 */
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}