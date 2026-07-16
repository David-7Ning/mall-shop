package com.shopmall.order.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * RabbitMQ 配置类
 * 使用死信队列（DLX）实现订单超时自动取消
 */
@Configuration
public class RabbitMQConfig {

    /** 延迟交换机 */
    public static final String ORDER_DELAY_EXCHANGE = "order.delay.exchange";
    /** 延迟队列 */
    public static final String ORDER_DELAY_QUEUE = "order.delay.queue";
    /** 延迟队列路由键 */
    public static final String ORDER_DELAY_ROUTING_KEY = "order.delay";

    /** 死信交换机 */
    public static final String ORDER_DLX_EXCHANGE = "order.dlx.exchange";
    /** 死信队列（消费者从这里拿消息） */
    public static final String ORDER_DLX_QUEUE = "order.dlx.queue";
    /** 死信队列路由键 */
    public static final String ORDER_DLX_ROUTING_KEY = "order.dlx";

    /** 订单超时时间：30 分钟（毫秒） */
    public static final long ORDER_TIMEOUT_MS = 30 * 60 * 1000;


    /**
     * 死信交换机
     */
    @Bean
    public DirectExchange dlxExchange() {
        return new DirectExchange(ORDER_DLX_EXCHANGE, true, false);
    }

    /**
     * 死信队列
     */
    @Bean
    public Queue dlxQueue() {
        return new Queue(ORDER_DLX_QUEUE, true);
    }

    /**
     * 死信队列绑定死信交换机
     */
    @Bean
    public Binding dlxBinding() {
        return BindingBuilder.bind(dlxQueue())
                .to(dlxExchange())
                .with(ORDER_DLX_ROUTING_KEY);
    }

    /**
     * 延迟交换机
     */
    @Bean
    public DirectExchange delayExchange() {
        return new DirectExchange(ORDER_DELAY_EXCHANGE, true, false);
    }

    /**
     * 延迟队列（配置死信交换机，消息过期后转发到死信队列）
     */
    @Bean
    public Queue delayQueue() {
        Map<String, Object> args = new HashMap<>();
        // 设置死信交换机
        args.put("x-dead-letter-exchange", ORDER_DLX_EXCHANGE);
        // 设置死信路由键
        args.put("x-dead-letter-routing-key", ORDER_DLX_ROUTING_KEY);
        // 设置消息过期时间（30 分钟）
        args.put("x-message-ttl", ORDER_TIMEOUT_MS);
        return new Queue(ORDER_DELAY_QUEUE, true, false, false, args);
    }

    /**
     * 延迟队列绑定延迟交换机
     */
    @Bean
    public Binding delayBinding() {
        return BindingBuilder.bind(delayQueue())
                .to(delayExchange())
                .with(ORDER_DELAY_ROUTING_KEY);
    }
}