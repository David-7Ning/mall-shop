package com.shopmall.order.mq;

import com.shopmall.order.config.RabbitMQConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 订单延迟消息生产者
 * 下单后发送消息到延迟队列，30 分钟后消息过期转入死信队列
 */
@Slf4j
@Component
public class OrderDelayProducer {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 发送订单延迟消息
     *
     * @param orderId 订单 ID
     */
    public void sendOrderDelayMessage(Long orderId) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.ORDER_DELAY_EXCHANGE,
                RabbitMQConfig.ORDER_DELAY_ROUTING_KEY,
                orderId
        );
        log.info("发送订单延迟消息，orderId: {}", orderId);
    }
}