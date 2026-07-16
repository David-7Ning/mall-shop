-- ============================================
-- 用户服务数据库初始化脚本
-- 使用方式：mysql -u root -p < sql/mall_user.sql
-- ============================================

CREATE DATABASE IF NOT EXISTS mall_user DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_general_ci;

USE mall_user;

CREATE TABLE `user` (
                        `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
                        `username` VARCHAR(50) NOT NULL COMMENT '用户名',
                        `password` VARCHAR(100) NOT NULL COMMENT '密码（BCrypt加密）',
                        `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
                        `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
                        `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
                        `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                        `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                        PRIMARY KEY (`id`),
                        UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';