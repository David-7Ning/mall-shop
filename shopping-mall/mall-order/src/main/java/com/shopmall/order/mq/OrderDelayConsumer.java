package com.shopmall.order.mq;

import com.rabbitmq.client.Channel;
import com.shopmall.order.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 订单延迟消息消费者
 * 监听死信队列，处理超时未支付的订单
 */
@Slf4j
@Component
public class OrderDelayConsumer {

    @Autowired
    private OrderService orderService;

    /**
     * 监听死信队列，处理超时订单
     *
     * @param orderId 订单 ID
     * @param message 原始消息
     * @param channel 通道
     */
    @RabbitListener(queues = "order.dlx.queue")
    public void handleOrderTimeout(Long orderId, Message message, Channel channel) {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            log.info("收到超时订单消息，orderId: {}", orderId);

            // 调用服务取消订单（内部会检查订单状态，只有待付款的才会被取消）
            orderService.cancelTimeoutOrder(orderId);

            // 手动确认消息
            channel.basicAck(deliveryTag, false);
            log.info("超时订单已取消，orderId: {}", orderId);

        } catch (Exception e) {
            log.error("处理超时订单失败，orderId: {}", orderId, e);
            try {
                // 拒绝消息，不重新入队（避免死循环）
                channel.basicNack(deliveryTag, false, false);
            } catch (IOException ioException) {
                log.error("消息确认失败", ioException);
            }
        }
    }
}