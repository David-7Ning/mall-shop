# Shopping Mall 电商微服务系统

基于 **Spring Cloud Alibaba** 的电商微服务项目，实现了完整的电商购物流程，包括用户注册登录、商品浏览、购物车、订单等核心功能。

## 技术栈

| 类别 | 技术 | 说明 |
|------|------|------|
| 基础框架 | Spring Boot 2.7.18 | 微服务基础框架 |
| 微服务 | Spring Cloud 2021.0.8 | 微服务治理 |
| 微服务 | Spring Cloud Alibaba 2021.0.5.0 | 阿里巴巴微服务组件 |
| 注册中心 | Nacos 2.2.3 | 服务注册与发现 |
| 网关 | Spring Cloud Gateway | API统一入口、路由转发 |
| 服务调用 | OpenFeign | 声明式HTTP客户端 |
| 负载均衡 | Spring Cloud LoadBalancer | 客户端负载均衡 |
| ORM | MyBatis-Plus 3.5.7 | 数据库CRUD操作 |
| 数据库 | MySQL 8.0 | 关系型数据库 |
| 缓存 | Redis | 购物车数据存储 |
| 连接池 | Druid | 阿里数据库连接池 |
| 工具库 | Hutool 5.8.25 | Java工具类库 |
| 工具库 | Lombok | 简化Java Bean编写 |

## 系统架构

```
                        ┌─────────────┐
                        │   前端/客户端  │
                        └──────┬──────┘
                               │
                        ┌──────▼──────┐
                        │  Gateway    │  端口: 8080
                        │  API网关     │  路由转发、跨域处理
                        └──────┬──────┘
                               │
          ┌────────────┬───────┼───────┬────────────┐
          │            │       │       │            │
   ┌──────▼──────┐ ┌──▼────┐ ┌▼─────┐ ┌▼──────┐ ┌──▼───────┐
   │  mall-user  │ │product│ │ cart │ │ order │ │ Nacos    │
   │  用户服务    │ │商品服务│ │购物车│ │订单服务│ │ 注册中心  │
   │  端口:8081  │ │:8082  │ │:8083 │ │:8084  │ │ :8848   │
   └──────┬──────┘ └──┬────┘ └──┬───┘ └──┬────┘ └──────────┘
          │           │         │        │
          ▼           ▼         ▼        ▼
   ┌──────────┐ ┌────────┐ ┌──────┐ ┌────────┐
   │ MySQL    │ │ MySQL  │ │Redis │ │ MySQL  │
   │mall_user │ │mall_   │ │      │ │mall_   │
   │          │ │product │ │      │ │order   │
   └──────────┘ └────────┘ └──────┘ └────────┘
```

## 模块说明

| 模块 | 端口 | 说明 | 数据存储 |
|------|------|------|----------|
| mall-common | - | 公共模块：统一响应、异常处理、工具类 | - |
| mall-gateway | 8080 | API网关：路由转发、跨域、负载均衡 | - |
| mall-user | 8081 | 用户服务：注册、登录、用户信息管理 | MySQL |
| mall-product | 8082 | 商品服务：商品列表、详情、库存扣减 | MySQL |
| mall-cart | 8083 | 购物车服务：添加、修改、删除、查询 | Redis |
| mall-order | 8084 | 订单服务：创建订单、查询、取消 | MySQL |

## 数据库设计

### mall_user 数据库

```sql
-- 用户表
CREATE TABLE `user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `username` VARCHAR(50) NOT NULL,
  `password` VARCHAR(100) NOT NULL COMMENT 'BCrypt加密',
  `nickname` VARCHAR(50),
  `phone` VARCHAR(20),
  `email` VARCHAR(100),
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
);
```

### mall_product 数据库

```sql
-- 商品分类表
CREATE TABLE `category` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(50) NOT NULL,
  `parent_id` BIGINT DEFAULT 0,
  `sort` INT DEFAULT 0,
  PRIMARY KEY (`id`)
);

-- 商品表
CREATE TABLE `product` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(200) NOT NULL,
  `description` TEXT,
  `price` DECIMAL(10,2) NOT NULL,
  `stock` INT NOT NULL DEFAULT 0,
  `category_id` BIGINT,
  `image_url` VARCHAR(500),
  `status` INT DEFAULT 1 COMMENT '0下架 1上架',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
);
```

### mall_order 数据库

```sql
-- 订单表
CREATE TABLE `order` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `order_no` VARCHAR(64) NOT NULL COMMENT '雪花算法生成',
  `user_id` BIGINT NOT NULL,
  `total_amount` DECIMAL(10,2) NOT NULL,
  `status` INT DEFAULT 0 COMMENT '0待付款 1已付款 2已发货 3已完成 4已取消',
  `address` VARCHAR(500),
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
);

-- 订单明细表
CREATE TABLE `order_item` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `order_id` BIGINT NOT NULL,
  `product_id` BIGINT NOT NULL,
  `product_name` VARCHAR(200),
  `price` DECIMAL(10,2) NOT NULL,
  `quantity` INT NOT NULL,
  `subtotal` DECIMAL(10,2) NOT NULL,
  PRIMARY KEY (`id`)
);
```

## 接口文档

### 用户服务 (mall-user)

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /user/register?username=xx&password=xx | 用户注册 |
| POST | /user/login?username=xx&password=xx | 用户登录 |
| GET | /user/{id} | 查询用户信息 |

### 商品服务 (mall-product)

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /product/list | 上架商品列表 |
| GET | /product/list/{categoryId} | 按分类查询商品 |
| GET | /product/{id} | 商品详情 |
| PUT | /product/reduceStock?productId=xx&quantity=xx | 扣减库存 |

### 购物车服务 (mall-cart)

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /cart/add?userId=xx | 添加商品到购物车 |
| PUT | /cart/update?userId=xx&productId=xx&quantity=xx | 修改商品数量 |
| DELETE | /cart/remove?userId=xx&productId=xx | 删除购物车商品 |
| GET | /cart/list?userId=xx | 查询购物车 |
| DELETE | /cart/clear?userId=xx | 清空购物车 |

### 订单服务 (mall-order)

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /order/create | 创建订单 |
| GET | /order/list?userId=xx | 用户订单列表 |
| GET | /order/{id} | 订单详情 |
| PUT | /order/cancel/{id} | 取消订单 |

### 网关路由规则

所有请求通过网关（端口8080）统一入口，路由规则：

| 网关路径 | 转发目标 | 说明 |
|----------|----------|------|
| /api/user/** | mall-user | 去掉 /api 前缀后转发 |
| /api/product/** | mall-product | 去掉 /api 前缀后转发 |
| /api/cart/** | mall-cart | 去掉 /api 前缀后转发 |
| /api/order/** | mall-order | 去掉 /api 前缀后转发 |

示例：`GET http://localhost:8080/api/product/list` → 转发到 `mall-product` 服务的 `/product/list`

## 快速启动

### 环境要求

- JDK 11+
- Maven 3.6+
- MySQL 8.0+
- Redis 6.0+
- Nacos 2.2.x

### 1. 启动中间件

```bash
# 启动 Nacos（单机模式）
cd ~/nacos/bin
sh startup.sh -m standalone

# 启动 Redis（Docker方式）
docker run -d --name mall-redis -p 6379:6379 redis

# 初始化数据库（项目内置SQL脚本，一键建库建表）
mysql -u root -p < sql/mall_user.sql
mysql -u root -p < sql/mall_product.sql
mysql -u root -p < sql/mall_order.sql
# 或者一条命令：cat sql/*.sql | mysql -u root -p
```

### 2. 修改配置

修改各服务 `application.yml` 中的数据库连接信息：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/mall_user?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: root
    password: 你的密码
```

### 3. 编译项目

```bash
cd shopping-mall
mvn clean install -DskipTests
```

### 4. 按顺序启动服务

1. **mall-user** → UserApplication
2. **mall-product** → ProductApplication
3. **mall-cart** → CartApplication
4. **mall-order** → OrderApplication
5. **mall-gateway** → GatewayApplication

### 5. 验证

```bash
# 通过网关访问商品列表
curl http://localhost:8080/api/product/list

# 用户注册
curl -X POST "http://localhost:8080/api/user/register?username=test&password=123456"

# 用户登录
curl -X POST "http://localhost:8080/api/user/login?username=test&password=123456"
```

## 订单状态流转

```
┌──────────┐   付款   ┌──────────┐   发货   ┌──────────┐   收货   ┌──────────┐
│  待付款   │ ──────→ │  已付款   │ ──────→ │  已发货   │ ──────→ │  已完成   │
│  status=0 │         │  status=1 │         │  status=2 │         │  status=3 │
└─────┬────┘         └──────────┘         └──────────┘         └──────────┘
      │
      │ 取消
      ▼
┌──────────┐
│  已取消   │
│  status=4 │
└──────────┘
```

## 项目目录结构

```
shopping-mall/
├── mall-common/                          # 公共模块
│   └── src/main/java/com/shopmall/common/
│       ├── result/                       # 统一响应封装
│       │   ├── ResultCode.java           # 状态码枚举
│       │   └── Result.java               # 统一响应类
│       └── exception/                    # 异常处理
│           ├── BusinessException.java     # 业务异常
│           └── GlobalExceptionHandler.java # 全局异常处理器
├── mall-gateway/                         # 网关服务
│   └── src/main/
│       ├── java/com/shopmall/gateway/
│       │   └── GatewayApplication.java
│       └── resources/application.yml
├── mall-user/                            # 用户服务
│   └── src/main/java/com/shopmall/user/
│       ├── entity/User.java
│       ├── mapper/UserMapper.java
│       ├── service/UserService.java
│       ├── service/impl/UserServiceImpl.java
│       └── controller/UserController.java
├── mall-product/                         # 商品服务
│   └── src/main/java/com/shopmall/product/
│       ├── entity/{Product,Category}.java
│       ├── mapper/{ProductMapper,CategoryMapper}.java
│       ├── service/ProductService.java
│       ├── service/impl/ProductServiceImpl.java
│       └── controller/ProductController.java
├── mall-cart/                            # 购物车服务
│   └── src/main/java/com/shopmall/cart/
│       ├── dto/CartItem.java
│       ├── config/RedisConfig.java
│       ├── service/CartService.java
│       ├── service/impl/CartServiceImpl.java
│       └── controller/CartController.java
├── mall-order/                           # 订单服务
│   └── src/main/java/com/shopmall/order/
│       ├── entity/{Order,OrderItem}.java
│       ├── mapper/{OrderMapper,OrderItemMapper}.java
│       ├── dto/CreateOrderRequest.java
│       ├── feign/ProductFeignClient.java
│       ├── feign/fallback/ProductFeignFallback.java
│       ├── service/OrderService.java
│       ├── service/impl/OrderServiceImpl.java
│       └── controller/OrderController.java
└── pom.xml                               # 父工程POM
```

## 学习要点

本项目适合 Java 入门学习者了解微服务架构，核心知识点包括：

1. **微服务拆分**：按业务领域拆分为独立服务，各自独立开发、部署
2. **服务注册与发现**：Nacos 作为注册中心，服务自动注册和发现
3. **API 网关**：统一入口，路由转发、跨域处理、负载均衡
4. **远程服务调用**：OpenFeign 声明式调用，像调本地方法一样调远程接口
5. **统一响应格式**：Result 封装，前端处理更简单
6. **全局异常处理**：@RestControllerAdvice 统一捕获异常
7. **Redis 缓存**：购物车数据存储在 Redis，读写性能高
8. **分布式事务**：@Transactional 保证订单创建和库存扣减的一致性
9. **服务降级**：Feign Fallback，当远程服务不可用时返回友好提示
10. **密码加密**：BCrypt 单向加密，保护用户密码安全

## 新电脑部署指南

将项目部署到另一台电脑，按以下步骤操作：

### 第一步：安装基础环境

| 软件 | 版本要求 | 下载地址 |
|------|----------|----------|
| JDK | 11+ | https://adoptium.net/ |
| Maven | 3.6+ | https://maven.apache.org/download.cgi |
| MySQL | 8.0+ | https://dev.mysql.com/downloads/ |
| IntelliJ IDEA | 任意版本 | https://www.jetbrains.com/idea/download/ |

安装后验证：

```bash
java -version      # 确认Java版本
mvn -version       # 确认Maven版本
mysql --version    # 确认MySQL版本
```

### 第二步：安装中间件

**Nacos：**

1. 下载 https://github.com/alibaba/nacos/releases （选 2.2.3 版本的 tar.gz）
2. 解压：`tar -xzvf nacos-server-2.2.3.tar.gz`
3. 启动：`cd nacos/bin && sh startup.sh -m standalone`
4. 访问 http://localhost:8848/nacos 确认启动成功

**Redis：**

方式一（Docker）：
```bash
docker run -d --name mall-redis -p 6379:6379 redis
```

方式二（Homebrew）：
```bash
brew install redis
redis-server
```

### 第三步：创建数据库

项目已内置SQL初始化脚本，位于 `sql/` 目录下：

```
sql/
├── mall_user.sql      # 用户服务数据库（建库+建表）
├── mall_product.sql   # 商品服务数据库（建库+建表+测试数据）
└── mall_order.sql     # 订单服务数据库（建库+建表）
```

在新电脑上执行以下命令即可一键初始化所有数据库：

```bash
cd shopping-mall
mysql -u root -p < sql/mall_user.sql
mysql -u root -p < sql/mall_product.sql
mysql -u root -p < sql/mall_order.sql
```

或者一条命令搞定：

```bash
cat sql/*.sql | mysql -u root -p
```

> 💡 脚本会自动创建数据库和表，商品服务还预置了3个分类和8个测试商品数据，无需手动导入导出。

### 第四步：复制项目代码

将整个 `shopping-mall` 文件夹复制到新电脑即可。

### 第五步：修改配置

如果新电脑的MySQL密码不同，修改以下文件中的 `password`：

- `mall-user/src/main/resources/application.yml`
- `mall-product/src/main/resources/application.yml`
- `mall-order/src/main/resources/application.yml`

```yaml
spring:
  datasource:
    password: 你的新密码
```

### 第六步：IDEA 配置

1. 打开 IDEA → **File → Open** → 选择 `shopping-mall` 文件夹
2. 等待 Maven 下载依赖（首次可能需要几分钟）
3. 安装 **Lombok 插件**：Settings → Plugins → 搜索 Lombok → Install → Restart
4. 开启 **Annotation Processing**：Settings → Build, Execution, Deployment → Compiler → Annotation Processors → 勾选 Enable annotation processing

### 第七步：启动服务

按以下顺序在IDEA中启动：

1. **mall-user** → UserApplication
2. **mall-product** → ProductApplication
3. **mall-cart** → CartApplication
4. **mall-order** → OrderApplication
5. **mall-gateway** → GatewayApplication

### 第八步：验证

```bash
# 通过网关访问商品列表
curl http://localhost:8080/api/product/list

# 用户注册
curl -X POST "http://localhost:8080/api/user/register?username=test&password=123456"

# 用户登录
curl -X POST "http://localhost:8080/api/user/login?username=test&password=123456"
```

### 部署清单

```
新电脑需要做的：
✅ 安装 JDK 11+
✅ 安装 Maven
✅ 安装 MySQL 8.0 + 创建3个数据库
✅ 安装 Nacos 并启动
✅ 安装 Redis 并启动
✅ 复制项目代码
✅ 修改数据库密码（如果不同）
✅ IDEA 安装 Lombok 插件
✅ 开启 Annotation Processing
✅ 启动5个微服务
✅ 验证接口是否正常
```

## 线上服务器部署指南

将项目部署到云服务器（阿里云/腾讯云/华为云等），实现公网访问。

### 服务器配置要求

| 配置项 | 最低要求 | 推荐配置 |
|--------|----------|----------|
| CPU | 2核 | 4核 |
| 内存 | 4GB | 8GB |
| 硬盘 | 40GB | 100GB |
| 系统 | CentOS 7.9 / Ubuntu 20.04 | 同左 |

### 第一步：安装基础环境

SSH 连接到服务器后执行：

```bash
# 安装 Docker（所有中间件都用Docker运行）
curl -fsSL https://get.docker.com | sh
systemctl start docker
systemctl enable docker

# 安装 Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
chmod +x /usr/local/bin/docker-compose

# 安装 JDK 11
yum install -y java-11-openjdk-devel   # CentOS
# 或 apt install -y openjdk-11-jdk     # Ubuntu

# 验证安装
java -version
docker --version
docker-compose --version
```

### 第二步：Docker Compose 编排中间件

在服务器上创建目录并编写配置：

```bash
mkdir -p /opt/mall-docker
cd /opt/mall-docker
```

创建 `docker-compose.yml`：

```yaml
version: '3.8'

services:
  # MySQL 数据库
  mysql:
    image: mysql:8.0
    container_name: mall-mysql
    environment:
      MYSQL_ROOT_PASSWORD: 你的强密码    # 生产环境必须设置强密码
      TZ: Asia/Shanghai
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql       # 数据持久化
    command: --character-set-server=utf8mb4 --collation-server=utf8mb4_general_ci
    restart: always

  # Redis 缓存
  redis:
    image: redis:7-alpine
    container_name: mall-redis
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data
    command: redis-server --appendonly yes  # 开启AOF持久化
    restart: always

  # Nacos 注册中心
  nacos:
    image: nacos/nacos-server:v2.2.3
    container_name: mall-nacos
    environment:
      MODE: standalone
      SPRING_DATASOURCE_PLATFORM: mysql
      MYSQL_SERVICE_HOST: mysql
      MYSQL_SERVICE_PORT: 3306
      MYSQL_SERVICE_DB_NAME: nacos_config
      MYSQL_SERVICE_USER: root
      MYSQL_SERVICE_PASSWORD: 你的强密码
      JVM_XMS: 256m
      JVM_XMX: 512m
    ports:
      - "8848:8848"
      - "9848:9848"                     # gRPC端口（Nacos 2.x需要）
    depends_on:
      - mysql
    restart: always

volumes:
  mysql_data:
  redis_data:
```

启动中间件：

```bash
docker-compose up -d

# 查看状态
docker-compose ps

# 查看日志
docker-compose logs -f nacos
```

### 第三步：初始化数据库

将项目的 `sql/` 目录上传到服务器，然后执行：

```bash
# 方式一：直接执行
mysql -h 127.0.0.1 -u root -p < sql/mall_user.sql
mysql -h 127.0.0.1 -u root -p < sql/mall_product.sql
mysql -h 127.0.0.1 -u root -p < sql/mall_order.sql

# 方式二：进入MySQL容器执行
docker exec -i mall-mysql mysql -u root -p你的密码 < sql/mall_user.sql
docker exec -i mall-mysql mysql -u root -p你的密码 < sql/mall_product.sql
docker exec -i mall-mysql mysql -u root -p你的密码 < sql/mall_order.sql
```

### 第四步：打包应用

在本地开发机上执行：

```bash
cd shopping-mall
mvn clean package -DskipTests
```

打包后生成的 jar 文件：

```
mall-user/target/mall-user-1.0-SNAPSHOT.jar
mall-product/target/mall-product-1.0-SNAPSHOT.jar
mall-cart/target/mall-cart-1.0-SNAPSHOT.jar
mall-order/target/mall-order-1.0-SNAPSHOT.jar
mall-gateway/target/mall-gateway-1.0-SNAPSHOT.jar
```

### 第五步：上传并修改配置

**1. 在服务器创建目录：**

```bash
mkdir -p /opt/mall-app/{user,product,cart,order,gateway}
mkdir -p /opt/mall-app/logs
```

**2. 将 jar 包上传到对应目录**（使用 scp 或 FTP 工具）：

```bash
# 在本地执行
scp mall-user/target/mall-user-1.0-SNAPSHOT.jar root@服务器IP:/opt/mall-app/user/
scp mall-product/target/mall-product-1.0-SNAPSHOT.jar root@服务器IP:/opt/mall-app/product/
scp mall-cart/target/mall-cart-1.0-SNAPSHOT.jar root@服务器IP:/opt/mall-app/cart/
scp mall-order/target/mall-order-1.0-SNAPSHOT.jar root@服务器IP:/opt/mall-app/order/
scp mall-gateway/target/mall-gateway-1.0-SNAPSHOT.jar root@服务器IP:/opt/mall-app/gateway/
```

**3. 修改生产环境配置**（数据库密码、Nacos地址等）

### 第六步：启动服务

创建启动脚本 `/opt/mall-app/start.sh`：

```bash
#!/bin/bash

APP_DIR=/opt/mall-app
LOG_DIR=$APP_DIR/logs

echo "=== 启动电商微服务 ==="

nohup java -jar $APP_DIR/user/mall-user-1.0-SNAPSHOT.jar > $LOG_DIR/user.log 2>&1 &
echo "用户服务启动中..."
sleep 5

nohup java -jar $APP_DIR/product/mall-product-1.0-SNAPSHOT.jar > $LOG_DIR/product.log 2>&1 &
echo "商品服务启动中..."
sleep 5

nohup java -jar $APP_DIR/cart/mall-cart-1.0-SNAPSHOT.jar > $LOG_DIR/cart.log 2>&1 &
echo "购物车服务启动中..."
sleep 5

nohup java -jar $APP_DIR/order/mall-order-1.0-SNAPSHOT.jar > $LOG_DIR/order.log 2>&1 &
echo "订单服务启动中..."
sleep 5

nohup java -jar $APP_DIR/gateway/mall-gateway-1.0-SNAPSHOT.jar > $LOG_DIR/gateway.log 2>&1 &
echo "网关服务启动中..."

echo ""
echo "=== 所有服务已启动 ==="
echo "网关地址: http://服务器公网IP:8080"
echo "查看日志: tail -f /opt/mall-app/logs/*.log"
```

执行启动：

```bash
chmod +x /opt/mall-app/start.sh
/opt/mall-app/start.sh
```

### 第七步：配置 Nginx 反向代理（推荐）

安装 Nginx 并配置反向代理，让用户通过 80 端口访问：

```bash
# 安装 Nginx
yum install -y nginx          # CentOS
# 或 apt install -y nginx     # Ubuntu
```

创建配置文件 `/etc/nginx/conf.d/mall.conf`：

```nginx
server {
    listen 80;
    server_name your-domain.com;    # 域名或公网IP

    location / {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }
}
```

重启 Nginx：

```bash
systemctl restart nginx
systemctl enable nginx
```

### 第八步：开放安全组端口

在云服务器控制台的 **安全组** 中开放以下端口：

| 端口 | 用途 | 是否必须 |
|------|------|----------|
| 80 | HTTP（Nginx） | ✅ 必须 |
| 443 | HTTPS（SSL） | 按需 |
| 8080 | 网关（不走Nginx时） | 按需 |

> ⚠️ **安全警告：** 3306、6379、8848 等中间件端口 **不要对公网开放**，只允许内网（127.0.0.1）访问！

### 第九步：验证部署

```bash
# 服务器本地测试
curl http://127.0.0.1/api/product/list

# 外网测试
curl http://服务器公网IP/api/product/list

# 或通过域名测试
curl http://your-domain.com/api/product/list
```

### 生产环境优化建议

| 优化项 | 说明 |
|--------|------|
| 🔒 安全 | MySQL/Redis/Nacos 不暴露公网，只绑定 127.0.0.1 |
| 📝 日志 | 配置日志按天滚动，避免日志文件过大 |
| 🔄 进程守护 | 用 systemd 管理 Java 进程，崩溃自动重启 |
| 💾 备份 | 定时备份 MySQL 数据库（crontab + mysqldump） |
| ⚡ 性能 | 多机部署 + Nginx 负载均衡 |
| 🔐 HTTPS | 申请 SSL 证书（Let's Encrypt 免费证书），启用 HTTPS |
| 🛡️ 防火墙 | 只开放必要端口，关闭其他所有端口 |

### 线上部署清单

```
线上服务器需要做的：
✅ 购买云服务器（2核4G+）
✅ 安装 Docker + Docker Compose
✅ 安装 JDK 11
✅ docker-compose 启动 MySQL + Redis + Nacos
✅ 初始化数据库（sql/ 目录下的脚本）
✅ 本地 mvn package 打包
✅ 上传 jar 包到服务器
✅ 修改生产环境配置（密码、地址）
✅ 执行 start.sh 启动服务
✅ 配置 Nginx 反向代理
✅ 开放安全组端口（80/443）
✅ 验证接口是否正常
```