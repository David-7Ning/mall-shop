package com.shopmall.common.exception;

import com.shopmall.common.result.ResultCode;
import lombok.Getter;

/**
 * 自定义业务异常
 * 当业务逻辑不满足条件时抛出，由全局异常处理器统一捕获返回给前端
 *
 * 使用示例：
 *   throw new BusinessException("商品库存不足");
 *   throw new BusinessException(ResultCode.STOCK_NOT_ENOUGH);
 */
@Getter
public class BusinessException extends RuntimeException {

    /** 错误状态码 */
    private final int code;

    /** 默认构造：使用 BUSINESS_ERROR 状态码 + 自定义信息 */
    public BusinessException(String message) {
        super(message);
        this.code = ResultCode.BUSINESS_ERROR.getCode();
    }

    /** 使用枚举状态码构造 */
    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
    }

    /** 自定义状态码和信息 */
    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }
}