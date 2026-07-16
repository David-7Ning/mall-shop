-- ============================================
-- 商品服务数据库初始化脚本
-- 使用方式：mysql -u root -p < sql/mall_product.sql
-- ============================================

CREATE DATABASE IF NOT EXISTS mall_product DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_general_ci;

USE mall_product;

-- 商品分类表
CREATE TABLE `category` (
                            `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '分类ID',
                            `name` VARCHAR(50) NOT NULL COMMENT '分类名称',
                            `parent_id` BIGINT DEFAULT 0 COMMENT '父分类ID（0表示一级分类）',
                            `sort` INT DEFAULT 0 COMMENT '排序号',
                            PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品分类表';

-- 插入测试数据
INSERT INTO `category` VALUES (1,'手机数码',0,1),(2,'电脑办公',0,2),(3,'家用电器',0,3);

-- 商品表
CREATE TABLE `product` (
                           `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '商品ID',
                           `name` VARCHAR(200) NOT NULL COMMENT '商品名称',
                           `description` TEXT COMMENT '商品描述',
                           `price` DECIMAL(10,2) NOT NULL COMMENT '商品价格',
                           `stock` INT NOT NULL DEFAULT 0 COMMENT '库存数量',
                           `category_id` BIGINT COMMENT '商品分类ID',
                           `image_url` VARCHAR(500) COMMENT '商品图片URL',
                           `status` INT DEFAULT 1 COMMENT '上架状态：0下架 1上架',
                           `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                           `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                           PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

-- 插入测试数据
INSERT INTO `product` VALUES
                          (1,'iPhone 15 Pro','苹果最新旗舰手机，A17 Pro芯片',8999.00,100,1,NULL,1,NOW(),NOW()),
                          (2,'华为Mate 60 Pro','华为旗舰手机，麒麟芯片回归',6999.00,80,1,NULL,1,NOW(),NOW()),
                          (3,'小米14','小米旗舰手机，骁龙8 Gen3',3999.00,200,1,NULL,1,NOW(),NOW()),
                          (4,'MacBook Pro 14','苹果笔记本电脑，M3芯片',14999.00,50,2,NULL,1,NOW(),NOW()),
                          (5,'联想ThinkPad X1','商务办公笔记本',9999.00,60,2,NULL,1,NOW(),NOW()),
                          (6,'戴尔U2723QE','4K专业显示器',3999.00,30,2,NULL,1,NOW(),NOW()),
                          (7,'海尔冰箱 BCD-470','470升大容量冰箱',3299.00,40,3,NULL,1,NOW(),NOW()),
                          (8,'美的空调 KFR-72LW','3匹柜机空调',5999.00,25,3,NULL,1,NOW(),NOW());