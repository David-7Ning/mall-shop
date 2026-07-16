package com.shopmall.gateway.filter;

import com.shopmall.common.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

/**
 * JWT 认证过滤器
 * 拦截请求，验证 Token 是否有效
 */
@Slf4j
@Component
public class JwtAuthFilter implements GlobalFilter, Ordered {

    /** 白名单：不需要验证 Token 的接口 */
    private static final List<String> WHITE_LIST = Arrays.asList(
            "/api/user/login",      // 登录接口
            "/api/user/register",   // 注册接口
            "/api/admin/login",     // 管理员登录接口
            "/api/product/list",    // 商品列表（浏览）
            "/api/product/",        // 商品详情（浏览）- 匹配 /api/product/{id}
            "/api/product/search",  // 商品搜索（浏览）
            "/api/product/category",// 分类商品（浏览）
            "/api/category/list",   // 分类列表（浏览）
            "/api/banner/list",     // 轮播图列表（浏览）
            "/api/file/url",        // 文件URL获取（图片浏览）
            "/api/file/upload"      // 文件上传（需要登录，但先放行由下游服务处理）
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // 1. 白名单接口，直接放行
        if (isWhiteList(path)) {
            log.info("白名单接口，直接放行: {}", path);
            return chain.filter(exchange);
        }

        // 2. 获取 Token
        String token = request.getHeaders().getFirst("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            log.warn("请求未携带 Token: {}", path);
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        // 3. 验证 Token
        token = token.substring(7);  // 去掉 "Bearer " 前缀
        if (!JwtUtil.validateToken(token)) {
            log.warn("Token 无效或已过期: {}", path);
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        // 4. Token 有效，将用户信息放入请求头，传递给下游服务
        Long userId = JwtUtil.getUserIdFromToken(token);
        String username = JwtUtil.getUsernameFromToken(token);
        
        ServerHttpRequest modifiedRequest = request.mutate()
                .header("X-User-Id", String.valueOf(userId))
                .header("X-Username", username)
                .build();

        log.info("Token 验证成功，用户ID: {}, 用户名: {}", userId, username);
        return chain.filter(exchange.mutate().request(modifiedRequest).build());
    }

    /**
     * 判断是否为白名单接口
     */
    private boolean isWhiteList(String path) {
        return WHITE_LIST.stream().anyMatch(path::startsWith);
    }

    @Override
    public int getOrder() {
        return 0;  // 过滤器优先级，数字越小优先级越高
    }
}