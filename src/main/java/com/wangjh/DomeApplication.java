package com.wangjh;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
@MapperScan("com.wangjh.mapper")
public class DomeApplication {
    public static void main(String[] args) {
        SpringApplication.run(DomeApplication.class,args);
    }
}
