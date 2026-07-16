-- ============================================
-- 订单服务数据库初始化脚本
-- 使用方式：mysql -u root -p < sql/mall_order.sql
-- ============================================

CREATE DATABASE IF NOT EXISTS mall_order DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_general_ci;

USE mall_order;

-- 订单表（order是MySQL关键字，需要用反引号）
CREATE TABLE `order` (
                         `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '订单ID',
                         `order_no` VARCHAR(64) NOT NULL COMMENT '订单号（雪花算法生成）',
                         `user_id` BIGINT NOT NULL COMMENT '用户ID',
                         `total_amount` DECIMAL(10,2) NOT NULL COMMENT '订单总金额',
                         `status` INT DEFAULT 0 COMMENT '订单状态：0待付款 1已付款 2已发货 3已完成 4已取消',
                         `address` VARCHAR(500) COMMENT '收货地址',
                         `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                         `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                         PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- 订单明细表
CREATE TABLE `order_item` (
                              `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '订单明细ID',
                              `order_id` BIGINT NOT NULL COMMENT '订单ID',
                              `product_id` BIGINT NOT NULL COMMENT '商品ID',
                              `product_name` VARCHAR(200) COMMENT '商品名称（冗余存储）',
                              `price` DECIMAL(10,2) NOT NULL COMMENT '商品单价（下单时价格）',
                              `quantity` INT NOT NULL COMMENT '购买数量',
                              `subtotal` DECIMAL(10,2) NOT NULL COMMENT '小计金额 = price × quantity',
                              PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单明细表';