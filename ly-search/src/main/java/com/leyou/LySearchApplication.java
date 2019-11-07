package com.leyou;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @version V1.0
 * @author: lg9288
 * @date: 2019/11/7 20:12
 * @description:
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class LySearchApplication {
    public static void main(String[] args) {
        SpringApplication.run(LySearchApplication.class,args);
    }
}
