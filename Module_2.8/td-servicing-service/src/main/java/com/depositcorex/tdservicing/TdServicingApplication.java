package com.depositcorex.tdservicing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(exclude = { UserDetailsServiceAutoConfiguration.class })
@EnableDiscoveryClient
@EnableFeignClients
@EnableScheduling
public class TdServicingApplication {
    public static void main(String[] args) {
        SpringApplication.run(TdServicingApplication.class, args);
    }
}
