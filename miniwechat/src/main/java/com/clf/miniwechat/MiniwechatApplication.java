package com.clf.miniwechat;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@MapperScan(basePackages = "com.clf.miniwechat.dao")
@ComponentScan(basePackages = {"com.clf.miniwechat", "org.n3r.idworker"})
public class MiniwechatApplication {

    public static void main(String[] args) {
        SpringApplication.run(MiniwechatApplication.class, args);
    }

}
