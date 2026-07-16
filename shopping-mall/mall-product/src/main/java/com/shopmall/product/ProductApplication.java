package com.shopmall.product;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.shopmall.product.mapper")
public class ProductApplication {
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(ProductApplication.class, args);
        Environment env = context.getEnvironment();
        String port = env.getProperty("server.port", "8082");
        
        System.out.println("========================================");
        System.out.println("   商品服务启动成功！端口：" + port);
        System.out.println("========================================");
    }
}