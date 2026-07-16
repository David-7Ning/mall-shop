package com.shopmall.admin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@MapperScan("com.shopmall.admin.mapper")
public class AdminApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(AdminApplication.class, args);
        Environment env = context.getEnvironment();
        String port = env.getProperty("server.port", "8086");
        
        System.out.println("========================================");
        System.out.println("   后台管理服务启动成功！端口：" + port);
        System.out.println("========================================");
    }
}