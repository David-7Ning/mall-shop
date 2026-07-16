package com.shopmall.order.feign;

import com.shopmall.common.result.Result;
import com.shopmall.order.feign.fallback.ProductFeignFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 商品服务Feign客户端
 * 通过Feign远程调用商品服务的扣减库存接口
 * fallback：当商品服务不可用时的降级处理
 */
@FeignClient(name = "mall-product", fallback = ProductFeignFallback.class)
public interface ProductFeignClient {

    /** 调用商品服务的扣减库存接口 */
    @PutMapping("/product/{id}/stock")
    Result<Void> reduceStock(@PathVariable("id") Long id,
                             @RequestParam("quantity") Integer quantity);
}