# 商城管理系统 (MallDemo)

## 项目简介
基于微服务架构的商城管理系统，包含后台管理端和用户端。

## 技术栈

### 前端 (mall-admin-web)
- Vue 3 + Vite
- Element Plus UI 组件库
- Axios HTTP 客户端
- Vue Router 路由管理

### 后端 (shopping-mall)
- Spring Boot 微服务框架
- Spring Cloud Alibaba (Nacos, Gateway)
- MyBatis-Plus ORM 框架
- MySQL 数据库
- MinIO 对象存储

## 微服务模块
- `mall-admin`: 管理服务（认证、权限）
- `mall-product`: 商品服务
- `mall-order`: 订单服务  
- `mall-user`: 用户服务
- `mall-file`: 文件服务（MinIO）

## 快速开始

### 环境要求
- JDK 17+
- Node.js 16+
- MySQL 8.0+
- Nacos 2.x
- MinIO Server

### 启动步骤
1. 启动基础设施：MySQL、Nacos、MinIO
2. 导入数据库脚本到 MySQL
3. 启动后端微服务（按顺序）：
   - ProductApplication
   - OrderApplication
   - UserApplication
   - FileApplication
   - AdminApplication
4. 启动前端：
   ```bash
   cd mall-admin-web
   npm install
   npm run dev
   


## 功能特性
 ✅ 商品管理（分类、图片上传）
 ✅ 订单管理（状态跟踪）
 ✅ 用户管理（状态控制）
 ✅ 数据统计仪表盘
 ✅ 文件上传（MinIO 集成）
