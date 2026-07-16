package com.shopmall.order.feign.fallback;

import com.shopmall.common.result.Result;
import com.shopmall.common.result.ResultCode;
import com.shopmall.order.feign.ProductFeignClient;
import org.springframework.stereotype.Component;

/**
 * 商品服务降级处理
 * 当商品服务不可用（宕机、超时）时，执行降级逻辑，返回友好提示
 */
@Component
public class ProductFeignFallback implements ProductFeignClient {

    @Override
    public Result<Void> reduceStock(Long id, Integer quantity) {
        return Result.fail("商品服务暂时不可用，请稍后重试");
    }
}